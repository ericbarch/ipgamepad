package com.ebarch.ipgamepad;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.MobileAnarchy.Android.Widgets.Joystick.DualJoystickView;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.content.SharedPreferences;


public class IPGamepad extends Activity {
    
    private SharedPreferences preferences;
    
    private NetworkingThread networkThread;
    private DatagramSocket udpSocket;
    private InetAddress ipAddress;
    private int packetRate;
    private int port;
    private boolean auxbyte;
    private boolean leftActive, rightActive = false;
    private int leftX, leftY, rightX, rightY = 0;
	
	private DualJoystickView joystick;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        joystick = (DualJoystickView)findViewById(R.id.dualjoystickView);
        joystick.setOnJostickMovedListener(_listenerLeft, _listenerRight);
        
        // Initialize preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Setup the networking
        try {
        	udpSocket = new DatagramSocket();
        	updateNetworking();
        }
        catch (Exception e) {
        	// Networking exception
        }
    }
    
    /* Call this whenever the network settings need to be reloaded */
    public void updateNetworking() {
    	try {
			ipAddress = InetAddress.getByName(preferences.getString("ipaddress", "192.168.1.22"));
			port = Integer.parseInt(preferences.getString("port", "4444"));
			packetRate = Integer.parseInt(preferences.getString("txinterval", "20"));
    	} catch (UnknownHostException e) {
    		// Networking exception
    	}
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		startActivity(new Intent(this, Preferences.class));
		return true;
	}
	
	private JoystickMovedListener _listenerLeft = new JoystickMovedListener() {

		@Override
		public void OnMoved(int pan, int tilt) {
			leftX = pan;
			leftY = tilt;
			leftActive = true;
		}

		@Override
		public void OnReleased() {
			leftActive = false;
		}
		
		public void OnReturnedToCenter() {
			leftActive = false;
		};
	};
	
    private JoystickMovedListener _listenerRight = new JoystickMovedListener() {

    	@Override
		public void OnMoved(int pan, int tilt) {
    		rightX = pan;
			rightY = tilt;
			rightActive = true;
		}

		@Override
		public void OnReleased() {
			rightActive = false;
		}
		
		public void OnReturnedToCenter() {
			rightActive = false;
		};
	};
    
    /* The main networking thread that sends the joystick UDP packets */
    class NetworkingThread extends Thread {
        private volatile boolean stop = false;

        public void run() {
                while (!stop) {
                	if (leftActive || rightActive) {
                		// Robot is enabled - let's send some data
    		    		try {
    		    			// A packet contains 5 bytes - leftJoystickY, leftJoystickX, rightJoystickY, rightJoystickX, Aux Byte
    		    			byte auxByte;
    		    			
    		    			// Aux byte can be used for things you'd like to enable/disable on a robot such as headlights or relays
    		    			if (auxbyte)
    		    				auxByte = (byte) 255;
    		    			else
    		    				auxByte = (byte) 0;
    		    			
    						byte[] buf = new byte[] { mapJoystick(leftY), mapJoystick(leftX), mapJoystick(rightY), mapJoystick(rightX), auxByte };
    						DatagramPacket p = new DatagramPacket(buf, buf.length, ipAddress, port);
    						udpSocket.send(p);
    					} catch (Exception e) {}
    					try {
    						Thread.sleep(packetRate);
    					}
    					catch (InterruptedException e) {}
    	    		}
    	    		else {
    	    			// Robot is disabled - wait a little bit before trying again
    	    			try {
    						Thread.sleep(packetRate);
    					}
    					catch (InterruptedException e) {}
    	    		}
                }
        }

        public synchronized void requestStop() {
                stop = true;
        }
    }
    
    private static byte mapJoystick(int input) {
    	int result = (int)mapValue((double)input, -150, 150, 0, 255);
    	
    	if (result < 0)
    		result = 0;
    	else if (result > 255)
    		result = 255;
    	
    	return (byte)result;
    }
    
    public static double mapValue(double input, double inMin, double inMax, double outMin, double outMax) {
    	return (input - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }
    
    /* Call this to start the main networking thread */
    public synchronized void startNetworkingThread(){
        if(networkThread == null){       
                networkThread = new NetworkingThread();
                networkThread.start();
        }
    }
    
    /* Call this to stop the main networking thread */
    public synchronized void stopNetworkingThread(){
        if(networkThread != null){
                networkThread.requestStop();
                networkThread = null;
        }
    }
    
    @Override
    protected void onPause() {
    	// End Ethernet communications
    	stopNetworkingThread();
    	
    	super.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Update networking settings
    	updateNetworking();
    	
    	// Update headlight status
    	auxbyte = preferences.getBoolean("auxbyte", false);
    	
    	// Begin Ethernet communications
    	startNetworkingThread();
    }
}