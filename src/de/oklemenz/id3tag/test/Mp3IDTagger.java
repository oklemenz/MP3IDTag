package de.oklemenz.id3tag.test;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.farng.mp3.AbstractMP3FragmentBody;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.filename.FilenameTag;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.FrameBodyAPIC;
import org.farng.mp3.id3.FrameBodyTRCK;
import org.farng.mp3.id3.ID3v2_3;
import org.farng.mp3.id3.ID3v2_3Frame;

public class Mp3IDTagger {

	public static String startDirectoryPath = "d:\\music";

	public static String VERSION = "V1.2";

	private static String PICTURE_TAG = "APIC\0";
    private static String TRACK_TAG   = "TRCK";
    
	private static String PICTURE_DATA_TAG = "Picture Data";
	private static String MIME_TAG = "MIME Type";

	public static void main(String[] args) throws IOException, TagException {
		File startDirectory = new File(startDirectoryPath);
		if (startDirectory.isDirectory()) {
			for (File bandFile : startDirectory.listFiles()) {
				if (bandFile.isDirectory()) {
					String bandName = bandFile.getName();
					for (File albumFile : bandFile.listFiles()) {
						String albumName = albumFile.getName();
						File imageFile = getImageFile(albumFile);
						byte[] imageBytes = getImageBytes(imageFile);
						String year = getYear(imageFile);
                        if (year == null) {
                            System.out.println("Year missing for " + bandName + " - " + albumName);
                            year = "";
                        }                        
						if (imageBytes != null) {
						        for (File songFile : albumFile.listFiles()) {
								if (songFile.isDirectory()) {
							        for (File cdFile : songFile.listFiles()) {
                                        String filename = songFile.getName();
							            if (filename.toLowerCase().endsWith(".mp3")) {
                                            String songName = getSong(cdFile);
                                            String track = getTrack(cdFile);
    										MP3File mp3File = new MP3File(cdFile);
    										writeYear(mp3File, year);
    										writeTrack(mp3File, track);
    										writeGenre(mp3File, "17"); // Rock
    										writeTitle(mp3File, songName);
    										writeArtist(mp3File, bandName);
    										writeAlbum(mp3File, albumName);
    										writeComment(mp3File, "");
    										writeImage(mp3File, imageBytes);
    										mp3File.save(2);
                                        }
									}						
								} else {
                                    String filename = songFile.getName();
                                    if (filename.toLowerCase().endsWith(".mp3")) {
                                        String songName = getSong(songFile);
                                        String track = getTrack(songFile);
    									MP3File mp3File = new MP3File(songFile);
    									writeYear(mp3File, year);
    									writeTrack(mp3File, track);
    									writeGenre(mp3File, "17"); // Rock
    									writeTitle(mp3File, songName);
    									writeArtist(mp3File, bandName);
    									writeAlbum(mp3File, albumName);
    									writeComment(mp3File, "");
    									writeImage(mp3File, imageBytes);
    									mp3File.save(2);
                                    }
								}
							}
						} else {
							System.out.println("Image not written for band " + bandName + " album " + albumName);
						}
					}
				}
			}
		}
	}

	public static File getImageFile(File directory) {
		for (File file : directory.listFiles()) {
			String filename = file.getName().toLowerCase();
			if (filename.startsWith("00. ") && filename.endsWith(".jpg")) {
				return file;				
			}
		}
		return null;
	}

	public static String getSong(File file) {
		String filename = file.getName();
		int i = filename.indexOf("-");
		int j = filename.lastIndexOf(".");
		return filename.substring(i+1, j).trim();
	}

	public static String getYear(File file) {
		String filename = file.getName();
		int i = filename.lastIndexOf("(");
        if (i > 0) {
            return filename.substring(i+1, i+5).trim();            
        }
        return null;
	}

	public static String getTrack(File file) {
		String filename = file.getName();
		int i = filename.indexOf(".");
		return filename.substring(0, i).trim();
	}

	public static byte[] getImageBytes(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] imageBytes = new byte[fis.available()];
        fis.read(imageBytes);
        return imageBytes;
	}

	private static void writeTrack(MP3File mp3File, String track) {
        AbstractID3v2Frame frame = null;
        AbstractMP3FragmentBody frameBody = null;
        AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
        if (id3v2 == null) {
            id3v2 = new ID3v2_3();
            frame = new ID3v2_3Frame();
            frameBody = new FrameBodyTRCK();
            frame.setBody(frameBody);
            id3v2.setFrame(frame);
            mp3File.setID3v2Tag(id3v2);
        } else {
            frame = id3v2.getFrame(TRACK_TAG);
            if (frame == null) {
                frame = new ID3v2_3Frame();
                frameBody = new FrameBodyTRCK();
                frame.setBody(frameBody);
                id3v2.setFrame(frame);
            } else {
                frameBody = frame.getBody();
            }
        }
        frameBody.setObject("Text", track);
	}

	private static void writeYear(MP3File mp3File, String year) {
        FilenameTag filenameTag = mp3File.getFilenameTag();
        filenameTag.setYearReleased(year);
	}

	private static void writeGenre(MP3File mp3File, String genre) {
        FilenameTag filenameTag = mp3File.getFilenameTag();
        filenameTag.setSongGenre(genre);
	}

	private static void writeComment(MP3File mp3File, String comment) {
        FilenameTag filenameTag = mp3File.getFilenameTag();
        filenameTag.setSongComment(comment);
	}

	private static void writeTitle(MP3File mp3File, String title) {
		FilenameTag filenameTag = mp3File.getFilenameTag();
        filenameTag.setSongTitle(title);
	}

	private static void writeArtist(MP3File mp3File, String artist) {
        FilenameTag filenameTag = mp3File.getFilenameTag();
		filenameTag.setLeadArtist(artist);
	}

	private static void writeAlbum(MP3File mp3File, String album) {
        FilenameTag filenameTag = mp3File.getFilenameTag();
        filenameTag.setAlbumTitle(album);
	}

	private static void writeImage(MP3File mp3File, byte[] imageBytes) {
        AbstractID3v2Frame frame = null;
        AbstractMP3FragmentBody frameBody = null;
        AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
        if (id3v2 == null) {
            id3v2 = new ID3v2_3();
            frame = new ID3v2_3Frame();
            frameBody = new FrameBodyAPIC();
            frame.setBody(frameBody);
            id3v2.setFrame(frame);
            mp3File.setID3v2Tag(id3v2);
        } else {
            frame = id3v2.getFrame(PICTURE_TAG);
            if (frame == null) {
                frame = new ID3v2_3Frame();
                frameBody = new FrameBodyAPIC();
                frame.setBody(frameBody);
                id3v2.setFrame(frame);
            } else {
                frameBody = frame.getBody();
            }
        }
        frameBody.setObject(MIME_TAG, "image/jpg");
        frameBody.setObject(PICTURE_DATA_TAG, imageBytes);
	}

	private static void writeURL(MP3File mp3File, String url) {
		AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
		if (id3v2 == null) {
			id3v2 = new ID3v2_3();
			mp3File.setID3v2Tag(id3v2);
		}
		AbstractID3v2Frame apic = id3v2.getFrame(PICTURE_TAG);
		if (apic == null) {
			apic = new ID3v2_3Frame();
			id3v2.setFrame(apic);
		}
		AbstractMP3FragmentBody apicBody = apic.getBody();
		if (apicBody == null) {
			apicBody = new FrameBodyAPIC();
			apic.setBody(apicBody);
		}
		apicBody.setObject(MIME_TAG, "-->");
		apicBody.setObject(PICTURE_DATA_TAG, url);
	}

	private Image readImage(String filename) throws Exception {
		File file = new File(filename);
		MP3File mp3file = new MP3File(file);
	        AbstractID3v2 id3v2 = mp3file.getID3v2Tag();
		if (id3v2 != null) {
			AbstractID3v2Frame apic = id3v2.getFrame(PICTURE_TAG);
			if (apic != null) {
				AbstractMP3FragmentBody apicBody = apic.getBody();
				String mimeType = (String)apicBody.getObject(MIME_TAG);
				Object bytes = apicBody.getObject(PICTURE_DATA_TAG);
				if (bytes != null) {
					byte[] pictureBytes = (byte[])bytes;
					File imageFile = new File(filename + ".jpg");
					FileOutputStream fos = new FileOutputStream(imageFile);
					fos.write(pictureBytes);
					Image image = Toolkit.getDefaultToolkit().createImage(pictureBytes);
					fos.close();	
					return image;
				}
			}
		}
		return null;
	}

    private static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuffer(strLen).append(
                Character.toTitleCase(str.charAt(0))).append(str.substring(1))
                .toString();
    }

    private static String capitalize(String str, char[] delimiters) {
        if (str == null || str.length() == 0) {
            return str;
        }
        int strLen = str.length();
        StringBuffer buffer = new StringBuffer(strLen);
        int delimitersLen = 0;
        if (delimiters != null) {
            delimitersLen = delimiters.length;
        }
        boolean capitalizeNext = true;
        for (int i = 0; i < strLen; i++) {
            char ch = str.charAt(i);

            boolean isDelimiter = false;
            if (delimiters == null) {
                isDelimiter = Character.isWhitespace(ch);
            } else {
                for (int j = 0; j < delimitersLen; j++) {
                    if (ch == delimiters[j]) {
                        isDelimiter = true;
                        break;
                    }
                }
            }
            if (isDelimiter) {
                buffer.append(ch);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer.append(Character.toTitleCase(ch));
                capitalizeNext = false;
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }
}