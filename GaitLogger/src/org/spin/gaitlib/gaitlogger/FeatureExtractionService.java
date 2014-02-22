
package org.spin.gaitlib.gaitlogger;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;

import org.spin.gaitlib.gait.DefaultGaitClassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class FeatureExtractionService extends Service {

    public static final String STATUS_UPDATE = "org.spin.gaitlib.gaitlogger.statusupdate";
    public static final String STATUS_UPDATE_TYPE = "org.spin.gaitlib.gaitlogger.statusupdatetype";
    public static final String STATUS_MESSAGE = "org.spin.gaitlib.gaitlogger.statusmessage";
    
    private FeatureExtractionTask task;

    // File management
    private String pathToCSVFiles;
    private String arffOutputPath;
    private String arffFileName;
    private File outputArffFile;
    private File outputXmlFile;

    // Database
    private final String DATABASE_TABLE_NAME = "accel"; // the name of the table in the database
    private final ArrayList<SQLiteDatabase> accelDBList = new ArrayList<SQLiteDatabase>();
    private LinkedHashMap<String, BigInteger[]> trialTimestamps;

    // Feature extraction parameters
    private static final int DEFAULT_WINDOW_SIZE = 2000;
    private static final int DEFAULT_SAMPLING_INTERVAL = 1000;
    private static final int DEFAULT_MIN_DATA_POINTS = 30;
    /**
     * The size of each sampling window, measured in milliseconds.
     */
    private int windowSize;
    /**
     * The interval at which sampling occurs, measured in milliseconds.
     */
    private int samplingInterval;
    /**
     * The minimum number of data points within each sampling window.
     */
    private int minDataPoints;

    private final int NUMBER_DIGITS_ALLOWED_IN_TIMESTAMP = 16;

    // the number of digits allowed in the timestamp. timestamps will be padded with leading zeros
    // to ensure each has this many digits.
    // This sets an upper limit on the time that the gait logging must be done.

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        broadcastStatusMessage("Service started.");
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMMdd");

        String folderName = intent.getStringExtra(FeatureExtractionActivity.SELECTED_DATA_FOLDER);
        arffOutputPath = FileManagerUtil.getDataFolderPath(folderName);
        arffFileName = folderName + simpleDateFormat.format(new Date()) + "-";
        pathToCSVFiles = FileManagerUtil.getDataFolderPath(folderName);

        windowSize = intent.getIntExtra(FeatureExtractionActivity.PARAM_WINDOW_SIZE,
                DEFAULT_WINDOW_SIZE);
        samplingInterval = intent.getIntExtra(FeatureExtractionActivity.PARAM_SAMPLING_INTERVAL,
                DEFAULT_SAMPLING_INTERVAL);
        minDataPoints = intent.getIntExtra(FeatureExtractionActivity.PARAM_MIN_DATA_POINTS,
                DEFAULT_MIN_DATA_POINTS);

        task = new FeatureExtractionTask();
        task.execute(null, null, null);

        return super.onStartCommand(intent, flags, startId);
    }
    
    private class FeatureExtractionTask extends AsyncTask<URL, Integer, Long> {
        
        @Override
        protected Long doInBackground(URL... urls) {
         // Find CSV data files
            String[] accel = {
                    "accel"
            };
            String[] study = {
                    "study"
            };
            String[] csv = {
                    "csv"
            };
            try {
                File[] accelFiles = CsvFileFinder.scanForFiles(pathToCSVFiles, accel, csv, null, true);
                File[] studyFiles = CsvFileFinder.scanForFiles(pathToCSVFiles, study, csv, null, true);

                // in lieu of creating the .sql and then the .db file;
                // creates and initializes the SQLiteDatabases, stored in accelDBList,
                // one for each accel-study CSV file pairing found at the specified file path
                if (createDatabases(DATABASE_TABLE_NAME, accelFiles, studyFiles) && !isCancelled()) {
                    extractAndOutputFeatures();
                }
            } catch (Exception e) {
                broadcastStatusMessage("Exception: " + e.getMessage());
            }
            return Long.valueOf(0);
        }

        // This is called each time you call publishProgress()
//        @Override
//        protected void onProgressUpdate(Integer... progress) {
////            setProgressPercent(progress[0]);
//        }

        // This is called when doInBackground() is finished
        @Override
        protected void onPostExecute(Long result) {
            FeatureExtractionService.this.stopSelf();
        }
    }

    /**
     * Find accelFile and studyFile pair. Creates and populates a database data structure using the
     * data found in corresponding accel and study CSV files Note that pathForCSVFiles should
     * already be set.
     * 
     * @param tableName the name of the table of each database to be created
     */
    public boolean createDatabases(String tableName, File[] accelFiles, File[] studyFiles) {
        if (accelFiles == null || accelFiles.length == 0) {
            broadcastStatusMessage("No data files.");
            return false;
        }

        if (accelFiles.length == 1){
            broadcastStatusMessage("There is 1 accelerometer data file.");
        } else {
            broadcastStatusMessage("There are " + accelFiles.length + " accelerometer data files.");
        }
        
        for (File accelFile : accelFiles)
        {
            if (accelFile != null && accelFile.isFile())
            {
                // find appropriate study file by matching the part of each file name that contains
                // the participant ID
                broadcastStatusMessage("accel file name: " + accelFile.getName());
                File studyFileFound = findMatchingStudyFile(studyFiles, accelFile);

                if (studyFileFound == null) { // if studyFileFound is still null, no appropriate
                                              // study file was found
                    broadcastStatusMessage("Error: no study file matching " + accelFile);
                    return false;
                }
                else {
                    // creates a database for each accelFile
                    createAndPopulateDatabase(accelFile, studyFileFound, tableName);
                }
            }
        }
        return true;
    }

    /**
     * @param studyFiles
     * @param accelFile
     * @return
     */
    private File findMatchingStudyFile(File[] studyFiles, File accelFile) {
        int pilotIndex = accelFile.getName().lastIndexOf("Participant");
        if (pilotIndex == -1) {
            pilotIndex = accelFile.getName().lastIndexOf("Test");
        }
        if (pilotIndex == -1) {
            pilotIndex = accelFile.getName().lastIndexOf("Pilot");
        }
        if (pilotIndex == -1) {
            return null;
        }

        String pilotText = accelFile.getName().substring(pilotIndex,
                accelFile.getName().length() - 1);
        for (File studyFile : studyFiles) {
            if (studyFile.getName().lastIndexOf(pilotText) >= 0) {
                if (studyFile.getName().lastIndexOf("gyro") == -1) { // if it is not a gyro file
                    broadcastStatusMessage("study file name: " + studyFile.getName());
                    return studyFile;
                }
            }
        }
        return null;
    }

    /**
     * Creates a SQLite database table using the found accel and study files.
     * 
     * @param accelFile the accel file, produced by GaitLogger app (within "GaitLogger")
     * @param studyFile the study file, produced by GaitLogger app (within "StudyRunner")
     * @param tableName name of the table used in creation of the SQLite database data structure
     */
    private void createAndPopulateDatabase(File accelFile, File studyFile, String tableName) {
        // initialize variables
        Hashtable<String, BigInteger> phoneCalibInStudyFile = new Hashtable<String, BigInteger>();
        trialTimestamps = new LinkedHashMap<String, BigInteger[]>();

        // Read in study file, extract phoneOffsets and trial timestamps
        BufferedReader studyIn = null;
        try {
            studyIn = new BufferedReader(new InputStreamReader(new FileInputStream(
                    studyFile)));
            studyIn.readLine(); // skip the first line in the study file

            BigInteger curStart = null, curEnd = null;
            String curGait = null, studyLine;

            while ((studyLine = studyIn.readLine()) != null) {
                String eventNameSetOffset = "Set ";
                String eventNameStartGait = "Start ";
                String eventNameStopGait = "Stop";

                String eventName = studyLine.trim().split(",")[0];
                String eventTimestamp = studyLine.trim().split(",")[1];

                if (eventName.startsWith(eventNameStartGait)) {
                    curGait = eventName.substring(eventNameStartGait.length());
                    curStart = new BigInteger(eventTimestamp);
                } else if (eventName.startsWith(eventNameStopGait)) {
                    curEnd = new BigInteger(eventTimestamp);
                    BigInteger[] startEndTimestamps = {
                            curStart, curEnd
                    };
                    trialTimestamps.put(curGait, startEndTimestamps);
                } else if (eventName.startsWith(eventNameSetOffset)) {
                    String phoneID = eventName.substring(eventNameSetOffset.length());
                    phoneCalibInStudyFile.put(phoneID, new BigInteger(eventTimestamp));
                } else {
                    throw new Exception("Incorrect format " + eventName + " in " + studyFile + ".");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (studyIn != null) {
                try {
                    studyIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Start analyzing accel file
        BufferedReader accelIn = null;
        try {
            accelIn = new BufferedReader(new InputStreamReader(new FileInputStream(accelFile)));            
            String participant = accelIn.readLine().trim().split(",")[1]; // participant
            String phoneID = accelIn.readLine().trim().split(",")[1]; // phoneID
            String location = accelIn.readLine().trim().split(",")[1]; // location on body
            BigInteger calibAccel = new BigInteger(accelIn.readLine().trim().split(",")[1]); // offset
            accelIn.readLine(); // skip the headers; should be t, x, y, z

            Iterator<Entry<String, BigInteger[]>> trialIterator = trialTimestamps.entrySet()
                    .iterator();
            Entry<String, BigInteger[]> currentTrial = trialIterator.next();
            String gait = currentTrial.getKey();
            BigInteger trialStart = currentTrial.getValue()[0];
            BigInteger trialEnd = currentTrial.getValue()[1];

            // Create database table using values from accel file and study file
            SQLiteDatabase accelDB = createDatabase(tableName);

            int rowIndex = 0;
            String accelLine;
            broadcastStatusMessage("Inserting rows.");
            while ((accelLine = accelIn.readLine()) != null && !task.isCancelled()) {
                String[] accelLineValues = accelLine.trim().split(",");
                BigInteger timeAccel = new BigInteger(accelLineValues[0]);
                BigInteger calibStudy = phoneCalibInStudyFile.get(phoneID);
                BigInteger t = (timeAccel).subtract(calibAccel).add(calibStudy);
                if (t.compareTo(trialStart) >= 0) {
                    if (t.compareTo(trialEnd) <= 0) { // inside the period of a trial
                        String xString = accelLineValues[1];
                        String yString = accelLineValues[2];
                        String zString = accelLineValues[3];

                        // Note that timestamps recording in the database have been padded with
                        // leading zeros and a leading 1 in order to ensure that the leading zeros
                        // are not removed during subsequent BigInteger calculations."
                        String timestampWithLeadingOne = convertTimestampToString(t);

                        insertRowIntoTable(accelDB, tableName, participant, phoneID, location,
                                gait, rowIndex, xString, yString, zString, timestampWithLeadingOne);
                        rowIndex++;
                    }
                    else {
                        if (trialIterator.hasNext()) {
                            currentTrial = trialIterator.next();
                            gait = currentTrial.getKey();
                            trialStart = currentTrial.getValue()[0];
                            trialEnd = currentTrial.getValue()[1];
                        } else {
                            break;
                        }
                    }
                }
            }
            accelIn.close();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (accelIn != null) {
                try {
                    accelIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param tableName
     * @return a database containing a table named <code>tableName</code>.
     */
    private SQLiteDatabase createDatabase(String tableName) {
        broadcastStatusMessage("Creating a database.");
        AccelOpenHelper accelOpenHelper = new AccelOpenHelper(getApplicationContext(), tableName);
        // sets table name that will be used when the database is created
        broadcastStatusMessage("Table name: " + tableName);

        SQLiteDatabase accelDB = accelOpenHelper.getWritableDatabase();
        accelDB.execSQL("DELETE FROM " + tableName); // clear existing table
        accelDBList.add(accelDB);
        broadcastStatusMessage("Maximum size the database can grow to (bytes): "
                + accelDB.getMaximumSize()); // roughly 1 TB
        return accelDB;
    }

    private void insertRowIntoTable(SQLiteDatabase db, String tableName, String participant,
            String phoneID, String location, String gait, int rowIndex, String xString,
            String yString, String zString, String timestampWithLeadingOne) {
        ContentValues dataValues = new ContentValues();
        // creates the object that stores the values to be inserted into this row of the database
        dataValues.put("Timestamp", timestampWithLeadingOne);
        dataValues.put("PhoneID", phoneID);
        dataValues.put("ParticipantID", participant);
        dataValues.put("Location", location);
        dataValues.put("Gait", gait);
        dataValues.put("X", xString);
        dataValues.put("Y", yString);
        dataValues.put("Z", zString);
        dataValues.put("RowNum", rowIndex);

        broadcastStatusMessage("Inserting row " + rowIndex, FeatureExtractionActivity.UPDATE_TYPE_REPLACEMENT);
        db.insert(tableName, null, dataValues);
    }

    /**
     * @param t
     * @return timestamp in {@link String} format 
     */
    private String convertTimestampToString(BigInteger t) {
        // Pad timestamps with zeros to allow for numeric comparison, despite being stored as a
        // string
        String tString = t.toString();
        int numTimestampDigits = tString.length();
        int numZerosForPadding = NUMBER_DIGITS_ALLOWED_IN_TIMESTAMP - numTimestampDigits;
        String newString = tString;
        String zero = "0";
        for (int l = 0; l < numZerosForPadding; l++) {
            newString = zero.concat(newString);
        }

        // Add a leading one to the timestamp so that the leading zeros are not removed during
        // subsequent BigInteger calculations!
        String finalTimestampWithLeadingOne = "1";
        finalTimestampWithLeadingOne = finalTimestampWithLeadingOne.concat(newString);
        return finalTimestampWithLeadingOne;
    }

    /**
     * Creates the databases for each accelerometer logging study, performs a feature extraction on
     * each 2 second window, and outputs an .arff file for each database.
     */
    public void extractAndOutputFeatures() {
        // feature extraction will be performed on a smaller window if a window of this length
        // contains data for more than one gait
        // because it will perform the feature extraction on the data for the first gait only if
        // there is more than one gait in this window
        Cursor maxTimestampInDatabaseCursor;

        broadcastStatusMessage("Going through each database and perform a feature extraction on each valid window.");
        // Perform a feature extraction on every (2 second) window of the accelerometer data of each
        // database
        for (SQLiteDatabase database : accelDBList) {
            if (task.isCancelled()){
                return;
            }
            maxTimestampInDatabaseCursor = database.query(DATABASE_TABLE_NAME, null, null, null,
                    null, null,
                    "RowNum ASC");
            // get the maximum timestamp in this database, so as to know when to stop retrieving
            // windows and performing feature extractions

            maxTimestampInDatabaseCursor.moveToLast();
            BigInteger lastTimestamp = new BigInteger(
                    maxTimestampInDatabaseCursor.getString(AccelOpenHelper.TIMESTAMP_INDEX));

            int distinguisher = 0;
            outputArffFile = new File(arffOutputPath + "/" + arffFileName + distinguisher + ".arff");
            outputXmlFile = new File(arffOutputPath + "/" + arffFileName + distinguisher + ".xml");
            // to ensure that each database produces a unique arff file
            try {
                while (!outputArffFile.createNewFile()) { // create the ARFF File; if it already
                                                          // exists, try again with a different name
                    distinguisher++;
                    outputArffFile = new File(arffOutputPath + "/" + arffFileName + distinguisher + ".arff");
                    outputXmlFile = new File(arffOutputPath + "/" + arffFileName + distinguisher + ".xml");
                    // to ensure that each database produces a unique arff file
                }
                broadcastStatusMessage("Empty ARFF and XML files created.");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            FileManagerUtil.updateIndex(outputArffFile.getAbsolutePath(), FeatureExtractionService.this);
            FileManagerUtil.updateIndex(outputXmlFile.getAbsolutePath(), FeatureExtractionService.this);
            broadcastStatusMessage("ARFF file path: " + outputArffFile.getAbsolutePath());

            // Get the names of the gaits in order to prepare the attributes in the ARFF file
            String sqlToGetGaits = "SELECT DISTINCT Gait FROM accel";
            Cursor gaitNamesCursor = database.rawQuery(sqlToGetGaits, null);
            gaitNamesCursor.moveToFirst();
            ArrayList<String> gaitNames = new ArrayList<String>();
            while (!gaitNamesCursor.isAfterLast()) {
                gaitNames.add(gaitNamesCursor.getString(0));
                gaitNamesCursor.moveToNext();
            }

            // Prepare ARFF file
            ArffXmlFileWriter.write(outputArffFile, outputXmlFile, gaitNames);

            // Get the first timestamp in this database
            Cursor firstTimestampInDBCursor = database.query(DATABASE_TABLE_NAME, null, null, null,
                    null, null, "RowNum ASC");
            // Cursor firstTimestampInDBCursor =
            // database.rawQuery("SELECT * FROM accel ORDER BY Timestamp ASC", null);
            firstTimestampInDBCursor.moveToFirst();

            BigInteger minTimestamp = new BigInteger(
                    firstTimestampInDBCursor.getString(AccelOpenHelper.TIMESTAMP_INDEX));

            // For this database, perform a feature extraction on the data corresponding to each
            // window of size windowSize. If a window has data for more than one gait, the
            // feature extraction is performed on the first gait and the next window will begin
            // where the last feature extraction left off

            while (lastTimestamp.compareTo(minTimestamp) > 0 && !task.isCancelled()) {
                // while extracting the next window will result in an extraction of at least 1 row
                // i.e. while there are still rows in the database that a feature extraction has not
                // been performed on
                String minTimestampString = minTimestamp.toString();
                // maximum timestamp permitted in this window, in nanoseconds
                String maxTimestampString = (minTimestamp.add(getWindowSizeInNs())).toString();

                String[] samplingQueryParams = {
                        maxTimestampString, minTimestampString
                };
                String samplingQuery = "Timestamp < ? AND Timestamp >= ?";
                String samplingQueryOrder = "Gait ASC, RowNum ASC";
                Cursor firstGaitCursor = database.query(DATABASE_TABLE_NAME, null,
                        samplingQuery, samplingQueryParams, null, null, samplingQueryOrder);

                if (firstGaitCursor.getCount() >= minDataPoints) {
                    firstGaitCursor.moveToFirst();

                    // Query data points with gait consistent to the first gait in the window

                    String windowGait = firstGaitCursor.getString(AccelOpenHelper.GAIT_INDEX);
                    String[] samplingQueryParamsWithGait = {
                            maxTimestampString, minTimestampString, windowGait
                    };
                    String samplingQueryWithGait = "Timestamp < ? AND Timestamp >= ? AND Gait = ?";

                    Cursor resultSetCursor = database.query(DATABASE_TABLE_NAME, null,
                            samplingQueryWithGait, samplingQueryParamsWithGait, null, null,
                            samplingQueryOrder);
                    
                    int numDataPoints = resultSetCursor.getCount();
                    if (numDataPoints >= minDataPoints) {
                        extractAndWriteFeaturesToFile(resultSetCursor, windowGait, outputArffFile);
                        broadcastStatusMessage("Current window: " + numDataPoints + " valid data points, added.");

                        // update minimum timestamp for the next sampling window
                        minTimestamp = minTimestamp.add(getSamplingIntervalInNs());
                    } else {
                        broadcastStatusMessage("Current window: " + numDataPoints + " valid data points, ignored.");
                        // update minimum timestamp for the next sampling window
                        resultSetCursor.moveToLast();
                        minTimestamp = new BigInteger(
                                resultSetCursor.getString(AccelOpenHelper.TIMESTAMP_INDEX))
                                .add(BigInteger.valueOf(1));
                    }

                }
                else {
                    broadcastStatusMessage("Current window: " + firstGaitCursor.getCount() + " data points, ignored.");
                    // update minimum timestamp for the next sampling window
                    minTimestamp = minTimestamp.add(getSamplingIntervalInNs());
                }
            }

            broadcastStatusMessage("Feature extraction is completed!");
        }
    }

    private BigInteger getWindowSizeInNs() {
        return BigInteger.valueOf(convertMsToNs(windowSize));
    }

    private BigInteger getSamplingIntervalInNs() {
        return BigInteger.valueOf(convertMsToNs(samplingInterval));
    }

    private int convertMsToNs(int timeInMs) {
        return timeInMs * 1000000;
    }

    /**
     * Extract features of a single window of data and write them to the arff file.
     * 
     * @param sqLiteDatabaseCursor the cursor of a query result from a SQLiteDatabase that contains
     *            the accelerometer data for the desired window
     * @param windowGait the name of the gait for the current window
     * @param arffFile the file to which the features are written.
     */
    private void extractAndWriteFeaturesToFile(Cursor sqLiteDatabaseCursor, String windowGait, File arffFile)
    {
        float[] signalX_float, signalY_float, signalZ_float, signalTime_float, zeroes_float;

        int numRows = sqLiteDatabaseCursor.getCount();// number of time samplings
        sqLiteDatabaseCursor.moveToFirst(); // moves cursor to first row of table

        signalX_float = new float[numRows];
        signalY_float = new float[numRows];
        signalZ_float = new float[numRows];
        signalTime_float = new float[numRows];
        zeroes_float = new float[numRows];

        float[][] signal_float = {
                signalX_float, signalY_float, signalZ_float
        };

        // go through each row of the accelerometer data and place its axes data into the
        // corresponding array
        int i = 0; // each i corresponds to one line in the database (i.e. one time sampling)
        while (!sqLiteDatabaseCursor.isAfterLast()) {
            String xString = sqLiteDatabaseCursor.getString(AccelOpenHelper.X_INDEX);
            String yString = sqLiteDatabaseCursor.getString(AccelOpenHelper.Y_INDEX);
            String zString = sqLiteDatabaseCursor.getString(AccelOpenHelper.Z_INDEX);
            String timeString = sqLiteDatabaseCursor.getString(AccelOpenHelper.TIMESTAMP_INDEX);

            signalX_float[i] = Float.parseFloat(xString);
            signalY_float[i] = Float.parseFloat(yString);
            signalZ_float[i] = Float.parseFloat(zString);
            signalTime_float[i] = Float.parseFloat(timeString) / 1000000000;
            zeroes_float[i] = 0.0f;

            i++;
            sqLiteDatabaseCursor.moveToNext();
        }

        double[] features = DefaultGaitClassifier.extractFeatures(signal_float, signalTime_float);

        sqLiteDatabaseCursor.moveToFirst();

        // Write this instance as one line in the existing ARFF file
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(arffFile, true));

            for (int u = 0; u < features.length - 1; u++) {
                out.write(features[u] + ", ");
            }
            out.write(windowGait);
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    
    private void broadcastStatusMessage(String message){
        broadcastStatusMessage(message, FeatureExtractionActivity.UPDATE_TYPE_ADDITION);
    }

    private void broadcastStatusMessage(String message, String updateType) {
//        Log.v("FeatureExtractionService", message);
        Intent intent = new Intent(STATUS_UPDATE);
        intent.putExtra(STATUS_MESSAGE, message);
        intent.putExtra(STATUS_UPDATE_TYPE, updateType);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        task.cancel(true);
        for (SQLiteDatabase database : accelDBList) {
            database.close();
        }
        accelDBList.clear();
        broadcastStatusMessage("Service stopped.");
        super.onDestroy();
    }

}
