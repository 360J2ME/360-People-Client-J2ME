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

import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;
import com.zyb.util.Index;

public class IndexTest extends TestCase
{	
	private Index index;
	
	public void setUp()
	{
		Index.MAX_SIZE = 3;
		index = Index.create();
	}

	public void testDepth1()
	{	
		// empty
		Assert.assertEquals("()", index.toString());
		
		// one element
		index = index.set("abc", "cba");
		
		Assert.assertEquals("cba", index.get("abc"));
		Assert.assertNull(index.get("abd"));
		Assert.assertEquals("(abc=cba)", index.toString());
		
		// three elements
		index = index.set("abc", "c b a")
		.set("bc", "cb")
		.set("bcd", "dcb");
		
		Assert.assertEquals("c b a", index.get("abc"));
		Assert.assertEquals("cb", index.get("bc"));
		Assert.assertEquals("dcb", index.get("bcd"));
		Assert.assertNull(index.get("abd"));
		Assert.assertEquals("(abc=c b a,bc=cb,bcd=dcb)", index.toString());
	}
	
	public void testDepth2()
	{
		index = index.set("abc", "cba")
		.set("bc", "cb")
		.set("bcd", "dcb")
		.set("cdef", "fedc");
		
		Assert.assertEquals("cba", index.get("abc"));
		Assert.assertEquals("fedc", index.get("cdef"));
		Assert.assertNull(index.get("abd"));
		Assert.assertEquals("(a=(bc=cba),b=(c=cb,cd=dcb),c=(def=fedc))", index.toString());
	
		// new node at depth 1; new node at depth 2; new node ""  
		index = index.set("def", "fed")
		.set("bce", "ecb")
		.set("c", "c");
		
		Assert.assertEquals("cba", index.get("abc"));
		Assert.assertEquals("ecb", index.get("bce"));
		Assert.assertEquals("c", index.get("c"));
		Assert.assertEquals("fed", index.get("def"));
		Assert.assertEquals("(a=(bc=cba),b=(c=cb,cd=dcb,ce=ecb),c=(def=fedc,=c),d=(ef=fed))", index.toString());
	}
	
	public void testDepth3()
	{
		index = index.set("abc", "cba")
		.set("bc", "cb")
		.set("bcd", "dcb")
		.set("cdef", "fedc")
		.set("def", "fed")
		.set("bce", "ecb")
		.set("c", "c")
		.set("bcde", "edcb");
		
		Assert.assertEquals("cb", index.get("bc"));
		Assert.assertEquals("dcb", index.get("bcd"));
		Assert.assertEquals("edcb", index.get("bcde"));
		Assert.assertEquals("ecb", index.get("bce"));
		Assert.assertEquals("(a=(bc=cba),b=(c=( =(=cb),d=(=dcb,e=edcb),e=(=ecb))),c=(def=fedc,=c),d=(ef=fed))", index.toString());
		
		// change at depth 2, depth 4, depth 4 
		index.set("abc", "c b a")
		.set("bc", "c b")
		.set("bcde", "e d c b");
		
		Assert.assertEquals("c b a", index.get("abc"));
		Assert.assertEquals("c b", index.get("bc"));
		Assert.assertEquals("e d c b", index.get("bcde"));
		Assert.assertEquals("(a=(bc=c b a),b=(c=( =(=c b),d=(=dcb,e=e d c b),e=(=ecb))),c=(def=fedc,=c),d=(ef=fed))", index.toString());
	}
	
	public void tearDown()
	{
		index = null;
	}
}
