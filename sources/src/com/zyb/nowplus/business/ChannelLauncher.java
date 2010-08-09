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
package com.zyb.nowplus.business;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.ConnectionNotFoundException;

import com.zyb.nowplus.MIDletContext;
//#if !polish.remove_status_tab
import com.zyb.nowplus.business.domain.Activity;
//#endif
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

//#if polish.device.supports.nativesms == false || polish.blackberry
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;
import javax.wireless.messaging.Message;
//#endif

//#if polish.blackberry
import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.blackberry.api.phone.phonelogs.CallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneCallLog;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogListener;
import net.rim.blackberry.api.phone.phonelogs.PhoneLogs;
import net.rim.blackberry.api.sms.OutboundMessageListener;
//#endif

/**
 * Launches communication over channels like call, sms or email.
 */
class ChannelLauncher  
//#if polish.blackberry
implements Runnable, PhoneLogListener, OutboundMessageListener
//#endif
{
	private final MIDletContext context;
	private final ServiceBroker services;
	private Channel lastChannelRequested;
	private String lastPhoneCallNumber;
	//#if polish.blackberry
	private MessageConnection mc = null;
	//#endif
	
	/**
	 * Creates a channel launcher that uses the given context
	 * and chat manager to launch communication.
	 */
	public ChannelLauncher(MIDletContext context, ServiceBroker services)
	{
		//#debug debug
		System.out.println("Constructing channel launcher");
		
		this.context = context;
		this.services = services;
		//#if polish.blackberry
		PhoneLogs.addListener(this);
		
		if(this.mc == null){
			try{
				mc = (MessageConnection) Connector.open("sms://:0");
				mc.setMessageListener(this);
			}catch (IOException e){
				//#debug error
				System.out.println("Could open SMS connection: " + e);
			}
		}
		//#endif
	}
	
	public void stop(){
		//#if polish.blackberry
		PhoneLogs.removeListener(this);
		try{
			this.mc.setMessageListener(null);
			this.mc.close();
		}catch (Exception e) {
			//#debug error
			System.out.println("Could not close message listener " + e);
		}
		//#endif
	}
	
	/**
	 * Launches communication over the given channel.
	 */
	public void launch(Channel channel) throws LaunchException
	{
		switch (channel.getType())
		{
			case Channel.TYPE_CALL :
				//Cleans phone number of invalid chars
				request("tel:", TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, channel.getName()));
			break;
			
			case Channel.TYPE_SMS :
				//Cleans phone number of invalid chars
				request("smsto:", TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, channel.getName()));
			break;
			
			case Channel.TYPE_MMS :
				//Cleans phone number of invalid chars
				request("mmsto:", TextUtilities.stripNonValidChars(TextUtilities.VALID_PHONE_NUMBER_CHARS, channel.getName()));
			break;
				
			case Channel.TYPE_CHAT :
			services.getChatManager().startConversation(channel);
			break;
			
			case Channel.TYPE_EMAIL :
			request("mailto:", channel.getName());
			break;
			
			case Channel.TYPE_BROWSE :
			request("", channel.getUrl());
			break;
		}
		
		this.lastChannelRequested = channel;
	}
	
	/**
	 * Launches a website.
	 */
	public void launch(String url) throws LaunchException
	{
		request("", url);
	}
	
	public void launchCancelled()
	{
		// try to cancel out a previous platform request
		try
		{
			context.platformRequest(null);
		}
		catch (Exception e)
		{
			//#debug error
			System.out.println("Platform request \"null\" failed" + e);
		}
	}
	
	/**
	 * Closes communication over the given channel.
	 */
	public void close(Channel channel)
	{
		if (channel.getType() == Channel.TYPE_CHAT)
		{
			services.getChatManager().stopConversation(channel);
		}
	}
	
	//#if polish.device.supports.nativesms == false
	
	//#if polish.device.sendsmsdelay:defined
	/**
	 * This is a monitor code as a workaround for Huawei and Samsung phones which send sms doubled
	 */
	private boolean nowSendingSms = false;
	private static Object SMS_MONITOR = new Object();
	//#endif
		
	public void sendSms(final String number, final String message, final Object data) 
	{
		//#debug debug
		System.out.println("Send SMS to " + number + ":" + message);
	
		//#if polish.device.sendsmsdelay:defined
		synchronized (SMS_MONITOR)
		{
			if (nowSendingSms)
			{
				return;
			}
			nowSendingSms = true;
		}
		//#endif	

		services.fireEvent(Event.Context.SMS_EDITOR, Event.SmsEditor.SEND_STARTED, data);
	
		new Thread() {
			public void run() 
			{
				MessageConnection clientConn = null;
				try 
				{
					clientConn = (MessageConnection) Connector.open("sms://" + number);

					TextMessage textmessage = (TextMessage) clientConn.newMessage(MessageConnection.TEXT_MESSAGE);
					textmessage.setAddress("sms://" + number);
					textmessage.setPayloadText(message);
					
					clientConn.send(textmessage);

					//#if polish.device.sendsmsdelay:defined
					//# try {
					//#= Thread.sleep(${polish.device.sendsmsdelay});
					//# } catch (InterruptedException ignore) {
					//# }
					//#endif
					
		            //#debug debug
		        	System.out.println("Send SMS succeeded.");
		            
		            services.fireEvent(Event.Context.SMS_EDITOR, Event.SmsEditor.SEND_OK, data);
				} 
				catch (Exception e) 
				{
					//#debug error
					System.out.println("Failed to send SMS" + e);
				
					services.fireEvent(Event.Context.SMS_EDITOR, Event.SmsEditor.SEND_FAILED, data);
				} 
				finally
				{
					if (clientConn != null)
					{
						try 
						{
							clientConn.close();
						}
						catch (IOException e) 
						{
							//Client connection is already closed 
						}
					}
				
					//#if polish.device.sendsmsdelay:defined
        			synchronized (SMS_MONITOR) 
        			{
        				nowSendingSms = false;
        			}
        			//#endif
				}
			}}.start();
	}
	//#endif
	
	private void request(String protocol, String name) throws LaunchException
	{
		//#if polish.blackberry
		this.protocol=protocol;
		channelName=name;
		
		new Thread(this).start();
		//#else
		try
		{
			if (context.platformRequest(protocol + name))
			{
				throw new LaunchException(LaunchException.TYPE_LAUNCH_POSTPONED); 
			}
		}
		catch (ConnectionNotFoundException e)
		{
			//#debug error
			System.out.println("Failed to open " + protocol + name + " " + e);
			
			throw new LaunchException(LaunchException.TYPE_LAUNCH_FAILED);
		}
		catch (SecurityException e)
		{			
			//#debug error
			System.out.println("No permission to open " + protocol + name + " " + e);
			
			throw new LaunchException(LaunchException.TYPE_LAUNCH_FAILED);
		}
		//#endif
	}
	
	//#mdebug error
	public String toString()
	{
		return "ChannelLauncher[" 
			+ "]";
	}
	//#enddebug
	
	//#if polish.blackberry
	
	/** recorded channel type*/
	private String protocol;
	/** recorded channel request name, e.g. receiver, url...*/
	private String channelName;
	
	public void run()
	{
		if("mailto:".equals(protocol))
			invokeEMail(channelName,"","");
		else if("smsto:".equals(protocol))
			invokeSMS(channelName,"");
		else if("mmsto:".equals(protocol))
			invokeMMS(channelName);
		else if("tel:".equals(protocol))
			invokePhoneCall(channelName);
		else if("".equals(protocol))
			invokeBrowser(channelName);
	}
	
	/**
	 * Make a call
	 * 
	 * @param phoneNumber
	 *            receiver of call
	 */
	private void invokePhoneCall(final String phoneNumber) 
	{
		//TODO check whether is calling himself 
		if(phoneNumber==null||phoneNumber.equals(""))
			    throw new java.lang.NullPointerException("Invalid method parameter for making a call.");
		
		PhoneArguments call = new PhoneArguments(PhoneArguments.ARG_CALL,
				phoneNumber);
		Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, call);
		this.lastPhoneCallNumber = phoneNumber;
	}
	
	
	/**
	 * Send sms via phone number
	 * 
	 * @param receiver
	 *            mobile number of receiver
	 * @param smsContent
	 *            string content of sending sms
	 */
	private void invokeSMS(final String receiver, String smsContent) 
	{
		//TODO whether check receiver number 
//		if (receiver == null || receiver.equals(""))
//			throw new java.lang.NullPointerException("Invalid method parameter for creating sms.");

		MessageConnection mc = null;
		try 
		{
			mc = (MessageConnection) Connector.open("sms://");
		} 
		catch (IOException e) 
		{
			System.out.println(this + " exception: " + e.toString());
		}
		
		TextMessage textMessage = (TextMessage) mc.newMessage(MessageConnection.TEXT_MESSAGE);
		
		textMessage.setAddress("sms://" + receiver);
		
		if (smsContent != null && !smsContent.equals(""))
			textMessage.setPayloadText(smsContent);
		
		this.lastPhoneCallNumber = receiver;
		Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES,
				new MessageArguments(textMessage));
		
	}
	
	/**
	 * Send mms via phone number
	 * 
	 * @param receiver
	 *            mobile number of receiver
	 */
	private void invokeMMS(final String receiver) 
	{
		if (receiver == null || receiver.equals(""))
			throw new java.lang.NullPointerException("Invalid method parameter for creating mms.");

		MessageConnection mc = null;
		try 
		{
			mc = (MessageConnection) Connector.open("mms://");
		} 
		catch (IOException e) 
		{
			System.out.println(this + " exception: " + e.toString());
		}
		
		TextMessage textMessage = (TextMessage) mc.newMessage(MessageConnection.TEXT_MESSAGE);
		
		textMessage.setAddress("mms://" + receiver);
		
		this.lastPhoneCallNumber = receiver;
		Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES,
				new MessageArguments(textMessage));
	}
	
	/**
	 * Create email
	 * 
	 * @param receiver
	 *            email address of receiver
	 * @param subject
	 *            string title of creating email
	 * @param body
	 *            string content of creating email
	 */
	private void invokeEMail(final String receiver, String subject, String body) 
	{
		if (receiver == null || receiver.equals(""))
			throw new java.lang.NullPointerException("Invalid method parameter for creating email.");

		MessageArguments messageArguments = new MessageArguments(
				MessageArguments.ARG_NEW, receiver, subject, body);
		
		Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, messageArguments);
	}
	
	/**
	 * Open the Internet browser with url
	 * 
	 * @param url
	 *            website URL
	 */
	private void invokeBrowser(final String url) 
	{
		BrowserSession browserSession = Browser.getDefaultSession();
		browserSession.displayPage(url);
	}
	
	public void callLogAdded(CallLog callLog) {
		if(callLog.getStatus() != CallLog.STATUS_NORMAL){
			return;
		}
		if(callLog instanceof PhoneCallLog){
			PhoneCallLog pcl = (PhoneCallLog) callLog;
			String number = pcl.getParticipant().getNumber();
			if(pcl.getType() == PhoneCallLog.TYPE_PLACED_CALL && number.equals(this.lastPhoneCallNumber)){
				long time = System.currentTimeMillis();
				ExternalNetwork network = services.getChatManager().findNetworkById(this.lastChannelRequested.getNetworkId());
				ManagedProfile[] profiles = new ManagedProfile[]{(ManagedProfile) this.lastChannelRequested.getProfile()};
				Activity activity = new Activity(
						-1, Activity.TYPE_CALL_DIALED, "", number, profiles, null, time, network
				);
				
				services.fireEvent(Event.Context.ACTIVITY, Event.Activities.OUTGOING_PHONECALL, activity);
			}
		}
	}

	public void callLogRemoved(CallLog arg0) {
		//nothing todo	
	}

	public void callLogUpdated(CallLog arg0, CallLog arg1) {
		//nothing todo
	}

	public void reset() {
		//nothing todo
	}

	public void notifyOutgoingMessage(Message msg) {
		TextMessage tMsg = (TextMessage) msg;
		String receiver = tMsg.getAddress();
		if(receiver.indexOf("sms://") != -1 || receiver.indexOf("mms://") != -1){
			receiver = receiver.substring(6);
		}
		if(receiver.equals(this.lastPhoneCallNumber)){
			//update timeline chat log
			long time = System.currentTimeMillis();
			ManagedProfile[] profiles = new ManagedProfile[]{(ManagedProfile) this.lastChannelRequested.getProfile()};
			ExternalNetwork network = services.getChatManager().findNetworkById(this.lastChannelRequested.getNetworkId());
			Activity activity = new Activity(
					-1, Activity.TYPE_MESSAGE_SMS_SENT, "", tMsg.getPayloadText(), profiles, null, time, network
			);
			
			services.fireEvent(Event.Context.ACTIVITY, Event.Activities.OUTGOING_SMS, activity);
		}
	}

	
	public void notifyIncomingMessage(MessageConnection arg0) {
		//nothing to do
	}

	//#endif
}
