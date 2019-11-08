package de.droidcachebox.utils;

public class SDBM_Hash {
    /**
     * SDBM-Hash algorithm for storing hash values into the database. This is neccessary to be compatible to the CacheBox@Home project.
     * Because the standard .net Hash algorithm differs from compact edition to the normal edition.
     *
     * @param str
     * @return
     */
    public static long sdbm(String str) {
        if (str == null || str.equals(""))
            return 0;

        long hash = 0;
        // set mask to 2^32!!!???!!!
        long mask = 42949672;
        mask = mask * 100 + 95;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            hash = (c + (hash << 6) + (hash << 16) - hash) & mask;
        }

        return hash;
    }
}
