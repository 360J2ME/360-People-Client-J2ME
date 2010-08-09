//#condition polish.usePolishGui

/*
 * Created on 27-May-2005 at 18:54:36.
 * 
 * Copyright (c) 2010 Robert Virkus / Enough Software
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
package de.enough.polish.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.ui.Background;
import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Display;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.ScreenChangeAnimation;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.backgrounds.TranslucentSimpleBackground;

/**
 * <p>Moves the new screen from the bottom to the top.</p>
 * <p>Activate this animation by specifying it in the corresponding screen's style:
 * <pre>
 * .myAlert {
 * 		screen-change-animation: bottom;
 * 		bottom-screen-change-animation-speed: 4; ( 2 is default )
 * 		bottom-screen-change-animation-move-previous: true; ( false is default )
 * }
 * </pre>
 * </p>
 *
 * <p>Copyright (c) Enough Software 2005 - 2009</p>
 * <pre>
 * history
 *        27-May-2005 - rob creation
 * </pre>
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class SlideScreenChangeAnimation extends ScreenChangeAnimation {
	
	protected int currentY;
	//#if polish.css.bottom-screen-change-animation-speed
		private int speed = -1;
	//#endif
		
	Background overlay;
		
	/**
	 * Creates a new animation 
	 */
	public SlideScreenChangeAnimation() {
		super();
		
		//#if polish.css.repaint-previous-screen
			//#if polish.color.overlay:defined
				//#= this.overlay = new TranslucentSimpleBackground( ${polish.color.overlay} );
			//#else
				this.overlay = new TranslucentSimpleBackground( 0xAA000000 );
			//#endif
		//#endif
		
		this.supportsDifferentScreenSizes = true;
	}
	
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ScreenChangeAnimation#setStyle(de.enough.polish.ui.Style)
	 */
	protected void setStyle(Style style)
	{
		super.setStyle(style);
		if (this.isForwardAnimation) {
			this.currentY = 0;
		} else {
			this.currentY = this.screenHeight;
		}
		//#if polish.css.bottom-screen-change-animation-speed
			Integer speedInt = style.getIntProperty( "bottom-screen-change-animation-speed" );
			if (speedInt != null ) {
				this.speed = speedInt.intValue();
			} else {
				this.speed = -1;
			}
		//#endif
	}


	/* (non-Javadoc)
	 * @see de.enough.polish.ui.ScreenChangeAnimation#animate()
	 */
	protected boolean animate() {
		int adjust;
		//#if polish.css.bottom-screen-change-animation-speed
			if (this.speed != -1) {
				adjust = this.speed;
			} else {
		//#endif
				adjust = (this.screenHeight - this.currentY) / 3;
				if (adjust < 2) {
					adjust = 2;
				}
		//#if polish.css.bottom-screen-change-animation-speed
			}
		//#endif
		
		if (this.isForwardAnimation) {
			if (this.currentY < this.screenHeight) {
				this.currentY += adjust;
				return true;
			}
		} else if (this.currentY > 0) {
			this.currentY -= adjust;
			return true;
		}
		return false;
	}
	
	protected void onShow( Style style, Display dsplay, final int width, final int height, Displayable lstDisplayable, Displayable nxtDisplayable, boolean isForward ) {
		// reset the abort flag
		this.abort = false;
		
		this.screenWidth = width;
		this.screenHeight = height;
		this.display = dsplay;
		this.nextCanvas = (Canvas) nxtDisplayable;
		this.lastDisplayable = lstDisplayable;
		this.nextDisplayable = nxtDisplayable;
		Screen lastScreen = (Screen) (lstDisplayable instanceof Screen ? lstDisplayable : null);
		Screen nextScreen = (Screen) (nxtDisplayable instanceof Screen ? nxtDisplayable : null);
		
		Image lastScreenImage = toImage( lstDisplayable, nextScreen, lastScreen, width, height);
		Image nextScreenImage = toImage( nxtDisplayable, nextScreen, lastScreen, width, height);
		
		//#debug
		System.out.println("ScreenAnimation: showing screen transition " + this + " for transition from " + lstDisplayable + " to " + nxtDisplayable);
		
		this.lastCanvasImage = lastScreenImage;
		if (this.useLastCanvasRgb) {
			int lstWidth = lastScreenImage.getWidth();
			int lstHeight = lastScreenImage.getHeight();
			this.lastCanvasRgb = new int[ lstWidth * lstHeight ];
			//#if polish.midp2
				lastScreenImage.getRGB(this.lastCanvasRgb, 0, lstWidth, 0, 0, lstWidth, lstHeight );
			//#endif
		}
		
		this.nextCanvasImage = nextScreenImage;
		if (this.useNextCanvasRgb) {
			int nxtWidth = nextScreenImage.getWidth();
			int nxtHeight = nextScreenImage.getHeight();
			this.nextCanvasRgb = new int[ nxtWidth * nxtHeight ];
			//#if polish.midp2
				nextScreenImage.getRGB(this.nextCanvasRgb, 0, nxtWidth, 0, 0, nxtWidth, nxtHeight );
			//#endif
		}
		this.isForwardAnimation = isForward;
		setStyle( style );
	}


	/* (non-Javadoc)
	 * @see javax.microedition.lcdui.Canvas#paint(javax.microedition.lcdui.Graphics)
	 */
	public void paintAnimation(Graphics g) {
		if (this.isForwardAnimation) {
			paintAnimation(this.nextContentX,this.nextContentY,this.nextDisplayable, this.lastCanvasImage, g);
		} else {
			paintAnimation(this.lastContentX,this.lastContentY,this.lastDisplayable, this.nextCanvasImage, g);
		}
	}
	
	public void paintAnimation(int x, int y, Displayable displayable, Image image, Graphics g) {
		// draw next as image
		g.drawImage( image, 0, 0, Graphics.TOP | Graphics.LEFT );

		// draw last as a translated screen
		Screen screen = (Screen)displayable;
		
		//#if polish.css.repaint-previous-screen
		this.overlay.paint(0, 0, this.screenWidth, this.screenHeight, g);
		screen.repaintPreviousScreen = false;
		//#endif
		
		g.translate(0, this.screenHeight - this.currentY);
		screen.paint(g);
		g.translate(0,-g.getTranslateY());
		
		//#if polish.css.repaint-previous-screen
		screen.repaintPreviousScreen = true;
		//#endif
	}

}
