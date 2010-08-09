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
//#if polish.blackberry && add.switch.application.form
package com.zyb.nowplus.presentation.view.items;

import javax.microedition.lcdui.Image;

import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Style;

public class ContextualMenuFormIconItem  extends IconItem{

	Style originalStyle;
	
	//#if polish.blackberry && add.switch.application.form
	public String discrip;
	public net.rim.device.api.system.ApplicationDescriptor appDescriptor;
	//#endif
	
	public ContextualMenuFormIconItem(String text, Image image) 
	{
		this(text, image, null);
	}
	
	public ContextualMenuFormIconItem(String text, Image image, Style style) 
	{
		super(text, image, style);
	}
	
		public Style focus(Style focusStyle, int direction) 
		{
			if(!this.isFocused && this.originalStyle == null) {
				this.originalStyle = this.style;
			}
			if(this.parent.isFocused)
				return super.focus(focusStyle, direction);
			return this.originalStyle;
		}
		
		public void defocus(Style originalStyle)
		{
			super.defocus(this.originalStyle);
		}
}

//#endif
