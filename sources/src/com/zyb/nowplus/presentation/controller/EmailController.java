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
package com.zyb.nowplus.presentation.controller;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.Email;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.view.forms.NewEmailForm;
import com.zyb.nowplus.presentation.view.forms.ShowEmailForm;
import com.zyb.util.event.Event;

/**
 * Controller for handling event associated with Email illustration and creation
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class EmailController extends ContextController 
{
	private ShowEmailForm sef;
	
	private NewEmailForm nef;
	
	public EmailController(Model model, Controller controller)
	{
		super(model, controller);
	}
	
	public EmailController(Model model, Controller controller,
			ExtendedScreenHistory history) {
		super(model, controller, history);
	}

	
	public byte getContext() 
	{
		return Event.Context.EMAIL;
	}

	public void handleEvent(byte context, int event, Object data) 
	{
		if(context == getContext())
		{
			switch (event) 
			{
				case Event.Email.OPEN_EMAIL:
					if(null != data && data instanceof Integer)
					{
						//#style base_form
						this.sef = new ShowEmailForm(model, controller, (Integer)data);
						
						getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, this.sef);
					}
					
					break;
				
				case Event.Email.COMPOSE_EMAIL:
					
					Email email = null;
					
					if(null != data && data instanceof Email)
						email = (Email) data;
					
					//#style base_form
					this.nef = new NewEmailForm(model, controller, email);
						
					getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.NEXT_CHECKPOINT, this.nef);
					
					break;		
					
				case Event.Email.SEND_EMAIL:
					
					if(null != data && data instanceof Email)
					{
						//TODO: Send email
					}
					
					break;						
			}
		}
	}
}
