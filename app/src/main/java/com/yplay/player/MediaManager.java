package com.yplay.player;

import android.content.Context;
import android.provider.Settings;

import com.yplay.BuildConfig;
import com.yplay.search.MediaObject;
import com.yplay.ypprotocol.YPPPackage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MediaManager {

    private static final int BUFFER_SIZE = 8192;

    public static String fetchMedia(Context context, MediaObject mediaObject) throws IOException {
        String directoryPath = context.getCacheDir().getPath() + "/files/";

        if (!alreadyFetched(context, mediaObject)) {
            String androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID); // TODO: find a better solution

            Socket socket = new Socket(BuildConfig.SERVER_ADDRESS, BuildConfig.PORT);
            requestConversion(socket, androidId, mediaObject);
            downloadMedia(socket, directoryPath, mediaObject);
            socket.close();
        }

        return directoryPath + mediaObject.getId() + ".opus";
    }

    private static void requestConversion(Socket socket, String androidId, MediaObject mediaObject) throws IOException {
        YPPPackage yppPackage = new YPPPackage(mediaObject.getId(), androidId, (byte) 0);
        socket.getOutputStream().write(yppPackage.asRequest());
    }

    private static void downloadMedia(Socket socket, String directoryPath, MediaObject mediaObject) throws IOException {
        FileOutputStream out = null;
        DataInputStream in = null;

        try {
            new File(directoryPath).mkdirs();

            out = new FileOutputStream(new File(directoryPath + mediaObject.getId() + ".opus"));
            in = new DataInputStream(socket.getInputStream());

            int count;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((count = in.read(buffer)) >= 0) {
                out.write(buffer, 0, count);
            }
        } finally {
            if (out != null) {
                out.close();
            }

            if (in != null) {
                in.close();
            }
        }
    }

    private static boolean alreadyFetched(Context context, MediaObject mediaObject) {
        return new File(context.getCacheDir() + "/files/" + mediaObject.getId() + ".opus").exists();
    }

}
