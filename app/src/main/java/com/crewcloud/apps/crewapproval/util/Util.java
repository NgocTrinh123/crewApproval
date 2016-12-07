package com.crewcloud.apps.crewapproval.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.crewcloud.apps.crewapproval.BuildConfig;
import com.crewcloud.apps.crewapproval.CrewCloudApplication;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Util {
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) CrewCloudApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

    public static boolean isWifiEnable() {
        WifiManager wifi = (WifiManager) CrewCloudApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connManager = (ConnectivityManager) CrewCloudApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isWifiEnabled() && mWifi.isConnected();
    }

    public static void printLogs(String logs) {
        if (BuildConfig.ENABLE_DEBUG) {
            if (logs == null)
                return;
            int maxLogSize = 1000;
            if (logs.length() > maxLogSize) {
                for (int i = 0; i <= logs.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i + 1) * maxLogSize;
                    end = end > logs.length() ? logs.length() : end;
                    Log.d("CrewApproval", logs.substring(start, end));
                }
            } else {
                Log.d("CrewApproval", logs);
            }
        }
    }

    public static String getString(int stringID) {
        return CrewCloudApplication.getInstance().getApplicationContext().getResources().getString(stringID);
    }

    public static boolean checkStringValue(String... params) {
        for (String param : params) {
            if (param != null) {
                if (TextUtils.isEmpty(param.trim())) {
                    return false;
                }

                if (param.contains("\n") && TextUtils.isEmpty(param.replace("\n", ""))) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public static long getTimeOffsetInMinute() {
        return TimeUnit.MINUTES.convert(getTimeOffsetInMillis(), TimeUnit.MILLISECONDS);
    }

    public static long getTimeOffsetInMillis() {
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();

        return mTimeZone.getRawOffset();
    }

    public static int getTimezoneOffsetInMinutes() {
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getRawOffset() / 60000;
        return offsetMinutes;
    }

    public static String getFullHour(String tv) {
        String hour = "", minute = "";
        int h = getHour(tv);
        int m = getMinute(tv);
        if (h < 10) hour = "0" + h;
        else hour = "" + h;
        if (m < 10) minute = "0" + m;
        else minute = "" + m;
        String text = hour + ":" + minute;
        return text;
    }

    public static int getHour(String tv) {
        int h = 0;
        String str[] = tv.trim().split(" ");
//        Log.e(TAG, str[1].split(":")[0]);
        h = Integer.parseInt(str[1].split(":")[0]);
        if (str[0].equalsIgnoreCase("PM")) h += 12;
        return h;
    }

    public static int getMinute(String tv) {
        return Integer.parseInt(tv.split(" ")[1].split(":")[1]);
    }

    public static String getPhoneLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static void showImage(String url, ImageView view) {
        if (url.contains("content") || url.contains("storage")) {
            File f = new File(url);
            if (f.exists()) {
                Picasso.with(view.getContext()).load(f).into(view);
            } else {
                Picasso.with(view.getContext()).load(url).into(view);
            }
        } else {
            Picasso.with(view.getContext()).load(CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + url).into(view);
        }
    }
}