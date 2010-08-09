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
//#condition polish.usePolishGui
/*
 * Created on Dec 8, 2008 at 7:38:29 AM.
 * 
 * Copyright (c) 2009 Robert Virkus / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package com.zyb.nowplus.presentation.view.items;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.Item;
import de.enough.polish.ui.Style;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.TextUtil;

public class ActivityInfoItem extends Item {
	
	public class ActivityEntry {
		/**
		 * 
		 */
		private String text;

		/**
		 * 
		 */
		private Font font;
		
		/**
		 * 
		 */
		private Font fontFocused;
		
		/**
		 * 
		 */
		private int color;
		
		/**
		 * 
		 */
		private int colorFocused;

		/**
		 * 
		 */
		private int relativeX;

		/**
		 * 
		 */
		private int relativeY;

		public ActivityEntry(String text, Style style) {
			Style styleFocused = (Style)style.getObjectProperty("focused-style");
			
			Font font = style.getFont();
			
			if(font == null)
			{
				font = Font.getDefaultFont();
			}
			
			Font fontFocused = font;
			
			int color = style.getFontColor();
			int colorFocused = color;
			
			if(styleFocused != null)
			{
				fontFocused = styleFocused.getFont();
				
				if(fontFocused == null)
				{
					fontFocused = font;
				}
				
				colorFocused = styleFocused.getFontColor();
			}
			
			init(text,font,fontFocused,color,colorFocused,0,0);
		}
		
		public ActivityEntry(String text, ActivityEntry source, int relativeX, int relativeY)
		{
			init(text,source.getFont(),source.getFocusedFont(),source.getColor(),source.getFocusedColor(),relativeX,relativeY);
		}
		
		void init(String text, Font font, Font fontFocused, int color, int colorFocused, int relativeX, int relativeY)
		{
			this.text = text;
			
			this.font = font;
			
			this.fontFocused = fontFocused;
			
			this.color = color;
			
			this.colorFocused = colorFocused;
			
			this.relativeX = relativeX;
			
			this.relativeY = relativeY;
		}

		public String getText() {
			return text;
		}

		public Font getFont() {
			return this.font;
		}
		
		public Font getFocusedFont() {
			return this.fontFocused;
		}
		
		public int getColor() {
			return this.color;
		}
		
		public int getFocusedColor() {
			return this.colorFocused;
		}

		public int getRelativeX() {
			return relativeX;
		}

		public int getRelativeY() {
			return relativeY;
		}
	}

	private static String appendix = "...";
	
	private ArrayList textList;
	
	private ArrayList paintList;
	
	private int x = 0;
	
	private int y = 0;
	
	private int lines = 0;
	
	private int maxLines = 5;
	
	/**
	 * 
	 */
	public ActivityInfoItem() {
		this(null);
	}

	/**
	 * @param style
	 */
	public ActivityInfoItem(Style style) {
		super(style);
		this.textList = new ArrayList();
		this.paintList = new ArrayList();
		
		setAppearanceMode(Item.INTERACTIVE);
	}

	/**
	 * @param text
	 */
	public void add(String text) {
		add(text, new Style());
	}

	/**
	 * @param text
	 * @param style
	 */
	public void add(String text, Style style) {
		this.textList.add(new ActivityEntry(text, style));
	}

	/**
	 * 
	 */
	public void reset() {
		this.textList.clear();
		//this.isInitialized = false;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#createCssSelector()
	 */
	protected String createCssSelector() {
		return null;
	}
		
	/**
	 * 
	 * @param font
	 */
	void nextLine(Font font)
	{
		this.y += font.getHeight();
		this.x = 0;
		this.lines++;
	}
	
	/**
	 * @return
	 */
	boolean isMaxLines()
	{
		return this.lines == this.maxLines - 1;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#initContent(int, int, int)
	 */
	protected void initContent(int firstLineWidth, int availWidth,
			int availHeight) {
		this.paintList.clear();
		
		this.x = 0;
		this.y = 0;
		this.lines = 0;
		
		ActivityEntry entry = null;
		String text;
		Font font = null;
		for (int i = 0; i < this.textList.size(); i++) {
			entry = (ActivityEntry)this.textList.get(i);
			text = entry.getText();
			
			if(this.isFocused)
			{
				font = entry.getFont();
			}
			else
			{
				font = entry.getFocusedFont();
			}
			
			while(text.length() != 0)
			{
				int lineIndex = 0;
				String[] lines = TextUtil.wrap(text, font, availWidth - x, availWidth - x);
				
				if(lines[lineIndex].equals(""))
				{
					lineIndex++;
					
					if(x != 0)
					{
						nextLine(font);
					}
				}
				
				text = text.substring(lines[lineIndex].length(), text.length());
				
				String line = lines[lineIndex];
				
				if(this.x == 0)
				{
					line = line.trim();
				}
				
				if(isMaxLines() && lines.length >= lineIndex + 1)
				{
					line = line.substring(0, line.length() - appendix.length()) + appendix;
					
					this.paintList.add(new ActivityEntry(line,entry,this.x,this.y));
					
					break;
				}
				else
				{
					this.paintList.add(new ActivityEntry(line,entry,this.x,this.y));
				}
				
				if(lines.length > 1 + lineIndex)
				{
					nextLine(font);
				}
				else
				{
					this.x += font.stringWidth(lines[0]);
				}
			}
		}
		
		this.contentWidth = availWidth;
		this.contentHeight = this.y + (null != font ? font.getHeight() : 0);	
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#paintContent(int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	protected void paintContent(int x, int y, int leftBorder, int rightBorder,
			Graphics g) {
		for (int i = 0; i < this.paintList.size(); i++) {
			ActivityEntry entry = (ActivityEntry) this.paintList.get(i);

			if(this.isFocused)
			{
				g.setFont(entry.getFocusedFont());
				g.setColor(entry.getFocusedColor());
			}
			else
			{
				g.setFont(entry.getFont());
				g.setColor(entry.getColor());
			}
			
			g.drawString(entry.getText(), x + entry.getRelativeX(), y
					+ entry.getRelativeY(), Graphics.TOP | Graphics.LEFT);
		}
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Item#setStyle(de.enough.polish.ui.Style)
	 */
	public void setStyle(Style style) {
		super.setStyle(style);
		
		//#if polish.css.max-lines
		Integer maxLinesInt = style.getIntProperty("max-lines");
		if(maxLinesInt != null)
		{
			this.maxLines = maxLinesInt.intValue();
		}
		//#endif
	}
}
