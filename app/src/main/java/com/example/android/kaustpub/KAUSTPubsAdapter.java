package com.example.android.kaustpub;

/**
 * Created by Osama on 2/21/2015.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link KAUSTPubsAdapter} exposes a list of KAUST Publications titles
 //* from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class KAUSTPubsAdapter extends CursorAdapter {

      private static final int VIEW_TYPE_COUNT = 2;
      private static final int VIEW_TYPE_TBLT = 0;
      private static final int VIEW_TYPE_MOB = 1;

    // Flag to determine if we want to use a separate view.
    private boolean mUseTodayLayout = true;

    public static class ViewHolder {
        public final TextView titleView;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.list_title_textview);
        }
    }

    public KAUSTPubsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TBLT: {
                layoutId = R.layout.list_titles_tblt;
                break;
            }
            case VIEW_TYPE_MOB: {
                layoutId = R.layout.list_titles;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read publications titles from cursor
        String strTitle = cursor.getString(KAUSTPubsFragment.COL_TITLE);
        if (strTitle.length() > 70)
            strTitle = strTitle.substring(0, 70) + "...";
        viewHolder.titleView.setText(strTitle);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TBLT : VIEW_TYPE_MOB;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}