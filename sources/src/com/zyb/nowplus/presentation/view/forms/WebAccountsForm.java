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

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ExternalNetwork;
import com.zyb.nowplus.business.domain.Identity;
import com.zyb.nowplus.business.domain.MyProfile;
//#if polish.blackberry
import com.zyb.nowplus.presentation.BlackBerryOptionsHandler;
import net.rim.device.api.ui.Keypad;
//#endif
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.nowplus.presentation.view.items.WebAccountItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.util.ItemPreinit;
import de.enough.polish.util.Locale;

/**
 * Implements the webaccounts form. From here the user should be able to signup for
 * and manage existing SN connections.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class WebAccountsForm extends BaseFramedForm
//#if polish.blackberry
implements BlackBerryOptionsHandler
//#endif
{
	/**
	 * Default command common to all profile tab screens
	 */
	public final static Command cmdOptions = new Command( Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
	public final static Command cmdAdd = new Command( Locale.get("polish.command.select"), Command.ITEM, 0);
	public final static Command cmdLogin = new Command( Locale.get("nowplus.client.java.webaccounts.command.login"), Command.ITEM, 0);
	public final static Command cmdLogout = new Command( Locale.get("nowplus.client.java.webaccounts.command.logout"), Command.ITEM, 0);
	
	private static final String KEY_IDENTITY = "ID";
	private static final String KEY_NETWORK = "NW";
	
	protected TitleBarItem titleitem;
	
	protected MyProfile me;
	
	//#if polish.blackberry
	private StringItem backButton;
	//#endif
	
	public WebAccountsForm(Model model, Controller controller)
	{
		this(model, controller, null);
	}
	
	public WebAccountsForm(Model model, Controller controller, Style style)
	{
		super(model, controller, null, style);
		
		this.me = getModel().getMe();

		updateContent(true);
		
		//#if polish.blackberry
		
		if (model.getSettings().firstLogin()
				&& model.isAddingSocialNetworkAccountsAtStartup())//@see class ServiceBroker#void nabImportFinished()
			{
				//#style ui_factory_button_item
				 backButton = UiFactory.createButtonItem(null, Locale.get("nowplus.client.java.webaccounts.command.done"), (de.enough.polish.ui.Command) cmdBack, null, null);
				 append(backButton);
			}
			
		//#else
			this.addCommand(cmdOptions);
		 	this.addCommand(cmdBack);
		//#endif
		 	
	}
	
	/**
	 * refresh ui for showing current screen
	 */
	public void update()
	{
		updateContent(true);
	}

	/**
	 * 
	 */
	private void updateContent(boolean isAwaitingUpdate)
	{
		try 
		{
			//synchronized (getPaintLock())
			{
				ExternalNetwork[] nw = getModel().getAvailableSocialNetworks();
				
				if ((this.me != null) && (nw != null))
				{
					this.me.load(true);
					
					for(int i=0; i<nw.length; i++)
					{
						//#debug debug
						System.out.println(i + " " + nw[i]);
						
						ExternalNetwork ex = nw[i];
						
						Identity iden = this.me.getAccount(ex);
						
						if(this.container.size() <= i || null == this.container.get(i))
						{
							//create new SN item
							Item itm = new WebAccountItem(ex,iden,isAwaitingUpdate);
							
							itm.setAttribute(KEY_NETWORK, ex);
							if(null != iden)
								itm.setAttribute(KEY_IDENTITY, iden);
							else
								itm.removeAttribute(KEY_IDENTITY);
								
							itm.setItemCommandListener(this);
							this.append(itm);
						}
						else
						{
							//update existing SN item
							WebAccountItem wai = (WebAccountItem)this.container.get(i);
							wai.updateAccount(ex, iden, isAwaitingUpdate);
							ItemPreinit.preinit(wai);
							
							wai.setAttribute(KEY_NETWORK, ex);
							if(null != iden)
								wai.setAttribute(KEY_IDENTITY, iden);
							else
								wai.removeAttribute(KEY_IDENTITY);
						}
					}
					
					this.requestInit();
					
					this.me.unload();
				}
			}
		} 
		catch (Exception e)
		{
			//#debug error
			System.out.println("Error while updating WebAccountsForm" + e);
		}
	}

	protected Item createBottomItem()
	{
		return null;
	}

	protected String createCssSelector()
	{
		return null;
	}

	protected Item[] createStaticItems() 
	{
		return new Item[]{ 
				this.titleitem = new TitleBarItem( Locale.get("nowplus.client.java.webaccounts.title"),getModel() )
				};
	}

	protected Item createTopItem() 
	{
		Item result;
		//#if polish.blackberry
			
			//#style .webaccount_screen_headline
			Container info = new Container(false );
			info.setAppearanceMode(Item.PLAIN);
			
			//#style .webaccount_screen_headline_text
			StringItem headerStringItem= new StringItem(null, Locale.get("nowplus.client.java.webaccounts.overview.title"));
			headerStringItem.setAppearanceMode(Item.PLAIN);
			
			info.add(headerStringItem);	
			result = info;
		  //#else
			//#style .webaccount_headline
			result = UiFactory.createStringItem(null, Locale.get("nowplus.client.java.webaccounts.overview.title"), null, null, null);
		  //#endif
			
		return result;
	}

	public byte getContext() 
	{
		return Event.Context.WEB_ACCOUNTS;
	}
	
	public void commandAction(Command c, Displayable d) 
	{
		if (c == cmdBack)
		{
			getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.BACK);
			
		//#if polish.blackberry
			if(backButton!=null)//delete back button ('Done' button command only for first time login)
			{
				remove(backButton);
				backButton=null;//free object for avoiding removing this object more than once
			}
		//#endif
		}
		else
		if(c == cmdOptions)
		{
			Item item = this.getCurrentItem();
			if(null != item && item instanceof WebAccountItem)
			{
				Identity iden = (Identity) item.getAttribute(WebAccountsForm.KEY_IDENTITY);
				
				//launch contextual menu
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, new Object[]{this.me,iden});
			}
		}
		else
		if(c == cmdClearKey)
		{
			Item item = this.getCurrentItem();
			
			if(null != item && item instanceof WebAccountItem)
			{
				Identity iden = (Identity) item.getAttribute(WebAccountsForm.KEY_IDENTITY);
				
				if(null != iden)
				{
					//envoke confirmation
					Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
					Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
					
					//event to fire upon confirmation
					Event event = new Event(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.REMOVE, iden);
					
					String name = iden.getName();
					
					//#style notification_form_delete
					ConfirmationForm cf = new ConfirmationForm(
							getModel(), getController(),
							Locale.get("nowplus.client.java.contextual.menu.confirm.delete.sn.title"),
							Locale.get("nowplus.client.java.contextual.menu.confirm.delete.sn.text1",name),
							ok, cancel,
							event);
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
				}
			}
		}
		else
			super.commandAction(c, d);
	}
	
	public void commandAction(Command c, Item item)
	{
		//#debug debug
		System.out.println("command:"+c);
		
		if(c == cmdAdd)
		{
			ExternalNetwork nw = (ExternalNetwork) item.getAttribute(WebAccountsForm.KEY_NETWORK);
			Identity iden = (Identity) item.getAttribute(WebAccountsForm.KEY_IDENTITY);
			getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.EDIT, new Object[]{nw, iden} );
		}
		else
		if(c == cmdLogout)
		{
			Identity iden = (Identity) item.getAttribute(WebAccountsForm.KEY_IDENTITY);
			getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGOUT, iden );
		}
		else
		if(c == cmdLogin)
		{
			Identity iden = (Identity) item.getAttribute(WebAccountsForm.KEY_IDENTITY);
			getController().notifyEvent(Event.Context.WEB_ACCOUNTS, Event.WebAccounts.LOGIN, iden );
		}		
		else
			super.commandAction(c, item);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.FramedForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		if( !isKeyPadKey(keyCode, gameAction) )
		{
			if( gameAction == Canvas.LEFT || gameAction == Canvas.RIGHT)
				return true;
			else
				//is keyCode keypad?
				return super.handleKeyPressed(keyCode, gameAction);
		}
		
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) {
		
		if(context == getContext())
		{
			if(event == Event.WebAccounts.REMOVE_SUCCEEDED ||
					event == Event.WebAccounts.LOGIN_SUCCEEDED ||
					event == Event.WebAccounts.LOGOUT_SUCCEEDED ||
					event == Event.WebAccounts.ADD_SUCCEEDED)
			{
				this.updateContent(false);
			}
		}
		else
		if( context == Event.Context.CONTACTS)
		{
			if(event == Event.Contacts.UPDATE)
			{
				//is contact id same?
				if(null != this.me && null != data && data instanceof Long && ((Long)data).longValue() == this.me.getCabId())
				{
					this.updateContent(false);
				}
			}
		}
		else
			super.handleEvent(context, event, data);
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions() {
		commandAction(cmdOptions, this);
		return true;
	}
	//#endif
	
	//#if polish.blackberry
	//overrides
	/**
	 * Handles key events.
	 * 
	 * WARNING: When this method should be overwritten, one need
	 * to ensure that super.keyPressed( int ) is called!
	 * 
	 * @param keyCode The code of the pressed key
	 * @see de.enough.polish.ui.Screen#keyPressed(int)
	 */
	public void keyPressed(int keyCode) 
	{
		 if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			commandAction(cmdBack, this); 
		else if (Keypad.key(keyCode) == Keypad.KEY_BACKSPACE) 
			commandAction(cmdClearKey, this); 
		else
			super.keyPressed(keyCode);
	}
	//#endif
}
