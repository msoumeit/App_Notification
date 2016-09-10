package com.app.saarr.notify.ui.cards;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.saarr.notify.NotifyApp;
import com.app.saarr.notify.R;
import com.app.saarr.notify.services.NotificationService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by  on 05/08/2015.
 */
public class CouponCardAdaptor extends RecyclerView.Adapter<CouponCardAdaptor.CouponViewHolder> {
    private final Tracker mTracker;
    private ArrayList<Coupon> item;
    private Random rand = new Random();
    private int[] adunit = {R.string.banner_ad_unit_id1,
            R.string.banner_ad_unit_id2,
            R.string.banner_ad_unit_id3,
            R.string.banner_ad_unit_id4,
            R.string.banner_ad_unit_id5};

    public CouponCardAdaptor(ArrayList<Coupon> item) {
        this.item = item;
        mTracker = NotifyApp.get().getDefaultTracker();
    }

    @Override
    public CouponViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_row, viewGroup, false);
        CouponViewHolder cardview = new CouponViewHolder(v);

        AdView mAdView = new AdView(NotifyApp.get());
        int result = rand.nextInt(4);
        String adStr = NotifyApp.get().getResources().getString(adunit[result]);
        Log.i("CouponCardAdaptor", adStr);
        mAdView.setAdUnitId(adStr);
        mAdView.setAdSize(AdSize.BANNER);

        RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lay.addRule(RelativeLayout.BELOW, cardview.displayText.getId());
        cardview.parentView.addView(mAdView, lay);
        cardview.setAdView(mAdView);

        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("089B26BC899CAB8AEEE61343254E4758")
//                                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        if (mAdView.getAdSize() != null && mAdView.getAdUnitId() != null)
            mAdView.loadAd(adRequest);


        return cardview;
    }

    @Override
    public void onBindViewHolder(CouponViewHolder CouponViewHolder, final int i) {
        final String packageName = item.get(i).getTargetIntent();
        CouponViewHolder.title.setText(item.get(i).getTitle());
        CouponViewHolder.displayText.setText(
                Html.fromHtml(item.get(i).getDisplayText()));

        CouponViewHolder.smallIcon.setImageResource(item.get(i).getSmallIcon());
        CouponViewHolder.displayTime.setText(DateUtils.getRelativeTimeSpanString(item.get(i).getCreationTime()
                , System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
        CouponViewHolder.displayText.setMovementMethod(LinkMovementMethod.getInstance());
        CouponViewHolder.smallIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = null;
                if (NotificationService.SMS_DEFAULT_APPLICATION.equalsIgnoreCase(packageName)) {
                    launchIntent = new Intent(Intent.ACTION_MAIN);
                    launchIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    launchIntent.setType("vnd.android-dir/mms-sms");
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Card")
                            .setAction("OpenSMSApp")
                            .build());

                } else {
                    PackageManager pm = NotifyApp.get().getPackageManager();
                    launchIntent = pm.getLaunchIntentForPackage(item.get(i).getTargetIntent());

                    if (launchIntent == null) {
                        Toast.makeText(NotifyApp.get(), "Failed to open the linked application", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Card")
                            .setAction("OpenApp")
                            .build());
                }
                NotifyApp.get().startActivity(launchIntent);
//                try {
//                    targetIntent = Intent.parseUri(item.get(i).getTargetIntent(), 0);
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
//                PendingIntent contentIntent = PendingIntent.getBroadcast(NotifyApp.get(), (int) System.currentTimeMillis(),
//                        targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                try {
//                    contentIntent.send();
//                } catch (PendingIntent.CanceledException e) {
//                    e.printStackTrace();
//                }
            }
        });


//        AdView mAdView = CouponViewHolder.adView;
//        AdRequest adRequest = new AdRequest.Builder()
////                .addTestDevice("089B26BC899CAB8AEEE61343254E4758")
////                                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .build();
//        if (mAdView.getAdSize() != null && mAdView.getAdUnitId() != null)
//            mAdView.loadAd(adRequest);


        //        CouponViewHolder.imagen.setImageResource(item.get(i).getSmallIcon());

    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView displayTime;
        TextView title, displayText;
        ImageView smallIcon;
        RelativeLayout parentView;
        AdView adView;

        public AdView getAdView() {
            return adView;
        }

        public void setAdView(AdView adView) {
            this.adView = adView;
        }


        public CouponViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.lbltitle);
            displayText = (TextView) itemView.findViewById(R.id.lbldisplaytext);
            smallIcon = (ImageView) itemView.findViewById(R.id.smallIcon);
            displayTime = (TextView) itemView.findViewById(R.id.lbltime);
            parentView = (RelativeLayout) itemView.findViewById(R.id.rowlayout);

        }
    }
}
