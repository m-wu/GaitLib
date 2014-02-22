
package org.spin.gaitlib.gaitlogger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GaitLoggerStartupActivity extends Activity
{

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        FileManagerUtil.setGaitLoggerDirectory(getExternalFilesDir(null));
        
        createMenuButtons();
    }

    private void createMenuButtons() {
        Button btnLaunchLogger = (Button) findViewById(R.id.btnLaunchLogger);
        btnLaunchLogger.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                launchLogger();
            }
            
        });
        
        Button btnLaunchStudyRunner = (Button) findViewById(R.id.btnLaunchStudyRunner);
        btnLaunchStudyRunner.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                launchStudyRunner();
            }
            
        });
        
        Button btnLaunchFeatureExtraction = (Button) findViewById(R.id.btnLaunchFeatureExtraction);
        btnLaunchFeatureExtraction.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                launchFeatureExtraction();
            }
            
        });
    }

    public void launchLogger()
    {
        startActivity(new Intent(this, GaitLoggerActivity.class));
    }

    public void launchStudyRunner()
    {
        startActivity(new Intent(this, StudyRunnerActivity.class));
    }

    public void launchFeatureExtraction()
    {
        startActivity(new Intent(this, FeatureExtractionActivity.class));
    }
}
