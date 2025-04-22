package src.media;

import java.io.*;

public class FileUtils {

    // Read a file into byte array
    public static byte[] readFile(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        byte[] fileData = new byte[(int) file.length()];
        
        FileInputStream fis = new FileInputStream(file);
        fis.read(fileData);
        fis.close();
        
        return fileData;
    }

    // Save byte array to file
    public static void saveFile(byte[] data, String filePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(data);
        fos.close();
    }

    // Save to folder
    public static void saveFile(byte[] data, String fileName, String folder) throws IOException {
        java.io.File dir = new java.io.File(folder);
        if (!dir.exists()) dir.mkdirs();
        saveFile(data, folder + "/" + fileName);
    }

    // Get file name from path
    public static String getFileName(String filePath) {
        java.io.File file = new java.io.File(filePath);
        return file.getName();
    }

    // Get readable file size
    public static String getFileSize(String filePath) {
        java.io.File file = new java.io.File(filePath);
        long bytes = file.length();
        
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }

    // Build transfer header
    public static String buildFileHeader(String filePath) {
        String fileName = getFileName(filePath);
        long size = new java.io.File(filePath).length();
        return "FILE|" + fileName + "|" + size;
    }
}
