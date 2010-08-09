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

import javax.microedition.lcdui.Displayable;

import com.zyb.nowplus.MIDletContext;
import com.zyb.nowplus.business.Active;
import com.zyb.nowplus.business.Model;
import com.zyb.nowplus.business.domain.ContactProfile;
import com.zyb.nowplus.business.event.EventDispatcherTask;
import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.FormStateResetter;
import com.zyb.nowplus.presentation.ExtendedScreenHistory.DisplayableContainer;
import com.zyb.nowplus.presentation.view.forms.ProfileForm;
import com.zyb.util.Queue;
import com.zyb.util.SafeRunnable;
import com.zyb.util.SafeThread;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventDispatcher;
import com.zyb.util.event.EventListener;
import de.enough.polish.ui.Screen;
import de.enough.polish.util.IntHashMap;

/*
//#if polish.Display.useUserInputValidation
import de.enough.polish.ui.Display.UserInputValidator;
//#endif
 */

//#if polish.device.supports.nativesms == false
import com.zyb.nowplus.presentation.controller.SmsController;
//#endif

/* This is the super class instance containing subclass controllers dedicated to handling user gestures from various parts of the app
 * */
public class Controller extends EventDispatcher implements SafeRunnable, EventListener, FormStateResetter, Active{
	
	/*
	 * Global key supressors, to be used post R1 for supressing e.g. keypad keys in BaseForm
	public static UserInputValidator supressAll, allowAll;
	
	static
	{
		supressAll = new UserInputValidator()
		{
			public boolean isKeyPressValid(int keyCode)
			{
				return false;
			}
			public boolean isKeyReleaseValid(int keyCode)
			{
				return false;
			}
			public boolean isKeyRepeatedValid(int keyCode)
			{
				return false;
			}
			public boolean isPointerPressValid(int x, int y)
			{
				return false;
			}
			public boolean isPointerReleaseValid(int x, int y)
			{
				return false;
			}
			public boolean isPointerDragValid(int x, int y)
			{
				return false;
			}
		};
		
		allowAll = new UserInputValidator()
		{
			public boolean isKeyPressValid(int keyCode)
			{
				return true;
			}
			public boolean isKeyReleaseValid(int keyCode)
			{
				return true;
			}
			public boolean isKeyRepeatedValid(int keyCode)
			{
				return true;
			}
			public boolean isPointerPressValid(int x, int y)
			{
				return true;
			}
			public boolean isPointerReleaseValid(int x, int y)
			{
				return true;
			}
			public boolean isPointerDragValid(int x, int y)
			{
				return true;
			}
		};
	}
	*/
		
	private SafeThread thread;
	
	/**
	 * the event queue
	 */
	private final Queue eventQueue;
	
	private Model model;
	
	/**
	 * the context controllers
	 */
	private IntHashMap controllers;
	
	/**
	 * the app screen stack
	 */
	private ExtendedScreenHistory history;
	
	/**
	 * Creates a new Controller instance
	 */
	public Controller(final MIDletContext midlet, Model model) 
	{
		//#debug debug
		System.out.println("Constructing ui controller");
		
		this.history = new ExtendedScreenHistory(midlet, this);
		this.eventQueue = new Queue();
		this.controllers = new IntHashMap();
		this.thread = new SafeThread(this);
		
		this.model = model;
		this.model.attachActive(this);
		initContexts();
	}
	
	private void initContexts()
	{
		attach(new AppController(this.model,this, history));
		attach(new ContactController(this.model,this, history));
		attach(new ProfileController(this.model,this, history));
		attach(new AuthenticationController(this.model,this,history));
		attach(new ContextualMenuController(this.model,this,history));
		attach(new ChatController(this.model,this,history));
		attach(new EditProfileController(this.model,this,history));		
		attach(new NavigationController(this.model,this,history));		
		attach(new SettingsController(this.model,this,history));		
		attach(new WebAccountsController(this.model,this,history));
		attach(new SignupController(this.model,this,history));
		attach(new SyncController(this.model,this,history));
		
		//#if activate.embedded.360email
		attach(new EmailController(model, this, history));
		//#endif

		//#if polish.device.supports.nativesms == false
		attach(new SmsController(model, this, history));
		//#endif
	}
	
	public void notifyEvent(byte context, int id, Object data)
	{
		//#debug debug
		System.out.println("context:"+context+"/event:"+id);

		if (context == Event.Context.APP &&
				id == Event.App.START)
		{
			//#debug info
			System.out.println("Starting ui dispatcher thread");
			
			thread.start("controller");
		}
		
		//check if controller has contextcontroller
		ContextController controller = (ContextController)this.controllers.get(context);
		
		if(controller != null)
		{
			Event event = new Event(context, id, data);
			
			
			if(context == Event.Context.NAVIGATION)//Navigation has highest priority
				eventQueue.pushPriority(event);
			else			
				eventQueue.push(event);
			
		}
		else
		{
			//#debug info
			System.out.println("could not find controller for context " + context);
		}
	}
	
	public void cancelEvent(byte context, int id, Object data) throws InterruptedException
	{
		Event event = new Event(context, id, data);
		eventQueue.cancel(event);		
	}
	

	public void init() 
	{
	}	

	public void work() {
		try 
		{
			Event event = (Event) eventQueue.pop();
		
			//#debug debug
			System.out.println("dispatch " + event);
			
			//#debug eventtrace
			System.out.println("EVENT: "+"context = " + event.getContext() + ", event ="+event.getId());
			
			if(event.getContext() == Event.Context.APP)
			{
				dispatchToAll(event);
				
				if(event.getId() == Event.App.STOP)
				{
					thread.stop();
				}
			}
			else
			{
				/* //Event must be dispatcher to all controllers after removal of View class
				 * dispatch(event);
				 */
				
				dispatchToAll(event);
			}
			
			//#debug debug
			System.out.println("dispatch done");
		}
		catch (InterruptedException e)
		{
			//#debug error
			System.out.println("Interrupted." + e);
			
			thread.stop();
		}
	}
	
	public void cleanUp() 
	{
	}

	public void releaseMemory() 
	{
	}	
	
	void dispatchToAll(Event event)
	{
		int[] keys = this.controllers.keys();
		
		for (int index = 0; index < keys.length; index++) {
			EventListener controller = (EventListener)this.controllers.get(keys[index]);
			//#debug debug
			System.out.println("Send event " + event + " to " + controller);
			controller.handleEvent(event.getContext(), event.getId(), event.getData());
		}
	}
	
	void dispatch(Event event) {
		EventListener controller = (EventListener)this.controllers.get(event.getContext());
		
		//#debug debug
		System.out.println("handling event with context controller " + controller);
		
		controller.handleEvent(event.getContext(), event.getId(), event.getData());
	}

	public EventDispatcherTask scheduleEvents(byte context, int id, long delay)
	{
		return null;
	}
	
	public EventDispatcherTask scheduleEvents(byte context, int id, long delay, long period)
	{
		return null;
	}
	
	public void attach(EventListener listener) {
		this.controllers.put(listener.getContext(), listener);
	}

	public Event getLatestEvent(byte context) {
		return null;
	}
	
	public void detach(EventListener listener) {
		this.controllers.remove(listener.getContext());
	}
	
	public void waitForFinish()
	{
		try
		{
			thread.join();
		}
		catch (InterruptedException e)
		{
			//#debug error
			System.out.println("Wait for finish interrupted." + e);
		}
	}

	public byte getContext() {
		return Event.Context.ALL;
	}

	public void handleEvent(byte context, int event, Object data) {
		notifyEvent(context, event, data);
	}
	
	/**
	 * Returns the model
	 * @return the model
	 */
	protected final Model getModel()
	{
		return this.model;
	}

	/* (non-Javadoc)
	 * @see com.zyb.nowplus.presentation.FormStateResetter#resetStateWith(javax.microedition.lcdui.Displayable, java.lang.Object)
	 */
	public void resetStateWith(Displayable disp,
			Object obj) {

		//#debug debug
		System.out.println("resetStateWith():"+disp+"/"+obj);
		
		if(disp instanceof ProfileForm && obj != null && obj instanceof ContactProfile)
		{
			//((ProfilePageDetailsTab)disp).updateContact((ContactProfile)obj);
		}
	}

	
	//#mdebug error
	public String toString()
	{
		return "Controller[]";
	}
    //#enddebug
	
	/* (non-Javadoc)
	 * @see com.zyb.nowplus.business.Active#isBusy()
	 */
	public boolean isBusy()
	{
		DisplayableContainer dispCont = this.history.currentGlobalAndLocalDisplayableContainer();
		if(dispCont == null || dispCont.disp == null)
			return false;
		
		if(dispCont.disp instanceof Screen)
		{
			boolean isbusy = ((Screen)dispCont.disp).isInteracted(5000);//Last 2 sec
			
			//#debug debug
			System.out.println("isbusy:"+isbusy);
			return isbusy;
		}
		return false;
	}
}
