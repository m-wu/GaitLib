package org.spin.gaitlib.gaitlogger;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class StudyRunnerActivity extends Activity
{
	Spinner spnParticipantID;
	Button btnStartStudy;
	
	Spinner spnPhoneID;
	Button btnSetPhoneOffset;
	
	Spinner spnGait;
	Button btnShuffleGaits;
	Button btnStartTrial;
	Button btnStopTrial;
	
	File outFile;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.studylayout);
    	
    	spnParticipantID = (Spinner) findViewById(R.id.spnParticipantID);
    	btnStartStudy = (Button) findViewById(R.id.btnStartStudy);
    	btnStartStudy.setOnClickListener(new StartStudyButtonClickListener());
    	
    	spnPhoneID = (Spinner) findViewById(R.id.spnPhoneID);
    	btnSetPhoneOffset = (Button) findViewById(R.id.btnSetPhone);
    	btnSetPhoneOffset.setOnClickListener(new SetPhoneOffsetButtonClickListener());
    	
    	spnGait = (Spinner) findViewById(R.id.spnGait);
    	btnShuffleGaits = (Button) findViewById(R.id.btnShuffleGait);
    	btnStartTrial = (Button) findViewById(R.id.btnStartTrial);
    	btnStopTrial = (Button) findViewById(R.id.btnStopTrial);
    	btnShuffleGaits.setOnClickListener(new ShuffleGaitButtonClickListener());
    	btnStartTrial.setOnClickListener(new StartTrialButtonClickListener());
    	btnStopTrial.setOnClickListener(new StopTrialButtonClickListener());
    	
    	spnParticipantID.setEnabled(true);
    	btnStartStudy.setEnabled(true);
    	
    	spnPhoneID.setEnabled(false);
    	btnSetPhoneOffset.setEnabled(false);
    	
    	spnGait.setEnabled(false);
    	btnShuffleGaits.setEnabled(false);
    	btnStartTrial.setEnabled(false);
    	btnStopTrial.setEnabled(false);
    	
    	
    }
    
    protected void startStudy()
    {
    	if (outFile != null)
    	{
    		outFile = null;
    	}
    	
		try
		{
	    	//set up output file (comma seperated value text file)
			//date formats for directory and file name
			//directory will have the current day as the name
			//file will have the day, hour, minute, and second as part of the file name 
			DateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMMdd");
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMMdd-hh-mm-ss");
			Date date = new Date();
	
			String participantName = spnParticipantID.getSelectedItem().toString();
			
			//set up directory and file name
			String dirString = "gaitlogger_"+simpleDateFormat.format(date);
			String fileString = getString(R.string.study_logfile_name)+dateFormat.format(date)+participantName+ ".csv";
			
			//create directory
			File dir = new File(FileManagerUtil.getDataFoldersParentDirectory(), dirString);
			if (!dir.exists() && !dir.mkdirs())
			{
				//couldn't create directory
				Toast.makeText(this, "Error creating directory: " + dir, Toast.LENGTH_LONG).show();
			}
	
			//create file and write header
			outFile = new File(dir, fileString);
			FileManagerUtil.updateIndex(outFile.getAbsolutePath(), this);
			PrintWriter out = new PrintWriter(new FileWriter(outFile, false));
			out.println(participantName);
			out.close();
			
			spnParticipantID.setEnabled(true);
	    	btnStartStudy.setEnabled(true);
	    	
	    	spnPhoneID.setEnabled(true);
	    	btnSetPhoneOffset.setEnabled(true);
	    	
	    	spnGait.setEnabled(true);
	    	btnShuffleGaits.setEnabled(true);
	    	btnStartTrial.setEnabled(true);
	    	btnStopTrial.setEnabled(true);
		} 
		catch (IOException e)
		{
			Toast.makeText(this, "Error:"+e.toString(), Toast.LENGTH_LONG).show();
			outFile = null;
	    	spnParticipantID.setEnabled(true);
	    	btnStartStudy.setEnabled(true);
	    	
	    	spnPhoneID.setEnabled(false);
	    	btnSetPhoneOffset.setEnabled(false);
	    	
	    	spnGait.setEnabled(false);
	    	btnStartTrial.setEnabled(false);
	    	btnStopTrial.setEnabled(false);
		}
    }

    protected void setPhoneOffset()
    {
    	if (outFile != null)
    	{
			try 
			{
	    		long offset = System.nanoTime();
	    		String phoneID = spnPhoneID.getSelectedItem().toString();
				PrintWriter out = new PrintWriter(new FileWriter(outFile, true));
	    		out.println("Set "+phoneID + ","+offset);
	    		out.flush();
	    		out.close();
	    		Toast.makeText(this, "Offset set: "+offset, Toast.LENGTH_SHORT).show();
			} 
			catch (IOException e)
			{
	    		//TODO: make this a resource
	    		Toast.makeText(this, "Error: couldn't write to file.", Toast.LENGTH_LONG).show();
			}
    	}
    	else
    	{
    		//TODO: make this a resource
    		Toast.makeText(this, "Error: couldn't write to file.", Toast.LENGTH_LONG).show();
    	}
    	
    }
    
    protected void shuffleGaits()
    {
    	//Get access the Gaits string array
    	Resources res = getResources();
    	String gaits[] = res.getStringArray(R.array.Gaits).clone();
    	Random r = new Random();
    	
    	//shuffle gaits randomly
    	for (int i = 0; i < gaits.length; i++)
    	{
    		int newIndex = r.nextInt(gaits.length);
    		String tmp = gaits[i];
    		gaits[i] = gaits[newIndex];
    		gaits[newIndex] = tmp;
    	}
    	
    	ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, gaits);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spnGait.setAdapter(adapter);
    }
    
    protected void startTrial()
    {
    	if (outFile != null)
    	{
			try 
			{
	    		long offset = System.nanoTime();
	    		String gait = spnGait.getSelectedItem().toString();
				PrintWriter out = new PrintWriter(new FileWriter(outFile, true));
	    		out.println("Start "+gait+","+offset);
	    		out.flush();
	    		out.close();
	    		Toast.makeText(this, "Started "+gait, Toast.LENGTH_SHORT).show();
			} 
			catch (IOException e)
			{
	    		//TODO: make this a resource
	    		Toast.makeText(this, "Error: couldn't write to file.", Toast.LENGTH_LONG).show();
			}
    	}
    	else
    	{
    		//TODO: make this a resource
    		Toast.makeText(this, "Error: couldn't write to file.", Toast.LENGTH_LONG).show();
    	}
    }
    
    protected void stopTrial()
    {
    	if (outFile != null)
    	{
			try 
			{
	    		long offset = System.nanoTime();
	    		String gait = spnGait.getSelectedItem().toString();
				PrintWriter out = new PrintWriter(new FileWriter(outFile, true));
	    		out.println("Stop "+gait+","+offset);
	    		out.flush();
	    		out.close();
	    		Toast.makeText(this, "Stopped "+gait, Toast.LENGTH_SHORT).show();
			} 
			catch (IOException e)
			{
	    		//TODO: make this a resource
	    		Toast.makeText(this, "Error: couldn't write to file.", Toast.LENGTH_LONG).show();
			}
    	}
    	else
    	{
    		//TODO: make this a resource
    		Toast.makeText(this, "Error: couldn't write to file.", Toast.LENGTH_LONG).show();
    	}
    }
    
    
    protected class StartStudyButtonClickListener implements OnClickListener
    {
		@Override
        public void onClick(View v)
		{
			startStudy();
		}
    }
    
    protected class SetPhoneOffsetButtonClickListener implements OnClickListener
    {
		@Override
        public void onClick(View v)
		{
			setPhoneOffset();
		}
    }
    
    protected class ShuffleGaitButtonClickListener implements OnClickListener
    {
    	@Override
        public void onClick(View v)
    	{
    		shuffleGaits();
    	}
    }
    
    protected class StartTrialButtonClickListener implements OnClickListener
    {
		@Override
        public void onClick(View v)
		{
			startTrial();
		}
    }
    
    protected class StopTrialButtonClickListener implements OnClickListener
    {
		@Override
        public void onClick(View v)
		{
			stopTrial();
		}
    }
    
    

}
