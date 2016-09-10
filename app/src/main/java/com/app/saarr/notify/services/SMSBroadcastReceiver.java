package com.app.saarr.notify.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.app.saarr.notify.NotifyApp;
import com.app.saarr.notify.R;
import com.app.saarr.notify.ui.cards.Coupon;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by sumit_tanay on 22-12-2015.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {

    private final Tracker mTracker;

    public SMSBroadcastReceiver() {
        super();
        mTracker = NotifyApp.get().getDefaultTracker();
    }

    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        try {

            if (msgs != null) {

                for (int i = 0; i < msgs.length; i++) {

                    SmsMessage currentMessage = msgs[i];
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    if (NotificationService.get().isOfferNotification(message)) {
                        NotificationService.get().insertNotification
                                (new Coupon(R.drawable.ic_sms,
                                        senderNum, message, NotificationService.SMS_DEFAULT_APPLICATION));

                    }
                    // Show Alert

                   /* int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "senderNum: "+ senderNum + ", message: " + message, duration);
                    toast.show();
                    */
                } // end for loop
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Card")
                        .setAction("ReadSMSNotification")
                        .build());
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
    }
}
