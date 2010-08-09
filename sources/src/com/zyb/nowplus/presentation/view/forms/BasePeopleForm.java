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

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.NavBarContainer;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventListener;
import com.zyb.util.event.Event.App;
import com.zyb.util.event.Event.Context;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Arrays;
import de.enough.polish.util.IntHashMap;
import de.enough.polish.util.Locale;

//#if activate.virtual.listprovider == true
import com.zyb.nowplus.presentation.view.providers.VirtualListProvider;
//#else
//# import com.zyb.nowplus.presentation.view.providers.ListProvider;
//#endif

//#ifdef testversion:defined
import de.enough.polish.util.Debug;
//#endif

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
//#endif


/**
 * Implements traits common to all People Page tabs.
 * <p>
 * Static items common to all tabs are implemented in createStaticItems.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public abstract class BasePeopleForm extends BaseTabForm implements EventListener
{
	/**
	 * Priorities on registered tabs
	 */
	public static final byte PEOPLEPAGE_CONTACTS_TAB_PRIORITY = 0,
    				   PEOPLEPAGE_TIMELINE_TAB_PRIORITY = 1,
					   PEOPLEPAGE_STATUS_TAB_PRIORITY = 2,
					   PEOPLEPAGE_ME_TAB_PRIORITY = 3,
					   PEOPLEPAGE_EMAIL_TAB_PRIORITY = 4;
	/**
	 * Default command common to all people page tab screens
	 */
	public final static Command cmdOptions,cmdOpen,cmdExit;//,cmdHide; hide is removed for now - Jens
	
	static
	{
		cmdOpen = new Command(Locale.get("polish.command.select"), Command.OK, 0);
		cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
		cmdExit = new Command(Locale.get("nowplus.client.java.command.exit"), Command.EXIT, 0);
	}
	
	/**
	 * Title item displaying both screen name and current time
	 */
	protected TitleBarItem titleitem;
	
	/**
	 * Navigation bar container. Visualizes the available tabs in different states.
	 */
	protected static NavBarContainer navBar;
	
	/**
	 * Style states of main navbar
	 */
	public static Style navBarFocusedStyle, navBarUnfocusedStyle, navBarActiveUnfocusedStyle;
	
	/**
	 * List provider of different sorts e.g. ContactsProvider for People screen
	 */
	//#if activate.virtual.listprovider == true
	protected VirtualListProvider provider;
	//#else
	//# protected ListProvider provider;
	//#endif
	
	/**
	 * Item to associated tab. Used to visualize tab.
	 */
	protected Item tabItem;
	
	/**
	 * String to associated tab. Name of tab.
	 */
	protected String tabTitle;
	
	/**
	 * int to keep track of tab index
	 */
	protected int tabIndex;
	
	/**
	 * Flag to help keep track of whether screen change was tab switch or switch to a totally different screen
	 */
	protected static boolean wasTabSwitch = false; 
	
	/**
	 * Flag to help keep track of main navbar focus state
	 */
	protected static volatile boolean focusNavbarAtTabswitch = false; 
	
	  /**
     * Flag to find out the handled key pressed request coming from contacts tab.
     */
	private static boolean reqFromContacts = false;
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param context
	 * @param priority
	 */
	public BasePeopleForm(final Model model, final Controller controller, int context, int priority)
	{
		this(model, controller, context, priority, null);
	}
	
	/**
	 * 
	 * @param model
	 * @param controller
	 * @param view
	 * @param context
	 * @param priority
	 * @param style
	 */
	public BasePeopleForm(final Model model, final Controller controller, int context, int priority, Style style) {
		super(model, controller, context, priority, style);
		
		//#if not polish.blackberry
		this.addCommand(cmdOptions);
		this.addCommand(cmdExit);
		//#endif
		
		if(null == navBar)
		{
			//Fake usage of style to avoid removal during preprocessing
			//#style peoplepage_navbar_active_defocus
			navBar = new NavBarContainer(true);
			
			//#style peoplepage_navbar
			navBar = new NavBarContainer(true);
			navBar.setAppearanceMode(Item.INTERACTIVE);
			navBar.allowCycling = true;
			//navBar.addCommand(cmdFake); //add fake command to suppress key pressing
		}
			
		if(null == titleitem)
			titleitem = new TitleBarItem("",getModel());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#getContext()
	 */
	public byte getContext()
	{
		return Event.Context.CONTACTS;
	}

	/**
	 * Implements BaseForm.createStaticItems()
	 */
	protected Item[] createStaticItems()
	{
		//fetch handles to navbar styles
		if(null == navBarFocusedStyle )
		{
			//#style .peoplepage_navbarfocused
			navBarFocusedStyle =new Style();
			
		}
		if(null == navBarUnfocusedStyle )
		{
			//#style .peoplepage_navbar
			navBarUnfocusedStyle =new Style();
		}
		if(null == navBarActiveUnfocusedStyle )
		{
			//#if polish.blackberry.isTouchBuild == true
					//#style .peoplepage_navbar_active_defocus
					navBarActiveUnfocusedStyle =new Style();
			//#else
					//#style .peoplepage_navbar_active_defocus
					navBarActiveUnfocusedStyle =new Style();
			//#endif
		}
		
		//init navbar style
		forceNavbarActiveDefocus();
		
		return new Item[]{
				titleitem,
				navBar
		};
	}
	
	/**
	 * Registers tab at super class and calls reEvaluateNavBarEntries()
	 * to sort items.
	 */
	protected void registerTab() {
		super.registerTab();
		
		//after adding new tab, make sure it is added to navbar
		reEvaluateNavBarEntries();
	}
	
	/**
	 * Analyses navBar items and sorts them according to priority.
	 */
	private void reEvaluateNavBarEntries()
	{
		if(null != tabForms && null != navBar && null != tabForms.get(this.getContext()))
		{
			IntHashMap tab = (IntHashMap)tabForms.get( this.getContext() );
			int[] keys = tab.keys();
			
			//sort keys according to priority
			if(keys.length > 1)
				Arrays.iQuick(keys, keys.length);
			
			//remove old entries
			navBar.clear();
			
			//add new navbar items
			Object obj;
			for(int i = 0; i < keys.length; ++i)
			{
				//#mdebug debug
				System.out.println("keys[i]: "+keys[i]);
				System.out.println("tab.get(keys[i]): "+tab.get(keys[i]));
				//#enddebug
				
				if(null != (obj = tab.get(keys[i])))
				{
					if(obj instanceof BasePeopleForm && null != ((BasePeopleForm)obj).tabItem)
					{
						tabIndex = navBar.size(); //store index of THIS tab, mainly used for tab switching for BB touch build
						navBar.add(((BasePeopleForm)obj).tabItem);
						navBar.requestInit();						
					}
				}
			}
		}
	}
	
	/**
	 * Sets the Item associated with the tab and calls resort.
	 * @param itm
	 */
	public void setTabItem(Item itm)
	{
		if(null != itm)
		{
			this.tabItem = itm;
			
			//#if polish.blackberry.isTouchBuild == true
			
			this.tabItem.setAppearanceMode(Item.INTERACTIVE);
			
			//#endif
			
			reEvaluateNavBarEntries();
		}
	}
	
	/**
	 * Sets the String associated with the tab and resets the titlebar item
	 * if tab is currently active.
	 */
	public void setTabTitle(String title)
	{
		if(null != title)
		{
			synchronized ( navBar )
			{
				this.tabTitle = title;
				titleitem.setTitle(this.tabTitle);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseTabForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		boolean result = false;
		
		//ensure that keypad keys are supressed
		if(!isKeyPadKey(keyCode, gameAction))
			//let super class take care of tab switching
			result = super.handleKeyPressed(keyCode, gameAction);
		
		//ensure navBar style state
		if( currentlyActiveContainer == topFrame && this.getCurrentItem() == staticTopItems )
			forceNavbarFocus();
		else
		if( currentlyActiveContainer == topFrame && this.getCurrentItem() == topItem ){
			//ensuring contactlist defocus
			if(this.provider != null&& this.reqFromContacts)
				this.provider.reset();
			forceNavbarDefocus();	
		}
		else
		if( currentlyActiveContainer == this.container ){
			forceNavbarActiveDefocus();
			//refresh the list 
			if(this.reqFromContacts)
				this.provider.update(true);
		}
		
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
	
		return super.handleKeyRepeated(keyCode, gameAction);
	}

	/**
	 * Sets the style of the navbar to focused.
	 * Used to switch mode of navbar.
	 */
	public void forceNavbarFocus()
	{
		if(navBar.getStyle() != navBarFocusedStyle)
			navBar.setStyle(navBarFocusedStyle);
		switchTabsStyles();
	}
	
	/**
	 * Sets the style of the navbar to defocused.
	 * Used to switch mode of navbar.
	 */
	public void forceNavbarDefocus()
	{
		if(navBar.getStyle() != navBarUnfocusedStyle)
			navBar.setStyle(navBarUnfocusedStyle);
		switchTabsStyles();
	}	
	
	/**
	 * Sets the style of the navbar to defocused.
	 * Used to switch mode of navbar.
	 */
	public void forceNavbarActiveDefocus()
	{
		if(navBar.getStyle() != navBarActiveUnfocusedStyle)
			navBar.setStyle(navBarActiveUnfocusedStyle);
		switchTabsStyles();
	}	
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#itemStateChanged(de.enough.polish.ui.Item)
	 */
	public void itemStateChanged(Item item)
	{
		super.itemStateChanged(item);
	}
	
	public boolean isNavBarUnfocused() {
		return (navBar.getStyle() == navBarUnfocusedStyle || navBar.getStyle() == navBarActiveUnfocusedStyle);
	}
	
	public boolean isNavBarFocused() {
		return navBar.getStyle() == navBarFocusedStyle;
	}
	
	public boolean isTabActive() {
		//#if polish.blackberry
		return NavBarContainer.nextFocus == tabIndex;
		//#else
		//# return !this.tabItem.isFocused;
		//#endif
	}
	
	/**
	 * Manages the update of the navbar depending on the outcome of super class tab switch.
	 */
	protected boolean switchTab(boolean isRight)
	{
		synchronized ( navBar )
		{
			//flag that last screen change was a tab switch
			wasTabSwitch = true;
			
			//store state of navbar before tabswitch to be able to restore focus after switch
			focusNavbarAtTabswitch = navBar.isFocused;
			
			if(this.provider != null)
				this.provider.reset();
			
			boolean result = super.switchTab(isRight);
			
			if(result)
			{
					//update navbar
					if(isRight)
						UiAccess.handleKeyPressed(navBar, 0, Canvas.RIGHT);
					else
						UiAccess.handleKeyPressed(navBar, 0, Canvas.LEFT);
			}
			return result;
		}
	}
	
	/**
	 * Calls switchTabItemStyle() on all tabs currently registered to this context.
	 * <p>
	 * Ensures that items in navBar changes style state according to navBar style state.
	 */
	private void switchTabsStyles()
	{
		if(null != tabForms && null != navBar && null != tabForms.get(this.getContext()))
		{
			IntHashMap tab = (IntHashMap)tabForms.get( this.getContext() );
			int[] keys = tab.keys();
			
			Object obj;
			for(int i = 0; i < keys.length; ++i)
			{
				//#mdebug debug
				System.out.println("keys[i]: "+keys[i]);
				System.out.println("tab.get(keys[i]): "+tab.get(keys[i]));
				//#enddebug
				
				if(null != (obj = tab.get(keys[i])))
				{
					if(obj instanceof BasePeopleForm)
						((BasePeopleForm)obj).switchTabItemStyle();						
				}
			}
		}
	}
	
	/**
	 * Switches the style of the tabItem to adapt to the current navbar mode.
	 * <p>
	 * Extending classes must implement this and manage what style to switch to.
	 */
	abstract protected void switchTabItemStyle();
	
	/**
	 * Makes sure that provider is kicked as this Tab is display on screen
	 */
	public void showNotify()
	{
		//#debug debug
		System.out.println("showNotify()");
		
		//Let super class handle tab switch 
		super.showNotify();
		
		//update titlebar
		if( activeTab instanceof BasePeopleForm )
		{
			//#debug debug
			System.out.println("updating title");
			
			setTabTitle(((BasePeopleForm)activeTab).tabTitle);
		}
		
		//init provider if any
		if(null != this.provider)
			this.provider.initView();
		
		//ensure that focus routines are only run at tab switching
		if(wasTabSwitch)
		{
			//Ensure that navbar stays focused at tab switch
			if(focusNavbarAtTabswitch)
			{
				//focus top frame
				this.setActiveFrame(Graphics.TOP);
				//focus 'staticTopItems' container
				if(this.currentlyActiveContainer.size()>0)
					this.currentlyActiveContainer.focusChild(0);
				//ensure contactlist defocus
				if(this.provider != null)
					this.provider.reset();
				//ensure navbar state
				forceNavbarFocus();
			}
			else
			{
				//focus content container
				this.setActiveFrame(-1);
				//focus first element in list
				if(this.currentlyActiveContainer.size()>0)
					this.currentlyActiveContainer.focusChild(0);
				//ensure navbar state
				forceNavbarActiveDefocus();			
			}
			wasTabSwitch = false;
		}
		
		if(	navBar.getFocusedIndex() == -1
			&& navBar.size() > 0)
		{
			navBar.focusChild(0);
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
		
		if(c == cmdExit)
		{
			this.getController().notifyEvent(Context.APP, App.EXIT, null);
		}
		else
			super.commandAction(c, d);
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
	 * @see de.enough.polish.ui.Screen#keyPressed(int)
	 */
	public void keyPressed(int keyCode) 
	{
		 if (Keypad.key(keyCode) == Keypad.KEY_ESCAPE)
			commandAction(cmdExit, this);
		else
			super.keyPressed(keyCode);
	}
	//#endif

	/* The following methods are a quick fix
	 * for PBLA-898. A more permanent fix is needed in the future.
	 */
	int focusedItemIndex = 0;
    Style focusedStyle = null;
    
    // Overwrite handlePointerPressed() so we can keep track of the currently selected
    // tab and tab style.
    protected boolean handlePointerPressed(int x, int y)
    {
        focusedItemIndex = navBar.getFocusedIndex() ;
        focusedStyle = navBar.getFocusedChild().getStyle();
        return super.handlePointerPressed(x, y);
    }
    
    // Overwrite handlePointerReleased so that, if the release event takes place outside of
    // the currently focused tab (the tab on which the press event originated), the previous
    // tab and tab style will be restored.
    protected boolean handlePointerReleased(int x, int y) {
        
        if ( ( y > ( navBar.relativeY + navBar.getItemAreaHeight() ) ) ||
            ( y < navBar.relativeY ) ||
            ( x < navBar.getFocusedChild().relativeX ) ||
            ( x > ( navBar.getFocusedChild().relativeX + navBar.getFocusedChild().itemWidth ) )
            )
        {
            if (focusedItemIndex != navBar.getFocusedIndex()) {
                navBar.focusChild(focusedItemIndex) ;
                navBar.getFocusedChild().setStyle(focusedStyle);
            }
        }
        
        return super.handlePointerReleased(x, y);
    }
    
    /**
	 *  To set the flag if the request of handle key pressed coming from contacts.
	 */
	public void setFromContacts(boolean value){
		this.reqFromContacts = value;
	}
}
