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
package com.zyb.nowplus.presentation.view.forms;

import javax.microedition.lcdui.Graphics;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
//#endif

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.ManagedProfile;
import com.zyb.nowplus.business.domain.Message;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.ChatMessageItem;
import com.zyb.nowplus.presentation.view.items.ChatTextFieldItem;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.HashUtil;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventListener;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.DeviceControl;
import de.enough.polish.util.Locale;

/**
 * Form for displaying a Chat thread.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ChatThreadForm extends BaseFramedForm implements EventListener
{
	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;
	
	/**
	 * Other chat participant
	 */
	protected Profile other;
	
	/**
	 * Channel to use
	 */
	protected Channel channel;
	
	/**
	 * visual item for other chat participant
	 */
	protected ContactSummarizedItem otherProfileItem;	
	
	/**
	 * Handle to emoticon container
	 */
	protected Container emoticons;
	
	/**
	 * Handle to emoticon Form
	 */
	protected Form emoticonForm;
	
	/**
	 * Custom text input field
	 */
	protected ChatTextFieldItem inputItem;
	
	/**
	 * this is only for BB touch important
	 */
	public static boolean reInitTextfield = true;
	
	public final static Command cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
	public final static Command cmdSelect = new Command(Locale.get("polish.command.select"), Command.OK, 0);
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 */
	public ChatThreadForm(Model model, Controller controller, Channel channel)
	{
		this(model, controller, channel, null);
	}
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param style
	 */
	public ChatThreadForm(Model model, Controller controller, Channel channel, Style style) 
	{
		super(model, controller, null, style);
		
		//#if not polish.blackberry
			this.addCommand(cmdBack);
			this.addCommand(cmdOptions);
		//#endif
		
		this.channel = channel;
		
		this.other = this.channel.getProfile();
		
		/*
		//test messages
		Message[] mgs = new Message[]
		                             {
				new Message("a",1,true),
				new Message("b",2,false),
				new Message("c",3,true),
				new Message("d",4,false),
				new Message("e",5,true),
				new Message("a",1,true),
				new Message("b",2,false),
				new Message("c",3,true),
				new Message("d",4,false),
				new Message("e",5,true),
				new Message("f",6,false)
		                             };
		
		this.loadChatThread(mgs, false);
		*/
		
		//load chat history items if any
		this.loadChatThread(channel);
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createStaticItems()
	 */
    protected Item[] createStaticItems() 
    {
    	if(null == otherProfileItem && null != other)
		{
			byte presenceMode ;
			presenceMode = ContactSummarizedItem.PRESENCE_MODE_ONLINE_GRAY;
			if(channel.getPresence() == Channel.PRESENCE_OFFLINE ||
					channel.getPresence() == Channel.PRESENCE_INVISIBLE){
				presenceMode = ContactSummarizedItem.PRESENCE_MODE_OFFLINE_ONLY;
			}
    		//#style chat_profile_item
			otherProfileItem = UiFactory.createUserProfileItem(other, ContactSummarizedItem.NETWORK_ICON_MODE_ALL, presenceMode, null);
			otherProfileItem.setAppearanceMode(Item.PLAIN);
			
			//set custom network image according to channel network
			if(null != this.channel)
				otherProfileItem.setNetworkIcon(this.channel.getNetworkId());
			
		}
		
    	Item[] result;
    	
    	//#if polish.blackberry.isTouchBuild == false
			if(null == titleitem) {
				titleitem = new TitleBarItem( Locale.get("nowplus.client.java.chat.thread.title"),getModel() );
			}
			
			result = new Item[]{titleitem,otherProfileItem}; 
		//#else 
			result = new Item[]{otherProfileItem};
		//#endif
			
		return result; 
    }
    
    /*
     * (non-Javadoc)
     * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createTopItem()
     */
    protected Item createTopItem() 
    {
    	return null;
    }

    /*
     * (non-Javadoc)
     * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createBottomItem()
     */
	protected Item createBottomItem() 
	{
		//#style chat_inputfield
		this.inputItem = new ChatTextFieldItem(this);

		//#if using.native.textfield:defined
		// For devices that do not have clear key we use native mode always. 
		this.inputItem.setInputMode(TextField.MODE_NATIVE);
		//#endif
		
		//#if polish.blackberry
		this.inputItem.setDefaultCommand(ChatTextFieldItem.cmdSend);
		//#endif
		
		this.inputItem.setSubmitButtonActive(this.channel.isLoggedIn());

		return this.inputItem;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createCssSelector()
	 */
	protected String createCssSelector() 
	{
		return null;
	}
	
	
	public void showNotify(){
		super.showNotify();
		//fix for PBLA PBLA-846
		//#if polish.blackberry.isTouchBuild
		this.bottomFrame.requestFullInit();
		//#endif
	}
	
	
	

	public void hideNotify(){
		//remove focus on hide this screen
		//#if polish.blackberry
		if(this.inputItem != null){
			this.inputItem.defocus(this.inputItem.getStyle());
		}
		//#endif
		super.hideNotify();
		
	}
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#initialize()
	 */
	protected void init( int width, int height)
	{
		super.init( width, height);
		
		//ensure that newest message is focused upon entering chat
		
		//focus center
		this.setActiveFrame(0);
		
		//scroll to last
		scrollToLastMessage();
		
		if(reInitTextfield){
			//focus textfield on chat form entry
			this.setActiveFrame(Graphics.BOTTOM);
			
			//fix for PBLA PBLA-846
			//#if polish.blackberry.isTouchBuild
			if(this.inputItem.isTextfieldFocused())
				this.bottomFrame.requestFullInit();
			//#endif
			this.currentlyActiveContainer.focusChild(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#getContext()
	 */
	public byte getContext() 
	{
		return Event.Context.CHAT;
	}
	
	/**
	 * Creates and adds ChatMessageItems to this form based on chat history.
	 * 
	 * @param channel chat channel to be used
	 * @param index starting index of message array
	 */
	private void loadChatThread(Channel channel)
	{
		loadChatThread(channel, false);
	}
	
	/**
	 * Creates and adds ChatMessageItems to this form based on chat history.
	 * <p>
	 * Newly added messages can optionally be marked as new
	 * 
	 * @param channel chat channel to be used
	 * @param index starting index of message array
	 * @param setLastAsNew if last element should be set as new
	 */
	private void loadChatThread(Channel channel, boolean setLastAsNew)
	{
		Message[] thread = channel.getMessages();
		
		loadChatThread(thread, setLastAsNew);
	}
	
	private void loadChatThread(Message[] thread, boolean setLastAsNew)
	{
		if(null == thread || thread.length == 0)
			return;
		
		//Remove redundant items if there are too many
		while(thread.length < this.container.size())
		{
			this.container.remove(this.container.size()-1);
		}
			
		//update local session elements
		Message msg = null;
		for(int i = 0; i < thread.length; ++i)
		{
			msg = thread[i];
			
			if( i < this.container.size())
			{
				//update existing 
				ChatMessageItem cmi = (ChatMessageItem)this.container.get(i);
				if(msg.isFromMe())
					cmi.updateMessage(Locale.get("nowplus.client.java.chat.thread.you"), msg, this.channel.getNetworkId());
				else
					cmi.updateMessage(other.getFullName(), msg, this.channel.getNetworkId());
			}
			else
			{
				//add new
				if(null != msg)
				{
					if(msg.isFromMe())
						addSelfMessageItem(msg);
					else
						addOtherMessageItem(msg);
				}
			}
		}
		
		//mark last item if any
		if(setLastAsNew && null != msg && this.container.size() > 1)
		{
			//mark last item as newly received
			if(!msg.isFromMe())
				((ChatMessageItem)(this.container.get(container.size()-1))).setJustRecieved(true);
		}
		
		//is textfield currently focused?
		if(this.bottomFrame == this.currentlyActiveContainer)
			scrollToLastMessage();
		
		//#if polish.blackberry
		if(this.inputItem != null){
			this.inputItem.focusTextfield();
		}
		//#endif
	}
	
	/**
	 * Creates and adds a new chat message authored by user
	 * 
	 * @param timeStamp time of message creation
	 * @param mgs message text
	 * @return ChatMessageItem
	 */
	private Item addOtherMessageItem(Message msg)
	{
		Item itm = null;
		
		if(null != msg)
		{
			//new chat item
			itm = new ChatMessageItem(
					other.getUserVisibleName(),
					msg, this.channel.getNetworkId());
					
			//add to form
			this.add(itm);
		}
		
		return itm;
	}
	
	/**
	 * Creates and adds a new chat message authored by friend/other/contact
	 * 
	 * @param timeStamp time of message creation
	 * @param mgs message text
	 * @return ChatMessageItem
	 */
	private Item addSelfMessageItem(Message msg)
	{
		Item itm = null;
		
		if(null != msg)
		{
			//new chat item
			itm = new ChatMessageItem(
					Locale.get("nowplus.client.java.chat.thread.you"),
					msg, this.channel.getNetworkId());
			
			//add to form
			this.add(itm);
		}
		
		return itm;
	}
	
	//#if !polish.blackberry.isTouchBuild
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction)
	{
		boolean result = false;
		
		//#if using.native.textfield:defined 
		if (gameAction == Canvas.FIRE && keyCode != Canvas.KEY_NUM5 && inputItem != null
				&& inputItem.getString() != null
				&& inputItem.getString().length() > 0) {
			return false;
		}
		//#endif
		
		//This nasty hack is NOT ok, cycling should be handled using polish.FramedForm.allowCycling in variables section of build.xml
//		//prevent wrapping when in top
//		if( (Canvas.UP == gameAction && Canvas.KEY_NUM2 != keyCode) &&
//				( ( this.topItem != null && getCurrentItem() == this.topItem) || this.topItem == null) && 
//				(this.container.size() > 0 && getCurrentItem() == this.container.get(0) ) ) 
//			result = true;
		
		//should key handling be passed to super?
		if( !result && !isKeyPadKey(keyCode, gameAction) )
			result = super.handleKeyPressed(keyCode, gameAction);		
		
		//was key already handled?
		if(!result)
		{
			/* NOTE: Do NOT use list index this.container.focusedIndex for input
			handling. It will change as the list is being updated and is hence 
			not suited as a control flag */ 
			
			//focus bottomItem by press of keypad
			if( !result && this.bottomItem != null && getCurrentItem() != this.bottomItem && isKeyPadKey(keyCode, gameAction) )
			{
				//activate bottom item
				this.setActiveFrame(Graphics.BOTTOM);
			}
			
			//pass on control to 'bottomItem' if focused
			if( !result && this.bottomItem != null && getCurrentItem() == this.bottomItem)
				if(UiAccess.handleKeyPressed(this.bottomItem, keyCode, gameAction))
					result = true;			
			
			//This nasty hack is NOT ok, cycling should be handled using polish.FramedForm.allowCycling in variables section of build.xml
//			//prevent wrapping when in bottom of contact list
//			if( !result && (this.container.size() > 0 && getCurrentItem() == this.container.get(this.container.size() - 1)) &&
//					(Canvas.DOWN == gameAction && Canvas.KEY_NUM8 != keyCode))
//			{
//				if(UiAccess.handleKeyPressed(this.getCurrentItem(), keyCode, gameAction))
//					result = true;
//			}
			
			//#mdebug debug
//			//switch input mode if textfield focused and '#' key press
//			if(getCurrentItem() == inputItem && Canvas.KEY_POUND == keyCode)
//			{
//				//switch input mode of textfield and change inputmode graphics
//				inputItem.switchInputMode();
//				result = true;
//			}
			//#enddebug
				
		}
		
		return result;
	}
	//#endif
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data)
	{
		if(context == this.getContext())
		{
			switch(event)
			{
				case Event.Chat.RECEIVED_MESSAGE: 
					if(null != data && data instanceof Channel)
					{
						Channel ch = (Channel) data;
						
						if(HashUtil.equals(this.channel.getConversationId(), ch.getConversationId()))
						{
							//load new items and label last as 'newly received'
							loadChatThread(ch, true);
						}
					}
					break;	
					
					//#if !polish.BlackBerry
				case Event.Chat.SWITCH_NATIVE:
					inputItem.setInputMode(TextField.MODE_NATIVE);
					break;
					
				case Event.Chat.SWITCH_MULTITAB:
					inputItem.setInputMode(TextField.MODE_FIRST_UPPERCASE);					
					break;	
					//#endif
				default:
					break;
			}
		}
		else
		if(context == Event.Context.CONTACTS)
		{
			if(event == Event.Contacts.UPDATE)
			{
				//is contact id same?
				if(null != other && null != data && data instanceof Long &&
						other instanceof ManagedProfile && ((Long)data).longValue() == ((ManagedProfile)other).getCabId())
				{
					//update profile item
					this.otherProfileItem.setContact(this.other);
					
					//ensure custom network image according to channel network
					if(null != this.channel)
						otherProfileItem.setNetworkIcon(this.channel.getNetworkId());
					
					this.inputItem.setSubmitButtonActive(channel.isLoggedIn());
				}
			}
		}
        else
            super.handleEvent(context, event, data);

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable disp)
	{
		//#debug debug
		System.out.println(c+"/"+disp);
		
		if(c == cmdOptions)
		{
			//launch contextual menu
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, new Object[]{this.other, new Integer(inputItem.getInputMode())} );
		}
		if(c == cmdBack)
		{
			if(disp == this)
			{
				//close chat
				getController().notifyEvent(Event.Context.CHAT, Event.Chat.CLOSE, this.channel );
			}
			else
			if(disp == emoticonForm)
			{
				//go back from emoticons
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK);
			}	
		}		
		else
			super.commandAction(c, disp);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command c, Item item) 
	{
		if(c == ChatTextFieldItem.cmdSend)
		{
			// Don't send message if user is offline.
			if (!this.channel.isLoggedIn()) {
				return;
			}

			String newMsg = inputItem.getString();
			
			if(null != newMsg && newMsg.length() != 0)
			{
				long timeStamp = System.currentTimeMillis();
				Message msg = new Message(newMsg, timeStamp, true);
				
				//add new item to self immediately
				addSelfMessageItem(msg); //Gives redundand entries in list
				
				//pass new message to controller
				getController().notifyEvent(Event.Context.CHAT, Event.Chat.SEND, new Object[]{this.channel, msg} );
				
				//clear textfield
				inputItem.clear();
				
				//make sure last is focused before retaining focus on textfield
				scrollToLastMessage();
				
				//focus textfield
				this.setActiveFrame(Graphics.BOTTOM);
				this.currentlyActiveContainer.focusChild(0);
				Item currentItem = getCurrentItem();
				if(currentItem instanceof ChatTextFieldItem){
					((ChatTextFieldItem) currentItem).focusTextfield();
				}
			}			
		}
		else
		if(c == ChatTextFieldItem.cmdEmoticons)
		{
		
			//#if polish.blackberry.isTouchBuild
			DeviceControl.hideSoftKeyboard();
			//#endif
			Form f = createEmoticonForm();
			
			//#if !polish.blackberry.isTouchBuild
			//focus textfield
			inputItem.focusTextfield();
			//#endif
			
			//set next
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, f);
		}
		else
		if(c == cmdSelect)
		{
			if(null != item && item instanceof IconItem)
			{
				int emoNum = emoticons.getFocusedIndex();
				
				//set text of text field according to choosen emoticons
				inputItem.setString(inputItem.getString()+emoticonIndexToString(emoNum));
				
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK);				
			}
		}
		else
			super.commandAction(c, item);
	}
	
	/**
	 * Scroll the main frame to last message item if any without focussing this item.
	 */
	private void scrollToLastMessage()
	{
		//move to latest message
		if(this.container.size() > 0)
		{
			Item itm = this.container.get(this.container.size()-1);
			
//			this.container.focusChild(this.container.size()-1);
			this.container.scroll(DOWN, itm, true);
		}
	}

	/**
	 * Returns a string representation of an emoticon based on an index in the
	 * emoticon container. 
	 * <p>
	 * This is hardcoded according to content and order of emoticons as dictated in 
	 * "/resources/properties/smileys.properties"
	 * 
	 * @param index
	 * @return
	 */
	private String emoticonIndexToString(int index)
	{
		switch (index) {
		case 0:
			return ":-)";
		case 1:
			return ">:-(";
		case 2:
			return "(adore)";
		case 3:
			return "%-}";
		case 4:
			return ":@";
		case 5:
			return "(bad)";
		case 6:
			return "<:o)";
		case 7:
			return "8-)";
		case 8:
			return ";-)";
		case 9:
			return "(woo)";
		case 10:
			return ":o";
		case 11:
			return ":-X";
		case 12:
			return ":'(";
		case 13:
			return ":S";
		case 14:
			return "(furious)";
		case 15:
			return "^-^";
		case 16:
			return "(hysteric)";
		case 17:
			return ":P";
		case 18:
			return ":-|";
		case 19:
			return "(worn)";
		case 20:
			return ":-h";
		case 21:
			return "8-|";
		case 22:
			return "(struggle)";
		case 23:
			return "O:-)";
		case 24:
			return "(K)";
		case 25:
			return "(kissed)";
		case 26:
			return ":-D";
		case 27:
			return ":(";
		case 28:
			return "|-)";
		case 29:
			return "(sweat)";
		case 30:
			return "(angel)";
		default:
			return "";
		}
	}
	
	
	
	private Form createEmoticonForm()
	{
		//#if not polish.blackberry
		
			//#style chat_emoticon_form
			emoticonForm =  new Form(null);
		
		//#else
			
			//#style chat_emoticon_form
			emoticonForm =  new Form(null)
			{
				//overrides
				/**
				 * Handles key events.
				 * 
				 * WARNING: When this method should be overwritten, one need
				 * to ensure that super.keyPressed( int ) is called!
				 * 
				 * @param keyCode The code of the pressed key
				 */
				public void keyPressed(int keyCode) 
				{
					if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
						getCommandListener().commandAction(cmdBack, this);
					else
						super.keyPressed(keyCode);
				}
				//#if polish.blackberry.isTouchBuild		
				public void showNotify(){
					reInitTextfield = false;
					if(ChatThreadForm.this.inputItem.isTextfieldFocused())
						ChatThreadForm.this.inputItem.defocusTextfield();
					ChatThreadForm.this.setActiveFrame(0);
					super.showNotify();
				}
				public void hideNotify(){
					super.hideNotify();
					reInitTextfield = true;
					DeviceControl.showSoftKeyboard();
					ChatThreadForm.this.setActiveFrame(Graphics.BOTTOM);
					ChatThreadForm.this.inputItem.focusTextfield();
				}
				//#endif
			};
			
		//#endif
		
		//#style chat_emoticon_container
		Container container = new Container(true);
		
		IconItem emoticon;
		
		emoticon = createEmoticonItem("/chat_emoticons_standardsmiley_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_annoyed_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_adore_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_amused_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_angry_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_badly_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_celebrating_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_cool_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_winkyface_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_woo_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_surprising_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_stop_25x25.png");
		container.add(emoticon);	
		
		emoticon = createEmoticonItem("/chat_emoticons_cryingface_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_dizzy_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_furious_25x25.png");
		container.add(emoticon);		
		
		emoticon = createEmoticonItem("/chat_emoticons_giggling_25x25.png");
		container.add(emoticon);			
		
		emoticon = createEmoticonItem("/chat_emoticons_hysterical_25x25.png");
		container.add(emoticon);			
		
		emoticon = createEmoticonItem("/chat_emoticons_tongueout_25x25.png");
		container.add(emoticon);	
		
		emoticon = createEmoticonItem("/chat_emoticons_indiffernet_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_worn_out_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_waving_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_study_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_struggle_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_innocent_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_kissing_25x25.png");
		container.add(emoticon);		
		
		emoticon = createEmoticonItem("/chat_emoticons_kissed_25x25.png");
		container.add(emoticon);	
		
		emoticon = createEmoticonItem("/chat_emoticons_rellyhappy_25x25.png");
		container.add(emoticon);
		
		emoticon = createEmoticonItem("/chat_emoticons_sadface_25x25.png");
		container.add(emoticon);	
		
		emoticon = createEmoticonItem("/chat_emoticons_sleep_25x25.png");
		container.add(emoticon);		
		
		emoticon = createEmoticonItem("/chat_emoticons_sweat_25x25.png");
		container.add(emoticon);	
		
		emoticon = createEmoticonItem("/chat_emoticons_sweet_angel_25x25.png");
		container.add(emoticon);				
		
		container.setItemCommandListener(this);

		emoticonForm.append(container);
		
		//#if not polish.blackberry
			emoticonForm.addCommand(cmdBack);
		//#endif
		
		emoticonForm.setCommandListener(this);
		
		//fetch handle to emoticon container
		emoticons = container;
		
		return emoticonForm;
	}
	
	private IconItem createEmoticonItem(String imgPath)
	{
		try
		{
			
			//#style chat_emoticon_item
			IconItem emoticon = new IconItem(null,null);
			emoticon.setImage(StyleSheet.getImage(imgPath, null, true));
			
			//#if polish.blackberry
			emoticon.setDefaultCommand(cmdSelect);
			//#else
			emoticon.addCommand(cmdSelect);
			//#endif
			
			emoticon.setItemCommandListener(this);
			return emoticon;
		} 
		catch (Exception e)
		{
			//#debug error
			System.out.println("Error loading emoticon");
		}
		
		return null;
	}
	
	/**
	 * Returns the channel associated with this chat thread
	 * 
	 * @return
	 */
	public Channel getChannel()
	{
		return channel;
	}
	
	//#if polish.blackberry
	//overrides
	/**
	 * Handles key events.
	 * 
	 * WARNING: When this method should be overwritten, one need
	 * to ensure that super.keyPressed( int ) is called!
	 * 
	 * @param keyCode The code of the pressed key
	 */
	public void keyPressed(int keyCode) 
	{
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			getCommandListener().commandAction(cmdBack, this);
		else if (Keypad.key(keyCode) == Keypad.KEY_ENTER)
			commandAction(ChatTextFieldItem.cmdSend, this.inputItem);
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
