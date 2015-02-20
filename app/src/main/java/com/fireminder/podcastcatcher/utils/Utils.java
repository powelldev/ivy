package com.fireminder.podcastcatcher.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import java.util.Locale;

/**
 * Created by michael on 2/16/2015.
 */
public class Utils {
    public static String computeHash(String string) {
        return String.format(Locale.US, "%08x%08x", string.hashCode(), string.length());
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnected();
    }
}
