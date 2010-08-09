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
import com.zyb.util.TextUtilities;

import javax.microedition.lcdui.Font;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: florin
 * Date: 2009-02-20
 * Time: 15:31:19
 */
public class TextUtilitiesTest
	extends TestCase
{
    public static final int NUMBER = 123;
    public static final String NUMBER_WITH_PREFIX = "P123";

    private static final String VALID_EMAIL = "test@tst.com";
    private static final String FIRST_LETTER_DOT_EMAIL = ".test@tst.com";
    private static final String CONSECUTIVE_DOTS_EMAIL = "t..est@tst.com";
    private static final String LAST_LETTER_DOT_EMAIL = "test@tst.com.";
    private static final String DOT_BEFORE_AT_EMAIL = "test.@tst.com";
    private static final String DOT_AFTER_AT_EMAIL = "test@.tst.com";
    private static final String NO_DOT_AFTER_AT_EMAIL = "test@tstcom";

    private static final String VALID_USERNAME = "testusername";
    private static final String INVALID_USERNAME = "name";

    private static final String LINE = "test test test test test test test test test";

    private static final String LINE_WRAP_WITH_CONCAT = "t...";
    private static final int LINE_WIDTH = 24;
    private static final int SMALL_LINE_WIDTH = 10;

    private static String[] stringArray = new String[]{"test1", "test2"};
    private static Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_MEDIUM);


    public void setUp() {
    }

    /**
     * Test line wrap min width 24
     */
    public void test_wrap_line() {
        String[] line = TextUtilities.wrap(LINE, LINE_WIDTH, font);
        assertNotNull(line);
    }

    /**
     * Negative test - should not wrap, the line width is too small 
     */
    public void test_wrap_line_width_to_small() {
        String[] line = TextUtilities.wrap(LINE, SMALL_LINE_WIDTH, font);
        assertTrue(line.length == 1);
        assertEquals("", line[0]);
    }

    /**
     * Test line wrap with concat, adding "..."
     */
    public void test_wrap_with_concat() {
        String[] line = TextUtilities.wrap(LINE, LINE_WIDTH, font, true);
        assertEquals(LINE_WRAP_WITH_CONCAT, line[0]);
    }

    /**
     * Negative test -  email null
     */
    public void test_email_null() {
        assertFalse(TextUtilities.isValidEmail(null));
    }

    /**
     * Positive test - valid email format
     */
    public void test_email_valid() {
        assertTrue(TextUtilities.isValidEmail(VALID_EMAIL));
    }

    /**
     * Negative test - email with first letter dot
     */
    public void test_email_with_first_letter_dot() {
        assertFalse(TextUtilities.isValidEmail(FIRST_LETTER_DOT_EMAIL));
    }

    /**
     * Negative test - email with consecutive dots
     */
    public void test_email_with_consecutive_dots() {
        assertFalse(TextUtilities.isValidEmail(CONSECUTIVE_DOTS_EMAIL));
    }

    /**
     * Negative test - emial with last letter dot
     */
    public void test_email_with_last_letter_dot() {
        assertFalse(TextUtilities.isValidEmail(LAST_LETTER_DOT_EMAIL));
    }

    /**
     * Negative test - email with dot before at: ".@"
     */
    public void test_email_with_dot_before_at() {
        assertFalse(TextUtilities.isValidEmail(DOT_BEFORE_AT_EMAIL));
    }

    /**
     * Negative test - email containig dot after at : "@."  
     */
    public void test_email_with_dot_after_at() {
        assertFalse(TextUtilities.isValidEmail(DOT_AFTER_AT_EMAIL));
    }

    /**
     * Negative test  - email with no dot after "@"
     */
    public void test_email_with_no_dot_after_at() {
        assertFalse(TextUtilities.isValidEmail(NO_DOT_AFTER_AT_EMAIL));
    }

    /**
     * Negative test - should return false, user is null
     */
    public void test_username_null() {
        assertFalse(TextUtilities.isValidUsername(null));
    }

    /**
     * Positive test - user should be valid
     */
    public void test_valid_username() {
        assertTrue(TextUtilities.isValidUsername(VALID_USERNAME));
    }

    /**
     * Negative test -  user length is < 6
     */
    public void test_invalid_username() {
        assertFalse(TextUtilities.isValidUsername(INVALID_USERNAME));
    }

    /**
     * Negative test - it should contain the value associated to null 
     */
    public void test_serialize_array_null_array() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        try {
            ArrayUtils.serializeArray((String[]) null, out);
            out.flush();
            assertTrue("Should contain something", bout.size() > 0);
        } catch (IOException e) {
            assertTrue("Should not throw IOException because the buffer is ByteArrayOutputStream", false);
        } catch (NullPointerException npe) {
            assertTrue("Should not throw null poninter exception", false);
        }
    }

    /**
     * Negative test - expected NullPointerException
     */
    public void test_serialize_array_null_stream() {
        try {
            ArrayUtils.serializeArray(stringArray, null);
            assertTrue(false);
        } catch (IOException e) {
            assertTrue("Should not throw IOException", true);
        } catch (NullPointerException npe) {
            assertTrue("Should throw null poninter exception", true);
        }
    }

    /**
     * Positive test - output should not be null 
     */
    public void test_serialize_array_not_null() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        try {
            ArrayUtils.serializeArray(stringArray, out);
            assertTrue(bout.size() > 0);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Negative test- expected NullPointerException
     */
    public void test_deserialize_string_array_null() {
        try {
            ArrayUtils.deserializeStringArray(null);
            assertTrue(false);
        } catch (NullPointerException npe) {
//            npe.printStackTrace();
            assertTrue(true);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Negative test- expected Exception
     */
    public void test_deserialize_string_array_empty() {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(new byte[0]);
            DataInputStream in = new DataInputStream(bin);
            ArrayUtils.deserializeStringArray(in);
            assertTrue(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }

    /**
     * Positive test - shold return a string array with 2 elements
     */
    public void test_deserialize_string_array_not_null() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        try {
            ArrayUtils.serializeArray(stringArray, out);
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            DataInputStream in = new DataInputStream(bin);
            String[] strings = ArrayUtils.deserializeStringArray(in);

            assertNotNull(strings);
            assertEquals(stringArray.length, strings.length);
        } catch (IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void tearDown() {
    }
}
