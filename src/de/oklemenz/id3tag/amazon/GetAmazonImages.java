package de.oklemenz.id3tag.amazon;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.oklemenz.id3tag.Mp3Tag;

/**
 * <p>Title: Advanced Programming Model</p>
 * <p>Description: Advanced Programming Model</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: OK</p>
 * @author Oliver Klemenz
 * @version 1.0
 */

public class GetAmazonImages {

    public static String accessKey = "1CYQ3TGT6QDY5EWY9382";
    public static String secretKey = "1pK/0lVLKa1BmuaPI/zvuausPjjvy08WtXvogU43";
    public static String endPoint  = "webservices.amazon.com"; //"ecs.amazonaws.com";
    
    private static String musicFilename = "D:\\oliver\\xml\\music\\music_dvd_all_15_09_06.xml";
    private static String imagePath = "D:\\temp\\Images\\";
  
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder builder;
    
    private static List<String> notFound = new ArrayList<String>();
    private static List<String> notUsed = new ArrayList<String>();
    private static List<String> noImage= new ArrayList<String>();
    private static List<String> mediumImage = new ArrayList<String>();
    private static List<String> noYear = new ArrayList<String>();
    
    private static boolean withYear = true;
    
    static {
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws SecurityException, IOException, ParserConfigurationException, SAXException {
        
        parseMusicXML(musicFilename);
    
        System.out.println();
        
        System.out.println("Not Found:\n");
        for (String string : notFound) {
            System.out.println(string);
        }
        System.out.println();

        System.out.println("Not Used:\n");
        for (String string : notUsed) {
            System.out.println(string);
        }
        System.out.println();

        System.out.println("No Image:\n");
        for (String string : noImage) {
            System.out.println(string);
        }
        System.out.println();

        System.out.println("Medium Image:\n");
        for (String string : mediumImage) {
            System.out.println(string);
        }
        System.out.println();

        System.out.println("No Year:\n");
        for (String string : noYear) {
            System.out.println(string);
        }
        System.out.println();

    }
    
    public static void parseMusicXML(String filename) throws ParserConfigurationException, SAXException, IOException {
        Document musicDocument = builder.parse( new File( filename ) );
        
        Element music = (Element)musicDocument.getElementsByTagName("music").item(0);
        NodeList bands = music.getElementsByTagName("band");
        for (int i = 0; i < bands.getLength(); i++) {
            Element band = (Element)bands.item(i);
            NamedNodeMap bandAttributes = band.getAttributes();
            Node bandName = bandAttributes.getNamedItem("name");
            NodeList albums = band.getElementsByTagName("album");
            for (int j = 0; j < albums.getLength(); j++) {
                Element album = (Element)albums.item(j);
                NamedNodeMap albumAttributes = album.getAttributes();
                Node albumTitle = albumAttributes.getNamedItem("title");
                executeAmazonSearch(bandName.getNodeValue(), albumTitle.getNodeValue(), null);
            }
        }
    }

    public static boolean executeAmazonSearch(String bandName, String albumTitle, String filePath) throws SecurityException, IOException, SAXException {
        ItemSearchRest itemSearch = new MyItemSearch(accessKey, bandName, albumTitle);
        itemSearch.issueRequest();
        if (!parseAmazonSearchResponse(itemSearch.getResponse(), bandName, albumTitle, filePath)) {
            if (!executeAmazonSimilaritySearch(bandName, albumTitle, filePath)) {
                if (filePath == null) {
                    notFound.add(bandName + " - " + albumTitle);
                    System.out.println("\t" + bandName + " - " + albumTitle);
                    new File(imagePath + bandName).mkdirs();
                    new File(imagePath + bandName + File.separatorChar + albumTitle).mkdirs();
                }
                return false;
            }
        }
        System.out.println(bandName + " - " + albumTitle);
        return true;
    }

    private static boolean executeAmazonSimilaritySearch(String bandName, String albumTitle, String filePath) throws SecurityException, IOException, SAXException {
        ItemSearchRest itemSimilaritySearch = new MyItemSimilaritySearch(accessKey, bandName, albumTitle);
        itemSimilaritySearch.issueRequest();
        return parseAmazonSearchResponse(itemSimilaritySearch.getResponse(), bandName, albumTitle, filePath);
    }
    
    private static boolean executeAmazonSimilaritySearchFull(String bandName, String albumTitle, String filePath) throws SecurityException, IOException, SAXException {
        ItemSearchRest itemSimilaritySearch = new MyItemSimilaritySearch(accessKey, bandName + " " + albumTitle);
        itemSimilaritySearch.issueRequest();
        return parseAmazonSearchResponse(itemSimilaritySearch.getResponse(), bandName, albumTitle, filePath);
    }
    
    private static boolean parseAmazonSearchResponse(String xmlResponse, String bandName, String albumTitle, String filePath) throws SAXException, IOException {
        Document searchDocument = builder.parse(new InputSource(new StringReader(xmlResponse)));
        Element itemSearchResponse = (Element)searchDocument.getElementsByTagName("ItemSearchResponse").item(0);
        Element itemGroup = (Element)itemSearchResponse.getElementsByTagName("Items").item(0);
        NodeList items = itemGroup.getElementsByTagName("Item");
        boolean found = false;
        for (int i = 0; i < items.getLength(); i++) {
            try {
                Element item = (Element)items.item(i);
                Element asin = (Element)item.getElementsByTagName("ASIN").item(0);
                Text text = (Text)asin.getFirstChild();
                String itemID = text.getData();
                Element itemAttributes = (Element)item.getElementsByTagName("ItemAttributes").item(0);
                Node artist = itemAttributes.getElementsByTagName("Artist").item(0);
                text = (Text)artist.getFirstChild();
                String artistName = text.getData();
                if (!calcLevenshtein(artistName, bandName)) {
                    if (filePath != null) {
                        notUsed.add(artistName + " / " + bandName + " - " + albumTitle);
                    }
                    continue;
                }
                Node title = itemAttributes.getElementsByTagName("Title").item(0);
                text = (Text)title.getFirstChild();
                String titleName = text.getData();
                if (!calcLevenshtein(titleName, albumTitle)) {
                    if (filePath != null) {
                        notUsed.add(artistName + " / " + bandName + " - " + titleName + " / " + albumTitle);
                    }
                    continue;
                }
                int year = executeAmazonAttributeLookup(itemID, bandName, albumTitle, i);
                if (executeAmazonImageLookup(itemID, bandName, albumTitle, i, year, filePath)) {
                    found = true;
                }
            } catch (NullPointerException npe) {
            }
        }
        return found;
    }

    private static boolean executeAmazonImageLookup(String itemID, String artist, String title, int index, int year, String filePath) throws SecurityException, IOException, SAXException {
        ItemLookupRest itemLookup = new MyItemLookup(accessKey, itemID, "Images");
        itemLookup.issueRequest();
        return parseAmazonImageLookupResponse(itemLookup.getResponse(), itemID, artist, title, index, year, filePath);
        
    }

    private static boolean parseAmazonImageLookupResponse(String xmlResponse, String itemID, String artist, String title, int index, int year, String filePath) throws SAXException, IOException {
        Document lookupDocument = builder.parse(new InputSource(new StringReader(xmlResponse)));
        Element itemLookupResponse = (Element)lookupDocument.getElementsByTagName("ItemLookupResponse").item(0);
        Element itemGroup = (Element)itemLookupResponse.getElementsByTagName("Items").item(0);
        NodeList items = itemGroup.getElementsByTagName("Item");
        boolean found = false;
        for (int i = 0; i < items.getLength(); i++) {
            try {
                Element item = (Element)items.item(i);
                Element asin = (Element)item.getElementsByTagName("ASIN").item(0);
                Text text = (Text)asin.getFirstChild();
                String itemID2 = text.getData();
                if (!itemID.equals(itemID2)) {
                    System.exit(-1);
                }
                Element image = (Element)item.getElementsByTagName("LargeImage").item(0);
                if (image == null) {
                    image = (Element)item.getElementsByTagName("MediumImage").item(0);
                    mediumImage.add(artist + " - " + title);
                    if (image == null) {
                        if (filePath == null) {
                            noImage.add(artist + " - " + title);
                            new File(imagePath + artist + File.separatorChar + title).mkdirs();
                            new File(imagePath + artist + File.separatorChar + title + File.separatorChar + year + ".txt").createNewFile();
                        }
                        continue;
                    }
                }
                Element url = (Element)image.getElementsByTagName("URL").item(0);
                text = (Text)url.getFirstChild();
                String urlName = text.getData();
                if (loadImageByURL(urlName, artist, title, index, year, filePath)) {
                    found = true;
                }
            } catch (NullPointerException npe) {
            }
        }
        return found;
    }

    private static int executeAmazonAttributeLookup(String itemID, String artist, String title, int index) throws SecurityException, IOException, SAXException {
        ItemLookupRest itemLookup = new MyItemLookup(accessKey, itemID, "ItemAttributes");
        itemLookup.issueRequest();
        return parseAmazonAttributeLookupResponse(itemLookup.getResponse(), itemID, artist, title, index);
        
    }

    private static int parseAmazonAttributeLookupResponse(String xmlResponse, String itemID, String artist, String title, int index) throws SAXException, IOException {
        Document lookupDocument = builder.parse(new InputSource(new StringReader(xmlResponse)));
        Element itemLookupResponse = (Element)lookupDocument.getElementsByTagName("ItemLookupResponse").item(0);
        Element itemGroup = (Element)itemLookupResponse.getElementsByTagName("Items").item(0);
        NodeList items = itemGroup.getElementsByTagName("Item");
        boolean found = false;
        for (int i = 0; i < items.getLength(); i++) {
            try {
                Element item = (Element)items.item(i);
                Element itemAttributes = (Element)item.getElementsByTagName("ItemAttributes").item(0);
                Element releaseDate = (Element)item.getElementsByTagName("ReleaseDate").item(0);
                Text text = (Text)releaseDate.getFirstChild();
                String releaseDateString = text.getData(); 
                String[] parts = releaseDateString.split("-"); 
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                return year;
            } catch (NullPointerException npe) {
            }
        }
        return 0;
    }
    
    private static boolean loadImageByURL(String urlName, String artist, String title, int index, int year, String filePath) throws IOException {
        URL url = new URL(urlName);
        ImageIcon imageIcon = new ImageIcon(url); 
        Image image = imageIcon.getImage();    
        BufferedImage bufferedImage = null;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage)image;
        } else {
            bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);        
            g.dispose();
        }
        return writeImageToFile(bufferedImage, artist, title, index, year, filePath);
    }

    private static boolean writeImageToFile(BufferedImage bufferedImage, String artist, String title, int index, int year, String filePath) throws IOException {
        artist = replaceForbiddenChars(artist);
        title = replaceForbiddenChars(title);
        String imageFilename = "00" + ". " + artist + " - " + title;
        if (withYear && year > 1900) {
            imageFilename += " (" + year + ")";
        } else if (withYear && year != 0) {
            noYear.add(artist + " - " + title);
        } else {
            noYear.add(artist + " - " + title);
        }
        if (index > 0) {
            imageFilename += " (" + index + ")";
        }
        imageFilename += ".jpg";
        File imageFile = null;
        if (filePath == null) {
            new File(imagePath + artist + File.separatorChar + title).mkdirs();
            imageFile = new File(imagePath + artist + File.separatorChar + title + File.separatorChar + imageFilename);
        } else {
            imageFile = new File(filePath + File.separatorChar + imageFilename);
        }
        ImageIO.write(bufferedImage, "jpg", imageFile);
        return true;
    }
    
    private static String replaceForbiddenChars(String string) {
        string = string.replace("/", "");
        string = string.replace("\\", "");
        string = string.replace(":", "");
        string = string.replace("*", "");
        string = string.replace("?", "");
        string = string.replace("\"", "");
        string = string.replace("<", "");
        string = string.replace(">", "");
        string = string.replace("|", "");
        return string;
    }

    private static int minimum (int a, int b, int c) {
        int mi;
        mi = a;
        if (b < mi) {
          mi = b;
        }
        if (c < mi) {
          mi = c;
        }
        return mi;
    }
    
    private static boolean calcLevenshtein(String s, String t) {
        int minLen = s.length() < t.length() ? s.length() : t.length();
        s = s.toUpperCase();
        t = t.toUpperCase();
        int d[][]; // matrix
        int n; // length of s
        int m; // length of t
        int i; // iterates through s
        int j; // iterates through t
        char s_i; // ith character of s
        char t_j; // jth character of t
        int cost; // cost

        // Step 1
        n = s.length();
        m = t.length();
        if (n == 0) {
            return m <= (minLen / 4);
        }
        if (m == 0) {
            return n <= (minLen / 4);
        }
        d = new int[n + 1][m + 1];
        // Step 2
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        // Step 3
        for (i = 1; i <= n; i++) {
            s_i = s.charAt(i - 1);
            // Step 4
            for (j = 1; j <= m; j++) {
                t_j = t.charAt(j - 1);
                // Step 5
                if (s_i == t_j) {
                    cost = 0;
                } else {
                    cost = 1;
                }
                // Step 6
                d[i][j] = minimum(d[i - 1][j] + 1, d[i][j - 1] + 1,
                        d[i - 1][j - 1] + cost);
            }
        }
        // Step 7
        return d[n][m] <= (minLen / 4);
    }
}