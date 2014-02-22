package org.spin.gaitlib.gaitlogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import org.spin.gaitlib.gaitlogger.R;


/**
 * Activity class for the GaitLogger application.
 * GaitLogger will record signal data (right now just accelerometer)
 * to a text file (TODO: mySQL or SQLite3 database)
 * 
 * @author oli
 */
public class GaitLoggerActivity extends Activity
{
	Button button_log;
	boolean logging = false;
	Intent gaitLoggerServiceIntent;
	TextView text_status;
	Spinner spnParticipantID;
	Spinner spnPhoneID;
	Spinner spnLocationOnPerson;
	RadioButton radRecordAccel;
	RadioButton radRecordGyro;
	RadioButton radRecordBoth;
	
	Button btnOffset;
	TextView txtOffset;
	private long offset = -1;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loggerlayout);
        button_log = (Button) findViewById(R.id.btnLog);
        button_log.setOnClickListener(new LogButtonClickListener());
        text_status = (TextView) findViewById(R.id.txtStatus);
        
    	spnParticipantID = (Spinner) findViewById(R.id.spnParticipantID);
    	spnPhoneID = (Spinner) findViewById(R.id.spnPhoneID);
    	spnLocationOnPerson = (Spinner) findViewById(R.id.spnLocationOnPerson);
    	
    	txtOffset = (TextView) findViewById(R.id.txtOffset);
    	txtOffset.setText(""+offset);
    	btnOffset = (Button) findViewById(R.id.btnSetOffset);
        btnOffset.setOnClickListener(new OffsetButtonClickListener());
        
        radRecordAccel = (RadioButton)findViewById(R.id.radAccel);
        radRecordGyro = (RadioButton)findViewById(R.id.radGyro);
        radRecordBoth = (RadioButton)findViewById(R.id.radBoth);
        RadioButtonClickListener radioButtonClickListener = new  RadioButtonClickListener();
        radRecordAccel.setOnClickListener(radioButtonClickListener);
        radRecordGyro.setOnClickListener(radioButtonClickListener);
        radRecordBoth.setOnClickListener(radioButtonClickListener);
        radRecordAccel.setChecked(true);
        
        //initialize service
        gaitLoggerServiceIntent = new Intent(this, GaitLoggerService.class);
        
        //check to see if the GaitLoggerService is already running
        if (GaitLoggerServiceStillRunning())
        {
        	setStateToLogging();
        }
        else
        {
        	setStateToNotLogging();
        }
    }
    
    private boolean GaitLoggerServiceStillRunning()
    {
    	ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        	//GaitLoggerService.class.toString() didn't work
            if ("spin.gaitlogger.GaitLoggerService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    
    private void setStateToLogging()
    {
    	logging = true;
    	button_log.setText(R.string.btnLogStopLogging);
		text_status.setText(R.string.txtStatus_Logging);
		text_status.setTextColor(getResources().getColor(R.color.txtStatus_LoggingColour));
		spnParticipantID.setEnabled(false);
		spnPhoneID.setEnabled(false);
		spnLocationOnPerson.setEnabled(false);
		btnOffset.setEnabled(false);
		radRecordAccel.setEnabled(false);
		radRecordGyro.setEnabled(false);
		radRecordBoth.setEnabled(false);
    }
    
    private void setStateToNotLogging()
    {
		logging = false;
		button_log.setText(R.string.btnLogText);
		text_status.setText(R.string.txtStatus_NotLogging);
		text_status.setTextColor(getResources().getColor(R.color.txtStatus_NotLoggingColour));
		spnParticipantID.setEnabled(true);
		spnPhoneID.setEnabled(true);
		spnLocationOnPerson.setEnabled(true);
		btnOffset.setEnabled(true);
		radRecordAccel.setEnabled(true);
		radRecordGyro.setEnabled(true);
		radRecordBoth.setEnabled(true);
    }
    

    private void startLogging()
	{
    	gaitLoggerServiceIntent.putExtra("participant", spnParticipantID.getSelectedItem().toString());
    	gaitLoggerServiceIntent.putExtra("phone", spnPhoneID.getSelectedItem().toString());
    	gaitLoggerServiceIntent.putExtra("location", spnLocationOnPerson.getSelectedItem().toString());
    	gaitLoggerServiceIntent.putExtra("offset", Long.toString(offset));
    	String toRecord = "";
    	if(radRecordAccel.isChecked())
    	{
    		toRecord = "accel";
    	} else if(radRecordGyro.isChecked())
    	{
    		toRecord = "gyro";
    	} else if(radRecordBoth.isChecked()) {
    		toRecord = "both";
    	}
    	gaitLoggerServiceIntent.putExtra("toRecord", toRecord);
		startService(gaitLoggerServiceIntent);
		setStateToLogging();
		
	}
	
	private void stopLogging()
	{
        stopService(gaitLoggerServiceIntent);
		setStateToNotLogging();
	}
    
	private void setOffset()
	{
		//offset = System.currentTimeMillis();
		offset = System.nanoTime();
    	txtOffset.setText(""+offset);
	}
	
	private void handleRadioButtons(int id)
	{
		if ( radRecordAccel.getId() == id)
		{
			radRecordGyro.setChecked(false);
			radRecordBoth.setChecked(false);
		} else if (radRecordGyro.getId() == id)
		{
			radRecordAccel.setChecked(false);
			radRecordBoth.setChecked(false);
		} else if (radRecordBoth.getId() == id)
		{
			radRecordAccel.setChecked(false);
			radRecordGyro.setChecked(false);
		}
	}

    
    protected class LogButtonClickListener implements OnClickListener
    {
		public void onClick(View v)
		{
			if(logging)
			{
				stopLogging();
			}
			else
			{
				startLogging();
			}
		}
    }
    
    protected class OffsetButtonClickListener implements OnClickListener
    {
		public void onClick(View v)
		{
			setOffset();
		}
    }
    
    protected class RadioButtonClickListener implements OnClickListener
    {
		public void onClick(View v)
		{
			handleRadioButtons(v.getId());
		}
    }
 
}