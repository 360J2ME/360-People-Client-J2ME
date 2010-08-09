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
package com.zyb.nowplus.data.protocol.transport;

import com.zyb.nowplus.data.protocol.AuthenticationListener;
import com.zyb.nowplus.data.protocol.CommunicationManagerImpl;
import com.zyb.nowplus.data.protocol.NetworkListener;
import com.zyb.nowplus.data.protocol.request.RequestQueue;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;
import com.zyb.nowplus.data.protocol.response.ResponseListener;

//#if polish.blackberry && use.servers.list.choices
//#= import com.zyb.nowplus.presentation.view.forms.ServerListForm;
//#endif

//#if polish.blackberry
import net.rim.device.api.system.RadioInfo;
//#endif

/**
 * 
 * A threaded class triggered by the connection manager and queued by the
 * RequestQueue to invoke requests on the backend and call back with the
 * results.
 * 
 * E.g. a RPGConnection can be used to retrieve contacts whilst another
 * RPGConnection can be used to set activities at the same time.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 * 
 */
public abstract class RPGConnection extends Thread
{
	public static final String SE_LAC = "com.sonyericsson.net.lac"; //location area code
	public static final String HOME_MCC =
		//#if polish.vendor == Nokia
		//#= "com.nokia.mid.mcc";
		//#elif polish.vendor == LG 
		//#= "com.lge.net.mcc";
		//#elif polish.vendor == Samsung
		//#= "com.samsung.mcc";
		//#elif polish.vendor == Huawei
		//#= "com.huawei.properties.mcc";
		//#else
		"com.sonyericsson.net.mcc"; //mobile country code
		//#endif
	public static final String HOME_MNC = "com.sonyericsson.net.mnc"; //mobile network code
	public static final String MCC = 		
		//#if polish.vendor == Nokia
		//#= "com.nokia.mid.networkID";
		//#elif polish.vendor == LG
		//#= "com.lge.net.cmcc";
		//#elif polish.vendor == Samsung
		//#= "com.samsung.cmcc";
		//#elif polish.vendor == Huawei
		//#= "com.huawei.properties.cmcc";
		//#else
		"com.sonyericsson.net.cmcc"; //current mobile country code
		//#endif
	public static final String MNC = "com.sonyericsson.net.cmnc"; //current mobile network code
	public static final String SE_RAT = "com.sonyericsson.net.rat"; //current Radio Access Technology
	public static final String SE_CELLID = "com.sonyericsson.net.cellid"; //cell id
	public static final String ISONHOMEPLMN =
		//#if polish.vendor == LG
		//#= "com.lge.net.isonhomeplmn";
		//#= public static final String HOME_MCC_3G = "com.3g.net.roaming.mcc";
		//#= public static final String MCC_3G = "com.3g.net.roaming.cmcc";
		//#else
		"com.sonyericsson.net.isonhomeplmn" ;
		//#endif
		
    //#if polish.blackberry && use.servers.list.choices
	//#=	public static  String RPG_URL = ServerListForm.RPG_URL;
	//#else
		//#if url.rpg:defined
			//#message Setting RPG_URL to ${url.rpg}
			//#= public static final String RPG_URL = "${url.rpg}";
		//#else				
			public static final String RPG_URL = "http://monstersinc04.next.vodafone.com:80/rpg/mcomet/";
		//#endif
	//#endif
	  
	//#if url.socket.rpg:defined
		//#message Setting RPG_SOCKET_URL to ${url.socket.rpg}
		//#= public static final String RPG_SOCKET_URL = "${url.socket.rpg}";
	//#else				
			public static final String RPG_SOCKET_URL = "socket://monstersinc05.next.vodafone.com:9900";
	//#endif
	
	//#if polish.blackberry && use.servers.list.choices
	//#=	public static  String AUTH_URL =ServerListForm.AUTH_URL;
	//#else
		//#if url.auth:defined
			//#message Setting AUTH_URL to ${url.auth}
			//#= public static final String AUTH_URL = "${url.auth}";
		//#else				
	    	public static final String AUTH_URL = "http://devapi.next-vodafone.com/services/hessian/";
		//#endif
    //#endif
	
	public static final String SE_HOME_MCC = "com.sonyericsson.net.mcc"; //mobile country code
	public static final String SE_HOME_MNC = "com.sonyericsson.net.mnc"; //mobile network code
	public static final String SE_MCC = "com.sonyericsson.net.cmcc"; //current mobile country code
	public static final String SE_MNC = "com.sonyericsson.net.cmnc"; //current mobile network code
	public static final String SE_ISONHOMEPLMN = "com.sonyericsson.net.isonhomeplmn" ;
	
    public static final int REQUESTTYPE_AUTH = 0, 
    						REQUESTTYPE_DATA = 1;

	// the maximum number of bytes the ResponseQueue is allowed to have before
	// the incoming connection will stop accepting incoming responses
	public static final int MAX_QUEUE_SIZE = 5120; // 5 kb of un-/compressed
	// data

	/*
	 * if the handset supports only 1 connection, OUT_IN_BOUND should be
	 * used. Otherwise create 2 connections and set one to OUTBOUND and the
	 * other to INBOUND for optimal results. OUT_IN_BOUND will cause the device
	 * to share one connection e.g. for setting contacts and at the same time
	 * retrieving chat messages. This will cause the chat message to be delayed
	 * until contacts have been set, of course.
	 */
	public static final byte OUTBOUND = 0, INBOUND = 1, OUT_IN_BOUND = 2;

	protected byte connectionMode;

	// active: poll every 30-45 seconds, low_pow: poll every 5 minutes
	// slow: poll every 5 seconds (on devs with only 1 connection)
	// idle: no polling for BB PUSH implementation - remember to check if the 5 hour logout on server is a issue with this state.
	public static final byte ACTIVE_MODE = 0, LOW_POW_MODE = 1, SLOW_MODE = 2, IDLE_MODE = 3;
	
	protected static byte pollingMode;
	
	// the different connection possibilities.
	public static final byte TCP_MODE = 1, HTTP_MODE = 2, UNKNOWN_MODE = 0;

	// poll every xx seconds in low power mode
	public static final long LOW_POWER_POLLING_INTERVAL = 300000;
	
	// the seconds the connection will stay in active mode without any
	// request activity
	public static final long MAX_SECONDS_ACTIVE = 80000;

	public static String sessionID, sessionSecret, userID, appInstance;
	protected static String username, password;
	protected final CommunicationManagerImpl cmMgr;
	protected final AuthenticationListener authListener;
	protected final NetworkListener netListener;

	// HANDLING OF CURRENT NETWORK STATES
	protected static final int STATE_UNKNOWN = 0, 
								STATE_SIGNUP = 1,
								STATE_USER_ACTIVATE = 2,
								STATE_REACTIVATE = 3,
								STATE_UPDATE_CHECK = 4,
								STATE_REAUTHENTICATE = 5,
								STATE_RUN_CONNECTION = 6,
								STATE_RETRIEVE_MSISDN = 7,
						        STATE_FINAL_POLL = 8;

	protected int connState;
	// END: HANDLING OF CURRENT NETWORK STATES

	protected final ResponseListener respListener;
	protected RequestQueue requestQueue;
	protected final ResponseDecoder responseDecoder;

    protected RPGConnection(String name, CommunicationManagerImpl cmMgr, ResponseDecoder responseDecoder, AuthenticationListener authListener,
    						NetworkListener netListener,
    						ResponseListener respListener)
    {
    	super(name);
    	
    	//#debug debug
    	System.out.println("Constructing RPGConnection");
    	
    	this.cmMgr = cmMgr;
    	this.responseDecoder = responseDecoder;
		this.authListener = authListener;
		this.netListener = netListener;
		this.respListener = respListener;
		this.requestQueue = RequestQueue.getInstance();
		
		this.connState = STATE_UNKNOWN;
	}
		
	/**
	 * Starts the connection and its thread.
	 * 
	 */
	public void startConnection()
	{
		setIsConnectionRunning();
		start();
	}

	/**
	 * Stops the connection and its thread.
	 * 
	 */
	public void stopConnection(boolean finalStop)
	{
		if (finalStop) {
			connState = STATE_FINAL_POLL;
			pollingMode = IDLE_MODE;
		}
		else {
			connState = STATE_UNKNOWN;
		}
	}
	
	/**
	 * 
	 * Sets to active connection mode after the user has registered and
	 * successfully logged in.
	 * 
	 */
	private void setIsConnectionRunning()
	{
		//#debug info
		System.out.println("ACTIVE CONNECTION CALLED");

		connState = STATE_RUN_CONNECTION;
	}	
	
	/**
	 * 
	 * Indicates if we are in roaming mode or not. This method depends
	 * on Sony Ericsson internal calls.
	 * 
	 * @return True if the device is currently in roaming mode,
	 * false otherwise.
	 * 
	 */
	public static boolean isRoaming()
	{
		//#debug info
		System.out.println("Check roaming");

		boolean result = false;

		// Only for debugging purposes.
		//#if activate.roamingonly
			//#= if (true) {
			//#= 	return true;
			//#= }
		//#endif

		//#if polish.blackberry
			result = ((RadioInfo.getNetworkService() & RadioInfo.NETWORK_SERVICE_ROAMING) > 0);
		//#else
			String  homeMCC = null, 
					currentMCC = null,
					isOnHomePLMN = null;
			
			try {
				homeMCC = System.getProperty(HOME_MCC);
				currentMCC = System.getProperty(MCC);
				isOnHomePLMN = System.getProperty(ISONHOMEPLMN);
				//#if polish.vendor == LG
				if(homeMCC == null) { //fallback to 3G
					//#= homeMCC = System.getProperty(HOME_MCC_3G);
				}
				if(currentMCC == null) { //fallback to 3G
					//#= currentMCC = System.getProperty(MCC_3G);
				}
				//#endif
			} catch (Exception e) {
				//#debug error
				System.out.println("Could not get SE roaming values.");
			}
	
			//#debug debug
			System.out.println("isRoaming(): " + MCC + " is \"" + homeMCC + "\", " + HOME_MCC + " is \"" + currentMCC + "\", "
					//#if polish.vendor == Sony-Ericsson || polish.vendor == LG 
					+ ISONHOMEPLMN 
					+ " is \"" + isOnHomePLMN
					//#endif
					+ "\"");
			
			if (null == homeMCC || null == currentMCC
					//#if polish.vendor == Sony-Ericsson || polish.vendor == LG
					|| null == isOnHomePLMN
					//#endif
					) {
	
				//#debug warning
				System.out.println("isRoaming(): Either " + MCC + " (\"" + homeMCC + "\") or " + HOME_MCC + " (\"" + currentMCC + "\") or "
						//#if polish.vendor == Sony-Ericsson || polish.vendor == LG
						+ ISONHOMEPLMN + " (\"" + isOnHomePLMN + "\") "
						//#endif
						+"system property is null. Assuming we are not roaming.");
				
				result = false;
			}
	
			if (homeMCC != null && !homeMCC.equalsIgnoreCase(currentMCC)
					//#if polish.vendor == Sony-Ericsson || polish.vendor == LG
					&& "false".equals(isOnHomePLMN)
					//#endif
					) {
				result = true;
			}
		//#endif

		return result;
	}
    
    //#mdebug info
    protected void printDebugRequest(ServerRequest[] batchRequests)
    {
    	for (int i = 0; i < batchRequests.length; i++) {
    		if (null != batchRequests[i]) {
                switch (batchRequests[i].getMessageType()) {
                	case ServerRequest.RPG_CHAT_GET_PRESENCE_REQUEST:
                		System.out.println("___GET PRESENCE REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	case ServerRequest.RPG_CHAT_SEND_MSG_REQUEST:
                		System.out.println("___SEND CHAT MESSAGE REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	case ServerRequest.RPG_CHAT_SET_PRESENCE_REQUEST:
                		System.out.println("___SET PRESENCE REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	case ServerRequest.RPG_CREATE_CONVERSATION_REQUEST:
                		System.out.println("___CREATE CONV REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	case ServerRequest.RPG_INTERNAL_MSG_REQUEST:
                		System.out.println("___INTERNAL API REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	case ServerRequest.RPG_MSG_REQUEST:
                		System.out.println("___ EXTERNAL REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	case ServerRequest.RPG_STOP_CONVERSATION_REQUEST:
                		System.out.println("___STOP CONV REQUEST___ " + batchRequests[i].getRequestID());
                		break;
                	default:
                		System.out.println("OTHER REQUEST: " + batchRequests[i].getMessageType() + "  " + batchRequests[i].getRequestID());
                		break;
                }
    		}
    	}
    }
    //#enddebug
    
    /**
     * 
     * Called by the response queue whenever a new response is on the queue
     * 
     */
	public abstract void notifyOfNewItemInQueue();
}
