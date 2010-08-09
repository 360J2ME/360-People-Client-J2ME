//#condition polish.usePolishGui
/*
 * Created on 31-Mar-2009 at tea time.
 * 
 * Copyright (c) 2009 Andre Schmidt / Enough Software
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
package de.enough.polish.ui.texteffects;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import de.enough.polish.ui.AnimationThread;
import de.enough.polish.ui.ClippingRegion;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextEffect;

/**
 * <p>A text effect that scrolls through the wrapped lines</p>
 *
 * <p>Copyright (c) Enough Software 2005 - 2009</p>
 * <pre>
 * history
 *        31-Mar-2009 - asc creation
 * </pre>
 * @author Andre Schmidt, j2mepolish@enough.de
 */
public class VerticalScrollTextEffect extends TextEffect{

	//transient Item parent;

	public static final int STAGE_SHOW = 0x00;
	public static final int STAGE_SCROLL = 0x01;
	
	int stageInterval = 2000;
	
	int maxLines = 1;
	
	int lines = 1;
	
	int lineHeight = 0;
	int drawCount = 0;
		
	String[] textLines = null;
	String[] drawLines = null;
	
	long stageTime = 0;
	int stageCurrent = STAGE_SHOW;
	
	int lineIndex = 0;
	int lineOffset = 0;
	
	int lastLineWidth = -1;
	
	boolean needsAnimation = false;
	
	public VerticalScrollTextEffect()
	{
		this.isTextSensitive = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#animate(de.enough.polish.ui.Item, long, de.enough.polish.ui.ClippingRegion)
	 */
	public void animate(Item parent, long currentTime, ClippingRegion repaintRegion) 
	{
		if (animate()) {
			parent.addRelativeToContentRegion( repaintRegion, 0, 0, parent.itemWidth, parent.itemHeight );
		}
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#animate()
	 */
	public boolean animate() {
		if(this.textLines != null && this.textLines.length != 1){
			long currentTime = System.currentTimeMillis();
			
			if (this.stageTime == 0) {
				this.stageTime = currentTime;
				return true;
			}
			
			// get the time passed since last animation
			long timePassed = currentTime - this.stageTime;
			
			switch(this.stageCurrent)
			{
				case STAGE_SHOW : 
					if (timePassed > this.stageInterval) {
						//#debug debug
						System.out.println("stage changed to STAGE_SCROLL");
	
						this.stageCurrent = STAGE_SCROLL;
						this.stageTime = currentTime;
					}
					return false;
				case STAGE_SCROLL : 
					this.lineOffset = getLineOffset(timePassed, this.lineHeight );
					
					// if the interval time has passed ... 
					if (timePassed > this.stageInterval) {
						//#debug debug
						System.out.println("stage change to STAGE_SHOW");
						
						this.lineIndex = (this.lineIndex + 1) % this.textLines.length;
						this.lineOffset = 0;
						
						this.stageCurrent = STAGE_SHOW;
						this.stageTime = currentTime;
					}
					return true;
			};
		}
		
		return false;
	}
	
	/**
	 * Calculates and returns the offset to draw the textlines
	 * for the animation  
	 * @param timePassed the passed time since the last animation
	 * @param lineHeight the line height
	 * @return the offset
	 */
	public int getLineOffset(long timePassed, int lineHeight)
	{
		int progress = (int)(timePassed * 1000 / this.stageInterval) * 100 / 1000;
		return (lineHeight * 1000 / 100) * progress / 1000;
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#drawStrings(java.lang.String[], int, int, int, int, int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	public void drawStrings(Item parent, String[] textLines, int textColor, int x, int y,
			int leftBorder, int rightBorder, int lineHeight, int maxWidth,
			int layout, Graphics g) {
		
		this.lineHeight = lineHeight;
		
		int index = this.lineIndex;
		for (int i = 0; i < this.drawLines.length; i++) {
			this.drawLines[i] = this.textLines[(index + i) % this.textLines.length];
		}
		
		int clipX = g.getClipX();
		int clipY = g.getClipY();
		int clipWidth = g.getClipWidth();
		int clipHeight = g.getClipHeight();
		
		int linesHeight;
		
		linesHeight = lineHeight * (this.lines);
		
		//#if polish.Bugs.needsBottomOrientiationForStringDrawing
			g.clipRect(x, y - linesHeight, rightBorder - leftBorder, linesHeight);
		//#else
			g.clipRect(x, y, rightBorder - leftBorder, linesHeight);
		//#endif
		
		leftBorder = x;
		
		super.drawStrings(this.drawLines, textColor, x, y - this.lineOffset, leftBorder, rightBorder,
				lineHeight, maxWidth, layout, g);
		
		g.setClip(clipX, clipY, clipWidth, clipHeight);
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#wrap(de.enough.polish.ui.Item, java.lang.String, int, javax.microedition.lcdui.Font, int, int, int, java.lang.String, int)
	 */
	public String[] wrap(Item item, String text, int textColor, Font font, int firstLineWidth, int lineWidth, int maxLinesParam, String maxLinesAppendix, int maxLinesAppendixPosition) {
		return wrap( item, text, textColor, font, firstLineWidth, lineWidth );
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#wrap(java.lang.String, int, javax.microedition.lcdui.Font, int, int)
	 */
	public String[] wrap(Item parent, String text, int textColor, Font font,
			int firstLineWidth, int lineWidth) {
		StringItem parentItem = (StringItem)parent;
		
		this.textLines = super.wrap(text, textColor, font, firstLineWidth, lineWidth);
		
		if(this.textLines.length > this.maxLines)
		{
			 this.drawLines = new String[this.maxLines + 1];
			 this.lines = this.maxLines;
			 
			 this.needsAnimation = true;
			 
			 AnimationThread.addAnimationItem(parent);
		}
		else
		{
			this.drawLines = this.textLines;
			this.lines = this.textLines.length;
			
			this.needsAnimation = false;
			
			AnimationThread.removeAnimationItem(parent);
		}

		
		String[] resultLines = new String[this.lines];
		
		for (int index = 0; index < resultLines.length; index++) {
			resultLines[index] = this.textLines[index];
		}
		
		return resultLines;
	}
	
	public void onAttach(Item parent) {
		if(parent.isInitialized() && this.needsAnimation) {
			AnimationThread.addAnimationItem(parent);
		}
	}
	
	public void onDetach(Item parent) {
		//reset the lineOffset on the end of animation
		this.lineOffset = 0;
		AnimationThread.removeAnimationItem(parent);
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#drawString(java.lang.String, int, int, int, int, javax.microedition.lcdui.Graphics)
	 */
	public void drawString(String text, int textColor, int x, int y,
			int anchor, Graphics g) {
		//#if polish.blackberry
		g.setFont(style.getFont());
		//#endif
		g.setColor(textColor);
		g.drawString(text, x, y, anchor);
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#setStyle(de.enough.polish.ui.Style)
	 */
	public void setStyle(Style style) {
		super.setStyle(style);
		
		//#if polish.css.vertical-scroll-interval
		Integer stageIntervalInt = style.getIntProperty("vertical-scroll-interval");
		if(stageIntervalInt != null)
		{
			this.stageInterval = stageIntervalInt.intValue();
		}
		//#endif
		
		//#if polish.css.vertical-scroll-max-lines
		Integer maxLinesInt = style.getIntProperty("vertical-scroll-max-lines");
		if(maxLinesInt != null)
		{
			this.maxLines = maxLinesInt.intValue();
		}
		//#endif
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.TextEffect#getMaxWidth(java.lang.String[])
	 */
	public int getMaxWidth(Item parent, String[] lines) {
		return super.getMaxWidth(parent, this.textLines);
	}
}
