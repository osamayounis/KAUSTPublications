/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.kaustpub.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the publicationsList database.
 */
public class PublicationContract {

    // The Content authority, a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.kaustpub.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_PUBLICATIONS = "publications";

    /* Inner class that defines the table contents of the publication table */
    public static final class PublicationEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PUBLICATIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PUBLICATIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PUBLICATIONS;

        // Table name
        public static final String TABLE_NAME = "publicationlist";

        // Columns
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_LINK = "link";
        //public static final String COLUMN_PUB_ID = "pub_id";

        public static Uri buildPublicationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
