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
package com.zyb.nowplus.test;

import com.sonyericsson.junit.framework.TestSuite;
// import jmunit.framework.cldc11.TestSuite;

import com.zyb.nowplus.business.content.test.ContentProviderErrorTest;
import com.zyb.nowplus.business.content.test.ContentProviderGetTest;
import com.zyb.nowplus.business.content.test.ContentProviderUpdateTest;
import com.zyb.nowplus.business.content.test.DataContentSourceTest;
import com.zyb.nowplus.business.content.test.StorageIndexCleanTest;
import com.zyb.nowplus.business.content.test.StorageIndexLoadTest;
import com.zyb.nowplus.business.content.test.StorageIndexStoreTest;
import com.zyb.nowplus.business.test.ActivitiesTest;
import com.zyb.nowplus.business.test.CommunicationTest;
import com.zyb.nowplus.business.test.ContactsTest;
import com.zyb.nowplus.business.test.FirstStartUpTest;
import com.zyb.nowplus.business.test.MyProfileTest;
import com.zyb.nowplus.business.test.NetworkTest;
import com.zyb.nowplus.business.test.NextStartUpTest;
import com.zyb.nowplus.business.test.SortPerfTest;
import com.zyb.nowplus.business.test.WebaccountsTest;
import com.zyb.nowplus.data.protocol.test.CommunicationManagerImplTest;
import com.zyb.nowplus.data.protocol.test.Micro_Hessian_Input_Test;
import com.zyb.nowplus.data.protocol.test.Micro_Hessian_Output_Test;
import com.zyb.nowplus.data.protocol.test.ToolkitTest;
import com.zyb.nowplus.data.storage.test.RMSDataStoreTest;
import com.zyb.nowplus.data.storage.test.RMSKeyValueStoreTest;
import com.zyb.nowplus.presentation.test.ExtendedScreenHistoryTest;
import com.zyb.util.test.CollapsingQueueTest;
import com.zyb.util.test.DateHelperTest;
import com.zyb.util.test.HashUtilTest;
import com.zyb.util.test.IndexTest;
import com.zyb.util.test.MD5Test;
import com.zyb.util.test.PrioritizedReferencesTest;
import com.zyb.util.test.PushLogHandlerTest;
import com.zyb.util.test.QueueTest;
import com.zyb.util.test.TextUtilitiesTest;

/**
 * Test suite containing all tests.
 * Activate the --suite argument in the run-unittest-directly task to 
 * use this test suite.  
 */
public class AllTestsSuite extends TestSuite
{
	public AllTestsSuite()
	{
		addTestSuite(ActivitiesTest.class);
		addTestSuite(CollapsingQueueTest.class);
		addTestSuite(CommunicationManagerImplTest.class);
		addTestSuite(CommunicationTest.class);
		addTestSuite(ContactsTest.class);
//		addTestSuite(ContentProviderErrorTest.class);
//		addTestSuite(ContentProviderGetTest.class);	
//		addTestSuite(ContentProviderUpdateTest.class);
//		addTestSuite(DataContentSourceTest.class);
		addTestSuite(DateHelperTest.class);
		addTestSuite(ExtendedScreenHistoryTest.class);
		addTestSuite(FirstStartUpTest.class);
		addTestSuite(HashUtilTest.class);
		addTestSuite(IndexTest.class);
		addTestSuite(MD5Test.class);
		addTestSuite(Micro_Hessian_Input_Test.class);
		addTestSuite(Micro_Hessian_Output_Test.class);
		addTestSuite(MyProfileTest.class);
		addTestSuite(NetworkTest.class);
		addTestSuite(NextStartUpTest.class);
		addTestSuite(PrioritizedReferencesTest.class); 
		addTestSuite(PushLogHandlerTest.class);
		addTestSuite(QueueTest.class);
		addTestSuite(RMSDataStoreTest.class);	
		addTestSuite(RMSKeyValueStoreTest.class);
//		addTestSuite(RPGConnectionTest.class); *
//		addTestSuite(SafeThreadTest.class); *
		addTestSuite(SortPerfTest.class);
//		addTestSuite(StorageIndexCleanTest.class);
//		addTestSuite(StorageIndexLoadTest.class);
//		addTestSuite(StorageIndexStoreTest.class);	
		addTestSuite(TextUtilitiesTest.class);
		addTestSuite(ToolkitTest.class);
		addTestSuite(WebaccountsTest.class);
		addTestSuite(TextUtilitiesTest.class);
	}
}
