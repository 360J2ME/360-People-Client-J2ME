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
//#condition activate.embedded.360email

package com.zyb.nowplus.presentation.view.forms;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Email;
import com.zyb.nowplus.business.domain.ProfileSummary;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
import de.enough.polish.ui.UiAccess;

/**
 * Form class for creating a new email message
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class NewEmailForm extends BaseFramedForm
{
	/**
	 * Commands
	 */
	//TODO: add TK
//	public static final Command cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
	public final static Command cmdSend = new Command("Send", Command.SCREEN, 0);

	private Email emailSource;
	
	private StringItem fromField;
	private StringItem subjectField;
	
	private TextField bodyTextField;
	
	private TitleBarItem titleitem;
	
	public NewEmailForm(final Model model, final Controller controller, Email email) 
	{
		this(model,controller,email,null);
	}
	
	public NewEmailForm(final Model model, final Controller controller, Email email, Style style) 
	{
		super(model,controller,null,style);
		
		this.emailSource = email;
		
		createContent();
		
//		this.addCommand(cmdOptions);
		this.addCommand(cmdSend);
		this.addCommand(cmdBack);		
	}
	
	public void commandAction(Command c, Displayable d) 
	{
//		if(c == cmdOptions)
//		{
//			//use first person in froms list for profile item in Contextual menu
//			ProfileSummary first = null;
//			if(null != this.emailSource && null != this.emailSource.getFroms() && this.emailSource.getFroms().length > 0)
//			{
//				first = this.emailSource.getFroms()[0];
//			}
//			
//			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, first);
//		}
		if(c == cmdSend)
		{
			//TODO: Construct Email object based on inputfields contents and pass to controller

			Email email = null;
			
			getController().notifyEvent(Event.Context.EMAIL, Event.Email.SEND_EMAIL, email);
		}		
		else
			super.commandAction(c, d);
	}
	
	public void commandAction(Command c, Item item) 
	{
		super.commandAction(c, item);
	}

	protected Item createBottomItem() 
	{
		return null;
	}

	protected String createCssSelector() 
	{
		return null;
	}

	protected Item[] createStaticItems()
	{
		//TODO: add TK
		this.titleitem = new TitleBarItem( "Compose Email", getModel() ); 
		
		return new Item[]{ this.titleitem };
	}

	protected Item createTopItem()
	{
		return null;
	}

	public byte getContext()
	{
		return Event.Context.EMAIL;
	}

	protected void createContent()
	{
		//TODO: Add TKs
		
		//#style email_headerfield_expand
		this.fromField = new StringItem(null, "From: " + getFromsString(this.emailSource.getFroms()) );
		this.fromField.setAppearanceMode(Item.INTERACTIVE);
		this.add(fromField);
		
		//#style email_headerfield
		this.subjectField = new StringItem(null, this.emailSource.getSubject());
		this.subjectField.setAppearanceMode(Item.INTERACTIVE);
		this.add(subjectField);
		
		//#style email_bodyfield_input
		this.bodyTextField = new TextField(null, null, Integer.MAX_VALUE, TextField.ANY | TextField.INITIAL_CAPS_SENTENCE);
		this.bodyTextField.setSuppressCommands(true);
		this.add(bodyTextField);

		//init according to avail content
		//TODO: Include message thread when properly supported by polish
		if(null != this.emailSource)
			this.bodyTextField.setString(getResponseThread(emailSource.getBody()));
		
		UiAccess.focus(this, this.bodyTextField);
		this.bodyTextField.setCaretPosition(0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.view.forms.BaseFramedForm#handleEvent(byte, int, java.lang.Object)
	 */
	public void handleEvent(byte context, int event, Object data) 
	{
		if( context == Event.Context.EMAIL )
		{
			switch(event)
			{
				case Event.Email.UPDATE_EMAIL:
					break;
			}
		}
        else
            super.handleEvent(context, event, data);
	}
	
	private String getFromsString(ProfileSummary[] froms)
	{
		if(null != froms)
		{
			StringBuffer sb = new StringBuffer();
			
			for(int i = 0; i < froms.length; ++i)
			{
				if(i>0)
					sb.append(";");
				sb.append(froms[i].getFullName());
			}
			return sb.toString();
		}
		else
			return "";
	}
	
	private String getResponseThread(String thread)
	{
		if(null != thread)
		{
			return
			TextUtilities.CHAR_NEWLINE + " " +
			TextUtilities.CHAR_NEWLINE + " " +
			"---" + TextUtilities.CHAR_NEWLINE +
			thread;
		}
		else
			return "";
	}
}
