package com.techprd.cordova.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ContentProviderService {

    private SimpleDateFormat dateFormatter;
    private Context context;
    private Uri externalContentUri;
    private QueryType queryType;
    private String whereClause = "";
    private String[] selectionArgs = null;
    private String sortOrder = "";
    private int limit = 0;
    private int offset = 0;

    ContentProviderService(Uri externalContentUri, QueryType queryType) {
        this.externalContentUri = externalContentUri;
        this.queryType = queryType;
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    ContentProviderService setContext(Context context) {
        this.context = context;
        return this;
    }

    ContentProviderService setWhereClause(String whereClause) {
        this.whereClause = whereClause;
        return this;
    }

    ContentProviderService setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    ContentProviderService setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    ContentProviderService setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    ContentProviderService setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    JSONObject queryContentProvider() throws JSONException {

        JSONObject columns = Utils.getQueryColumnsByType(queryType);
        final ArrayList<String> columnKeys = Utils.getColumnsKeys(columns);
        final ArrayList<String> columnValues = Utils.getColumnsValues(columns);
        final Cursor cursor = getQueryCursor(columnValues, columns);

        final JSONArray buffer = new JSONArray();
        JSONObject output = new JSONObject();
        JSONObject metadata = new JSONObject();
        if (limit > 0) {
            if (cursor != null && cursor.move(offset)) {
                do {
                    populateBuffer(columns, columnKeys, cursor, buffer);
                    cursor.moveToNext();
                } while (!cursor.isAfterLast() && (limit + offset) > cursor.getPosition());
                metadata.put("count", cursor.getCount());
                metadata.put("position", cursor.getPosition());
                cursor.close();
            }
        } else {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    populateBuffer(columns, columnKeys, cursor, buffer);
                } while (cursor.moveToNext());
                metadata.put("count", cursor.getCount());
                metadata.put("position", cursor.getPosition());
                cursor.close();
            }
        }

        output.put("data", buffer);
        output.put("metadata", metadata);
        return output;
    }

    private Cursor getQueryCursor(ArrayList<String> columnValues, JSONObject columns) {
        return context.getContentResolver().query(
                externalContentUri,
                columnValues.toArray(new String[columns.length()]),
                whereClause, selectionArgs, sortOrder);
    }

    private void populateBuffer(JSONObject columns,
                                ArrayList<String> columnNames,
                                Cursor cursor,
                                JSONArray buffer) throws JSONException {
        JSONObject item = getContent(columns, columnNames, cursor);
        if (item.has(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)) {
            if (externalContentUri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                long videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                getVideoThumbnail(context, item, videoId);
            } else {
                long imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                getImageThumbnail(context, item, imageId);
            }
        } else {
            if (queryType == QueryType.PHOTO) {
                item.put("thumbnail", item.get("nativeURL"));
            }
        }
        buffer.put(item);
    }

    private JSONObject getContent(JSONObject columns, ArrayList<String> columnNames, Cursor cursor) throws JSONException {
        JSONObject item = new JSONObject();
        for (String column : columnNames) {
            int columnIndex = cursor.getColumnIndex(columns.get(column).toString());

            if (column.startsWith("int.")) {
                item.put(column.substring(4), cursor.getInt(columnIndex));
                if (column.substring(4).equals("width") && item.getInt("width") == 0) {
                    System.err.println("cursor: " + cursor.getInt(columnIndex));
                }
            } else if (column.startsWith("float.")) {
                item.put(column.substring(6), cursor.getFloat(columnIndex));
            } else if (column.startsWith("date.")) {
                long intDate = cursor.getLong(columnIndex);
                Date date = new Date(intDate);
                item.put(column.substring(5), dateFormatter.format(date));
            } else {
                item.put(column, cursor.getString(columnIndex));
            }
        }

        return item;
    }

    private void getImageThumbnail(Context context, JSONObject item, long imageId) throws JSONException {

        Cursor thumbnailsCursor = MediaStore.Images.Thumbnails
                .queryMiniThumbnail(context.getContentResolver(),
                        imageId, MediaStore.Images.Thumbnails.MINI_KIND,
                        null);

        if (thumbnailsCursor != null && thumbnailsCursor.getCount() > 0) {
            thumbnailsCursor.moveToFirst();
            String thumbnail = thumbnailsCursor.getString(thumbnailsCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            item.put("thumbnail", thumbnail);
            thumbnailsCursor.close();
        }
    }

    private void getVideoThumbnail(Context context, JSONObject item, long videoID) throws JSONException {
        String[] projection = {
                MediaStore.Video.Media.DATA,
        };
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                new String[]{String.valueOf(videoID)},
                null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String thumbnail = cursor.getString(0);
            item.put("thumbnail", thumbnail);
            cursor.close();
        }
    }

}
