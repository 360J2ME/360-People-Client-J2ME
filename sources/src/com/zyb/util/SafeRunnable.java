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
 * This is an alternative to Runnable to be used with 
 * {@link com.zyb.util.SafeThread}.
 */
public interface SafeRunnable 
{
	/**
	 * Initialises the job. Is called once before the first call to work().
	 */
	public void init();
		
	/**
	 * Executes a piece of work. Is called repeatedly.
	 */
	public void work();

	/**
	 * Cleans up after the job. Is called once after the last call to work().
	 */
	public void cleanUp();
		
	/**
	 * Releases as much memory as possible. Is called when an OutOfMemoryError has occured. 
	 */
	public void releaseMemory();
}
