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

import javax.microedition.lcdui.Image;

import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.ClippingRegion;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;

/**
 * Item used to show a sequence of images as an animation.
 * <p>
 * The Polish class Sprite is not used here as it depends of on one image (filmstrip)
 * holding all the frames of the animation. Since the scaling routines of the Now+ 
 * project may distort the ratio aspect of the image, this class is nessasary.
 * <p>
 * The class now supports focus/defocus states, Anders.
 * 
 * @author Jens Vesti
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class AnimatedIconItem extends ImageItem
{
	private Image[] focusImageArray, unfocusImageArray;
	private int counter = 0;
	private long animationInterval = 50;
	private long frameStamp = System.currentTimeMillis(); 
	
	public AnimatedIconItem(final int animationInterval, final String[] unfocusImages, final String[] focusImages) 
	{
		this(animationInterval, unfocusImages, focusImages, null);
	}

	public AnimatedIconItem(final int animationInterval, final String[] unfocusImages, final String[] focusImages, Style style) 
	{
		super(null, null, 0, null, style);
		
		if(animationInterval > 0)
			this.animationInterval = animationInterval;
		else
			this.animationInterval = AnimationThread.ANIMATION_INTERVAL;
		
		//#mdebug debug
		if( null == unfocusImages )
			throw new IllegalArgumentException("no default imagesets defined");
		
		if(null != unfocusImages && null != focusImages && unfocusImages.length != focusImages.length )
			throw new IllegalArgumentException("imagesets not same length");
		//#enddebug
		
		unfocusImageArray = new Image[unfocusImages.length];
		if(null != focusImages)
			focusImageArray = new Image[focusImages.length];
		
		//loading filmstrips
		for(int i=0; i<unfocusImages.length; i++)
		{
			try 
			{
				unfocusImageArray[i] = StyleSheet.getImage(unfocusImages[i], null, false);
				if(null != focusImageArray && null != focusImages)
					focusImageArray[i] = StyleSheet.getImage(focusImages[i], null, false);
			}
			catch (IOException e) 
			{
				//#debug error
				System.out.println("Error loading filmstrip, imagepath was not found: "+e);
			}
		}
	}
	
	protected void init(int firstLineWidth, int availWidth, int availHeight) 
	{
		if(null == this.getImage())
		{
			if(this.isFocused && null != focusImageArray && focusImageArray.length >= counter)
				this.setImage(focusImageArray[counter]);
			else
			if(null != unfocusImageArray && unfocusImageArray.length >= counter)				
				this.setImage(unfocusImageArray[counter]);
		}
		
		super.init(firstLineWidth, availWidth, availHeight);
	}

	public void showNotify()
	{
		AnimationThread.addAnimationItem(this);
		super.showNotify();
	}
	
	public void hideNotify()
	{
		AnimationThread.removeAnimationItem(this);
		super.hideNotify();
	}
	
	protected void defocus(Style originalStyle) 
	{
		if(null != unfocusImageArray && null != unfocusImageArray[counter])
			this.setImage(unfocusImageArray[counter]);
		super.defocus(originalStyle);
	}
	
	protected Style focus(Style newStyle, int direction) 
	{
		if(null != focusImageArray && null != focusImageArray[counter])
			this.setImage(focusImageArray[counter]);
		return super.focus(newStyle, direction);
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#animate()
	 */
	public boolean animate() {
		if(null != unfocusImageArray)
		{
			/*
			 * Make sure that the desired animation interval has passed before switching to next frame.
			 * This approach does introduce inconsistency as it requires the AnimationThread to run at
			 * its default speed of 20 frames per sec or higher to 'look good', but on the other hand
			 * prevents this frame switch from happening too fast while ensuring that the frames that
			 * the animation consist of are shown in the intended order.
			 */
			if( (System.currentTimeMillis() - frameStamp) > animationInterval)
			{
				frameStamp = System.currentTimeMillis();
				counter = ++counter%unfocusImageArray.length;
				
				return true;
			}
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#animate(long, de.enough.polish.ui.ClippingRegion)
	 */
	public void animate(long currentTime, ClippingRegion repaintRegion) {
		super.animate(currentTime, repaintRegion);
		if(this.isFocused && null != focusImageArray && focusImageArray.length >= counter) {
			setImage(focusImageArray[counter]);
		}
		else if(unfocusImageArray.length >= counter)
		{
			setImage(unfocusImageArray[counter]);
		}
		
		repaintRegion.addRegion( getAbsoluteX(), getAbsoluteY(), this.itemWidth, this.itemHeight );
	}
}
