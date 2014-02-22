#include <jni.h>
#include <errno.h>
//#include <jstring.h>
#include <stdio.h>
#include <stdlib.h>

#include <android/sensor.h>
#include <android/log.h>
#include <android/looper.h>


#define TAG "GaitLib_ReadSensors"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

//RESOURCES:
//http://www.sensorplatforms.com/native-sampling
//http://developer.android.com/reference/android/app/NativeActivity.html

#define LOOPER_ID 1
//#define SAMP_PER_SEC 100 //i've changed to 120, even 10, but nothing happen
//#define SAMP_PER_SEC 20 //i've changed to 120, even 10, but nothing happen


#define CONTINUE_CALLBACK 1
#define STOP_CALLBACK 0

int callback_setting = CONTINUE_CALLBACK;
ASensorEventQueue* queue;
FILE* accelOutFile;
FILE* gyroOutFile;
const int CACHE_SIZE = 200;
ASensorEvent *accelEventCache, *gyroEventCache;
int accelEventCache_i = 0, gyroEventCache_i = 0;



//this function is from
//http://stackoverflow.com/questions/8989686/access-faster-polling-accelerometer-via-nativeactivity-ndk

int get_sensor_events(int fd, int events, void* data)
{
	ASensorEvent event;
	int i;
	//LOGI("get_sensor_events");
	 while (ASensorEventQueue_getEvents(queue, &event, 1) > 0)
	 {
		 //LOGI("INSIDE LOOP");
		 if(event.type==ASENSOR_TYPE_ACCELEROMETER) {
			 /*LOGI("accl(x,y,z,t): %f %f %f %lld",
			 event.acceleration.x,
			 event.acceleration.y,
			 event.acceleration.z,
			 event.timestamp);
			  */

			 accelEventCache[accelEventCache_i].timestamp = event.timestamp;
			 accelEventCache[accelEventCache_i].acceleration.x = event.acceleration.x;
			 accelEventCache[accelEventCache_i].acceleration.y = event.acceleration.y;
			 accelEventCache[accelEventCache_i].acceleration.z = event.acceleration.z;

			 accelEventCache_i++;

			 if (accelEventCache_i >= CACHE_SIZE)
			 {
				 for (i = 0; i < CACHE_SIZE; i++)
				 {
					 fprintf(accelOutFile,"%lld,%f,%f,%f\n",
							 accelEventCache[i].timestamp,
							 accelEventCache[i].acceleration.x,
							 accelEventCache[i].acceleration.y,
							 accelEventCache[i].acceleration.z);
				 }
				 accelEventCache_i = 0;
			 }
		 }

		 if(event.type==ASENSOR_TYPE_GYROSCOPE) {
				 gyroEventCache[gyroEventCache_i].timestamp = event.timestamp;
				 gyroEventCache[gyroEventCache_i].acceleration.x = event.acceleration.x;
				 gyroEventCache[gyroEventCache_i].acceleration.y = event.acceleration.y;
				 gyroEventCache[gyroEventCache_i].acceleration.z = event.acceleration.z;

				 gyroEventCache_i++;

				 if (gyroEventCache_i >= CACHE_SIZE)
				 {
					 for (i = 0; i < CACHE_SIZE; i++)
					 {
						 fprintf(gyroOutFile,"%lld,%f,%f,%f\n",
								 gyroEventCache[i].timestamp,
								 gyroEventCache[i].acceleration.x,
								 gyroEventCache[i].acceleration.y,
								 gyroEventCache[i].acceleration.z);
					 }
					 gyroEventCache_i = 0;
				 }
		 	 }
	 }

	 //should return 1 to continue receiving callbacks, or 0 to unregister
	 if(CONTINUE_CALLBACK == callback_setting)
	 {
		 return 1;
	 }
	 else{
	 	 return 0;
	 }
}


//void Java_ReadAccelerometer_Monitor(JNIEnv* env, jclass clazz) {
JNIEXPORT void JNICALL Java_org_spin_gaitlib_gaitlogger_NativeAccelerometerReader_monitorSensors(JNIEnv* env, jobject this, jstring participant, jstring phone, jstring location, jstring offset, jstring accel_filename, jstring gyro_filename, jint SAMP_PER_SEC) {
	LOGD("LOADED");

	char *maccel_filename;
	char *mgyro_filename;
	jboolean iscopy;

	char *mparticipant = (*env)->GetStringUTFChars(env, participant, &iscopy);
	char *mphone = (*env)->GetStringUTFChars(env, phone, &iscopy);
	char *mlocation = (*env)->GetStringUTFChars(env, location, &iscopy);
	char *moffset = (*env)->GetStringUTFChars(env, offset, &iscopy);

	accelEventCache = (ASensorEvent*)malloc(sizeof(ASensorEvent)*CACHE_SIZE);
	gyroEventCache = (ASensorEvent*)malloc(sizeof(ASensorEvent)*CACHE_SIZE);

	LOGD("ABOUT TO OPEN FILES");
	accelOutFile = NULL;
	gyroOutFile = NULL;

	accelEventCache_i = 0;


	if (accel_filename != NULL)
	{
		LOGD("\tABOUT TO CONVERT STRING TO char*...");
		maccel_filename = (*env)->GetStringUTFChars(env, accel_filename, &iscopy);
		LOGD("\tdone. ABOUT TO OPEN ACCEL FILE...");
		accelOutFile = fopen(maccel_filename, "w");
		LOGD("\tdone. ABOUT TO WRITE HEADER TO ACCEL FILE...");
		fprintf(accelOutFile, "participant,%s\nphone,%s\nlocation,%s\noffset,%s\nt,x,y,z\n",mparticipant, mphone, mlocation, moffset);
		LOGD("\tdone.");
	}
	if(gyro_filename != NULL)
	{
		LOGD("\tABOUT TO OPEN GYRO FILE...");
		mgyro_filename = (*env)->GetStringUTFChars(env, gyro_filename, &iscopy);
		gyroOutFile = fopen(mgyro_filename, "w");
		LOGD("\tdone. ABOUT TO WRITE HEADER TO GYRO FILE...");
		fprintf(gyroOutFile, "participant,%s\nphone,%s\nlocation,%s\noffset,%s\nt,ax,ay,az\n",mparticipant, mphone, mlocation, moffset);
		LOGD("\tdone.");
	}


	LOGD("WROTE HEADERS, SETTING UP CALLBACKS");


	callback_setting = CONTINUE_CALLBACK;
    ASensorManager* sensorManager = ASensorManager_getInstance();
    LOGD("monitorSensors run");
    ALooper* looper = ALooper_forThread();
    if(looper == NULL)
        looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);

    ASensorRef accelerometerSensor = ASensorManager_getDefaultSensor(sensorManager,ASENSOR_TYPE_ACCELEROMETER);
    LOGI("accelerometerSensor: %s, vendor: %s", ASensor_getName(accelerometerSensor), ASensor_getVendor(accelerometerSensor));

    ASensorRef gyroscopeSensor = ASensorManager_getDefaultSensor(sensorManager,ASENSOR_TYPE_GYROSCOPE);
	LOGI("gyroscopeSensor: %s, vendor: %s", ASensor_getName(gyroscopeSensor), ASensor_getVendor(gyroscopeSensor));


    queue = ASensorManager_createEventQueue(sensorManager, looper, LOOPER_ID, get_sensor_events, NULL);

    if(accelOutFile != NULL)
    {
		ASensorEventQueue_enableSensor(queue, accelerometerSensor);
		ASensorEventQueue_setEventRate(queue, accelerometerSensor, (1000L/SAMP_PER_SEC)*1000);
    }
    if(gyroOutFile != NULL)
	{
		ASensorEventQueue_enableSensor(queue, gyroscopeSensor);
		ASensorEventQueue_setEventRate(queue, gyroscopeSensor, (1000L/SAMP_PER_SEC)*1000);
	}


/*
    //int ident;//identifier
    //int events;
    //while (1) {
        //while ((ident=ALooper_pollAll(-1, NULL, &events, NULL) >= 0)) {
            // If a sensor has data, process it now.
            if (ident == LOOPER_ID) {
                ASensorEvent event;
                while (ASensorEventQueue_getEvents(queue, &event, 1) > 0) {
                    LOGI("aaaaaaa accelerometer X = %f y = %f z=%f ", event.acceleration.x, event.acceleration.y, event.acceleration.z);
                }
            }
    //    }
    //}
     * */

}


JNIEXPORT void JNICALL Java_org_spin_gaitlib_gaitlogger_NativeAccelerometerReader_stopMonitoringSensors(JNIEnv* env, jobject this)
{
	callback_setting = STOP_CALLBACK;
	if(accelOutFile != NULL)
	{
		fclose(accelOutFile);
	}
	if(gyroOutFile != NULL)
	{
		fclose(gyroOutFile);
	}

	free(accelEventCache);
	free(gyroEventCache);
}
