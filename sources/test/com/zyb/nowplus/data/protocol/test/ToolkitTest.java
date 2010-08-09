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
package com.zyb.nowplus.data.protocol.test;

import com.zyb.nowplus.test.TestCase;
import com.zyb.nowplus.data.protocol.apihelpers.Toolkit;

import java.util.Hashtable;

/**
 * User: ftite
 */

public class ToolkitTest extends TestCase {

    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final String TEST_HASH = "098f6bcd4621d373cade4e832627b4f6";


    Hashtable hReq;
    Hashtable hMore;

    public void setUp()
    {
        hReq = new Hashtable();
        hMore = new Hashtable();
    }

    private String getRandomId() {
        long ts = ((long) System.currentTimeMillis() / 1000);

       // create unique ID
        //long l = (new Random()).nextLong();
        return Long.toString(ts);
    }

    /**
     * Test MD5 for string, should be equal to TEST_HASH
     */
    public void  test_MD5_string_param() {
        assertEquals(TEST_HASH, Toolkit.MD5(USERNAME));
    }

    /**
     * test MD5 for byte array, should be equal to TEST_HASH
     */
    public void  test_MD5_byte_array_param() {
        assertEquals(TEST_HASH, Toolkit.MD5(USERNAME.getBytes()));
    }

    /**
     * Negative test - invalid params, should return null   
     */
    public void test_getPasswordHash_invalid_params() {
        assertNull(Toolkit.getPasswordHash(-1, "", "", ""));
    }

    /**
     * Positive test - valid password hash
     */
    public void test_getPasswordHash() {
        assertNotNull(Toolkit.getPasswordHash(Long.parseLong(getRandomId()), USERNAME, PASSWORD, getRandomId()));
    }

    /**
     * Test singned byte to int conversion
     */
    public void test_signedBytesToInt() {
        assertTrue(Toolkit.signedBytesToInt((byte)1,(byte)1,(byte)1, (byte)1) > 0);
    }

    /**
     * Test in to signed byte conversion
     */
    public void test_intToSignedBytes() {
        assertTrue(Toolkit.intToSignedBytes(1).length > 0);
    }

    public void tearDown()
	{
        hReq = null;
        hMore = null;
    }

}
