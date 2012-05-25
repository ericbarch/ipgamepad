package com.ebarch.ipgamepad;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;


public class OldAccel extends Activity implements SensorEventListener {
 
 // Accelerometer X, Y, and Z values
 private TextView accelXValue;
 private TextView accelYValue;
 private TextView accelZValue;
 
 // Orientation X, Y, and Z values
 private TextView orientXValue;
 private TextView orientYValue;
 private TextView orientZValue;
 
 public int yOrient;
 public int zOrient;
 
 public int robotActive;
 
 private SensorManager sensorManager = null;
 

 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a reference to a SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        setContentView(R.layout.main);
       
        // Initialize accelerometer related view elements
        accelXValue.setText("0.00");
        accelYValue.setText("0.00");
        accelZValue.setText("0.00");
       
        // Initialize orientation related view elements
        orientXValue.setText("0.00");
        orientYValue.setText("0.00");
        orientZValue.setText("0.00");
        
        robotActive = 1;
        
        new Thread(new Runnable() {
        	public void run() {
            	while(true) {
            		if (robotActive == 1) {
            			try {
            				byte[] buf;
            				if (zOrient < 20 && zOrient > -20)
            					buf = new byte[] { (byte) (90 + yOrient), (byte) (90 + yOrient*(-1)) };
            				else
            					buf = new byte[] { (byte) (90 + zOrient), (byte) (90 + zOrient) };
            				DatagramSocket s = new DatagramSocket();
            				InetAddress local = InetAddress.getByName("192.168.1.33");
            				DatagramPacket p = new DatagramPacket(buf, buf.length,local,4444);
            				s.send(p); 
            			} catch (Exception e) {}
            			try {
            				Thread.sleep(30);
            			}
            			catch (InterruptedException e) {}
                	}
            	}
            }
        }).start();
    }
   
    // This method will update the UI on new sensor events
    public void onSensorChanged(SensorEvent sensorEvent) {
     synchronized (this) {
      if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
       accelXValue.setText(Float.toString(sensorEvent.values[0]));
       accelYValue.setText(Float.toString(sensorEvent.values[1]));
       accelZValue.setText(Float.toString(sensorEvent.values[2]));       
      }
      
      if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
       orientXValue.setText(Float.toString(sensorEvent.values[0]));
       orientYValue.setText(Float.toString(sensorEvent.values[1]));
       orientZValue.setText(Float.toString(sensorEvent.values[2]));  
       yOrient = ((int)sensorEvent.values[1]) * -1;
       zOrient = ((int)sensorEvent.values[2]) * -1;
      } 
     }
    }
   
    // I've chosen to not implement this method
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }
   
    @Override
    protected void onResume() {
     super.onResume();
     // Register this class as a listener for the accelerometer sensor
     sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
     // ...and the orientation sensor
     sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
     robotActive = 1;
    }
   
    @Override
    protected void onStop() {
     // Unregister the listener
     sensorManager.unregisterListener(this);
     robotActive = 0;
     super.onStop();
    } 
    
}