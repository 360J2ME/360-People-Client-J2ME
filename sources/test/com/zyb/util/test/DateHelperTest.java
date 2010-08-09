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


import java.util.Calendar;
import com.zyb.util.DateHelper;
import com.zyb.nowplus.test.TestCase;

/**
 * 
 */

/**
 * @author Jens Vesti
 *
 */
public class DateHelperTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    public void test_edgecases() {
    	
    	{
    		Calendar dob = Calendar.getInstance();
    		dob.set(Calendar.YEAR, 2000);
    		dob.set(Calendar.MONTH, 3);
    		dob.set(Calendar.DAY_OF_MONTH, 15);
    		Calendar now = Calendar.getInstance();
    		now.set(Calendar.YEAR, 2015);
    		now.set(Calendar.MONTH, 3);
    		now.set(Calendar.DAY_OF_MONTH, 14);
    		assertTrue(DateHelper.ageAcceptable(dob.getTime(), now.getTime(), 14));
    	}

    	{
    		Calendar dob = Calendar.getInstance();
    		dob.set(Calendar.YEAR, 2000);
    		dob.set(Calendar.MONTH, 3);
    		dob.set(Calendar.DAY_OF_MONTH, 15);
    		Calendar now = Calendar.getInstance();
    		now.set(Calendar.YEAR, 2014);
    		now.set(Calendar.MONTH, 3);
    		now.set(Calendar.DAY_OF_MONTH, 14);
    		assertTrue(!DateHelper.ageAcceptable(dob.getTime(), now.getTime(), 14));
    	}
    	
    	{
    		Calendar dob = Calendar.getInstance();
    		dob.set(Calendar.YEAR, 2000);
    		dob.set(Calendar.MONTH, 3);
    		dob.set(Calendar.DAY_OF_MONTH, 15);
    		Calendar now = Calendar.getInstance();
    		now.set(Calendar.YEAR, 2014);
    		now.set(Calendar.MONTH, 3);
    		now.set(Calendar.DAY_OF_MONTH, 15);
    		assertTrue(DateHelper.ageAcceptable(dob.getTime(), now.getTime(), 14));
    	}
    	
    	{
    		Calendar dob = Calendar.getInstance();
    		dob.set(Calendar.YEAR, 2000);
    		dob.set(Calendar.MONTH, 3);
    		dob.set(Calendar.DAY_OF_MONTH, 15);
    		Calendar now = Calendar.getInstance();
    		now.set(Calendar.YEAR, 2014);
    		now.set(Calendar.MONTH, 3);
    		now.set(Calendar.DAY_OF_MONTH, 16);
    		assertTrue(DateHelper.ageAcceptable(dob.getTime(), now.getTime(), 14));
    	}
    	
    	{
    		Calendar dob = Calendar.getInstance();
    		dob.set(Calendar.YEAR, 2000);
    		dob.set(Calendar.MONTH, 4);
    		dob.set(Calendar.DAY_OF_MONTH, 15);
    		Calendar now = Calendar.getInstance();
    		now.set(Calendar.YEAR, 2014);
    		now.set(Calendar.MONTH, 3);
    		now.set(Calendar.DAY_OF_MONTH, 15);
    		assertTrue(!DateHelper.ageAcceptable(dob.getTime(), now.getTime(), 14));
    	}
    	
    	{
    		Calendar dob = Calendar.getInstance();
    		dob.set(Calendar.YEAR, 2000);
    		dob.set(Calendar.MONTH, 3);
    		dob.set(Calendar.DAY_OF_MONTH, 15);
    		Calendar now = Calendar.getInstance();
    		now.set(Calendar.YEAR, 2014);
    		now.set(Calendar.MONTH, 4);
    		now.set(Calendar.DAY_OF_MONTH, 15);
    		assertTrue(DateHelper.ageAcceptable(dob.getTime(), now.getTime(), 14));
    	}
    }
}





