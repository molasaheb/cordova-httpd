package com.techprd.cordova.provider;

import android.content.Context;
import android.provider.MediaStore;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoLibraryService {

    private static PhotoLibraryService instance = null;
    private com.techprd.cordova.provider.ContentProviderService photoAlbumProvider;
    private ContentProviderService photoProvider;
    private ContentProviderService videoAlbumProvider;
    private ContentProviderService videoProvider;
    private ContentProviderService musicAlbumProvider;
    private ContentProviderService musicAlbumCoverProvider;
    private ContentProviderService musicProvider;

    private PhotoLibraryService() {
        this.photoAlbumProvider =
                new ContentProviderService(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, QueryType.PHOTO_ALBUM);
        this.photoProvider =
                new ContentProviderService(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, QueryType.PHOTO);
        this.videoAlbumProvider =
                new ContentProviderService(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, QueryType.VIDEO_ALBUM);
        this.videoProvider =
                new ContentProviderService(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, QueryType.VIDEO);
        this.musicAlbumProvider =
                new ContentProviderService(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, QueryType.MUSIC_ALBUM);
        this.musicProvider =
                new ContentProviderService(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, QueryType.MUSIC);
        this.musicAlbumCoverProvider =
                new ContentProviderService(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, QueryType.MUSIC_ALBUM_COVER);
    }

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

    public JSONObject getPhotoAlbums(Context context) throws JSONException {
        return photoAlbumProvider.setContext(context)
                .setWhereClause("1) GROUP BY 1,(2")
                .setSortOrder(MediaStore.Images.Media.DATE_TAKEN + " DESC")
                .queryContentProvider();
    }

    public JSONObject getVideoAlbums(Context context) throws JSONException {
        return videoAlbumProvider.setContext(context)
                .setWhereClause("1) GROUP BY 1,(2")
                .setSortOrder(MediaStore.Video.Media.DATE_TAKEN + " DESC")
                .queryContentProvider();
    }

    public JSONObject getMusicAlbums(Context context) throws JSONException {
        return musicAlbumProvider.setContext(context)
                .setWhereClause("1) GROUP BY 1,(2")
                .setSortOrder(MediaStore.Audio.AlbumColumns.ALBUM_ID + " DESC")
                .queryContentProvider();
    }

    public JSONObject getPhotos(Context context, String album, int limit, int offset) throws JSONException {
        return photoProvider.setContext(context)
                .setWhereClause(MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?")
                .setSelectionArgs(new String[]{album})
                .setSortOrder(MediaStore.Images.Media.DATE_TAKEN + " DESC ")
                .setLimit(limit)
                .setOffset(offset)
                .queryContentProvider();
    }

    public JSONObject getVideos(Context context, String album, int limit, int offset) throws JSONException {
        return videoProvider.setContext(context)
                .setWhereClause(MediaStore.Video.Media.BUCKET_DISPLAY_NAME + "=?")
                .setSelectionArgs(new String[]{album})
                .setSortOrder(MediaStore.Video.Media.DATE_TAKEN + " DESC ")
                .setLimit(limit)
                .setOffset(offset)
                .queryContentProvider();
    }

    public JSONObject getMusics(Context context) throws JSONException {
        return musicProvider.setContext(context)
                .setWhereClause(MediaStore.Audio.Media.IS_MUSIC + "!= 0")
                .setSortOrder(MediaStore.Audio.Media.DATE_ADDED + " DESC")
                .queryContentProvider();
    }

    public JSONObject getMusicAlbumCover(Context context, String albumId) throws JSONException {
        return musicAlbumCoverProvider.setContext(context)
                .setWhereClause(MediaStore.Audio.Albums._ID + " = ?")
                .setSelectionArgs(new String[]{albumId})
                .queryContentProvider();
    }
}
