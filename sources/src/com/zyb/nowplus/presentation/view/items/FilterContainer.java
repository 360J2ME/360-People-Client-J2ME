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

import java.util.Timer;
import java.util.TimerTask;

import com.zyb.nowplus.business.domain.filters.Filter;
import com.zyb.nowplus.presentation.UiFactory;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.util.event.Event;
import javax.microedition.lcdui.Graphics;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Display;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;


/**
 * Container for managing and constructing filter elements. 
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class FilterContainer extends Container
{
	private int currentFilterIndex;
	
	private Filter[] filters;
	
	protected Timer delayedFilterEnvokeTimer;
	
	Container container;
	
	Controller controller;
	
	ItemCommandListener commandListener;
	
	private Displayable nextDisplay;
	
	public void setDisplayable(Displayable d){
		this.nextDisplay = d;
	}
	
	public Displayable getDisplayable(){
		return this.nextDisplay;
	}
	
	/**
	 * Time in millis that should pass before envoking new filter
	 */
	public static final int KEY_INPUT_DELAY_MILLIS = 400;
	
	/**
	 * 
	 * @param filters
	 * @param provider
	 */
	public FilterContainer(Filter[] filters, Container container, Controller controller, ItemCommandListener commandListener)
	{
		this( filters, container, controller, commandListener, null);
	}
	
	/**
	 * 
	 * @param filters
	 * @param provider
	 * @param style
	 */
	public FilterContainer(Filter[] filters, Container container, Controller controller, ItemCommandListener commandListener, Style style)
	{
		super(false, style);
		
		this.commandListener = commandListener;
		
		this.container = container;
		
		this.controller = controller;
		
		setFilters(filters);
	}
		
	/*
	 * @see de.enough.polish.ui.Container#paintContent(int, int, int, int, de.enough.polish.blackberry.ui.Graphics)
	 */
	protected void paintContent(int x, int y, int leftBorder, int rightBorder,
			Graphics g)
	{
		if(!this.isFocused)
			this.defocus(this.getStyle());
		
		super.paintContent(x, y, leftBorder, rightBorder, g);
	}
	
	/**
	 * builds the list settings like order and filter 
	 */
	private void build()
	{
		if(this.filters != null)
		{
			Filter filter;
			for (int i = 0; i < this.filters.length; i++) 
			{
				filter = this.filters[i]; 
				
				Item filterIcon = getIcon(filter);
				//#if polish.blackberry.isTouchBuild == true
					Command filterCmd = new Command(filter.getName(), Command.ITEM, 0);
				//#endif
				//using Item if present, defaulting to filter String representation
				if(filterIcon != null)
				{
					//#if polish.blackberry.isTouchBuild == true
						filterIcon.setDefaultCommand(filterCmd);
						filterIcon.setItemCommandListener(this.commandListener);
					//#endif
					this.add(filterIcon);
					
				}
				else
				{
					String name = filter.getName();
					StringItem item = new StringItem(null,name);
					//#if polish.blackberry.isTouchBuild == true
						item.setDefaultCommand(filterCmd);
						item.setItemCommandListener(this.commandListener);
					//#endif
					this.add(item);
				}
			}
			
			if(this.size() > 0)
				focusChild(0);
		}
	}
	
	public void setCurrentFilterIndex(int filterIndex){
		this.currentFilterIndex = filterIndex;
				
	}
	
	public Filter[] getFilters(){
		return this.filters;
	}
	
	public Filter getSelectedFilter()
	{
		return (currentFilterIndex == -1) ? null : filters[currentFilterIndex];
	}
	
	/**
	 * Returns the icon associated to a filter
	 */
	public Item getIcon(Filter filter)
	{
		if (filter == null)
		{
			return null;
		}
		
		//#style peoplepage_filtercontainer_filter_item_base
		return UiFactory.createFilterNetworkIcon(filter.getName(),filter.getType(),filter.getIconId());
	}	

	//#if polish.blackberry.isTouchBuild == true
		
	 protected boolean handlePointerPressed(int x, int y){
        if(this.isInItemArea(x, y))
            return super.handlePointerPressed(x,y);
        Displayable displayable = this.getDisplayable();
		if(displayable != null){
            Display.getInstance().setCurrent(displayable);
        	
		}
        return false;
    }
	
	
	//#endif
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction)
	{
		if( (Canvas.LEFT == gameAction && Canvas.KEY_NUM4 != keyCode) ||
				(Canvas.RIGHT == gameAction && Canvas.KEY_NUM6 != keyCode) )
		{
			//pass control to view
			if(null != this.getView() && this.getView().handleKeyPressed(keyCode, gameAction))
			{
				int index = this.getFocusedIndex();
				if(index != this.currentFilterIndex)
				{
					
					this.container.focusChild(-1);
					this.currentFilterIndex = index;
					
					//cancel old timer if present
					if(null != delayedFilterEnvokeTimer)
						delayedFilterEnvokeTimer.cancel();
					
					//init new timer
					this.delayedFilterEnvokeTimer = new Timer();
					TimerTask tt = new TimerTask()
					{
						public void run()
						{
							filter();
						}
					};
					//schedule event
					this.delayedFilterEnvokeTimer.schedule(tt, KEY_INPUT_DELAY_MILLIS); 
				}
				
				return true;
			}
		}
		
		return super.handleKeyPressed(keyCode, gameAction);
	}
	
	public void filter() {
		this.controller.notifyEvent(Event.Context.CONTACTS,
				Event.Contacts.FILTER, this);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handleKeyRepeated(int, int)
	 */
	protected boolean handleKeyRepeated(int keyCode, int gameAction)
	{
		return handleKeyPressed(keyCode, gameAction);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Container#handleKeyReleased(int, int)
	 */
	protected boolean handleKeyReleased(int keyCode, int gameAction)
	{
		return super.handleKeyReleased(keyCode, gameAction);
	}
	
	

    
  
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#showNotify()
	 */
	public void showNotify() 
	{
		//#if polish.blackberry.isTouchBuild == false
			AnimationThread.addAnimationItem(this);
			super.showNotify();
		//#endif
		//reset filter
//		focusChild(0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Container#hideNotify()
	 */
	public void hideNotify() 
	{
		//#if polish.blackberry.isTouchBuild == false
			AnimationThread.removeAnimationItem(this);
			super.hideNotify();
		//#endif
	}	
	
	public void releaseResources() 
	{
		//cancel timers
		if(null != delayedFilterEnvokeTimer)
			delayedFilterEnvokeTimer.cancel();

		super.releaseResources();
	}
	
	public void setFilters(Filter[] filters)
	{
		if(null != filters)
		{
			//remove old filters
			this.clear();
			//set new filters
			this.filters = filters;
			//reset index
			this.currentFilterIndex = -1;			
			//rebuild
			build();
			
			//#if polish.blackberry
				UiAccess.defocus(((IconSwapItem)this.get(0)),((IconSwapItem)this.get(0)).getStyle());
			//#endif
		}
	}
	
	//#if polish.blackberry && polish.blackberry.isTouchBuild == false
	 
    public boolean focusChild(int index){      
        ((IconSwapItem)this.get(index)).setTextVisible(true); 
        boolean erg =  super.focusChild(index);
        for(int i = 0; i < this.filters.length; i++){
           UiAccess.focus(this.get(i), 0, this.get(i).getFocusedStyle());
            ((IconSwapItem)this.get(i)).setTextVisible(false);
       }
           ((IconSwapItem)this.get(index)).setTextVisible(true);
       return erg;
   //return true;
        }

          
       protected Style focus(Style focusStyle, int direction) {
              Style erg = super.focus(focusStyle, direction);
              for(int i = 0; i < this.size(); i++){
                   if(this.get(i)!= null)
                	   UiAccess.focus( ((IconSwapItem)this.get(i)), 0,  ((IconSwapItem)this.get(i)).getFocusedStyle());
              }
           return erg;
       }

    public void defocus(Style originalStyle) 
    {
         super.defocus(originalStyle);
         
         //(itemsList.size()==this.filters.length) fixing bug: PeopleTabContacts):/java.lang.IndexOutOfBoundsException: the index [1] is not valid for this list with the size [1].
         //FIX bug #PBLA-956 [ Broken filter bar]
         for (int i = 0; (itemsList.size()==this.filters.length)&&(i < this.filters.length); i++)
        	 UiAccess.defocus(this.get(i),null);           
      }
	
	//#endif
}
