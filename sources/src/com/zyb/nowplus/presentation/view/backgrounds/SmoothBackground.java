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
package com.zyb.nowplus.presentation.view.backgrounds;

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.ui.Background;
import de.enough.polish.ui.StyleSheet;

public class SmoothBackground extends Background
{
    private volatile static Hashtable tilescontainer = new Hashtable();
    private volatile Hashtable tiles;

	private int hash,hashWidth;
	private Integer currentImage;
	
	private Image bg;
	private String image;
	// private int bgcolor;
	private int marginTop;
	private int marginBottom;
	private int marginLeft;
	private int marginRight;

	private int CORNER_WIDTH;
	private int CORNER_HEIGHT_TOP, CORNER_HEIGHT_BOTTOM;

	private static final Integer BG_FOCUSED_TOP 		= new Integer(0);
	private static final Integer BG_FOCUSED_MIDDLE 		= new Integer(1);
	private static final Integer BG_FOCUSED_BOTTOM 		= new Integer(2);
	private static final Integer BG_UNFOCUSED_TOP 		= new Integer(3);
	private static final Integer BG_UNFOCUSED_MIDDLE 	= new Integer(4);
	private static final Integer BG_UNFOCUSED_BOTTOM 	= new Integer(5);


	public SmoothBackground(final String image, final int bgcolor, final int clipHeightTop, final int clipHeightBottom, final int clipWidth,  final int marginTop, final int marginBottom, final int marginLeft, final int marginRight)
	{
		try
		{
			this.image = image;
			this.hash = image.hashCode();
			// this.bgcolor = bgcolor;
			this.CORNER_WIDTH = clipWidth;
			this.CORNER_HEIGHT_TOP = clipHeightTop;
			this.CORNER_HEIGHT_BOTTOM = clipHeightBottom;
			
			this.marginTop = marginTop;
			this.marginBottom = marginBottom;
			this.marginLeft = marginLeft;
			this.marginRight = marginRight;
			
//			if(bgimages.containsKey(image))
//				bg = (Image)bgimages.get(image);
//			else
//			{
//				bg = Image.createImage(image);
//				bgimages.put(image, bg);
//			}
		}
		catch(Exception e)
		{
			//#debug error
			System.out.println("SmoothBackground() failed:"+e.getMessage()+"/"+image);
		}
	}
			
	protected Image updateImage(ARGBImage target, ARGBImage source, final int width, final int height, final int offset, final boolean focused, final Integer type) 
	{
		if(type == BG_FOCUSED_TOP || type == BG_UNFOCUSED_TOP)
		{
			//Upper left corner
			target.setClip(offset, 0, CORNER_WIDTH, CORNER_HEIGHT_TOP);
			target.drawARGBImageOnto(source, offset, 0);
	
			//Upper right corner
			target.setClip(width-CORNER_WIDTH+offset, 0, CORNER_WIDTH, CORNER_HEIGHT_TOP);		
			target.drawARGBImageOnto(source, width-source.getWidth()+offset, 0);
	
			//Upper border
			for(int x=CORNER_WIDTH;x<width-CORNER_WIDTH;x++)
			{
				target.setClip(x+offset, 0, 1, CORNER_HEIGHT_TOP);		
				target.drawARGBImageOnto(source, x-source.getWidth()/2+offset, 0);
			}
		}
		else
		if(type == BG_FOCUSED_MIDDLE || type == BG_UNFOCUSED_MIDDLE)
		{
			//Left border
			target.setClip(offset, 0, CORNER_WIDTH, CORNER_HEIGHT_TOP);		
			target.drawARGBImageOnto(source, offset, -CORNER_HEIGHT_TOP);

			//Lower border
			for(int x=CORNER_WIDTH;x<width-CORNER_WIDTH;x++)
			{
				target.setClip(x+offset, 0, 1, CORNER_HEIGHT_TOP);		
				target.drawARGBImageOnto(source, -CORNER_WIDTH+x, -CORNER_HEIGHT_TOP);
			}

			//Right border
			target.setClip(width-CORNER_WIDTH+offset, 0, CORNER_WIDTH, CORNER_HEIGHT_TOP);		
			target.drawARGBImageOnto(source, width-source.getWidth()+offset, -CORNER_HEIGHT_TOP);
		}
		else
		if(type == BG_FOCUSED_BOTTOM || type == BG_UNFOCUSED_BOTTOM)
		{
			//Lower left corner
			target.setClip(offset, 0, CORNER_WIDTH, CORNER_HEIGHT_BOTTOM);		
			target.drawARGBImageOnto(source, offset, -source.getHeight()+CORNER_HEIGHT_BOTTOM);
	
			//Lower right corner
			target.setClip(width-CORNER_WIDTH+offset,0, CORNER_WIDTH, CORNER_HEIGHT_BOTTOM);		
			target.drawARGBImageOnto(source, width-source.getWidth()+offset, -source.getHeight()+CORNER_HEIGHT_BOTTOM);
	
			//Lower border
			for(int x=CORNER_WIDTH;x<width-CORNER_WIDTH;x++)
			{
				target.setClip(x+offset, 0, 1, CORNER_HEIGHT_BOTTOM);		
				target.drawARGBImageOnto(source, x-source.getWidth()/2+offset, -source.getHeight()+CORNER_HEIGHT_BOTTOM);
			}
		}
		return target.getImage();
	}
	
	public void recalculatebg(final int width)
	{
		if(tiles.get(BG_FOCUSED_TOP) == null)
			try
			{
				if(bg == null)
					//#if polish.classes.ImageLoader:defined
					bg = StyleSheet.getImage(image, null, false);
					//#else
					bg = Image.createImage(image);
					//#endif

				ARGBImage bgARGB = new ARGBImage(bg);

				ARGBImage rgb = new ARGBImage(width,CORNER_HEIGHT_TOP);
				Image tmpfocused = updateImage(rgb, bgARGB, width, CORNER_HEIGHT_TOP, 0, true, BG_FOCUSED_TOP);
				tiles.put(BG_FOCUSED_TOP, tmpfocused);
				
				rgb.reInit(width, 1);
				tmpfocused = updateImage(rgb, bgARGB, width, 1, 0, true, BG_FOCUSED_MIDDLE);
				tiles.put(BG_FOCUSED_MIDDLE, tmpfocused);
				
				rgb.reInit(width,CORNER_HEIGHT_BOTTOM);
				tmpfocused = updateImage(rgb, bgARGB, width, CORNER_HEIGHT_BOTTOM, 0, true, BG_FOCUSED_BOTTOM);
				tiles.put(BG_FOCUSED_BOTTOM, tmpfocused);

				rgb.reInit(width,CORNER_HEIGHT_TOP);
				tmpfocused = updateImage(rgb, bgARGB, width, CORNER_HEIGHT_TOP, 0, false, BG_UNFOCUSED_TOP);
				tiles.put(BG_UNFOCUSED_TOP, tmpfocused);
				
				rgb.reInit(width,1);
				tmpfocused = updateImage(rgb, bgARGB, width,1, 0, false, BG_UNFOCUSED_MIDDLE);
				tiles.put(BG_UNFOCUSED_MIDDLE, tmpfocused);
				
				rgb.reInit(width,CORNER_HEIGHT_BOTTOM);
				tmpfocused = updateImage(rgb, bgARGB, width,CORNER_HEIGHT_BOTTOM, 0, false, BG_UNFOCUSED_BOTTOM);
				tiles.put(BG_UNFOCUSED_BOTTOM, tmpfocused);
			}
			catch(Exception e)
			{
				//#debug error
				System.out.println("recalculatebg failed:" + e);
			}
			
			bg = null;
	}

	
	public void paint(final int x, final int y, final int width, final int height, final Graphics g )
	{
		if(width == 0 || height == 0)
			return;
		
		//Calculating the actual width and height taking margins into consideration
		final int internalWidth = width-marginLeft-marginRight;
		final int internalHeight = height-marginTop-marginBottom;
		final int offsetX = x+marginLeft;
		final int offsetY = y+marginTop;
		
		//Keep track of changes in width
		if(hashWidth != this.hash + internalWidth)
		{
			hashWidth = this.hash + internalWidth;
			currentImage =  new Integer(hashWidth);
		}
		
		if(tilescontainer.containsKey(currentImage))
			tiles = (Hashtable)tilescontainer.get(currentImage);
		else
		{
			tiles = new Hashtable();
			tilescontainer.put(currentImage, tiles);
		}
		
		//If there are no image parts in the hashtable  
		if(tiles.size()==0)
			recalculatebg(internalWidth);
		
		g.drawImage((Image)tiles.get(BG_FOCUSED_TOP), offsetX, offsetY, Graphics.TOP|Graphics.LEFT);
		final Image tmp = (Image)tiles.get(BG_FOCUSED_MIDDLE);
		for(int i=CORNER_HEIGHT_TOP; i<internalHeight-CORNER_HEIGHT_BOTTOM; i++)
			g.drawImage(tmp, offsetX, i+offsetY, Graphics.TOP|Graphics.LEFT);
		g.drawImage((Image)tiles.get(BG_FOCUSED_BOTTOM), offsetX, internalHeight+offsetY-CORNER_HEIGHT_BOTTOM, Graphics.TOP|Graphics.LEFT);
	}
}
