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
//#condition activate.timeline.tab

package com.zyb.nowplus.presentation.view.forms;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Activity;
import com.zyb.nowplus.business.domain.ListSelection;
import com.zyb.nowplus.business.domain.Profile;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.providers.TimelineListProvider;
import com.zyb.util.event.Event;

import com.zyb.nowplus.presentation.view.items.ActivityItem;
import com.zyb.nowplus.presentation.view.items.TimelineActivityItem;


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
 * The 'Timeline' tab of the PeoplePages.
 * <p>
 * NOTE: previously named 'lifedrive'
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class PeopleTabTimeline extends BasePeopleForm
{
	//#if polish.blackberry.isTouchBuild == true
	private Command timelineTabCmd;
	//#endif
	
	//command for request next set of items to be added into list 
	public final static Command cmdRequest = new Command(Locale.get("polish.command.select"),Command.OK, 0);
	
	//#if polish.blackberry
	static int requestSize = 15;
	//#else
	//#	static int requestSize = 15;  
	//#endif
	
	int fetchSize = requestSize;
	
	StringItem activityNoResults;
	
	TimelineListProvider provider;
	
	long lastActivityId = -1;
	
	boolean empty; 
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 */
	public PeopleTabTimeline(Model model, Controller controller)
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
	public PeopleTabTimeline(Model model, Controller controller, Style style)
	{
		super(model, controller, Event.Context.CONTACTS, PEOPLEPAGE_TIMELINE_TAB_PRIORITY, style);
		
		//#debug startup
		System.out.println("Constructing Timeline Tab");
		
		//enable scrolling cycling for fixing bug#0016902: PBLA-133 Timeline: Scrolling in the Timeline is incorrect
		de.enough.polish.ui.UiAccess.setCycling(this, true);
		de.enough.polish.ui.UiAccess.setCycling(container, true);
		
		String iconImageName = null;
		
		//#if polish.blackberry 
			 iconImageName = Locale.get("nowplus.client.java.peoplepage.tab.chat");
		//#endif
		
		//Fake usage of style to avoid removal during preprocessing
		//#style peoplepage_navbar_item_timeline_active
		this.tabItem = new IconItem(iconImageName, null);
		
		//#style peoplepage_navbar_item_timeline
		this.tabItem.setStyle();
		
		this.tabItem.setDefaultCommand(cmdFake);	
		
		//#if polish.blackberry.isTouchBuild == true
		timelineTabCmd = new Command("timelineTab", Item.BUTTON, 0);
		this.tabItem.setDefaultCommand(timelineTabCmd);
		this.tabItem.setItemCommandListener(this);
		super.setCommandListener(this);
		//#endif
		
		setTabItem(this.tabItem);
		
		this.tabTitle = Locale.get("nowplus.client.java.peoplepage.tab.chat");
			
		setTabTitle(this.tabTitle);
		
		//#style activitylist_noresults
		this.activityNoResults = new StringItem(null, Locale.get("nowplus.client.java.peoplepage.tab.timeline.empty"));
		
		this.provider = new TimelineListProvider(this.container,this,this,requestSize,cmdRequest);
	}
	
	synchronized void requestActivities(int count, boolean request)
	{
		// prepare content container
		ListSelection selection = getModel().getLifeDrive(0, count);

		Object[] entries = selection.getEntries();

		if (entries != null && entries.length > 0)
		{
			Activity activity = (Activity) entries[0];
			
			long activityId = activity.getId();

			if (this.lastActivityId == activityId && !request)
			{
				if (!isActive(this.container))
					this.container.setScrollYOffset(0);
				
				return;
			}

			if (activityId != -1)
				this.lastActivityId = activityId;
		}

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
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#getPriority()
	 */
	public byte getPriority() 
	{
		return PEOPLEPAGE_TIMELINE_TAB_PRIORITY;
	}

	/*
	 * (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data)
	{
		//#debug debug
		System.out.println(context+"/"+event+"/"+data);

		if( context == Event.Context.ACTIVITY )
		{
			switch(event)
			{
				case Event.Activities.TIMELINE_CHANGED :
					/*
					//Do NOT automatically fetch new updates, bug 0010229
					int newActivities = ((Integer)data).intValue();
					*/
					requestActivities(this.fetchSize /*+ newActivities*/,false);
					break;
					
				case Event.Activities.TIMELINE_TIMELINE_CHAT_UPDATE:
					
					this.provider.updateTimeLineActivity(data);//data is object of Activity
					
					break;
			}
		}
        else
			super.handleEvent(context, event, data);
	}
	
	//#if polish.blackberry.isTouchBuild == true
	public void commandAction(Command c, Item item) {
        
        if(c == timelineTabCmd)
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
		
		ActivityItem currentActivityItem=null;
		
		if (getCurrentItem() instanceof TimelineActivityItem)
		{
			currentActivityItem=(TimelineActivityItem) getCurrentItem();
			
			profile = currentActivityItem.getProfile();
		}
		
		if(profile==null)
			profile = getModel().getMe();
		
		if( c == cmdOpen && currentActivityItem!=null && profile != getModel().getMe()) //must not open a chat with userself
		{
			com.zyb.nowplus.business.domain.Channel targetChannel=((Profile) profile).getChatChannel(currentActivityItem
					.getActivity().getSource().getNetworkId(),
					currentActivityItem.getActivity().getSourceName());
			
			if(targetChannel==null)
				targetChannel=((Profile)profile).getPrimaryChatChannel();
			
			getController().notifyEvent(Event.Context.CHAT, Event.Chat.OPEN,targetChannel);
		}
		else if(c == cmdOptions)
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, profile);
		else
			super.commandAction(c, d);
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
				//#style .peoplepage_navbar_item_timeline_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_timeline_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_timeline_active
				tabItem.setStyle();
			}
		}
		else if ( isNavBarFocused() )
		{
			
			if( !isTabActive() )
			{
				//#if polish.blackberry.isTouchBuild == true
				//#style .peoplepage_navbar_item_timeline_active_focus
				tabItem.setStyle();
				//#else
				//#style .peoplepage_navbar_item_timeline_focus
				tabItem.setStyle();
				//#endif
			}
			else
			{
				//#style .peoplepage_navbar_item_timeline
				tabItem.setStyle();
			}
		}
	}
	
	//#if polish.blackberry
	public boolean handleShowOptions() {
		commandAction(cmdOptions, this);
		return true;
	}
	//#endif
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BasePeopleForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) {
		
		//#if polish.blackberry
		if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
		{
			commandAction(cmdExit, this); 
			return true;
		}
		//#endif
		
		return super.handleKeyPressed(keyCode, gameAction);
	}
	
	//#mdebug error
	public String toString()
	{
		return "PeopleTabTimeline[]";
	}
	//#enddebug
}
