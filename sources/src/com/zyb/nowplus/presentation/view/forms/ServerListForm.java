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
//#condition use.servers.list.choices
package com.zyb.nowplus.presentation.view.forms;

import javax.microedition.midlet.MIDlet;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.List;
import de.enough.polish.ui.Style;

import com.zyb.nowplus.business.ServiceBroker;

public class ServerListForm extends List implements CommandListener
{
	private static final String NAME_CMD_CLEAR_STORAGE="CLEAR_STORAGE";
	
		public ServerListForm(Object _obj,MIDlet _midlet)
		{
			this(_obj, _midlet, null);
		}
		
		public ServerListForm(Object _obj,MIDlet _midlet,Style style)
		{
			super( /*title*/null, List.IMPLICIT, style );
			
			obj=_obj;
			
			for(int i=0;i<NAMES.length;i++ )
				//#style choice_servers_list
				append( NAMES[i], null);
			
			//#style choice_servers_list
			append( NAME_CMD_CLEAR_STORAGE, null);

			setCommandListener(this);
		}
	  
	  public void commandAction(Command cmd, Displayable disp) 
	  {		
			if (disp != this) 
				return;
			
			if (cmd == List.SELECT_COMMAND) 
			{
				if (obj instanceof ServiceBroker)
				{
					if (getSelectedIndex() < NAMES.length)
					{
						AUTH_URL = URLS_AUTH[getSelectedIndex()];
						RPG_URL = URLS_RPG[getSelectedIndex()];
						((ServiceBroker) obj).applicationStarted();
	
					}
					else if (getString(getSelectedIndex()).equals(NAME_CMD_CLEAR_STORAGE))
						((ServiceBroker) obj).exit(true);
				}
				
				//FIXME thrown screen changed animation 
//				showAlert(URLS_AUTH[getSelectedIndex()]+"\n"+URLS_RPG[getSelectedIndex()]);
			} 
	  }
	  
		private Object obj;
		
		public static String AUTH_URL;
		public static String RPG_URL;
		
		//FIXME add list data into extra properties file instead of constants
		private final String[] NAMES = { "PROD","QA", "PRE", "ELLER" };
		private final String[] URLS_AUTH = 
		{ 
				"http://api.vodafone360.com/services/hessian", //PROD
				"http://devapi.next-vodafone.com/services/hessian/", //QA
				"https://api.preprod.vodafonepeople.com/services/hessian", //PRE
				"http://jupiter.next-vodafone.com/services/hessian/" //ELLER
		};
		
		private final String[] URLS_RPG = 
		{ 
				"http://rpg.vodafone360.com/rpg/mcomet/", //PRO
				"http://monstersinc04.next.vodafone.com:80/rpg/mcomet/", //QA
				"http://rpg.preprod.nowplus.com:80/rpg/mcomet/", //PRE
				"http://monstersinc01.next.vodafone.com:80/rpg/mcomet/" //ELLER
		};
}
