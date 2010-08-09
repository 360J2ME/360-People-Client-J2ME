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
import de.enough.polish.util.DrawUtil;
/**
 * @author jens
 * This background creates a solid (solidwithborder) or gradient (gradientwithborder) background with borders.
 * Because Polish Border is buggy we need to have this.
 * 
 *  Common parameters are:
 *	bgcolor - back ground colour, if transparent is needed use "transparent" colour, default is white
 *	bordercolor - colour of the borders used, default is white
 *	topwidth - width of the top border, default is white 
 *	bottomwidth - width of the bottom border, default is white
 *	rightwidth - width of the right border, default is white
 *	leftwidth - width of the left border, default is white
 *
 *  Specific for gradientwithborder are
 *	bgtocolor - colour we are grading to from bgcolor
 */
public class BorderBackground extends Background {

	private Color bgColor,bgToColor;
	private Color bordercolor;
	private int topwidth;
	private int rightwidth;
	private int leftwidth;
	private int bottomwidth;
	private int[] gradient;

	/* Constructor for the "solidwithborder" style
	 * */
	public BorderBackground(final Color bgColor, final Color bordercolor, final int topwidth, final int bottomwidth, final int rightwidth, final int leftwidth)
	{
		this(bgColor, null, bordercolor, topwidth, bottomwidth, rightwidth, leftwidth );
	}
	
	/* Constructor for the "gradientwithborder" style
	 * */
	public BorderBackground(final Color bgColor, final Color bgToColor, final Color bordercolor, final int topwidth, final int bottomwidth, final int rightwidth, final int leftwidth)
	{
		this.bgColor = bgColor;
		this.bgToColor = bgToColor;
		this.bordercolor = bordercolor;
		this.topwidth = topwidth;
		this.bottomwidth = bottomwidth;
		this.rightwidth = rightwidth;
		this.leftwidth = leftwidth;
	}
	
	public void paint(final int x, final int y, final int width, final int height, final Graphics g )
	{
		
		if(this.bgColor != null)
		{
			if(this.bgToColor != null)//Gradient
			{
				gradient = DrawUtil.getGradient( bgColor.getColor(), bgToColor.getColor(), height );
				
				g.setColor( bgColor.getColor() );
				int offsetY = y;
				for (int i = 0; i < height; i++) 
				{
					int color = gradient[i];
					g.setColor( color );
					g.drawLine( x, offsetY, x + width, offsetY);
					offsetY++;
				}

			}
			else//solid
			{
				if(bgColor.getColor() != Color.TRANSPARENT)
				{
					g.setColor(this.bgColor.getColor());
					g.fillRect(x, y, width, height);
				}
			}
		}
		
		if(bordercolor != null)
		{
			g.setColor(bordercolor.getColor());
			if(this.topwidth > 0)
				g.fillRect(x, y, width, this.topwidth);
			if(this.bottomwidth > 0)
				g.fillRect(x, y+height-this.bottomwidth, width, this.bottomwidth);
			if(this.leftwidth > 0)
				g.fillRect(x, y, this.leftwidth, height);
			if(this.rightwidth > 0)
				g.fillRect(x+width-this.rightwidth, y, this.rightwidth, height);
		}
	}
}
