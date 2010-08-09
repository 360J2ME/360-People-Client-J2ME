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

//#if not polish.blackberry
package com.zyb.nowplus.data.protocol.apihelpers;



//#if use.alternative.java.math.path:defined
import com.nowplus.math.BigInteger;
//#else
//#= import java.math.BigInteger;
//#endif

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;

/*
 * VIS - IP Communications - Software Development
 * PROJECT: Vodafone API
 *
 * Copyright 2007-2009 Vodafone Group Services GmbH, (VGSG)
 * Copyright 2007-2009 Bruno Rodrigues <bruno.rodrigues@vodafone.com>
 * Registered Office: Mannesmannufer 2, D-40213 Duesseldorf, Germany,
 * Registered in Germany No. HRB 53554
 *
 * Unless otherwise stated in writing, software and IPR are fully owned by VGSG
 * and regarded as C3 - STRICTLY CONFIDENTIAL.
 * Any source code is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * C3 - STRICTLY CONFIDENTIAL
 */


/**
 * http://www.bouncycastle.org/latest_releases.html
 *
 * @author bruno
 */
public class CryptoRSA {

    public static final String PUBLIC_KEY = "" //
            + "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCj6QjJQuwnESiqFoiy8xjdT8/A" //
            + "mOGD/ScCWCm6L9ed8SiX5aGdY+AsBO7Y+6lS3yj8eg8HA7esDSCNKTMbZV+IQRAQ" //
            + "KDnA9AgRULAVYmEonHJaeRj10d9V8EP+qe7FGK1/hZ3Z3W+mG/fAkYy5rgWMSdp1" //
            + "2Y/wjSUFgERsllLUCQIDAQAB";

    public static final byte[] defaultPubKeyModulo = new byte[] { 0, -93, -23, 8, -55, 66, -20, 39, 17, 40, -86, 22, -120, -78,
            -13, 24, -35, 79, -49, -64, -104, -31, -125, -3, 39, 2, 88, 41, -70, 47, -41, -99, -15, 40, -105, -27, -95, -99,
            99, -32, 44, 4, -18, -40, -5, -87, 82, -33, 40, -4, 122, 15, 7, 3, -73, -84, 13, 32, -115, 41, 51, 27, 101, 95,
            -120, 65, 16, 16, 40, 57, -64, -12, 8, 17, 80, -80, 21, 98, 97, 40, -100, 114, 90, 121, 24, -11, -47, -33, 85, -16,
            67, -2, -87, -18, -59, 24, -83, 127, -123, -99, -39, -35, 111, -90, 27, -9, -64, -111, -116, -71, -82, 5, -116, 73,
            -38, 117, -39, -113, -16, -115, 37, 5, -128, 68, 108, -106, 82, -44, 9 };
    public static final byte[] defaultPubKeyExponential = new byte[] { 1, 0, 1 };

    /* *
     * Composes a RSA Public Key from its components.
     *
     * @param   mod     the RSA modulo.
     * @param   exp     the RSA exponent.
     * @return          the RSA public key.
     * /
    public static RSAKeyParameters getRSAPubKey( final byte[] key ) {
        ...FIXME...
    }*/

    /**
     * Composes a RSA Public Key from its components.
     *
     * @param   mod     the RSA modulo.
     * @param   exp     the RSA exponent.
     * @return          the RSA public key.
     */
    public static RSAKeyParameters getRSAPubKey( final byte[] mod, final byte[] exp ) {
        return new RSAKeyParameters( false, new BigInteger( mod ), new BigInteger( exp ) );
    }

    /**
     * Composes a RSA Public Key from its components.
     *
     * @param   mod     the RSA modulo.
     * @param   exp     the RSA exponent.
     * @return          the RSA public key.
     */
    public static RSAKeyParameters getRSAPubKey( final BigInteger mod, final BigInteger exp ) {
        return new RSAKeyParameters( false, mod, exp );
    }

    /**
     * Encrypts bytes with the given RSA Public Key.
     *
     * @param   pubKey      the RSA Public Key.
     * @param   data        the data to encrypt.
     * @return              the encrypted data.
     * @throws InvalidCipherTextException
     */
    public static byte[] encryptRSA( final RSAKeyParameters pubKey, final byte[] data ) throws InvalidCipherTextException {
        return CryptoRSA.rsa( true, pubKey, data );
    }

    /**
     * Encrypts or Decrypts bytes with the given RSA Public or Private Key.
     *
     * @param   encrypt     true for encrypt, false for decrypt.
     * @param   key         the RSA Public or Private Key.
     * @param   data        the data to encrypt or decrypt.
     * @return              the encrypted or decrypted data.
     */
    protected static byte[] rsa( final boolean encrypt, final RSAKeyParameters key, final byte[] data )
            throws InvalidCipherTextException {
        final byte[] dataAligned = new byte[ CryptoRSA.roundUp( data.length, 16 ) ];
        System.arraycopy( data, 0, dataAligned, 0, data.length );
        final RSAEngine rsa = new RSAEngine();
        final AsymmetricBlockCipher pkcs1 = new PKCS1Encoding( rsa );
        pkcs1.init( encrypt, key );
        return CryptoRSA.trimZeros( pkcs1.processBlock( dataAligned, 0, dataAligned.length ) );
    }

    protected static int roundUp( final int v, final int t ) {
        if( v % t == 0 )
            return v;
        return ( v / 16 + 1 ) * 16;
    }

    protected static byte[] trimZeros( final byte[] data ) {
        if( data == null )
            return null;
        if( data.length > 0 && data[data.length - 1] == 0 ) {
            byte[] result;
            for( int i = data.length - 1; i >= 0; i-- )
                if( data[i] != 0 ) {
                    result = new byte[ i + 1 ];
                    System.arraycopy( data, 0, result, 0, result.length );
                    return result;
                }
        }
        return data;
    }

    protected CryptoRSA() {
        super();
    }

}
//#endif