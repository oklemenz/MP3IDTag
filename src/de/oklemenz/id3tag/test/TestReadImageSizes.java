package de.oklemenz.id3tag.test;

import gsearch.Result;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class TestReadImageSizes {

    private static File currentDir = null;

    private static final int MIN_WIDTH  = 400;
    private static final int MIN_HEIGHT = 400;
    
    private static final int WIDTH  = 500;
    private static final int HEIGHT = 500;
    
    private static final int USE_CASE_LIST   = 0;
    private static final int USE_CASE_DELETE = 1;
    private static final int USE_CASE_SEARCH = 2;
    private static final int USE_CASE_SCALE  = 3;
    
    private static final int useCase = USE_CASE_LIST;
    
    private static List<ImageInfo> images = new ArrayList<ImageInfo>();
    
	public static void main(String[] args) {
		currentDir = new File("/Volumes/PALIMPSEST/music");
		//currentDir = new File("/Users/oklemenz/Desktop/Covers");
		readFile(currentDir);
		Collections.sort(images, new ImageComperator());
		
		int i = 0;
		for (ImageInfo image : images) {
			try {
				switch (useCase) {
					case USE_CASE_LIST:
						if (image.width != WIDTH || image.height != HEIGHT) {
							System.out.println(image.name + " : (" + image.width + ", " + image.height + ")");
							i++;
						}
						break;
					case USE_CASE_DELETE:
						if (image.width == WIDTH && image.height == HEIGHT) {
							image.file.delete();
						}
						break;
					case USE_CASE_SEARCH:
						if (image.width != WIDTH || image.height != HEIGHT) {
							BufferedImage googleImage = searchGoogleImage(image.name);
							if (googleImage != null) {
								ImageIO.write(googleImage,"JPG", new File(image.file.getAbsolutePath() + ".x.jpg"));
							}
						}
						break;
					case USE_CASE_SCALE:
						if (image.width != WIDTH || image.height != HEIGHT) {
							scaleImageFile(image.file, WIDTH, HEIGHT);
						}
						break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(i + " images");
	}
	
	private static BufferedImage searchGoogleImage(String fileName) throws IOException {
		fileName = fileName.substring(3);
		int dash = fileName.indexOf("-");
		int bracket = fileName.indexOf("(");
		String artist = fileName.substring(0, dash).trim();
		String album = fileName.substring(dash+1, bracket).trim();
		MyGoogleClient gSearchClient = new MyGoogleClient();
		List<Result> gSearchResult = null;
		int start = 0;
		int minWidthDiff = Integer.MAX_VALUE;
		double minRatioDiff = Double.MAX_VALUE;
		BufferedImage googleImage = null;
		String googleImageURL = "";
		do {
			gSearchResult = gSearchClient.searchImages(artist + " " + album, start);
			for (Result result : gSearchResult) {
				BufferedImage image = loadImageByURL(result.getUnescapedUrl());
				if (image != null) {
					if (image.getWidth() >= MIN_WIDTH || image.getHeight() >= MIN_HEIGHT) {
						int widthDiff = Math.abs(image.getWidth() - WIDTH);
						double ratioDiff = Math.abs(image.getWidth() * (1.0 / image.getHeight()) - 1.0) * 100; 
						if (widthDiff + ratioDiff < minWidthDiff + minRatioDiff) {
							minWidthDiff = widthDiff;
							minRatioDiff = ratioDiff;
							googleImage = image;
							googleImageURL = result.getUnescapedUrl();
						}
					}
				}
			}
			start += MyGoogleClient.PAGE_SIZE;
			if (start > 24) {
				break;
			}
		} while (gSearchResult != null && !gSearchResult.isEmpty());
		return googleImage;
	}
	
	private static BufferedImage loadImageByURL(String urlString) throws IOException {
		try {
	        URL url = new URL(urlString);
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
	        return bufferedImage;
		} catch (RuntimeException e) {
			return null;
		}
    }
	
	private static void readFile(File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				readFile(subFile);
			}
		} else {
			if (file.getName().endsWith(".jpg") && file.getName().startsWith("00.") && !file.getName().endsWith(".x.jpg")) {
				images.add(new ImageInfo(file));
			}
		}
	}
	
    private static void scaleImageFile(File file, int width, int height) throws IOException {
    	BufferedImage sourceImage = ImageIO.read(file);
    	BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	Graphics2D g = destImage.createGraphics();
    	AffineTransform at = AffineTransform.getScaleInstance(
    							(double)width/sourceImage.getWidth(), (double)height/sourceImage.getHeight());
    	g.drawRenderedImage(sourceImage, at);
    	ImageIO.write(destImage,"JPG", file);
    }
	
	static class ImageInfo {
		
		public String name;
		public int width;
		public int height;
		public File file;
		
		public ImageInfo(File file) {
			try {
				this.file = file;
				name = file.getName();
				BufferedImage image = ImageIO.read(file);
				width = image.getWidth();
				height = image.getHeight();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static class ImageComperator implements Comparator<ImageInfo> {
		
		public ImageComperator() {
		}
		
		public int compare(ImageInfo image1, ImageInfo image2) {
			return new Integer(image1.width).compareTo(new Integer(image2.width)); 
		}
	}
}
