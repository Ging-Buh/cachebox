package de.droidcachebox.Components;

import android.util.Log;

public class Lg {
        
        private static final String LOG_TAG = "x";
        

        public static void verbose(final String message) {
                Log.v(LOG_TAG, message);
        }
        public static void verbose(final String message, final Throwable throwable) {
                Log.v(LOG_TAG, message, throwable);
        }
        public static void debug(final String message) {
                Log.d(LOG_TAG, message);
        }
        public static void debug(final String message, final Throwable throwable) {
                Log.d(LOG_TAG, message, throwable);
        }
        public static void info(final String message) {
                Log.i(LOG_TAG, message);
        }
        public static void info(final String message, final Throwable throwable) {
                Log.i(LOG_TAG, message, throwable);
        }
        public static void warn(final String message) {
                Log.w(LOG_TAG, message);
        }
        public static void warn(final String message, final Throwable throwable) {
                Log.w(LOG_TAG, message, throwable);
        }
        public static void error(final String message) {
                Log.e(LOG_TAG, message);
        }
        public static void error(final String message, final Throwable throwable) {
                Log.e(LOG_TAG, message, throwable);
        }
        
}
