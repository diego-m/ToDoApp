package com.example.android.todoapp.data;

/**
 * Created by diego on 22/02/15.
 */
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

import com.example.android.todoapp.R;

public class TodoContentProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.android.todoapp.data.contentprovider";

    private static final String BASE_PATH = "todos";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo";

    private TodoDatabaseHelper database;

    private static final int TODOS = 10;
    private static final int TODO_ID = 20;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, TODOS);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ID);
    }

    @Override
    public boolean onCreate() {
        database = new TodoDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        checkColumns(projection);
        queryBuilder.setTables(TodoTable.TABLE_TODO);

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case TODOS:
                break;
            case TODO_ID:
                queryBuilder.appendWhere(TodoTable.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_uri) + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    private void checkColumns(String[] projection) {
        String[] available = { TodoTable.COLUMN_CATEGORY, TodoTable.COLUMN_SUMMARY, TodoTable.COLUMN_DESCRIPTION, TodoTable.COLUMN_ID };
        if (projection != null) {
            HashSet<String> requestCols = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableCols = new HashSet<String>(Arrays.asList(available));
            if (!availableCols.containsAll(requestCols)) {
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_cols));
            }
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase dataBase = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case TODOS:
                id = dataBase.insert(TodoTable.TABLE_TODO, null, values);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_uri) + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsNumDeleted = 0;
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase dataBase = database.getWritableDatabase();
        switch (uriType) {
            case TODOS:
                rowsNumDeleted = dataBase.delete(TodoTable.TABLE_TODO, selection, selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsNumDeleted = dataBase.delete(TodoTable.TABLE_TODO, TodoTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsNumDeleted = dataBase.delete(TodoTable.TABLE_TODO, TodoTable.COLUMN_ID + "=" + id
                                    + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_uri) + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsNumDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsNumUpdated = 0;
        int uriType = uriMatcher.match(uri);
        SQLiteDatabase dataBase = database.getWritableDatabase();
        switch (uriType) {
            case TODOS:
                rowsNumUpdated = dataBase.update(TodoTable.TABLE_TODO, values, selection, selectionArgs);
                break;
            case TODO_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsNumUpdated = dataBase.update(TodoTable.TABLE_TODO, values, TodoTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsNumUpdated = dataBase.update(TodoTable.TABLE_TODO, values, TodoTable.COLUMN_ID + "=" + id
                                    + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.unknown_uri) + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsNumUpdated;
    }

}
