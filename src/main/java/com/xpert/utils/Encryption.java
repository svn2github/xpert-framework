package com.xpert.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author ayslan
 */
public class Encryption {

    public static String getMD5(String string, String salt) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        if (salt != null && !salt.trim().isEmpty()) {
            string = salt + string;
        }
        BigInteger hash = new BigInteger(1, md.digest(string.getBytes()));
        String s = hash.toString(16);
        if (s.length() % 2 != 0) {
            s = "0" + s;
        }
        
        return s;
    }

    public static String getMD5(String string) throws NoSuchAlgorithmException {
        return getMD5(string, null);
    }

    public static String getSHA256(String string) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] hash = md.digest(string.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
