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
package com.zyb.util;



import de.enough.polish.browser.Browser;
import de.enough.polish.browser.TagHandler;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Style;
import de.enough.polish.util.HashMap;
import de.enough.polish.util.TextUtil;
import de.enough.polish.xml.SimplePullParser;

/**
 * <p>TagHandler removing &lt;a&gt; and &lt;/a&gt; tags.</p>
 * 
 * <p>parent - TagHandler ATagHandler will be registered to.</p>
 * 
 * @author Jakub Kalisiak
 * 
 */
public class ATagHandler extends TagHandler{
	private TagHandler parent;

	public ATagHandler( TagHandler parent ) {
		this.parent = parent;
	}

	public void register(Browser browser)
	{
		browser.addTagHandler("a", this);
	}

	public void register2(Browser browser)
	{
		browser.addTagHandler("title", this);
		
	}
	
	public boolean handleTag(Container parentItem, SimplePullParser parser, 
			String tagName, boolean opening, HashMap attributeMap, Style style)
	{
		//System.out.println("tag=" + tagName + ", class=" + attributeMap.get("class") 
			//+ ", id=" + attributeMap.get("id") + ", style=" + style);
		String elementClass = (String) attributeMap.get("class");
		if ( TextUtil.equalsIgnoreCase("a", tagName) )
		{
			/*if (opening)
			{
			;	
			}
			else
			{
			;	
			}*/
			return true;
		}
		if (TextUtil.equalsIgnoreCase("title", tagName))
		{
			/*if (opening)
			{
			;	
			}
			else
			{
			;	
			}*/	
			return true; 
		}
		if (this.parent == null) { //this forwards handling inner tags to other handler
			return false;
		} else {
			//#debug
			System.out.println("forwarding tag " + tagName + ", opening=" + opening + ", elementClass=" + elementClass);//collectData=" + this.collectData + ", 
			return this.parent.handleTag(parentItem, parser, tagName, opening, attributeMap, style);
		}
	}
}
