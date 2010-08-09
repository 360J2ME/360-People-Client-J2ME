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
package com.zyb.nowplus.data.protocol.transport.http;

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.NetworkListener;
import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;
import com.zyb.nowplus.data.protocol.hessian.MicroHessianInput;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.response.ResponseQueue;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;
import com.zyb.nowplus.data.protocol.types.Registration;
import com.zyb.nowplus.data.protocol.types.Update;
//#debug performancemonitor
import com.zyb.util.PerformanceMonitor;

//#if polish.blackberry
import com.zyb.util.BlackBerryConnectionSuffix;
//#endif

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;


/**
 * Extends the RPGConnection by polling and sending requests over Http.
 *
 * @author Rudy Norff (rudy.norff@vodafone.com)
 */
public class HttpRPGConnection extends RPGConnection
{
	public static final int HTTP_ERROR = 1,
							UNKNOWN_ERROR = 2,
							IO_ERROR = 3,
							SECURITY_ERROR = 4;

	private static final int SERIALISED_SIZE_POLL = 207;

	private static final long LOST_COVERAGE_RETRY_INTERVAL = 28000;	// retry connecting after lost coverage every 28 secs

	private static final long SERVER_DOWN_RETRY_INTERVAL = 10000;

	private static final Object lockInbound = new Object();
	
	private static final Object lockOutbound = new Object();

	private static long lastUserInteractionTime;

	// First time polls are to know fast if backend is reachable.
    private static boolean isFirstTimePoll = true;

    private HttpConnection http;
	private Registration registration;
	private long signupUserID;
	private Update update;

    public HttpRPGConnection(String name, CommunicationManagerImpl cmMgr, ResponseDecoder responseDecoder, AuthenticationListener authListener, 
    						 NetworkListener netListener, ResponseListener respListener, byte connectionMode)
    {
        super(name, cmMgr, responseDecoder, authListener, netListener, respListener);
        
        setConnectionMode(connectionMode);
        
    	if (connectionMode == OUT_IN_BOUND) {
			setPollingMode(SLOW_MODE); // if we have a broken device do 2 conns in 1
		}
    	else {
			setPollingMode(ACTIVE_MODE); // first we are in the active mode
		}
    
		//#debug info
        System.out.println("Created HttpRPGConnection");
    }

    /**
     * Polls the RPG by invoking an HTTP-connection on it. Reads the
     * returned bytes and adds them to the response queue if necessary.
     *
     * @param pollingMode The mode to poll in. Can be active or slow mode.
     * @param url         The URL to poll against.
     */
    private void pollRPG(byte pollingMode, String url)
    {
    	// check if we are roaming and if so we call back the network
    	// listener that we are
    	if (RPGConnection.isRoaming()) {
   			netListener.roamingActive();
    	}

        // get value for the mode we are currently in
        String modeStr = null;

        switch (pollingMode) {
            case RPGConnection.ACTIVE_MODE:
                modeStr = "active";
                break;

            case RPGConnection.SLOW_MODE:
                modeStr = "slow";
                break;

            case RPGConnection.LOW_POW_MODE:
                modeStr = "low-power";
                break;

            case RPGConnection.IDLE_MODE:
                modeStr = "idle";
                break;
        }

        //#debug debug
        System.out.println("Mode: " + modeStr);
        
        Hashtable ht = new Hashtable();
        ht.put("mode", modeStr);
        ht.put("batchsize", new Integer(20480));

        if (isFirstTimePoll) {	// first time polling? only poll one sec
        	//#debug debug
        	System.out.println("First Time Poll. Only One Second!");

        	ht.put("pollInterval", new Integer(1));
        }
        
    	if (cmMgr.getDeviceId() != null) {
        	ht.put("bbpin", cmMgr.getDeviceId());
    	}

        // invoke the post, if it fails sleep 500 msec to not run loops at insane speeds
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALISED_SIZE_POLL);
            
            baos.write(ServerRequest.EMPTY_RPG_HEADER);
            
            ServerRequest.getRequestPayload(ht, "", false, null, null, null, baos);
            baos.flush();
            baos.close();
            byte[] fullPoll = baos.toByteArray();
            baos = null;
            ServerRequest.getRPGHeader(ServerRequest.RPG_MSG_POLL, fullPoll);
            
            //#debug info
            System.out.println("\nINVOKING POLL (mode " + modeStr + ")");
            
            byte[] response = null;
            boolean didNetworkErrorOccur = false;

            for (int i = 1; i < 3; i++) {	// repeat the whole procedure twice if it fails
	            try {
	            	response = invokeHttpPost(fullPoll, RPG_URL + userID);
	            	didNetworkErrorOccur = false;
	            	break;
		        }
	            catch (HttpException he) {
		        	//#debug error
					System.out.println(he.getMessage());
					
					if (i == 2) {
						respListener.errorReceived(-1, ResponseListener.REQUEST_SERVER_UNREACHABLE);
						sleepTime(SERVER_DOWN_RETRY_INTERVAL);
					}
		        }
		        catch (SecurityException se) {
		        	//#debug error
					System.out.println("Error: Calling user disallowed connection!");
					
		        	authListener.userDisallowedConnection();
		        } 
		        //#if polish.blackberry
			        catch(net.rim.device.api.io.ConnectionClosedException cce)
			        {
			        	//#debug error
						System.out.println("Blackberry Error: Connection Closed Exception: "+cce);
			        }
		        //#endif
		        catch (Exception e1) {
		        	didNetworkErrorOccur = true;
		        	
		            //#debug error
		            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Error sending request <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		            //#debug error
		            e1.printStackTrace();
		        }
		        
		        // we sleep to give the server/connection some time to recover
		        sleepTime(1000);
            }
            // polling is done twice (if it fails on the first go)
            // if it fails twice then we tell the network 
            // listener that the network is down. We only
            // inform if the network wasn't down before
            if (didNetworkErrorOccur && !cmMgr.isNetworkDown()) {
            	if (null != netListener) {
        			netListener.networkDown();
        			
            		//#debug info
		            System.out.println("Network down event sent!");
        		}

        		cmMgr.setNetworkState(false);
            }
            else if (!didNetworkErrorOccur && cmMgr.isNetworkDown()) {
            	if (null != netListener) {
        			netListener.networkUp();
        			
            		//#debug info
		            System.out.println("Network up event sent!");
        		}

            	cmMgr.setNetworkState(true);
            }
            
            //#debug info
            System.out.println("\nRETURNED POLL " + (!didNetworkErrorOccur ? "success" : "failure"));
            
            // if we just have a 16 length response, there is nothing to decode
            if (null != response && response.length != ServerRequest.RPG_HEADER_LENGTH) {
                responseDecoder.addResponseForDecoding(response);
            }
        }
        catch (Exception e) {
            //#debug error
            System.out.println("error invoking request" + e);

            sleepTime(500);
        }

        isFirstTimePoll = false;
    }

	/**
	 * <p>
	 * Sets the mode of the connection. The mode can be divided into 3 different
	 * operation levels. Setting it to OUTBOUND will create a connection that
	 * handles an outbound connection useful for invoking requests, e.g. setting
	 * contacts. INBOUND will set the connection to fetch only incoming
	 * responses, e.g. chat messages.
	 * </p>
	 * <p>
	 * Devices only allowing one connection at a time can be served by setting
	 * the OUT_IN_BOUND mode, which will handle incoming responses and the
	 * outgoing requests in the same connection. This will however affect the
	 * user experience.
	 * </p>
	 * 
	 * 
	 * @param connectionMode
	 *            The mode to set the connection to.
	 */
	public void setConnectionMode(byte connectionMode) {
		// set connection mode
		this.connectionMode = connectionMode;

		// if it is not a valid connection mode we set it to a dual connection
		if ((this.connectionMode != OUTBOUND)
				&& (this.connectionMode > OUT_IN_BOUND)
				&& (this.connectionMode != INBOUND)) {
			this.connectionMode = OUT_IN_BOUND;
		}

		if (this.connectionMode == OUTBOUND) {
			this.requestQueue.registerRequestListener(this);
		}
	}
	
    /**
     * 
     * Invokes several requests on the RPG via HTTP and reads 
     * the incoming bytes which should be 16 bytes with the 3rd 
     * byte being 1.
     *
     * @param requestData The request data to send to the backend.
     * @param requestType REQUESTTYPE_AUTH for a secure request,
     * REQUESTTYPE_DATA for an RPG request.
     * @param requestID The ID of the request.
     * 
     * @return The bytes of the request's response or null if something
     * went wrong.
     * 
     */
	private byte[] invokeRPGRequest(byte[] requestData, int requestType, int requestID)
	{
		return invokeRPGRequest(requestData, requestType, new int[] {requestID});
	}
	
    /**
     * Invokes a request on the RPG via HTTP and reads the incoming
     * bytes which should be 16 bytes with the 3rd byte being 1.
     *
     * @param os The output stream to write to. Must be null for HTTP
     * requests, but must be passed for the TCP request as the 2 
     * connections differ.
     * @param requestData The request data to send to the backend.
     * @param requestType REQUESTTYPE_AUTH for a secure request,
     * REQUESTTYPE_DATA for an RPG request.
     * @param requestIDs The IDs of the batch-requests.
     * 
     * @return The bytes of the request's response or null if something
     * went wrong.
     */
    private byte[] invokeRPGRequest(byte[] requestData, int requestType, int[] requestIDs)
    {
        byte[] result = null;
        String requestUrl;
        boolean didNetworkErrorOccur = false;
        boolean isServerUnreachable = false;
    	
        // we get an input stream and send the request
        if (requestType == REQUESTTYPE_AUTH) {
            requestUrl = AUTH_URL;
        }
        else if(requestType == REQUESTTYPE_DATA){
            requestUrl = RPG_URL + userID;
        }
        else {
            throw new IllegalArgumentException("Invalid Request Type: " + requestType );
        }

        // try to send the request 3 times
        for (int i = 1; i < 4; i++) {
        	//#debug info
        	System.out.println("\nSENDING REQUEST " + requestUrl);

            try {
            	result = invokeHttpPost(requestData, requestUrl);
            	didNetworkErrorOccur = false;
            	isServerUnreachable = false;
	            break; // break out of the retry loop
	        }
	        catch (SecurityException se) {				
	        	authListener.userDisallowedConnection();	        
	        }
	        //#if polish.blackberry
	        catch (net.rim.device.api.io.ConnectionClosedException cce) {
	        	//#debug error
				System.out.println("Blackberry Http Connection Closed Exception: "+cce);

				sleepTime(1000);
	        } 
	        //#endif
	        catch (HttpException he) {				
				isServerUnreachable = true;
	        }
	        catch (Exception e) {
	        	didNetworkErrorOccur = true;
	        }
	        finally {
		        //#debug info
		        System.out.println("\nEND >>>>>>>>>>>>>>>>>>>>>>>>>> " + (result != null ? "success" : "failure"));
	        }
        }
        
        // sending the request is done 3 times, if it fails 3 
        // times then we tell the network listener that the 
        // network is down
        if (didNetworkErrorOccur && !cmMgr.isNetworkDown()) {
        	if (netListener != null) {
            	netListener.networkDown();
        	}

        	cmMgr.setNetworkState(false);        	
        }

        if (isServerUnreachable && requestIDs != null) {
        	for (int i = 0; i < requestIDs.length; i++) {
        		respListener.errorReceived(requestIDs[i], ResponseListener.REQUEST_SERVER_UNREACHABLE);
        	}
        }
	        
        return result;
    }

    public void run()
    {
    	//#debug info
    	System.out.println("Started RPGConnection");
    	
    	cmMgr.setNetworkState(true);
    	
        try {
            ResponseQueue responseQueue = responseDecoder.getResponseQueue();
            ServerRequest[] batchRequests = null;

            // handle synchronous connections
            
            try {
	            handleSignup();
	            handleUserActivation();
	            handleReactivation();
	            handleReauthentication();
	            handleUpdateCheck();
	            handleMsisdnRetrieval();
            }
            catch (Exception e) {
    			//#debug error
    			System.out.println("Exception " + e);

    			sleepTime(1000);
            }
            lastUserInteractionTime = System.currentTimeMillis();
            while (connState == STATE_RUN_CONNECTION) {
            	// handle different modes
            	if (respListener.isBusy()
            		|| (System.currentTimeMillis() - lastUserInteractionTime) < RPGConnection.MAX_SECONDS_ACTIVE) {
            		if (pollingMode != ACTIVE_MODE) {
               			setPollingMode(ACTIVE_MODE);
            		}
            	}
            	else if (!respListener.isBusy() 
            			 && (System.currentTimeMillis() - lastUserInteractionTime) >= RPGConnection.MAX_SECONDS_ACTIVE) {
            		if (pollingMode != LOW_POW_MODE) {
            			setPollingMode(LOW_POW_MODE);
            		}
            	}
            	
                // we handle the inbound/polling connection here
                if (connectionMode == INBOUND || connectionMode == OUT_IN_BOUND) {
                    try {
                        pollRPG(pollingMode, RPG_URL + userID);
                    }
                    catch (Exception e) {
                        //#debug error
                        System.out.println("Error polling: " + e.getMessage());
                    }

                    if (pollingMode == LOW_POW_MODE) {
                    	//#debug info
                    	System.out.println("Waiting in low power mode.");

                    	if (connectionMode == OUT_IN_BOUND) {
                            // every 2 seconds we check whether the connection
                            // still needs to be in low power mode for the
                            // time we should not poll in low power mode.
                            long currentTime = System.currentTimeMillis();
                            long lowPowerPollingTime = System.currentTimeMillis();

	                    	while (pollingMode == LOW_POW_MODE
		                           && (currentTime - lowPowerPollingTime) <= RPGConnection.LOW_POWER_POLLING_INTERVAL
		                           && !requestQueue.hasNextRequest()) {
		                    	sleepTime(2000);
		                        currentTime = System.currentTimeMillis();
	                    	}
	                    }
                    	else {
                    		// wait until we get a signal the we got an outbound request
                    		// or the timeout is reached.
                    		synchronized (lockInbound) {
                    			lockInbound.wait(RPGConnection.LOW_POWER_POLLING_INTERVAL);
                    		}
                    	}

	                    //#debug debug
	                    System.out.println("Don't wait in low power mode anymore.");
                    }
                    
                    // the response queue is full!! let's wait until it goes
                    // below an acceptable level again!
                    while (responseQueue.getSize() >= MAX_QUEUE_SIZE) {
                    	sleepTime(500);
                    }
                }

                // outbound/request-sending connection here
                if ((connectionMode == OUTBOUND || connectionMode == OUT_IN_BOUND) && !isFirstTimePoll) {
            		
                	//not using this at the moment, it makes the thread to wait for
                	//notify message and in the meantime all requests are killed by timer.
                	/*synchronized (lockOutbound) {
            			if (!requestQueue.hasNextRequest()) {
            				//#debug info
            				System.out.println("Outbound connection waiting for new requests.");

            				lockOutbound.wait();
            			}
            		}*/

                    // get next item on our stack
                    // if there is no item we drop out
                    if (null != (batchRequests = requestQueue.getNextRequests())) {
                        //#debug info
                    	printDebugRequest(batchRequests);
                    	
                    	for (int i = 0; i < batchRequests.length; i++) {
                    		if (!batchRequests[i].getNeedsSynchronousConnection()) {
                    			continue;
                    		}
                    		
	                    	// send synchronous requests
	                    	if (!sendSynchronousRequest(batchRequests[i])) {
	                    		ServerRequest[] sentRequests = new ServerRequest[1];
	                    		sentRequests[0] = batchRequests[i];
	                    		sendRequestErrors(sentRequests, true);
	                    	}
                    	}
                    	
                    	// send asynchronous requests
                    	boolean didSendingSucceed = sendAsynchronousRequests(batchRequests);

                    	if (!didSendingSucceed) {
                    		sendRequestErrors(batchRequests, false);
                    	}
                    }

                    lastUserInteractionTime = System.currentTimeMillis();
                }
            
                checkNetworkConnection(true);
                sleepTime(500);
            }
        }
        catch (Throwable t) {
            //#debug error
            System.out.println("Run-loop error: " + t);
        }
        
    	//#debug info
    	System.out.println("Finished RPGConnection");
    }
    
    /**
     * 
     * Sends requests in batches. Sends only asynchronous
     * requests.
     * 
     * @param batchRequests The batch requests to send.
     * @param onlySendSynchronousRequests True if requests sent must only
     * be synchronous. False will send asynchronous requests only.
     * 
     * @return True if the sending succeeded. False otherwise.
     * 
     */
    protected boolean sendAsynchronousRequests(ServerRequest[] batchRequests)
    {
    	if (null == batchRequests || cmMgr.isNetworkDown()) {
    		return false;
    	}
    	
        // if true we failed in sending a request
        boolean didSendingRequestSucceed = false;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(ServerRequest.SERIALISED_SIZE);
        int[] requestIDs = new int[batchRequests.length];
        
        // write each request to an output stream
        for (int j = 0; j < batchRequests.length; j++) {
            if (null != batchRequests[j]) {
            	requestIDs[j] = batchRequests[j].getRequestID();
            	
            	if (batchRequests[j].getNeedsSynchronousConnection()) {
            		continue;
            	}

            	batchRequests[j].writeToRPGDataStructure(baos, true);
            }
        }
        
        try {
        	baos.flush();
        }
        catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
        }
        finally {
        	try {
        		baos.close();
			}
        	catch (Exception e2) {
				//#debug error
				System.out.println("Exception " + e2);
			}
        }
        
        byte[] data = baos.toByteArray();

        if (data == null || data.length == 0) {
        	return didSendingRequestSucceed;
        }
        
        // sends and then removes the request from the queue
        if (null != (invokeRPGRequest(data, REQUESTTYPE_DATA, requestIDs))) {
        	didSendingRequestSucceed = true;

            // remove the fire and forget requests immediately so they do not get stuck!
            for (int j = 0; j < batchRequests.length; j++) {
                if (null != batchRequests[j]
                    && batchRequests[j].isFireAndForgetRequest()) {
                	if (batchRequests[j].getNeedsSynchronousConnection()) {
                		continue;
                	}
                	
                    //#debug debug
                    System.out.println("Removing FAFR: " + batchRequests[j].getRequestID());

                    requestQueue.removeRequest(batchRequests[j].getRequestID()); 	
                }
            }
            
            baos = null;
        }
        else {
            // sleep for 500ms
        	sleepTime(500);
        }

        data = null;
        return didSendingRequestSucceed;
    }
    
    /**
     * 
     * Handles the synchronous authentication from the run-loop.
     * 
     */
    private final void handleReauthentication()
    {
        while (connState == STATE_REAUTHENTICATE) {
    		//#debug info
            System.out.println("Authenticating user now.");
    		
    		authenticate(username, password);
    		connState = STATE_UNKNOWN;
            checkNetworkConnection(false); 
        }
    }
    
    /**
     * 
     * Handles the synchronous signup from the run-loop.
     * 
     */ 
    private final void handleSignup()
    {
        while (connState == STATE_SIGNUP) {
    		//#debug info
            System.out.println("Registering new user now.");
    		
    		registerUser();
    		connState = STATE_UNKNOWN;     
            checkNetworkConnection(false);
        }    	
    }
    
    /**
     * 
     * Handles the synchronous sms activation from the run-loop.
     * 
     */
    private final void handleUserActivation()
    {
        while (connState == STATE_USER_ACTIVATE) {
            //#debug info
            System.out.println("Activating user now.");     		
    		
    		activateUser();
    		connState = STATE_UNKNOWN;
            checkNetworkConnection(false);
        }
    }
    
    /**
     * 
     * Handles the synchronous sms reactivation from the run-loop.
     * 
     */
    private final void handleReactivation()
    {
        while (connState == STATE_REACTIVATE) {
    		//#debug info
            System.out.println("Reasking activation code now.");
    		
    		rerequestActivationForUser();
    		connState = STATE_UNKNOWN;
            checkNetworkConnection(false);
        }
    }

    /**
     * 
     * Handles the asynchronous update check.
     * 
     */
    private final void handleUpdateCheck()
    {
        while (connState == STATE_UPDATE_CHECK) {
    		//#debug info
            System.out.println("Checking for updates.");
    		
    		checkForUpdates();
    		connState = STATE_UNKNOWN;
            checkNetworkConnection(false);
        }
    }
    
    /**
     * 
     * Handles the synchronous msisdn retrieval from the run-loop.
     * 
     */
    private final void handleMsisdnRetrieval()
    {
        while (connState == STATE_RETRIEVE_MSISDN) {
    		//#debug info
            System.out.println("Checking for MSISDN.");
    		
    		retrieveMSISDN();
    		connState = STATE_UNKNOWN;
            checkNetworkConnection(false);
        }
    }
    
	/**
	 * 
	 * Handles the logic to signup/register a user synchronously
	 * on the backend.
	 *  
	 * @return -1 if there was an error or the user ID if it succeeded.
	 * 
	 */
	private final long registerUser()
	{
		if (null == registration || null == registration.toHashtable()) {
			authListener.registrationFailed(AuthenticationListener.UNKNOWN);
			return -1;
		}
		
		MicroHessianInput in = null;
		Object o = null;
		
		try {
			Hashtable hReq = registration.toHashtable();
			byte[] payload = ServerRequest.getRequestPayload(
										hReq, ServerRequest.SIGNUP,
										false, null, null, null);
			byte[] response = invokeRPGRequest(payload, REQUESTTYPE_AUTH, 0);
			hReq.clear();
			hReq = null;
			payload = null;
			
			if (null != response) {
				//#debug debug
				System.out.println("Got response for registration: " + new String(response));
				
				InputStream is = new ByteArrayInputStream(response);
				in = new MicroHessianInput(is, cmMgr, null, 0, true,
									ServerRequest.UNKNOWN,
									ServerRequest.UNKNOWN);
				try {
					in.startReply();
					o = in.readObject();
					in.completeReply();
				} catch (Exception he) {
					//#debug error
					System.out.println("Exception " + he);
				}

				try {
					is.close();
				} catch (IOException ie) {
					//#debug error
					System.out.println("Could not close InputStream:"+ie.getMessage());
				}

				response = null;
			}

			Hashtable hResp = null;

			if (null != o) {
				//#debug debug
				System.out.println("Got object for registration");
				
				hResp = (Hashtable) o; // read the response table

				if (null != hResp) {
					//#debug debug
					System.out.println("Got table for registration: " + hResp.toString());
					
					boolean noSmsToBeSent = false;
					
					try {
						Object noSmsObj = hResp.get("nosms");

						if (null != noSmsObj) {
							noSmsToBeSent = ((Boolean) noSmsObj).booleanValue();
						}
						
						//#debug info
						System.out.println("User was autodetected as a Vodafone customer: " + noSmsToBeSent);
					}
					catch (Exception e) {
						//#debug error
						System.out.println("Autodetection failed" + e);
					}

					long userID = ((Long) hResp.get("userid")).longValue();
					cmMgr.registerUserRequestSuccessful(userID, noSmsToBeSent);
					return userID;
				}

				o = null;
			}
			else {	// instead of getting an auth obj back, we probably received a fault
				//#debug info
				System.out.println("No response.");
			}
		}
		catch (Exception e) {
			//#debug error
			System.out.println("An unknown error occured!" + e);
		}
		
		authListener.registrationFailed(AuthenticationListener.UNKNOWN);
		return -1;
	}

	/**
	 * 
	 * Handles the logic to get the MSISDN of the user.
	 *  
	 * @return The MSISDN of the user or null if it was not retrieved (no
	 * Vodafone customer in that case or APN used to connect does not pass
	 * headers such as MSISDN).
	 * 
	 */
	private final String retrieveMSISDN()
	{
		MicroHessianInput in = null;
		Object o = null;
		String msisdn = null;
		
		try {
			byte[] payload = ServerRequest.getRequestPayload(
										null, ServerRequest.GET_NETWORK_INFO,
										false, null, null, null);
			byte[] response = invokeRPGRequest(payload, REQUESTTYPE_AUTH, 0);
			payload = null;
			
			if (null != response) {
				//#debug debug
				System.out.println("Got response for get network info: " + new String(response));
				
				InputStream is = new ByteArrayInputStream(response);
				in = new MicroHessianInput(is, cmMgr, null, 0, true,
									ServerRequest.UNKNOWN,
									ServerRequest.UNKNOWN);
				try {
					in.startReply();
					o = in.readObject();
					in.completeReply();
				}
				catch (Exception he) {
					//#debug error
					System.out.println("Exception " + he);
				}

				try {
					is.close();
				}
				catch (IOException ie) {
					//#debug error
					System.out.println("Could not close InputStream:" + ie.getMessage());
				}

				response = null;
			}

			Hashtable hResp = null;

			if (null != o) {
				//#debug debug
				System.out.println("Got object for get network info");
				
				hResp = (Hashtable) o; // read the response table

				if (null != hResp) {
					//#debug debug
					System.out.println("Got table for network info: " + hResp.toString());
					
					msisdn = (String) hResp.get("msisdn");
				}

				o = null;
			} else {	// instead of getting an auth obj back, we probably received a fault
				//#debug info
				System.out.println("No response.");
			}
		} catch (Exception e) {
			//#debug error
			System.out.println("An unknown error occured!" + e);
		}
		
		cmMgr.msisdnReceived(msisdn);
		return msisdn;
	}
	
	/**
	 * 
	 * When the signup request has been carried out and 
	 * the activation SMS was received this method is called
	 * to send the content of the SMS to the auth/activate
	 * method. The account is then activated.
	 * 
	 * 
	 * @return True if the request succeeded, false otherwise.
	 * 
	 */
	private final boolean activateUser()
	{
		if (null == registration || null == registration.toHashtable()) {
			return false;
		}		
		
		MicroHessianInput in = null;
		Object o = null;
		
		try {
			Hashtable hReq = registration.toHashtable();
			byte[] payload = ServerRequest.getRequestPayload(hReq, 
										ServerRequest.ACTIVATE_USER,
										false, null, null, null);
			byte[] response = invokeRPGRequest(payload, REQUESTTYPE_AUTH, 0);
			payload = null;
			
			if (null != response) {
				
				InputStream is = new ByteArrayInputStream(response);
				in = new MicroHessianInput(is, cmMgr, null, 0, true,
									ServerRequest.UNKNOWN,
									ServerRequest.UNKNOWN);
				try {
					in.startReply();
					o = in.readObject();
					in.completeReply();
				}
				catch (Exception he) {
					//#debug error
					System.out.println("Exception " + he);
				}

				try {
					is.close();
				}
				catch (IOException ie) {
					//#debug error
					System.out.println("Could not close InputStream:"+ie.getMessage());
				}
				
				response = null;
			}

			if (null != o) {
				try {
					o = null;
					cmMgr.registrationSucceeded(signupUserID);
					return true;
				}
				catch (ClassCastException cce) {
						//#debug info
						System.out.println("Received an error message.");

						cmMgr.registrationFailed(AuthenticationListener.UNKNOWN);
						return false;
				}
			}
			else {	// instead of getting an auth obj back, we probably received a fault
				//#debug info
				System.out.println("No response.");

				cmMgr.registrationFailed(AuthenticationListener.UNKNOWN);
				return false;
			}
		}
		catch (Exception e) {
			//#debug error
			System.out.println("An unknown error occured!" + e);
		}
		
		return false;
	}
		
	/**
	 * 
	 * Rerequests the activation code for the user if the
	 * activation SMS has never arrived.
	 * 
	 * 
	 * @return True if the request succeeded, false otherwise.
	 * 
	 */
	private final boolean rerequestActivationForUser()
	{
		if (null == registration || null == registration.toHashtable()) {
			return false;
		}
		
		MicroHessianInput in = null;
		Object o = null;
			
		try {
			Hashtable hReq = registration.toHashtable();
			byte[] payload = ServerRequest.getRequestPayload(hReq, 
						ServerRequest.REQUEST_ACTIVATION_CODE,
						false, null, null, null);
			byte[] response = invokeRPGRequest(payload, REQUESTTYPE_AUTH, 0);
			payload = null;
			
			if (null != response) {
				InputStream is = new ByteArrayInputStream(response);
				in = new MicroHessianInput(is, cmMgr, null, 0, true,
									ServerRequest.UNKNOWN,
									ServerRequest.UNKNOWN);
				try {
					in.startReply();
					o = in.readObject();
					in.completeReply();
				}
				catch (Exception he) {
					//#debug error
					System.out.println("Exception " + he);
				}

				try {
					is.close();
				}
				catch (IOException ie) {
					//#debug error
					System.out.println("Could not close InputStream:"+ie.getMessage());
				}
				
				response = null;
			}

			if (null != o) {
				boolean noSmsToBeSent = false;
				try {
					Hashtable ht = (Hashtable) o;
					noSmsToBeSent = ((Boolean) ht.get("nosms")).booleanValue();
				} catch (Exception e) {
					//#debug error
					System.out.println("Exception " + e);
				}
				
				o = null;
				//#debug info
				System.out.println("Successfully rerequested the activation code!");
				cmMgr.activationCodeRequested(noSmsToBeSent);
				return true;
			}
			else {	// instead of getting an auth obj back, we probably received a fault
				//#debug info
				System.out.println("No response.");

				authListener.registrationFailed(AuthenticationListener.ACTIVATION_TIMED_OUT);
				return false;
			}
		}
		catch (Exception e) {
			//#debug error
			System.out.println("An unknown error occured!" + e);

			authListener.registrationFailed(AuthenticationListener.ACTIVATION_TIMED_OUT);
		}
        
        return false;
	}
	
	/**
	 * 
	 * Checks if an update for the client is available.
	 * 
	 * 
	 * @return True if the request succeeded, false otherwise.
	 * 
	 */
	private final void checkForUpdates()
	{
		if ((null == update) || (null == update.toHashtable())) {
			cmMgr.clientIsUpToDate();
			return;
		}
		
		MicroHessianInput in = null;
		Object o = null;
		
		try {
			Hashtable hReq = update.toHashtable();
			byte[] payload = ServerRequest.getRequestPayload(hReq, 
										ServerRequest.IS_UPDATE_AVAILABLE,
										false, null, null, null);
			byte[] response = invokeRPGRequest(payload, REQUESTTYPE_AUTH, 0);
			payload = null;
			
			if (null != response) {
				InputStream is = new ByteArrayInputStream(response);
				in = new MicroHessianInput(is, cmMgr,
									respListener, 
									0, true,
									ServerRequest.GET,
									ServerRequest.UPDATE_AVAILABLE);
				try {
					in.startReply();
					o = in.readObject();
					in.completeReply();
				}
				catch (Exception he) {
					//#debug error
					System.out.println("Exception " + he);
					
					// we return, the fault was called back in MHI
					return;
				}

				try {
					is.close();
				}
				catch (IOException ie) {
					//#debug error
					System.out.println("Could not close InputStream:"+ie.getMessage());
				}
				
				response = null;
			}

			Hashtable htResponse = null;

			if (null != o) {
				try {
					htResponse = (Hashtable) o;
					Update update = new Update(htResponse);
					
					if (update.isUpdateAvailable()) {
						//#debug info
						System.out.println("Client has UPDATE AVAILABLE.");
						cmMgr.clientUpdateAvailable(update);
						return;
					}
				}
				catch (Exception e) {
					//#debug error
					System.out.println("Could not cast response hashtable in UPDATE " + e);
				}
			}
		}
		catch (Exception e) {
			//#debug error
			System.out.println("An unknown error occured!" + e);
		}
		
		cmMgr.clientIsUpToDate();
		return;
	}
    	
	/**
	 * Authenticates a user with the Vodafone API-backend using the
	 * getsessionbycredentials-method. Sets the needed objects (sessions) in the
	 * connectivity class so it can reuse them in later calls. The
	 * authentication method is "smart". It detects whether a user session is
	 * already there and if so uses it instead of re-requesting a new session.
	 *
	 * @param username The username of the user.
	 * @param password The password of the user.
	 *
	 * @return True if authenticating succeeded, false otherwise.
	 *
	 */
	protected final boolean authenticate(String username, String password)
	{
		if (username == null || password == null) {
			//#debug info
			System.out.println("username or password are null!");

			return false;
		}

		//#debug info
		System.out.println("Authenticate called with: " + username + "; " + password);

		boolean result = false;

		try {
			Object o = null;
			Hashtable hResp = null;
			Hashtable hContent = null;
			
			byte[] payload = ServerRequest.getRequestPayload(null, 
								 	ServerRequest.AUTHENTICATE,
								 	true, username, password, 
								 	null);
			byte[] response = invokeRPGRequest(payload, REQUESTTYPE_AUTH, 0);
			payload = null;
			
			if (null != response) {
				InputStream is = new ByteArrayInputStream(response);
				MicroHessianInput in = new MicroHessianInput(is, cmMgr, 
									null, 0, true,
									ServerRequest.UNKNOWN,
									ServerRequest.UNKNOWN);
				try {
					in.startReply();
					o = in.readObject();
					in.completeReply();
				}
				catch (Exception he) {
					fireAuthFailed(he);
					return false;
				}

				try {
					is.close();
				}
				catch (IOException ie) {
					//#debug error
					System.out.println("Could not close InputStream:"+ie.getMessage());
				}
			}

			if (null != o) {
				try {
					hResp = (Hashtable) o; // read the response table
				}
				catch (ClassCastException cce) {
					fireAuthFailed(cce);
					return false;
				}

				o = null;
				hContent = (Hashtable) hResp.get("session");

				if (null != hContent) {
					//#debug info
					System.out.println("SessID: " + hContent.get("sessionid") 
							+ " SessSec: " + hContent.get("sessionsecret") 
							+ " UserID: " + hContent.get("userid"));
					
					sessionID = (String) hContent.get("sessionid");
					sessionSecret = (String) hContent.get("sessionsecret");						
					userID = ((Long) hContent.get("userid")).toString();

					if ((null != sessionID) && (null != sessionSecret) && (null != userID)) {
						result = true;
					}
					else {
						fireAuthFailed(new Exception("no session credentials received"));
						return false;
					}
					
					hContent.clear();
					hContent = null;
				}
				else {
					fireAuthFailed(new Exception("no session received"));
					return false;
				}
				
				hResp.clear();
				hResp = null;
			}
			else {	// instead of getting an auth obj back, we probably received a fault
				fireAuthFailed(new Exception("auth obj = null"));
				
				// reset username and password, the object was null something was really wrong.
				RPGConnection.username = null;
				RPGConnection.password = null;
				
				return false;
			}
		}
		catch (Exception e) {
			fireAuthFailed(e);
			return false;
		}
		
		if (true == result) {
			fireAuthSucceeded();
		}		

		return result;
	}	
    
	private void fireAuthSucceeded()
	{
		//#debug info
		System.out.println("Authentication succeeded");
		
		// set our session values in the conn mgr
		cmMgr.setSessionValues(sessionID, sessionSecret, userID, appInstance);

        //fix for bug 0006180, don't show auth related notification if the connection is not running
        //or server response errors
		// MH: auth failed always needs to be fired
		cmMgr.authenticationSucceeded();
	}
	
	private void fireAuthFailed(Exception e)
	{
		//#debug error
		System.out.println("Authentication failed " + e);
		
        // reset username and password
		cmMgr.setSessionValues(null, null, null, null);
		
        //fix for bug 0006180, don't show auth related notification if the connection is not running
        //or server response errors
		// MH: auth failed always needs to be fired		
        cmMgr.authenticationFailed(AuthenticationListener.AUTH_FAILED_UNKNOWN);	
	}
    
    /**
     * 
     * Sends requests in batches. Sends only synchronous or asynchronous
     * requests at once, but never both at the same time.
     * 
     * @param request The request to send synchronously.
     * 
     * @return True if the sending succeeded. False otherwise.
     * 
     */
    private boolean sendSynchronousRequest(ServerRequest request) {
    	if (null == request || cmMgr.isNetworkDown()) {
    		return false;
    	}
    	
        // if true we failed in sending a request
        boolean didSendingRequestSucceed = false;
        
        // try to send the request 3 times
        for (int i = 1; i < 4; i++) {
        	byte[] responseData = null;
        	ByteArrayOutputStream baos = new ByteArrayOutputStream(ServerRequest.SERIALISED_SIZE);
        	request.writeToRPGDataStructure(baos, false);
        	
            // sends and then removes the request from the queue
            if (null != (responseData = invokeRPGRequest(baos.toByteArray(), 
            								REQUESTTYPE_AUTH,
            								request.getRequestID()))) {
            	didSendingRequestSucceed = true;

                // this only applies for synchronous requests, we fetch
                // the response data directly
                if ((null != responseData) && (responseData.length > 0)) {
                	//#debug debug
                	System.out.println("Adding synchronous response data.");
                	responseDecoder.addResponseForDecoding(responseData, 
                									request.getRequestID());
                }
                break;
            } else {
                //#debug info
                System.out.println("Resending request(s)");

                // sleep for: <num of tries> * 500ms
                sleepTime(500 * i);
            }
            
            try {
            	baos.flush();
    			baos.close();
    			baos = null;
    		}
            catch (Exception e) {
            	// Ignore.
            }
        }

        return didSendingRequestSucceed;
    }

    
    /**
     * 
     * Calls back the response listener with the requests that failed.
     * 
     * @param batchRequests The batch requests to call back for.
     * @param onlySendSynchronousRequests True if only
     * synchronous requests failed, false for asynchronous requests.
     * 
     */
    private void sendRequestErrors(ServerRequest[] batchRequests,
									boolean onlySendSynchronousRequests)
    {
        // get comms manager's response listener and notify of error!

        for (int j = 0; j < batchRequests.length; j++) {        	
        	int requestID = -1;

        	if (null != batchRequests[j]) {
        		if (onlySendSynchronousRequests != batchRequests[j].getNeedsSynchronousConnection()) {
            		continue;
            	}
        		
        		requestID = batchRequests[j].getRequestID();
        	}
        	
            //#debug error
            System.out.println("Failed sending request: " + requestID);

            respListener.errorReceived(requestID, ResponseListener.REQUEST_FAILED_SENDING);
            requestQueue.removeRequest(requestID);
        }
    }
    
	/**
	 * <p>
	 * Sets the polling mode. There are 4 different modi:
	 * </p>
	 * 
	 * <p>
	 * ACTIVE_MODE is when the device is actively polling the connection.
	 * </p>
	 * <p>
	 * SLOW_MODE is when the device is actively polling on the connection, but
	 * the device only supports one connection for the up and downstream
	 * connection. The user experience of the SLOW_MODE is reduced and should
	 * only be used when ACTIVE_MODE will not work.
	 * </p>
	 * <p>
	 * LOW_POWER_MODE will be used in non-Vodafone operator environments when
	 * the device has been connected for too long without any activity from the
	 * user. The polling will have a time distance of 5 minutes between each
	 * poll to save power.
	 * </p>
	 * <p>
	 * IDLE_MODE will be used in Vodafone networks and kicks in when the device
	 * has not been actively used as well (see LOW_POWER_MODE). The difference
	 * is there will be no polling at all during that time and the device can be
	 * woken up to the ACTIVE_MODE again by a wakeup SMS.
	 * IDLE_MODE is currently used for Blackberry PUSH implementation.
	 * However in this case the a PUSH message does not wake up the client
	 * On a blackberry the PUSH message updates the Notificaton icon on the main screen with the number of unread messages.
	 * </p>
	 * 
	 * @param pollingMode
	 *            The mode to set the polling to.
	 */
    private synchronized void setPollingMode(byte pollingMode)
	{
		// set polling mode
		RPGConnection.pollingMode = pollingMode;

		//#debug info
		System.out.println("SETTING TO NEW MODE (ACTIVE=0; LOW POW=1; SLOW=2): " + pollingMode);
		
		// if it is not a valid connection mode we set it to a dual connection
		if ((pollingMode != ACTIVE_MODE)
				&& (pollingMode != SLOW_MODE)
				&& (pollingMode != LOW_POW_MODE)
				&& (pollingMode != IDLE_MODE)) {
			//#debug error
			System.out.println("Mode not recognized, setting to active.");

			pollingMode = ACTIVE_MODE;
		}
	}
    
    private void checkNetworkConnection(boolean tryOnce)
	{
    	boolean looping = true;

		//something went wrong with the network
		while (cmMgr.isNetworkDown() && looping) {
			//#debug info
			System.out.println("Trying to reconnect.");

			// we will do a short poll. the error handling
			// inside the poll will send the networkDown/Up
			// events if needed
			isFirstTimePoll = true; // short poll
				
			pollRPG(pollingMode, RPG_URL + userID);
			
			if (cmMgr.isNetworkDown()) {
				// sleep 28 seconds for the next poll
				sleepTime(LOST_COVERAGE_RETRY_INTERVAL);
			}

			if (tryOnce) {
				looping = false;
			}
		}
	}
	
	/**
	 * 
	 * Sets to signup mode where the user will be able to register
	 * to Now+.
	 * 
	 * @param registration The registration object.
	 * 
	 */
	public void setNeedsUserRegistration(Registration registration)
	{
		//#debug info
		System.out.println("SIGNUP CALLED");

		connState = STATE_SIGNUP;
		this.registration = registration;
	}
	
	/**
	 * 
	 * Sets to activation mode after the user has registered and
	 * received the activation SMS.
	 * 
	 * @param registration The registration object.
	 * 
	 */
	public void setNeedsUserActivation(Registration registration, long userID)
	{
		//#debug info
		System.out.println("SMS AUTHENTICATE CALLED");

		connState = STATE_USER_ACTIVATE;
		this.signupUserID = userID;
		this.registration = registration;
	}
	
	
	/**
	 * 
	 * Called if the user registered but the activation code was 
	 * never received via SMS:
	 * 
	 * @param registration The registration object.
	 * 
	 */
	public void setNeedsReactivationSMS(Registration registration)
	{
		//#debug info
		System.out.println("SMS AUTHENTICATE CALLED");

		connState = STATE_REACTIVATE;
		this.registration = registration;
	}
	
	/**
	 * 
	 * Passes the username and password and sets to authentication mode.
	 * 
	 * @param username The username to be authenticated against.
	 * @param password The password to use for the authentication.
	 * 
	 */
	public void setNeedsReauthentication(String username, String password)
	{
		//#debug info
		System.out.println("REAUTH CALLED: " + username + " " + password);

		connState = STATE_REAUTHENTICATE;
		RPGConnection.username = username;
		RPGConnection.password = password;
	}
	
	/**
	 * 
	 * Sets to mode that allows for checking of updates.
	 * 
	 */
	public void setNeedsUpdateCheck(Update update)
	{
		//#debug info
		System.out.println("UPDATE CHECK CALLED");

		connState = STATE_UPDATE_CHECK;
		this.update = update;
	}
	
	/**
	 * 
	 * Sets to mode to check for the MSISDN of the user.
	 * 
	 */
	public void setNeedsMsisdnRetrieval()
	{
		//#debug info
		System.out.println("MSISDN RETRIEVAL CALLED");

		connState = STATE_RETRIEVE_MSISDN;
	}

    /**
     * Posts data to a URL and fetches the response and returns it in bytes.
     *
     * @param postData The data in bytes to post.
     * @param url      The URL to post to.
     * @return The response in bytes.
     * 
     * @throws Exception Thrown if there was an error with the connection.
     */
    protected byte[] invokeHttpPost(byte[] postData, String url) throws Exception
    {
        if (postData == null || url == null) {
            throw new HttpException("Post data or URL were null.");
        }
        
        int dataCounter = 0;
        
        //#debug debug
        Toolkit.printHessian(new ByteArrayInputStream(postData));
        
        InputStream is = null;
        OutputStream os = null;
        byte[] data = null;

        try {
        	//#if polish.blackberry
        		url = BlackBerryConnectionSuffix.connSuffixStr == null ? url : url + BlackBerryConnectionSuffix.connSuffixStr;
        	//#endif
        		
	        http = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
	
	        // set http specific data
	        http.setRequestMethod(HttpConnection.POST);
	        http.setRequestProperty("Content-Type", "application/binary");
	        http.setRequestProperty("User-Agent", System
	                .getProperty("microedition.platform")
	                + " Profile/"
	                + System.getProperty("microedition.profiles")
	                + " Configuration/"
	                + System.getProperty("microedition.configuration"));
	        
	        os = http.openOutputStream();
	        // one shot. Some WapGW's are broken and don't support POSTing in chunks

	        //#debug performancemonitor
	        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.HTTP_OUT);

	        os.write(postData);

	        //#debug performancemonitor
	        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.HTTP_OUT);
	    	
			os.flush();
	
        	if (null != os) {
        		os.close();
        		os = null;
        	}
	        dataCounter += postData.length;
	
	        // parse the response
	        int rc = http.getResponseCode();
	        int len = (int) http.getLength();
	
	        //#debug info
	        System.out.println("Resp code: " + rc + "; Resp len: " + len + "; Mime Type: " + http.getHeaderField("Content-Type"));
	
	        if (rc == 200) {
		        // Get the length and process the data
		        is = http.openInputStream();
		
		        // read our bytes
		        if (len >= 0) {
		        	data = new byte[len];
		        	dataCounter += len;
		            int actual = 0;
		            int bytesread = 0;
		
		            while ((bytesread != len) && (actual != -1)) {
		    			//#debug performancemonitor
		    	        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.HTTP_IN);

		                actual = is.read(data, bytesread, len - bytesread);

		    			//#debug performancemonitor
		    	        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.HTTP_IN);

		                bytesread += actual;
		            }
		        } else {
		            ByteArrayOutputStream baos = new ByteArrayOutputStream();
		            int actual;
		
	    			//#debug performancemonitor
	    	        PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.HTTP_IN);
		            while (-1 != (actual = is.read())) {
		            	dataCounter++;
		                baos.write(actual);
		            }
	    			//#debug performancemonitor
	    	        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.HTTP_IN);
		
		            baos.flush();
		            baos.close();
		            data = baos.toByteArray();
		            baos = null;
		        }
		        if(is!=null){
	            	is.close();
	            	is = null;
	            }
		    } else {
	        	//#debug error
	            System.out.println("HTTP Error " + rc + " connecting to "+url);

	            throw new HttpException("HTTP Exception");
	        }
	     
	        
        } catch (SecurityException se) {
        	//#debug error
			System.out.println("User disallowed connection!" + se);
			
			throw se;	        
        } 
        //#if polish.blackberry
        catch (net.rim.device.api.io.ConnectionClosedException e) 
        {
            //#debug error
            System.out.println("Blackberry http exception (net.rim.device.api.io.ConnectionClosedException) and also be thrown out:" + e);
            
            throw e;
        }   
        //#endif
        catch (IOException ioe) {	
        	//#debug error
        	System.out.println("I/O failure!" + ioe);
          
        	throw new Exception(ioe.getMessage());
        } catch (Exception e) {
            //#debug error
            System.out.println("Unknown http issue!" + e);
                      
            throw new HttpException("Unknown Connection Exception");
        } finally {
			//#debug performancemonitor
	        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.HTTP_IN);
			//#debug performancemonitor
	        PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.HTTP_OUT);
        	
        	if (null != is) {
	        	try {
	        		is.close();
	        	} catch (Exception e) {
	    			//#debug error
	    			System.out.println("Exception " + e);
	        	}

	        	is = null;
        	}
        	
        	if (null != os) {
	        	try {
	        		os.close();
	        	} catch (Exception e) {
	    			//#debug error
	    			System.out.println("Exception " + e);
	        	}

	        	os = null;
        	}
        	
    		respListener.dataTransmitted(dataCounter);

        	if (null != http) {
        	    try {
        	    	http.close();
        	    	http = null;
        	    } catch (Exception e) {
        	        //#debug error
        	        System.out.println("Unable to close http connection" + e);
        	    }

        	    
        	}
        }
        
        //#debug debug
        Toolkit.printHessian(new ByteArrayInputStream(data));

        return data;
    }

    /**
     * 
     * Stops this connection thread.
     * 
     */
	public void stopConnection(boolean finalStop)
	{
		super.stopConnection(finalStop);
		
		if (null != http) {
			try {
				http.close();
			} catch (Exception e) {
				//#debug error
				System.out.println("Exception " + e);
			}
		}
		
		http = null;
	}

	public void notifyOfNewItemInQueue()
	{
		//#debug debug
		System.out.println("New request in request queue");

		synchronized (lockOutbound) {
			lockOutbound.notify();
		}
	}

	public static void notifyUserInteraction()
	{
		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis - lastUserInteractionTime < RPGConnection.MAX_SECONDS_ACTIVE) {
			return;
		}

		lastUserInteractionTime = currentTimeMillis;

		//#debug debug
		System.out.println("Notify user interaction");

		synchronized (lockInbound) {
			lockInbound.notify();
		}
	}

    private void sleepTime(long msecs)
    {
        try {
        	Thread.sleep(msecs);
        }
        catch (Exception e) {
			//#debug error
			System.out.println("Exception " + e);
        }
	}
}
