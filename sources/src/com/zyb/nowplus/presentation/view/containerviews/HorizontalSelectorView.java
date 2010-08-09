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
package com.zyb.nowplus.presentation.view.containerviews;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.ClippingRegion;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.ContainerView;
import de.enough.polish.ui.Dimension;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.UiAccess;

/**
 * A ContainerView that displays items in a horizontal layout. Items scroll 
 * horizontally in in accordance to current item selection. An animated 'seeker'
 * follows the currently focused item in the background.
 * <p>
 * According to spec: http://wiki.zyb.local/index.php?title=R1_Sprint1#Main_menu_item 
 * <p>
 * Custom CSS attributes:
 * horizontal-padding-arrows: distance for carousel horizontal bounds to 'arrow images'
 * left-arrow-image: image used for lefthand arrow representation
 * right-arrow-image: image used for righthand arrow representation
 * <p>
 * NOTE NOTE NOTE:
 * This class is designed to handle content generically. The UE specs this ContainerView
 * implements are very specific and were developed through several iterations after usability
 * tests during March-May 2009. Therefore DO NOT HACK / ALTER / MODIFY this class as doing so
 * breaks the clients developed in that period. If you want device specific behavior, then
 * extend this class or create a new ContainerView from scratch. /Anders Bo Pedersen
 *  
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class HorizontalSelectorView extends ContainerView
{
	/*
	 * custom css members
	 */
	protected Image leftArrow, rightArrow;
	protected Dimension arrowPadding;
	protected Style seekAndDestroyStyle;
	
	/*
	 * animated seeker item
	 */
	protected transient Item seekAndDestroy;
	
	/*
	 * Local measure & animation members
	 */
	protected int absoluteCarouselWidth = 0, visibleItemsWidth = 0;
	protected int horizontalCarouselOffset,animatedHorizontalCarouselOffset;
	protected int horsCenterAdjustOffset = 0;
	
	/**
	 * Flag used to disable seeker animation when view is not focused
	 */
	private boolean isSeekerAnimationActive = false;
	
	/**
	 * Counter used to check if update should be called
	 */
	private int lastItemCount = 0;
	
	/**
	 * Side paddings as result of presence of arrows
	 */
	private int leftPad,rightPad;
	
	/**
	 * Flag to help keep track of init state
	 */
	private boolean firstInit = false;
	
	public HorizontalSelectorView()
	{
		super();
		
		
		//do not call in initContent()
		this.isHorizontal = true;
		this.isVertical = false;
		this.allowCycling = true;
		this.allowsAutoTraversal = false;
		this.allowsDirectSelectionByPointerEvent = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#initContent(de.enough.polish.ui.Item, int, int, int)
	 */
	protected void initContent(Item parentContainerItem, int firstLineWidth,
			int availWidth, int availHeight)
	{
		super.initContent(parentContainerItem, firstLineWidth, availWidth, availHeight);
		
		//setting default
		this.contentWidth = availWidth;
		this.contentHeight = 0;
		
		//initiating relative positions
		Item[] myItems = ((Container)parentContainerItem).getItems();
		Item item;
		for (int i = 0; i < myItems.length; i++)
		{
			item = myItems[i];
			item.relativeX = i * (item.getItemWidth(firstLineWidth, availWidth) + this.paddingHorizontal);
			item.relativeY = 0;
			
			this.contentHeight = Math.max(this.contentHeight, item.getItemHeight(firstLineWidth, availWidth, availHeight));
		}
		
		//init seeker item
		this.seekAndDestroy = new StringItem(null, "", this.seekAndDestroyStyle);
		this.seekAndDestroy.setAppearanceMode(Item.PLAIN);
		this.seekAndDestroy.setItemHeight(this.contentHeight);
		
		lastItemCount = 0;
		
		//running analysis routines
		if(!firstInit)
		{
			recalcAbsoluteCarouselWidth(((Container)parentContainerItem).getItems());
			evaluateHorizontalOffsetAnimation(0);
			firstInit = true;
		}
	}

	/**
	 * Calculated total width of all held items.
	 * <p>
	 * Used for animation and general painting routines.
	 * 
	 * @param items
	 */
	private synchronized void recalcAbsoluteCarouselWidth(Item[] items)
	{
		//has number of items changed?
		if(null != this.parentContainer && this.lastItemCount != this.parentContainer.size())
		{
			this.lastItemCount = this.parentContainer.size();
			
			//define available width based on presence of arrows
			this.leftPad = (this.leftArrow != null ? this.leftArrow.getWidth() + this.arrowPadding.getValue(this.contentWidth): 0);
			this.rightPad = (this.rightArrow != null ? this.rightArrow.getWidth() + this.arrowPadding.getValue(this.contentWidth): 0);
			int availWidth = this.contentWidth - this.leftPad - this.rightPad;
			int avgItemWidth = 0;
			
			//calculate absolute width
			this.absoluteCarouselWidth = -this.paddingHorizontal;
			Item itm=null;
			for(int i = 0; i < items.length; i++)
			{
				itm = items[i];
				if(itm != null)
				{
					this.absoluteCarouselWidth += itm.getItemWidth(availWidth, availWidth) + this.paddingHorizontal;
					avgItemWidth += itm.getItemWidth(availWidth, availWidth);					
				}
			}
			
			avgItemWidth = ((avgItemWidth << 10) / items.length) >> 10;
			
			//update hors centering offset
			this.visibleItemsWidth = -this.paddingHorizontal;
			while(this.visibleItemsWidth <= (availWidth - (avgItemWidth + this.paddingHorizontal)) )
				this.visibleItemsWidth += avgItemWidth + this.paddingHorizontal;

			this.horsCenterAdjustOffset = (availWidth - (this.visibleItemsWidth > this.absoluteCarouselWidth ? this.absoluteCarouselWidth : this.visibleItemsWidth) >> 1);
			
			//fix animation if items width less than available width
			if(this.absoluteCarouselWidth < availWidth)
			{
				this.horizontalCarouselOffset = 0;
				this.animatedHorizontalCarouselOffset = this.horizontalCarouselOffset;
			}
			
			//#mdebug
			
			System.out.println("availWidth: "+availWidth);
			System.out.println("visibleItemsWidth: "+visibleItemsWidth);
			System.out.println("absoluteCarouselWidth: "+absoluteCarouselWidth);
			System.out.println("horizontalCarouselOffset: "+horizontalCarouselOffset);
			System.out.println("animatedHorizontalCarouselOffset: "+animatedHorizontalCarouselOffset);
			System.out.println("horsCenterAdjustOffset: "+horsCenterAdjustOffset);
			
			//#enddebug
		}
	}
	
	/**
	 * Returns the next item of a array relative to currently focused.
	 * Helper function to avoid using circular linked list.
	 */
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#getNextItem(int, int)
	 */
	protected Item getNextItem(int keyCode, int gameAction) {
		
		synchronized (this)
		{
			if(this.parentContainer.size() > 1)
			{
				if(!this.isSeekerAnimationActive)
					this.isSeekerAnimationActive = true;
				
				if(gameAction == Canvas.LEFT  && keyCode != Canvas.KEY_NUM4)
				{
					if(this.focusedIndex - 1 >= 0)
						--this.focusedIndex;
					else if(this.allowCycling)
						this.focusedIndex = this.parentContainer.size() - 1;
					
					this.parentContainer.focusChild(this.focusedIndex);
					
					evaluateHorizontalOffsetAnimation(gameAction);
					
					//#if polish.use-ui-animation == false
					this.seekAndDestroy.relativeX = this.focusedItem.relativeX;
					this.animatedHorizontalCarouselOffset = this.horizontalCarouselOffset;
					//#endif
					
					return this.focusedItem;
				}
				else 
				if(gameAction == Canvas.RIGHT  && keyCode != Canvas.KEY_NUM6)
				{
					if(this.focusedIndex + 1 < this.parentContainer.size())
						++this.focusedIndex;
					else if(this.allowCycling)
						this.focusedIndex = 0;
					
					this.parentContainer.focusChild(this.focusedIndex);
					
					evaluateHorizontalOffsetAnimation(gameAction);
					
					//#if polish.use-ui-animation == false
					this.seekAndDestroy.relativeX = this.focusedItem.relativeX;
					this.animatedHorizontalCarouselOffset = this.horizontalCarouselOffset;
					//#endif
					
					return this.focusedItem;
				}
				else
				if((gameAction == Canvas.UP  && keyCode != Canvas.KEY_NUM2) || (gameAction == Canvas.DOWN  && keyCode != Canvas.KEY_NUM8))
				{
					//force bypass of default container handling
					return null;
				}		
				else
					return super.getNextItem(keyCode, gameAction);
			}
		}
		
		return null;
	}
	
	/**
	 * Evaluates the horizontal offset of the item layout.
	 * <p>
	 * Used in horizontal scrolling/animation
	 */
	private synchronized void evaluateHorizontalOffsetAnimation(int gameAction)
	{
		if(this.isSeekerAnimationActive)
		{
			//define available width based on presence of arrows
			int availWidth = this.contentWidth - this.leftPad - this.rightPad;
			
			if(this.absoluteCarouselWidth > availWidth)
			{
				//update animation values
				if(this.focusedIndex == 0)
				{
					//adjust offset if last user input resulted in a cycle/roundtrip to first item
					this.horizontalCarouselOffset = 0;
				}
				else
				if(this.focusedIndex == this.parentContainer.size() - 1)
				{
					//adjust offset if last user input resulted in a cycle/roundtrip to last item
					this.horizontalCarouselOffset = this.visibleItemsWidth - this.absoluteCarouselWidth;
				}
				else 
				if(null != this.focusedItem) 
				{
					//evaluate horizontal offset based on new item focus
					int absoluteX = this.horizontalCarouselOffset + this.focusedItem.relativeX;
					
					if(absoluteX + this.focusedItem.itemWidth < this.leftPad)
					{
						this.horizontalCarouselOffset += this.focusedItem.itemWidth + this.paddingHorizontal;
					}
					else if(absoluteX > this.contentWidth - this.rightPad)
					{
						this.horizontalCarouselOffset -= this.focusedItem.itemWidth + this.paddingHorizontal;
					}
				}
			}
			else
			{
				//no animation, set fixed offsets
				if(this.horizontalCarouselOffset != this.horsCenterAdjustOffset)
				{
					this.horizontalCarouselOffset = 0;
					this.animatedHorizontalCarouselOffset = this.horizontalCarouselOffset;
				}
			}
			
			//seeker slide in's
			if( this.parentContainer.size() > 1 && (this.focusedIndex == 0 || this.focusedIndex == this.parentContainer.size() - 1) )
			{
				int seekWidth = seekAndDestroy.getItemWidth(this.absoluteCarouselWidth, this.absoluteCarouselWidth);
				
				//reset seeker to translate in from the left or right if needed
				if( this.seekAndDestroy.relativeX < (seekWidth>>2) && this.parentContainer.focusedIndex == this.parentContainer.size() - 1 && gameAction == Canvas.LEFT)
					//move to slide in from the right 
					this.seekAndDestroy.relativeX = this.horizontalCarouselOffset + this.absoluteCarouselWidth + this.paddingHorizontal + this.rightPad;
				else
				if( this.seekAndDestroy.relativeX+seekWidth > (this.absoluteCarouselWidth - (seekWidth>>2)) && this.parentContainer.focusedIndex == 0 && gameAction == Canvas.RIGHT )
					//move to slide in from the left
					this.seekAndDestroy.relativeX = this.horizontalCarouselOffset - seekWidth - this.paddingHorizontal - this.leftPad;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#paintContent(de.enough.polish.ui.Container, de.enough.polish.ui.Item[], int, int, int, int, int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	protected void paintContent(Container container, Item[] myItems, int x,
			int y, int leftBorder, int rightBorder, int clipX, int clipY,
			int clipWidth, int clipHeight, Graphics g) 
	{
		//#debug debug
		System.out.println("container:"+container+"/myItems:"+myItems+"/x:"+x+"/y:"+y+"/leftBorder:"+leftBorder+"/rightBorder:"+rightBorder+"/clipX:"+clipX+"/clipY:"+clipY+"/clipWidth:"+clipWidth+"/clipHeight:"+clipHeight);
		
		if(null == myItems || myItems.length == 0)
			return;
		
		recalcAbsoluteCarouselWidth(myItems);
		
		//force item focus
		if(null != this.focusedItem && !this.focusedItem.isFocused)
			focusItem(this.focusedIndex, this.focusedItem);
		
		int initialWidth = rightBorder - leftBorder;
		
		g.clipRect(x, y, initialWidth, clipHeight);
		
		if(null != this.seekAndDestroy)
		{
			//fix seeker position if not animated
			if( null != this.focusedItem && !this.isSeekerAnimationActive &&
					this.seekAndDestroy.relativeX != this.focusedItem.relativeX)
				this.seekAndDestroy.relativeX = this.focusedItem.relativeX;
			
			//force seeker height
			if(this.seekAndDestroy.getItemHeight(initialWidth, initialWidth) < this.contentHeight)
				this.seekAndDestroy.setItemHeight(this.contentHeight);
			
			//paint seeker in background
			paintItem(this.seekAndDestroy, 0, x + this.leftPad + this.horsCenterAdjustOffset + this.animatedHorizontalCarouselOffset + this.seekAndDestroy.relativeX, y + this.seekAndDestroy.relativeY, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
		}
		
		//paint arrows if present and update borders while doing so
		
		if(this.leftArrow!=null)
		{
			g.drawImage(this.leftArrow, x + this.arrowPadding.getValue( initialWidth ), y + (this.contentHeight >> 1), Graphics.LEFT|Graphics.VCENTER);
			leftBorder += this.leftPad;
		}
		
		if(this.rightArrow!=null)
		{
			g.drawImage(this.rightArrow, x + initialWidth - this.arrowPadding.getValue( initialWidth), y + (this.contentHeight >> 1), Graphics.RIGHT|Graphics.VCENTER);
			rightBorder -= this.rightPad;
		}
		
		g.clipRect(leftBorder + 1, clipY, rightBorder - leftBorder - 2, clipHeight);
		
		//paint items inaccordance to offset factors
		for (int i = 0; i < myItems.length; i++) 
		{
			Item item = myItems[i];
			paintItem(item, i, x + this.leftPad + this.horsCenterAdjustOffset + this.animatedHorizontalCarouselOffset + item.relativeX, y + item.relativeY, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
		}
		
		g.setClip(clipX, clipY, clipWidth, clipHeight);
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#setStyle(de.enough.polish.ui.Style)
	 */
	protected void setStyle(Style style)
	{
		super.setStyle(style);

		synchronized (this)
		{
			//#ifdef polish.css.left-arrow-image
			String leftArrowImageUrl = style.getProperty("left-arrow-image");
			if (leftArrowImageUrl != null) {
				try {
					this.leftArrow = StyleSheet.getImage( leftArrowImageUrl, this, true );
				} catch (IOException e) {
					//#debug error
					System.out.println("Unable to load left arrow image [" + leftArrowImageUrl + "]" + e );
				}
			}
			//#endif
			
			//#ifdef polish.css.right-arrow-image
			String rightArrowImageUrl = style.getProperty("right-arrow-image");
			if (rightArrowImageUrl != null) {
				try {
					this.rightArrow = StyleSheet.getImage( rightArrowImageUrl, this, true );
				} catch (IOException e) {
					//#debug error
					System.out.println("Unable to load right arrow image [" + rightArrowImageUrl + "]" + e );
				}
			}
			//#endif
		}
		
		//#ifdef polish.css.horizontal-padding-arrows
		Dimension value = (Dimension) style.getObjectProperty("horizontal-padding-arrows");
		if(value != null)
			this.arrowPadding = value;
		//#endif
		
		//#ifdef polish.css.seeker-style
		if(style.getObjectProperty("seeker-style") != null)
			this.seekAndDestroyStyle = (Style)style.getObjectProperty("seeker-style");
		else
		//#endif
			this.seekAndDestroyStyle = style;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#isValid(de.enough.polish.ui.Item, de.enough.polish.ui.Style)
	 */
	protected boolean isValid(Item parent, Style style) {
		return (parent instanceof Container);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#animate(long, de.enough.polish.ui.ClippingRegion)
	 */
	public void animate(long currentTime, ClippingRegion repaintRegion) 
	{
		//#if polish.use-ui-animation == false
		//remove this ItemView and parent Item from AnimationThread to disable animations
		if(null != this.parentItem)
		{
			AnimationThread.removeAnimationItem(this.parentItem);
			return;			
		}
		//#endif
		
		super.animate(currentTime, repaintRegion);
		
		boolean animated = false;
		
		if(null != this.focusedItem)
		{
			if(null != this.seekAndDestroy)
			{
				this.seekAndDestroy.animate();
				
				//animate seeker
				if(this.seekAndDestroy.relativeX > this.focusedItem.relativeX)
				{
					int dist = (this.seekAndDestroy.relativeX - this.focusedItem.relativeX) >> 1;
					if(dist == 0)
						dist = 1;
					this.seekAndDestroy.relativeX -= dist;
					animated = true;
				}
				else if(this.seekAndDestroy.relativeX < this.focusedItem.relativeX)
				{
					int dist = (this.focusedItem.relativeX - this.seekAndDestroy.relativeX) >> 1;
					if(dist == 0)
						dist = 1;
					this.seekAndDestroy.relativeX += dist;
					animated = true;
				}
			}
		}
		
		//animate hors scroll
		if(this.animatedHorizontalCarouselOffset > this.horizontalCarouselOffset)
		{
			int dist = (this.animatedHorizontalCarouselOffset - this.horizontalCarouselOffset) >> 1;
			if(dist == 0)
				dist = 1;
			this.animatedHorizontalCarouselOffset -= dist;
			animated = true;
		}
		else if(this.animatedHorizontalCarouselOffset < this.horizontalCarouselOffset)
		{
			int dist = (this.horizontalCarouselOffset - this.animatedHorizontalCarouselOffset) >> 1;
			if(dist == 0)
				dist = 1;
			this.animatedHorizontalCarouselOffset += dist;
			animated = true;
		}
		
		//request repaint
		if (animated && null != this.parentItem)
		{
			
			repaintRegion.addRegion( 
					this.parentItem.getAbsoluteX(), 
					this.parentItem.getAbsoluteY(), 
					this.parentItem.itemWidth, 
					this.parentItem.itemHeight
			);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#showNotify()
	 */
	public void showNotify() 
	{
		//update state of non-nested seeker item to enable css animation
		if(null != this.seekAndDestroy)		
			UiAccess.showNotify(this.seekAndDestroy);
		
		super.showNotify();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#handleKeyPressed(int, int)
	 */
	public boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		//update state of non-nested seeker item to enable css animation
		if(null != this.seekAndDestroy)
			UiAccess.handleKeyPressed(this.seekAndDestroy, keyCode, gameAction);
		
		return super.handleKeyPressed(keyCode, gameAction);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#handleKeyPressed(int, int)
	 */
	public boolean handleKeyReleased(int keyCode, int gameAction) 
	{
		//update state of non-nested seeker item to enable css animation
		if(null != this.seekAndDestroy)		
			UiAccess.handleKeyReleased(this.seekAndDestroy, keyCode, gameAction);
		
		return super.handleKeyReleased(keyCode, gameAction);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#hideNotify()
	 */
	public void hideNotify() 
	{
		//update state of non-nested seeker item to enable css animation
		if(null != this.seekAndDestroy)		
			UiAccess.hideNotify(this.seekAndDestroy);
		
		super.hideNotify();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#focus(de.enough.polish.ui.Style, int)
	 */
	public void focus(Style focusstyle, int direction) 
	{
		//update state of non-nested seeker item to enable 'on-focus' css animation
		if(null != this.seekAndDestroy)
			UiAccess.focus(this.seekAndDestroy, 0, this.seekAndDestroy.getFocusedStyle());
		
		super.focus(focusstyle, direction);
	}

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#defocus(de.enough.polish.ui.Style)
	 */
	protected void defocus(Style originalStyle) 
	{
		this.isSeekerAnimationActive = false;
		
		//update state of non-nested seeker item to enable 'on-focus' css animation
		if(null != this.seekAndDestroy)
			UiAccess.defocus(this.seekAndDestroy, this.seekAndDestroy.getStyle());
		
		super.defocus(originalStyle);
		
	}
}
