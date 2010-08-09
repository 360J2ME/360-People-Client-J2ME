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
package com.zyb.nowplus.data.email.response;

/**
 * 
 * @author Jens Vesti
 */
public class EmailFolderResponse extends EmailResponse {

	private String[] folderNames;
	
	public EmailFolderResponse(String response) {
		super(response);
	}
	
	protected void parseResponse() {
		
		//TODO: improve and optimise parsing

	}
	
	public String[] getFolderNames() {
		return folderNames;
	}
	
	//#mdebug error
	public String toString() {
		return "EmailFolderResponse["
		+ "]";
	}
	//#enddebug
	
//	* 3281 EXISTS
//	* 3 RECENT
//	* OK [UNSEEN 1869] mailbox contains unseen messages
//	* OK [UIDVALIDITY 828] UIDs are valid for this mailbox
//	* OK [UIDNEXT 8842] next expected UID is 8842
//	* FLAGS (\Answered \Deleted \Draft \Flagged \Seen $Forwarded $MDNSent Forwarded $Junk $NotJunk Junk JunkRecorded NonJunk NotJunk)
//	* OK [PERMANENTFLAGS (\Answered \Deleted \Draft \Flagged \Seen $Forwarded $MDNSent Forwarded \*)] junk-related flags are not permanent
//	* OK [HIGHESTMODSEQ 106387] modseq tracked on this mailbox
//	a4 OK [READ-WRITE] SELECT completed
}
