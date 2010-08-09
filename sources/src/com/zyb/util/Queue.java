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
 * A thread-safe fi-fo collection. This queue
 * uses a wait/notify mechanism in the pop.
 */
public class Queue 
{
	private QueueElement root;
	
	/**
	 * Constructs an empty queue.
	 */
	public Queue()
	{
		root = new QueueElement();
		root.content = null;
		root.prev = root;
		root.next = root;		
	}
	
	/**
	 * Checks if the queue is empty.
	 */
	public synchronized boolean isEmpty()
	{
		return (root.content == null);
	}
	
	/**
	 * Adds an object to the queue.
	 */
	public synchronized void push(Object o)
	{
		if (root.content == null)
		{
			root.content = o;
		}
		else
		{
			QueueElement e = new QueueElement();
			e.content = o;
			root.prev.next = e;
			e.next = root;
			e.prev = root.prev;
			root.prev = e;
		}
		notify();
	}
	
	/**
	 * Adds an object to the queue.
	 */
	public synchronized void pushPriority(Object o) {
		if (root.content == null)
		{
			root.content = o;
		}
		else
		{
			QueueElement e = new QueueElement();
			e.content = o;
			root.prev.next = e;
			e.next = root;
			e.prev = root.prev;
			root.prev = e;
			root = e;
		}
		notify();
	}
	
	/**
	 * Takes the next object from the queue. Waits if the queue is empty.
	 */
	public synchronized Object pop() throws InterruptedException
	{
		if (root.content == null)
		{
			wait();
		}
		Object o = root.content;
		remove(root);
		root = root.next;
		return o;
	}
	
	/**
	 * Returns true if the specified object is in the queue, otherwise false
	 * @param o the object
	 * @return true if the specified object is in the queue, otherwise false
	 */
	public synchronized boolean contains(Object o)
	{
		for (QueueElement e = root.next; (e != root); e = e.next)
		{
			if (e.content.equals(o))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Removes a given object from the queue.
	 */
	public synchronized void cancel(Object o) throws InterruptedException
	{
		if (root.content != null)
		{
			if (root.content.equals(o))
			{
				pop();
			}
			else
			{
				for (QueueElement e = root.next; (e != root); e = e.next)
				{
					if (e.content.equals(o))
					{
						remove(e);
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns the object that was added most recently. Doesn't wait if the queue is empty.
	 */
	protected Object latest() 
	{
		return (root.content == null) ? null : root.prev.content;
	}
		
	private void remove(QueueElement e)
	{
		e.content = null;
		e.prev.next = e.next;
		e.next.prev = e.prev;
	}
	
	private static class QueueElement
	{
		Object content;
		QueueElement prev;
		QueueElement next;
	}
	
	//#mdebug error
	public String toString()
	{
		return "Queue["
			+ "]";
	}
	//#enddebug



}
