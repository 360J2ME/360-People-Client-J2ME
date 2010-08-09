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
package com.zyb.nowplus.presentation.controller;

import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.util.event.EventListener;

public abstract class ContextController implements EventListener{

	/**
	 * the model
	 */
	final Model model;
	
	/**
	 * the controller
	 */
	protected final Controller controller;
	
	/**
	 * screen history reference
	 */
	protected ExtendedScreenHistory history;

	/**
	 * Creates a new ContextController instance
	 * @param model the model
	 */
	public ContextController(Model model, Controller controller)
	{
		this.model = model;
		this.controller = controller;
		this.history = null;
	}
	
	/**
	 * Creates a new ContextController instance
	 * @param model the model
	 */
	public ContextController(Model model, Controller controller, ExtendedScreenHistory history)
	{
		this.model = model;
		this.controller = controller;
		this.history = history;
	}
	
	/**
	 * Returns the model
	 * @return the model
	 */
	protected final Model getModel()
	{
		return this.model;
	}
	
	/**
	 * Returns the controller
	 * @return the controller
	 */
	protected final Controller getController()
	{
		return this.controller;
	}

	//#mdebug error
	public String toString()
	{
		return "ContextController[]";
	}
    //#enddebug

}
