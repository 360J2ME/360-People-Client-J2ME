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

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
import com.zyb.nowplus.presentation.BlackBerryOptionsHandler;
//#endif
import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.NavBarContainer;
import com.zyb.nowplus.presentation.view.items.SearchFieldItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
import de.enough.polish.util.Arrays;
import de.enough.polish.util.IntHashMap;

/**
 * Base abstraction for all screens that employ tabs.
 * <p>
 * Handles to all active tabs are maintained in static member tabForms.
 * References to active tabs are registered automatically  at initilization
 * and also automatically deregiserted when 'cmdBack' is called. Method 
 * releaseResources() are called for all Screen type tabs in the process 
 * of deregistering.
 * <p>
 * Grouping of tabs are done using the context of the forms. Forms that 
 * share context will hence be grouped.
 * <p>
 * Presedence of a grouping of tabs are done using the getPriority() method.
 * Lowest value have higest presedence [0..127]
 * <p>
 * Switching of tabs is handled by method switchTab(). This method is to be
 * overwritten by extending classes if a tab switch is to trigger some event.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 *
 */
public abstract class BaseTabForm extends BaseFramedForm 
//#if polish.blackberry
implements BlackBerryOptionsHandler
//#endif
{
	/**
	 * hashmap of tab references sort by context and priority. 
	 */
	protected static IntHashMap tabForms;
	protected static final byte INITIAL_NUM_TABS = 8;
	
	/**
	 * Currently active/focused/selected tab
	 */
	protected volatile static BaseTabForm activeTab;
	
	public BaseTabForm(final Model model, final Controller controller, int context, int priority)
	{
		this(model, controller, context, priority, null);
	}
	
	public BaseTabForm(final Model model, final Controller controller, int context, int priority,  Style style)
	{
		super(model, controller, null,style);
		
		registerTab();
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction)
	{
		//only handle key input if current is shown on screen
		if(this == activeTab)
		{
			if( handleTabSwitch(keyCode, gameAction) )
				return true;
			else
				return super.handleKeyPressed(keyCode, gameAction);
		}
		else
			return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#handleKeyRepeated(int, int)
	 */
	protected boolean handleKeyRepeated(int keyCode, int gameAction) {
		//enable continuous tab switching
		return handleKeyPressed(keyCode, gameAction);
	}
	
	//#if polish.blackberry
	protected boolean handleKeyReleased(int keyCode, int gameAction)
    {
		if(Keypad.key(keyCode) == Keypad.KEY_MENU){
			if(this.handleShowOptions()){
				return true;
			}
		}
		return super.handleKeyReleased(keyCode, gameAction);
    }
	//#endif
	
	/**
	 * Handles routines associated with switching tabs based on key input.
	 * 
	 * @param keyCode
	 * @param gameAction
	 * @return
	 */
	protected boolean handleTabSwitch(int keyCode, int gameAction) 
	{
		boolean tabSwitch = false;
		
		//handle leftwards or rightwards tab switch
		if(Canvas.LEFT == gameAction && Canvas.KEY_NUM4 != keyCode){
			//This nasty hack is NOT ok, cycling should be handled using polish.FramedForm.allowCycling in variables section of build.xml
//			//#if polish.blackberry
//			if(activeTab.getPriority() != BasePeopleForm.PEOPLEPAGE_CONTACTS_TAB_PRIORITY)
//			//#endif	
				tabSwitch = switchTab(false);
		}
		else
		if(Canvas.RIGHT == gameAction && Canvas.KEY_NUM6 != keyCode)
		{
			//This nasty hack is NOT ok, cycling should be handled using polish.FramedForm.allowCycling in variables section of build.xml
//			//#if polish.blackberry
//			if(activeTab.getPriority() != BasePeopleForm.PEOPLEPAGE_ME_TAB_PRIORITY)
//			//#endif	
				tabSwitch = switchTab(true);
			
		}
		if(tabSwitch)
		{
			//notify controller of view change
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.SWITCH, getActiveTab());
		}
		
		return tabSwitch;
	}

	/**
	 * Switches currently displayed tabs to the next tab in the same context group.
	 * 
	 * @param isRight true if tab switch is rightwards, false if leftwards
	 * @return true if tab switch was correctly processed, false otherwise
	 */
	protected boolean switchTab(boolean isRight)
	{
		if( null == tabForms || tabForms.get(this.getContext()) == null || ((IntHashMap)tabForms.get(this.getContext())).size() <= 1)
			return false;
		
		//fetch IntHashMap of tabs relevant to this context
		IntHashMap tabs = (IntHashMap)tabForms.get(this.getContext());
		
		//get and sort keys
		int[] keys = tabs.keys();
		Arrays.iQuick(keys, keys.length);
		
		//linear search for occurrence of this priority
		int i;
		for(i = 0; i < keys.length;++i)
			if(keys[i] == this.getPriority())
				break;
		
		//find next tab based on sorted keys
		if(isRight)
		{
			while(null == tabs.get(keys[i]) || this == tabs.get(keys[i]))
			{
				if( i + 1 < keys.length )
					++i;
				else
					i = 0;
			}
		}
		else
		{
			while(null == tabs.get(keys[i]) || this == tabs.get(keys[i]))
			{
				if( i - 1 >= 0 )
					--i;
				else
					i = keys.length - 1;
			}
		}
		
		//update active tab
		activeTab = (BaseTabForm) tabs.get(keys[i]);
		
		//#if polish.blackberry
			NavBarContainer.nextFocus = i;
		//#endif
		
		//#mdebug debug
		System.out.println("tab priority: "+keys[i]);
		System.out.println("activeTab: "+activeTab);
		//#enddebug
		
		return true;
	}
	
	/**
	 * Switching to a tab by index
	 * 
	 * @param next the index of the tab
	 * @return true if the tab with the content has been switched else false
	 */
	public boolean switchTabByIndex(int next) {
		if (null == tabForms || tabForms.get(this.getContext()) == null
				|| ((IntHashMap) tabForms.get(this.getContext())).size() <= 1)
			return false;

		// fetch IntHashMap of tabs relevant to this context
		IntHashMap tabs = (IntHashMap) tabForms.get(this.getContext());

		// get and sort keys
		int[] keys = tabs.keys();
		Arrays.iQuick(keys, keys.length);

		// update active tab
		activeTab = (BaseTabForm) tabs.get(keys[next]);
		
		//flag that last screen change was a tab switch
		BasePeopleForm.wasTabSwitch = true;
		
		//store state of navbar before tabswitch to be able to restore focus after switch
		BasePeopleForm.focusNavbarAtTabswitch = BasePeopleForm.navBar.isFocused;

		// notify controller of view change
		getController().notifyEvent(Event.Context.NAVIGATION,
				Event.Navigation.SWITCH, getActiveTab());

		return true;
	}
	
	
	/**
	 * Registers tab in hashmap based on context and priority
	 */
	protected void registerTab()
	{
		if(null == tabForms)
			tabForms = new IntHashMap(INITIAL_NUM_TABS);
		
		IntHashMap tabNums;
		if( tabForms.get(this.getContext()) != null)
		{
			//add tab to existing
			tabNums = (IntHashMap) tabForms.get( this.getContext() );
			tabNums.put(this.getPriority(), this);
		}
		else
		{
			//init sub tab and add new 
			tabNums = new IntHashMap(INITIAL_NUM_TABS);
			tabNums.put(this.getPriority(), this);
			tabForms.put(this.getContext(), tabNums);
			
			//reset active tab as tab group reinitiated
			activeTab = this;
		}
	}
	
	/**
	 * Deregisters tab in hashmap based on context and priority
	 */
	protected void deregisterTab() 
	{
		if(null != tabForms)
		{
			IntHashMap tabNums;
			if( tabForms.get( this.getContext() ) != null)
			{
				//set value reference null
				tabNums = (IntHashMap) tabForms.get( this.getContext() );
				tabNums.remove( this.getPriority() );
			}
		}
	}

	/*
	 * Releases resources and deregisters tab reference in hashmap
	 * 
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#releaseResources()
	 */
	public void releaseResources()
	{
		/* 
		 * Remember to lock Screen.paintLock to block painting routines
		 * while resources are being released
		 */
		synchronized (getPaintLock())
		{
			//call release methods on all tabs of current context
			if(tabForms != null)
			{
				Object obj;
				if( (obj = tabForms.get(getContext())) != null )
				{
					IntHashMap tabNums = (IntHashMap) obj;
					int[] keys = tabNums.keys();
					Screen scr = null;
					
					//remove from context hashmap before calling release on other tabs
					tabForms.remove( this.getContext() );	
					
					//call release for all context tabs
					for(int i = keys.length; --i >= 0;)
					{
						scr = (Screen) tabNums.get(keys[i]);
						if(scr != null && scr != this)
							scr.releaseResources();
					}
				}
			}
		}
		
		super.releaseResources();
	}
	
	/*
	 * Releases resources and deregisters tab reference in hashmap
	 * 
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#destroy()
	 */
	public void destroy()
	{
		/* 
		 * Remember to lock Screen.paintLock to block painting routines
		 * while resources are being released
		 */
		synchronized (getPaintLock())
		{
			//call destroy methods on all tabs of current context
			if(tabForms != null)
			{
				Object obj;
				if( (obj = tabForms.get(getContext())) != null )
				{
					IntHashMap tabNums = (IntHashMap) obj;
					int[] keys = tabNums.keys();
					Screen scr = null;
					
					//remove from context hashmap before calling release on other tabs
					tabForms.remove( this.getContext() );	
					
					//call release for all context tabs
					for(int i = keys.length; --i >= 0;)
					{
						scr = (Screen) tabNums.get(keys[i]);
						if(scr != null && scr != this)
							scr.destroy();
					}
				}
			}
		}
		
		super.destroy();
	}
	
	/**
	 * Returns a handle to currently active tab
	 */
	protected BaseTabForm getActiveTab()
	{
		return activeTab;
	}
	
	/**
	 * The priority of the tab. Must be implemented by extending classes.
	 * @return byte the priority of the tab
	 */
	public abstract byte getPriority();
		
	/**
	 * Tries to retrive a handle to a BaseTabForm instance based on
	 * context and priority.
	 * 
	 * @param context
	 * @param priority
	 */
	public static BaseTabForm getTab(int context, int priority)
	{
		if(null != tabForms && null != tabForms.get(context))
		{
			IntHashMap group = (IntHashMap)tabForms.get(context);
			if(null != group.get(priority))
				return (BaseTabForm)group.get(priority);
			else
				return null;
		}
		else
			return null;
	}
	
	public void showNotify() 
	{
		//#mdebug debug
		if(activeTab == null)
			System.out.println("activeTab == null");
		else
			System.out.println("tabForms.get( "+activeTab.getContext()+" ):"+tabForms.get( activeTab.getContext() ));
		//#enddebug
		
		//update active tab if needed
		//has currently active tab context been released and should hence be replaced/updated??
		if( null != activeTab && null == tabForms.get( activeTab.getContext() ))
		{
			//#debug debug
			System.out.println("showNotify()");

			activeTab = this;
		}
		
		super.showNotify();
	}
	
	//#if polish.blackberry.isTouchBuild == true
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#focus(int, boolean)
	 */
	public void focus(Item item, boolean force) {
		//for searchfield it is possible to focus the Textfield
		if(item instanceof TextField && item.getParent() != null && item.getParent() instanceof SearchFieldItem)
			super.focus(item, force);
		
		// ignore focusing of top frame items (which are tabs and should not be selected upon touch)
		else{
			Item parent = item;
			while (parent.getParent() != null) {
				parent = parent.getParent();
			}
			if (parent == getRootContainer()) {
				super.focus(item, force);
			}
		}
	}

	//#endif
}
