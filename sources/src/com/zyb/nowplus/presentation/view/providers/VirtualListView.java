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
package com.zyb.nowplus.presentation.view.providers;

import com.zyb.nowplus.business.domain.ListSelection;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.ClippingRegion;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.ContainerView;
import de.enough.polish.ui.DebugHelper;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.ArrayList;

public class VirtualListView extends ContainerView
{
	static int SCROLLING_INTERVAL = 500;
	
	static int UPDATE_INTERVAL = 500;

	transient VirtualListProvider provider;
	
	transient VirtualListRange range;
	
	transient Container container;
	
	int bufferSize;
	
	ArrayList activeItems;
	
	boolean firstAnimate = true;
	
	long lastNoticationOffset = -1;
	
	long lastScrollOffset = -1;
	
	long lastScrollTime = -1;
	
	boolean updateRequested = false;
	
	long lastUpdateTime = -1;
	
	public VirtualListView(VirtualListProvider provider, int bufferSize)
	{
		this.provider = provider;
		this.container = this.provider.getContainer();
		this.bufferSize = bufferSize;
		this.activeItems = new ArrayList();
	}
	
	public VirtualListRange getRange() {
		return this.range;
	}
	
	/**
	 * Returns the height of the first item
	 * 
	 * @return the height
	 */
	int getReferenceHeight(int width) {
		Item sampleItem = this.provider.createItem(this.provider.getSampleData(), null);
		return sampleItem.getItemHeight(width,width);
	}	
	
	public int getDirection(long previousOffset, long currentOffset) {
		if(previousOffset > currentOffset) {
			return Canvas.DOWN;
		} else {
			return Canvas.UP;
		}
	}
    
    public boolean isScrolling(int scrollOffset) {
            return ( this.lastScrollOffset == -1 || this.lastScrollTime == -1 || this.lastScrollOffset != scrollOffset);
    }
    
    protected void requestUpdate() {
    	this.updateRequested = true;
    }
    
    protected void reset() {
    	this.lastNoticationOffset = -1;
    	
    	this.lastScrollOffset = -1;
    	
    	this.lastScrollTime = -1;
    	
    	this.updateRequested = false;
    	
    	this.lastUpdateTime = -1;
    }
    
	/* (non-Javadoc)
     * @see de.enough.polish.ui.ItemView#animate(long, de.enough.polish.ui.ClippingRegion)
     */
    public void animate(long currentTime, ClippingRegion repaintRegion)
    {
    	  try
          {
                  if(null != this.container)
                  {
                	 // get the scroll offsets
                          int scrollOffset = Math.abs(this.container.getScrollYOffset());
                          
                          // if the last update is more than 500 ms ago ...
                          if( this.updateRequested && currentTime - this.lastUpdateTime > UPDATE_INTERVAL) {
                        	  this.updateRequested = false;
                        	  this.lastUpdateTime = currentTime;
                        	  
                        	  // update the list
                    		  this.provider.update(true);
                    		  UiAccess.init(this.container, this.container.getAvailableWidth(), this.container.getAvailableWidth(), this.container.getAvailableHeight());
                          }
                          
                          // if scrolling ...
                          if( isScrolling(scrollOffset) ) {
                                  if(null != this.range && null != this.provider)
                                  {   
                                	      /*      if the scroll direction is DOWN (offset is decreased) and the scroll offset
                                           *      is below the current range 
                                           *      or 
                                           *      the scroll direction is UP (offset is increased) 
                                           *      and the scroll offset is above the current range
                                           */
                                	  if( this.range.belowRange(scrollOffset) || this.range.overRange(scrollOffset) || this.firstAnimate) 
                                          {
                                                  // get the scroll direction
                                                  int direction = getDirection(this.lastScrollOffset, scrollOffset);
                                  
                                                  // update the range
                                                  this.range.update(scrollOffset, direction, this.provider.total());
                                                  
                                                  // select for the new range
                                                  ListSelection selection = this.provider.select(this.range);
                                                  
                                                  // apply the selection
                                                  this.provider.apply(null,selection.getEntries(), selection.size(),null,true);
                                                  
                                                  UiAccess.init(this.container, this.container.getAvailableWidth(), this.container.getAvailableWidth(), this.container.getAvailableHeight());
                                                  
                                                  this.firstAnimate = false;
                                          }
                                  }
                                  
                                  this.lastScrollTime = currentTime; 
                                
                                // notify the inactive items
                                notifyInactiveItems();
                          } else {
                        	  if(currentTime - this.lastScrollTime > SCROLLING_INTERVAL && this.lastNoticationOffset != scrollOffset) {
                    		      // notify the active items
                                  notifyActiveItems();
                                  this.lastNoticationOffset = scrollOffset;
                        	  }
                          }
                          
                          this.lastScrollOffset = scrollOffset;
                	  }
          }
          catch(Exception e)
          {
        	 e.printStackTrace();
             DebugHelper.trace();
          }
    }
	
	/**
	 * Initializes the range
	 * @param availWidth the available width
	 */
	public void initRange(int availWidth, int availHeight) {
		if(this.range == null) {
			this.range = new VirtualListRange(getReferenceHeight(availWidth),availHeight,this.bufferSize);
			this.range.setRange(0, -1, this.provider.total());
		}
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#initContent(de.enough.polish.ui.Item, int, int, int)
	 */
	protected void initContent(Item parentContainerItem, int firstLineWidth,
			int availWidth, int availHeight) {
		//#debug debug
		System.out.println("initialising content");
		
		super.initContent(parentContainerItem, firstLineWidth, availWidth, availHeight);
		
		initRange(availWidth,availHeight);
		initListContent(this.range);
	}

	/**
	 * Adjusts the scroll offset
	 */
	/*protected void setScrollOffset(Container container, VirtualListRange range)
	{
		if(range.getOffset() != -1)
		{
			Item item = container.getFocusedItem();
			int index = container.getFocusedIndex();
			
			int visibleHeight = range.getAvailableHeight();
			int listHeight = range.getTotalHeight();
			int referenceHeight = range.getReferenceHeight();
			
			int yOffset = 0;
			
			if(listHeight > visibleHeight && this.container.getFocusedIndex() != 0)
			{
				if(index == this.container.size() - 1)
				{
					yOffset = (listHeight - visibleHeight) * -1;
				}
				else if(item != null && item.relativeY > range.getOffset())
				{
					yOffset = -item.relativeY + range.getOffset();
					
					if(item.relativeY > 0 && range.getOffset() == -1)
					{
						yOffset = -item.relativeY + referenceHeight;
					}
				}
			}
			
			this.parentContainer.setScrollYOffset(yOffset,false);
		}
	}*/
	
	/**
	 * Initializes the container settings to simulate a list consisting of the
	 * total count of entries
	 * 
	 * @param range
	 *            the range to initialize the container 
	 */
	void initListContent(VirtualListRange range) {
		//#debug debug
		System.out.println("initialising list content");

		int height = range.getReferenceHeight();
		int start = range.getStart();

		int itemOffset = height * start;
			
		for (int i = 0; i < this.parentContainer.size(); i++) {

			Item item = this.parentContainer.get(i);
			item.relativeY = itemOffset;
				
			itemOffset += height;
		}
		
		this.contentHeight = range.getTotalHeight();
		
		//#debug debug
		System.out.println("setting content height to " + this.contentHeight);
	}
	

	/**
	 * Notifies active items
	 */
	public void notifyActiveItems()
	{
		for (int i = 0; i < this.container.size(); i++) {
			Item item = this.container.get(i);
			if(isItemShown(item))
			{
				this.provider.notify(item, true);
				this.activeItems.add(item);
			}
		}
	}
	
	/**
	 * Notifies inactive items
	 */
	public void notifyInactiveItems()
	{
		for (int i = 0; i < this.activeItems.size(); i++) {
			Item item = (Item)this.activeItems.get(i);
			if(!isItemShown(item)) {
				this.provider.notify(item, false);
				this.activeItems.remove(i);
				i--;
			}
		}
	}
	
	/**
	 * Returns true if an item is in the visible area of the managed container
	 * @param item the item
	 * @return true if an item is in the visible area of the managed container otherwise false
	 */
	public boolean isItemShown(Item item)
	{
		int verticalMin = this.container.getAbsoluteY() + Math.abs(this.container.getScrollYOffset());
		int verticalMax = verticalMin + this.container.getScrollHeight();
		
		int itemTop= item.getAbsoluteY();
		int itemBottom = item.getAbsoluteY() + item.itemHeight;
		
		return !(itemBottom < verticalMin || itemTop > verticalMax);
	}

	protected boolean isVirtualContainer() {
		return true;
	}
	
	
}
