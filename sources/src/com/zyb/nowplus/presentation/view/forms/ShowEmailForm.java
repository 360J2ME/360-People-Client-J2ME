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
import com.zyb.nowplus.presentation.view.items.ProgressIndicatorItem;
import com.zyb.nowplus.presentation.view.items.TitleBarItem;
import com.zyb.util.event.Event;

import de.enough.polish.ui.Command;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.Style;
import de.enough.polish.util.Locale;

/**
 * Form class for showing a single email
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ShowEmailForm extends BaseFramedForm
{
	/**
	 * Commands
	 */
	//TODO: add TK
	public static final Command cmdOptions = new Command(Locale.get("nowplus.client.java.command.options"), Command.SCREEN, 0);
	public final static Command cmdReply = new Command("Reply", Command.OK, 0);
	public final static Command cmdExpand = new Command("Expand", Command.OK, 0);

	private Email emailSource;
	private int id;
	
	private StringItem fromField;
	private StringItem subjectField;
	private StringItem dateField;
	private StringItem bodyField;
	
	private Container subjectContainer;
	
	private TitleBarItem titleitem;
	
	private NotificationForm loadEmailNotifier;

	public ShowEmailForm(final Model model, final Controller controller, Integer id) 
	{
		this(model,controller,id,null);
	}
	
	public ShowEmailForm(final Model model, final Controller controller, Integer id, Style style) 
	{
		super(model,controller,null,style);
		
		if(null != id)
			this.id = id.intValue();
		
		createContent();
		
		this.addCommand(cmdOptions);
		this.addCommand(cmdBack);		
	}
	
	public void commandAction(Command c, Displayable d) 
	{
		if(c == cmdOptions)
		{
			//use first person in froms list for profile item in Contextual menu
			ProfileSummary first = null;
			if(null != this.emailSource && null != this.emailSource.getFroms() && this.emailSource.getFroms().length > 0)
			{
				first = this.emailSource.getFroms()[0];
			}
			
			getController().notifyEvent(Event.Context.CONTEXTUAL_MENU, Event.ContextualMenu.OPEN, first);
		}
		else
			super.commandAction(c, d);
	}
	
	public void commandAction(Command c, Item item) 
	{
		if(cmdReply == c)
		{
			getController().notifyEvent(Event.Context.EMAIL, Event.Email.COMPOSE_EMAIL, emailSource);
		}
		else
		if(cmdExpand == c)
		{
			//TODO: Expand email details
		}
		else
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
		this.titleitem = new TitleBarItem( "Email", getModel() ); 
		
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
		this.fromField = new StringItem("From:", null);
		this.fromField.setAppearanceMode(Item.INTERACTIVE);
		this.fromField.addCommand(cmdExpand);
		this.fromField.setItemCommandListener(this);
		this.add(fromField);
		
		//#style email_subjectfield_container
		this.subjectContainer = new Container(false);
		this.subjectContainer.setAppearanceMode(Item.INTERACTIVE);
		this.add(this.subjectContainer);
		
		//#style email_subjectfield_subject
		this.subjectField = new StringItem(null, null);
		this.subjectContainer.add(subjectField);
		
		//#style email_subjectfield_date
		this.dateField = new StringItem(null, null);
		this.subjectContainer.add(dateField);
		
		//#style email_bodyfield
		this.bodyField = new StringItem(null, null);
		this.bodyField.setAppearanceMode(Item.INTERACTIVE);
		this.bodyField.addCommand(cmdReply);
		this.bodyField.setItemCommandListener(this);
		this.add(bodyField);
		
		//#style notification_form_progress
		this.loadEmailNotifier = new NotificationForm(
				model, controller, 
				"Loading Email",
				null,
				null,
				0, false, ProgressIndicatorItem.PROGRESS_INFINITE
				);
		
		getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT, this.loadEmailNotifier); //NOT NEXT_GLOBAL
		
		//open message
		getModel().openMessage(this.id);
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
					
					Email email = getModel().getCurrentMessage2();
					
					if(null != email)
					{
						this.emailSource = email;

						//remove notification
						if(null != this.loadEmailNotifier);
							getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.DISMISS, this.loadEmailNotifier);
						
						//update fields	
						this.fromField.setText( getFromsString(this.emailSource.getFroms()) );
						this.subjectField.setText( this.emailSource.getSubject() );
						this.dateField.setText( this.emailSource.getDate() );
						this.bodyField.setText( this.emailSource.getBody() );
					}
					
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
}
