package com.app.saarr.notify;

import android.app.Dialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SearchView;

import com.app.saarr.notify.aboutus.AboutUsActivity;
import com.app.saarr.notify.services.NotificationService;
import com.app.saarr.notify.ui.cards.Coupon;
import com.app.saarr.notify.ui.cards.CouponCardAdaptor;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "MainActivity";
    //    private ShareActionProvider mShareActionProvider;
    private static String QUERY_EXTRA_KEY = "QUERY_EXTRA_KEY";
    private final String isInitialAppLaunch = "isInitialAppLaunch";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager lManager;
    private RecyclerView.Adapter mAdapter;

    private ArrayList<Coupon> mCoupons = null;
    private Tracker mTracker;
    private String APP_PNAME = "com.app.saarr.notify";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        NotifyApp application = (NotifyApp) getApplication();
        mTracker = application.getDefaultTracker();
        // [END shared_tracker]


        final SearchView searchView = (SearchView) findViewById(R.id.searchView);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                fab.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate));
                getLoaderManager().restartLoader(0, null, MainActivity.this);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Card")
                        .setAction("Refresh")
                        .build());
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.requestFocus();
        lManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(lManager);

        mCoupons = new ArrayList<Coupon>();

        mAdapter = new CouponCardAdaptor(mCoupons);
        mRecyclerView.setAdapter(mAdapter);


        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipe(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                deleteCard(reverseSortedPositions);
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                deleteCard(reverseSortedPositions);
                            }
                        });

        mRecyclerView.addOnItemTouchListener(swipeTouchListener);

        /**
         * Binding a Search View to your searchable Activity
         */
        // Use the Search Manager to find the SearchableInfo related
        // to this Activity.
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo =
                searchManager.getSearchableInfo(getComponentName());

        // Bind the Activity's SearchableInfo to the Search View


        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconifiedByDefault(false);
        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) searchView.findViewById(searchCloseButtonId);
        // Set on click listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                getIntent().removeExtra(QUERY_EXTRA_KEY);
                getLoaderManager().restartLoader(0, null, MainActivity.this);
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Search")
                        .setAction("Clear")
                        .build());
            }
        });

        SharedPreferences prefs = getSharedPreferences(getResources().getString(R.string.app_name), 0);
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getBoolean(isInitialAppLaunch, false)) {
            Log.i(TAG, "Sms content already run");
        } else {
            onCoachMark();
            //First Time App launched, you are putting isInitialAppLaunch to false and calling Notification sms store.
            editor.putBoolean(isInitialAppLaunch, true);
            editor.commit();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationService.get().readSmsStore();
                }
            });
            thread.start();

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Card")
                    .setAction("ReadSMSStore")
                    .build());
        }

        // Initiate the Cursor Loader
        getLoaderManager().initLoader(0, null, this);
        NotifyUtils.sendScreen(mTracker, TAG);
    }

    private void deleteCard(int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            Coupon aCoupon = mCoupons.get(position);
            NotificationService.get().deleteNotification(aCoupon);
//                                    getLoaderManager().restartLoader(0, null, MainActivity.this);
            mCoupons.remove(position);
            mAdapter.notifyItemRemoved(position);
        }
        mAdapter.notifyDataSetChanged();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Card")
                .setAction("Delete")
                .build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Activity")
                .setAction("onResume")
                .build());
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Locate MenuItem with ShareActionProvider
//        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//        mShareActionProvider.setOnShareTargetSelectedListener(this);

        // Set the Intent
//        setShareIntent(getDefaultShareIntent());

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notifaccess) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("NotificationReadAccess")
                    .build());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, NotifyUtils.STR_SHARE + "\n\n" + Html.fromHtml("https://play.google.com/store/apps/details?id=" + APP_PNAME));
            share.putExtra(android.content.Intent.EXTRA_SUBJECT, "Have you checked NoteTrap App?");
            startActivity(Intent.createChooser(share, "share"));
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("Share")
                    .build());

        } else if (id == R.id.nav_rateus) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("RateUs")
                    .build());
        } else if (id == R.id.nav_writetous) {
            Intent emailIntent = NotifyUtils.getEmailIntent("contact@notetrap.com", "Hi ", "");
            startActivity(Intent.createChooser(emailIntent, "We are listening!"));
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("WriteToUs")
                    .build());
        } else if (id == R.id.nav_aboutus) {
            startActivity(new Intent(this, AboutUsActivity.class));
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("AboutUs")
                    .build());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = null;
        String where = null;
        String[] whereArgs = null;
        String sortOrder = NotifyContentProvider.KEY_CREATION_DATE + " DESC";
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = "";
            // Extract the search query from the arguments.
            if (args != null)
                query = args.getString(QUERY_EXTRA_KEY);
            // Construct the new query in the form of a Cursor Loader.
//            projection = new String[]{
//                    NotifyContentProvider.KEY_ID,
//                    NotifyContentProvider.KEY_RAW_NOTIFICATION
//            };
            where = NotifyContentProvider.KEY_RAW_NOTIFICATION + " LIKE \'%" + query + "%\'";
            whereArgs = null;
            sortOrder = NotifyContentProvider.KEY_CREATION_DATE + " DESC";
        }
        CursorLoader loader = new CursorLoader(this,
                NotifyContentProvider.CONTENT_URI, projection, where, whereArgs, sortOrder);

        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        int keyIdIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_ID);
        int keyCreationDateIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_CREATION_DATE);
        int keyDisplayIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_DISPLAY_NOTIFICATION);
        int keyRawIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_RAW_NOTIFICATION);
        int keySourceIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_SOURCE);
        int keyTargetIntentIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_INTENT);
        int keySmallIconIndex = cursor.getColumnIndexOrThrow(NotifyContentProvider.KEY_SMALL_ICON);


        mCoupons.clear();
        while (cursor.moveToNext()) {
            Coupon newCoupon = new Coupon(cursor.getInt(keySmallIconIndex), cursor.getString(keySourceIndex),
                    cursor.getString(keyDisplayIndex), cursor.getString(keyTargetIntentIndex));
            newCoupon.set_Id(cursor.getInt(keyIdIndex));
            newCoupon.setCreationTime(cursor.getLong(keyCreationDateIndex));
            newCoupon.setRawText(cursor.getString(keyRawIndex));
            mCoupons.add(newCoupon);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        parseIntent(getIntent());
    }

    private void parseIntent(Intent intent) {
        // If the Activity was started to service a Search request,
        // extract the search query.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            // Perform the search
            performSearch(searchQuery);
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Card")
                    .setAction("Search")
                    .build());
        }
    }

    // Execute the search.
    private void performSearch(String query) {
        // Pass the search query as an argument to the Cursor Loader
        Bundle args = new Bundle();
        args.putString(QUERY_EXTRA_KEY, query);
        // Restart the Cursor Loader to execute the new query.
        getLoaderManager().restartLoader(0, args, this);
    }

    public void onCoachMark() {

        final Dialog dialog = new Dialog(new ContextThemeWrapper(this, R.style.WalkthroughTheme));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.coach_mark);
        dialog.setCanceledOnTouchOutside(true);
        //for dismissing anywhere you touch
        View masterView = dialog.findViewById(R.id.coach_mark_master_view);
        masterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
