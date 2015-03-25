package com.example.android.kaustpub.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.kaustpub.MyApp;
import com.example.android.kaustpub.R;
import com.example.android.kaustpub.data.PublicationContract;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KAUSTPubsSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = KAUSTPubsSyncAdapter.class.getSimpleName();
    /** The url of the current preference of publication category, or the default feeder URL.
    Categories include: 1) Research Publications Available in the KAUST Repository,
    2) KAUST Research Output as indexed by Elsevier Scopus database,
    3) KAUST Research Output as indexed by ISI Web of Knowledge.**/
    static Context cx = MyApp.getAppContext();
    static SharedPreferences shared = cx.getSharedPreferences("pref_fetch_key", Context.MODE_PRIVATE);
    public static String fetchCategory = (shared.getString("pref_fetch_key", "http://feed.informer.com/widgets/NCAI3LSMSY.js"));

    // Interval at which to sync with the publications, in seconds. 60 seconds * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public KAUSTPubsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        startFetching();
    }

    //scheduling the sync adapter periodic execution
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet. If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

             //If you don't set android:syncable="true" in in your <provider> element in the manifest,
             //then call ContentResolver.setIsSyncable(account, AUTHORITY, 1) here.
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {

         //Since we've created an account
        KAUSTPubsSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        //Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        //Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    protected void doInBackground() {
        String[][] pubInfo = new String[10][10];

        //reading links
        Pattern p = Pattern.compile("(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

        String pattern1 = "_blank\\\">";
        String pattern2 = "<\\/a>";
        Pattern p2 = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
             //Construct the feeder URL
            URL url = new URL(fetchCategory.toString());

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null) {
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line; int row = 0;
            line = reader.readLine();
            Matcher m = p.matcher(line);
            Matcher m2 = p2.matcher(line);
            buffer.append(line);

                while (m.find() && m2.find() && row<=9) {
                    String link=m.group();
                    link= link.replace("\"", "");

                    String title=m2.group();
                    title = title.replace(pattern1, ""); title = title.replace(pattern2, "");

                    pubInfo[row][0] = title; pubInfo[row][1] = link;
                    row++;
                }
            updateDB(pubInfo);

            if (buffer.length() == 0) {
                // Stream was empty.
                Log.d(LOG_TAG, "The stream is empty!");
                return;
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data.
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }
    public void startFetching() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                doInBackground();
            }
        };
        new Thread(runnable).start();
    }


    private void updateDB(String[][] pubInfo) throws JSONException {

        try {
            String[][] fullInfo = pubInfo;

            // Insert the new publication information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(fullInfo.length);

            for(int i = 0; i < fullInfo.length; i++) {
                // These are the values that will be collected.
                String pubTitle, pubLink, pubID;

                pubTitle = fullInfo[i][0].toString();
                pubLink = fullInfo[i][1].toString();

                ContentValues publicationValues = new ContentValues();

                publicationValues.put(PublicationContract.PublicationEntry.COLUMN_LINK, pubLink);
                publicationValues.put(PublicationContract.PublicationEntry.COLUMN_TITLE, pubTitle);

                cVVector.add(publicationValues);
            }

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(PublicationContract.PublicationEntry.CONTENT_URI,
                    PublicationContract.PublicationEntry._ID + " <= ?",
                    new String[]{Long.toString(System.currentTimeMillis() - 1)});

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(PublicationContract.PublicationEntry.CONTENT_URI, cvArray);
            }

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
