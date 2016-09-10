package com.app.saarr.notify.services;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.app.saarr.notify.NotifyApp;
import com.app.saarr.notify.NotifyContentProvider;
import com.app.saarr.notify.NotifyingAsyncQueryHandler;
import com.app.saarr.notify.R;
import com.app.saarr.notify.ui.cards.Coupon;

import java.util.concurrent.TimeUnit;

/**
 * Created by sumit_tanay on 21-12-2015.
 */
public class NotificationService implements NotifyingAsyncQueryHandler.AsyncQueryListener {

    public static final String SMS_DEFAULT_APPLICATION = "com.android.messaging";

    //Filter
    private static final CharSequence OFF = "OFF";
    private static final CharSequence PERCENT = "%";
    private static final CharSequence FLAT = "FLAT";
    private static final CharSequence BUY = "BUY";
    private static final CharSequence DEAL = "DEAL";
    private static final CharSequence OFFER = "OFFER";
    private static final CharSequence COUPON = "COUPON";
    private static final CharSequence DISCOUNT = "DISCOUNT";
    private static final CharSequence FREE = "FREE";
    private static final CharSequence TOLLFREE = "TOLL FREE";

    private static NotificationService instance;
    private static final String INBOX = "content://sms/inbox";
    private static final String SENT = "content://sms/sent";
    private static final String DRAFT = "content://sms/draft";



    public static NotificationService get() {
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized NotificationService getSync() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    private NotificationService() {
        // here you can directly access the Application context calling
        NotifyApp.get();
    }

    public void insertNotification(Coupon coupon) {
        ContentResolver cr = NotifyApp.get().getContentResolver();
        ContentValues values = new ContentValues();
        values.put(NotifyContentProvider.KEY_SOURCE, coupon.getTitle());
        values.put(NotifyContentProvider.KEY_SMALL_ICON, coupon.getSmallIcon());
        values.put(NotifyContentProvider.KEY_DISPLAY_NOTIFICATION, coupon.getDisplayText());
        values.put(NotifyContentProvider.KEY_RAW_NOTIFICATION, coupon.getDisplayText());
        values.put(NotifyContentProvider.KEY_INTENT, coupon.getTargetIntent());
        values.put(NotifyContentProvider.KEY_CREATION_DATE, coupon.getCreationTime());
        cr.insert(NotifyContentProvider.CONTENT_URI, values);

//        NotifyingAsyncQueryHandler asyncQueryHandler =
//                new NotifyingAsyncQueryHandler(NotifyApp.get(), this);
//
//        asyncQueryHandler.startInsert(1, null, NotifyContentProvider.CONTENT_URI, values);
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {

    }

    public void deleteNotification(Coupon coupon) {
        ContentResolver cr = NotifyApp.get().getContentResolver();
        String where = NotifyContentProvider.KEY_ID + "=?";
        String[] args = new String[]{String.valueOf(coupon.get_Id())};
        cr.delete(NotifyContentProvider.CONTENT_URI, where, args);
    }

    public void readSmsStore() {
        ContentResolver cr = NotifyApp.get().getContentResolver();
        Cursor cursor = cr.query(Uri.parse(INBOX), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                int keyAddress = cursor.getColumnIndexOrThrow("address");
                int keyBody = cursor.getColumnIndexOrThrow("body");
                int keyDate = cursor.getColumnIndexOrThrow("date");
                String body = cursor.getString(keyBody);
                Long msgDate = cursor.getLong(keyDate);
                if (isOfferNotification(body) && isOlder(msgDate, System.currentTimeMillis())) {
                    final String address = cursor.getString(keyAddress);
                    Coupon coupon = new Coupon(R.drawable.ic_sms, address
                            , body, SMS_DEFAULT_APPLICATION);
                    coupon.setCreationTime(msgDate);
                    insertNotification(coupon);
                }
            } while (cursor.moveToNext());
        } else {
            // empty box, no SMS
        }
    }

    public boolean isOfferNotification(String notification) {

        String nUpper = notification.toUpperCase();
        if (nUpper.contains(DEAL)
                || nUpper.contains(OFFER)
                || nUpper.contains(COUPON)
                || nUpper.contains(DISCOUNT)
                || nUpper.contains(OFF)
                || nUpper.contains(PERCENT)
                || nUpper.contains(FLAT)
                || nUpper.contains(BUY)
                || (nUpper.contains(FREE) && !(nUpper.contains(TOLLFREE)))) {
            return true;
        } else {
            return false;
        }

    }

    public boolean isOlder(long msgDate, long cDate) {

        if (getDateDiff(msgDate, cDate, TimeUnit.DAYS) <= 7) {
            return true;
        }
        return false;
    }


    /**
     * Get a diff between two dates
     *
     * @param date1    the oldest date
     * @param date2    the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long getDateDiff(Long date1, Long date2, TimeUnit timeUnit) {
        long diffInMillies = date2 - date1;
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
