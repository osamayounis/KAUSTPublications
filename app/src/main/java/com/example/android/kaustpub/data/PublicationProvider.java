package com.example.android.kaustpub.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class PublicationProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PublicationDbHelper mOpenHelper;

    static final int PUBLICATION = 100;


    private static final SQLiteQueryBuilder sPublicationsQueryBuilder;

    static{
        sPublicationsQueryBuilder = new SQLiteQueryBuilder();
        sPublicationsQueryBuilder.setTables(
                PublicationContract.PublicationEntry.TABLE_NAME);
    }

    static UriMatcher buildUriMatcher() {
        // The code passed into the constructor represents the code to return for the root
        // URI. It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PublicationContract.CONTENT_AUTHORITY;

        //Code to add type of URI.
        matcher.addURI(authority, PublicationContract.PATH_PUBLICATIONS, PUBLICATION);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PublicationDbHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {
        return PublicationContract.PublicationEntry.CONTENT_ITEM_TYPE;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        retCursor = mOpenHelper.getReadableDatabase().query(
                PublicationContract.PublicationEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        delete(uri, null, null);//Delete old records before insert new records to avoid endless history

        Uri returnUri;

                long _id = db.insert(PublicationContract.PublicationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = PublicationContract.PublicationEntry.buildPublicationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

                rowsUpdated = db.update(PublicationContract.PublicationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        delete(uri, null, null);//Delete old records before insert new records to avoid endless history
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PublicationContract.PublicationEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(PublicationContract.PublicationEntry.TABLE_NAME, null, null);

        return 0;
    }

    // a method specifically to assist the testing framework in running smoothly
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}