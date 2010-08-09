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

import com.zyb.util.md5.MD5;
import com.zyb.nowplus.test.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: florin
 * Date: 2009-02-20
 * Time: 09:46:10
 */
public class MD5Test extends TestCase {

    private static final byte[] TEST_DIGEST = new byte[]{
            9, -113, 107, -51, 70, 33, -45, 115,
            -54, -34, 78, -125, 38, 39, -76, -10
    };

    private static final String TEST_STRING = "test";
    private static final String TEST_HASH = "098f6bcd4621d373cade4e832627b4f6";
    private static final String TEST_STRING_TO_HEX = "74657374";
    private static final String B64_ENCODED_TEST = "dGVzdA==";

    MD5 md5;

    public void setUp() {
        md5 = new MD5(TEST_STRING.getBytes());
    }

    public void tearDown() {
        md5 = null;
    }

    /**
     * Tests if the constructor is working properly without null param
     */
    public void test_md5_contructor_param_not_null() {
        try {
            new MD5(new byte[0]);
        } catch (NullPointerException npe) {
            assertTrue(false);
        }
    }

    /**
     * Negative test for MD5 Constructor
     */
    public void test_constructor_param_null() {
        try {
            new MD5(null);
        } catch (NullPointerException npe) {
            assertTrue(false);
        }
    }

    /**
     * Negative test for MD5.toHex(null)
     */
    public void test_md5_tohex_param_null() {
        String mdHex32 = null;
        try {
            mdHex32 = MD5.toHex(null);
            assertTrue(false);
        } catch (NullPointerException npe) {
        }
        assertNull(mdHex32);
    }

    /**
     *  Positive test - real param, real result
     */
    public void test_md5_toHex_not_null() {
        assertEquals(TEST_STRING_TO_HEX, MD5.toHex(TEST_STRING.getBytes()));
    }

    /**
     * Negative test - real param, wrong result
     */
    public void test_md5_toHex_wrong_result() {
        assertFalse(TEST_STRING.equals(MD5.toHex(TEST_STRING.getBytes())));
    }

    /**
     * Tests the hex output data representation:
     * Should be 32 chars long, not null and equal to TEST_HASH
     */
    public void test_md5_hex_hash() {
        String mdHex32 = md5.toHex();

        assertNotNull(mdHex32);

        assertEquals(32, mdHex32.length());

        assertEquals(TEST_HASH, mdHex32);
    }

    /**
     * Test the hash representing the current state of the object
     * Should be equal to TEST_DIGEST in length and elem  
     */
    public void test_md5_hash() {
        byte[] digest = md5.doFinal();

        assertNotNull(digest);

        assertEquals(TEST_DIGEST.length, digest.length);

        for (int i = 0; i < digest.length; i++) {
            assertEquals(TEST_DIGEST[i], digest[i]);
        }
    }

    /**
     * Positive test - equal hash
     */
    public void test_md5_equals_hash() {
        assertTrue(MD5.equals(TEST_HASH.getBytes(), TEST_HASH.getBytes()));
    }

    /**
     * Negative test - hash not equal
     */
    public void test_md5_not_equals_hash() {
        assertFalse(MD5.equals(TEST_HASH.getBytes(), TEST_STRING.getBytes()));
    }

    /**
     * Positive test for b64 encoding - should be equal to B64_ENCODED_TEST 
     */
    public void test_to_base64_equal() {
        assertEquals(B64_ENCODED_TEST, MD5.toBase64(TEST_STRING.getBytes()));
    }

    /**
     * Negative test for b64 encoding - should not be equal 
     */
    public void test_to_base64_not_equal() {
        assertFalse(TEST_STRING.equals(MD5.toBase64(TEST_STRING.getBytes())));
    }

    /**
     * Negative test for fingerprint - NullPointerException expected
     */
    public void test_fingerprint_param_null() {
        try {
            md5.fingerprint(null);
            assertTrue(true);
        } catch (NullPointerException npe) {
            assertTrue(false);
        }
    }

    /**
     * Positive test for fingerprint - output should not be null
     */
    public void test_fingerprint_param_not_null() {
        assertNotNull(md5.fingerprint(TEST_STRING.getBytes()));
    }

}
