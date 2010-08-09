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

import com.zyb.nowplus.presentation.view.items.IconSwapItem;

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
 * A ContainerView that displays items in a horizontal circular carousel layout.
 * Selected item is always centered. In focused state all items are shown, in defocused 
 * state only the centered item is shown.
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
public class HorizontalCarouselView extends ContainerView
{
	//custom css members
	protected Image leftArrow, rightArrow;
	protected Dimension arrowPadding;
	
	/*
	 * Local measure & animation members
	 */
	protected int absoluteCarouselWidth = 0;
	protected int animatedHorizontalCarouselOffset;
	
	/*Keeps track of input history. Used a display mode flag*/
	protected int lastHorsGameAction = Canvas.RIGHT;
	
	/**
	 * Side paddings as result of presence of arrows
	 */
	private int leftPad,rightPad;
	
	/**
	 * Style for stretcher
	 */
	private Style stretcherStyle;
	
	/**
	 * animated stretcher item
	 */
	protected transient Item stretcher;
	
	public HorizontalCarouselView() 
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
		
		//initiating height
		Item[] myItems = ((Container)parentContainerItem).getItems();
		Item item;
		for (int i = 0; i < myItems.length; i++)
		{
			item = myItems[i];
			this.contentHeight = Math.max(this.contentHeight, item.getItemHeight(firstLineWidth, availWidth, availHeight));
		}
		
		// init stretcher item
		this.stretcher = new StringItem(null, "", this.stretcherStyle);
		this.stretcher.setAppearanceMode(Item.PLAIN);
		//We need to initialise the stretcher before setting itemheight else it will be overwritten once initialising.
		//Asking for ItemHeight forces an init - JV
		//TODO make sure the parent/children link is created in order for interdependent styles to work, and remove this "init" line
		this.stretcher.getItemHeight(firstLineWidth, availWidth, availHeight);
		this.stretcher.setItemHeight(this.contentHeight);
		
		this.leftPad = (this.leftArrow != null ? this.leftArrow.getWidth() + this.arrowPadding.getValue(this.contentWidth): 0);
		this.rightPad = (this.rightArrow != null ? this.rightArrow.getWidth() + this.arrowPadding.getValue(this.contentWidth): 0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#paintContent(de.enough.polish.ui.Container, de.enough.polish.ui.Item[], int, int, int, int, int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	protected void paintContent(Container container, Item[] myItems, int x,
			int y, int leftBorder, int rightBorder, int clipX, int clipY,
			int clipWidth, int clipHeight, Graphics g) 
	{
		if(null == myItems || myItems.length == 0)
			return;
		
		int initialWidth = rightBorder - leftBorder;
		
		//#debug debug
		System.out.println("this.focusedItem: "+this.focusedItem);
		
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
		
		//fixed clipping over a very large area causing broken main navigation
		//g.clipRect(leftBorder + 1, clipY, rightBorder - leftBorder - 2, clipHeight);
		g.clipRect(leftBorder + 1, this.parentContainer.getAbsoluteY(), rightBorder - leftBorder - 2, this.parentContainer.itemHeight);
		int numPainted = 0;
		
//		//force stretcher height
//		if(this.stretcher.getItemHeight(clipWidth, clipWidth) < this.contentHeight)
//			this.stretcher.setItemHeight(this.contentHeight);
		
		if(this.isFocused)
			paintItem(this.stretcher, 0, x + ((initialWidth - this.stretcher.getItemWidth(initialWidth, initialWidth)) >> 1), y, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
		
		if(this.focusedItem == null) {
			return;
		}
			
		//force item text drawing
		if(!this.focusedItem.isFocused && this.focusedItem instanceof IconSwapItem)
			((IconSwapItem)this.focusedItem).forceTextVisibility(true);
		
		//paint focused center
		paintItem(this.focusedItem, this.focusedIndex, x + this.animatedHorizontalCarouselOffset + ((initialWidth - this.focusedItem.getItemWidth(initialWidth, initialWidth)) >> 1), y, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
		++numPainted;
		
		if(numPainted==myItems.length)
			return;
		
		//only paint remaining members if focused
		if(this.isFocused)
		{
			//init offsets
			Item left,right;
			int leftOffset,rightoffset;
			leftOffset = rightoffset = this.animatedHorizontalCarouselOffset + (initialWidth >> 1); 
			leftOffset -= this.stretcher.getItemWidth(initialWidth, initialWidth) >> 1;
			rightoffset += (this.stretcher.getItemWidth(initialWidth, initialWidth) >> 1) + this.paddingHorizontal;
			
			int iteration = 1;
			
			
				while (numPainted < myItems.length) 
			
			{
				//using input direction to balance display mode
				if(this.lastHorsGameAction == Canvas.RIGHT)
				{
					right = getNextRelativeToFocus(true,myItems,iteration);
					if(right != null)
					{
						paintItem(right, 0, x + rightoffset, y, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
						rightoffset += (right.getItemWidth(initialWidth, initialWidth) + this.paddingHorizontal);
						++numPainted;
						
							if(numPainted==myItems.length)
								break;
						
						
						
					}
					
					left = getNextRelativeToFocus(false,myItems,iteration);
					if(left != null)
					{
						leftOffset -= (left.getItemWidth(initialWidth, initialWidth) + this.paddingHorizontal);
						paintItem(left, 0, x + leftOffset, y, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
						++numPainted;
							if(numPainted==myItems.length)
								break;
					
					}
				}
				else
				{
					left = getNextRelativeToFocus(false,myItems,iteration);
					if(left != null)
					{
						leftOffset -= (left.getItemWidth(initialWidth, initialWidth) + this.paddingHorizontal);
						paintItem(left, 0, x + leftOffset, y, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
						++numPainted;
							if(numPainted==myItems.length)
								break;
					
					}
					
					right = getNextRelativeToFocus(true,myItems,iteration);
					if(right != null)
					{
						paintItem(right, 0, x + rightoffset, y, leftBorder, rightBorder, clipX, clipY, clipWidth, clipHeight, g);
						rightoffset += (right.getItemWidth(initialWidth, initialWidth) + this.paddingHorizontal);
						++numPainted;
						
							if(numPainted==myItems.length)
								break;
						
					}
				}
				++iteration;
			}
		}
		
		g.setClip(clipX, clipY, clipWidth, clipHeight);
	}
	
	/**
	 * Returns the next item of a array relative to currently focused.
	 * Helper function to avoid using circular linked list.
	 * 
	 * @param isRight
	 * @param items
	 * @return
	 */
	private Item getNextRelativeToFocus(boolean isRight, Item[] items, int factor)
	{
		if(null == items || items.length == 0)
			return null;
		
		int relativeIndex = this.focusedIndex + (isRight ? factor : -factor);
		
		if(relativeIndex < 0)
			relativeIndex += items.length;
		if(relativeIndex >= items.length)
			relativeIndex -= items.length;
		
		return items[relativeIndex];
	}
		
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#getNextItem(int, int)
	 */
	protected Item getNextItem(int keyCode, int gameAction)
	{
		synchronized (this) 
		{
			if(this.parentContainer.size() > 1)
			{
				if(gameAction == Canvas.LEFT  && keyCode != Canvas.KEY_NUM4)
				{
					if(this.focusedIndex - 1 >= 0)
						--this.focusedIndex;
					else if(this.allowCycling)
						this.focusedIndex = this.parentContainer.size() - 1;
					
					this.parentContainer.focusChild(this.focusedIndex);
					
					//#debug debug
					System.out.println("this.focusedIndex: "+this.focusedIndex);
					
					//#if polish.use-ui-animation == true
					this.animatedHorizontalCarouselOffset -= this.stretcher.getItemWidth(this.contentWidth, this.contentWidth) >> 1;
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
					
					//#debug debug
					System.out.println("this.focusedIndex: "+this.focusedIndex);				
					
					//#if polish.use-ui-animation == true
					this.animatedHorizontalCarouselOffset += this.stretcher.getItemWidth(this.contentWidth, this.contentWidth) >> 1;
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
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#handleKeyPressed(int, int)
	 */
	public boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		//store last keypress, used in painting routines
		if( (gameAction == Canvas.LEFT && keyCode != Canvas.KEY_NUM4) || 
				(gameAction == Canvas.RIGHT && keyCode != Canvas.KEY_NUM6) )
			this.lastHorsGameAction = gameAction;
		
		//update state of non-nested seeker item to enable css animation
		if(null != this.stretcher)		
			UiAccess.handleKeyPressed(this.stretcher, keyCode, gameAction);
		
		return super.handleKeyPressed(keyCode, gameAction);
	}

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#handleKeyReleased(int, int)
	 */
	public boolean handleKeyReleased(int keyCode, int gameAction)
	{
		//store last keypress, used in painting routines
		if( (gameAction == Canvas.LEFT && keyCode != Canvas.KEY_NUM4) || 
				(gameAction == Canvas.RIGHT && keyCode != Canvas.KEY_NUM6) )
			this.lastHorsGameAction = gameAction;
		
		//update state of non-nested seeker item to enable css animation
		if(null != this.stretcher)
			UiAccess.handleKeyReleased(this.stretcher, keyCode, gameAction);
		
		return super.handleKeyReleased(keyCode, gameAction);
	}
		
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ContainerView#setStyle(de.enough.polish.ui.Style)
	 */
	protected void setStyle(Style style)
	{
		super.setStyle(style);

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
		
		//#ifdef polish.css.horizontal-padding-arrows
		Dimension value = (Dimension) style.getObjectProperty("horizontal-padding-arrows");
		if(value != null)
			this.arrowPadding = value;
		//#endif
		
		//#ifdef polish.css.stretcher-style
		if(style.getObjectProperty("stretcher-style") != null)
			this.stretcherStyle = (Style)style.getObjectProperty("stretcher-style");
		else
		//#endif
			this.stretcherStyle = style;
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
		
		if(this.isFocused && null != this.focusedItem)
		{
			this.stretcher.animate();
			
			//animate seeker
			if(this.animatedHorizontalCarouselOffset > 0)
			{
				int dist = this.animatedHorizontalCarouselOffset >> 1;
				if(dist == 0)
					dist = 1;
				this.animatedHorizontalCarouselOffset -= dist;
				animated = true;
			}
			else 
			if(this.animatedHorizontalCarouselOffset < 0)
			{
				int dist = Math.abs(this.animatedHorizontalCarouselOffset >> 1);
				if(dist == 0)
					dist = 1;
				this.animatedHorizontalCarouselOffset += dist;
				animated = true;
			}
		}
		else
		if(this.animatedHorizontalCarouselOffset != 0)
			
			//no animation
			this.animatedHorizontalCarouselOffset = 0;
		
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
		//#if polish.use-ui-animation == true
		
		//update state of non-nested seeker item to enable css animation
		if(null != this.stretcher)
			UiAccess.showNotify(this.stretcher);
		
		//#endif
		
		super.showNotify();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#hideNotify()
	 */
	public void hideNotify() 
	{
		//#if polish.use-ui-animation == true
		
		//update state of non-nested seeker item to enable css animation
		if(null != this.stretcher)		
			UiAccess.hideNotify(this.stretcher);
		
		//#endif
		
		super.hideNotify();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#focus(de.enough.polish.ui.Style, int)
	 */
	public void focus(Style focusstyle, int direction)
	{
		//#if polish.use-ui-animation == true
		
		AnimationThread.addAnimationItem( this.parentItem );
		
		//update state of non-nested seeker item to enable css animation
		if(null != this.stretcher)
			UiAccess.focus(this.stretcher, 0, this.stretcher.getFocusedStyle());
		
		//#endif
		
		super.focus(focusstyle, direction);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemView#defocus(de.enough.polish.ui.Style)
	 */
	protected void defocus(Style originalStyle) 
	{
		//#if polish.use-ui-animation == true
		
		AnimationThread.removeAnimationItem( this.parentItem );
		
		this.animatedHorizontalCarouselOffset = 0;
		
		//update state of non-nested seeker item to enable css animation
		if(null != this.stretcher)
			UiAccess.defocus(this.stretcher, this.stretcher.getStyle());
		
		//#endif
		
		super.defocus(originalStyle);
	}
}
