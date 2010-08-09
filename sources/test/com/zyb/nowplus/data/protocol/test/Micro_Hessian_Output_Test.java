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

import com.zyb.nowplus.data.protocol.hessian.MicroHessianOutput;
import com.zyb.nowplus.test.Assert;
import com.zyb.nowplus.test.TestCase;

import java.util.Hashtable;
import java.util.Date;
import java.util.Vector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: ftite
 */

public class Micro_Hessian_Output_Test extends TestCase {
    private static final String USERNAME = "test";
    private static final String HASH = "2342342";
    private static final String AUTH = "3442232";
    private static final String FAULT = "fault";
    private static final String AUTH_METHOD = "auth/getsessionbycredentials";

    Hashtable hReq;
    Hashtable hMore;
    ByteArrayOutputStream os;
    MicroHessianOutput hOut;


    public void setUp()
    {
        hReq = new Hashtable();
        hMore = new Hashtable();
        os = new ByteArrayOutputStream();
        hOut = new MicroHessianOutput(os);
    }
    
    private String getRandomId() {
        long ts = ((long) System.currentTimeMillis() / 1000);

       // create unique ID
        // long l = (new Random()).nextLong();
        return Long.toString(ts);
    }

    private void fill_hReq() {
        hMore.put("appinstance", getRandomId());
        hReq.put("timestamp", new Long(Long.parseLong(getRandomId())));
        hReq.put("username", USERNAME);
        hReq.put("hash", HASH);
        hReq.put("more", hMore);
        hReq.put("auth", AUTH);
    }

    /**
     * Negative test - uninitialized Hessian output stream
     * expected NullPointerException 
     */
    public void test_startCall_null_outputStream() {
        hOut = new MicroHessianOutput();
        try {
            hOut.startCall(AUTH_METHOD);
            assertTrue(false);
        } catch (NullPointerException e) {
            assertTrue(true);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException" + e.getMessage());
        }
    }

    /**
     * Negative test - null param
     * should not be null
     */
    public void test_startCall_null_param() {
        try {
            hOut.startCall(null);
            assertTrue(os.toByteArray().length > 0);
        } catch (NullPointerException npe) {
            Assert.fail("Unexpected NullPointerException: " + npe.getMessage());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Positive test - valid params 
     */
    public void test_startCall() {
        try {
            hOut.startCall(AUTH_METHOD);
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test add completeCall tag to stream 
     */
    public void test_completeCall() {
        try {
            hOut.completeCall();
            assertTrue(os.toByteArray().length > 0); 
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test add startReply tag to strem 
     */
    public void test_startReply() {
        try {
            hOut.startReply();
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test add completeReply tag to string
     */
    public void test_completeReply() {
        try {
            hOut.completeReply();
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Negative test - add fault tag to stream null param
     * should not be null
     */
    public void test_writeFault_null() {
        try {
            hOut.writeFault(null, null, null);
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Positive test - add fault tag to stream  with valid params
     */
    public void test_writeFault_not_null() {
        try {
            hOut.writeFault(FAULT, FAULT, FAULT);
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test write ref to stream, should not be null 
     */
    public void test_writeRef() {
        try {
            hOut.writeRef(1);
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test writting different object types to stream
     */
    public void test_writeObject() {
        Vector v = new Vector();
        v.addElement("test");
        Object[] obj = new Object[]{new Integer(1)};
        try {
            //writeNull
            hOut.writeObject(null);
            int length = os.toByteArray().length;
            assertTrue("writeNull : ", length > 0);

            //writeString
            hOut.writeObject("test");
            assertTrue("writeString : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeBoolean
            hOut.writeObject(Boolean.TRUE);
            assertTrue("writeBoolean  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeInt
            hOut.writeObject(new Integer(1));
            assertTrue("writeInt  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeLong
            hOut.writeObject(new Long(2));
            assertTrue("writeLong  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeUTCDate
            hOut.writeObject(new Date());
            assertTrue("writeUTCDate  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeBytes
            hOut.writeObject("test".getBytes());
            assertTrue("writeBytes  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeVector
            hOut.writeObject(v);
            assertTrue("writeVector  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeArray
            hOut.writeObject(obj);
            assertTrue("writeArray  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeHashtable
            fill_hReq();
            hOut.writeObject(hReq);
            assertTrue("writeHashtable  : ", os.toByteArray().length > length);
            length = os.toByteArray().length;

            //writeCustomObject
            hOut.writeObject(new TestCase());
            assertTrue("writeCustomObject  : ", os.toByteArray().length > length);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test write ListBegin tag to stream
     */
    public void test_writeListBegin() {
        try {
            hOut.writeListBegin(2, "test");
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test write ListEnd to stream 
     */
    public void test_writeListEnd() {
        try {
            hOut.writeListEnd();
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test write Map begin tag to stream 
     */
    public void test_writeMapBegin() {
        try {
            hOut.writeMapBegin("test");
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test write Map end tag to stream
     */
    public void test_writeMapEnd() {
        try {
            hOut.writeMapEnd();
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test write a remote object reference to stream
     */
    public void test_writeRemote() {
        try {
            hOut.writeRemote("test", "http://test.tst");
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Negative test - null param. should not be null
     */
    public void test_printLenString_null() {
        try {
            hOut.printLenString(null);
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Positive test  - param not null
     */
    public void test_printLenString_not_null() {
        try {
            hOut.printLenString("test");
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /**
     * Test print String, should not be null
     */
    public void test_printString() {
        try {
            hOut.printString("test");
            assertTrue(os.toByteArray().length > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    public void tearDown()
	{
        hReq = null;
        hMore = null;
        hOut = null;
        if (os != null) {
            try {
                os.close();
                os = null;
            } catch (IOException e) {
               Assert.fail("Error on closing the OutputStream, IOException: " + e.getMessage());
            }
        }
    }
}
