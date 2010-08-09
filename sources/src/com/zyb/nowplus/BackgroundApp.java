/*******************************************************************************
 * CDDL HEADER START
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * src/com/vodafone360/people/VODAFONE.LICENSE.txt or
 * http://github.com/360/360-Engine-for-Android
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 *  include the License file at src/com/vodafone360/people/VODAFONE.LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 * 
 * CDDL HEADER END
 * 
 * Copyright 2010 Vodafone Sales & Services Ltd.  All rights reserved.
 * Use is subject to license terms.
 ******************************************************************************/
//#condition polish.blackberry && enable.blackberry.push == true 
package com.zyb.nowplus;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;



import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.device.api.io.http.HttpServerConnection;
import net.rim.device.api.io.http.MDSPushInputStream;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;


/**
 * @author Prasad P. Pulikal
 * Background Application used for listening to Push messages and to update the Application Indicator icon with number of messages
 * 
 */
//TODO: 1) Replace all debug statements with the #debug.
//TODO: 2) Check is the BIS registration request is really required to be done every time in startup.
//TODO: 3) StreamConnectionNotifier not able to read the PUSH message...though the pushed message has reached the device. - Done
//TODO: 4) Currently for testing purposes the Application Indicator icon will always been shown in startUP..irrespective of there being a message or not.  
//TODO: 5) Get UE to make a small icon image 20x20 to be used...for now using a temporary vodafone icon image that exists in the resource folder.
//TODO: 6) Reset Application icon value to 0 whenever the main application is started.
//TODO: 7) Give user the option to deregister.
public class BackgroundApp extends Application{	
	
	  /** The application ID used in the push process. */
	    // TODO: Provide a valid ID
	    private static final String APP_ID = "229-k24o381MiD50tll3";
	    
	    /** The port number to listen for messages from the BlackBerry Push server. */
	    // TODO: Provide a valid port number
	    private static final String BIS_PORT = "http://:20135";
	
	    /** The server address for the BlackBerry Push server. */
	    private static final String BBP_SERVER = "http://pushapi.eval.blackberry.com";    
	
	    private static final int CHUNK_SIZE = 256;
	
	    /** Used as the URL to register this device to the BlackBerry Push server. */
	    private static final String REGISTER_URL = BBP_SERVER + "/mss/PD_subReg?serviceid=" + APP_ID + "&osversion="
	            + getDeviceSoftwareVersion() + "&model=" + DeviceInfo.getDeviceName() + ";ConnectionType=mds-public;deviceside=false";
	
	    /** Used as the URL to deregister this device to the BlackBerry Push server */
	    private static final String DEREGISTER_URL = BBP_SERVER + "/mss/PD_subDereg?serviceid=" + APP_ID
	            + ";ConnectionType=mds-public;deviceside=false"; 
    
		  
		private static BackgroundApp _instance = null;
		private static Thread listenerThread;
		private static boolean stopThread;
		
		ApplicationIndicatorRegistry reg;
		ApplicationIndicator AppIndicator ;
		

	    
	    private ListeningThread _dataListener;
	    private int counter = 1;
		
		private BackgroundApp() {
			
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
			
			//For now use a Temporary image...Have to get a icon from the UE team once all things are in place.
			EncodedImage image = EncodedImage.getEncodedImageResource( "vodafone_15x15_checked.png" ); 
			
			ApplicationIcon icon = new ApplicationIcon( image );
			
			ApplicationIndicator indicator = reg.register( icon, false, true);
			
			AppIndicator = reg.getApplicationIndicator();
			AppIndicator.setIcon( icon);
			
		}
		
		public static BackgroundApp _getInstance() {
			
			if(_instance == null)
				_instance = new BackgroundApp();
			
			return _instance;
		}
		

	          
       /**
        * Closes this connection by ensuring the ListeningThread is stopped.
        */
       public void close()
       {
           // Kill the listening thread. 
           if( _dataListener != null )
           {
               _dataListener.stopThread();

               try
               {
                   _dataListener.join();
               }
               catch( InterruptedException e )
               {
                   System.err.println(e.toString());
               }
           }
       }
       
       
	   protected void destroyApp(boolean unconditional) 
		{

							
		}
	 
	   /**
	     * Gets the device's os software version. 
	     * @return The String representing the device's os software version.
	     */
	    private static String getDeviceSoftwareVersion()
	    {
	        // #ifdef VERSION_4_3_PLUS
	        
	        return DeviceInfo.getSoftwareVersion();
	        
	        // #else
//	        
//	        ApplicationManager appManager = ApplicationManager.getApplicationManager();       
//	        ApplicationDescriptor[] appDes = appManager.getVisibleApplications();
//	        
//	        for (int appNo = appDes.length - 1; appNo >= 0; appNo--)
//	        {
//	            if (appDes[appNo].getModuleName().equals("net_rim_bb_ribbon_app"))
//	            {
//	                return appDes[appNo].getVersion();
//	            }
//	        }
//	        return ""; // Should not happen.
	        // #endif   
	    }
	    /**
	     * Connects to a URL and read its contents.
	     * 
	     * @param url The URL to connect to
	     * @return The contents of the URL
	     * @throws IOException Thrown if a IO error occurs while opening the
	     *             connection or reading in the data.
	     */
	    public static byte[] connectAndRead(String url) throws IOException
	    {
	        HttpConnection httpConn = (HttpConnection) Connector.open(url);

	        DataInputStream dis = httpConn.openDataInputStream();

	        byte[] arr = new byte[ CHUNK_SIZE ];
	        int len;
	        DataBuffer buffer = new DataBuffer();
	        while( (len = dis.read(arr)) != -1 )
	        {
	            buffer.write(arr, 0, len);
	        }

	        dis.close();

	        return buffer.getArray();
	    }
	    
	    /**
	     * Attempts to register this device with the BlackBerry Push server. This 
	     * method also alerts the user whether the registration was successful 
	     * or not.
	     * 
	     * @return True if the registration was successful, false otherwise.
	     */
	    public boolean registerForService()
	    {
	        if( !isRadioConnected() )
	        {
	        	try {
	        		//Sleep thread and wait for the user to enter PIN and be connected...FIX
					Thread.sleep(2000);
					
					return registerForService();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//	            return false;
	        }
	     
	        try
	        {
	            // Open a connection with the BB Push server and request registration
	            byte[] encryptedData = connectAndRead(REGISTER_URL);
	             
	            // Open a new connection and register using the encrypted url.
	            final String encryptedParam = new String(encryptedData);
	            String encryptedUrl = BBP_SERVER + "/mss/PD_subReg?osversion=" + getDeviceSoftwareVersion()
	                    + "&model=" + DeviceInfo.getDeviceName() + "&" + encryptedParam + ";ConnectionType=mds-public;deviceside=false";
	          
	            byte[] statusCodeData = connectAndRead(encryptedUrl);
	         
	            // Status code sent back to the application from the BB Push server
	            final String statusCode = new String(statusCodeData);
	            
	            // Note: Developers should also register with the Content Provider 
	            // in order to notify the server that it can push data to this 
	            // device.

	            // If registration succeeded then start listening to messages 
	            // from the server.
	            if( (statusCode.equals(StatusCode.REGISTER_SUCCESSFUL) || statusCode
	                    .equals(StatusCode.USER_ALREADY_SUSCRIBED))||
	                    statusCode.startsWith("Internal Server Error"))           
	            {
	                _dataListener = new ListeningThread();
	                _dataListener.start();
	              
	                System.out.println("Registration Succeeded.");
	                return true;
	            }
	            else
	            {
	            	System.out.println("Registration Failed " + statusCode);
	                return false;
	            }
	        }
	        catch( final Exception e )
	        {
	        	
	        	System.out.println("Registration Error\n" + e.toString());
	            return false;
	        }
	    }
	    
	    /**
	     * Checks if the device's radio is connected to a network. In the case in
	     * which no connection is detected then a dialog box will inform the user.
	     * 
	     * @return True if the device is connected to a network, false otherwise.
	     */
	    private boolean isRadioConnected()
	    {
	        if( RadioInfo.getState() == RadioInfo.STATE_OFF
	                || (RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_DATA) == 0 )
	        {
	            System.out.println("Network services not detected.");          
	            return false;
	        }

	        return true;
	    }
		/**************************************************************************************************************/
		  /**
	     * This thread's role is to listen for any information pushed by the 
	     * BB Push server push server and render it to the screen.
	     */
	    private class ListeningThread extends Thread
	    {
	        private boolean _stop = false;
	        private StreamConnectionNotifier _notifier;

	        /**
	         * Stops the thread from listening.
	         */
	        public void stopThread()
	        {
	        	
	            _stop = true;
	            try
	            {
	                if( _notifier != null )
	                    _notifier.close();
	            }
	            catch( IOException e )
	            {
	                System.err.println(e);
	            }
	        }

	        /**
	         * Listens for any data to read from the server and renders it onto the
	         * TextField. This method will run indefinitely until stop() is called.
	         * 
	         * @see #stop()
	         */
	        public void run()
	        {
	     
	            // Do not attempt to listen for push data if the device is a simulator.
	            if ( DeviceInfo.isSimulator() )
	            {	
	            	//for testing purpose
	            	AppIndicator.setValue(50);
	            	
	            	System.out.println("Listening for push data not supported on a device simulator.");              
	                _stop = true;
	                return;          
	            }
	           
	            StreamConnection stream;
	            while( !_stop )
	            {
	            	
	                try
	                {
	                	System.out.println("Listening for push data.");
	                    // Synchronize here so that we don't end up creating a connection that is never closed.
	                    synchronized( this )
	                    {
	                        // Open the connection once (or re-open after an IOException),  so we don't end up 
	                        // in a race condition, where a push is lost if it comes in before the connection 
	                        // is open again. We open the url with a parameter that indicates that we should 
	                        // always use MDS when attempting to connect.
	                        _notifier = (StreamConnectionNotifier) Connector.open(BIS_PORT + ";ConnectionType=mds-public;deviceside=false");
	                    }

	                    while( !_stop )
	                    {
	                        stream = _notifier.acceptAndOpen(); // Blocking

	                        readAndShowData(stream);
	                    }

	                }
	                catch( Exception ioe )
	                {
	                
	                    // Likely the stream was closed. Catches the exception thrown by 
	                    // _notify.acceptAndOpen() when this thread is stopped.  
	                	System.out.println("Exception in run of Listening Thread-->"+ioe);
	                }

	                if( _notifier != null )
	                {
	                    try
	                    {
	                    	_notifier.close();
	                        _notifier = null;
	                    }
	                    catch( IOException e )
	                    {
	                    
	                    }
	                }
	                
	            }
	       
	        }

	        /**
	         * Reads data from the stream and updates the TextField with the data.
	         * 
	         * @param stream The StreamConnection to read from
	         */
	        public void readAndShowData(StreamConnection stream)
	        {
	            InputStream input = null;
	            MDSPushInputStream pushInputStream;
	       
	            try
	            {
	                input = stream.openInputStream();
	                pushInputStream = new MDSPushInputStream((HttpServerConnection) stream, input);

	                // Extract the data from the input stream.
	                DataBuffer buffer = new DataBuffer();
	                byte[] data = new byte[ CHUNK_SIZE ];
	                int chunk = 0;

	                while( -1 != (chunk = input.read(data)) )
	                {
	                    buffer.write(data, 0, chunk);
	                }

	                // Signal that we have finished reading.
	                pushInputStream.accept();
	                System.out.println("Finished reading thread");	
	                //increment the counter for unread messages
//	                AppIndicator.setValue(counter++);
	                System.out.println(new String(buffer.getArray()));
	                int modHandle = CodeModuleManager.getModuleHandle("bb8900_vf$2dzyb");
	                System.out.println(" modHandle --> "+modHandle);
	                ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
	                System.out.println("After CodeModuleManager.getApplicationDescriptors");
	                ApplicationManager.getApplicationManager().runApplication(apDes[0]);
	                System.out.println("After runApplication");
	               
	            }
	            catch( Exception e )
	            {
	            	
	                // A problem occurred with the input stream , however, the original 
	                // StreamConnectionNotifier is still valid.
	                System.err.println(e.toString());
	                System.out.println("Read Error: restarting input stream&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
	            }
	           
	            // Close the input streams
	            if( input != null )
	            {
	                try
	                {
	                    input.close();
	                }
	                catch( IOException e2 )
	                {
	                }
	            }

	            if( stream != null )
	            {
	                try
	                {
	                    stream.close();
	                }
	                catch( IOException e2 )
	                {
	                }
	            }

	        }
	    }
	    
	    /*******************************************************************************************/
	    /**
	     * Holds a collection of status codes returned by the BlackBerry Push server which
	     * indicates a register or deregister success.
	     */
	    private interface StatusCode
	    {
	        static final String REGISTER_SUCCESSFUL = "rc=200";
	        static final String DEREGISTER_SUCCESSFUL = REGISTER_SUCCESSFUL;
	        static final String USER_ALREADY_SUSCRIBED = "rc=10003";
	        static final String ALREADY_UNSUSCRIBED_BY_USER = "rc=10004";
	        static final String ALREADY_UNSUSCRIBED_BY_PROVIDER = "rc=10005";
	    }
		
}
