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

import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.Background;
import de.enough.polish.ui.Color;

/**
 * 
 * @author Anders Bo Pedersen
 *
 */
public class DiagonalStripesBackground extends Background
{
	private Color bgColor;
	private int stripeHeight;
	private int stripeWidth;
	private Color stripeColor;

	/**
	 * New BG
	 * 
	 * @param bgcolor
	 * @param stripeheight
	 * @param stripewidth
	 * @param stripecolor
	 */
	public DiagonalStripesBackground(final Color bgcolor, final int stripewidth, final int stripeheight, final Color stripecolor) 
	{
		this.bgColor = bgcolor;
		this.stripeHeight = stripeheight;
		this.stripeWidth = stripewidth;
		this.stripeColor = stripecolor;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Background#paint(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	public void paint(int x, int y, int width, int height, Graphics g) 
	{
		//fill back
		if(bgColor.getColor() != Color.TRANSPARENT)
		{
			g.setColor(this.bgColor.getColor());
			g.fillRect(x, y, width, height);
		}
		
		int newStripeWidth = stripeWidth, newStripeHeight = stripeHeight;
		
		//adjust line dims according to desired height
		while(newStripeHeight < height){
			newStripeWidth <<= 1;
			newStripeHeight <<= 1;
		}
		
		//draw diagonal stripes
		g.setColor(stripeColor.getColor());
		int xIncre = x - newStripeWidth;
		while(xIncre < width+newStripeWidth)
		{
			g.drawLine(xIncre, y, xIncre-newStripeWidth, y+newStripeHeight);
			xIncre += stripeWidth;
		}
	}
}
