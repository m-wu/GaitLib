
package org.spin.gaitlib.gaitlogger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class FileManagerUtil {

    public static final String DATA_FOLDER_PREFIX = "gaitlogger_";

    private static File gaitLoggerDirectory;

    public static File getGaitLoggerDirectory() {
        return gaitLoggerDirectory;
    }

    public static void setGaitLoggerDirectory(File gaitLoggerDirectory) {
        FileManagerUtil.gaitLoggerDirectory = gaitLoggerDirectory;
    }

    public static File getDataFoldersParentDirectory() {
        return gaitLoggerDirectory;
    }
    
    public static String getDataFolderPath(String folderName){
        return getDataFoldersParentDirectory().getAbsolutePath() + "/" + folderName;
    }
    
    public static void updateIndex(String fileName, Context context) {
        MediaScannerHelper msh = new MediaScannerHelper();
        msh.addFile(fileName, context);
    }
    
    public static class MediaScannerHelper implements MediaScannerConnectionClient {

        public void addFile(String filename, Context context) {
            String[] paths = new String[1];
            paths[0] = filename;
            MediaScannerConnection.scanFile(context, paths, null, this);
        }

        @Override
        public void onMediaScannerConnected() {
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            Log.i("ScannerHelper", "Scan done - path:" + path + " uri:" + uri);
        }
    }

}
