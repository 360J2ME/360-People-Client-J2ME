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
package com.zyb.nowplus.data.protocol;

/**
 * 
 * Tells the model when the network is down or up due
 * to signal out of reach, etc.
 * 
 * @author Rudy Norff (rudy.norff@vodafone.com)
 *
 */
public interface NetworkListener {
	/**
	 * 
	 * Sent when the network signal is there again.
	 * 
	 */
	public void networkUp();
	
	/**
	 * 
	 * Sent when the connectivity went down. The 
	 * network code indicates what went wrong.
	 * 
	 */
	public void networkDown();
	
	
	/**
	 * 
	 * Called when the client switched to roaming
	 * mode.
	 * 
	 */
	public void roamingActive();
	
	
	/**
	 * 
	 * Indicates that the connection-autodetection has 
	 * successfully finished.
	 * 
	 */
	public void autodetectConnectionFinished();
}
