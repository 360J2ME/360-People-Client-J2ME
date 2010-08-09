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
package com.zyb.nowplus.presentation.controller;

import java.util.Enumeration;
import java.util.Hashtable;

import com.zyb.nowplus.business.LaunchException;
import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.Message;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.ChatThreadForm;
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.util.HashUtil;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.util.Locale;

public class ChatController extends ContextController
{
	private static final Command cmdOpen = new Command(Locale.get("nowplus.client.java.command.open"), Command.SCREEN, 0);

	private static final Command cmdIgnore = new Command(Locale.get("nowplus.client.java.command.ignore"), Command.CANCEL, 0);
	
	private boolean initialStartupFinished = false;
	
	private Hashtable missedMessages;
	
	/**
	 * 
	 * @param model
	 * @param view
	 * @param controller
	 */
	public ChatController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		super(model, controller, history);
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#getContext()
	 */
	public byte getContext() 
	{
		return Event.Context.CHAT;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		if(context == getContext())
		{
			switch(event)
			{		
				case Event.Chat.OPEN: 
					if (data instanceof Channel)
					{
						Channel channel = (Channel) data;

						if (!channel.isOpen())
						{
							try {
								getModel().launch(channel);
							}
							catch (LaunchException le) {
								//#debug error
								System.out.println("Error launching chat channel: "+le.getMessage());
								
								//action requires app to close down
								if (LaunchException.TYPE_LAUNCH_POSTPONED == le.getType())
									getController().notifyEvent(Event.Context.APP,Event.App.CONFIRMED_EXIT);
							}
						}

						//show chat
						
						//#style base_form
				        ChatThreadForm ctf = new ChatThreadForm(getModel(),getController(), channel);
				        
				        getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, ctf);
					}						
					break;			

				case Event.Chat.CLOSE:
					//go back until no more chat sessions on stack
					while( history.isInStack(ChatThreadForm.class) )
					{
						//is current chat form instance?
						if(ChatThreadForm.class.isInstance( history.currentGlobalAndLocalDisplayableContainer().disp ))
						{
							ChatThreadForm aChat = (ChatThreadForm)history.currentGlobalAndLocalDisplayableContainer().disp;
							
							Channel channel = aChat.getChannel();
							
							//notify model chat closure
							if(null != channel)
								getModel().close( channel );
						}
						
						//back to next checkpoint
						history.back(true); //direct call to screenstack, do not use Controller
					}
					
					break;
					
				case Event.Chat.SEND: 
					if(data instanceof Object[])
					{
						Object[] objs = (Object[])data;
						
						try
						{
							//send new chat message						
							getModel().sendChatMessage((Channel)objs[0], (Message)objs[1]);
						} 
						catch (Exception e) 
						{
							//#debug error
							System.out.println("Error sending chat message: "+e.getMessage());
						}
					}					
					break;

				case Event.Chat.RECEIVED_MESSAGE:
					//is a chat session already active and does the channel of that thread match this one?
					if( (data instanceof Channel) &&
						(
							!(history.currentGlobalAndLocalDisplayableContainer().disp instanceof ChatThreadForm) ||
									( history.currentGlobalAndLocalDisplayableContainer().disp instanceof ChatThreadForm &&
								!HashUtil.equals(((ChatThreadForm)history.currentGlobalAndLocalDisplayableContainer().disp).getChannel().getConversationId() , ((Channel)data).getConversationId()) )
							
						)
					)
					{
						if(this.initialStartupFinished){
							showIncomingMessageNotification(data);
						}else{
							if(this.missedMessages == null){
								this.missedMessages = new Hashtable();
							}
							Channel channel = (Channel) data;
							missedMessages.put(channel, data);
						}
						
						//#if polish.blackberry
						final int timeVibrate=1000;
				    	new Thread()
				    	{
				    		public void run()
				    		{
				    			net.rim.device.api.system.Alert.startVibrate(timeVibrate);
				    		}
				    	}.start();
				    	
				    	//sound //FIXME change to better tune data from designer
				    	final short[] TUNE = new short[] { 415, 125, 0, 10};
				    	new Thread()
				    	{
				    		public void run()
				    		{
				    			int volume = net.rim.device.api.system.Alert.getVolume();
				    			
				    			//device in silence mode volume==0
				    			if(volume<=0)
				    				return;
				    			
				    			if (net.rim.device.api.system.Alert.isAudioSupported())
				    				net.rim.device.api.system.Alert.startAudio(TUNE, volume);
				    			else if (net.rim.device.api.system.Alert.isBuzzerSupported())
				    				net.rim.device.api.system.Alert.startBuzzer(TUNE, volume);
				    		}
				    	}.start();
				    	//#endif
					}
					break;
					
//				case Event.Chat.SWITCH_NATIVE:
//				case Event.Chat.SWITCH_MULTITAB:
//					if(null != currentChatSession)
//						currentChatSession.handleEvent(context, event, data);
//					break;	
				case Event.Chat.READY_FOR_INCOMING_MESSAGES:
					this.initialStartupFinished = true;
					if(this.missedMessages != null && this.missedMessages.size() > 0){
						Enumeration en = this.missedMessages.keys();
						while (en.hasMoreElements()) {
							Object msgData = en.nextElement();
							showIncomingMessageNotification(msgData);
						}
						this.missedMessages = null;
					}
					break;
				default: 
					break;
			}
		}
	}
	
	private void showIncomingMessageNotification(Object data)
	{
		//event to fire upon confirmation
		Event next = new Event(Event.Context.CHAT,Event.Chat.OPEN,data);

		Channel channel = (Channel) data;
		Message[] messages = channel.getMessages();
		Profile profile = channel.getProfile();
		String showName = profile.getFullName();

		if (showName == null || showName.length() == 0) {
			showName = channel.getName();
		}

		//#style chat_confirmation_form
		ConfirmationForm cf = new ConfirmationForm(
				getModel(), getController(),
				showName,
				messages[messages.length - 1].getText(),
				cmdOpen, cmdIgnore,
				next);
		
		//#if polish.blackberry
			cf.setShownCancelCmdButton(true);/*Command ignore showed*/
		//#endif

		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, cf);
	}
	
	//#mdebug error
	public String toString()
	{
		return "ChatController[]";
	}
    //#enddebug

}
