package tonyg.example.com.examplebleperipheral.utilities;


import android.util.Log;

import java.util.Random;

/**
 * Convert data formats
 *
 * @author Tony Gaitatzis backupbrain@gmail.com
 * @date 2015-12-21
 */

public class DataConverter {

    /**
     * convert bytes to hexadecimal for debugging purposes
     *
     * @param bytes
     * @return Hexadecimal String representation of the byte array
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes.length <=0) return "";
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = 0x20; // space
        }
        return new String(hexChars);
    }

    /**
     * convert bytes to an integer in Little Endian for debugging purposes
     *
     * @param bytes a byte array
     * @return integer integer representation of byte array
     */
    //
    public static String bytesToInt(byte[] bytes) {
        if (bytes.length <=0) return "";
        char[] decArray = "0123456789".toCharArray();
        char[] decChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            decChars[j * 2] = decArray[v >>> 4];
            decChars[j * 2 + 1] = decArray[v & 0x0F];
        }

        return new String(decChars);
    }

    /**
     * Generate a random String
     *
     * @param int length of resulting String
     * @return random String
     */
    private static final String ALLOWED_CHARACTERS ="0123456789abcdefghijklmnopqrstuvwxyz";
    public static String getRandomString(final int length) {
        final Random random = new Random();
        final StringBuilder sb=new StringBuilder(length);
        for(int i=0; i<length; i++) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }
}
