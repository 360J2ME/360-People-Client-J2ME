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
package com.zyb.nowplus;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;

//#if polish.blackberry
//# import de.enough.polish.blackberry.midlet.MIDlet;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.DeviceInfo;
//#else	
import javax.microedition.midlet.MIDlet;
//#endif	

//#if polish.blackberry && enable.flurry==true
import com.flurry.blackberry.FlurryAgent;
import com.zyb.util.BlackBerryConnectionSuffix;
//#endif

import com.zyb.nowplus.business.ServiceBroker;
import com.zyb.nowplus.data.protocol.transport.http.HttpRPGConnection;
import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.util.Collator;
import com.zyb.util.TextUtilities;

import de.enough.polish.ui.Canvas;

//#if use.servers.list.choices
import com.zyb.nowplus.presentation.view.forms.ServerListForm;
//#endif

/**
 * @author Jens Vesti
 * This is the MIDlet class which initialises the MVC classes and takes input from jad + AMS  
 */
public class NowPlus extends MIDlet implements MIDletContext
{

	//#ifdef polish.device.requires.loadLanguageTextKeysEarly
	static {
		//loading language files from 'solid archive'
		TextUtilities.loadLanguageTextKeys( TextUtilities.getCurrentLanguageOnly() );

	}
	//#endif

	private boolean firstStartup = true;
	
	private ServiceBroker model;
	
	/**
	 * Creates a new MIDlet.
	 */
	public NowPlus() 
	{
		//#debug start
		System.out.println("Constructing MIDlet");
		
		//#ifdef polish.device.requires.preloadscreen
		/*
		 * On some nokia's there is an empty screen with text: application is running nothing to display
		 * The workaround is to set a canvas on a display which is done here 
		 */
	
		Display mDisplay = Display.getDisplay(this);
		
		mDisplay.setCurrent(new Canvas() {
			private final int bgColor =
				//#if polish.device.preloadscreen.bgcolor:defined
				//#= ${polish.device.preloadscreen.bgcolor};
				//#else
				0x00ffffff;
				//#endif
			
			//#if polish.device.preloadscreen.gradientcolor:defined
			//#= int startcolor = ${polish.device.preloadscreen.gradientcolor};
			//# int nolines = 23;
			
			//# int rpart = (startcolor & 0x00ff0000 ) >> 16;
			//# int gpart = (startcolor & 0x0000ff00 ) >> 8;
			//# int bpart = (startcolor & 0x000000ff );

			//# float rinc = (((bgColor & 0x00ff0000 ) - (startcolor & 0x00ff0000 ) ) >> 16 ) / (float)nolines;
			//# float ginc = (((bgColor & 0x0000ff00 ) - (startcolor & 0x0000ff00 ) ) >> 8) / (float)nolines;
			//# float binc = (((bgColor & 0x000000ff ) - (startcolor & 0x000000ff ) )) / (float)nolines;
			//#endif
			
			protected void paint(Graphics g) {
				g.setColor(bgColor);
				int width = getWidth();
				int height = getHeight();
				g.fillRect(0, 0, width, height);
			
				//#if polish.device.preloadscreen.gradientcolor:defined
				//# for(int i = 0; i < nolines + 1; ++i ) {
				//# 	g.setColor((int)(rpart + rinc * i), (int)(gpart + ginc * i), (int)(bpart + binc * i));
				//# 	g.drawLine(0, height - i - 1, width, height - i - 1);
				//# }
				//#endif
			}
		}
			);
		//#endif
		
		allocateHeap();
		
		//#ifndef polish.device.requires.loadLanguageTextKeysEarly
		//loading language files from 'solid archive'
		TextUtilities.loadLanguageTextKeys(TextUtilities.getCurrentLanguageOnly());
		//#endif
			
		model = new ServiceBroker(this);
			
		Controller controller = new Controller(this, model);
		
		model.attach(controller);	
		
		//#ifdef Server_Short_Name:defined
		//#= System.out.println("Server:<<<<<<<<<<<<"+"${Server_Short_Name}"+">>>>>>>>>>>>"+System.currentTimeMillis());
		//#endif
	
		//#debug start
		System.out.println("done");
		
		//#if polish.blackberry
		//#= addGlobalEventListener(model);
		//#endif
	}

	private void allocateHeap()
	{
		//#debug debug
		System.out.println("Allocating heap, initial size = " + Runtime.getRuntime().totalMemory());
		
		String minimumHeap = null;
		try 
		{
			minimumHeap = getAppProperty("MIDlet-Heap-Size");
		} 
		catch (Exception e) 
		{
			//#debug debug
			System.out.println("Could not read JAD-Property MIDlet-Heap-Size");
		}		

		//Allocating to get the minimum required heap
		if(minimumHeap != null)
		{
			Vector vec = new Vector();
			try
			{
				int minHeap = Integer.valueOf(minimumHeap).intValue();
				
				while(minHeap > Runtime.getRuntime().totalMemory())
				{
					byte[] tmp = new byte[4096];
					vec.addElement(tmp);
				}
			}
			catch(Throwable e)
			{
				//#debug debug
				System.out.println("Allocating heap failed" + e);
			}
			vec = null;
		
			//#debug debug
			System.out.println("Tried to allocate " + minimumHeap + ", allocated " + Runtime.getRuntime().totalMemory());
		}
	}

	public void startApp() throws MIDletStateChangeException 
	{
		//#if polish.blackberry && enable.flurry==true
			//#message Including Flurry using account with key ${flurry.account}
			BlackBerryConnectionSuffix.checkConnectionSuffixStr();
	
			if(BlackBerryConnectionSuffix.connSuffixStr != null)
				FlurryAgent.appendToReportUrl(BlackBerryConnectionSuffix.connSuffixStr);
	
			//#ifdef flurry.account
				//#=FlurryAgent.onStartApp("${flurry.account}");
			//#else
				FlurryAgent.onStartApp("N1NRV5UDRSZYF9QSDI4L");
			//#endif
			
			FlurryAgent.setVersionName(model.getAppVersion());
		//#endif
		
		//#debug debug
		System.out.println("Starting MIDlet");
				
		Collator.getInstance().loadAlphabet(getCurrentLanguage());

		//#if use.servers.list.choices
			if(firstStartup) {
				//#style servers_list_screen
				ServerListForm menuScreen = new ServerListForm(model,this);
				setCurrent(menuScreen);
				
				firstStartup = false;
			}
			else
			{
				model.applicationStarted();
			}
		//#else
			model.applicationStarted();
		//#endif
	}

	protected void pauseApp() 
	{
		//#debug debug
		System.out.println("pauseApp()");

		//#if polish.blackberry && enable.flurry==true
			FlurryAgent.onPauseApp();
		//#endif
		
		model.applicationPaused();
	}

	protected void destroyApp(boolean unconditional) throws MIDletStateChangeException 
	{
		//#debug closedown
		System.out.println("System requested shutdown");

		model.applicationStopped(false);
		
		//#if polish.blackberry && enable.flurry==true
			FlurryAgent.onDestroyApp();
		//#endif
		
		//#if polish.blackberry
			//#= removeGlobalEventListener(model);
		//#endif

		//#if polish.blackberry
			notifyDestroyed();
			System.exit(0);
		//#endif
	}

	public void setCurrent(Displayable disp)
	{
		//#debug debug
		System.out.println("setCurrent("+disp+")");

		Display.getDisplay(this).setCurrent(disp);
		
		if (disp instanceof Canvas) {
			Canvas canvas = (Canvas) disp;
			canvas.repaint();
			canvas.serviceRepaints();
		}
	}
	
	public String getMsisdn()
	{
		return null; // TODO
	}
	
	public void exit(final boolean deleteStorageBefore)
	{
		//#debug closedown
		System.out.println("User requested shutdown");
		
		model.applicationStopped(deleteStorageBefore);

		//#if polish.blackberry && enable.flurry==true
		FlurryAgent.onDestroyApp();
		//#endif
	
		notifyDestroyed();
	}

	public boolean checkFiles(String[] requiredFiles)
	{
		//#debug debug
		System.out.println("Checking all files are still there");
		
		String[] presentFiles = RecordStore.listRecordStores();

		if (presentFiles != null) {
			for (int i = 0; i < presentFiles.length; i++) {
				for (int j = 0; j < requiredFiles.length; j++) {
					if ((requiredFiles[j] != null) && requiredFiles[j].equals(presentFiles[i])) {
						requiredFiles[j] = null;
						break;
					}
				}
			}
		}
		
		for (int j = 0; j < requiredFiles.length; j++) {
			if (requiredFiles[j] != null) {
				//#debug error
				System.out.println("File " + requiredFiles[j] + " missing");
				
				return false;
			}
		}
	
		return true;
	}
	
	public void deleteRecordStores(String[] selection)
	{
		//#debug info
		System.out.println("Deleting record stores");
		
	    String[] list = RecordStore.listRecordStores();

	    if (list != null) {
		    for (int i = 0; i < list.length; i++) {
		    	boolean delete = false;

		    	if (selection == null) {
		    		delete = true;
		    	}
		    	else {
		    		for (int j = 0; j < selection.length; j++) {
		    			if (list[i].startsWith(selection[j])) {
		    				delete = true;
		    				break;
		    			}
		    		}
		    	}

		    	if (delete) {
		    		//#debug debug
		    		System.out.println("Delete recordstore " + list[i]);

		    		try {
			    		RecordStore.deleteRecordStore(list[i]);
			    	}
			    	catch(Exception e) {
			    		//#debug error
			    		System.out.println("Failed deleting recordstore " + list[i] + ":" + e);
			    	}
		    	}
		    }
	    }
	    
		//#debug info
		System.out.println("Done deleting record stores");
	}
	
	public String getCurrentLanguage()
	{
		return System.getProperty("microedition.locale");
	}

	public Displayable getCurrent()
	{
		return Display.getDisplay(this).getCurrent();
	}

	public boolean supportsTcp()
	{
		try {
			String tcpSupport = getAppProperty("X-VF-TCPSupport");			

			if ("true".equals(tcpSupport)) {
				return true;
			}
		}
		catch (Exception e) {
			//#debug error
			System.out.println("Could not read JAD-Property");
		}
		
		return false;
	}
	
	public String getDeviceId()
	{
		//#if enable.blackberry.push
			return Integer.toHexString(DeviceInfo.getDeviceId());
		//#else
			//#= return null;
		//#endif		
	}
	
	//#if polish.blackberry && enable.blackberry.push == true
	public static void main(String[] args)
	{ 
		if (args != null && args.length > 0 && "test".equals(args[0])) {
			ApplicationManager myApp = ApplicationManager.getApplicationManager(); 
	
			while(myApp.inStartup()) {
				try {
					Thread.sleep(2000);
				}
				catch(Exception e) {
					// Catch Exception        
				}
			} 
	
			BackgroundApp._getInstance().registerForService();
	    }
		else {
			try {
				//#= MIDlet midlet = (MIDlet) Class.forName("${polish.classes.midlet-1}").newInstance();
				//# midlet.startApp();	
				//# midlet.enterEventDispatcher();
			}
			catch (Exception e) {
				e.printStackTrace();
			} 
	    }
	}
	
	//#endif
	
	public static void notifyUserInteraction()
	{
		//#if polish.blackberry
			HttpRPGConnection.notifyUserInteraction();
		//#endif
	}
}
