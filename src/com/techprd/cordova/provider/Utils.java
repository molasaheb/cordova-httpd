package com.techprd.cordova.provider;

import android.provider.MediaStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Utils {
    private static JSONObject getPhotoAlbumColumns() throws JSONException {
        return new JSONObject() {{
            put("id", MediaStore.Images.ImageColumns.BUCKET_ID);
            put("title", MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
        }};
    }

    private static JSONObject getVideoAlbumColumns() throws JSONException {
        return new JSONObject() {{
            put("id", MediaStore.Video.VideoColumns.BUCKET_ID);
            put("title", MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME);
        }};
    }

    private static JSONObject getMusicAlbumColumns() throws JSONException {
        return new JSONObject() {{
            put("id", MediaStore.Audio.AlbumColumns.ALBUM_ID);
            put("album", MediaStore.Audio.AlbumColumns.ALBUM);
            put("artist", MediaStore.Audio.AlbumColumns.ARTIST);
        }};
    }

    private static JSONObject getPhotoColumns() throws JSONException {
        return new JSONObject() {{
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
    }

    private static JSONObject getVideoColumns() throws JSONException {
        return new JSONObject() {{
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
    }

    private static JSONObject getMusicColumns() throws JSONException {
        return new JSONObject() {{
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
    }

    private static JSONObject getMusicAlbumCoverColumns() throws JSONException {
        return new JSONObject() {{
            put("album_art", MediaStore.Audio.Albums.ALBUM_ART);
            put("album_id", MediaStore.Audio.Albums._ID);
        }};
    }

    static JSONObject getQueryColumnsByType(com.techprd.cordova.provider.QueryType type) throws JSONException {
        JSONObject jsonObject = null;
        switch (type) {
            case PHOTO_ALBUM:
                jsonObject = getPhotoAlbumColumns();
                break;
            case PHOTO:
                jsonObject = getPhotoColumns();
                break;
            case VIDEO_ALBUM:
                jsonObject = getVideoAlbumColumns();
                break;
            case VIDEO:
                jsonObject = getVideoColumns();
                break;
            case MUSIC_ALBUM_COVER:
                jsonObject = getMusicAlbumCoverColumns();
                break;
            case MUSIC_ALBUM:
                jsonObject = getMusicAlbumColumns();
                break;
            case MUSIC:
                jsonObject = getMusicColumns();
                break;
            default:
                break;
        }

        return jsonObject;
    }

    static ArrayList<String> getColumnsKeys(JSONObject columns) {
        final ArrayList<String> columnKeys = new ArrayList<>();
        Iterator<String> iteratorFields = columns.keys();
        while (iteratorFields.hasNext()) {
            String column = iteratorFields.next();
            columnKeys.add(column);
        }

        return columnKeys;
    }

    static ArrayList<String> getColumnsValues(JSONObject columns) throws JSONException {
        final ArrayList<String> columnValues = new ArrayList<>();
        Iterator<String> iteratorFields = columns.keys();
        while (iteratorFields.hasNext()) {
            String column = iteratorFields.next();
            columnValues.add("" + columns.getString(column));
        }
        return columnValues;
    }
}
