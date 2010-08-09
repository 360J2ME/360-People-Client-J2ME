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

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;

/**
 * This TouchStringItemButton allows a StringItem to change his Style only if it  was pressed
 * 
 * @author Marcel Schröder
 *
 */
public class TouchStringItemButton extends StringItem{

        Style focusedStyle;
        Style normalStyle;
        
        public TouchStringItemButton(String label, String text, Style style) {
            super(label, text, style);
            this.normalStyle = style;
        }

        public void setFocusedStyle(){
            //do nothing
        }
        
        public void setFocusedStyle(Style style){
        	this.focusedStyle = style;
        }
        

        protected void defocus(Style originalStyle) {
        	super.defocus(this.normalStyle);
        }


        protected Style focus(Style newStyle, int direction) {
        	return super.focus(this.focusedStyle, direction);
        }

        
       public boolean handlePointerTouchDown(int x, int y) {
          if (this.isInItemArea(x, y)){
        	  super.focus(this.focusedStyle, 0);
          }
          return super.handlePointerTouchDown(x, y);
       }

        
        public boolean handlePointerTouchUp(int x, int y) {
                boolean erg = super.handlePointerTouchUp(x, y);
                        super.defocus(this.normalStyle);              
                return erg;
        } 
}
