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

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Graphics;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Channel;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.forms.ConfirmationForm;
import com.zyb.nowplus.presentation.view.items.ContactSummarizedItem;
import com.zyb.nowplus.presentation.view.items.FilterContainer;
import com.zyb.nowplus.presentation.view.items.IconSwapItem;
import com.zyb.nowplus.presentation.view.items.SearchFieldItem;
import com.zyb.nowplus.presentation.view.providers.VirtualContactProvider;
//#= import com.zyb.nowplus.presentation.view.providers.ListProvider;
//#= import com.zyb.nowplus.presentation.view.providers.ContactProvider;
import com.zyb.util.event.Event;

import de.enough.polish.event.EventManager;
import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Display;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Debug;
import de.enough.polish.util.DeviceControl;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;

	//#if polish.blackberry.isTouchBuild == true
	//# import net.rim.device.api.ui.VirtualKeyboard;
	//#endif
	
//#endif

/**
 * The 'People' tab of the PeoplePages
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class PeopleTabContacts extends BasePeopleForm
{
	/**
	 * Time in millis that should pass before input in searchfield is passed to controller
	 */
	public static final int KEY_INPUT_DELAY_MILLIS = 500;
	
	/**
	 * Time in millis that should pass before searchfield automatically goes away
	 */
	public static final int AUTO_HIDE_DELAY_MILLIS = 5000;
	
	/**
	 * Filter menu for filtering contact according to groups
	 */
	protected FilterContainer filter;

	private Command cmdFilter;
	
	//#if polish.blackberry
		boolean firstInit = true;
	//#endif
	
	//#if polish.blackberry.isTouchBuild == true
		protected FilterForm filterForm;
		private Command contactTabCmd;
	//#endif
	
	/**
	 * Searchfield for alphabetical filtering of contacts
	 */
	protected SearchFieldItem search;
	
	/**
	 * Timer used to enable small delay in keyinput handling
	 * before Events are fired
	 */
	protected Timer delayedSearchTimer, autoSearchHideTimer;	
	
	public boolean isFilterRequested = false;
	
	//<mobica
	//#if not polish.key.ClearKey:defined
    //# private Command cmdClear = new Command(Locale.get("polish.command.clear"), Command.CANCEL, 0);
	//#endif
	//mobica>
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 */
	public PeopleTabContacts(Model model, Controller controller)
	{
		this(model, controller, null);
	}

	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param style
	 */
	public PeopleTabContacts(Model model, Controller controller, Style style)
	{
		super(model, controller, Event.Context.CONTACTS, PEOPLEPAGE_CONTACTS_TAB_PRIORITY, style);

		//#debug debug
		System.out.println("Constructing Contacts Tab");
			
		setCommandListener(this);
		
		String iconImageName = null;
		
		//#if polish.blackberry
			 iconImageName = Locale.get("nowplus.client.java.peoplepage.tab.icon.name.contacts"); //"Contacts";
		//#endif
		
		//Fake usage of style to avoid removal during preprocessing
		//#style peoplepage_navbar_item_contacts_active
		this.tabItem = new IconItem(iconImageName, null);
		
		//#style peoplepage_navbar_item_contacts
		this.tabItem.setStyle();
		
		this.tabItem.setDefaultCommand(cmdFake);	
		
		//#if polish.blackberry.isTouchBuild == true
			contactTabCmd = new Command("contactTab", Item.BUTTON, 0);
			this.tabItem.setDefaultCommand(contactTabCmd);
			this.tabItem.setItemCommandListener(this);
		//#endif
			
		setTabItem(this.tabItem);
		
		this.tabTitle = Locale.get("nowplus.client.java.peoplepage.tab.people");
			
		setTabTitle(this.tabTitle);
		
		int minimumItems;
		// load 30 Contacts for BB Storm to fix a empty contact list by scrolling.
		//#if polish.blackberry.isTouchBuild == true
		minimumItems = 30;
		//#else
		minimumItems = 15;
		//#endif
		
		//#if activate.virtual.listprovider == true
		this.provider = new VirtualContactProvider(this.model,this.controller,this.container,this,this,minimumItems);
		//#else
		//# this.provider = new ContactProvider(model,
		//# controller,
		//# this.container,
		//# this,
		//# minimumItems,
		//# 5,
		//# ListProvider.DEFAULT_INTERVAL);
		//#endif
		
        //#if polish.blackberry
        removeCommand(cmdOpen);
        removeCommand(cmdExit);
        removeCommand(cmdOptions);
        //#endif
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#getPriority()
	 */
	public byte getPriority() 
	{
		return PEOPLEPAGE_CONTACTS_TAB_PRIORITY;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createBottomItem()
	 */
	protected Item createBottomItem() 
	{
		//#style searchfield_base
		this.search = new SearchFieldItem();
		
		//this.search.addCommand(cmdFake);  //add fake command to suppress key pressing
		
		//#if polish.blackberry.isTouchBuild == true
			//#if activate.virtual.listprovider == true
			this.search.setProvider(this.provider);
			//#endif
			this.search.addCommand(cmdFake);
		//#else
			this.search.setVisible(false);
		//#endif
		
		return search;
	}
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#isBottomItemAccessible()
	 */
	protected boolean isBottomItemAccessible()
	{
		return this.search.isInUse();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createCssSelector()
	 */
	protected String createCssSelector() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createTopItem()
	 */
	protected Item createTopItem() 
	{
		Filter[] filters = getModel().getContactFilters();	
		
		Item result;
		//#if polish.blackberry.isTouchBuild == true
			//Filter Button
			//Displayable lastScreen = Display.getInstance().getCurrent();			
			//#style filter_form
			this.filterForm = new FilterForm(getModel(),getController());						
			this.filter = new FilterContainer(filters, this.container, getController(), this.filterForm);
			this.filterForm.setFilter(filter);
			IconSwapItem button = this.filterForm.getButton();			
			cmdFilter = new Command("", Command.ITEM, 0);
			button.setDefaultCommand(cmdFilter);
			button.setItemCommandListener(this);
			result = button;
		//#else
			//#style peoplepage_filtercontainer
			this.filter = new FilterContainer(filters, this.container, getController(), null);		
			this.filter.setAppearanceMode(Item.INTERACTIVE);
			this.filter.allowCycling = true;
			result = this.filter;
		//#endif
			
		return result;
	}
	
	public void requestFocusFilter() {
		this.isFilterRequested = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#showNotify()
	 */
	public void showNotify() 
	{
		super.showNotify();
		
		if(this.isFilterRequested) {
			forceFilterFocus();
			this.isFilterRequested = false;
		}
		else {
			//prepare content container
			this.provider.update(isActive(this.container));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#hideNotify()
	 */
	public void hideNotify() 
	{
		//#if polish.blackberry.isTouchBuild == false
			//hide searchfield if active
			hideLocalSearchField();
		//#endif
			
		super.hideNotify();
	}
	
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
				case Event.Contacts.REFRESHING_LIST: 
				case Event.Contacts.REFRESH_LIST : 
				case Event.Contacts.UPDATE_IN_LIST :
				case Event.Contacts.ADD_TO_LIST :
					requestUpdate();
					break;

				case Event.Contacts.REMOVE_FROM_LIST :
					handleRemoval(data);
					break;
				
				case Event.Contacts.FILTER_CHANGED :
					this.provider.reset();
					break;

				case Event.Contacts.FILTERS_UPDATED:	
					//update filter item
					if(null != this.filter)
					{
						Filter[] filters = getModel().getContactFilters();
						
						if(null != filters)
							this.filter.setFilters(filters);
					}
					
					break;
			}
		}
        else
            super.handleEvent(context, event, data);
	}
	
	protected void handleRemoval(Object data)
	{
		ContactSummarizedItem contactSummarizedItem = (ContactSummarizedItem)this.container.getFocusedItem();
		Object focusedData = null;
		if(contactSummarizedItem != null)
		{
			focusedData = contactSummarizedItem.getContact();
		}
		
		if(focusedData != null)
		{
			Container container = this.provider.getContainer();
			int index = container.getFocusedIndex();
			if(index == container.size() -1)
			{
				index = index - 1;
			}
			else
			{
				index = index + 1;
			}
			
			Object scope = null;
			if(index != -1)
			{
				contactSummarizedItem = (ContactSummarizedItem)container.get(index);
				
				scope = contactSummarizedItem.getContact();
			}
			
			this.provider.update(scope, isActive(this.container));
		}
		else
		{
			this.provider.update(isActive(this.container));
		}
	}
	
	protected void requestUpdate() {
		this.provider.requestUpdate();
	}
	
	protected void handleUpdate()
	{
		this.provider.update(isActive(this.container));
	}
	
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		boolean result = false;
		
		//is keyCode keypad?
		if( !isKeyPadKey(keyCode, gameAction) 
			//#if polish.key.ClearKey:defined && !polish.blackberry
				//make sure that textfield does not autohide while clear key is being pressed
				&& !( getCurrentItem() == this.bottomItem && keyCode == TextField.KEY_DELETE )
			//#endif
			)
		{
			//Override super class handling of topItem to avoid tab switch when filter is focused
			if( this.topItem != null && getCurrentItem() == this.topItem && this.topItem == this.filter)
			{
				if(UiAccess.handleKeyPressed(this.topItem, keyCode, gameAction))
					return true;
			}
			else
			if( null != this.search && this.search.isInUse() &&
					(
							(gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8) ||
							(gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2) ||
							(gameAction == Canvas.LEFT && keyCode != Canvas.KEY_NUM4) ||
							(gameAction == Canvas.RIGHT && keyCode != Canvas.KEY_NUM6)
					)
			)
			{
				//cancel timers
				if(null != delayedSearchTimer)
					delayedSearchTimer.cancel();
				if(null != autoSearchHideTimer)
					autoSearchHideTimer.cancel();
				
				//reset search criteria
				this.search.setString("");
				
				//hide searchfield
				hideLocalSearchField();
				
				//redirect key handling to container for navigation
				return UiAccess.handleKeyPressed(this.container, keyCode, gameAction);
			}
			
			//handle navbar & filterbar transition if list is empty
			if(this.container.size() == 0)
			{
				if( navBar.getStyle() == navBarFocusedStyle  && currentlyActiveContainer == this.topFrame &&
						currentlyActiveContainer.getFocusedIndex() == 0 &&
						(gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2))
				{
					forceFilterFocus();
					return true;	
				}
				else
				if( navBar.getStyle() == navBarUnfocusedStyle  && currentlyActiveContainer == this.topFrame &&
						currentlyActiveContainer.getFocusedIndex() == 1 &&
						(gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8))
				{
					currentlyActiveContainer.focusChild(0);
					forceNavbarFocus();
					return true;
				}
			}
			
			//making sure that bottom frame (search field)is not envoked via UP/DOWN navigation
			if( (currentlyActiveContainer == this.container &&
					currentlyActiveContainer.getFocusedIndex() == currentlyActiveContainer.size() - 1 &&
					(gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8))
				||
				(currentlyActiveContainer == this.topFrame &&
					currentlyActiveContainer.getFocusedIndex() == 0 &&
					(gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2))
					)
			{
				//This nasty hack is NOT ok, cycling should be handled using polish.FramedForm.allowCycling in variables section of build.xml
//				//#if polish.blackberry
//				//disable wrap from bottom to top etc
//				if( (currentlyActiveContainer == this.container &&
//						currentlyActiveContainer.getFocusedIndex() == currentlyActiveContainer.size() - 1 &&
//						(gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8))){
//					//setActiveFrame(Graphics.TOP);
//					//currentlyActiveContainer.focusChild(0);
//					return true;
//				}else if( (currentlyActiveContainer == this.topFrame &&
//						(gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2))){
//					return true;
//				}
//				//#endif
				
				if(Canvas.UP == gameAction)
				{
					setActiveFrame(Graphics.BOTTOM);
					this.currentlyActiveContainer.focusChild(this.currentlyActiveContainer.size()-1);
					//return true; //No! Handled by super
				}
				else
				if(Canvas.DOWN == gameAction)
				{
					setActiveFrame(Graphics.TOP);
					this.currentlyActiveContainer.focusChild(0);
					forceNavbarFocus();
					//ensuring contactlist defocus
					if(this.provider != null)
						this.provider.reset();
					return true;
				}
			}
			setFromContacts(true);
			result = super.handleKeyPressed(keyCode, gameAction);
		}
        // used to avoid filter bar defocus when pressing * (KEY_STAR), fix for bug:0003919  
		else
		if (keyCode == Canvas.KEY_STAR &&
				(null == this.search || (null != this.search && !this.search.isInUse())))
		{
			return true;
		}
		else
		if((getCurrentItem() != null  && getCurrentItem() == this.filter) || isActive(this.container) || isActive(this.bottomFrame) ) 
		{
			//focus bottomItem by press of keypad
			if( null != this.bottomItem && getCurrentItem() != this.bottomItem )
			{
				//activate bottom item
				this.setActiveFrame(Graphics.BOTTOM);
			}
			
			//override super class 'bottomItem' handling for special handling of local searchfield
			if( !result && this.bottomItem != null && getCurrentItem() == this.bottomItem)
				if(handleBottomItemInput(keyCode, gameAction))
					result = true;
		}
		
		//#if polish.blackberry
			if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			{
				//#if polish.blackberry.isTouchBuild == true    
				
				if( !result && this.bottomItem != null && getCurrentItem() == this.bottomItem)
					hideLocalSearchField();
				else
					commandAction(cmdExit, this); 
				
				//#else
				
					commandAction(cmdExit, this); 
					
				//#endif
					
				result = true;
			}
			else if (Keypad.key(keyCode) == Keypad.KEY_MENU)
			{
				commandAction(cmdOptions, this); 
				
				result = true;
			}
		//#endif		
		
		return result;
	}
		
	protected boolean handleKeyRepeated(int keyCode, int gameAction) 
	{
		//#ifdef testversion:defined
		if(keyCode == KEY_POUND)
		{
			Debug.showLog(StyleSheet.display);
			return true;
		}
		//#endif

		if(!isActive(this.container))
		{
			// redirect keyRepeated to keyPressed to ensure right focus
			// if active container is not the main container
			return handleKeyPressed(keyCode, gameAction);
		}
		else
		{
			return super.handleKeyRepeated(keyCode, gameAction);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#handleBottomItemInput(int, int)
	 */
	protected boolean handleBottomItemInput(int keyCode, int gameAction)
	{
		boolean result = false;
		
		//#if polish.blackberry
		
			//#if polish.blackberry.isTouchBuild == true
		
			if(this.bottomItem == this.search)
			{
				startSearchTimers();
				showLocalSearchField();
				result = true;
			}
			
			//#else
				if(isKeyPadKey(keyCode, gameAction)){
					startSearchTimers();
					showLocalSearchField();
					result = false;
				}
				
				
			//#endif
		
		//#else
			
			if(this.bottomItem == this.search)
			{
				if( (result = UiAccess.handleKeyPressed(this.search, keyCode, gameAction)) )
					startSearchTimers();
				else
				{
					//#debug debug
					System.out.println("not handled");
				}
			}
			
			if(!this.search.getString().equals(""))
			{
				//search activation, show search bar
				showLocalSearchField();
			}
			else
			{
				//search deactivation, hide search bar
				hideLocalSearchField();
			}
			
		//#endif
		
		return result;
	}
	
	/**
	 * Start the timers for the search and for removing the search field.
	 */
	private void startSearchTimers()
	{
		//cancel old timer if present
		if(null != delayedSearchTimer)
			delayedSearchTimer.cancel();
		
		//init new timer
		this.delayedSearchTimer = new Timer();
		TimerTask tt = new TimerTask()
		{
			public void run()
			{
				//#if polish.blackberry
				
					//#if !polish.blackberry.isTouchBuild
				
					//start searching only if there is something to search for
					if(!search.getString().equals(""))
					{
						provider.getContainer().focusChild(-1);
						provider.search(search.getString());
					}
				
					try
					{
	                    Thread.sleep(200);
	                }
					catch(InterruptedException e)
					{
	                    System.out.println(e);
	                }
					
					//#else
					
					provider.search(search.getString());
					provider.getContainer().focusChild(-1);
					
					//#endif
				
				//#else
				
					provider.search(search.getString());
					provider.getContainer().focusChild(-1);
						
				//#endif
			}
		};
		
		//schedule event
		//#if polish.blackberry
		Display.getInstance().callSerially(tt);
		//#else
		this.delayedSearchTimer.schedule(tt, KEY_INPUT_DELAY_MILLIS); 
		//#endif
		
		//cancel old timer if present
		if(null != autoSearchHideTimer)
			autoSearchHideTimer.cancel();
		
		//init new timer
		this.autoSearchHideTimer = new Timer();
		tt = new TimerTask()
		{
			public void run()
			{
				hideLocalSearchField();
			}
		};
		//schedule event
		this.autoSearchHideTimer.schedule(tt, AUTO_HIDE_DELAY_MILLIS);
	}
	
	protected void setActiveFrame(Container newFrame, boolean keepMainFocus, int direction)
	{
		if(this.currentlyActiveContainer == this.bottomFrame && newFrame == this.container && !this.search.isInUse())
		{
			this.provider.update(getModel().getLastContact(), true);
		}
		
		if(this.currentlyActiveContainer == this.topFrame && newFrame == this.container)
		{
			this.provider.update(getModel().getFirstContact(), true);
		}
		
		//#if !polish.hasPointerEvents || polish.hasPointerEvents == false
			super.setActiveFrame(newFrame, keepMainFocus, direction);
		//#endif
	}
	
	private void hideLocalSearchField()
	{
		
		if(this.search.isInUse()
		//#if polish.blackberry.isTouchBuild == true      
	    || !this.topFrame.isVisible()
	    //#endif
		)
		
		{
			//#if not polish.blackberry
			//<mobica
			//#if not polish.key.ClearKey:defined
			//# this.addCommand(cmdExit);
			//# this.removeCommand(cmdClear);
			//#endif
			//mobica>
			//#endif
			
			int focusedIndex = this.container.getFocusedIndex();
			this.container.focusChild(-1);
			this.setActiveFrame(-1);
			this.container.focusChild(focusedIndex);
			
			this.search.setString("");
			EventManager.fireEvent("searchHide", this, null);
			this.search.setInUse(false);
			//#if polish.blackberry.isTouchBuild == true
			UiAccess.setVisible(this.topFrame, true);
			//# Display.getInstance().getVirtualKeyboard().setVisibility( VirtualKeyboard.HIDE );
			//#endif
		}
	}
	
	public void showLocalSearchField()
	{
		if(!this.search.isInUse())
		{
			//#if not polish.blackberry
			//<mobica
			//#if not polish.key.ClearKey:defined
			//# this.removeCommand(cmdExit);
			//# this.addCommand(cmdClear);
			//#endif
			//mobica>
			//#endif
			
			EventManager.fireEvent("searchShow", this, null);
			this.search.setInUse(true);
			//#if polish.blackberry.isTouchBuild == true
			UiAccess.setVisible(this.topFrame, false);
			//#endif
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if(c == cmdOptions)
		{
			//launch contextual menu
			if(null != getCurrentItem() && getCurrentItem() instanceof ContactSummarizedItem)
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, ((ContactSummarizedItem)getCurrentItem()).getContact());
			else
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, model.getMe());
		}
		else
		if(c == cmdGreenCallKey)
		{
			Channel ch = null;
			
			if(null != getCurrentItem() && getCurrentItem() instanceof ContactSummarizedItem)
			{
				ProfileSummary ps = ((ContactSummarizedItem)getCurrentItem()).getContact();
				
				if(ps instanceof Profile)
					((Profile)ps).load(true);
				
				ch = ps.getPrimaryCallChannel();
				
				if(ps instanceof Profile)
					((Profile)ps).unload();
			}
			
			getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, ch);		
		}
		else
		if(c == cmdClearKey)
		{
			//#if !(polish.blackberry.isTouchBuild == true)
			//the clearkey on bb touch is only for editing the search entry. Not to delete a contact.
			
			if(currentlyActiveContainer == this.container && null != this.getCurrentItem() && this.getCurrentItem() instanceof ContactSummarizedItem)
			{
				ContactSummarizedItem csi = (ContactSummarizedItem)this.getCurrentItem();
				
				//invoke confirmation
				Command ok = new Command(Locale.get("polish.command.ok"), Command.SCREEN, 0);
				Command cancel = new Command(Locale.get("polish.command.cancel"), Command.CANCEL, 0);
				
				//event to fire upon confirmation
				Event event = new Event(getContext(),Event.Contacts.DELETE_CONTACT, csi.getContact());
				
				String formattedName = ((String)(csi.getContact().getFullName()));
				
				//#style notification_form_delete
				ConfirmationForm cf = new ConfirmationForm(
						getModel(), getController(),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.contact.title"),
						Locale.get("nowplus.client.java.contextual.menu.confirm.delete.contact.text",formattedName),
						ok, cancel,
						event);
				getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_GLOBAL, cf);
			}
			//#endif
		}
		//#if not polish.blackberry
		//<mobica
		//#if not polish.key.ClearKey:defined
		//# else if(c == cmdClear) {
		//#		startSearchTimers();
		//#		this.search.deleteCharAtCaretPosition();
		//# }
		//#endif
		//mobica>
		//#endif
		else
			super.commandAction(c, d);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#switchTabItemStyle()
	 */
	protected void switchTabItemStyle() 
	{
		if( isNavBarUnfocused() )
		{
			if( !isTabActive() )
			{
				//#if polish.blackberry.isTouchBuild == true
				//#style .peoplepage_navbar_item_contacts_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_contacts_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_contacts_active
				tabItem.setStyle();
			}
		}
		else if ( isNavBarFocused() )
		{
			if( !isTabActive() )
			{
				//#if polish.blackberry.isTouchBuild == true
				//#style .peoplepage_navbar_item_contacts_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_contacts_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_contacts
				tabItem.setStyle();
			}
		}
		
		//#if polish.blackberry
		if (firstInit)
		{
			 //#style .peoplepage_navbar_item_contacts_active
			 tabItem.setStyle();
             firstInit = false;
        }
		//#endif
	}
	
	public void forceFilterFocus()
	{
		//defocus navbar
		forceNavbarDefocus();
		
		//focus filter
		this.setActiveFrame(Graphics.TOP,false);
		this.topFrame.focusChild( this.topFrame.size() - 1 );
		UiAccess.focus(this, filter);
		
		repaint();
		
		if (this.provider != null) {
			this.provider.reset();
		}
	}
	
	public void releaseResources() 
	{
		//cancel timers
		if(null != delayedSearchTimer)
			delayedSearchTimer.cancel();
		if(null != autoSearchHideTimer)
			autoSearchHideTimer.cancel();
		
		super.releaseResources();
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions()
	{
		commandAction(cmdOptions, this);
		return true;
	}

	protected boolean handleKeyReleased(int keyCode, int gameAction)
	{
		if (Keypad.key(keyCode) == Keypad.KEY_SEND)
		{
            //check for profile number
            Item curItem = getCurrentItem();
            if(null != getCurrentItem() && getCurrentItem() instanceof ContactSummarizedItem){
            	Profile profile = (Profile) ((ContactSummarizedItem) curItem).getContact();
            	profile.load(true);
            	Channel callChannel = profile.getPrimaryCallChannel();
            	profile.unload();
            	//Identity[] idents = profile.getIdentities(Identity.TYPE_PHONE);
            	//if(callChannel == null && idents.length > 0){
            	//	callChannel = idents[0].getChannel(Channel.TYPE_CALL);
            	//}
            	//#debug info
            	System.out.println("Got profile " + profile.getFullName() + " with primary call number " + callChannel);
            	if(callChannel != null){
            		getController().notifyEvent(Event.Context.PROFILE, Event.Profile.LAUNCH_CHANNEL, callChannel);
            		return true;
            	}
            }
        }
		else if (Keypad.key(keyCode) == Keypad.KEY_MENU)
		{
        	//launch contextual menu
        	Item item = getCurrentItem();
			if(null != item && item instanceof ContactSummarizedItem){
				getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, ((ContactSummarizedItem)getCurrentItem()).getContact());
				return true;
			}
        }

		return super.handleKeyReleased(keyCode, gameAction);
	}
	//#endif
	
	//#mdebug error
	public String toString()
	{
		return "PeopleTabContacts[]";
	}
	//#enddebug
	
	//#if polish.blackberry.isTouchBuild == true
	
	public void commandAction(Command c, Item item)
	{
        //refactored from VirtualContact Provider   
		try
		{
			if (c == BasePeopleForm.cmdOpen)
			{
				//fix for PBLA-833
				hideLocalSearchField();
				
				ContactSummarizedItem contactSummarizedItem = (ContactSummarizedItem) item;

				Object data = contactSummarizedItem.getContact();

				this.controller.notifyEvent(Event.Context.PROFILE,
						Event.Profile.OPEN, data);
			}

		}
		catch (Exception e)
		{
			System.out.println(this + "thrown exception:" + e.toString());
		}
		
		
        if(c == cmdFilter) {
        		Display.getInstance().setCurrent(this.filterForm);
                return;
        }else                  
        
        if(c == contactTabCmd)
        {
        	//special tab switch handling for BB touch devices
            super.switchTabByIndex(this.tabIndex);         
            return;
        }  
        else
        	super.commandAction(c,item);
	}
	
   /*
    * (non-Javadoc)
    * @see de.enough.polish.ui.Screen#handlePointerTouchDown(int, int)
    */
    public boolean handlePointerTouchDown( int x, int y) {
      if(!this.topFrame.isVisible())
              return handlePointerPressed( x, y );
      return super.handlePointerTouchDown(x,y);
   }
    
	/**
	 * Handles key events.
	 * 
	 * WARNING: When this method should be overwritten, one need to ensure that
	 * super.keyPressed( int ) is called!
	 * 
	 * @param keyCode
	 *            The code of the pressed key
	 */
    public void keyPressed(int keyCode)
    {
    	if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE && this.search.isInUse())
    	{
    		DeviceControl.hideSoftKeyboard();
            hideLocalSearchField();
            showNotify();
    	}
    	else if (Keypad.key(keyCode) == Keypad.KEY_ENTER && this.search.isInUse())
    	{
    		//do nothing
    	}
    	else
    	{
    		super.keyPressed(keyCode);
    	}
    }
	
    //#endif
}
