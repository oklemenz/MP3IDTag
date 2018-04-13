package de.oklemenz.id3tag;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.TagOptionSingleton;
import org.farng.mp3.id3.AbstractID3v1;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.AbstractID3v2FrameBody;
import org.farng.mp3.id3.FrameBodyAPIC;
import org.farng.mp3.id3.FrameBodyTALB;
import org.farng.mp3.id3.FrameBodyTCON;
import org.farng.mp3.id3.FrameBodyTIT2;
import org.farng.mp3.id3.FrameBodyTPE1;
import org.farng.mp3.id3.FrameBodyTPOS;
import org.farng.mp3.id3.FrameBodyTRCK;
import org.farng.mp3.id3.FrameBodyTYER;
import org.farng.mp3.id3.ID3v1_1;
import org.farng.mp3.id3.ID3v2_3;
import org.farng.mp3.id3.ID3v2_3Frame;
import org.farng.mp3.lyrics3.AbstractLyrics3;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.oklemenz.id3tag.amazon.GetAmazonImages;

public class Mp3Tag extends JFrame{

	private static final long serialVersionUID = 5772601537527598505L;
	
	private static final int STATUS_NONE  = 0;
    private static final int STATUS_MUSIC = 1;
    private static final int STATUS_ALBUM = 2;
    private static final int STATUS_BAND  = 3;
    private static final int STATUS_SONG  = 4;
    
    private static final int ACTION_UPDATE = 1;
    private static final int ACTION_IMAGE  = 2;
    
	private static final String V1_ARTIST   = "artist";
	private static final String V1_ALBUM    = "album";
	private static final String V1_TRACK    = "track";
	private static final String V1_TITLE    = "title";
	private static final String V1_YEAR     = "year";
	private static final String V1_GENRE    = "genre";
	private static final String V1_COMMENT  = "comment";

	private static final String V2_ARTIST   = "TPE1";
	private static final String V2_ALBUM    = "TALB";
	private static final String V2_TRACK    = "TRCK"; // 1/2
	private static final String V2_CD       = "TPOS"; // 1/2
	private static final String V2_TITLE    = "TIT2";
	private static final String V2_YEAR     = "TYER";
	private static final String V2_GENRE    = "TCON";
	private static final String V2_COMPOSER = "TCOM";
	private static final String V2_COMMENT  = "COMM";
	private static final String V2_LYRICS   = "SYLT"; // or USLT
	private static final String V2_PICTURE  = "APIC";

	private static final int GRUNGE      = 6;
	private static final int METAL       = 9;
	private static final int DEATH_METAL = 22;
	private static final int ROCK        = 17;
	private static final int ALTERN_ROCK = 40;
	private static final int HARD_ROCK   = 79;
    
	private static final String TEXT_ENCODING_TAG = "Text encoding";
	private static final String TEXT_TAG          = "Text";
    private static final String TEXT_LANG         = "Language";
    private static final String TEXT_DESCRIPTION  = "Short content descrip.";
    private static final String TEXT_TEXT         = "The actual text";
    
    private static final String MIME_TYPE_TAG     = "MIME Type";
    private static final String PICTURE_TYPE      = "Picture type";
    private static final String DESCRIPTION_TAG   = "Description";
    private static final String PICTURE_DATA_TAG  = "Picture Data";
	
	private static final byte TEXT_ENCODING       = 0;
	private static final byte ALBUM_COVER         = 0;
	private static final String IMAGE_JPG         = "image/jpg";
	private static final String IMAGE_REF         = "-->";

	private static final int SAVE_WRITE     = 1;
 	private static final int SAVE_OVERWRITE = 2;
	private static final int SAVE_APPEND    = 3;

    private boolean withMax         = false;
    private int imageWidth          = 500;
    private int imageHeight         = 500;
    
    private int updates             = 0;
    private int errors              = 0;
    private int missImage           = 0;
    private int missYear            = 0;
    private int possibleApostrophe  = 0;
    
    private File currentDir         = null;
    private String sourceFilePath   = "";
    private String targetFilePath   = "";
    
    private int status = STATUS_NONE;
    
    private static Map<String, String> missingApostropheCase = new HashMap<String, String>();
    private static Map<String, String> possibleApostropheCase = new HashMap<String, String>();
    
    static {
    	// Missing
    	fillMissing("Im", "I'm");
    	fillMissing("Ive", "I've");
    	fillMissing("Youre", "You're");
    	fillMissing("Youll", "You'll");
    	fillMissing("Youd", "You'd");
    	fillMissing("Youve", "You've");
    	fillMissing("Hes", "He's");
    	fillMissing("Hed", "He'd");
    	fillMissing("Shes", "She's");
    	fillMissing("Itll", "It'll");
    	fillMissing("Itd", "It'd");
    	fillMissing("Wed", "We'd");
    	fillMissing("Weve", "We've");
    	fillMissing("Theyre", "They're");
    	fillMissing("Theyll", "They'll");
    	fillMissing("Theyd", "They'd");
    	fillMissing("Theyve", "They've");
    	fillMissing("Theres", "There's");
    	fillMissing("Therell", "There'll");
    	fillMissing("Thered", "There'd");
    	fillMissing("Thats", "That's");
    	fillMissing("Thatll", "That'll");
    	fillMissing("Thatd", "That'd");
    	fillMissing("Arent", "Aren't");
    	fillMissing("Cant", "Can't");
    	fillMissing("Couldnt", "Couldn't");
    	fillMissing("Couldve", "Could've");
    	fillMissing("Didnt", "Didn't");
    	fillMissing("Doesnt", "Doesn't");
    	fillMissing("Dont", "Don't");
    	fillMissing("Hadnt", "Hadn't");
    	fillMissing("Hasnt", "Hasn't");
    	fillMissing("Isnt", "Isn't");
    	fillMissing("Mustnt", "Mustn't");
    	fillMissing("Neednt", "Needn't");
    	fillMissing("Shouldnt", "Shouldn't");
    	fillMissing("Shouldve", "Should've");
    	fillMissing("Wasnt", "Wasn't");
    	fillMissing("Werent", "Weren't");
    	fillMissing("Wont", "Won't");
    	fillMissing("Wouldnt", "Wouldn't");
    	fillMissing("Wouldve", "Would've");
    	fillMissing("Aint", "Ain't");
    	fillMissing("Whats", "What's");
       	fillMissing("Wheres", "Where's");
    	fillMissing("Whos", "Whos's");
      	// Possible
    	fillPossible("Ill", "I'll");
    	fillPossible("Id", "I'd");
    	fillPossible("Hell", "He'll");
    	fillPossible("Shed", "She'd");
    	fillPossible("Shell", "She'll");
    	fillPossible("Its", "It's");
    	fillPossible("Were", "We're");
    	fillPossible("Well", "We'll");
    }
    
    private static void fillMissing(String a, String b) {
    	missingApostropheCase.put(a, b);
    }
    
    private static void fillPossible(String a, String b) {
    	possibleApostropheCase.put(a, b);
    }
    
    public Mp3Tag() {
        
        currentDir = new File("/Users/oklemenz/Documents/New Music");        
        
        setTitle("Java Mp3 ID3 Tagger");
        setLayout(new BorderLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 1));
                        
        JButton button = new JButton("Remove ID3 Tag");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Folder", true);
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    possibleApostrophe = 0;
                    removeMp3Tags(file);
                }
            }
        });
        panel.add(button);
        
        button = new JButton("Update ID3 Tag of Music");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Music", true);
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    possibleApostrophe = 0;
                    status = STATUS_MUSIC;
                    updateMusic(file, ACTION_UPDATE);
                }
            }
        });
        panel.add(button);

        button = new JButton("Update ID3 Tag of Band");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Band");
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    possibleApostrophe = 0;
                    status = STATUS_BAND;
                    updateArtist(file, ACTION_UPDATE);
                }
            }
        });
        panel.add(button);

        button = new JButton("Update ID3 Tag of Album");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Album");
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    possibleApostrophe = 0;
                    status = STATUS_ALBUM;
                    updateAlbum(file, null, ACTION_UPDATE);
                }
            }
        });
        panel.add(button);

        button = new JButton("Update ID3 Tag of Song");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Song");
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    possibleApostrophe = 0;
                    status = STATUS_SONG;
                    updateSong(file, ACTION_UPDATE);
                }
            }
        });
        panel.add(button);

        button = new JButton("Capitalize Files");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Directory", true);
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    sourceFilePath = file.getAbsolutePath();
                    targetFilePath = file.getAbsolutePath() + "_uc";
                    traverseDirectory(file);
                    JOptionPane.showMessageDialog(Mp3Tag.this, "" + updates + " updated content directories", "Processing finsihed!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        panel.add(button);
  
        button = new JButton("Rename Files");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Album");
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    possibleApostrophe = 0;
                    status = STATUS_ALBUM;
                    renameFiles(file, true);
                }
            }
        });
        panel.add(button);
        
        button = new JButton("List Other Size Images");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Music", true);
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    status = STATUS_MUSIC;
                    listOtherSizeImages(file);
                }
            }
        });
        panel.add(button);
        
        button = new JButton("List Wrong File Names");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Music", true);
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    status = STATUS_MUSIC;
                    listWrongFileNames(file);
                }
            }
        });
        panel.add(button);
        
        button = new JButton("Write Music XML");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File file = showFileChooser("Music", true);
                if (file != null) {
                    updates = 0;
                    errors = 0;
                    missImage = 0;
                    missYear = 0;
                    status = STATUS_MUSIC;
                    writeXML(file);
                }
            }
        });
        panel.add(button);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        add(panel, BorderLayout.CENTER);
        Dimension dimension = getToolkit().getScreenSize();
        setSize(250, 300);
        setLocation((dimension.width - getWidth()) / 2, (dimension.height - getHeight()) / 2);
        
        setVisible(true);
    }
    
	public static void main(String[] agrs) throws FileNotFoundException, IOException, TagException {
        new Mp3Tag();
	}

	private File showFileChooser(String source, boolean parent) {
		JFileChooser chooser = new JFileChooser();
		if (parent) {
			chooser.setCurrentDirectory(currentDir.getParentFile());
		} else {
			chooser.setCurrentDirectory(currentDir);
		}
		chooser.setDialogTitle("Choose the " + source + " for MP3 tagging");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.exists()) {
				if (file.isDirectory()) {
					currentDir = file;
				} else {
					currentDir = file.getParentFile();
				}
				return file;
			}
			currentDir = chooser.getCurrentDirectory();
			return currentDir;
		}
		return null;
	}
	
    private File showFileChooser(String source) {
    	return showFileChooser(source, false);
    }
    
    private void updateMusic(File musicFile, int action) {
        if (musicFile.isDirectory()) {
            for (File artistFile : musicFile.listFiles()) {
                updateArtist(artistFile, action);
            }
        }
        if (status == STATUS_MUSIC) {
            showMessage();
        }
    }

    private void updateArtist(File artistFile, int action) {
        if (artistFile == null) {
            return;
        }
        if (artistFile.isDirectory()) {
            for (File albumFile : artistFile.listFiles()) {
            	if (!getFilenameWOExtension(albumFile.getName()).equals("")) {
            		updateAlbum(albumFile, null, action);
            	}
            }
        }
        if (status == STATUS_BAND) {
            showMessage();
        }
    }

    private void updateAlbum(File albumFile, String singleSongFilename, int action) {
        String album = "";
        String artist = "";
        String title = "";
        String year = "";
        String genre = "" + ALTERN_ROCK;
        
        File imageFile = null;
        byte[] imageBytes = null;
        try {
            if (albumFile == null) {
                return;
            }
            artist = albumFile.getParentFile().getName();
            artist = capitalizeText(artist); 
            album = albumFile.getName();
            album = capitalizeText(album);
            try {
                Integer.parseInt(album.substring(0, 4));
                if (album.substring(4, 7).trim().equals("-")) {
                    album = album.substring(7);
                }
            } catch (NumberFormatException efe) {
            }
            if (action == ACTION_IMAGE) {
                getImage(artist, album, albumFile.getAbsolutePath());
                return;
            }
            int maxTrack = 0;
            int maxCD = 0;
            for (File songFile : albumFile.listFiles()) {
            	if (getFilenameWOExtension(songFile.getName()).equals("")) {
            		continue;
            	}
              	String filename = albumFile.getName();
            	if (filename.toLowerCase().endsWith(".mp3")) {
            		maxTrack++;
            	}
            	if (songFile.isDirectory()) {
            		maxCD++;
            	}
            }
            for (File songFile : albumFile.listFiles()) {
            	if (getFilenameWOExtension(songFile.getName()).equals("")) {
            		continue;
            	}
                if (songFile.isDirectory()) {
                    // Search image file, if not yet found for album
                    if (action == ACTION_UPDATE) {
                        imageFile = getImageFile(songFile);
                        if (imageFile != null) {
                        	scaleImage(imageFile, imageWidth, imageHeight); 
	                        imageBytes = getImageBytes(imageFile);
	                        if (imageBytes == null) {
	                            missImage++;
	                            System.out.println("Warning: Image missing " + artist + " - " + album);
	                        }
	                        year = getYear(imageFile);
	                        if (year == null) {
	                            missYear++;
	                            System.out.println("Warning: Year missing " + artist + " - " + album);
	                            year = "";
	                        }
                        }
                    }
                    String cd = songFile.getName().substring(2);   
                    maxTrack = 0;
                    for (File cdFile : songFile.listFiles()) {
                    	if (getFilenameWOExtension(cdFile.getName()).equals("")) {
                    		continue;
                    	}
                    	String filename = cdFile.getName();
                    	if (filename.toLowerCase().endsWith(".mp3")) {
                    		maxTrack++;
                    	}
                    }
                    for (File cdFile : songFile.listFiles()) {
                    	if (getFilenameWOExtension(cdFile.getName()).equals("")) {
                    		continue;
                    	}
                        String filename = cdFile.getName();
                        if (filename.toLowerCase().endsWith(".mp3") && 
                           (singleSongFilename == null || filename.equals(singleSongFilename))) {
                            title = getSong(cdFile);
                            title = capitalizeText(title);
                            String track = getTrack(cdFile);
                            if (action == ACTION_UPDATE) {
                                updateMp3(cdFile, title, artist, album, track, ""+maxTrack, cd, ""+maxCD, year, genre, imageBytes);
                            }
                        }
                    }                       
                } else {
                    // Search image file, if not yet found for album
                    if (imageFile == null && action == ACTION_UPDATE) {
                        imageFile = getImageFile(albumFile);
                        scaleImage(imageFile, imageWidth, imageHeight);                         
                        imageBytes = getImageBytes(imageFile);                
                        if (imageBytes == null) {
                            missImage++;
                            System.out.println("Warning: Image missing " + artist + " - " + album);
                        }
                        year = getYear(imageFile);
                        if (year == null) {
                            missYear++;
                            System.out.println("Warning: Year missing " + artist + " - " + album);
                            year = "";
                        }                        
                    }
                    String filename = songFile.getName();
                    if (filename.toLowerCase().endsWith(".mp3") && 
                       (singleSongFilename == null || filename.equals(singleSongFilename))) {
                        title = getSong(songFile);
                        title = capitalizeText(title);
                        String track = getTrack(songFile);
                        if (action == ACTION_UPDATE) {
                            updateMp3(songFile, title, artist, album, track, ""+maxTrack, "1", "1", year, genre, imageBytes);
                        }
                    }
                }
            }
        } catch (NullPointerException npe) {
            errors++;
            System.out.println("Error: " + artist + " - " + album + " - " + title);
            return;
        } catch (Exception e) {
            errors++;
            e.printStackTrace();
            System.out.println("Error: " + artist + " - " + album + " - " + title);
        }
        if (status == STATUS_ALBUM) {
            showMessage();
        }
    }
    
    private void updateSong(File songFile, int action) {
        File albumFolder = songFile.getParentFile(); 
        if (isCDFolder(albumFolder)) {
            albumFolder = albumFolder.getParentFile();
        }    
        updateAlbum(albumFolder, songFile.getName(), action);
        if (status == STATUS_SONG) {
            showMessage();
        }
    }

    private void showMessage() {
    	String text = "";
    	if (updates > 0) {
    		text += updates + " Updates";
    	}
    	if (missImage > 0) {
    		if (!text.equals("")) {
    			text += ", ";
    		}
    		text += missImage + " Missing Covers";
    	}
    	if (missYear > 0) {
    		if (!text.equals("")) {
    			text += ", ";
    		}
    		text += missYear + " Missing Years";
    	}
    	if (possibleApostrophe > 0) {
    		if (!text.equals("")) {
    			text += ", ";
    		}
    		text += possibleApostrophe + " Possible Apostrophes";
    	}
    	if (errors > 0) {
    		if (!text.equals("")) {
    			text += ", ";
    		}
    		text += errors + " Errors";
    	}

        JOptionPane.showMessageDialog(this, "Processing Finished. " + text, "Processing finsihed!", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private boolean isCDFolder(File directory) {
        if (directory.isDirectory()) {
            String filename = directory.getName();
            return filename.toUpperCase().matches("CD[1-9]*");
        }
        return false;
    }
    
    private void getImage(String artist, String album, String filePath) throws IOException, SecurityException, SAXException{
        if (GetAmazonImages.executeAmazonSearch(artist, album, filePath)) {
            updates++;
        } else {
            errors++;
        }
    }
   
    private void removeMp3Tags(File file) {
    	removeMp3TagsRecursive(file);
    	showMessage();
    }
    
    private void removeMp3TagsRecursive(File file) {
    	if (file.isDirectory()) {
    		for (File dirFile : file.listFiles()) {
    			removeMp3TagsRecursive(dirFile);
    		}
    	} else {
			String filename = file.getName();
			if (getFileExtension(filename).equalsIgnoreCase("mp3")) {
	    		try {
	    			clearMp3(file);
					MP3File mp3File = new MP3File(file);
					AbstractID3v1 id3v1 = mp3File.getID3v1Tag();
			        if (id3v1 != null) {
			            mp3File.delete(id3v1);
			        }
			        AbstractLyrics3 lyrics3 = mp3File.getLyrics3Tag();
			        if (lyrics3 != null) {
			            mp3File.delete(lyrics3);
			        }
			        AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
			        if (id3v2 != null) {
			            mp3File.delete(id3v2);
			        }
			        mp3File.save();
				} catch (Exception e) {
					errors++;
		            e.printStackTrace();
		            System.out.println("Error: " + filename);
	    		}
			}
    	}
    }
    
    private void updateMp3(File file, String title, String artist, String album, String track, String maxTrack, String cd, String maxCD, String year, String genre, byte[] imageBytes) throws IOException, TagException {
        MP3File mp3File = new MP3File(file);

        // IDv1 tags
        AbstractID3v1 id3v1 = mp3File.getID3v1Tag();
        if (id3v1 == null) {
            id3v1 = new ID3v1_1();
            mp3File.setID3v1Tag((ID3v1_1)id3v1);
        }
        id3v1.setSongTitle(title);
        id3v1.setLeadArtist(artist);
        id3v1.setAlbumTitle(album);
        id3v1.setYearReleased(year);
        id3v1.setSongComment("");
        id3v1.setSongGenre(genre);
        id3v1.setTrackNumberOnAlbum(track);

        // IDv2 tags
        AbstractID3v2 id3v2 = mp3File.getID3v2Tag();
        if (id3v2 == null) {
            id3v2 = new ID3v2_3();
            mp3File.setID3v2Tag((ID3v2_3)id3v2);
        }
        setSongTitle(id3v2, title);
        setLeadArtist(id3v2, artist);
        setAlbumTitle(id3v2, album);
        setYearReleased(id3v2, year);
        setSongGenre(id3v2, genre);
        if (withMax) {
        	maxTrack = addLeadingZero(maxTrack);
        	setTrackNumberOnAlbum(id3v2, track, maxTrack);
        } else {
        	setTrackNumberOnAlbum(id3v2, track);
        }
        if (withMax) {
        	setCDNumberOnAlbum(id3v2, cd, maxCD);
        } else {
        	setCDNumberOnAlbum(id3v2, cd);
        }
        setAlbumCover(id3v2, imageBytes);
        
        removeSongComment(id3v2);
        removeSongLyric(id3v2);
        removeAuthorComposer(id3v2);

        TagOptionSingleton.getInstance().setOriginalSavedAfterAdjustingID3v2Padding(false);
        
        mp3File.save(SAVE_WRITE);
        updates++;
    	checkMissingApostropheCase(artist + " " + artist + " " + title);
    	System.out.println("Updated: " + album + " - " + artist + " - " + title);
    }
    
    private void clearMp3(File file) throws IOException, TagException {
    	updateMp3(file, "", "", "", "0", "0", "0", "0", "", "0",  new byte[0]);
    }
    
    private void renameFiles(File albumFile, boolean simulate) {
    	if (albumFile.isDirectory()) {
    		int mp3Count = 0;
    		for (File songFile : albumFile.listFiles()) {
    			String filename = songFile.getName();
    			if (getFileExtension(filename).equalsIgnoreCase("mp3")) {
    				mp3Count++;
    			}
    		}
    		if (mp3Count > 0) {
    			List<String> newFilenames = new ArrayList<String>();
	            for (File songFile : albumFile.listFiles()) {
	            	String filename = songFile.getName();
	            	if (getFileExtension(filename).equalsIgnoreCase("mp3")) {
		            	String title = filename.substring(0, filename.lastIndexOf("."));
		            	String[] parts = title.split("-");
		            	if (parts.length >= 3) {
			            	int number = Integer.parseInt(parts[0]);
			            	String numberString = String.format("%0"+(int)(Math.floor(Math.log10(mp3Count))+1)+"d", number);
			            	String band = replaceMissingApostropheCase(toTitleCase(parts[1]));
			            	String song = replaceMissingApostropheCase(toTitleCase(parts[2]));
			            	String newFilename = numberString + ". " + band + " - " + song + ".mp3";
			            	if (simulate) {
			            		newFilenames.add(newFilename);
			            	} else {
			            		System.out.println(newFilename);
			            		File renameFile = new File(songFile.getParent() + File.separatorChar + newFilename);
			            		songFile.renameTo(renameFile);
			            	}
		            	} else if (!title.equals("")) {
		            		System.out.println("Error: " + filename);
		            		errors++;
		            	}
	            	}
	            }
	            if (simulate && newFilenames.size() > 0) {
	            	String text = "";
	            	for (String newFilename : newFilenames) {
	            		text += newFilename + "\n";
	            	}
				    int result = JOptionPane.showConfirmDialog(this, text, "Rename files?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				    if (result == JOptionPane.YES_OPTION) {
				    	renameFiles(albumFile, false);
				    	return;
				    }
				    
	            }
    		}
    	}
    	if (status == STATUS_ALBUM) {
            showMessage();
        }
    }
    
    private String replaceMissingApostropheCase(String name) {
    	for (Map.Entry<String, String> entry : missingApostropheCase.entrySet()) {
    		name = name.replaceAll(entry.getKey() + " ", entry.getValue() + " ");
    	}
    	for (Map.Entry<String, String> entry : possibleApostropheCase.entrySet()) {
    		if (name.contains(entry.getKey() + " ")) {
    			possibleApostrophe++;
    			System.out.println("Possible Apostrophe: " + name + " (" + entry.getKey() + " -> " + entry.getValue() + ")");
    		}
    	}
    	return name;
    }
    
    private boolean checkMissingApostropheCase(String name) {
    	boolean found = false;
    	for (Map.Entry<String, String> entry : missingApostropheCase.entrySet()) {
    		if (name.contains(entry.getKey() + " ")) {
    			possibleApostrophe++;
    			System.out.println("Missing Apostrophe: " + name + " (" + entry.getKey() + " -> " + entry.getValue() + ")");
    			found = true;
    		}    		
    	}
    	for (Map.Entry<String, String> entry : possibleApostropheCase.entrySet()) {
    		if (name.contains(entry.getKey() + " ")) {
    			possibleApostrophe++;
    			System.out.println("Possible Apostrophe: " + name + " (" + entry.getKey() + " -> " + entry.getValue() + ")");
    			found = true;
    		}
    	}
    	return found;
    }
    
    private static String getFileExtension(String filename) {
    	int pos = filename.lastIndexOf(".");
    	if (pos >= 0) {
    		return filename.substring(pos+1);	
    	}
    	return "";
    }  
    
    private static String getFilenameWOExtension(String filename) {
    	int pos = filename.lastIndexOf("."); 
    	if (pos > 0) {
    		return filename.substring(0, filename.lastIndexOf("."));
    	} else if (pos == -1) {
    		return filename;
    	}
    	return "";    	
    }  
    
    private static String toTitleCase(String name) {
    	String result = "";
    	String[] parts = name.split("_");
    	for (String part : parts) {
    		 if (part.length() == 0) {
	            part = "";
    		 } else if (part.length() == 1) {
    			part = "" + Character.toTitleCase(part.charAt(0));
    		 } else {
	            part = "" + Character.toTitleCase(part.charAt(0)) + part.substring(1);
    		 }
    		 result += part + " ";
    	}
    	result = result.trim();
    	result = result.replaceAll("\\s+", " ");
    	return toUpperCaseBrackets(result); 
    }
    
    private static String toUpperCaseBrackets(String name) {
    	String result = "";
    	String[] parts = name.split("\\(");
    	int i = 0;
    	for (String part : parts) {
    		 if (part.length() == 0) {
	            part = "";
    		 } else if (part.length() == 1) {
    			part = "" + Character.toTitleCase(part.charAt(0));
    		 } else {
	            part = "" + Character.toTitleCase(part.charAt(0)) + part.substring(1);
    		 }
    		 if (i > 0) {
    			 result += "(";
    		 }
    		 result += part;
    		 i++;
    	}
    	return result; 
    }
    
    private void listOtherSizeImages(File musicFile) {
    	checkFileImageSize(musicFile);
    	showMessage();
    }
    
    private void checkFileImageSize(File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				checkFileImageSize(subFile);
			}
		} else {
			if (file.getName().endsWith(".jpg") && file.getName().startsWith("00.")) {
				BufferedImage image = null;
				try {
					image = ImageIO.read(file);
					int width = image.getWidth();
					int height = image.getHeight();
					if (width != imageWidth || height != imageHeight) {
						System.out.println(file.getName() + " : (" + width + ", " + height + ")");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
    
    private void listWrongFileNames(File musicFile) {
    	checkWrongFileNames(musicFile);
    	showMessage();
    }
    
    private void checkWrongFileNames(File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				checkWrongFileNames(subFile);
			}
		} else {
			if (file.getName().endsWith(".mp3")) {
				checkMissingApostropheCase(file.getName());
				int bracketOpen = file.getName().replaceAll("[^(]", "").length();
				int bracketClose = file.getName().replaceAll("[^)]", "").length();
				if (bracketOpen != bracketClose) {
					System.out.println("Brackets do not match in " + file.getName());
				}
			}
		}
	}
    
    private void writeXML(File musicFile) {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			
			DocumentBuilder builder = factory.newDocumentBuilder();
	        Document document = builder.newDocument();
	        document.appendChild(document.createProcessingInstruction("xml-stylesheet"," type=\"text/xsl\" href=\"music.xsl\""));
	        writeXMLMusic(musicFile, document);
	        
	        TransformerFactory transFactory = TransformerFactory.newInstance();
            transFactory.setAttribute("indent-number", new Integer(2));
            Transformer transform = transFactory.newTransformer();
            transform.setOutputProperty(OutputKeys.INDENT, "yes");
            transform.setOutputProperty(javax.xml.transform.OutputKeys.DOCTYPE_SYSTEM, "music.dtd");
            
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            DOMSource source = new DOMSource(document);
            transform.transform(source, result);
            String xmlString = stringWriter.toString();
            
            System.out.println(xmlString);
            
            File musicXMLFile = new File(musicFile.getAbsolutePath() + File.separatorChar + "music.xml");
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(musicXMLFile),"UTF-8");
        	out.write(xmlString);
        	out.close();
        	showMessage();
		} catch (ParserConfigurationException e) {
			errors++;
            System.out.println("Error: " + musicFile.toString());
            return;
		} catch (TransformerException e) {
			errors++;
            System.out.println("Error: " + musicFile.toString());
            return;
		} catch (IOException e) { 
			errors++;
            System.out.println("Error: " + musicFile.toString());
            return;
    	}
    }
    
    private void writeXMLMusic(File musicFile, Document document) {
		if (musicFile.isDirectory()) {
			Element  music = document.createElement("music");
			document.appendChild(music);
	    	for (File bandFile : musicFile.listFiles()) {
				String filename = bandFile.getName();
				if (!getFilenameWOExtension(filename).equals("")) {
					writeXMLBand(bandFile, document, music);
				}
			}
		}
    }
    
    private void writeXMLBand(File bandFile, Document document, Element music) {
    	if (bandFile.isDirectory()) {
    		Element band = document.createElement("band");
    		band.setAttribute("name", escapeString(bandFile.getName()));
    		music.appendChild(band);
	    	for (File albumFile : bandFile.listFiles()) {
				String filename = albumFile.getName();
				if (!getFilenameWOExtension(filename).equals("")) {
					writeXMLAlbum(albumFile, document, band);
				}
			}
    	}
    }
    
    private void writeXMLAlbum(File albumFile, Document document, Element band) {
    	if (albumFile.isDirectory()) {
	    	Element album = document.createElement("album");
	    	String name = albumFile.getName();
	    	int pos = name.indexOf("-");
	    	if (pos < 0) {
	    		errors++;
	            System.out.println("Error: " + albumFile.toString());
	            return;
	    	}
	    	album.setAttribute("year", escapeString(name.substring(0, pos)));
	    	album.setAttribute("title", escapeString(name.substring(pos+1)));
	    	band.appendChild(album);
	    	for (File cdOrSongFile : albumFile.listFiles()) {
				String filename = cdOrSongFile.getName();
				if (!getFilenameWOExtension(filename).equals("")) {
					if (filename.startsWith("CD") && getFileExtension(filename).equals("")) {
						writeXMLCD(cdOrSongFile, document, album);	
					} else if (getFileExtension(filename).equalsIgnoreCase("mp3")) {
						writeXMLSong(cdOrSongFile, document, album);
					}
				}
			}
    	}
    }
    
    private void writeXMLCD(File cdFile, Document document, Element album) {
    	if (cdFile.isDirectory()) {
    		Element cd = document.createElement("cd");
    		cd.setAttribute("name", escapeString(cdFile.getName()));
    		album.appendChild(cd);
	    	for (File songFile : cdFile.listFiles()) {
				String filename = songFile.getName();
				if (getFileExtension(filename).equalsIgnoreCase("mp3")) {
					writeXMLSong(songFile, document, cd);
				}
			}
    	}
    }
    
    private void writeXMLSong(File songFile, Document document, Element albumOrCD) {
    	if (!songFile.isDirectory()) {
	    	Element song = document.createElement("song");
	    	String name = getFilenameWOExtension(songFile.getName());
	    	int dot = name.indexOf(".");
	    	String number = name.substring(0, dot);
	    	String title = name.substring(name.indexOf("-") + 1);
	    	song.setAttribute("no", escapeString(number));
	    	song.setTextContent(escapeString(title));
	    	albumOrCD.appendChild(song);
    	}
    }
    
    private String escapeString(String string) {
    	return string.trim();
    }
    
    private void setSongTitle(AbstractID3v2 id3v2, String songTitle) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_TITLE);
        AbstractID3v2FrameBody frameBody;
        if (songTitle != null && !songTitle.isEmpty()) {
	        if (frame == null) {
	            frameBody = new FrameBodyTIT2(TEXT_ENCODING, songTitle);
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, songTitle);
	        }  
        } else {
        	id3v2.removeFrame(V2_TITLE);
        }
    }
    
    private void setLeadArtist(AbstractID3v2 id3v2, String leadArtist) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_ARTIST);
        AbstractID3v2FrameBody frameBody;
        if (leadArtist != null && !leadArtist.isEmpty()) {
	        if (frame == null) {
	            frameBody = new FrameBodyTPE1(TEXT_ENCODING, leadArtist);
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, leadArtist);
	        }  
        } else {
        	id3v2.removeFrame(V2_ARTIST);
        }
    }
    
    private void setAlbumTitle(AbstractID3v2 id3v2, String albumTitle) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_ALBUM);
        AbstractID3v2FrameBody frameBody;
        if (albumTitle != null && !albumTitle.isEmpty()) {
	        if (frame == null) {
	            frameBody = new FrameBodyTALB(TEXT_ENCODING, albumTitle);
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, albumTitle);
	        }  
	    } else if (frame != null) {
	    	id3v2.removeFrame(V2_ALBUM);
	    }
    }
    
    private void setYearReleased(AbstractID3v2 id3v2, String yearReleased) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_YEAR);
        AbstractID3v2FrameBody frameBody;
        if (yearReleased != null && !yearReleased.equals("0")) {
	        if (frame == null) {
	            frameBody = new FrameBodyTYER(TEXT_ENCODING, yearReleased);
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, yearReleased);
	        }
        } else {
        	id3v2.removeFrame(V2_YEAR);
        }
    }
   
    private void setSongGenre(AbstractID3v2 id3v2, String genre) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_GENRE);
        AbstractID3v2FrameBody frameBody;
        if (genre != null && !genre.equals("0")) {
	        if (frame == null) {
	            frameBody = new FrameBodyTCON(TEXT_ENCODING, "(" + genre + ")");
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, "(" + genre + ")");
	        }  
        } else {
        	id3v2.removeFrame(V2_GENRE);
        }
    }
    
    private void setTrackNumberOnAlbum(AbstractID3v2 id3v2, String track, String maxTrack) {
    	String trackText = null; 
    	if (track != null && maxTrack != null) {
    		trackText = track + "/" + maxTrack;
    	} else if (track != null) {
    		trackText = track;
    	}
    	setTrackNumberOnAlbum(id3v2, trackText);
    }
    
    private void setTrackNumberOnAlbum(AbstractID3v2 id3v2, String track) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_TRACK);
        AbstractID3v2FrameBody frameBody;
        if (track != null && !track.equals("0") && !track.equals("0/0")) {
	        if (frame == null) {
	            frameBody = new FrameBodyTRCK(TEXT_ENCODING, track);
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, track);
	        }  
        } else {
        	id3v2.removeFrame(V2_TRACK);
        }
    }

    private void setCDNumberOnAlbum(AbstractID3v2 id3v2, String cd, String maxCD) {
    	String cdText = null;
    	if (cd != null && maxCD != null) {
    		cdText = cd + "/" + maxCD;
    	} else if (cd != null) {
    		cdText = cd;
    	}
    	setCDNumberOnAlbum(id3v2, cdText);
    }
    
    private void setCDNumberOnAlbum(AbstractID3v2 id3v2, String cd) {
    	if (cd == null || cd.equals("")) {
    		return;
    	}
        AbstractID3v2Frame frame = id3v2.getFrame(V2_CD);
        if (cd != null && !cd.equals("0") && !cd.equals("0/0")) {
	        AbstractID3v2FrameBody frameBody;
	        if (frame == null) {
	            frameBody = new FrameBodyTPOS(TEXT_ENCODING, cd);
	            frame = new ID3v2_3Frame(frameBody);
	            id3v2.setFrame(frame);
	        } else {
	            frameBody = (AbstractID3v2FrameBody)frame.getBody();
	            frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
	            frameBody.setObject(TEXT_TAG, cd);
	        }
        } else {
        	id3v2.removeFrame(V2_CD);
        }
    }
    
    private void setAlbumCover(AbstractID3v2 id3v2, byte[] imageBytes) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_PICTURE);
        AbstractID3v2FrameBody frameBody;
        Iterator i = id3v2.getFrameOfType(V2_PICTURE);
        if (!i.hasNext()) {
            if (imageBytes != null) {
                frameBody = new FrameBodyAPIC(TEXT_ENCODING, IMAGE_JPG, ALBUM_COVER, "0", imageBytes);
                frame = new ID3v2_3Frame(frameBody);
                id3v2.setFrame(frame);
            } else {
            	id3v2.removeFrame(V2_PICTURE);
            }
        } else {
            frame = (AbstractID3v2Frame)i.next();
            if (imageBytes != null) {
                frameBody = (AbstractID3v2FrameBody)frame.getBody();
                frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
                frameBody.setObject(MIME_TYPE_TAG, IMAGE_JPG);
                frameBody.setObject(PICTURE_TYPE, ALBUM_COVER);
                frameBody.setObject(DESCRIPTION_TAG, "0");
                frameBody.setObject(PICTURE_DATA_TAG, imageBytes);
            } else {
                frameBody = (AbstractID3v2FrameBody)frame.getBody();
                frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
                frameBody.setObject(MIME_TYPE_TAG, "");
                frameBody.setObject(PICTURE_TYPE, ALBUM_COVER);
                frameBody.setObject(DESCRIPTION_TAG, "0");
                frameBody.setObject(PICTURE_DATA_TAG, new byte[0]);
            }
        }  
    }

    private void setAlbumCoverRef(AbstractID3v2 id3v2, String imageURL) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_PICTURE);
        AbstractID3v2FrameBody frameBody;
        Iterator i = id3v2.getFrameOfType(V2_PICTURE);
        if (!i.hasNext()) {
            if (imageURL != null) {
                frameBody = new FrameBodyAPIC(TEXT_ENCODING, IMAGE_REF, ALBUM_COVER, "0", imageURL.getBytes());
                frame = new ID3v2_3Frame(frameBody);
                id3v2.setFrame(frame);
            }
        } else {
            frame = (AbstractID3v2Frame)i.next();
            if (imageURL != null) {
                frameBody = (AbstractID3v2FrameBody)frame.getBody();
                frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
                frameBody.setObject(MIME_TYPE_TAG, IMAGE_REF);
                frameBody.setObject(PICTURE_TYPE, ALBUM_COVER);
                frameBody.setObject(DESCRIPTION_TAG, "0");
                frameBody.setObject(PICTURE_DATA_TAG, imageURL.getBytes());
            } else {
                frameBody = (AbstractID3v2FrameBody)frame.getBody();
                frameBody.setObject(TEXT_ENCODING_TAG, TEXT_ENCODING);
                frameBody.setObject(MIME_TYPE_TAG, "");
                frameBody.setObject(PICTURE_TYPE, ALBUM_COVER);
                frameBody.setObject(DESCRIPTION_TAG, "0");
                frameBody.setObject(PICTURE_DATA_TAG, new byte[0]);
            }
        }  
    }
    
    private void removeSongComment(AbstractID3v2 id3v2) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_COMMENT);
        if (frame != null) {
            id3v2.removeFrame(V2_COMMENT);
        }  
    }

    private void removeSongLyric(AbstractID3v2 id3v2) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_LYRICS);
        if (frame != null) {
            id3v2.removeFrame(V2_LYRICS);
        }  
    }

    private void removeAuthorComposer(AbstractID3v2 id3v2) {
        AbstractID3v2Frame frame = id3v2.getFrame(V2_COMPOSER);
        if (frame != null) {
            id3v2.removeFrame(V2_COMPOSER);
        }  
    }
    
    private void writeImageToFile(AbstractID3v2 id3v2, String imageFilename) throws IOException {
        Iterator i = id3v2.getFrameOfType("APIC");
        while (i.hasNext()) {
            Object object = i.next();
            FrameBodyAPIC body = (FrameBodyAPIC)((AbstractID3v2Frame)object).getBody();
            String mimeType = (String)body.getObject("MIME Type");
            Long pictureType = (Long)body.getObject("Picture Type");
            byte[] imageBytes = (byte[])body.getObject("Picture Data");
            File imageFile = new File(imageFilename);
            FileOutputStream out = new FileOutputStream(imageFile);
            out.write(imageBytes);
            out.close();
        } 
    }

    private byte[] getImageBytes(File file) throws IOException {
        if (file != null && file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            byte[] imageBytes = new byte[fis.available()];
            fis.read(imageBytes);
            return imageBytes;
        } 
        return null;
    }

    private File getImageFile(File directory) {
        if (directory != null && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                String filename = file.getName().toLowerCase();
                if (filename.startsWith("00") && filename.toLowerCase().endsWith(".jpg")) {
                    return file;                
                }
            }
        }
        return null;
    }
    
    private String getSong(File file) {
        if (file != null && file.isFile()) {
            String filename = file.getName();
            int i = filename.indexOf("-");
            int j = filename.lastIndexOf(".");
            return filename.substring(i+1, j).trim();
        }
        return null;
    }

    private String getYear(File file) {
        if (file != null && file.isFile()) {
            String filename = file.getName();
            int i = filename.lastIndexOf("(");
            if (i > 0) {
                return filename.substring(i+1, i+5).trim();            
            }
        }
        return null;
    }

    private String getTrack(File file) {
        if (file != null && file.isFile()) {
            String filename = file.getName();
            int i = filename.indexOf(".");
            return filename.substring(0, i).trim();
        }
        return null;
    }
    
    private String addLeadingZero(String track) {
    	if (track != null && track.length() == 1) {
    		return "0" + track;
    	}
    	return track;
    }
    
    private void scaleImage(File file, int width, int height) throws IOException {
    	BufferedImage sourceImage = ImageIO.read(file);
    	BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	Graphics2D g = destImage.createGraphics();
    	AffineTransform at = AffineTransform.getScaleInstance(
    							(double)width/sourceImage.getWidth(), (double)height/sourceImage.getHeight());
    	g.drawRenderedImage(sourceImage, at);
    	ImageIO.write(destImage,"JPG", file);
    }
    
    private String capitalize(String string) {
        if (string.length() == 0) {
            return null;
        } else if (string.length() == 1) {
            return "" + Character.toTitleCase(string.charAt(0));
        } else {
            return Character.toTitleCase(string.charAt(0)) + string.substring(1);
        }
    }
    
    private String capitalizeText(String string) {
        String result = "";
        String[] parts = string.split(" ");
        for (String part : parts) {
        	part = part.trim();
        	if (part.equals("*")) {
        		continue;
        	}
            String capText = capitalize(part);
            if (capText != null) {
                result += capText + " ";
            } else {
                System.out.println("Empty part: " + string);
            }
        }
        return result.trim();
    }

    private String camelCaseText(String string) {
        return capitalize(string.toLowerCase());
    }

    private boolean traverseDirectory(File directory) {
        boolean wasFile = false;
        if (directory.isDirectory()) {
            String year = getYearFromDir(directory);
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    year = getYearFromDir(file);
                    copyDir(file, year, true);                    
                    if (traverseDirectory(file)) {
                        updates++;
                    }
                } else {
                    copyDir(file, year, false);
                    wasFile = true;
                }
            }
            return wasFile;
        }
        return false;
    }
    
    private String getYearFromDir(File file) {
        File imageFile = getImageFile(file);
        if (imageFile != null) {
            return getYear(imageFile);
        }
        return null;
    }
   
    private void copyDir(File file, String year, boolean isDir) {
        String parentParentFilepath;
        String parentFilepath;
        String filepath = capitalizeText(file.getName());
        if (isDir) {
            parentParentFilepath = "";
            parentFilepath = file.getParent();
        } else {
            parentParentFilepath = file.getParentFile().getParent(); 
            parentFilepath = file.getParentFile().getName();
        }
        String capitalizedFilename = parentParentFilepath + File.separatorChar + parentFilepath + File.separatorChar + filepath;
        capitalizedFilename = capitalizedFilename.replace(sourceFilePath, targetFilePath);
        File capitalizedFile = new File(capitalizedFilename);
        if (file.isDirectory()) {
            capitalizedFile.mkdirs();
        } else {
            file.renameTo(capitalizedFile);
        }
    }    
}