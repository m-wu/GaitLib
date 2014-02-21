
package org.spin.gaitlib.gaitlibdemo.beat;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GaitSoundService extends Service {

    private static final double CADENCE_THRESHOLD_MAX = 3;
    private static final double CADENCE_THRESHOLD_MIN = 0.2;

    private final GaitAnalysisServiceReceiver receiver = new GaitAnalysisServiceReceiver();

    private SoundPool mSoundPool;
    private final Map<String, Integer> gaitSoundIds = new HashMap<String, Integer>();

    private String gait = null;
    private float cadence = 0;

    private boolean isStarted = false;
    private boolean isClassifyingGait = false;
    private SoundTask soundTask;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isClassifyingGait = intent.getBooleanExtra(MainActivity.GAIT_CLASSIFICATION, false);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        initSoundPool();
        registerReceiver(receiver, new IntentFilter(
                GaitAnalysisService.GAIT_UPDATE));
        soundTask = new SoundTask();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        soundTask.stopTimer();
        soundTask.cancel(true);
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void initSoundPool() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        int walkSoundID = mSoundPool.load(this, R.raw.footstep, 1);
        int narrowSoundID = mSoundPool.load(this, R.raw.drumroll, 1);
        int twofoothopSoundID = mSoundPool.load(this, R.raw.spring, 1);
        int penguinSoundID = mSoundPool.load(this, R.raw.chicken, 1);
        int liftkneesSoundID = mSoundPool.load(this, R.raw.knee, 1);
        int dingSoundID = mSoundPool.load(this, R.raw.ding, 1);

        gaitSoundIds.put("walk", walkSoundID);
        gaitSoundIds.put("narrow", narrowSoundID);
        gaitSoundIds.put("twofoothop", twofoothopSoundID);
        gaitSoundIds.put("penguin", penguinSoundID);
        gaitSoundIds.put("walksideways", penguinSoundID);
        gaitSoundIds.put("liftknees", liftkneesSoundID);
        gaitSoundIds.put("ding", dingSoundID);
    }
    
    private class SoundTask extends AsyncTask<URL, Integer, Long> {        
        private final Timer timer = new Timer();
        
        @Override
        protected Long doInBackground(URL... urls) {
            startTimer();
            return Long.valueOf(0);
        }
        
        private void startTimer() {
            if (cadence < CADENCE_THRESHOLD_MIN || cadence > CADENCE_THRESHOLD_MAX) {
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        startTimer();
                    }

                }, 200);
            } else {
                final long delay = (long) (1000 / cadence);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        playSound();
                        startTimer();
                    }

                }, delay);
            }
        }
        
        private void playSound() {
            Integer soundId = gaitSoundIds.get(gait);
            Log.v("SoundService", "gait = " + gait + "; sound id = " + soundId);
            if (soundId == null || !isClassifyingGait) {
                soundId = gaitSoundIds.get("ding");
            }
            mSoundPool.play(soundId, 1, 1, 1, 0, 1f);
        }
        
        private void stopTimer(){
            timer.cancel();
            timer.purge();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class GaitAnalysisServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (GaitAnalysisService.GAIT_UPDATE.equals(action)) {
                cadence = intent.getFloatExtra(GaitAnalysisService.CADENCE, 0);
                gait = intent.getStringExtra(GaitAnalysisService.GAIT);
                if (!isStarted) {
                    soundTask.execute(null, null, null);
                    isStarted = true;
                }
            }
        }

    }

}
