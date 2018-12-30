package com.techprd.cordova.httpd;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class PhotoLibraryService {

    private SimpleDateFormat dateFormatter;
    private static PhotoLibraryService instance = null;
    private Pattern dataURLPattern = Pattern.compile("^data:(.+?)/(.+?);base64,");

    private PhotoLibraryService() {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static final String PERMISSION_ERROR = "Permission Denial: This application is not allowed to access Photo data.";

    public static PhotoLibraryService getInstance() {
        if (instance == null) {
            synchronized (PhotoLibraryService.class) {
                if (instance == null) {
                    instance = new PhotoLibraryService();
                }
            }
        }
        return instance;
    }


    public ArrayList<JSONObject> getAlbums(Context context) throws JSONException {

        // All columns here: https://developer.android.com/reference/android/provider/MediaStore.Images.ImageColumns.html,
        // https://developer.android.com/reference/android/provider/MediaStore.MediaColumns.html
        JSONObject columns = new JSONObject() {{
            put("id", MediaStore.Images.ImageColumns.BUCKET_ID);
            put("title", MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        }};

        final ArrayList<JSONObject> queryResult = queryContentProvider(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, "1) GROUP BY 1,(2");

        return queryResult;

    }

    private ArrayList<JSONObject> queryContentProvider(Context context, Uri collection, JSONObject columns, String whereClause) throws JSONException {

        final ArrayList<String> columnNames = new ArrayList<String>();
        final ArrayList<String> columnValues = new ArrayList<String>();

        Iterator<String> iteratorFields = columns.keys();

        while (iteratorFields.hasNext()) {
            String column = iteratorFields.next();

            columnNames.add(column);
            columnValues.add("" + columns.getString(column));
        }

        final String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        final Cursor cursor = context.getContentResolver().query(
                collection,
                columnValues.toArray(new String[columns.length()]),
                whereClause, null, sortOrder);

        final ArrayList<JSONObject> buffer = new ArrayList<JSONObject>();

        assert cursor != null;
        if (cursor.moveToFirst()) {
            do {
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
                buffer.add(item);

                // TODO: return partial result

            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return buffer;

    }

}
