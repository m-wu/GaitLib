
package org.spin.gaitlib.gaitlibdemo.beat;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import org.spin.gaitlib.GaitAnalysis;
import org.spin.gaitlib.core.GaitData;
import org.spin.gaitlib.core.IGaitUpdateListener;
import org.spin.gaitlib.filter.FilterNotSetException;
import org.spin.gaitlib.gait.IClassifierModelLoadingListener;

public class GaitAnalysisService extends Service {

    public static final String GAIT_UPDATE = "spin.gaitlib.GaitAnalysisService.GAIT_UPDATE";
    public static final String CADENCE = "spin.gaitlib.GaitAnalysisService.CADENCE";
    public static final String GAIT = "spin.gaitlib.GaitAnalysisService.GAIT";
    public static final String GAIT_ALL = "spin.gaitlib.GaitAnalysisService.GAIT_ALL";
    public static final String GAITLIB_STATUS_UPDATE = "spin.gaitlib.GaitAnalysisService.GAITLIB_STATUS_UPDATE";
    public static final String GAITLIB_STATUS_MESSAGE = "spin.gaitlib.GaitAnalysisService.GAITLIB_STATUS_MESSAGE";

    private WakeLock wakeLock;

    private GaitAnalysis mGaitAnalysis = null;

    private Logger logger = null;

    @Override
    public void onCreate() {
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int WAKE_LOCK = PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE;
        wakeLock = mgr.newWakeLock(WAKE_LOCK, "MyWakeLock");
        wakeLock.acquire();
        mGaitAnalysis = new GaitAnalysis();
        logger = new Logger();
        registerSensorListeners();
        mGaitAnalysis.registerGaitUpdateListener(new IGaitUpdateListener() {

            public void onGaitUpdated(GaitData data) {
                try {
                    float cadence = mGaitAnalysis.getCadence(false);
                    String gait = mGaitAnalysis.getCurrentGait();
                    logger.addCadence(cadence);
                    logger.addGait(gait);
                    logger.addTimeStamp(data.getTimeStamp());

                    Intent intent = new Intent(GAIT_UPDATE);
                    intent.putExtra(CADENCE, cadence);
                    intent.putExtra(GAIT, gait);
                    intent.putStringArrayListExtra(GAIT_ALL, logger.getGaits());

                    sendBroadcast(intent);
                } catch (FilterNotSetException e) {
                }

            }
        });

        mGaitAnalysis
                .addGaitClassifierModelLoadingListener(new IClassifierModelLoadingListener() {

                    public void onModelLoaded(boolean success) {
                        String message = success ? "Model loaded successfully."
                                : "GaitLib failed to load the model.";
                        Intent intent = new Intent(GAITLIB_STATUS_UPDATE);
                        intent.putExtra(GAITLIB_STATUS_MESSAGE, message);
                        sendBroadcast(intent);
                    }

                    public void onLoadingStart() {
                        String message = "GaitLib is loading the model.";
                        Intent intent = new Intent(GAITLIB_STATUS_UPDATE);
                        intent.putExtra(GAITLIB_STATUS_MESSAGE, message);
                        sendBroadcast(intent);
                    }
                });

        mGaitAnalysis.startGaitAnalysis(2000, 1000);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        wakeLock.release();
        mGaitAnalysis.stopGaitAnalysis();
        unregisterSensorListeners();
        clearCache();
        super.onDestroy();
    }

    private void registerSensorListeners() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(mGaitAnalysis.getSignalListener(),
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterSensorListeners() {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(mGaitAnalysis.getSignalListener());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void clearCache() {
        logger.clearAll();
    }

}
