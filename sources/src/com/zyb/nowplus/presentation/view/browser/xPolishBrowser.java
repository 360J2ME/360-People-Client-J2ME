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
package com.zyb.nowplus.presentation.view.browser;

import de.enough.polish.browser.html.*;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.StringTokenizer;
import de.enough.polish.util.TextUtil;

/**
 * Polish browser extension to use less memory.
 * Different tokenizer delimiter is used so less memory is consumed. 
 * 
 * 
 * @author jakub.kalisiak@mobica.com
 *
 */
public class xPolishBrowser extends HtmlBrowser{
	
	protected void handleText(String text)
	{
		
		if (text.length() > 0)
		{
			StringTokenizer st = new StringTokenizer(text, "\n");

			while (st.hasMoreTokens())
			{
				String str = st.nextToken();
				str = TextUtil.replace(str, "&nbsp;", " ");
				StringItem stringItem = null;
				if (this.htmlTagHandler.textStyle != null) {
					stringItem = new StringItem(null, str, this.htmlTagHandler.textStyle);
				} 
				else 
					if (this.htmlTagHandler.textBold && this.htmlTagHandler.textItalic)
					{
						//#style browserTextBoldItalic
						stringItem = new StringItem(null, str);
					}
					else if (this.htmlTagHandler.textBold)
					{
						//#style browserTextBold
						stringItem = new StringItem(null, str);
					}
					else if (this.htmlTagHandler.textItalic)
					{
						//#style browserTextItalic
						stringItem = new StringItem(null, str);
					}
					else
					{
						//#style browserText
						stringItem = new StringItem(null, str);
					}
				add(stringItem);
			}
		}
		
	}

}
