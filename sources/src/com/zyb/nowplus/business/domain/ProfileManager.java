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
package com.zyb.nowplus.business.domain;

/**
 * Loads ands stores the data in a profile.
 */
public interface ProfileManager
{
	/**
	 * Requests complex data for the given profile.
	 */
	public void request(ManagedProfile profile);
	
	/**
	 * Cancels a request for complex data for the given profile.
	 */
	public void cancelRequest(ManagedProfile profile);
	
	/**
	 * Executes the request.
	 */
	public void executeRequest(ManagedProfile profile);
	
	/**
	 * Stores changes in a profile. 
	 * @param local If true, the change is not send to the server
	 * @param ignoreList If true, the change won't affect the contact list
	 * @param silent If true, the UI is not notified of the change
	 */
	public void commit(ManagedProfile profile, boolean local, boolean ignoreList, boolean silent);

	/**
	 * Reverts changes in a profile.
	 */
	public void revert(ManagedProfile profile);
	
	/**
	 * Notifies the model that the client is finished editing a profile.
	 */
	public void finishedEditing(ManagedProfile profile);

	public Profile getProfileByUserId(long userId);
	
	public ManagedProfile getProfileBySabId(long sabId);
	
	public ManagedProfile getProfileByCabId(long cabId);
	
	public boolean isDeleted(long sabId);
}

