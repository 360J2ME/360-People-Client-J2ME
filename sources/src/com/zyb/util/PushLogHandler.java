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

import de.enough.polish.log.LogEntry;
import de.enough.polish.log.LogHandler;
import de.enough.polish.util.ArrayList;

public class PushLogHandler extends LogHandler {
	public static final int MAX_LOG_ENTRIES = 2000 ;
	
	public static final Object logLock = new Object();
	
	public static ArrayList logEntries = new ArrayList() ;
	
	private int logTrimCount = 0;
	
	public void handleLogEntry(final LogEntry log) throws Exception {
		synchronized(logLock) {
			// Check the log is not too large
			if(logEntries.size() > MAX_LOG_ENTRIES) {
				logEntries.remove(0);
			}
			
			logEntries.add(log) ;
			logTrimCount++ ;
			
			if((logTrimCount > (MAX_LOG_ENTRIES * 2)) ) {
				logEntries.trimToSize() ;
				logTrimCount = 0 ;
			}
		}
	}
	
	/**
	 * @return An ArrayList containing all of the LogEntry objects logged since 
	 * the last call to this method.
	 */
	public static ArrayList detachLogEntries() {
		final ArrayList detached ; 
		synchronized(logLock) {
			detached = logEntries ;
			logEntries = new ArrayList() ;
		}
		return detached ;
	}
	
	/**
	 * @return An ArrayList containing all of the LogEntry objects currently logged.
	 */
	public static ArrayList getLogEntries() {
		final ArrayList log ; 
		synchronized(logLock) {
			log = new ArrayList();
			for(int i = 0; i < logEntries.size(); ++i )
				log.add(logEntries.get(i));
		}
		return log ;
	}
}
