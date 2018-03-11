package com.yplay;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yplay.playlists.PlaylistObject;
import com.yplay.search.MediaObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class    DatabaseHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    private final Context context;
    private final String dbName;
    private final String dbPath;
    private SQLiteDatabase db;

    public DatabaseHandler(Context context, String dbName) {
        super(context, dbName, null, DB_VERSION);

        this.context = context;
        this.dbName = dbName;

        String dbFullPath = context.getDatabasePath(dbName).toString();
        Log.i("DatabaseHandler", dbFullPath);
        this.dbPath = dbFullPath.substring(0, dbFullPath.lastIndexOf("/") + 1);
    }

    /**
     * Check if the database already exists.
     */
    private boolean checkDatabase() {
        try {
            SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READONLY)
                    .close();
            return true;
        } catch (SQLiteCantOpenDatabaseException e) {
            e.printStackTrace();
            Log.i("DatabaseHandler", "No database was found.");
        }

        return false;
    }

    /**
     * Create an empty database on the system and overwrites it with a given database.
     * */
    private void createDatabase() {
        this.getWritableDatabase(); // Create and open an empty database in order to be overwritten.
        copyDatabase();
    }

    /**
     * Copy the database from your local assets folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * */
    private void copyDatabase() {
        try {
            InputStream inputStream = context.getAssets().open(dbName);

            OutputStream outputStream = new FileOutputStream(dbPath + dbName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("DatabaseHandler", "Something went wrong when trying to copy the database.");
        }
    }

    /**
     * Not an actual opening of the database, but rather check to see if it exists.
     * If it already exists do nothing, else create it.
     *
     * TODO: not sure if synchronized is required
     */
    public synchronized void open() {
        if (!checkDatabase()) {
            createDatabase();
        }
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
        }

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    /**
     * Delete any existing database.
     */
    public void delete() {
        context.deleteDatabase(dbName);
    }

    public List<PlaylistObject> loadPlaylists() {
        List<PlaylistObject> playlists = new ArrayList<>();

        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery(
                "SELECT id,name FROM playlists ORDER BY name ASC",
                new String[] {}
        );

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int count = countMediaFromPlaylist(cursor.getString(0));
            int duration = calculateDurationOfPlaylist(cursor.getString(0));
            playlists.add(new PlaylistObject(cursor.getString(1), cursor.getInt(0), count, duration)); // TODO: duration and count
        }

        cursor.close();

        return playlists;
    }

    /**
     * @param id The id of the playlist itself
     *
     */
    public ArrayList<MediaObject> loadMediaFromPlaylist(String id) {
        ArrayList<MediaObject> videos = new ArrayList<>();

        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery(
                "SELECT id,y_id,name,duration FROM videos WHERE p_id = ?",
                new String[] { id }
        );

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            videos.add(new MediaObject(cursor.getString(1), cursor.getString(2), cursor.getInt(3), "0"));
        }

        cursor.close();

        return videos;
    }

    public void addPlaylist(String name) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READWRITE);

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);

        db.insert("playlists", null, contentValues);
    }

    public void insertVideo(int pId, String yId, String name, int duration) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READWRITE);

        ContentValues contentValues = new ContentValues();
        contentValues.put("y_id", yId);
        contentValues.put("p_id", Integer.toString(pId));
        contentValues.put("name", name);
        contentValues.put("duration", duration);

        db.insert("videos", null, contentValues);
    }

    public void renamePlaylist(String id, String newName) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READWRITE);

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", newName);

        db.update("playlists", contentValues, "id = ?", new String[] { id });
    }

    public void deletePlaylist(String id) { // TODO!!!!: WHEN YOU DELETE A DATABASE, CHANGE THE IDs THE OTHERS AND CHANGE THE PID FOR THEIR RESPECTIVE VIDEOS. ALTERNATIVELY WHEN YOU ADD A NEW PLAYLIST, ADD IT TO THE LOWEST OPENED POSITION (ID) AND AVOID CHANGING THE PID FOR EACH VIDEO
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READWRITE);

        deleteVideosWithPId(id); // Also delete all the videos linked to this playlist.
        db.delete("playlists", "id = ?", new String[] { id });
    }

    private void deleteVideosWithPId(String id) {
        db.delete("videos", "p_id = ?", new String[] { id });
    }

    public void deleteVideo(String id, int p_id) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READWRITE);
        db.delete("videos", "y_id = ? AND p_id = ?", new String[] { id, String.valueOf(p_id) });
    }

    private int countMediaFromPlaylist(String id) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM videos WHERE p_id = ?",
                new String[] { id }
        );

        cursor.moveToFirst();
        int count = cursor.getInt(0);

        cursor.close();

        return count;
    }

    private int calculateDurationOfPlaylist(String id) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery(
                "SELECT SUM(duration) FROM videos WHERE p_id = ?",
                new String[] { id }
        );

        cursor.moveToFirst();
        int sum = cursor.getInt(0);

        cursor.close();

        return sum;
    }

    public String retrievePlaylistName(int id) {
        db = SQLiteDatabase.openDatabase(dbPath + dbName, null, SQLiteDatabase.OPEN_READONLY);

        Cursor cursor = db.rawQuery(
                "SELECT name FROM playlists WHERE id = ?",
                new String[] { String.valueOf(id) }
        );

        cursor.moveToFirst();
        String name = cursor.getString(0);

        cursor.close();

        return name;
    }

}
