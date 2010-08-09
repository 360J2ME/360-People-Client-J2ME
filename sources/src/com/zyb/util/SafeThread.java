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
package com.zyb.util;

/**
 * This is an alternative to Thread with improved exception handling.
 */
public class SafeThread implements Runnable
{
	private Thread thread;
	private final SafeRunnable runnable;
	private boolean running;
	
	/**
	 * Creates a thread for the given runnable.
	 * @param runnable
	 */
	public SafeThread(SafeRunnable runnable)
	{
		this.runnable = runnable;
	}
	
	/**
	 * Starts the thread on which the runnable runs.
	 */
	public synchronized void start(String name)
	{
		thread = new Thread(this, name);
		thread.start();
	}
	
	/**
	 * Waits for the thread to die.
	 */
	public void join() throws InterruptedException
	{
		if(thread != null)
			thread.join();
	}
	
	/**
	 * Stops the thread after the current piece of work is finished. 
	 */
	public void stop()
	{
		thread = null;
		running = false;
	}
	
	public void run()
	{
		//#debug info
		System.out.println(runnable + " started on " + Thread.currentThread().getName());
		
		running = true;
		
		try
		{
			runnable.init();
		}
		catch (OutOfMemoryError e)
		{
			tryRecover();
		}
		catch (Exception e)
		{
			running = false;
			
			//#debug error
			System.out.println("Exception in init() of " + runnable + ":" + e);			
		}
		
		while (running)
		{
			try
			{
				runnable.work();
			}
			catch (OutOfMemoryError e)
			{
				tryRecover();
			}
			catch (Exception e)
			{
				//#debug error
				System.out.println("Ignored exception in work() of " + runnable + ":" + e);
			}
		}
		
		try
		{
			runnable.cleanUp();
		}
		catch (OutOfMemoryError e)
		{
			tryRecover();
		}
		catch (Exception e)
		{	
			//#debug error
			System.out.println("Exception in cleanUp() of " + runnable + ":" + e);				
		}
		
		//#debug closedown
		System.out.println(runnable + " stopped on " + Thread.currentThread().getName());
	}
	
	private void tryRecover()
	{
		try
		{
			runnable.releaseMemory();
			
			//#debug error
			System.out.println("Recovered from out of memory error in " + runnable);
		}
		catch (OutOfMemoryError e)
		{
			running = false;
		}
	}
}
