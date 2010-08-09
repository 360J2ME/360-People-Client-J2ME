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
import com.zyb.util.ArrayUtils;
import com.zyb.util.HashUtil;

/**
 * Created by IntelliJ IDEA.
 * User: florin
 * Date: 2009-02-20
 * Time: 12:48:28
 */
public class HashUtilTest extends TestCase {

    private long[] longArray = new long[]{777, 888, 999};


    public void setUp()
    {
    }

    /**
     * Positive test equals object 
     */
    public void test_equal() {
        Integer obj1 = new Integer(1);
        assertTrue(HashUtil.equals(obj1, obj1));
    }

    /**
     * Negative test - objects no equal
     */
    public void test_not_equal(){
        Integer obj1 = new Integer(1);
        Integer obj2 = new Integer(2);
        assertFalse(HashUtil.equals(obj1, obj2));
    }

    /**
     * Test trim "long" array
     */
    public void test_trim_long_array() {
        long[] res = ArrayUtils.trimArray(longArray, longArray.length);
        assertEquals(longArray.length, res.length);
        assertEquals(res[0], longArray[0]);
        assertEquals(res[1], longArray[1]);
        assertEquals(res[2], longArray[2]);
    }

    public void tearDown()
	{
	}
}
