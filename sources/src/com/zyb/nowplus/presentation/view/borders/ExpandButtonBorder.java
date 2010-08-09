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
package com.zyb.nowplus.presentation.view.borders;

import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.Border;
import de.enough.polish.util.DrawUtil;

/**
 * Border class for 'expandable button' items. E.g. under email views
 * @author anders
 */

public class ExpandButtonBorder extends Border
{
	protected int color;
	protected int cornerDim;
	
	/**
	 */
	public ExpandButtonBorder( int color, int borderWidth, int cornerDim ) {
		super( borderWidth, borderWidth, borderWidth, borderWidth );
		this.color = color;
		this.cornerDim = cornerDim;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Border#paint(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	public void paint(int x, int y, int width, int height, Graphics g)
	{
		// draw frame
		width--;
		height--;
		g.setColor( this.color );
		g.drawRect( x, y, width, height );
		if (this.borderWidthLeft > 1) {
			int border = this.borderWidthLeft - 1;
			while ( border > 0) {
				g.drawRect( x+border, y+border, width - (border<<1), height - (border<<1) );
				border--;
			}
		}
		
		//draw corner bottom right
		int	x1 = x+width,y1=y+height;
		int x2 = x1 ,y2 = y1 - cornerDim;
		int x3 = x1 - cornerDim,y3 = y1 ;
		
		DrawUtil.fillTriangle(x1, y1, x2, y2, x3, y3, g);
	}

}

