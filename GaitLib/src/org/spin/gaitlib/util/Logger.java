
package org.spin.gaitlib.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A logger for GaitLib, recording raw sensor data, cadence and gait calculation results in CSV log
 * form. Write to the log by calling one of the print functions.
 * 
 * @author Mike
 */
public class Logger {

    private static final String TAG = "GaitLibLogger";
    private static final String FOLDER_NAME = "gaitlib";
    private static final String SUBFOLDER_PREFIX = "gaitlib_";
    private static final String DEFAULT_FILENAME_EXTENSION = ".csv";

    private final String filename;
    private final String filenameExtension;
    private final String headerLine;

    private PrintWriter printWriter = null;

    private boolean isEnabled = false;

    /**
     * Create a logger. Log files are in CSV format and stored at
     * gaitlib/gaitlib_yyyy-MM-dd/filename_yyyy-MM-dd_hh-mm-ss.csv
     * 
     * @param filename
     * @param filenameExtension in the format of ".csv".
     * @param headerLine Header in CSV format.
     */
    public Logger(String filename, String filenameExtension, String headerLine) {
        this.filename = filename;
        this.filenameExtension = filenameExtension != null ? filenameExtension
                : DEFAULT_FILENAME_EXTENSION;
        this.headerLine = headerLine;
    }

    /**
     * Close writing current file and start logging on a new file.
     */
    public void reset() {
        close();
        initPrintWriter();
    }

    /**
     * @return <code>true</code> if PrintWriter is created successfully, <code>false</code>
     *         otherwise.
     */
    private boolean initPrintWriter() {
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date();

        String dirString = FOLDER_NAME + "/" + SUBFOLDER_PREFIX + simpleDateFormat.format(date);
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + dirString);

        if (!dir.exists() && !dir.mkdirs())
        {
            Log.v(TAG, "Couldn't create directory for log files.");
        }

        String fileString = dateFormat.format(date) + filenameExtension;
        if (filename != null && filename.length() > 0) {
            fileString = filename + "_" + fileString;
        }
        File logFile = new File(dir, fileString);
        try {
            printWriter = new PrintWriter(logFile);
            if (headerLine != null && headerLine.length() > 0) {
                printWriter.print(headerLine);
                printWriter.print("\n");
            }
            return true;
        } catch (FileNotFoundException e) {
            Log.v(TAG, e.toString());
            return false;
        }
    }

    public void print(long timestamp, String value) {
        if (!isEnabled) {
            return;
        }
        printWriter.printf("%f," + value + "\n", timestamp);
    }

    public void print(long timestamp, String[] values) {
        if (!isEnabled) {
            return;
        }
        printWriter.printf("%f,", timestamp);
        printRow(values);
    }

    public void printTable(String[][] table) {
        if (!isEnabled) {
            return;
        }
        for (String[] row : table) {
            printRow(row);
        }
    }

    public void printRow(String[] row) {
        if (!isEnabled) {
            return;
        }
        for (int i = 0; i < row.length; i++) {
            if (i < row.length - 1) {
                printWriter.print(row[i] + ",");
            } else {
                printWriter.print(row[i]);
            }
        }
        printWriter.print("\n");
    }

    public void printColumn(String[] column) {
        if (!isEnabled) {
            return;
        }
        for (int i = 0; i < column.length; i++) {
            printWriter.println(column[i]);
        }
    }

    public void close() {
        if (isEnabled) {
            printWriter.close();
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.isEnabled == enabled) {
            return;
        }

        if (enabled) {
            boolean success = initPrintWriter();
            if (!success) {
                return;
            }
        } else {
            close();
        }
        this.isEnabled = enabled;
    }

}
