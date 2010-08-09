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

package com.zyb.nowplus.presentation.test;

import javax.microedition.io.ConnectionNotFoundException;

import com.zyb.nowplus.presentation.ExtendedScreenHistory;
import com.zyb.nowplus.presentation.FormStateResetter;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;

import de.enough.polish.midp.ui.Form;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Displayable;
import com.zyb.nowplus.MIDletContext;

public class ExtendedScreenHistoryTest extends TestCase implements FormStateResetter, MIDletContext, CommandListener
{
	private ExtendedScreenHistory history;
//	private Command cmdDismiss = new Command("Dismiss","Dismiss",Command.OK,0);
	
	
	public void setUp()
	{
		history = new ExtendedScreenHistory(this, this);
		history.clearHistory();
	}
	
	/*
	 * 
	 * */
	public void testNextBackWorking() throws Exception
	{
		Form form1 = new Form("Form 1");
		history.next(form1, null);
		Assert.assertEquals(form1, history.currentGlobalAndLocalDisplayableContainer().disp);

		Form form2 = new Form("Form 2");
		history.next(form2, null);
		Assert.assertEquals(form2, history.currentGlobalAndLocalDisplayableContainer().disp);

		Form form3 = new Form("Form 3");
		history.replaceCurrentDisplayable(form3);
		Assert.assertEquals(form3, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.back();
		Assert.assertEquals(form1, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.back();
		Assert.assertEquals(form1, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.back();
		Assert.assertEquals(form1, history.currentGlobalAndLocalDisplayableContainer().disp);
	}

	/*
	 * 
	 * */
	public void testClearHistory() throws Exception
	{
		Form form1 = new Form("Form 1");
		history.next(form1, null);
		history.clearHistory();
		Assert.assertEquals(history.currentGlobalAndLocalDisplayableContainer(), null);
	}

	public void testScreenGlobalStackWorking() throws Exception
	{
		/*
		Form statescreen = new Form("screen");
		
		Form global1 = new Form("Global 1");
		history.displayGlobal(global1, null);

		statescreen.setTitle("screen 1");
		history.next(statescreen, "screen 1");

		statescreen.setTitle("screen 2");
		history.next(statescreen, "screen 2");

		Form global2 = new Form("Global 2");
		history.displayGlobal(global2, null);

		statescreen.setTitle("screen 3");
		history.next(statescreen, "screen 3");

		statescreen.setTitle("screen 4");
		history.next(statescreen, "screen 4");

		Alert error1 = history.displayGlobalAlert("headline", "error1", AlertType.WARNING, Alert.FOREVER, this, new Command[]{cmdDismiss});
		history.displayGlobalAlert("headline", "error1", AlertType.WARNING, Alert.FOREVER, this, new Command[]{cmdDismiss});

		statescreen.setTitle("screen 5");
		history.next(statescreen, "screen 5");

		statescreen.setTitle("screen 6");
		history.next(statescreen, "screen 6");

		Alert error2 = history.displayGlobalAlert("headline", "error2", AlertType.ERROR, Alert.FOREVER, this, new Command[]{cmdDismiss});
		history.displayGlobalAlert("headline", "error2", AlertType.ERROR, Alert.FOREVER, this, new Command[]{cmdDismiss});
		Alert error3 = history.displayGlobalAlert("headline", "error3", AlertType.ERROR, Alert.FOREVER, this, new Command[]{cmdDismiss});
		history.displayGlobalAlert("headline", "error3", AlertType.ERROR, Alert.FOREVER, this, new Command[]{cmdDismiss});

		history.displayGlobalAlert("headline", "warning1", AlertType.WARNING, Alert.FOREVER, this, new Command[]{cmdDismiss});
		Alert warning1 = history.displayGlobalAlert("headline", "warning1", AlertType.WARNING, Alert.FOREVER, this, new Command[]{cmdDismiss});

		Alert info1 = history.displayGlobalAlert("headline", "info1", AlertType.INFO, Alert.FOREVER, this, null);
		history.displayGlobalAlert("headline", "info1", AlertType.INFO, Alert.FOREVER, this, new Command[]{cmdDismiss});

		Assert.assertEquals(info1, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(info1);
		Assert.assertEquals(warning1, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(warning1);
		Assert.assertEquals(error3, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(error3);
		Assert.assertEquals(error2, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(error2);
		Assert.assertEquals(error1, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(error1);
		Assert.assertEquals(global2, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(global2);
		Assert.assertEquals(global1, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.dismissGlobal(global1);

		Assert.assertEquals("screen 6", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 5", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 4", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 3", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 2", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 1", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 1", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 1", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 1", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
		Assert.assertEquals("screen 1", ((Form)history.currentGlobalAndLocalDisplayableContainer().disp).getTitle());
		history.back();
	*/
	}

	/*
	 * 
	 * */
	public void testCheckpointingWorking() throws Exception
	{
		Form form1 = new Form("Form 1");
		history.next(form1, null);
		Assert.assertEquals(form1, history.currentGlobalAndLocalDisplayableContainer().disp);

		Form form2 = new Form("Form 2");
		history.next(form2, null,false);
		Assert.assertEquals(form2, history.currentGlobalAndLocalDisplayableContainer().disp);

		Form form3 = new Form("Form 3");
		history.next(form3, null,false);
		Assert.assertEquals(form3, history.currentGlobalAndLocalDisplayableContainer().disp);

		Form form4 = new Form("Form 4");
		history.next(form4, null,false);
		Assert.assertEquals(form4, history.currentGlobalAndLocalDisplayableContainer().disp);

		history.back();
		Assert.assertEquals(form3, history.currentGlobalAndLocalDisplayableContainer().disp);
		history.back(true);
		Assert.assertEquals(form1, history.currentGlobalAndLocalDisplayableContainer().disp);
	}

	
	public void resetStateWith(Displayable disp, Object obj) 
	{
		if(disp instanceof Form)
			((Form)disp).setTitle((String)obj);
	}

	public boolean platformRequest(String url) throws ConnectionNotFoundException
	{
		return true;
	}
	public void exit(final boolean deleteStorageBefore)
	{}
	public void setCurrent(final Displayable disp)
	{}
	public String getImsi() 
	{
		return "mysim";
	}
	public String getMsisdn()
	{
		return "mynumber";
	}
	public void commandAction(Command c, Displayable d) 
	{}
	
	public boolean checkFiles(String[] files)
	{
		return true;
	}
	public void deleteRecordStores(String[] selection)
	{}

	public void resetStateWith(javax.microedition.lcdui.Displayable disp,
			Object obj) {
		// TODO Auto-generated method stub
		
	}

	public void setCurrent(javax.microedition.lcdui.Displayable disp) {
		// TODO Auto-generated method stub
		
	}

	public String getCurrentLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	public Displayable getCurrent()
	{
		return null;
	}

	public boolean supportsTcp() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getDeviceId() {
		// TODO Auto-generated method stub
		return null;
	}
}
