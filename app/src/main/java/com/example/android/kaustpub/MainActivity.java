package com.example.android.kaustpub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.kaustpub.sync.KAUSTPubsSyncAdapter;


public class MainActivity extends ActionBarActivity implements KAUSTPubsFragment.Callback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    final String disclaimerPopup = "welcomeScreenShown";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //display a disclaimer msg, second argument is the default if the preference not found
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean welcomeScreenShown = sharedPrefs.getBoolean(disclaimerPopup, false);
        if (!welcomeScreenShown) {
            String disclaimer = "This application is NOT official and NOT produced by KAUST";
            new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Disclaimer").setMessage(disclaimer).setPositiveButton(
                R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                    }
                }).show();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(disclaimerPopup, true);
            editor.commit(); // to save the preference
        Handler handler = new Handler();  handler.postDelayed
                (new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "10 most recent publications - updating automatically",Toast.LENGTH_LONG).show();
                    }  }, 10000);
        }

        if (findViewById(R.id.publication_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.publication_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        KAUSTPubsFragment kaustPubsFragment = ((KAUSTPubsFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_kaustpubs));
        kaustPubsFragment.setUseTodayLayout(!mTwoPane);

        KAUSTPubsSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here to automatically handle clicks on the Home/Up button.
        int id = item.getItemId();

        if (id==R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
       super.onResume();

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by adding or replacing
            // the detail fragment using a fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.publication_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

}
