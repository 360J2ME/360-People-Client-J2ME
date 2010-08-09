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

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.util.DrawUtil;

import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.ClippingRegion;
import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

/**
 * @author Jens Vesti
 *
 */
public class ProgressIndicatorItem extends CustomItem
{
	//ITEMS here are referred to the colored polygons
	
	public int counter = 0;//An infinite incremental counter to make the progress indicator tick
	private int DISTANCE_BETWEEN_ITEMS = 30; 
	private int ITEM_WIDTH = 10; 
	private int ITEM_HEIGHT = 10; 
	private int ITEM_COLOR = 0x000000; 
	private int BORDER_COLOR = 0x000000; 
	private Image leftImage,rightImage;//Images drawn on top of the crude progress bar to give a nice edge
	private byte progressType = PROGRESS_INFINITE;
	private int[] yPoints,xPoints;//Polygon descriptive coordinates
	private int TOP_BOTTOM_DIFF = -5;//Difference along x-axis in pixels between top and bottom. The reason for the tilting items  
	
	//Following are used for the smooth progress bar
	//The smooth progress bar is used to give the user a perception of something happening
	private float percentageTarget;//The percentage of the incremental indicator to be filled
	private float percentagePrevious;//The previous percentage target 
	private float percentageCurrent;//Current percentage in the progress bar
	private long timeWhenPercentageWasSet;//The time when the percentage was set
	private long timeSmoothOverTo;//The time when target percentage must be reached when doing smooth sliding
	
	public static final byte PROGRESS_INFINITE = 0;
	public static final byte PROGRESS_INCREMENTAL = 1;
	
	/**
	 * @param label
	 */
	public ProgressIndicatorItem(final byte progressType)
	{
		this(progressType,null);
	}

	public ProgressIndicatorItem(final byte progressType, Style style) {
		super(null,style);
		
		this.progressType = progressType;
		this.setAppearanceMode(Item.PLAIN);
		
		//#ifdef polish.css.distance-between-items
		if(style.getIntProperty("distance-between-items") != null)
			this.DISTANCE_BETWEEN_ITEMS = style.getIntProperty("distance-between-items").intValue();
		//#endif
		
		//#ifdef polish.css.item-width
		if(style.getIntProperty("item-width") != null)
			this.ITEM_WIDTH = style.getIntProperty("item-width").intValue();
		//#endif

		//#ifdef polish.css.item-height
		if(style.getIntProperty("item-height") != null)
			this.ITEM_HEIGHT = style.getIntProperty("item-height").intValue();
		//#endif

		//#ifdef polish.css.item-color
		if(style.getIntProperty("item-color") != null)
			this.ITEM_COLOR = style.getIntProperty("item-color").intValue();
		//#endif

		//#ifdef polish.css.item-border-color
		if(style.getIntProperty("item-border-color") != null)
			this.BORDER_COLOR = style.getIntProperty("item-border-color").intValue();
		//#endif
		
		String LEFT_IMAGE_URL = null;
		String RIGHT_IMAGE_URL = null;

		//#ifdef polish.css.item-left-image
		if(style.getProperty("item-left-image") != null)
			LEFT_IMAGE_URL = style.getProperty("item-left-image");
		//#endif

		//#ifdef polish.css.item-right-image
		if(style.getProperty("item-right-image") != null)
			RIGHT_IMAGE_URL = style.getProperty("item-right-image");
		//#endif

		try
		{
			if(LEFT_IMAGE_URL != null)
				leftImage = StyleSheet.getImage(LEFT_IMAGE_URL, null, true);
			if(RIGHT_IMAGE_URL != null)
				rightImage = StyleSheet.getImage(RIGHT_IMAGE_URL, null, true);
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println("Could not load image:"+e.getMessage());
		}
		
		//Initialise polygon points
		yPoints = new int[]{0,0,ITEM_HEIGHT,ITEM_HEIGHT};
		xPoints = new int[yPoints.length]; 

	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#getMinContentHeight()
	 */
	protected int getMinContentHeight() {
		return ITEM_HEIGHT;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#getMinContentWidth()
	 */
	protected int getMinContentWidth() {
		return ITEM_WIDTH;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#getPrefContentHeight(int)
	 */
	protected int getPrefContentHeight(int height)
	{
		return ITEM_HEIGHT;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#getPrefContentWidth(int)
	 */
	protected int getPrefContentWidth(int width) {
		return 1000;//Max
	}
	
	public void animate( long currentTime, ClippingRegion repaintRegion) {
		super.animate(currentTime,repaintRegion);
		counter++;
		counter%=(DISTANCE_BETWEEN_ITEMS+ITEM_WIDTH);//distance from beginning of item to beginning of next item
		repaintRegion.addRegion(getAbsoluteX(), getAbsoluteY(), itemWidth, itemHeight);
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#hideNotify()
	 */
	public void hideNotify()
	{
		AnimationThread.removeAnimationItem(this);
		super.hideNotify();
	}
	
	public void showNotify()
	{
		AnimationThread.addAnimationItem(this);
		super.showNotify();
	}

	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CustomItem#paint(javax.microedition.lcdui.Graphics, int, int)
	 */
	protected void paint(Graphics g, int w, int h) {
		g.setColor(ITEM_COLOR);

		if(progressType == PROGRESS_INFINITE)
		{
			for(int x=0; x<w+DISTANCE_BETWEEN_ITEMS+ITEM_WIDTH; x+=DISTANCE_BETWEEN_ITEMS+ITEM_WIDTH)
			{
				xPoints[0] = -ITEM_WIDTH+x+counter;
				xPoints[1] = x+counter;
				xPoints[2] = TOP_BOTTOM_DIFF +x+counter;
				xPoints[3] = -ITEM_WIDTH-5+x+counter;
				
				//#if polish.device.requires.customPolygonDraw == true
				fillQuadrilateral(xPoints, yPoints, g);
				//#else
				DrawUtil.fillPolygon(xPoints, yPoints, ITEM_COLOR, g);
				//#endif
			}
		}
		else //PROGRESS_INCREMENTAL
		{
			if(this.timeWhenPercentageWasSet == this.timeSmoothOverTo)//We don't do a smooth sliding and simply show what we got
			{
				g.fillRect(0, 0, (int)((w*percentageTarget)/100), h);
			}
			else//smooth sliding
			{
				float percentageDiff = this.percentageTarget - this.percentagePrevious;//The difference in percent between previous offset set by setPercentage and the target percentage
				float timeDiff = this.timeSmoothOverTo - this.timeWhenPercentageWasSet;
				float timeElapsed = System.currentTimeMillis() - this.timeWhenPercentageWasSet;
				float timeElapsedPercentage = ((timeElapsed/timeDiff)>1?1:(timeElapsed/timeDiff));//How much of the expected time has passed
				
				//We store the current percentage which will be the offset when next percentage is set.
				//This will only look acceptable if the smoothOverSeconds in setPercentage is relatively accurate
				this.percentageCurrent = this.percentagePrevious+percentageDiff*timeElapsedPercentage;
				
				g.fillRect(0, 0, (int)((w*percentageCurrent)/100), h);
			}
		}

		g.setColor(BORDER_COLOR);

		
		//Draw top and bottom lines
		g.drawLine(0, 0, w, 0);
		g.drawLine(0, h-1, w, h-1);
		
		//Finish off with nice edges
		if(this.rightImage != null)
			g.drawImage(this.rightImage, w, 0, Graphics.RIGHT|Graphics.TOP);
		else
			g.drawLine(0, 0, 0, h);
			
		if(this.leftImage != null)
			g.drawImage(this.leftImage, 0, 0, Graphics.LEFT|Graphics.TOP);
		else
			g.drawLine(w-1, 0, w-1, h);
	}
	//#if polish.device.requires.customPolygonDraw == true
	/**
	 * Draws filled figure with four angles using lines. 
	 * This is solution for Huawei bug in drawing polygons. 
	 * 
	 * This method expects two arrays with four coordinates. 
	 * 
	 * @param xPoints
	 * @param yPoints
	 */
	public void fillQuadrilateral(int[] xPoints, int[] yPoints, Graphics g)
	{
		/* x1,y1               x2,y2 
		 *  +-------------------+
		 *  |                   |
		 *  |                   |
		 *  |                   |
		 *  +-------------------+
		 * x4,y4                 x3,y3  
		 *
		 * 		x1,y1               x2,y2 
		 *      +-------------------+
		 *     /                   /
		 *    /                   /
		 *   /                   /
		 *  +-------------------+
		 * x4,y4                x3,y3    
		 */
		
		int y1 = yPoints[0]; 
		int y2 = yPoints[1];
		int y3 = yPoints[2];
		int y4 = yPoints[3];
		int x1 = xPoints[0];
		int x2 = xPoints[1];
		int x3 = xPoints[2];
		int x4 = xPoints[3];
		
		int maxY = (y4<=y3) ? y4 : y3;
		int minY = (y1>=y2) ? y1 : y2;
		int h1 = y4-y1; 
		int h2 = y3-y2;
		int d1 = -(x1-x4);
		int d2 = -(x2-x3); 
		
		for (int y = minY; y<=maxY; y++)
		{
			int a1 = (d1*y)/h1;
			int a2 = (d2*y)/h2;
			g.drawLine(x1+a1, y, x2+a2 , y);
		}
	}
	//#endif

	/**
	 * Set the percentage of the indicator
	 * @param percentage is the percentage which the progress indicator should be filled
	 */
	public void setPercentage(int percentage)
	{
		// Never set a lower percentage value then before.
		if (percentage < this.percentageTarget) {
			return;
		}

		this.percentageTarget = percentage;
		this.percentagePrevious = this.percentageCurrent;
	}

	/**
	 * Set the percentage of the indicator
	 * @param percentage is the percentage which the progress indicator should be filled
	 */
	public void setPercentage(int percentage, int smoothOverSeconds)
	{
		// Never set a lower percentage value then before.
		if (percentage < this.percentageTarget) {
			return;
		}

		this.percentagePrevious = this.percentageCurrent;
		this.percentageTarget = percentage;
		this.timeWhenPercentageWasSet = System.currentTimeMillis();//now
		this.timeSmoothOverTo = this.timeWhenPercentageWasSet + smoothOverSeconds*1000;
	}

}
