package com.example.android.kaustpub;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.kaustpub.data.PublicationContract;
import com.example.android.kaustpub.sync.KAUSTPubsSyncAdapter;

/**
 * Encapsulates fetching the publications and displaying them.
 */
public class KAUSTPubsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public static int pubItemPosition;
    private KAUSTPubsAdapter mKAUSTPubsAdapter;

    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    private static final String SELECTED_KEY = "selected_position";

    private static final int PUBLICATION_LOADER = 0;

    //Specify the columns we need.
    private static final String[] Publication_COLUMNS = {
            PublicationContract.PublicationEntry.TABLE_NAME + "." + PublicationContract.PublicationEntry._ID,
            PublicationContract.PublicationEntry.COLUMN_LINK,
            PublicationContract.PublicationEntry.COLUMN_TITLE
    };

    // These indices are tied to Publication_COLUMNS.
    static final int COL_PUB_ID = 0;
    static final int COL_LINK = 1;
    static final int COL_TITLE = 2;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item selections.
     */
    public interface Callback {
        public void onItemSelected(Uri dateUri); }

    public KAUSTPubsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.kaustpubsfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on
        // the Home/Up button.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updatePublicationList();
            Context context = MyApp.getAppContext();
            //Context context = getActivity().getApplicationContext();
            Toast.makeText(context, "The list has been updated", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The Adapter will take data from a source and to populate the ListView.
        mKAUSTPubsAdapter = new KAUSTPubsAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_publications);
        mListView.setAdapter(mKAUSTPubsAdapter);

        // Calling the MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                pubItemPosition = position;
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(PublicationContract.PublicationEntry.buildPublicationUri(COL_PUB_ID));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet. Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        mKAUSTPubsAdapter.setUseTodayLayout(mUseTodayLayout);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PUBLICATION_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updatePublicationList() {
        KAUSTPubsSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri publicationsUri = PublicationContract.PublicationEntry.buildPublicationUri(id);

        return new CursorLoader(getActivity(), publicationsUri, Publication_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mKAUSTPubsAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mKAUSTPubsAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTbltLayout) {
        mUseTodayLayout = useTbltLayout;
        if (mKAUSTPubsAdapter != null) {
            mKAUSTPubsAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }
}
