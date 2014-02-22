
package org.spin.gaitlib.gaitlogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class allows a SQLite database to be created with a table for the accel data. Call
 * getWritableDatabase() to write to the database and getReadableDatabase() to read from it.
 * (Database is not actually created after the constructor is called; one of these functions need to
 * be subsequently called before it is created.
 * 
 * @Author LL
 */
public class AccelOpenHelper extends SQLiteOpenHelper {
    public static final int TIMESTAMP_INDEX = 0;
    public static final int PHONE_ID_INDEX = 1;
    public static final int PARTICIPANT_ID_INDEX = 2;
    public static final int LOCATION_INDEX = 3;
    public static final int GAIT_INDEX = 4;
    public static final int X_INDEX = 5;
    public static final int Y_INDEX = 6;
    public static final int Z_INDEX = 7;
    public static final int ROW_NUM_INDEX = 8;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "accelDB";

    private final String tableName;

    public AccelOpenHelper(Context context, String tableName) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.tableName = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatmenet = "CREATE TABLE "
                + tableName
                + " (Timestamp text, PhoneID text, ParticipantID text, Location text, Gait text, X text, Y text, Z text, RowNum integer);";
        db.execSQL(createTableStatmenet);
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
}
