package org.ngo.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ShortMD5Util {

	public static void main(String[] args) {
		try {
            byte[] bytes = args[0].getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            //MAX_RADIX because we'll generate the shortest string possible... (while still
            //using only numbers 0-9 and letters a-z)
            byte[] mag = md.digest(bytes);
            String ret = new BigInteger(1, mag).toString(Character.MAX_RADIX).toLowerCase();
            System.out.println(ret);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

	}

}
