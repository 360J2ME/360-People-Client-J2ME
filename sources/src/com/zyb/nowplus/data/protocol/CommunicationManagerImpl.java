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
package com.zyb.nowplus.data.protocol;

import java.util.Hashtable;
import java.util.Timer;

import javax.microedition.rms.RecordStore;

import com.zyb.nowplus.MIDletContext;
import com.zyb.nowplus.data.protocol.request.RequestQueue;
import com.zyb.nowplus.data.protocol.request.RequestTimerTask;
import com.zyb.nowplus.data.protocol.request.ServerRequest;
import com.zyb.nowplus.data.protocol.response.ResponseDecoder;
import com.zyb.nowplus.data.protocol.response.ResponseListener;
import com.zyb.nowplus.data.protocol.transport.RPGConnection;
import com.zyb.nowplus.data.protocol.transport.http.HttpRPGConnection;
import com.zyb.nowplus.data.protocol.transport.tcp.TcpRPGConnection;
import com.zyb.nowplus.data.protocol.types.ChatObject;
import com.zyb.nowplus.data.protocol.types.Registration;
import com.zyb.nowplus.data.protocol.types.ServiceObject;
import com.zyb.nowplus.data.protocol.types.Update;
//#debug performancemonitor
import com.zyb.util.PerformanceMonitor;

//#if polish.blackberry
import com.zyb.util.BlackBerryConnectionSuffix;
//#endif


public class CommunicationManagerImpl implements CommunicationManager 
{
	private static final int SMS_PORT = 16999;
	private static final int ACTIVATION_SMS_TIMEOUT = 72;	// timeout in secs
    
	// this should be set according to the devices capabilities. Some devices
	// will only support 1 connection at a time. Set to false for these devices.
	//#ifdef polish.device.supports.multiple.connections:defined
	//#message support for multiple connections is ${polish.device.supports.multiple.connections}
	//#= public static final boolean DOES_DEVICE_SUPPORT_2_CONNECTIONS = ${polish.device.supports.multiple.connections};
	//#else
	//#message support for multiple connections is disabled by default
	public static final boolean DOES_DEVICE_SUPPORT_2_CONNECTIONS = false;
	//#endif
	
	private static final byte SESSION_ID_IDENTIFIER = 0, 
							  SESSION_SECRET_IDENTIFIER = 1,
							  USER_ID_IDENTIFIER = 2,
							  APP_INSTANCE_IDENTIFIER = 3;
	
	// the name of the store to write session values to if no other value was 
	// passed
	public static final String AUTH_STORE = "authStore93576502";
	
	public boolean isVodafoneCustomer;
	
	private final MIDletContext context;
	private RPGConnection outboundConn;
	private RPGConnection inboundConn;
	private AuthenticationListener authListener;
	private NetworkListener netListener;
	private RequestQueue queue;
	private ResponseListener responseListener;
	private ResponseDecoder responseDecoder;
	private Timer timeoutTimer;
	private boolean doStoreSession;
	private PushEngine pe;
	private long userID;		// userID for registration process
	private boolean didActivationCodeExpire;
	private HttpRPGConnection syncConnection;
	private boolean wereConnectionsStarted;
	private byte connectionMode;
	
    private boolean isNetworkDown;
    
	/**
	 * 
	 * Public constructor used for unit testing.
	 * 
	 * @param appKey The application key.
	 * @param appSecret The application secret.
	 * @param authListener The authentication listener called back in case of
	 * successful or failed authentications.
	 * @param inboundConn The incoming connection or null if only one connection is 
	 * to be used.
	 * @param outboundConn The outgoing connection. Must not be null.
	 * 
	 */
	public CommunicationManagerImpl(MIDletContext context, AuthenticationListener authListener,
									RPGConnection inboundConn,
									RPGConnection outboundConn) {
		
		this.context = context;
		
		this.inboundConn = inboundConn;
		this.outboundConn = outboundConn;
		
		if (null == inboundConn) {
			this.inboundConn = this.outboundConn;
		}
		
		this.authListener = authListener;
		queue = RequestQueue.getInstance();
		wereConnectionsStarted = false;
		isVodafoneCustomer = false;
		
		connectionMode = RPGConnection.UNKNOWN_MODE;
		
		//#if polish.blackberry
		BlackBerryConnectionSuffix.checkConnectionSuffixStr();
    	//#endif
	}
	
	public CommunicationManagerImpl(MIDletContext context) {
		//#debug debug
		System.out.println("Constructing protocol");
		
		this.context = context;
	}
	
	public void registerListeners(AuthenticationListener authListener,
			 NetworkListener netListener, ResponseListener responseListener)
	{	
		this.authListener = authListener;
		this.netListener = netListener;
		this.responseListener = responseListener;
		
		queue = RequestQueue.getInstance();
		
		// used for decoding and dispatching callbacks for the incoming responses
		responseDecoder = new ResponseDecoder(this, responseListener);
		if (!responseDecoder.isAlive()) {
			responseDecoder.start();
		}
		
		pe = new PushEngine(this);
		pe.startListening(SMS_PORT);
		
		wereConnectionsStarted = false;
		
		connectionMode = RPGConnection.UNKNOWN_MODE;
		
		//#if polish.blackberry
			BlackBerryConnectionSuffix.checkConnectionSuffixStr();
    	//#endif
	}
	
	public void startConnections() {
		//#debug info
		System.out.println("Starting connections");
		
		wereConnectionsStarted = true;
				
		// we didn't authenticate yet and will call back with
		// auth failed.
		if ((null == RPGConnection.sessionID) ||
				(null == RPGConnection.sessionSecret) ||
				(null == RPGConnection.userID)) {
			//#debug info
			System.out.println("No session values and user credentials." + " Attempting to read from RMS.");
			
			// reads the session values and populates RPGConnection session values
			readSessionValues();
			
			// if the values are still null we declare a fail
			if ((null == RPGConnection.sessionID) || 
					(null == RPGConnection.sessionSecret) || 
					(null == RPGConnection.userID)) {
				authListener.authenticationFailed(
							AuthenticationListener.AUTH_FAILED_UNKNOWN);
				return;
			}
		}

		setupConnections();
		triggerConnections();
	}

	/**
	 * 
	 * Detects if we can do TCP or HTTP and sets up either
	 * 1 connection (for TCP or devices that only support 
	 * one HTTP connection) or 2 connections for HTTP.
	 * 
	 * 
	 */
	private void setupConnections()
	{
		if (null != outboundConn) {	// Unit Test workaround!
			//#debug info
			System.out.println("RUNNING UNIT TEST CONNECTION!");
			
			if (null == inboundConn) {
				inboundConn = outboundConn;
			}
			
			return;
		}
		
		if (connectionMode == RPGConnection.TCP_MODE) {
			//#debug info
			System.out.println("RUNNING TCP CONNECTION!");

			outboundConn = new TcpRPGConnection("tcp", this, responseDecoder, authListener, 
								netListener, queue, 
								responseListener);
			inboundConn = outboundConn;
		}
		else if (connectionMode == RPGConnection.HTTP_MODE && DOES_DEVICE_SUPPORT_2_CONNECTIONS) {
			//#debug info
			System.out.println("RUNNING MULTI HTTP CONNECTION!");

			// setup: it must be at least 2: one outbound and one inbound

			//#debug info
			System.out.println("Starting in 2 connections mode");
			
			if (null == outboundConn || null == inboundConn) {
				outboundConn = new HttpRPGConnection("outbound", this, responseDecoder, authListener,
										netListener, responseListener, 
										RPGConnection.OUTBOUND);
				inboundConn = new HttpRPGConnection("inbound", this, responseDecoder, authListener,
										netListener, responseListener, 
										RPGConnection.INBOUND);
			}
		}
		else {	// only start one connection
			//#debug info
			System.out.println("RUNNING SINGLE HTTP CONNECTION!");
			
			//#debug debug
			System.out.println("Starting in single connection mode");

			if (null == outboundConn || null == inboundConn) {
				outboundConn = new HttpRPGConnection("out/inbound", this, responseDecoder, authListener, 
										netListener, responseListener, 
										RPGConnection.OUT_IN_BOUND);
				inboundConn = outboundConn;
			}
		}
	}
	
	/**
	 * 
	 * Triggers the connections and the response decoder.
	 * 
	 * 
	 */
	private void triggerConnections() {		
		if (connectionMode == RPGConnection.TCP_MODE) {
			if ((null != outboundConn) && (!outboundConn.isAlive())) {
				outboundConn.startConnection();
			}
		// setup: it must be at least 2: one outbound and one inbound
		} else if ((connectionMode == RPGConnection.HTTP_MODE) &&
			DOES_DEVICE_SUPPORT_2_CONNECTIONS) {
			if ((null != inboundConn) && (!inboundConn.isAlive())) {
				inboundConn.startConnection();
			}
			
			if ((null != outboundConn) && (!outboundConn.isAlive())) {
				outboundConn.startConnection();
			}
		} else {	// only start one connection
			if ((null != outboundConn) && (!outboundConn.isAlive())) {
				outboundConn.startConnection();
			}
		}
	}
	
	
	
	public void stopConnections(boolean finalStop) {
		
		// it must be at least 2: one for generic and one for chat
		if (DOES_DEVICE_SUPPORT_2_CONNECTIONS) {
			if (inboundConn != null) {
				inboundConn.stopConnection(false);
			}
			if (outboundConn != null) {
				outboundConn.stopConnection(finalStop);
			}
		} else {
			if (outboundConn != null) {
				outboundConn.stopConnection(finalStop);
			}
		}
		
		if (finalStop) {
			pe.stopListening();
		}
		
		//#debug closedown
		System.out.println("Stopped connections");
	}
	
	/**
	 * 
	 * Reads the session values from the RMS and populates them to the 
	 * RPGConnection. The sessionID-byte representation in the RecordStore starts
	 * with a 0 whereas the sessionSecret-record starts with a 1.
	 * 
	 */
	private void readSessionValues() {
		// grab session values from Storage, if none exist we need to auth
		
		RecordStore rs = null;
		byte[] value = null;
		
		try {
			rs = RecordStore.openRecordStore(AUTH_STORE, false);
					
			for (int i = 1; i < (rs.getNumRecords() + 1); i++) {
				value = rs.getRecord(i);
				
				if ((null != value) && (value.length > 0)) {	// sessionID
					byte[] temp = new byte[value.length - 1];
					System.arraycopy(value, 1, temp, 0, temp.length);	
	
					if (value[0] == SESSION_ID_IDENTIFIER) {
						RPGConnection.sessionID = new String(temp);
					} else if (value[0] == SESSION_SECRET_IDENTIFIER) {	// sessionSecret
						RPGConnection.sessionSecret = new String(temp);
					} else if (value[0] == USER_ID_IDENTIFIER) {	// userID
						RPGConnection.userID = new String(temp);
					} else if (value[0] == APP_INSTANCE_IDENTIFIER) {
						RPGConnection.appInstance = new String(temp);
					}
				}
			}
			
			//#debug info
			System.out.println("SessID " + RPGConnection.sessionID + "; SessSec " + RPGConnection.sessionSecret + "; UID " + RPGConnection.userID);
			
			rs.closeRecordStore();
		} catch (Exception e) {
			//#debug error
			System.out.println("Could not read from record store." + e);
		}
	}
	
	/**
	 * <p>Sets the session values for connecting to the backend. This method is used
	 * for internal purposes and should only be called by external code if the 
	 * developer knows what he is doing. The method stores the values on the RMS
	 * and retrieves them again once a new instance of CommunicationManager is called.</p>
	 * 
	 * <p>Session values can be reused until the backend decides to invalidate them.</p>
	 * 
	 * @param sessionID The session secret.
	 * @param sessionSecret The sessionID.
	 * @param userID The userID.
	 * @param appInstance The applications unique identifier.
	 * 
	 */
	public void setSessionValues(String sessionID, String sessionSecret,
								 String userID, String appInstance) {
		try {
			// delete RecordStore before
			RecordStore.deleteRecordStore(AUTH_STORE);
		} catch (Exception e) {
			//#debug error
			System.out.println("Could not delete RS." + e);
		}
		
		try {
			RecordStore rs = RecordStore.openRecordStore(AUTH_STORE, true);
			
			if (doStoreSession) {
				//#debug info
				System.out.println("<=================     Writing session values. SessID=" +
									sessionID + " SessSec=" + sessionSecret + 
									" UserID=" + userID);
				
				RPGConnection.sessionID = sessionID;
				RPGConnection.sessionSecret = sessionSecret;
				RPGConnection.userID = userID;
				RPGConnection.appInstance = appInstance;
				
				byte[] idStore = null;
				if (null != sessionID) {
					idStore = new byte[sessionID.length() + 1];
					System.arraycopy(sessionID.getBytes(), 0, idStore, 1, sessionID.length());
					idStore[0] = SESSION_ID_IDENTIFIER;
				}
				//#debug performancemonitor
				PerformanceMonitor.getInstance().registerThread(PerformanceMonitor.RMSWRITE);
				if (null != idStore) {
					rs.addRecord(idStore, 0, idStore.length);
				}
				byte[] secretStore = null;
				if (null != sessionSecret) {
					secretStore = new byte[sessionSecret.length() + 1];
					System.arraycopy(sessionSecret.getBytes(), 0, secretStore, 1, sessionSecret.length());
					secretStore[0] = SESSION_SECRET_IDENTIFIER;
				}
				if (null != secretStore) {
					rs.addRecord(secretStore, 0, secretStore.length);
				}
				byte[] userStore = null;
				if (null != userID) {
					userStore = new byte[userID.length() + 1];		
					System.arraycopy(userID.getBytes(), 0, userStore, 1, userID.length());
					userStore[0] = USER_ID_IDENTIFIER;
				}
				if (null != userStore) {
					rs.addRecord(userStore, 0, userStore.length);
				}
				//#debug performancemonitor
				PerformanceMonitor.getInstance().unregisterThread(PerformanceMonitor.RMSWRITE);
			}
			
			byte[] appInstanceStore = null;
			if (null != appInstance) {
				appInstanceStore = new byte[appInstance.length() + 1];
				System.arraycopy(appInstance.getBytes(), 0, appInstanceStore, 1, 
						appInstance.length());
				appInstanceStore[0] = APP_INSTANCE_IDENTIFIER;
			}
			if (null != appInstanceStore) {
				rs.addRecord(appInstanceStore, 0, appInstanceStore.length);
			}
			
			rs.closeRecordStore();
			
			//#debug info
			System.out.println("Successfully wrote session to RMS.");
		} catch(Exception e) {
			//#debug error
			System.out.println("Failed setting session values." + e);
		}
	}
	
	public String getDeviceId()
	{
		return context.getDeviceId();
	}
	
	public void authenticate(String username, String password, boolean doStoreSession, HttpRPGConnection authConnection)
	{
		//#debug info
		System.out.println("REAUTH CALLED: " + username + " " + password);

		readSessionValues();

		if (null == RPGConnection.appInstance) {
			RPGConnection.appInstance = ServerRequest.getAppInstance();
			setSessionValues(null, null, null, RPGConnection.appInstance);

			//#debug info
			System.out.println("Application Instance set 1st time: " + RPGConnection.appInstance);
		}
		
		if (null == username || null == password
			|| "".equals(username) || ("".equals(password))) {
			authListener.authenticationFailed(AuthenticationListener.AUTH_INVALID_CREDENTIALS);
			return;
		}

		this.doStoreSession = doStoreSession;
		syncConnection = null;

		if (null != authConnection) {	// unit test workaround :\
			syncConnection = authConnection;
		}
		else {
			syncConnection = new HttpRPGConnection("auth", this, responseDecoder, authListener, netListener, responseListener, RPGConnection.INBOUND);
		}
			
		syncConnection.setNeedsReauthentication(username, password);
		syncConnection.start();
	}

	public void authenticationSucceeded()
	{
		authListener.authenticationSucceeded();
	}
	
	public void authenticationFailed(int authCode)
	{
		authListener.authenticationFailed(authCode);
	}
	
	public void registerUser(
			String username, 
			String password, 
			String fullName,
			String birthdate, 
			String msisdn,
			boolean acceptedTermsAndConditions, 
			String countryCode,
			String userEmailAddr,
			String timezone, 
			String language, 
			int mobileOperatorID,
			int mobileModelID, 
			boolean subscribedToNewsletter,
			HttpRPGConnection signupConnection) 
	{
		boolean isDummyRegistration = false;
		
		//#if registration.code:defined
		//#message Dummy registration will call back with error code: ${registration.code}
		//#= authListener.registrationFailed(${registration.code});
		//#= isDummyRegistration = true;
		//#endif
		
		
		//#if registration.timedout:defined
		//#message Dummy registration timed out.
		//#= authListener.registrationFailed(AuthenticationListener.ACTIVATION_TIMED_OUT);
		//#= isDummyRegistration = true;
		//#endif
		
		//#if registration.succeeded:defined
		//#message Dummy registration succeeded.
		//#= authListener.registrationSucceeded(666);
		//#= isDummyRegistration = true;
		//#endif
		
		if (isDummyRegistration) {
			return;
		}
		
		didActivationCodeExpire = false;

		Registration reg = new Registration(username, password, fullName,
				birthdate, msisdn, acceptedTermsAndConditions, countryCode, userEmailAddr,
				timezone, language, mobileOperatorID, mobileModelID,
				subscribedToNewsletter);
		// #debug info
		System.out.println("Registering User: " + username);

		stopConnections(false);

		syncConnection = null;
		if (null != signupConnection) {	// unit test workaround :\
			syncConnection = signupConnection;
		} else {
			syncConnection = new HttpRPGConnection("register", this, responseDecoder, 
					authListener, netListener, responseListener, RPGConnection.INBOUND);
		}
			
		syncConnection.setNeedsUserRegistration(reg);
		syncConnection.start();
	}

	/**
	 * 
	 * Called back from the RPGConnection once the signupUser response
	 * comes in.
	 * 
	 * @param userID The userID as created on the backend.
	 * @param noSmsWillBeSent True if the backend already verified the number
	 * via the gateway.
	 * 
	 */
	public void registerUserRequestSuccessful(long userID, boolean noSmsWillBeSent) {
		isVodafoneCustomer = noSmsWillBeSent;
		
		// #debug info
		System.out.println("Register call successful " + userID);

		this.userID = userID;
		
		// true if no SMS will be sent because the GW was already
		// able to verify the phone number
		if (noSmsWillBeSent) {
			if (wereConnectionsStarted) {
				startConnections();
			}
			authListener.registrationSucceeded(userID);
		} else {
			// let's wait for the SMS an XX amount of seconds.
			// if it does not arrive we will send fail to the UI.
			timeoutTimer = null;
			timeoutTimer = new Timer();
			try {
				timeoutTimer.schedule(new RequestTimerTask(this),
						(ACTIVATION_SMS_TIMEOUT * 1000));			
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed scheduling registration timer!" + e);
			}
		}
	}

	/**
	 * 
	 * Called by the PushEngine once the registration was successful and the SMS
	 * activation code was sent via SMS and received by the device.
	 * 
	 * @param activationCode The activation code sent via SMS.
	 * 
	 */
	public void activationCodeReceived(String activationCode) {
		// if the timeout expired we should not activate the code anymore
		// as the UI (triggered by a registrationFailed(ACTIVATION_TIMED_OUT)
		// will already have given the user the choice to re-request the SMS.
		if (didActivationCodeExpire) {
			return;
		}

		// we deactivate the timer here so he does not call back with a
		// registration failed once the registration succeeded
		if (null != timeoutTimer) {
			//#debug debug
			System.out.println("Cancelling registration timer.");
			timeoutTimer.cancel();
			//#debug debug
			System.out.println("Timeout timer successfully cancelled.");
		}

		// #debug info
		System.out.println("Activation code received. Activating User.");

		Registration reg = new Registration(activationCode);

		stopConnections(false);
		syncConnection = null;
		syncConnection = new HttpRPGConnection("activate", this, responseDecoder,
				authListener, netListener, responseListener, RPGConnection.INBOUND);

		syncConnection.setNeedsUserActivation(reg, userID);
		syncConnection.start();
	}

	/**
	 * 
	 * Needed if the activation SMS after the registerUser never arrived and the
	 * activationTimerExpired() method was called. This method is triggered by
	 * the user after the first activation attempt went wrong.
	 * 
	 * @param username
	 *            The username of the registered user.
	 * @param msisdn
	 *            The MSISDN of the user to send the SMS to.
	 * 
	 */
	public void rerequestActivationCodeForUser(String username, String msisdn) {
		
		//#debug debug
		System.out.println("rerequestActivationCodeForUser:"+username+":"+msisdn);

		didActivationCodeExpire = false;

		Registration reg = new Registration(username, msisdn);

		stopConnections(false);
		syncConnection = null;
		syncConnection = new HttpRPGConnection("activationcode", this, responseDecoder,
				authListener, netListener, responseListener, RPGConnection.INBOUND);

		syncConnection.setNeedsReactivationSMS(reg);
		syncConnection.start();
	}

	/**
	 * 
	 * Called back by RPGConnection with the response that comes
	 * from the requestActivationCode-call.
	 * 
	 * @param noSmsWillBeSent True if no SMS will be sent due to
	 * the server to be able to recognize the MSISDN automatically.
	 * 
	 */
	public void activationCodeRequested(boolean noSmsWillBeSent) {
		isVodafoneCustomer = noSmsWillBeSent;
		
		// true if no SMS will be sent because the GW was already
		// able to verify the phone number
		if (noSmsWillBeSent) {
			if (wereConnectionsStarted) {
				startConnections();
			}
			authListener.registrationSucceeded(userID);
			return;
		} else {
			// let's wait for the SMS an XX amount of seconds.
			// if it does not arrive we will send fail to the UI.
			timeoutTimer = null;
			timeoutTimer = new Timer();
			try {
				timeoutTimer.schedule(new RequestTimerTask(this),
						(ACTIVATION_SMS_TIMEOUT * 1000));			
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed scheduling registration timer!");
			}
		}
	}
	
	/**
	 * 
	 * Called back if an error in the registration occured.
	 * 
	 * @param errorCode The error of the failed registration.
	 * 
	 */
	public void registrationFailed(int errorCode) {
		//#debug error
		System.out.println("ERROR: " + errorCode + "; (2=activation SMS timed out.)");

		if (errorCode == AuthenticationListener.ACTIVATION_TIMED_OUT) {
			didActivationCodeExpire = true;
		}

		if (wereConnectionsStarted) {
			startConnections();
		}
		
		authListener.registrationFailed(errorCode);
	}
	
	/**
	 * 
	 * Called back if the registration succeeded.
	 * 
	 * @param userID The user ID of the successful
	 * registrant.
	 * 
	 */
	public void registrationSucceeded(long userID) {
		if (wereConnectionsStarted) {
			startConnections();
		}
		
		authListener.registrationSucceeded(userID);
	}
	
	public void checkForUpdates(String versionNumber, HttpRPGConnection updateConnection) {
		//#debug info
		System.out.println("Checking for upgrade");
		
		Update update = new Update(versionNumber);
		syncConnection = null;

		if (null != updateConnection) {	// unit test workaround :\
			syncConnection = updateConnection;
		}
		else {
			syncConnection = new HttpRPGConnection("upgradecheck", this, responseDecoder,
				authListener, netListener, responseListener, RPGConnection.INBOUND);
		}
			
		syncConnection.setNeedsUpdateCheck(update);
		syncConnection.start();
	}
	
	/**
	 * 
	 * Called back by the RPGConnection if update is available.
	 * 
	 * @param update The available update.
	 */
	public void clientUpdateAvailable(Update update) {
		if (wereConnectionsStarted) {
			startConnections();
		}
		
		responseListener.clientUpdateAvailable(update);
	}
	
	/**
	 * 
	 * Called back by the RPGConnection if no 
	 * update is available.
	 * 
	 */
	public void clientIsUpToDate() {
		if (wereConnectionsStarted) {
			startConnections();
		}
		
		responseListener.clientIsUpToDate();
	}

	public void clientInitialized()
	{
		responseListener.clientInitialized();
	}

	public void requestMsisdn(HttpRPGConnection msisdnConn) {
		stopConnections(false);
		
		syncConnection = null;
		if (null != msisdnConn) {	// unit test workaround :\
			syncConnection = msisdnConn;
		} else {
			syncConnection = new HttpRPGConnection("msisdnrequest", this, responseDecoder,
				authListener, netListener, responseListener, RPGConnection.INBOUND);
		}
			
		syncConnection.setNeedsMsisdnRetrieval();
		syncConnection.start();
	}
	
	/**
	 * 
	 * Called when the get network info call returns from the server.
	 * 
	 * @param msisdn The retrieved MSISDN.
	 * 
	 */
	public void msisdnReceived(final String msisdn) {
		if (wereConnectionsStarted) {
			startConnections();
		}
		
		responseListener.msisdnReceived(msisdn);
	}
	
	public int sendRequest(byte verb, byte noun, ServiceObject[] items, 
			Hashtable params, byte priority) {
		
		return sendRequest(verb, noun, items, params, priority, false);
	}
	
	public int sendRequest(byte verb, byte noun, ServiceObject[] items, 
			Hashtable params, byte priority, boolean fireAndForget) {
		
		return queue.addRequest(priority, verb, noun, items, params, fireAndForget);	
	}
	
	public int sendRequest(byte verb, byte noun, ServiceObject[] items,
			Hashtable params, byte priority, int timeoutInSecs) {

		int result = queue.addRequest(priority, verb, noun, items, 
										params, false);

		// check if timeout is valid (> 0)
		if (timeoutInSecs > 0) {
			// now add to timer so we see when 
			if (null == timeoutTimer) {
				timeoutTimer = new Timer();
			}
		
			try {
				timeoutTimer.schedule(new RequestTimerTask(result, responseListener), timeoutInSecs * 1000);
			} catch (Exception e) {
				//#debug error
				System.out.println("Failed scheduling request timer!");
			}
		}

		return result;
	}
	
	public int[] sendMultipleRequests(byte[] verbs, byte[] nouns, 
			ServiceObject[][] items, Hashtable[] params, byte[] priorities) {
		int[] results = queue.addRequests(priorities, verbs, nouns, items, params,
						false);
	
		return results;
	}

	public int sendCreateConversationRequest(String network, String name) {
		ServiceObject[] servObjects = new ServiceObject[1];
		String[] networks = new String[] { network };
		String[] names = new String[] { name };
		
		servObjects[0] = new ChatObject(networks, names);
		
		int result = queue.addRequest(ServerRequest.HIGH_PRIORITY, ServerRequest.START,
										ServerRequest.CONVERSATION, servObjects,
										null, false);
		
		return result;
	}
	
	public int sendStopConversationRequest(String network, String name, String conversationID) {
		ServiceObject[] servObjects = new ServiceObject[1];		
		servObjects[0] = new ChatObject(conversationID);
		
		int result = queue.addRequest(ServerRequest.HIGH_PRIORITY, ServerRequest.STOP, 
										ServerRequest.CONVERSATION, servObjects, 
										null, true);
		
		return result;
	}
	
	public int sendChatMessage(String network, String name, String conversationID, String body) {
		String[] networks = new String[1];
		String[] names = new String[1];
		names[0] = name;
		networks[0] = network;
		
		ServiceObject[] servObjects = new ServiceObject[1];		
		servObjects[0] = new ChatObject(conversationID, networks, names, body);
		
		//#debug info
		System.out.println("Sending IM with conv ID: " + conversationID + " to " + name + " (" + network + ").");
		
		int result = queue.addRequest(ServerRequest.HIGH_PRIORITY, ServerRequest.SEND, 
										ServerRequest.CHAT_MESSAGES, servObjects, 
										null, true);
		
		return result;
	}

	public int getPresences() {
		int result = queue.addRequest(ServerRequest.MEDIUM_PRIORITY, 
										ServerRequest.GET, ServerRequest.PRESENCE, 
										null, null, false);
		
		return result;
	}

	public int loadBinary(String url) {
		int result = queue.addRequest(ServerRequest.HIGH_PRIORITY, url);
		
		return result;
	}
	
	public int[] loadBinaries(String[] urls) {
		if (null == urls) {
			return null;
		}
		int[] results = new int[urls.length];
		
		for (int i = 0; i < urls.length; i++) {
			results[i] = queue.addRequest(
					ServerRequest.HIGH_PRIORITY, urls[i]);
		}

		return results;
	}	

	public boolean cancelRequest(int requestID) {
		if(null != queue) {
			return queue.removeRequest(requestID);
		}
		
		return false;
	}
	
	//#mdebug error
	public String toString()
	{
		return "CommunicationManagerImpl[" 
			+ "]";
	}
	//#enddebug
    
    public boolean isRoaming() {
    	return RPGConnection.isRoaming();
    }
    
    public boolean getIsVodafoneCustomer() {
    	return isVodafoneCustomer;
    }


	public void autodetectConnection(boolean supportsTcp) {
		//#debug info
		System.out.println("Auto detecting connection type");
		
		if (supportsTcp) {
			connectionMode = RPGConnection.TCP_MODE;
		} else {
			connectionMode = RPGConnection.HTTP_MODE;			
		}
		
		autodetectConnectionFinished();
	}
	
	/**
	 * 
	 * Called back by the connection once the connection was detected.
	 * If TCP is not supported this method might take a long time until 
	 * it is called back.
	 * 
	 */
	private void autodetectConnectionFinished() {
		if (null != netListener) {
			netListener.autodetectConnectionFinished();
		}
	}
	
    /**
     * Sets the state of the network to either up (conneciton is alive)
     * or down (no connection possible).
     * 
     * @param isNetworkUp True if a network connection could be made.
     */
    public void setNetworkState(boolean isNetworkUp)
    {
		//#debug info
		System.out.println("******************* NETWORK STATE IS " + (isNetworkUp ? "UP" : "DOWN") + " *******************");
  
		isNetworkDown = !isNetworkUp;
    }
    
    /**
     * Returns true if the network is down or false if 
     * the network is up.
     * 
     * @return True if the network is down or false if 
     * the network is up.
     */
    public boolean isNetworkDown()
    {
    	return isNetworkDown;
    }
}
