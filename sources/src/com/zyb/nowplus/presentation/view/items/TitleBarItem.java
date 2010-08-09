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
package com.zyb.nowplus.presentation.view.items;

import java.io.IOException;

import com.zyb.nowplus.business.Model;

import de.enough.polish.ui.Animatable;
import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.ClippingRegion;
import de.enough.polish.ui.ClockItem;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.StyleSheet;

/**
 * Displays screen title, the current time and active natworking icons.
 * Used in most screens.
 *  
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class TitleBarItem extends Container implements Animatable
{
	private Container iconList;
	
	/**
	 * Right aligned clock item
	 */
	private ClockItem clock;
	
	/**
	 * Center title item
	 */
	private StringItem title;
	
	/**
	 * Left aligned activity icons
	 */
	private IconItem offlineIcon;
	private AnimatedIconItem syncIcon;
	
	/**
	 * Model reference
	 */
	private Model model;

	private long lastSync;

	public TitleBarItem(String title, Model model)
	{
		//#style title_bar_base
		super(false);
		
		this.model = model;
		
		//#style title_bar_icon_container
		this.iconList = new Container(false);
		this.iconList.setAppearanceMode(Item.PLAIN);
		
		//#style title_bar_title
		this.title = new StringItem("",title);
		this.title.setAppearanceMode(Item.PLAIN);
		
		//fill clock container
		
		//#style title_bar_clock
		this.clock = new ClockItem(null);
		this.clock.setAppearanceMode(Item.PLAIN);
		
		//add wrappers
		add(this.iconList);
		add(this.title);
		

		//#ifndef polish.device.requires.noclock
			add(this.clock);
		//#endif
			
		setAppearanceMode(Item.PLAIN);
	}
	
	/**
	 * Update title of titlebar.
	 * 
	 * @param title
	 */
	public void setTitle(String title)
	{
		this.title.setText(title);
		this.repaint();
	}
	
	public void showNotify()
	{
		checkIcons(true);
		AnimationThread.addAnimationItem(this);
		super.showNotify();
	}

	public void hideNotify()
	{
		checkIcons(true);
		AnimationThread.removeAnimationItem(this);
		super.hideNotify();
	}
	
	public boolean animate() {
		if (!((model.getFeedback() & (Model.FEEDBACK_NAB_INTERACTION | Model.FEEDBACK_SAB_INTERACTION)) == 0))
		{
			return true;
		} else {
			return false;
		}
	}
	
	public void animate(long currentTime, ClippingRegion repaintRegion) {
		checkIcons(false);
		
		super.animate(currentTime, repaintRegion);
	}

	/**
	 * @param b
	 */
	private void checkIcons(boolean suppressAnimation) {
		int feedback = model.getFeedback();
		
		//#debug debug
		System.out.println("feedback:"+feedback);
		
		if ((feedback & Model.FEEDBACK_CONNECTION_DOWN) == 0)
			removeOfflineIcon(suppressAnimation);
		else
			addOfflineIcon();
		
		if (!((feedback & (Model.FEEDBACK_NAB_INTERACTION | Model.FEEDBACK_SAB_INTERACTION)) == 0))
			addSyncIcon();
		
		//Test if it is time to remove the sync icon
		synchronized(this)
		{
			if(System.currentTimeMillis()-lastSync > 3000)//3 seconds
				removeSyncIcon();
		}
	}

	/**
	 * 
	 */
	private void removeSyncIcon() {
		synchronized(this)
		{
			if(syncIcon != null)
			{
				//#debug info
                System.out.println("Removing");
				this.iconList.remove(syncIcon);
				syncIcon = null;
			}
		}
	}

	
	private void addSyncIcon()
	{
		synchronized(this)
		{
			if(syncIcon == null)
			{
				//#style title_bar_activity_item
				syncIcon = new AnimatedIconItem(
						100,
						new String[]{"/sync_01_now6_40_13x13.png","/sync_02_now6_40_13x13.png","/sync_03_now6_40_13x13.png","/sync_04_now6_40_13x13.png"},
						null 
						);
				
				//#if polish.blackberry
					syncIcon.setAppearanceMode(Item.PLAIN);
				//#endif
	
				//#debug debug
				System.out.println("Adding");
				this.iconList.add(syncIcon);
			}
			lastSync = System.currentTimeMillis();
		}
	}
	
	
	private void addOfflineIcon()
	{
 		synchronized(this)
		{
        	if (offlineIcon == null) 
            {
                try 
                {
                    //#style title_bar_activity_item
                    offlineIcon = new IconItem( null, StyleSheet.getImage("/offline_13x15.png",null,true));
                    offlineIcon.setAppearanceMode(Item.PLAIN);
                    this.iconList.add(offlineIcon);
                } 
                catch (IOException e) 
                {
            		//#debug error
            		System.out.println("Failed to create offline icon" + e);
                }
            }
		}

	}
	
	private void removeOfflineIcon(boolean suppressAnimation)
	{
		synchronized(this)
		{
          if (offlineIcon != null) 
          {
        	  //TODO was this a test ? the paint thread 
        	  //was blocked by this for it 500 ms which 
        	  //resulted in the now infamous filter bug
        	  //on blackberry after reconnect
              /*if(!suppressAnimation)
              {
	        	  EventManager.fireEvent( "move-up-and-out", offlineIcon, null );
	              try{Thread.sleep(ANIMATIONSPEED);}catch(Exception e){}
              }*/
              this.iconList.remove(offlineIcon);
          }
		}
	}

	
	/**
	 * returns the current titlebar title
	 */
	public String getTitle()
	{
		return this.title.getText();
	}

	/* (non-Javadoc)
	 * @see com.zyb.util.event.EventListener#getContext()
	 */
	public byte getContext() {
		return -1;
	}

	//#mdebug error
    public String toString()
    {
    	return "TitleBarItem[]";
    }
    //#enddebug
    

}
