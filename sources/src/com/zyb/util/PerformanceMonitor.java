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
//#mdebug performancemonitor

package com.zyb.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Jens Vesti
 *
 */
public class PerformanceMonitor {
	
	public static final int HTTP_IN = 0;
	public static final int HTTP_OUT = 1;
	public static final int RMSREAD = 2;
	public static final int RMSWRITE = 3;
	public static final int PIM = 4;
	public static final int RMSOPEN = 5;
	public static final int RMSCLOSE = 6;

	public static final int NUMBER_OF_MEASUREMENTPOINTS = 7;
	
	private boolean[] on = new boolean[NUMBER_OF_MEASUREMENTPOINTS];
	private boolean[] off = new boolean[NUMBER_OF_MEASUREMENTPOINTS];
	
	private volatile boolean running = true;
	private Vector measurements = new Vector();
	private StringBuffer currentTypes = new StringBuffer();

	private static PerformanceMonitor instance;
	
	
	public static PerformanceMonitor getInstance()
	{
		if(instance == null)
			instance = new PerformanceMonitor();
		
		return instance;
	}
	
	private PerformanceMonitor()
	{
		new Thread()
		{
			public void run()
			{
				System.out.println("Running");
				Runtime runtime = Runtime.getRuntime();
				long lasttime = System.currentTimeMillis();
				long lastmemuse = runtime.totalMemory()-runtime.freeMemory();
				
				while(running)
				{
					long memused = runtime.totalMemory()-runtime.freeMemory();
					long memavailable = runtime.totalMemory();
					long time = System.currentTimeMillis();
					boolean gc = false;
					if(memused<lastmemuse)
						gc = true;

					for(int i=0; i<10; i++)
					{
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
						}
					}
					
					if(time-lasttime>150)
						measurements.addElement(new Measurement(memused, memavailable, time-lasttime, time, gc, currentUsedTypesString()));
					
					if(measurements.size()>1)
						flush();
					
					lastmemuse = memused;
					lasttime = time;
				}
			}
		}.start();
	}

	/**
	 * @return
	 */
	protected String currentUsedTypesString() {
		currentTypes.delete(0, currentTypes.length());
		for(int i=0; i<NUMBER_OF_MEASUREMENTPOINTS; i++)
		{
			if(on[i])
			{
				switch(i)
				{
					case HTTP_IN: currentTypes.append("HTTPIN"); break;
					case HTTP_OUT: if(currentTypes.length()>0) currentTypes.append(","); currentTypes.append("HTTPOUT"); break;
					case RMSREAD: if(currentTypes.length()>0) currentTypes.append(","); currentTypes.append("RMSREAD"); break;
					case RMSWRITE: if(currentTypes.length()>0) currentTypes.append(","); currentTypes.append("RMSWRITE"); break;
					case PIM: if(currentTypes.length()>0) currentTypes.append(","); currentTypes.append("PIM"); break;
					case RMSOPEN: if(currentTypes.length()>0) currentTypes.append(","); currentTypes.append("RMSOPEN"); break;
					case RMSCLOSE: if(currentTypes.length()>0) currentTypes.append(","); currentTypes.append("RMSCLOSE"); break;
				}
			}
			if(off[i])
				on[i]=false;
		}
		return currentTypes.toString();
	}

	public void registerThread(int type)
	{
		on[type] = true;
		off[type] = false;
		//try {Thread.sleep(100);} catch (InterruptedException e) {}
	}

	public void unregisterThread(int type)
	{
		off[type] = true;
	}

	
	public void stop()
	{
		this.running = false;

		flush();
	}
	
	private void flush() {
		Enumeration e = measurements.elements();
		while(e.hasMoreElements())
		{
			Measurement m = (Measurement)e.nextElement();
			System.out.println("\t"+m.memoryused+"\t"+m.memoryavailable+"\t"+m.timespent+"\t"+m.time+"\t"+m.gc+"\t"+m.threadsrunning);
		}
		measurements.removeAllElements();
	}


	public class Measurement
	{
		public long memoryused;
		public long memoryavailable;
		public long timespent;
		public long time;
		public boolean gc;
		public String threadsrunning;
		
		public Measurement(long memoryused, long memoryavailable, long timespent, long time, boolean gc, String threadsrunning)
		{
			this.memoryused = memoryused;
			this.memoryavailable = memoryavailable;
			this.timespent = timespent;
			this.time = time;
			this.gc = gc;
			this.threadsrunning = threadsrunning;
		}
	}
}
//#enddebug
