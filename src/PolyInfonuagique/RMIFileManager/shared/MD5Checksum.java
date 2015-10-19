package PolyInfonuagique.RMIFileManager.shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Java program to generate MD5 checksum for files in Java. This Java example
 * uses core Java security package and Apache commons codec to generate MD5
 * checksum for a File.
 *
 * @author Javin Paul
 */
public class MD5Checksum {

    /*
     * Calculate checksum of a File using MD5 algorithm
     */
    public static String checkSum(String path) {
        String checksum = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            MessageDigest md = MessageDigest.getInstance("MD5");

            //Using MessageDigest update() method to provide input
            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while ((numOfBytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString(16); //don't use this, truncates leading zero
        } catch (IOException | NoSuchAlgorithmException ignored) {
        }

        return checksum;
    }

    /*
    * Calculate checksum of a bytes[] using MD5 algorithm
    */
    public static String checkSum(byte[] buffer) {
        String checksum = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);

            //Using MessageDigest update() method to provide input
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString(16); //don't use this, truncates leading zero
        } catch (  NoSuchAlgorithmException ignored) {
        }

        return checksum;
    }


}
