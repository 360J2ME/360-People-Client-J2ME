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
package com.zyb.nowplus.presentation;

import java.util.Enumeration;
import java.util.Stack;

import javax.microedition.lcdui.Displayable;

import com.zyb.nowplus.MIDletContext;
import com.zyb.nowplus.presentation.view.forms.BaseForm;

import de.enough.polish.ui.Screen;
import de.enough.polish.util.ArrayList;

//#if polish.blackberry
import net.rim.device.api.ui.UiApplication;
//#endif

/**
 * This class provides means of controlling the screen history.
 * There are two stacks, one with alerts and global screens which precedes the second normal screen stack
 * 
 * There is a new screen history in place. Here is how it works.
 * First part is a regular stack where displayables are added and back is called.
 * 
 * - next(displayable,stateobj,checkpoint)
 * - next(displayable,stateobj) same as above but with checkpoint==true as default, you therefore need explicit to set if it should not be a checkpoint
 * - back(checkpoint)
 * - back() same as above but with checkpoint==false as default, you therefore need to explicit set if it should revert back to last checkpoint
 * 
 * Stateobj is an associated object which can be used to describe the current state of the displayable. 
 * The implemented FormStateResetter interface passed during init() will take care of how this is handled.
 * 
 * The second part is a stack of globally visible displayables, meaning screens which takes precedence 
 * over all other displayables. Examples are login screen and alerts notifying the user about lost connectivity.
 * - displayGlobal(displayable,stateobj)
 * 
 * Because globals by nature are async they must be dismissed individually using
 * - dismissGlobal(displayable)
 * 
 * Finally there is a displayGlobalAlert() which can make life easier to create uniform alerts.
 * 
 * You can display as many alerts as you wish, but if they are identical only one will be shown.
 * displayGlobalAlert() will return the created or active alert which reference is used for dismissing. 
 * But you will probably use the displayable passed as argument in commandAction()
 * @author Jens Vesti
 */
public class ExtendedScreenHistory {

	private final Stack navigationDisplayHistory = new Stack();
	private final Stack navigationAlertHistory = new Stack();//Alerts always have higher precedence than other displayables 
	private MIDletContext context;
	private FormStateResetter resetter;

	public ExtendedScreenHistory()
	{
	}

	public ExtendedScreenHistory(final MIDletContext context, final FormStateResetter resetter)
	{
		//#debug debug
		System.out.println("Construct form history");
		
		init(context,resetter);
	}
	
	public void clearHistory()
	{
		navigationDisplayHistory.removeAllElements();
		navigationAlertHistory.removeAllElements();
	}
	
	/************************************************************************************************************************
	 * Sets the display which we use to show displayables
	 */
	public void init(final MIDletContext context, final FormStateResetter resetter)
	{
		this.context = context;
		this.resetter = resetter;
	}
	
	/************************************************************************************************************************
	 * Display a displayable globally taking precedence over any other displayables
	 * @param disp which is the displayable to show
	 */
	public Displayable displayGlobal(final Displayable disp, final Object stateObj)
	{
		//#debug debug
		System.out.println("setting alert");

		Displayable retDisp = disp;
		
		boolean doPush = true;
		
		//If it exists in stack we remove it to avoid showing it several times
		if(navigationAlertHistory.size()>=1)
		{
			final Enumeration elements = navigationAlertHistory.elements();
			while(elements.hasMoreElements())
			{
				final Object obj = elements.nextElement();
				
				if(disp == ((DisplayableContainer)obj).disp)
				{
					retDisp = ((DisplayableContainer)obj).disp;
					doPush = false;
				}
			}
		}
		
		//...and push it again
		if(doPush)
		{
			//#debug debug
			System.out.println("Adding:"+disp);
			navigationAlertHistory.push(new DisplayableContainer(disp,stateObj,false));
		}
		displayCurrent(false);

		return retDisp;
	}

	/************************************************************************************************************************
	 * Dismisses an alert/displayable
	 */
	public void dismissGlobal(final de.enough.polish.ui.Displayable disp)
	{
		//#debug debug
		System.out.println("Trying to dismiss");
		
		if(disp == null)
			return;
		
		final ArrayList skipped = new ArrayList();
		final Enumeration elements = navigationAlertHistory.elements();
		while(elements.hasMoreElements())
		{
			final Object obj = elements.nextElement();

			if(((DisplayableContainer)obj).disp == disp)
			{
				//#debug debug
				System.out.println("Removing:"+((DisplayableContainer)obj).disp);
				navigationAlertHistory.removeElement(obj);

				
				if(((DisplayableContainer)obj).disp instanceof Screen) {
					Screen screen =((Screen)((DisplayableContainer)obj).disp);
					skipped.add(screen);
				}
			}
		}

		//#if polish.blackberry
			// //#= synchronized(UiApplication.getEventLock())
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
		//#endif
			{
				displayCurrent(true);
				
				for (int i = 0; i < skipped.size(); i++) {
					Screen screen = (Screen)skipped.get(i);
					screen.destroy();
				}
			}
		//#if polish.blackberry
		});
		//#endif
	}
	
	/** 
	 * Will dismiss any displayable regardless alert or displayable stack
	 * @param disp
	 */
	public void dismiss(final de.enough.polish.ui.Displayable disp) 
	{
		//Find in display history and remove if it exists here
		final Enumeration elements = navigationDisplayHistory.elements();
		while(elements.hasMoreElements())
		{
			final Object obj = elements.nextElement();

			if(((DisplayableContainer)obj).disp == disp)
			{
				navigationDisplayHistory.removeElement(obj);
				
				if(((DisplayableContainer)obj).disp instanceof Screen)
					((Screen)(((DisplayableContainer)obj).disp)).destroy();

			}
			
		}
		
		//Else remove it from alert history. Will for next current displayable to be displayed.
		dismissGlobal(disp);

	}
	
	
	/************************************************************************************************************************
	 * Replace the existing local displayable, not global.
	 * NB State data and checkpoint is kept!
	 * @param disp which is the displayable to show
	 */
	public void replaceCurrentDisplayable(final Displayable disp)
	{
		if(navigationDisplayHistory.size()>=1)
			((DisplayableContainer)navigationDisplayHistory.peek()).disp = disp;
		
		displayCurrent(true);
	}

	
	/************************************************************************************************************************
	 * Return current Displayable in display history, that is not incl globals
	 */
	public DisplayableContainer currentLocalDisplayableContainer()
	{
		if(navigationDisplayHistory.size()>=1)
			return (DisplayableContainer)navigationDisplayHistory.peek();
		
		return null;
	}

	
	/************************************************************************************************************************
	 * Return current Displayable
	 */
	public DisplayableContainer currentGlobalAndLocalDisplayableContainer()
	{
		
		if(navigationAlertHistory.size()>=1)
			return (DisplayableContainer)navigationAlertHistory.peek();
		else
		if(navigationDisplayHistory.size()>=1)
			return (DisplayableContainer)navigationDisplayHistory.peek();
		
		return null;
	}
	
	
	/************************************************************************************************************************
	 * Display the current screen
	 */
	public boolean displayCurrent(final boolean triggerStateUpdate)
	{
		final DisplayableContainer dispCont = currentGlobalAndLocalDisplayableContainer();
		if(dispCont != null)
		{
			if(dispCont.disp != null)
			{
				if(dispCont.stateData != null && triggerStateUpdate)
					this.resetter.resetStateWith(dispCont.disp, dispCont.stateData);

				//#debug debug
				System.out.println("dispCont.disp:"+dispCont.disp);
				
				// prepare form if BaseForm
				if(dispCont.disp instanceof BaseForm) {
					BaseForm baseform = (BaseForm)dispCont.disp;
					baseform.create();
				}
				
				//when native mode is used we need to check if we don't discard TextBox
				//#if using.native.textfield:defined
				if(!(this.context.getCurrent() instanceof de.enough.polish.midp.ui.TextBox))
				//#endif
				setCurrentAndRepaint(dispCont.disp);
				return true;
			}
		}
		return false;
	}

	/************************************************************************************************************************
	 * Display the previous screen and pop from stack. Will show the last displayable
	 */
	public void back()
	{
		this.back(false, false);
	}

	public void back(final boolean checkpoint)
	{
		this.back(checkpoint,false);
	}

	/************************************************************************************************************************
	 * Display the previous screen and pop from stack
	 * @param checkpoint if we need to rollback to latest checkpoint, and not just last diaplayable
	 */
	public void back(final boolean checkpoint, final boolean silent)
	{
		boolean displayableFound = false;
		
		final ArrayList skipped = new ArrayList();

		while(!displayableFound)
		{
			if(navigationDisplayHistory.size()>1)
			{
				Object obj = navigationDisplayHistory.pop();
				
				if(((DisplayableContainer)obj).disp instanceof Screen) {
					Screen screen =((Screen)((DisplayableContainer)obj).disp);
					skipped.add(screen);
				}
				
				//Peek at the next to see if it is a checkpoint
				obj = navigationDisplayHistory.peek();
				
				if(((DisplayableContainer)obj).checkpoint || !checkpoint)//If we are looking for a checkpoint, or not interested at all in a checkpoint, we reached the previous displayable to show
					displayableFound = true;
			}
			else
				displayableFound = true;
		}
		
		//#if polish.blackberry
			// //#= synchronized(UiApplication.getEventLock())
		UiApplication.getUiApplication().invokeLater(new Runnable()
		{
			public void run()
		//#endif
			{
				if(!silent)
					displayCurrent(true);
				
				for (int i = 0, size = skipped.size(); i < size ; i++) {
					Screen screen = (Screen)skipped.get(i);
					screen.destroy();
				}
			}
		//#if polish.blackberry
		});
		//#endif
	}

	/************************************************************************************************************************
	 * Display the next screen and push to stack
	 * @param dis is the next displayable to be shown
	 * @param obj is the state object of teh displayable
	 */
	public void next(final Displayable dis, final Object obj)
	{
			//#debug debug
			System.out.println("Adding:"+dis);

			navigationDisplayHistory.push(new DisplayableContainer(dis,obj,true));
			displayCurrent(false);
	}

	/************************************************************************************************************************
	 * Display the next screen and push to stack
	 * @param dis is the next displayable to be shown
	 * @param obj is the state object of teh displayable
	 * @param checkpoint is the checkpoint we wish to roll back to in case needed
	 */
	public void next(final Displayable dis, final Object obj, final boolean checkpoint)
	{
			//#debug debug
			System.out.println("Adding:"+dis);

			navigationDisplayHistory.push(new DisplayableContainer(dis,obj,checkpoint));
			displayCurrent(false);
	}

	
	private void setCurrentAndRepaint(Displayable dis)
	{
		//#debug debug
		System.out.println("setCurrentAndRepaint()");
		
		//switch to display
		context.setCurrent( dis );
	}
	
	
	
	/**
	 * Container for holding both displayable and state data in same stack
	 */
	public class DisplayableContainer
	{
		public Displayable disp;//The actual display
		public Object stateData;//The data we use for recreating a forms state
		public boolean checkpoint;//Flag to indicate if this is a checkpoint
		
		/**
		 * @param disp the displayable we want to show
		 * @param stateData the state data associated with the disp  
		 */
		protected DisplayableContainer(final Displayable disp, final Object stateData, final boolean checkpoint) {
			super();
			this.stateData = stateData;
			this.disp = disp;
			this.checkpoint = checkpoint;
		}
	}
	
	/**
	 * Will hide the application. When reinvoked it will start where it left off.
	 */
	public void hide()
	{
		context.setCurrent( null );
	}

	/**
	 * @param currentLocalDisplayableContainer
	 */
	public void backSilent() {
		this.back(false, true);
	}

	/************************************************************************************************************************
	 * Checks if a given Displayable type is in stack
	 * 
	 * @param disp displayable to look for
	 * @return true if found false otherwise
	 */
	public boolean isInStack(final Class c)
	{
		if(null != c)
		{
			Enumeration e = navigationDisplayHistory.elements();
			
			for(;e.hasMoreElements();)
				if( c.isInstance( ((DisplayableContainer)e.nextElement()).disp ) )
					return true;
		}
		
		return false;
	}
	
	//#mdebug screenstacktrace
	
	public void printDisplayHistory()
	{
		Displayable dis;
		for(int i = navigationDisplayHistory.size(); --i>=0; )
		{
			dis = (Displayable)((DisplayableContainer)navigationDisplayHistory.elementAt(i)).disp;
			System.out.println("Stackindex: "+i+", DisplayableType: "+dis.getClass().toString());
		}
	}
	
	public void printAlertHistory()
	{
		Displayable dis;
		for(int i = navigationAlertHistory.size(); --i>=0; )
		{
			dis = (Displayable)((DisplayableContainer)navigationAlertHistory.elementAt(i)).disp;
			System.out.println("Stackindex: "+i+", AlertType: "+dis.getClass().toString());
		}
	}

	//#enddebug
}
