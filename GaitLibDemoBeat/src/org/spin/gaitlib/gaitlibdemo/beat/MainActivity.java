
package org.spin.gaitlib.gaitlibdemo.beat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    public static final String GAIT_CLASSIFICATION = "org.spin.gaitlib.gaitlibdemo.gaitClassification";
    private final GaitAnalysisServiceReceiver receiver = new GaitAnalysisServiceReceiver();
    private Intent gaitAnalysisReceiver;
    private Intent gaitSoundService;

    private TextView text_cadence, text_gait, text_status;
    private Switch soundSwitch;
    private Switch gaitClassificationSwitch;

    public float cadence;
    public String gait;
    private final ArrayList<String> gaits = new ArrayList<String>();
    private ArrayAdapter<String> gaitListAdapter;
    private final boolean startGaitSoundOnStart = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_cadence = (TextView) findViewById(R.id.cadenceTextView);
        text_gait = (TextView) findViewById(R.id.gaitTextView);
        text_status = (TextView) findViewById(R.id.statusTextView);
        soundSwitch = (Switch) findViewById(R.id.soundSwitch);
        gaitClassificationSwitch = (Switch) findViewById(R.id.gaitClassificationSwitch);

        ListView gaitListView = (ListView) findViewById(R.id.gaitListView);
        gaitListAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, gaits);
        gaitListView.setAdapter(gaitListAdapter);

        gaitAnalysisReceiver = new Intent(this, GaitAnalysisService.class);
        startService(gaitAnalysisReceiver);

        gaitSoundService = new Intent(this, GaitSoundService.class);
        gaitSoundService.putExtra(GAIT_CLASSIFICATION, false);
        soundSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if (isChecked) {
                    startSoundService();
                } else {
                    stopService(gaitSoundService);
                }
            }

        });
        if (startGaitSoundOnStart) {
            soundSwitch.setChecked(true);
            startSoundService();
        }

        gaitClassificationSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (soundSwitch.isChecked()) {
                    startSoundService();
                }
            }
        });
    }

    private void startSoundService() {
        stopService(gaitSoundService);
        gaitSoundService.removeExtra(GAIT_CLASSIFICATION);
        gaitSoundService.putExtra(GAIT_CLASSIFICATION, gaitClassificationSwitch.isChecked());
        startService(gaitSoundService);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        IntentFilter gaitUpdateFilter = new IntentFilter(
                GaitAnalysisService.GAIT_UPDATE);
        registerReceiver(receiver, gaitUpdateFilter);
        IntentFilter gaitLibStatusUpdateFilter = new IntentFilter(
                GaitAnalysisService.GAITLIB_STATUS_UPDATE);
        registerReceiver(receiver, gaitLibStatusUpdateFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService(gaitAnalysisReceiver);
        stopService(gaitSoundService);
        super.onDestroy();
    }

    public class GaitAnalysisServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (GaitAnalysisService.GAIT_UPDATE.equals(action)) {
                cadence = intent.getFloatExtra(GaitAnalysisService.CADENCE, 0);
                gait = intent.getStringExtra(GaitAnalysisService.GAIT);
                
                text_cadence.setText(String.format("%.1f", cadence));
                text_gait.setText(gait);

                if (gait != null && cadence > 0) {
                    gaits.add(0, gait);
                    gaitListAdapter.notifyDataSetChanged();
                }
            } else if (GaitAnalysisService.GAITLIB_STATUS_UPDATE.equals(action)) {
                String message = intent
                        .getStringExtra(GaitAnalysisService.GAITLIB_STATUS_MESSAGE);
                text_status.setText(message);
            }

        }

    }

}
