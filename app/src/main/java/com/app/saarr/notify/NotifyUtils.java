package com.app.saarr.notify;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.net.URLEncoder;

/**
 * Created by sumit_tanay on 01-01-2016.
 */
public class NotifyUtils {
    public static final String STR_PRIVACY = "<h2>\"We don’t store your data, period.\"</h2>\n" +
            "<p>None of your personal data ever leaves the app without your consent. So even if anyone asked nicely to see your data, we wouldn’t have anything to show them.</p>\n" +
            "\n" +
            "<p>That’s why, with NoteTrap, what happens on your app stays on your app.</p>\n" +
            "\n" +
            "We use Google Analytics for analysing NoteTrap App usage.Google Analytics is a web analysis service provided by Google. Google utilizes the data collected to track and examine the use of the app, to prepare reports on its activities and share them with other Google services.\n" +
            "Google may use the data collected to contextualize and personalize the ads of its own advertising network.\n" +
            "\n" +
            "\n<p>Personal data collected: NoteTrap App Usage Data.</p>";

    public static final String STR_ABOUTUS = "<p>People are fed up of all the mobile notifications they regularly receive offers and coupons from text,\n" +
            "                                    apps and emails. Do you remember to use them when you shop?</p>\n" +
            "                                    <p><b>NoteTrap</b> is a mobile platform that lets you save all these notifications to a single place as note cards automatically.\n" +
            "                                        Now, you know where to find them when you shop.</p>";
    public static final String STR_SHARE = "Hi\n\nHave you checked out the NoteTrap App? It lets you save all the offer and coupon notifications automatically. Download app for free here.";

    /**
     * Record a screen view hit for the visible {@link MainActivity} displayed
     * inside {@link FragmentPagerAdapter}.
     */
    public static void sendScreen(Tracker mTracker, String name) {

        // [START screen_view_hit]
        Log.i(name, "Setting screen name: " + name);
        mTracker.setScreenName("Activity " + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]
    }

    public static Intent getEmailIntent(String emailAddress, String subject, String body) {
//        Uri uri = Uri.parse("mailto:" + emailAddress)
//                .buildUpon()
//                .appendQueryParameter("subject", subject)
//                .appendQueryParameter("body", body)
//                .build();

        String uriText = "mailto:" + emailAddress +
                "?subject=" + URLEncoder.encode(subject) +
                "&body=" + URLEncoder.encode(body);
        Uri uri = Uri.parse(uriText);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(uri);
//        emailIntent.setType("text/plain");
        return emailIntent;
    }
}
