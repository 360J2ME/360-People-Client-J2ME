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

import com.zyb.nowplus.test.TestCase;
import com.zyb.util.PushLogHandler;
import de.enough.polish.log.LogEntry;
import de.enough.polish.util.ArrayList;

public class PushLogHandlerTest extends TestCase {

    LogEntry logEntry1 = new LogEntry("test1", 1, 2, "test1", "test1", "test1");
    LogEntry logEntry2 = new LogEntry("test2", 1, 2, "test2", "test2", "test2");
    PushLogHandler pushLogHandler;


    public void setUp()
    {
       if (pushLogHandler == null)
           pushLogHandler = new PushLogHandler();
    }

    /**
     * Test log entry handle
     */
    public void test_handleLogEntry() {
        try {
            pushLogHandler.handleLogEntry(logEntry1);
            pushLogHandler.handleLogEntry(logEntry2);
            assertNotNull(PushLogHandler.logEntries);
            assertTrue(PushLogHandler.logEntries.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test log entry detach, logEntries should not contain elem 
     */
    public void test_detachLogEntries() {
        try {
            pushLogHandler.handleLogEntry(logEntry1);
            pushLogHandler.handleLogEntry(logEntry2);
            ArrayList log = PushLogHandler.detachLogEntries();

            assertNotNull(log);
            assertTrue(log.size() > 0);
            assertFalse(PushLogHandler.logEntries.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test get two log entry
     */
    public void test_getLogEntries() {
        try {
            pushLogHandler.handleLogEntry(logEntry1);
            pushLogHandler.handleLogEntry(logEntry2);
            ArrayList log = PushLogHandler.getLogEntries();

            assertNotNull(log);
            assertEquals(PushLogHandler.logEntries.size(), log.size());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void tearDown()
	{
        pushLogHandler = null;
    }
    
}
