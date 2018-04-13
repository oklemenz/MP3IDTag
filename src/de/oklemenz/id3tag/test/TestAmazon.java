package de.oklemenz.id3tag.test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import de.oklemenz.id3tag.amazon.SignedRequestsHelper;

public class TestAmazon {

    private static String accessKey = "1CYQ3TGT6QDY5EWY9382";
    private static String secretKey = "1pK/0lVLKa1BmuaPI/zvuausPjjvy08WtXvogU43";
    private static String endPoint  = "webservices.amazon.com"; // "ecs.amazonaws.com";

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("SearchIndex", "All");
            parameters.put("Keywords", "Harry%20Potter"); 
            parameters.put("ResponseGroup", "Medium");
            String resultXML = callAWS("ItemSearch", parameters);
            printXML(resultXML);

            Element itemSearchResponse = (Element) getDocument(resultXML)
                    .getElementsByTagName("ItemSearchResponse").item(0);
            Element itemGroup = (Element) itemSearchResponse
                    .getElementsByTagName("Items").item(0);
            NodeList items = itemGroup.getElementsByTagName("Item");
            
            for (int i = 0; i < items.getLength(); i++) {
                
                Element item = (Element) items.item(i);
                Element asin = (Element) item.getElementsByTagName("ASIN")
                        .item(0);
                Text text = (Text)asin.getFirstChild();
                String itemID = text.getData();
                parameters.clear();
                
                parameters.put("ItemId", itemID);
                parameters.put("ResponseGroup", "Images");
                resultXML = callAWS("ItemLookup", parameters);
                printXML(resultXML);

                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String callAWS(String operation,
            Map<String, String> parameters) {
        try {

            SignedRequestsHelper helper = SignedRequestsHelper.getInstance(
                    endPoint, accessKey, secretKey);
            URL url = new URL(helper.sign(parameters, operation));
            System.out.println(url);

            URLConnection connection = url.openConnection();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[1000];
            int amount = 0;
            while (amount != -1) {
                result.write(buffer, 0, amount);
                amount = input.read(buffer);
            }
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void printXML(String xml) {
        try {
            Document doc = getDocument(xml);
            OutputFormat format = new OutputFormat(doc);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(doc);
            System.out.println(out.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Document getDocument(String xml) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}