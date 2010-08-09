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

/**
 * @author Jens Vesti
 *
 */
public class ButtonBorder extends Border{

	protected final int topColor;
	protected final int bottomColor;

	/**
	 */
	public ButtonBorder( int topColor, int bottomColor)
	{
		super(1, 1, 1, 1);
		this.topColor = topColor;
		this.bottomColor = bottomColor;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Border#paint(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	public void paint(int x, int y, int width, int height, Graphics g)
	{
		int rightX = x + width - 1;
		int bottomY = y + height - 1;
		// paint top border:
		g.setColor( this.topColor );
		int border = this.borderWidthTop;
		while ( border >= 0) {
			g.drawLine( x, y + border, rightX, y + border );
			border--;
		}
		// paint bottom border:
		g.setColor( this.bottomColor );
		border = this.borderWidthBottom;
		while ( border >= 0) {
			g.drawLine( x, bottomY - border, rightX, bottomY - border );
			border--;
		}
		// paint left border:
		g.setColor( this.topColor );
		border = this.borderWidthLeft;
		while ( border >= 0) {
			g.drawLine( x + border, y, x + border, bottomY );
			border--;
		}
		// paint right border:
		g.setColor( this.bottomColor );
		border = this.borderWidthRight;
		while ( border >= 0) {
			g.drawLine( rightX - border, y, rightX - border, bottomY );
			border--;
		}
	}
}

