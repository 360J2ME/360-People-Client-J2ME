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

import de.enough.polish.ui.CustomItem;
import de.enough.polish.ui.Item;

/**
 * Item with dynamic width. Used to dynamically change horizontal placement of items
 * held in same parent Container object.  
 * 
 * Note that, in order to shift other items horizontally using this item, the parent 
 * should NOT use a 'view-type' CSS attribute that uses line skipping (e.g. ContainerView
 * 'horizontal') and it should NOT use Container specific CSS attribute 'columns'.
 *  
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class DynamicWidthItem extends CustomItem
{
	private int forcedWidth;
	
	public DynamicWidthItem(int initialSize) 
	{
		super(null);
		
		this.paddingBottom = 0;
		this.paddingHorizontal = 0;
		this.paddingLeft = 0;
		this.paddingRight = 0;
		this.paddingTop = 0;
		this.paddingVertical = 0;
		
		this.marginBottom = 0;
		this.marginLeft = 0;
		this.marginRight = 0;
		this.marginTop = 0;
		
		this.appearanceMode = Item.PLAIN;
	}

	public void setWidth(int newWidth)
	{
		this.forcedWidth = newWidth;
		initContent(forcedWidth, forcedWidth, 1);
	}
	
	protected int getMinContentHeight() 
	{
		return 1;
	}

	protected int getMinContentWidth() 
	{
		return forcedWidth;
	}

	protected int getPrefContentHeight(int width)
	{
		return 1;
	}

	protected int getPrefContentWidth(int height)
	{
		return forcedWidth;
	}

	protected void paint(Graphics g, int w, int h)
	{
		/*
		g.setColor(0XFF0000);
		g.fillRect(this.getContentX(), this.getContentY(), this.itemWidth, this.itemHeight);
		*/
	}
}
