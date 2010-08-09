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

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandItem;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;

/**
 * A subclass of CommandItem to circumvent some
 * features of the CommandItem
 * @author Andre
 */
public class CascadeItem extends CommandItem{

	/**
	 * Creates a new CascadeItem
	 * @param command the command
	 * @param parent the parent item
	 */
	public CascadeItem(Command command, Item parent) {
		super(command, parent);
	}
	
	/**
	 * Creates a new CascadeItem
	 * @param command the command
	 * @param parent the parent item
	 * @param style the style
	 */
	public CascadeItem(Command command, Item parent, Style style)
	{
		super(command,parent,style);
	}

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Item#init(int, int, int)
	 */
	protected void init(int firstLineWidth, int availWidth, int availHeight) {
		super.init(firstLineWidth, availWidth, availHeight); 
		if(this.hasChildren)
		{
			int w = this.contentWidth;
			//#if polish.blackberry
			if(w < this.getAvailableWidth() / 2) {
				w = this.getAvailableWidth() / 2;
			}
			//#endif
			//added to correct the width of the item.
			if( w < this.getAvailableWidth()/3){
				w = this.getAvailableWidth()/3+8;
			}
			this.children.getItemWidth( w, w, availHeight );

			children.relativeX = this.contentX;
			children.relativeY = this.contentY;
		}

	}

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.CommandItem#paintChildren(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	protected void paintChildren(int x, int y, int leftBorder, int rightBorder, Graphics g)
	{
		if (isOpen()) {
			// container is relative 0/0 to the content:
			this.children.paint( x, y, x, x + this.contentWidth, g );
		}
	}


	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CommandItem#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction) {
		if(gameAction == Canvas.UP || gameAction == Canvas.DOWN || gameAction == Canvas.FIRE)
		{
			// only let DOWN,UP and FIRE pass to super.handleKeyPressed()
			return super.handleKeyPressed(keyCode, gameAction);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.CommandItem#handleKeyReleased(int, int)
	 */
	protected boolean handleKeyReleased(int keyCode, int gameAction) {
		if(gameAction == Canvas.UP || gameAction == Canvas.DOWN || gameAction == Canvas.FIRE)
		{
			// only let DOWN,UP and FIRE pass to super.handleKeyPressed()
			return super.handleKeyReleased(keyCode, gameAction);
		}
		
		return false;
	}

}
