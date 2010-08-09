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
//#condition !polish.remove_status_tab
package com.zyb.nowplus.presentation.view.forms;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.StatusActivityItem;
import com.zyb.nowplus.presentation.view.providers.StatusListProvider;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
//#endif

/**
 * The 'Status' tab of the PeoplePages
 * <p>
 * NOTE: previously named 'Friendstream'
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class PeopleTabStatus extends BasePeopleForm
{
	public final static Command cmdRequest = new Command(Locale.get("nowplus.client.java.statuspage.command.request"),Command.OK, 0);
	
	static int requestSize = 15;
	
	int fetchSize = requestSize;
	
	StringItem activityNoResults;
	
	//#if polish.blackberry.isTouchBuild == true
	private Command statusTabCmd;
	//#endif	
	
	StatusListProvider provider;
	
//	long lastActivityId = -1;
	
	boolean empty = false; 
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 */
	public PeopleTabStatus(Model model, Controller controller)
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
	public PeopleTabStatus(Model model, Controller controller, Style style)
	{
		super(model, controller, Event.Context.CONTACTS, PEOPLEPAGE_STATUS_TAB_PRIORITY, style);

		//#debug debug
		System.out.println("Constructing Status Tab");
		
		//enable scrolling cycling for fixing bug#0016902: PBLA-133 Timeline: Scrolling in the Timeline is incorrect
		de.enough.polish.ui.UiAccess.setCycling(this, true);
		de.enough.polish.ui.UiAccess.setCycling(container, true);
		
		setCommandListener(this);
		
		String iconImageName = null;
		
		//#if polish.blackberry
			 iconImageName = Locale.get("nowplus.client.java.peoplepage.tab.status");// "Status";
		//#endif
		
		//Fake usage of style to avoid removal during preprocessing
		//#style peoplepage_navbar_item_status_active
		this.tabItem = new IconItem(iconImageName, null);
		
		//#style peoplepage_navbar_item_status
		this.tabItem.setStyle();
		
		this.tabItem.setDefaultCommand(cmdFake);	 
		
		//#if polish.blackberry.isTouchBuild == true
			statusTabCmd = new Command("statusTab", Item.BUTTON, 0);
			this.tabItem.setDefaultCommand(statusTabCmd);
			this.tabItem.setItemCommandListener(this);
			super.setCommandListener(this);
		//#endif
		
		setTabItem(this.tabItem);
		
		this.tabTitle = Locale.get("nowplus.client.java.peoplepage.tab.status");
			
		setTabTitle(this.tabTitle);
			
		//#style activitylist_noresults
		this.activityNoResults = new StringItem(null,Locale.get("nowplus.client.java.statuspage.noresult"));
		
		this.provider = new StatusListProvider(this.container,this,this,requestSize,cmdRequest);
		
		//#if polish.blackberry
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
		return PEOPLEPAGE_STATUS_TAB_PRIORITY;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createBottomItem()
	 */
	protected Item createBottomItem()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#createCssSelector()
	 */
	protected String createCssSelector() 
	{
		return null;
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
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#showNotify()
	 */
	public void showNotify()
	{
		//#debug debug
		System.out.println("showNotify()");

		requestActivities(this.fetchSize,false);
		
		//Call super MUST be last to retain navBar focus state
		super.showNotify();
	}
	
	synchronized void requestActivities(int count, boolean request)
	{
		//prepare content container
		ListSelection selection = getModel().getFriendsStream(0,count);
		
		Object[] entries = selection.getEntries();
				
//		if(entries != null && entries.length > 0) {
//			Activity activity = (Activity)entries[0];
//			long activityId = activity.getId();
//			
//			if(this.lastActivityId == activityId && !request) {
//				if(!isActive(this.container)) {
//					this.container.setScrollYOffset(0);
//				}
//				return;
//			}
//			
//			if(activityId != -1) {
//				this.lastActivityId = activityId;
//			}
//			
//		}
		
		setActivities(selection, entries, request);
	}
	
	public void setActivities(ListSelection selection, Object[] entries, boolean request) {
		if(entries == null || entries.length == 0)
		{
			if(!this.empty) {
				this.provider.clear();
				//#style .activitylist_noresults
				setStyle();
				this.container.add(this.activityNoResults);
				this.empty = true;
			}
		}
		else
		{
			if(this.empty) {
				//#style .base_form
				setStyle();
				this.empty = false;
			}
			
			this.provider.apply(selection, entries, selection.size(), null, this.isActive(this.container),request);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		if( context == Event.Context.ACTIVITY )
		{
			switch(event)
			{
				case Event.Activities.STATUS_STREAM_CHANGED :
					/*
					//Do NOT automatically fetch new status updates, bug 0010229
					int newActivities = ((Integer)data).intValue();
					*/
					requestActivities(this.fetchSize /*+ newActivities*/,false);
					break;
			}
		}
        else
            super.handleEvent(context, event, data);

	}
	
	
	//#if polish.blackberry.isTouchBuild == true
	public void commandAction(Command c, Item item) {
        
        if(c == statusTabCmd)
        {
        	//special tab switch handling for BB touch devices
            super.switchTabByIndex(this.tabIndex);    
            return;
        } 
        else
        	super.commandAction(c,item);
	}
	//#endif
	
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.CommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Displayable)
	 */
	public void commandAction(Command c, Displayable d) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
				
		if(c == cmdRequest)
		{
			this.fetchSize += requestSize;
			requestActivities(this.fetchSize,true);
			return;
		}
		
		//fetch profile handle
		Object profile = null;
		if(	null != getCurrentItem() && 
			getCurrentItem() instanceof StatusActivityItem )
		{
			StatusActivityItem activityItem = (StatusActivityItem)getCurrentItem();
			
			Object activityProfile = activityItem.getProfile();
			
			if(activityProfile != null)
			{
				profile = activityProfile;
			}
		}
		else
		{
			profile = getModel().getMe();
		}
			
		if( c == cmdOpen)
		{
			//fix for bug  0006749
			//#if polish.blackberry.isTouchBuild == false
            if (navBar.getStyle() != navBarFocusedStyle) 
            //#endif
            {
            	controller.notifyEvent(Event.Context.PROFILE,Event.Profile.OPEN, profile);
            }
            return;
		}
		
		if(c == cmdOptions)
		{
			controller.notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, profile);
			return;
		}
		
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
				//#style .peoplepage_navbar_item_status_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_status_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_status_active
				tabItem.setStyle();
			}
		}
		else if ( isNavBarFocused() )
		{
			
			if( !isTabActive() )
			{
				//#if polish.blackberry.isTouchBuild == true
				//#style .peoplepage_navbar_item_status_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_status_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_status
				tabItem.setStyle();
			}
		}
	}
		
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) {
		
		//This nasty hack is NOT ok, cycling should be handled using polish.FramedForm.allowCycling in variables section of build.xml
//		//#if polish.blackberry
//		//disable wrap from bottom to top etc
//		if( (currentlyActiveContainer == this.container &&
//				currentlyActiveContainer.getFocusedIndex() == currentlyActiveContainer.size() - 1 &&
//				(gameAction == Canvas.DOWN && keyCode != Canvas.KEY_NUM8))){
//			return true;
//		}else if( (currentlyActiveContainer == this.topFrame &&
//				(gameAction == Canvas.UP && keyCode != Canvas.KEY_NUM2))){
//			return true;
//		}
//		//#endif
		
		//#if polish.blackberry
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
		{
			commandAction(cmdExit, this); 
			return true;
		}
		//#endif
		setFromContacts(false);
		return super.handleKeyPressed(keyCode, gameAction);
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions() {
		commandAction(cmdOptions, this);
		return true;
	}
	//#endif
		
	//#mdebug error
	public String toString()
	{
		return "PeopleTabStatus[]";
	}
	//#enddebug
}
