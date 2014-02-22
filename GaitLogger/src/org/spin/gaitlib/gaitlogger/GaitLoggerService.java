package org.spin.gaitlib.gaitlogger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GaitLoggerService
 * 
 * Logs signal data (currently only accelerometer)
 * to a text file (eventually mySQL or SQLite3 database)
 * 
 * Services execute in the application process and typically
 * in the main thread, but will run in the background.
 * 
 * @author oli
 *
 */
public class GaitLoggerService extends Service implements SensorEventListener
{
	private static final String TAG = "GaitLoggerService";
	private static final int APP_UID = 1; //an ID unique to this application
	private PrintWriter accelOut;
	private PrintWriter gyroOut;
	WakeLock wakeLock;
	
	final int SENSOR_SAMPLING_SPEED = SensorManager.SENSOR_DELAY_FASTEST;
	//final int SENSOR_SAMPLING_SPEED = 1000; //in ms //SensorManager.SENSOR_DELAY_NORMAL;

	//final int WAKE_LOCK = PowerManager.PARTIAL_WAKE_LOCK;
	final int WAKE_LOCK = PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE;	
	
	final boolean PLAY_ERROR_SOUND = false;
	final boolean CAPTURE_GYROSCOPE = false;
	final boolean REREGISTER_ON_SCREEN_OFF = false;
	final boolean WRITE_TO_FILES = true;
	final boolean REACQUIRE_WAKELOCK_ON_SCREEN_OFF = true;
	final boolean HANDLE_SCREEN_OFF_WITH_DELAY = false;
	final boolean USE_EVENT_TIME = true;	

	private String dir_path = null;
    private String accel_path_to_use = null;
    private String gyro_path_to_use = null;
	
	//for playing an error
	SoundPool mSoundPool;
	int errorSoundID;
	long lastTimeStamp = -1;
	final long ERROR_CUTOFF = 500000000; //the minimum number of nanoseconds required to decide that an error has occurred
	
	final long SCREEN_OFF_RECEIVER_DELAY = 500; //delay for reregistering listeners in milliseconds

	//Central Logic
	
	/**
	 * setup()
	 * 
	 * called when we want to start logging
	 */
	private void setup(Intent intent) {
		//announce to user that we're starting to log
		//TODO: make this a resource
		Toast.makeText(this, "log service started", Toast.LENGTH_SHORT).show();
	
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        errorSoundID = mSoundPool.load(this, R.raw.error, 1);
			
			
		//this puts the thread to run in the foreground
		//so it should continue to log even while sleeping
		//startForeground(APP_UID, new Notification());
		PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();
		//PowerManager.ACQUIRE_CAUSES_WAKEUP
			
		//set up output file (comma seperated value text file)
		//date formats for directory and file name
		//directory will have the current day as the name
		//file will have the day, hour, minute, and second as part of the file name 
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMMdd");
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMMdd-hh-mm-ss");
		Date date = new Date();

		//get parameters
		//TODO: Put these keys into strings.xml
		String offset = intent.getExtras().getString("offset");
		String participantID = intent.getExtras().getString("participant");
		String phoneID = intent.getExtras().getString("phone");
		String location = intent.getExtras().getString("location");
		String toRecord = intent.getExtras().getString("toRecord");
		
		//set up directory and file name
		String dirString = FileManagerUtil.DATA_FOLDER_PREFIX+simpleDateFormat.format(date);
		String accelFileString = getString(R.string.accel_logfile_name)+dateFormat.format(date) + participantID + ".csv";
		String gyroFileString = getString(R.string.gyro_logfile_name)+dateFormat.format(date) + participantID + ".csv";
		
		//create directory
		//Toast.makeText(this, Environment. getExternalStorageState(), Toast.LENGTH_LONG).show();
		File dir = new File(FileManagerUtil.getDataFoldersParentDirectory() + "/" + dirString);
		
		if (!dir.exists() && !dir.mkdirs())
		{
			//couldn't create directory
			Toast.makeText(this, "Error creating directory: " + dir, Toast.LENGTH_LONG).show();
		}
		
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
        
        dir_path = dir.getAbsolutePath() +"/";
        if (toRecord.equals("accel")) {
        	accel_path_to_use=dir_path + accelFileString;
        } else if (toRecord.equals("gyro")) {
        	gyro_path_to_use=dir_path + gyroFileString;
        } else {
        	accel_path_to_use=dir_path + accelFileString;
        	gyro_path_to_use=dir_path + gyroFileString;
        }
        
        
        NativeAccelerometerReader.monitorSensors(
    			participantID,
    			phoneID,
    			location,
    			offset,
    			accel_path_to_use,
    			gyro_path_to_use,
    			100);
            
        //startForeground(0, new Notification());
	}
	
	/**
	 * teardown()
	 * 
	 * called when we want to stop logging
	 */
	private void teardown() {
		NativeAccelerometerReader.stopMonitoringSensors();  
		
		if (dir_path != null){
		    FileManagerUtil.updateIndex(dir_path, this);
		}
        if (accel_path_to_use != null){
            FileManagerUtil.updateIndex(accel_path_to_use, this);
        }
        if (gyro_path_to_use != null){
            FileManagerUtil.updateIndex(gyro_path_to_use, this);            
        }
		
		//unregister with the sensor manager
		//SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//sensorManager.unregisterListener(this);
		
		//close output streams
		//if (accelOut != null)
		//	accelOut.close();
		//if (gyroOut != null)
		//	gyroOut.close();
		
		//no longer need to run in background
		wakeLock.release();

		unregisterReceiver(mReceiver);
		
		//announce to the user that we're done
		//TODO: make this a resource
	    Toast.makeText(this, "logging service done", Toast.LENGTH_SHORT).show(); 
	}
	
	/**
	 * recordAccelerometerValues
	 * 
	 * saves the accelerometer reading to a file
	 * using the established PrintWriter
	 * and current system time in milliseconds or nanoseconds
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	private void recordAccelerometerValues(long t, double x, double y, double z) {
		//DEBUG
		Log.v(TAG, "record "+t+","+x+","+y+","+z);
		if (accelOut != null)
		{
			if(!USE_EVENT_TIME)
			{
				t = System.currentTimeMillis();
			}
			accelOut.printf("%d,%f,%f,%f\n", t,x,y,z);
			//accelOut.flush();
			
			if(PLAY_ERROR_SOUND && lastTimeStamp != -1 && (t - lastTimeStamp) > ERROR_CUTOFF) {
				mSoundPool.play(errorSoundID, 1f, 1f, 1, 0, 1f);
			}
			lastTimeStamp = t;
		}
	}
	
	/**
	 * recordGyroscopeValues
	 * 
	 * saves the gyroscope reading to a file
	 * using the established PrintWriter
	 * and current system time in milliseconds or nanoseconds
	 * 
	 * @param ax
	 * @param ay
	 * @param az
	 */
	private void recordGyroscopeValues(long t, double ax, double ay, double az)
	{
		//DEBUG
		//Log.v(TAG, "record "+x+","+y+","+z);
		if (gyroOut != null)
		{
			if (!USE_EVENT_TIME)
			{
				t = System.currentTimeMillis();
			}
			gyroOut.printf("%d,%f,%f,%f\n", t,ax,ay,az);
			//gyroOut.flush();
		}
	}
	

// Service Methods
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		setup(intent);
	     
		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
    public void onDestroy()
	{
		teardown();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// this is not a bound service; return null
		return null;
	}

	/* onLowMemory(): Kept for possible issues in future.
	//if we are memory intensive, this might help to
	//keep the service alive.
	@Override
	public void onLowMemory ()
	{
		//STUB
	}
	*/

	
	// SensorEventListener Methods
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		switch (event.sensor.getType())
		{
			case Sensor.TYPE_ACCELEROMETER:
				recordAccelerometerValues(event.timestamp, event.values[0], event.values[1], event.values[2]);
			case Sensor.TYPE_GYROSCOPE:
				recordGyroscopeValues(event.timestamp, event.values[0], event.values[1], event.values[2]);
			break;
		}		
	}
	
	
	public void reregisterListeners()
	{
		SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		//unregister from the accelerometer and the gyroscope
		sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		if(CAPTURE_GYROSCOPE)
		{
			sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
		}
		
		//register the accelerometer and gyroscope sensors
		sensorManager.registerListener(this,
			sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
			SENSOR_SAMPLING_SPEED); // Tells the SensorManager to send accel data to GaitLoggerService
		
		if (CAPTURE_GYROSCOPE)
		{
		//register with gyroscope
		//NOTE: originally tried registering with both simultaneously, but then both signals were received at the same time
		//		with only 3 values :S
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
					SENSOR_SAMPLING_SPEED); // Tells the SensorManager to send gyro data to GaitLoggerService
		}
	}

	
	//from http://nosemaj.org/android-persistent-sensors
	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        Log.v(TAG, "onReceive("+intent+")");
	 
	        if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	            return;
	        }
	        
	        if(HANDLE_SCREEN_OFF_WITH_DELAY)
	        {
	 
		        Runnable runnable = new Runnable() {
		            @Override
                    public void run() {
		                
		                if(REREGISTER_ON_SCREEN_OFF)
		                {
			                Log.i(TAG, "Re-registering IMU listeners");
		                	reregisterListeners();
		                }
		                
		                if(REACQUIRE_WAKELOCK_ON_SCREEN_OFF)
		                {
		                	wakeLock.release();
		                	wakeLock.acquire();
		                }
		                
		            }
		        };
		        new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);

	        } else
	        {
	        	if(REREGISTER_ON_SCREEN_OFF)
                {
	                Log.i(TAG, "Re-registering IMU listeners");
                	reregisterListeners();
                }
                
                if(REACQUIRE_WAKELOCK_ON_SCREEN_OFF)
                {
                	wakeLock.release();
                	wakeLock.acquire();
                }
	        }
	 
	    }
	};


}
