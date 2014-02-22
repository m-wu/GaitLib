package org.spin.gaitlib.gaitlogger;

public class NativeAccelerometerReader {

	
	static {
		System.loadLibrary("android");
		System.loadLibrary("log");
		System.loadLibrary("ReadAccelerometer");
	}
	
	// See jni/ReadAccelerometer.c for the implementation of these methods

	public static native void monitorSensors(String participant, String phone, String location, String offset, String accel_filename, String gyro_filename, int SAMP_PER_SEC);
	
	public static native void stopMonitoringSensors(); 
}
