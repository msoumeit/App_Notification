package com.app.saarr.notify.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

import com.app.saarr.notify.NotifyApp;
import com.app.saarr.notify.R;
import com.app.saarr.notify.ui.cards.Coupon;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sumit_tanay on 22-12-2015.
 */
public class AppNotificationService extends NotificationListenerService {

    public static final String COM_ANDROID_MESSAGING = "com.android.messaging";
    public static final String COM_ANDROID_MMS = "com.android.mms";
    Context context;
    private Tracker mTracker;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
        mTracker = ((NotifyApp) getApplication()).getDefaultTracker();

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        try {
            String pack = sbn.getPackageName();

            if (COM_ANDROID_MESSAGING.equalsIgnoreCase(pack) || COM_ANDROID_MMS.equalsIgnoreCase(pack)) {
                return;
            }
//        String ticker = sbn.getNotification().tickerText.toString();
            Notification simpleNotification = sbn.getNotification();

            printNotification(sbn);
            Bundle extras = simpleNotification.extras;
            String title = extras.getString("android.title");
            String text = extras.getCharSequence("android.text").toString();

//            int sIcon = extras.getInt(Notification.EXTRA_SMALL_ICON);
            //        Bitmap lIcon = simpleNotification.largeIcon;
//        Icon lIcon = sbn.getNotification().getLargeIcon();
//        Icon sIcon = sbn.getNotification().getSmallIcon();

            if (NotificationService.get().isOfferNotification(text)) {
                NotificationService.get().insertNotification(new Coupon(R.drawable.ic_play, title, text, sbn.getPackageName()));
            }

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Card")
                    .setAction("ReadAppNotification")
                    .build());
        } catch (Exception e) {
            if (e != null && e.getLocalizedMessage() != null)
                Log.e("AppNotificationService", e.getLocalizedMessage());
        }
    }

    private void printNotification(StatusBarNotification sbn) {

        Notification notif = sbn.getNotification();

        Log.i("PrintNaughty", "---------------------------------------------------------------------");
        Log.i("PrintNaughty", "Package : " + sbn.getPackageName());
        Log.i("PrintNaughty", Notification.EXTRA_BIG_TEXT + " " + notif.extras.getString(Notification.EXTRA_BIG_TEXT));
        Log.i("PrintNaughty", Notification.EXTRA_INFO_TEXT + " " + notif.extras.getString(Notification.EXTRA_INFO_TEXT));
        Log.i("PrintNaughty", Notification.EXTRA_BACKGROUND_IMAGE_URI + " " + notif.extras.getString(Notification.EXTRA_BACKGROUND_IMAGE_URI));
        Log.i("PrintNaughty", Notification.EXTRA_SUB_TEXT + " " + notif.extras.getString(Notification.EXTRA_SUB_TEXT));
        Log.i("PrintNaughty", Notification.EXTRA_SUMMARY_TEXT + " " + notif.extras.getString(Notification.EXTRA_SUMMARY_TEXT));
        Object objText = notif.extras.get(Notification.EXTRA_TEXT);
        if (objText instanceof SpannableString) {
            SpannableString spannableString = (SpannableString) objText;
            Log.i("PrintNaughty", Notification.EXTRA_TEXT + " " + spannableString.toString());
        } else {
            Log.i("PrintNaughty", Notification.EXTRA_TEXT + " " + (String) objText);
        }
        Log.i("PrintNaughty", Notification.EXTRA_TITLE + " " + notif.extras.getString(Notification.EXTRA_TITLE));
        Log.i("PrintNaughty", Notification.EXTRA_TITLE_BIG + " " + notif.extras.getString(Notification.EXTRA_TITLE_BIG));
        Log.i("PrintNaughty", "---------------------------------------------------------------------");

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("Msg", "Notification Removed");

    }

    /**
     * Return the Intent for PendingIntent.
     * Return null in case of some (impossible) errors: see Android source.
     *
     * @throws IllegalStateException in case of something goes wrong.
     *                               See {@link Throwable#getCause()} for more details.
     */
    public Intent getIntent(PendingIntent pendingIntent) throws IllegalStateException {
        try {
            Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");
            return (Intent) getIntent.invoke(pendingIntent);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
