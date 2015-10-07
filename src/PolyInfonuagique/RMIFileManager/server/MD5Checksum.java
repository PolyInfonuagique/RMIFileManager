package PolyInfonuagique.RMIFileManager.server;

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
    public static String getChecksum(byte[] buffer){
        String checksum = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString(16);
        } catch (NoSuchAlgorithmException ignored) {
        }

        return checksum;
    }


}
