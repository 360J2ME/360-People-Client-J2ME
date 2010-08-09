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
package com.zyb.util.test;

import com.zyb.util.Queue;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;

public class QueueTest extends TestCase
{
	private Queue queue;
	
	public void setUp()
	{
		queue = new Queue();
	}
	
	public void testPushAndPop() throws Exception
	{
		Assert.assertEquals(true, queue.isEmpty());
		
		queue.push("first");
		
		Assert.assertEquals(false, queue.isEmpty());
		Assert.assertEquals("first", queue.pop());
	}
	
	public void testPushesAndPops() throws Exception
	{
		queue.push("first");
		queue.push("second");
		queue.pop();
		queue.push("third");
		
		Assert.assertEquals("second", queue.pop());
		Assert.assertEquals("third", queue.pop());
		Assert.assertEquals(true, queue.isEmpty());
	}
	
	public void testPushesAndCancels() throws Exception
	{
		queue.push("first");
		queue.cancel("first");
		
		Assert.assertEquals(true, queue.isEmpty());
		
		queue.push("first");
		queue.push("second");
		queue.cancel("second");
		
		Assert.assertEquals("first", queue.pop());
		Assert.assertEquals(true, queue.isEmpty());
		
		queue.push("first");
		queue.push("second");
		queue.push("third");
		queue.cancel("second");
		
		Assert.assertEquals("first", queue.pop());
		Assert.assertEquals("third", queue.pop());
		Assert.assertEquals(true, queue.isEmpty());
	}
	
	public void testBlocking() throws Exception
	{
		new Thread(new Runnable() {
			public void run()
			{
				try 
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
				}
				queue.push("first");
			}
		}).start();
		
		Assert.assertEquals(true, queue.isEmpty());
		Assert.assertEquals("first", queue.pop());
	}
	
	public void tearDown()
	{
		queue = null;
	}
}
