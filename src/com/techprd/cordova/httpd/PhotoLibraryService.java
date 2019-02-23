package com.techprd.cordova.httpd;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
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


    JSONObject getPhotoAlbums(Context context) throws JSONException {

        // All columns here: https://developer.android.com/reference/android/provider/MediaStore.Images.ImageColumns.html,
        // https://developer.android.com/reference/android/provider/MediaStore.MediaColumns.html
        JSONObject columns = new JSONObject() {{
            put("id", MediaStore.Images.ImageColumns.BUCKET_ID);
            put("title", MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        }};

        final String[] selectionArgs = null;
        final String whereClause = "1) GROUP BY 1,(2";
        final String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        return queryContentProvider(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                whereClause, selectionArgs, sortOrder, 0, 0);

    }

    JSONObject getVideoAlbums(Context context) throws JSONException {

        JSONObject columns = new JSONObject() {{
            put("id", MediaStore.Video.VideoColumns.BUCKET_ID);
            put("title", MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME);
        }};

        final String[] selectionArgs = null;
        final String whereClause = "1) GROUP BY 1,(2";
        final String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";

        return queryContentProvider(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                columns,
                whereClause, selectionArgs, sortOrder, 0, 0);

    }

    JSONObject getMusicAlbums(Context context) throws JSONException {

        // All columns here: https://developer.android.com/reference/android/provider/MediaStore.Images.ImageColumns.html,
        // https://developer.android.com/reference/android/provider/MediaStore.MediaColumns.html
        JSONObject columns = new JSONObject() {{
            put("id", MediaStore.Audio.AlbumColumns.ALBUM_ID);
            put("album", MediaStore.Audio.AlbumColumns.ALBUM);
            put("artist", MediaStore.Audio.AlbumColumns.ARTIST);
        }};

        final String[] selectionArgs = null;
        final String whereClause = "1) GROUP BY 1,(2";
        final String sortOrder = MediaStore.Audio.AlbumColumns.ALBUM_ID + " DESC";

        return queryContentProvider(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                columns,
                whereClause, selectionArgs, sortOrder, 0, 0);

    }

    JSONObject getPhotos(Context context, String album, int limit, int offset) throws JSONException {
        JSONObject columns = new JSONObject() {{
            put("int.id", MediaStore.Images.Media._ID);
            put("fileName", MediaStore.Images.ImageColumns.DISPLAY_NAME);
            put(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC, MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC);
            put("int.width", MediaStore.Images.ImageColumns.WIDTH);
            put("int.height", MediaStore.Images.ImageColumns.HEIGHT);
            put("albumId", MediaStore.Images.ImageColumns.BUCKET_ID);
            put("date.creationDate", MediaStore.Images.ImageColumns.DATE_TAKEN);
            put("float.latitude", MediaStore.Images.ImageColumns.LATITUDE);
            put("float.longitude", MediaStore.Images.ImageColumns.LONGITUDE);
            put("nativeURL", MediaStore.MediaColumns.DATA); // will not be returned to javascript
        }};

        final String whereClause = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
        final String[] selectionArgs = new String[]{album};
        final String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC ";

        return queryContentProvider(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                whereClause, selectionArgs, sortOrder, limit, offset);
    }

    JSONObject getPhotosThumbnail(Context context, String imagePath) throws JSONException {
        File thumbnailPath = thumbnailPathFromMediaId(media.getString("id"));
        if (thumbnailPath.exists()) {
            System.out.println("Thumbnail Already Exists!!!. Not Creating New One");
            media.put("thumbnail", thumbnailPath);
        } else {
            if (ops == null) {
                ops = new BitmapFactory.Options();
                ops.inJustDecodeBounds = false;
                ops.inSampleSize = 1;
            }
            media.put("error", "true");

            File image = new File(media.getString("data"));

            int sourceWidth = media.getInt("width");
            int sourceHeight = media.getInt("height");

            if (sourceHeight > 0 && sourceWidth > 0) {
                int destinationWidth, destinationHeight;

                if (sourceWidth > sourceHeight) {
                    destinationHeight = BASE_SIZE;
                    destinationWidth = (int) Math.ceil(destinationHeight * ((double) sourceWidth / sourceHeight));
                } else {
                    destinationWidth = BASE_SIZE;
                    destinationHeight = (int) Math.ceil(destinationWidth * ((double) sourceHeight / sourceWidth));
                }

                if (sourceWidth * sourceHeight > 600000 && sourceWidth * sourceHeight < 1000000) {
                    ops.inSampleSize = 4;
                } else if (sourceWidth * sourceHeight > 1000000) {
                    ops.inSampleSize = 8;
                }

                Bitmap originalImageBitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), ops); //creating bitmap of original image

                if (originalImageBitmap == null) {
                    ops.inSampleSize = 1;
                    originalImageBitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), ops);
                }

                if (originalImageBitmap != null) {

                    if (destinationHeight <= 0 || destinationWidth <= 0) {
                        System.out.println("destinationHeight: " + destinationHeight + "  destinationWidth: " + destinationWidth);
                    }

                    Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(originalImageBitmap, destinationWidth, destinationHeight, true);
                    originalImageBitmap.recycle();

                    if (thumbnailBitmap != null) {
                        int orientation = media.getInt("orientation");
                        if (orientation > 0)
                            thumbnailBitmap = rotate(thumbnailBitmap, orientation);

                        byte[] thumbnailData = getBytesFromBitmap(thumbnailBitmap);
                        thumbnailBitmap.recycle();
                        if (thumbnailData != null) {
                            FileOutputStream outStream;
                            try {
                                outStream = new FileOutputStream(thumbnailPath);
                                outStream.write(thumbnailData);
                                outStream.close();
                            } catch (IOException e) {
                                Log.e("Mendr", "Couldn't write the thumbnail image data");
                                e.printStackTrace();
                            }

                            if (thumbnailPath.exists()) {
                                System.out.println("Thumbnail didn't Exists!!!. Created New One");
                                media.put("thumbnail", thumbnailPath);
                                media.put("error", "false");
                            }
                        } else
                            Log.e("Mendr", "Couldn't convert thumbnail bitmap to byte array");
                    } else
                        Log.e("Mendr", "Couldn't create the thumbnail bitmap");
                } else
                    Log.e("Mendr", "Couldn't decode the original image");
            } else
                Log.e("Mendr", "Invalid Media!!! Image width or height is 0");
        }

        return media;
    }

    JSONObject getVideos(Context context, String album, int limit, int offset) throws JSONException {
        JSONObject columns = new JSONObject() {{
            put("int.id", MediaStore.Video.Media._ID);
            put("fileName", MediaStore.Video.VideoColumns.DISPLAY_NAME);
            put(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC, MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC);
            put("int.width", MediaStore.Video.VideoColumns.WIDTH);
            put("int.height", MediaStore.Video.VideoColumns.HEIGHT);
            put("albumId", MediaStore.Video.VideoColumns.BUCKET_ID);
            put("date.creationDate", MediaStore.Video.VideoColumns.DATE_TAKEN);
            put("float.latitude", MediaStore.Video.VideoColumns.LATITUDE);
            put("float.longitude", MediaStore.Video.VideoColumns.LONGITUDE);
            put("nativeURL", MediaStore.MediaColumns.DATA);
        }};

        final String whereClause = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + "=?";
        final String[] selectionArgs = new String[]{album};
        final String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC ";

        return queryContentProvider(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns,
                whereClause, selectionArgs, sortOrder, limit, offset);
    }

    JSONObject getMusics(Context context) throws JSONException {
        JSONObject columns = new JSONObject() {{
            put("int.id", MediaStore.Audio.Media._ID);
            put("artist", MediaStore.Audio.Media.ARTIST);
            put("album", MediaStore.Audio.Media.ALBUM);
            put("title", MediaStore.Audio.Media.TITLE);
            put("albumId", MediaStore.Audio.Media.ALBUM_ID);
            put("int.duration", MediaStore.Audio.Media.DURATION);
            put("nativeURL", MediaStore.MediaColumns.DATA);
            put("int.date_added", MediaStore.Audio.Media.DATE_ADDED);
            put("display_name", MediaStore.Audio.Media.DISPLAY_NAME);
        }};

        final String whereClause = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        final String[] selectionArgs = new String[]{};
        final String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        return queryContentProvider(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns,
                whereClause, selectionArgs, sortOrder, 0, 0);
    }

    JSONObject getMusicAlbumCover(Context context, String albumId) throws JSONException {

        JSONObject columns = new JSONObject() {{
            put("album_art", MediaStore.Audio.Albums.ALBUM_ART);
            put("album_id", MediaStore.Audio.Albums._ID);
        }};

        final String whereClause = MediaStore.Audio.Albums._ID + " = ?";
        final String[] selectionArgs = new String[]{albumId};
        final String sortOrder = null;

        return queryContentProvider(context, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, columns,
                whereClause, selectionArgs, sortOrder, 0, 0);
    }

    private JSONObject queryContentProvider(Context context,
                                            Uri collection,
                                            JSONObject columns,
                                            String whereClause,
                                            String[] selectionArgs,
                                            String sortOrder,
                                            int limit,
                                            int offset) throws JSONException {

        final ArrayList<String> columnNames = new ArrayList<>();
        final ArrayList<String> columnValues = new ArrayList<>();

        Iterator<String> iteratorFields = columns.keys();

        while (iteratorFields.hasNext()) {
            String column = iteratorFields.next();

            columnNames.add(column);
            columnValues.add("" + columns.getString(column));
        }

        final Cursor cursor = context.getContentResolver().query(
                collection,
                columnValues.toArray(new String[columns.length()]),
                whereClause, selectionArgs, sortOrder);

        final JSONArray buffer = new JSONArray();
        JSONObject output = new JSONObject();
        JSONObject metadata = new JSONObject();
        if (limit > 0) {
            if (cursor != null && cursor.move(offset)) {
                do {
                    JSONObject item = getContent(columns, columnNames, cursor);
                    if (item.has(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)) {
                        if (collection == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                            long videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                            getVideoThumbnail(context, item, videoId);
                        } else {
                            long imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                            getImageThumbnail(context, item, imageId);
                        }
                    }
                    buffer.put(item);
                    cursor.moveToNext();
                } while (!cursor.isAfterLast() && (limit + offset) > cursor.getPosition());
                metadata.put("count", cursor.getCount());
                metadata.put("position", cursor.getPosition());
                cursor.close();
            }
        } else {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject item = getContent(columns, columnNames, cursor);
                    if (item.has(MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC)) {
                        if (collection == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                            long videoId = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                            getVideoThumbnail(context, item, videoId);
                        } else {
                            long imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                            getImageThumbnail(context, item, imageId);
                        }
                    }
                    buffer.put(item);
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
