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

import javax.microedition.lcdui.Image;

import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Style;

/**
 * Extension of IconItem. Shows Icon image and text when focused
 * and only icon when defocused.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class IconSwapItem extends IconItem{

	//#if polish.blackberry
		Style originalStyle;
	//#endif
	
	public IconSwapItem(String text, Image image) 
	{
		this(text, image, null);
	}
	
	public IconSwapItem(String text, Image image, Style style) 
	{
		super(text, image, style);
		//#if polish.blackberry.isTouchBuild == false
			this.setTextVisible(false);
		//#endif
	}
	
	

		public Style focus(Style focusStyle, int direction) 
		{
			//#if polish.blackberry
			if(!this.isFocused && this.originalStyle == null) {
				this.originalStyle = this.style;
			}
			//#endif
			
			Style s = super.focus(focusStyle, direction);
			//#if !polish.blackberry
				this.setTextVisible(true);
			//#endif
			return s;
		}
		
		public void defocus(Style originalStyle)
		{
			//#if polish.blackberry
				super.defocus(this.originalStyle);
			//#else
				super.defocus(originalStyle);
			//#endif
			//#if !polish.blackberry
			if(this.parent.isFocused) {
				this.setTextVisible(false);
			}
			//#endif
		}
		
		public void forceTextVisibility(boolean force)
		{
			this.setTextVisible(force);
		}

}
