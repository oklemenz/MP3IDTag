package de.oklemenz.id3tag.test;

import java.io.File;

/**
 * <p>Title: Advanced Programming Model</p>
 * <p>Description: Advanced Programming Model</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: OK</p>
 * @author Oliver Klemenz
 * @version 1.0
 */
public class RenameDirectory {

    private static final String sourceFilePath = "D:\\music";
    private static final String targetFilePath = "D:\\music uc";
    
    private static int directoryCount = 0;
    
    public static void main(String[] args) {
        traverseDirectory(new File(sourceFilePath));
        System.out.println("Directory File Count: " + directoryCount);
    }

    private static boolean traverseDirectory(File directory) {
        boolean wasFile = false;
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                copyDir(file);
                if (file.isDirectory()) {
                    if (traverseDirectory(file)) {
                        directoryCount++;
                    }
                } else {
                    wasFile = true;
                }
            }
            return wasFile;
        }
        return false;
    }
    
    private static String capitalize(String string) {
        return Character.toTitleCase(string.charAt(0)) + string.substring(1);
    }
    
    private static String capitalizeText(String string) {
        String result = "";
        String[] parts = string.split(" ");
        for (String part : parts) {
            result += capitalize(part) + " ";
        }
        return result.trim();
    }
    
    private static void copyDir(File file) {
        String capitalizedFilename = file.getParent() + File.separatorChar + capitalizeText(file.getName());
        capitalizedFilename = capitalizedFilename.replace(sourceFilePath, targetFilePath);
        File capitalizedFile = new File(capitalizedFilename);
        if (file.isDirectory()) {
            capitalizedFile.mkdirs();
        } else {
            file.renameTo(capitalizedFile);
        }
    }    
}