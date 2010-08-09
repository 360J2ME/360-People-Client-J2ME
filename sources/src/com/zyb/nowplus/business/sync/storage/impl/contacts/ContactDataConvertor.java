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
package com.zyb.nowplus.business.sync.storage.impl.contacts;

import com.zyb.nowplus.business.domain.*;
import com.zyb.nowplus.business.sync.util.CRC32;
import com.zyb.util.ArrayUtils;
import com.zyb.util.ImageUtil;

import javax.microedition.pim.Contact;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import java.util.Date;
import java.util.Calendar;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Contacts Sync Data Convertor
 * <p/>              Iden
 * This is the 'transparent' Conversion Layer which handles
 * contacts conversion, contacts revisioning, device fragmentation and
 * field usage preselection, etc. , for both the Native Contacts and
 * the Client Application Contacts.
 *
 * @author Andrei Marcut, andrei@zybmail.com
 */
public class ContactDataConvertor
{
    public static final int CAB_HOME = Identity.SUBTYPE_HOME;
    public static final int CAB_MOBILE = Identity.SUBTYPE_MOBILE;
    public static final int CAB_FAX = Identity.SUBTYPE_FAX;
    public static final int CAB_WORK = Identity.SUBTYPE_WORK;
    public static final int CAB_OTHER = Identity.SUBTYPE_OTHER;

    public static final int CAB_ADDR_HOME = Address.TYPE_HOME;
    public static final int CAB_ADDR_WORK = Address.TYPE_WORK;
    public static final int CAB_ADDR_OTHER = Address.TYPE_OTHER;

    public static final int NAB_NONE = Contact.ATTR_NONE;
    public static final int NAB_ASST = Contact.ATTR_ASST;
    public static final int NAB_AUTO = Contact.ATTR_AUTO;
    public static final int NAB_FAX = Contact.ATTR_FAX;
    public static final int NAB_HOME = Contact.ATTR_HOME;
    public static final int NAB_MOBILE = Contact.ATTR_MOBILE;
    public static final int NAB_OTHER = Contact.ATTR_OTHER;
    public static final int NAB_PAGER = Contact.ATTR_PAGER;
    public static final int NAB_PREF = Contact.ATTR_PREFERRED;
    public static final int NAB_SMS = Contact.ATTR_SMS;
    public static final int NAB_WORK = Contact.ATTR_WORK;
    public static final int NAB_FORMATTED = 1024;
    
//#if polish.blackberry
    public static final int NAB_HOME2 = net.rim.blackberry.api.pdap.BlackBerryContact.ATTR_HOME2;
    public static final int NAB_WORK2 = net.rim.blackberry.api.pdap.BlackBerryContact.ATTR_WORK2;
//#endif

    public static final int NAB_EXT_1 = 2048;
    public static final int NAB_EXT_2 = 4096;
    public static final int NAB_EXT_3 = 8192;

    public static final int NAB_UNSUPPORTED = 1 << 31;

    /* Complex TEL attributes */
    /* Mobile extensions */
    public static final int NAB_MOBILE_OTHER = Contact.ATTR_MOBILE | Contact.ATTR_OTHER;
    public static final int NAB_MOBILE_PRIVATE = NAB_MOBILE_OTHER;
    public static final int NAB_MOBILE_WORK = Contact.ATTR_MOBILE | Contact.ATTR_WORK;
    public static final int NAB_MOBILE_HOME = Contact.ATTR_MOBILE | Contact.ATTR_HOME;

//    /* Preffered extensions*/
//    public static final int NAB_PREF_FAX = Contact.ATTR_PREFERRED | Contact.ATTR_FAX;
//    public static final int NAB_PREF_HOME = Contact.ATTR_PREFERRED | Contact.ATTR_HOME;
//    public static final int NAB_PREF_MOBILE = Contact.ATTR_PREFERRED | Contact.ATTR_MOBILE;
//    public static final int NAB_PREF_WORK = Contact.ATTR_PREFERRED | Contact.ATTR_OTHER;
//    public static final int NAB_PREF_OTHER = Contact.ATTR_PREFERRED | Contact.ATTR_OTHER;
//    public static final int NAB_PREF_MOBILE_WORK = Contact.ATTR_PREFERRED | NAB_MOBILE_WORK;
//    public static final int NAB_PREF_MOBILE_OTHER = Contact.ATTR_PREFERRED | NAB_MOBILE_OTHER;
//    public static final int NAB_PREF_MOBILE_PRIVATE = NAB_PREF_MOBILE_OTHER;


    /*NAB_MOBILE_HOME, NAB_PREF_MOBILE_HOME, NAB_MOBILE_OTHER, NAB_PREF_MOBILE_OTHER*/
//    public static final int NAB_VOICE_CELL = Contact.ATTR_MOBILE | Contact.ATTR_OTHER;
//    public static final int NAB_CELL_WORK = Contact.ATTR_MOBILE | Contact.ATTR_WORK;
//    public static final int NAB_CELL_HOME = Contact.ATTR_MOBILE | Contact.ATTR_HOME;
//    public static final int NAB_VOICE_HOME = Contact.ATTR_HOME | Contact.ATTR_OTHER;
//    public static final int NAB_VOICE_WORK = Contact.ATTR_WORK | Contact.ATTR_OTHER;
//    public static final int NAB_FAX_HOME = Contact.ATTR_FAX | Contact.ATTR_HOME;
//    public static final int NAB_FAX_WORK = Contact.ATTR_FAX | Contact.ATTR_WORK;
//
//    public static final int NAB_PREF_VOICE_WORK = Contact.ATTR_PREFERRED | Contact.ATTR_WORK | Contact.ATTR_OTHER;
//
//    public static final int NAB_PREF_FAX_WORK = Contact.ATTR_PREFERRED | Contact.ATTR_FAX | Contact.ATTR_WORK;
//    public static final int NAB_PREF_PAGER = Contact.ATTR_PREFERRED | Contact.ATTR_PAGER;
//    public static final int NAB_PREF_CELL_WORK = Contact.ATTR_PREFERRED | Contact.ATTR_MOBILE | Contact.ATTR_WORK;
//    public static final int NAB_PREF_CELL_HOME = Contact.ATTR_PREFERRED | Contact.ATTR_MOBILE | Contact.ATTR_HOME;
//
//    public static final int NAB_PREF_FAX_HOME = Contact.ATTR_PREFERRED | Contact.ATTR_FAX | Contact.ATTR_HOME;

    //#if (polish.identifier==Samsung/SGH-U700-ZYB)
    //Samsung U700 extensions
    public static final int NAB_SMSNG_U700_ADDR_HOME = 65536;
    public static final int NAB_SMSNG_U700_ADDR_WORK = 131072;
    //#endif
    
    //#if (polish.identifier==Samsung/SGH-F330-ZYB)
    //Samsung F330 extensions
    public static final int NAB_SMSNG_F330_ADDR_HOME = 65536;
    public static final int NAB_SMSNG_F330_ADDR_WORK = 131072;
    //#endif

    //#if (polish.identifier==Samsung/SGH-U800-ZYB)
    //Samsung U800 extensions
    public static final int NAB_SMSNG_U800_EMAIL_HOME = 32768;
    public static final int NAB_SMSNG_U800_EMAIL_WORK = 65536;
    public static final int NAB_SMSNG_U800_ADDR_HOME = 524288;
    public static final int NAB_SMSNG_U800_ADDR_WORK = 1048576;
    //#endif
    
    //#if (polish.identifier==Samsung/SGH-U900-ZYB)
    //Samsung U900 extensions
    public static final int NAB_SMSNG_U900_EMAIL_HOME = 32768;
    public static final int NAB_SMSNG_U900_EMAIL_WORK = 65536;
    public static final int NAB_SMSNG_U900_ADDR_HOME = 524288;
    public static final int NAB_SMSNG_U900_ADDR_WORK = 1048576;
    //#endif

    //#if (polish.identifier==Samsung/SGH-F400-ZYB)
    //Samsung F400 extensions
    public static final int NAB_SMSNG_F400_EMAIL_HOME = 32768;
    public static final int NAB_SMSNG_F400_EMAIL_WORK = 65536;
    public static final int NAB_SMSNG_F400_ADDR_HOME = 524288;
    public static final int NAB_SMSNG_F400_ADDR_WORK = 1048576;
    //#endif
    
    //#if (polish.identifier==LG/KS500-ZYB)
    //LG-KS500 extensions
    public static final int NAB_LG_ADDR_HOME = 100;
    public static final int NAB_LG_ADDR_WORK = 16777216;
    //#endif
    
    // private static final String LINE_SEPARATOR = "\r\n";
    //TODO: (fix) this is a workaround because "\r\n" is not supported in the formatted_addr value.
    private static final String LINE_SEPARATOR = " ";
    private static final String COMMA_SEPARATOR = ",";
    private static final String SPACE_SEPARATOR = " ";


//#message Preprocessing Internal Sync Conversion

/* CAB_SUBTYPE: NAB_ATTR */
//#message TEL
//#if polish.pim.support.TEL:defined

    //#if polish.pim.support.TEL.PREF:defined
    //#= private static boolean isTelPrefSupported = true;
    //#else
    private static boolean isTelPrefSupported = false;
    //#endif
    private static final int[][][] telOccMaps;
    private static final int[] telSubtypes;
    //#if polish.pim.support.TEL.maxValues:defined
    //#= private static int maxTelCount = ${polish.pim.support.TEL.maxValues};
    //#else
    private static int maxTelCount = 1;
//#endif


    static {
    	//#debug debug 
        System.out.println("TEL Static Initializer");

        telSubtypes = new int[]{
        		//#= ${polish.pim.support.TEL}
        };
        int[][] telMaps = new int[telSubtypes.length][];
        telOccMaps = new int[telMaps.length][][];
//#message CAB_TEL::${polish.pim.support.TEL}

        int i = 0, j;

//#foreach fieldMap in polish.pim.support.TEL

//#message ${fieldMap} <==> ${polish.pim.mapping.TEL.${fieldMap}}
//#debug debug 
//#=        System.out.println("${fieldMap} <==> ${polish.pim.mapping.TEL.${fieldMap}}");
//#=    telMaps[i] =new int[]{${polish.pim.mapping.TEL.${fieldMap}}};
//#=    telOccMaps[i] = new int[telMaps[i].length][];
        j = 0;
//#foreach fieldOccMap in polish.pim.mapping.TEL.${fieldMap}
//#if polish.pim.mapping.occurency.TEL.${fieldOccMap}:defined
//#=   telOccMaps[i][j++] = new int[]{${polish.pim.mapping.occurency.TEL.${fieldOccMap}}};
//#else
        telOccMaps[i][j++] = new int[]{
//#=        ${fieldOccMap}
        };
//#endif
//#next fieldOccMap
//#= i++;        
//#next fieldMap
    }
//#else
//#message Undefined
//#endif

/* CAB_SUBTYPE: NAB_ATTR */
//#message EMAIL
//#if polish.pim.support.EMAIL:defined

    //#if polish.pim.support.EMAIL.PREF:defined
    //#= private static boolean isEmailPrefSupported = true;
    //#else
    private static boolean isEmailPrefSupported = false;
    //#endif    

    private static final int[][][] emailOccMaps;
    private static final int[] emailSubtypes;
    //#if polish.pim.support.EMAIL.maxValues:defined
    //#= private static int maxEmailCount = ${polish.pim.support.EMAIL.maxValues};
    //#else
    private static int maxEmailCount = 1;
    //#endif

    static {
//#debug 
        System.out.println("EMAIL Static Initializer");
        emailSubtypes = new int[]{
//#=        ${polish.pim.support.EMAIL}
        };
        int[][] emailMaps = new int[emailSubtypes.length][];
        emailOccMaps = new int[emailMaps.length][][];
//#message CAB_EMAIL::${polish.pim.support.EMAIL}
        int i = 0, j;
//#foreach fieldMap in polish.pim.support.EMAIL

//#message ${fieldMap} <==> ${polish.pim.mapping.EMAIL.${fieldMap}}
//#debug debug
//#=        System.out.println("${fieldMap} <==> ${polish.pim.mapping.EMAIL.${fieldMap}}");        
//#=    emailMaps[i] =new int[]{${polish.pim.mapping.EMAIL.${fieldMap}}};
//#=    emailOccMaps[i] = new int[emailMaps[i].length][];
        j = 0;
//#foreach efOccMap in polish.pim.mapping.EMAIL.${fieldMap}
//#message Now on ${efOccMap}
//#if polish.pim.mapping.occurency.EMAIL.${efOccMap}:defined
//#=   emailOccMaps[i][j++] = new int[]{${polish.pim.mapping.occurency.EMAIL.${efOccMap}}};
//#else
//#message UNDEFINED!? polish.pim.mapping.occurency.EMAIL.${efOccMap}
        emailOccMaps[i][j++] = new int[]{
//#=        ${efOccMap}
        };
//#endif
//#next efOccMap
//#= i++;        
//#next fieldMap
    }
//#else
//#message Undefined
//#endif


/* CAB_SUBTYPE: NAB_ATTR */
//#message ADDRESS
//#if polish.pim.support.ADDRESS:defined

    private static final int[][] addrMaps;
    private static final int[] addrSubtypes;

    static {
        addrSubtypes = new int[]{
//#=        ${polish.pim.support.ADDRESS}
        };
        addrMaps = new int[addrSubtypes.length][];
//#message CAB_ADDRESS::${polish.pim.support.ADDRESS}
//#=    int i = 0;
//#foreach fieldMap in polish.pim.support.ADDRESS
//#message ${fieldMap} <==> ${polish.pim.mapping.ADDRESS.${fieldMap}}
//#=    addrMaps[i++] =new int[]{${polish.pim.mapping.ADDRESS.${fieldMap}}};
//#next fieldMap
    }
//#else
//#message Undefined
//#endif

    private static final String EMPTY_STRING = "";
    private static final String ONE_SPACE = " ";
    private static final int WORK_TITLE = 2;
    private static final int WORK_COMPANY = 0;

    public static int calcCheckSum(ContactProfile contactProfile)
    {
        //#debug debug
        System.out.println("ProfileRevision");

        int checksum = 31;

        String value;
        //#message NAME
        //#if (polish.pim.mapping.NAME:defined && ${lowercase(polish.pim.mapping.NAME)} == nab_formatted)
        //#= checksum = CRC32.update(contactProfile.getFormattedName(), checksum);
        //#message FORMATTED
        //#else
        //#message DEFAULT
        //#if (polish.pim.support.NAME.FIRST:defined && ${lowercase(polish.pim.support.NAME.FIRST)} == true)
        //#debug debug
        System.out.println("NAME");

        if (null != (value = contactProfile.getFirstName())
                && !EMPTY_STRING.equals(value)) {
            checksum = CRC32.update(value, checksum);
        }
        //#endif
        //#if (polish.pim.support.NAME.MIDDLE:defined && ${lowercase(polish.pim.support.NAME.MIDDLE)} == true)
        if (null != (value = contactProfile.getMiddleNames())
                && !EMPTY_STRING.equals(value)) {
            checksum = CRC32.update(value, checksum);
        }
        //#endif
        //#if (polish.pim.support.NAME.LAST:defined && ${lowercase(polish.pim.support.NAME.LAST)} == true)
        if (null != (value = contactProfile.getLastName())
                && !EMPTY_STRING.equals(value)) {
            checksum = CRC32.update(value, checksum);
        }
        //#endif
        //#endif

        //#debug debug
        System.out.println("chks1=" + checksum);

        //#if (polish.pim.support.TITLE:defined || polish.pim.support.ORG:defined)
        String[] wd = contactProfile.getWorkDetails();

        if (wd.length >= 3) {
            //#endif
            //#if polish.pim.support.TITLE
            //#debug debug
            System.out.println("TITLE");

            if (wd[WORK_TITLE] != null && !EMPTY_STRING.equals(wd[WORK_TITLE]))
                checksum = CRC32.update(wd[WORK_TITLE], checksum);
            //#endif
            //#if polish.pim.support.ORG
            //#debug debug
            System.out.println("ORG");

            if (wd[WORK_COMPANY] != null && !EMPTY_STRING.equals(wd[WORK_COMPANY]))
                checksum = CRC32.update(wd[WORK_COMPANY], checksum);
            //#endif
            //#if (polish.pim.support.TITLE:defined || polish.pim.support.ORG:defined)
        }
        //#endif

        //#if polish.pim.support.URL
        //#debug debug
        System.out.println("URL");

        Identity url = contactProfile.getUrl();

        if (url != null && !url.isEmpty())
            checksum = CRC32.update(url.getName(), checksum);
        //#endif

        //#if polish.pim.support.BIRTHDAY:defined && polish.pim.support.BIRTHDAY
        {
            //#debug debug
            System.out.println("BDAY");

            Date birthdate;

            if ((birthdate = contactProfile.getDateOfBirth()) != null) {
                Calendar c = Calendar.getInstance();

                synchronized (c) {
                    c.setTime(birthdate);
                    checksum = CRC32.update(c.get(Calendar.DATE), checksum);
                    checksum = CRC32.update(c.get(Calendar.MONTH), checksum);
                    checksum = CRC32.update(c.get(Calendar.DAY_OF_MONTH), checksum);
                }
//                checksum = CRC32.update(birthdate.getTime(), checksum);
                //#debug debug
//                System.out.println("BDAY=" + birthdate.getTime());
            }
        }
        //#endif

        //#debug debug
        System.out.println("chks2=" + checksum);

        //#if polish.pim.support.NICKNAME:defined
        //#debug debug
        System.out.println("NICKNAME");

        checksum = CRC32.update(contactProfile.getNickname(), checksum);
        //#endif

        //#if polish.pim.support.NOTE:defined
        //#debug debug
        System.out.println("NOTE");
        
        Note note = contactProfile.getNote();
        checksum = CRC32.update((note == null) ? "" : note.getContent(), checksum);
        //#endif

        //#debug debug
        System.out.println("chks3=" + checksum);

        //#if polish.pim.support.TEL:defined
        //#debug debug
        System.out.println("TEL");

        checksum = calcCheckSum(Identity.TYPE_PHONE, telOccMaps, contactProfile, checksum);
        //#endif

        //#debug debug
        System.out.println("chks4=" + checksum);

        //#if polish.pim.support.EMAIL:defined
        //#debug debug
        System.out.println("EMAIL");

        checksum = calcCheckSum(Identity.TYPE_EMAIL, emailOccMaps, contactProfile, checksum);
        //#endif

        //#debug debug
        System.out.println("chks5=" + checksum);

        //#if polish.pim.support.ADDRESS:defined
        //#debug debug
        System.out.println("ADDR");

        Address addr;

        for (int i = 0; i < addrMaps.length; i++) {
            for (int j = 0; j < addrMaps[i].length; j++) {
                if (0 != (addrMaps[i][j] & NAB_FORMATTED)) {
                    addr = contactProfile.getAddress(addrMaps[i][j] - NAB_FORMATTED);
                }
                else {
                    addr = contactProfile.getAddress(addrMaps[i][j]);
                }

                if (null != addr) {
                    if (null != (value = addr.getCountry()) &&
                            !EMPTY_STRING.equals(value))
                        checksum = CRC32.update(value, checksum);
                    if (null != (value = addr.getPostcode()) &&
                            !EMPTY_STRING.equals(value))
                        checksum = CRC32.update(value, checksum);
                    if (null != (value = addr.getRegion()) &&
                            !EMPTY_STRING.equals(value))
                        checksum = CRC32.update(value, checksum);
                    if (null != (value = addr.getStreet1()) &&
                            !EMPTY_STRING.equals(value))
                        checksum = CRC32.update(value, checksum);
                    if (null != (value = addr.getStreet2()) &&
                            !EMPTY_STRING.equals(value))
                        checksum = CRC32.update(value, checksum);
                    if (null != (value = addr.getTown()) &&
                            !EMPTY_STRING.equals(value))
                        checksum = CRC32.update(value, checksum);
                }
            }
        }
        //#endif

        //#if polish.pim.support.PHOTO:defined
        CRC32.update(ImageUtil.getRGB(contactProfile.getProfileImage().getImage()), checksum);
        //#endif

        //#debug debug
        System.out.println("chksFinal=" + checksum);

        return checksum;
    }

    private static int calcCheckSum(int type, int[][][] occMap, ContactProfile contactProfile, int checksum)
    {
        for (int i = 0; i < occMap.length; i++) {
            for (int j = 0; j < occMap[i].length; j++) {
                for (int o = 0; o < occMap[i][j].length; o++) {
                    int attr = occMap[i][j][o];
                    Identity idy = contactProfile.getIdentity(type, attr);

                    if (idy != null) {
                        /* NOT EXISTING */
                    	String value = idy.getName();
                        checksum = CRC32.update(value, checksum);
                        checksum = CRC32.update(attr, checksum);
                    }
                }
            }
        }

        return checksum;
    }


    public static int calcCheckSum(Contact contact)
    {
        //#debug debug
        System.out.println("ContactRevision");

        int checksum = 37;

        String value;
        //#if (polish.pim.support.NAME:defined && ${lowercase(polish.pim.support.NAME)} == nab_formatted)
        //handle Formatted Name
        if (contact.countValues(Contact.FORMATTED_NAME) > 0) {
            checksum = CRC32.update(contact.getString(Contact.FORMATTED_NAME, 0), checksum);
        }
        //#else
        //#debug debug
        System.out.println("NAME");
        if (contact.countValues(Contact.NAME) > 0) {
            String[] names = contact.getStringArray(Contact.NAME, 0);
            
            //#if (polish.identifier==Samsung/SGH-U700-ZYB) || (polish.identifier==Samsung/SGH-U800-ZYB) || (polish.identifier==Samsung/SGH-U900-ZYB) || (polish.identifier==Samsung/SGH-F330-ZYB) || (polish.identifier==Samsung/SGH-F400-ZYB)
            String[] samsungNames = new String[3];
            System.arraycopy(names, 0, samsungNames, 0, names.length);
            samsungNames[Contact.NAME_OTHER] = EMPTY_STRING;
            names = samsungNames;
            //#endif  
            
            if (null != (value = names[Contact.NAME_FAMILY])
                    && !EMPTY_STRING.equals(value))
                checksum = CRC32.update(value, checksum);

            if (null != (value = names[Contact.NAME_GIVEN])
                    && !EMPTY_STRING.equals(value))
                checksum = CRC32.update(value, checksum);

            if (null != (value = names[Contact.NAME_OTHER])
                    && !EMPTY_STRING.equals(value))
                checksum = CRC32.update(value, checksum);
        }
        //#endif

        //#if polish.pim.support.TITLE
        //#debug debug
        System.out.println("TITLE");

        if (contact.countValues(Contact.TITLE) > 0)
            checksum = CRC32.update(contact.getString(Contact.TITLE, 0), checksum);
        //#endif

        //#if polish.pim.support.ORG
        //#debug debug
        System.out.println("ORG");

        if (contact.countValues(Contact.ORG) > 0)
            checksum = CRC32.update(contact.getString(Contact.ORG, 0), checksum);
        //#endif

        //#if polish.pim.support.URL
        //#debug debug
        System.out.println("URL");

        if (contact.countValues(Contact.URL) > 0)
            checksum = CRC32.update(contact.getString(Contact.URL, 0), checksum);
        //#endif

        //#if polish.pim.support.BIRTHDAY:defined && polish.pim.support.BIRTHDAY
        //#debug debug
        System.out.println("BDAY");

        if (contact.countValues(Contact.BIRTHDAY) > 0) {
            Calendar c = Calendar.getInstance();

            synchronized (c) {
                c.setTime(new Date(contact.getDate(Contact.BIRTHDAY, 0)));
                checksum = CRC32.update(c.get(Calendar.DATE), checksum);
                checksum = CRC32.update(c.get(Calendar.MONTH), checksum);
                checksum = CRC32.update(c.get(Calendar.DAY_OF_MONTH), checksum);
            }
        }
        //#endif

        //#if polish.pim.support.NICKNAME:defined
        //#debug debug
        System.out.println("NNAME");

        if (contact.countValues(Contact.NICKNAME) > 0)
            checksum = CRC32.update(contact.getString(Contact.NICKNAME, 0), checksum);
        //#endif

        //#if polish.pim.support.NOTE:defined
        //#debug debug
        System.out.println("NOTE");

        if (contact.countValues(Contact.NOTE) > 0)
            checksum = CRC32.update(contact.getString(Contact.NOTE, 0), checksum);
        //#endif


        //#if polish.pim.support.TEL:defined
        //#debug debug
        System.out.println("TEL");

        checksum = calcCheckSum(Contact.TEL, isTelPrefSupported, telOccMaps, contact, checksum);
        //#endif

        //#if polish.pim.support.EMAIL:defined
        //#debug debug
        System.out.println("EMAIL");

        checksum = calcCheckSum(Contact.EMAIL, isEmailPrefSupported, emailOccMaps, contact, checksum);
        //#endif

        //#if polish.pim.support.ADDRESS:defined
        //#debug debug
        System.out.println("ADDRESS");

        {
            int[] attrs = extractAttributes(Contact.ADDR, contact);

            //#if polish.pim.support.FORMATTED_ADDRESS:defined
            int[] fattrs = extractAttributes(Contact.FORMATTED_ADDR, contact);
            //#endif
            int idx;

            for (int i = 0; i < addrMaps.length; i++) {
                for (int j = 0; j < addrMaps[i].length; j++) {
                    //#if polish.pim.support.FORMATTED_ADDRESS:defined
                    if (0 != (addrMaps[i][j] & NAB_FORMATTED)) {
                        if (-1 != (idx = ArrayUtils.firstIndexOf(addrMaps[i][j] - NAB_FORMATTED, fattrs))
                                && null != (value = contact.getString(Contact.FORMATTED_ADDR, idx))
                                && !EMPTY_STRING.equals(value))
                            checksum = CRC32.update(value, checksum);
                    }
                    else
                        //#endif
                        if (-1 != (idx = ArrayUtils.firstIndexOf(addrMaps[i][j], attrs))) {
                            String[] adr = contact.getStringArray(Contact.ADDR, idx);
                            for (int k = 0; k < adr.length; k++)
                                if (null != adr[k] && !EMPTY_STRING.equals(adr[k]))
                                    checksum = CRC32.update(adr[k], checksum);
                        }
                }
            }
        }
        //#endif

        return checksum;
    }

    private static int calcCheckSum(int field, boolean supportsPref, int[][][] occMap, Contact contact, int checksum)
    {
        int[] attributes = extractAttributes(field, contact);
        String value;
        int oAttr, idx;

        for (int i = 0; i < occMap.length; i++) {
            for (int j = 0; j < occMap[i].length; j++) {
                for (int o = 0, len = occMap[i][j].length; o < len; o++) {
                	oAttr = occMap[i][j][o];
                	
                	try
                	{//for exception IndexOutOfBoundsExceptions from 'contact.getString(field, idx)'
                		
                        if (-1 != (idx = ArrayUtils.indexOf(oAttr, o, attributes))
                                && null != (value = contact.getString(field, idx))
                                && !EMPTY_STRING.equals(value)
                                || (supportsPref && -1 != (idx = ArrayUtils.indexOf((oAttr = oAttr | NAB_PREF), o, attributes))
                                && null != (value = contact.getString(field, idx))
                                && !EMPTY_STRING.equals(value))) {
                            checksum = CRC32.update(value, checksum);
                            checksum = CRC32.update(oAttr, checksum);
                        }                    	
                	}
                	catch(Exception e) {
                		//#debug error
                		System.out.println("Failed to calculate checksum" + e);
                		
                		continue;
                	}
                	
                }
            }
        }

        return checksum;
    }

    public static void overwrite(ContactProfile target, Contact source)
    {
    	//#debug debug
    	System.out.println("Overwrite CAB with NAB");
    	
    	//#debug debug
    	System.out.println("Before: " + target);
    	
//        target.setNabId(source.getString(Contact.UID, 0));

    	//#debug debug
    	System.out.println("Overwrite; target=" + target);

    	//#if (polish.pim.support.NAME:defined && ${lowercase(polish.pim.support.NAME)} == nab_formatted)
        //#= try{
    	//#= String fn;
        //#= if (source.countValues(Contact.FORMATTED_NAME) > 0
        //#=    && null != (fn = source.getString(Contact.FORMATTED_NAME, 0))
        //#=    && !EMPTY_STRING.equals(fn)) {
        //#=    int ios = fn.indexOf(' ');
        //#= target.setName(fn.substring(0, ios), EMPTY_STRING, ios > 0 ? fn.substring(ios + 1) : EMPTY_STRING);
        //#= } else {
        //#=    target.setName(EMPTY_STRING,EMPTY_STRING,EMPTY_STRING);
        //#= }
    	//#= }
        //#= catch (InvalidValueException e) {
        //#=    // TODO
        //#= }
        //#else
        //#debug debug
        System.out.println("Name");

        try {
            if (source.countValues(Contact.NAME) > 0) {
            	
                String[] names = source.getStringArray(Contact.NAME, 0);

                //#if (polish.identifier==Samsung/SGH-U700-ZYB) || (polish.identifier==Samsung/SGH-U800-ZYB) || (polish.identifier==Samsung/SGH-U900-ZYB) || (polish.identifier==Samsung/SGH-F330-ZYB) || (polish.identifier==Samsung/SGH-F400-ZYB)
                String[] samsungNames = new String[3];
                if (names.length > 1) {
                    samsungNames[Contact.NAME_FAMILY] = names[Contact.NAME_FAMILY];
                    samsungNames[Contact.NAME_GIVEN] = names[Contact.NAME_GIVEN];
                } else {
                    samsungNames[Contact.NAME_GIVEN] = names[0];
                    samsungNames[Contact.NAME_FAMILY] = EMPTY_STRING;
                }
            
                samsungNames[Contact.NAME_OTHER] = EMPTY_STRING;
                names = samsungNames;
                //#endif  
                
                //#if (polish.pim.support.NAME.FIRST:undefined || ${lowercase(polish.pim.support.NAME.FIRST)} != true)
                names[Contact.NAME_GIVEN] = target.getFirstName();
                //#endif
                //#if (polish.pim.support.NAME.MIDDLE:undefined || ${lowercase(polish.pim.support.NAME.MIDDLE)} != true)
                names[Contact.NAME_OTHER] = target.getMiddleNames();
                //#endif
                //#if (polish.pim.support.NAME.LAST:undefined || ${lowercase(polish.pim.support.NAME.LAST)} != true)
                names[Contact.NAME_FAMILY] = target.getLastName();
                //#endif                
                target.setName(names[Contact.NAME_GIVEN], names[Contact.NAME_OTHER], names[Contact.NAME_FAMILY]);
            } else {
                //#if (polish.identifier==Samsung/SGH-U700-ZYB) || (polish.identifier==Samsung/SGH-U800-ZYB) || (polish.identifier==Samsung/SGH-U900-ZYB) || (polish.identifier==Samsung/SGH-F330-ZYB) || (polish.identifier==Samsung/SGH-F400-ZYB)
            	String fn;

            	if (source.countValues(Contact.FORMATTED_NAME) > 0
                        && null != (fn = source.getString(Contact.FORMATTED_NAME, 0)) && !EMPTY_STRING.equals(fn)) {
                    int ios = fn.indexOf(' ');
                    if (ios == -1) {
                        target.setName(fn, EMPTY_STRING, EMPTY_STRING);
                    } else {
                        target.setName(fn.substring(0, ios), EMPTY_STRING, ios > 0 ? fn.substring(ios + 1)
                                : EMPTY_STRING);
                    }
                }
            	else {
                    target.setName(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
                }
                //#else
                target.setName(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
                //#endif
            }
        }
        catch (InvalidValueException e) {
            //#debug error
            System.out.println("caught InvalidValueException");
        }
        //#endif

        //#if (polish.pim.support.TITLE:defined || polish.pim.support.ORG:defined)
        //#debug debug
        System.out.println("WorkDetails");

        String[] wd;

        if (null == (wd = target.getWorkDetails())
                || wd.length < 3) {
            wd = new String[3];

            //#debug debug
            System.out.println("ContactProfile.getWorkDetails() returned Crappy WorkDetails!");
        }
        //#endif

        //#if polish.pim.support.TITLE
        //#debug debug
        System.out.println("TITLE");

        if (source.countValues(Contact.TITLE) > 0) {
            wd[WORK_TITLE] = source.getString(Contact.TITLE, 0);
        }
        else {
            wd[WORK_TITLE] = EMPTY_STRING;
        }
        //#endif

        //#if polish.pim.support.ORG
        //#debug debug
        System.out.println("ORG");

        if (source.countValues(Contact.ORG) > 0) {
            wd[WORK_COMPANY] = source.getString(Contact.ORG, 0);
        } else {
            wd[WORK_COMPANY] = EMPTY_STRING;
        }
        //#endif

        //#if (polish.pim.support.TITLE:defined || polish.pim.support.ORG:defined)
        target.setWorkDetails(wd);
        //#endif

        //#if polish.pim.support.URL
        //#debug debug
        System.out.println("URL");

        if (source.countValues(Contact.URL) > 0) {
            Identity idy = Identity.createUrl(source.getString(Contact.URL, 0));
            target.setUrl(idy);

            //#debug debug
            System.out.println("set " + idy);
        }
        else {
            //#debug debug
            System.out.println("rem");

            target.setUrl(null);
        }
        //#endif

        //#if polish.pim.support.BIRTHDAY:defined && polish.pim.support.BIRTHDAY
        //#debug debug
        System.out.println("BIRTHDAY");

        if (source.countValues(Contact.BIRTHDAY) > 0) {
            target.setDateOfBirth(new Date(source.getDate(Contact.BIRTHDAY, 0)));
        }
        else {
            target.setDateOfBirth(null);
        }
        //#endif

        String value;
        //#if polish.pim.support.NICKNAME:defined
        //#debug debug
        System.out.println("NICKNAME");

        if (source.countValues(Contact.NICKNAME) > 0
                && null != (value = source.getString(Contact.NICKNAME, 0))) {
            target.setNickname(value);
        }
        else {
            target.setNickname(EMPTY_STRING);
        }
        //#endif

        //#if polish.pim.support.NOTE:defined
        //#debug debug
        System.out.println("NOTE");

        if (source.countValues(Contact.NOTE) > 0
                && null != (value = source.getString(Contact.NOTE, 0))) {
            target.setNote(value);
        }
        else {
            target.setNote(EMPTY_STRING);
        }
        //#endif

        //#if polish.pim.support.TEL:defined
        //#debug debug
        System.out.println("TEL");

        populateProfileIdentity(Contact.TEL, isTelPrefSupported, Identity.TYPE_PHONE, telOccMaps, telSubtypes, source, target);
        //#endif

        //#if polish.pim.support.EMAIL:defined
        //#debug debug
        System.out.println("EMAIL");

        populateProfileIdentity(Contact.EMAIL, isEmailPrefSupported, Identity.TYPE_EMAIL, emailOccMaps, emailSubtypes, source, target);
        //#endif
        //#if polish.pim.support.ADDRESS:defined
        //#debug debug
        System.out.println("ADDRESS");

        {
            int idx, attr;
            for (int i = 0; i < addrMaps.length; i++) {
                for (int j = 0; j < addrMaps[i].length; j++) {
                    attr = addrMaps[i][j];

                    //#if polish.pim.support.FORMATTED_ADDRESS:defined
                    if (0 != (attr & NAB_FORMATTED)
                            && -1 != (idx = indexOf(Contact.FORMATTED_ADDR, (attr = addrMaps[i][j] - NAB_FORMATTED), source))) {
                        String formattedAddress = source.getString(Contact.FORMATTED_ADDR, idx);
                        String[] address = splitFormattedAddr(formattedAddress);
                        Address addr =
                                Address.createAddress(
                                        addrSubtypes[i],
                                        attr & NAB_FORMATTED,
                                        null,
                                        address[Contact.ADDR_STREET],
                                        address[Contact.ADDR_EXTRA],
                                        address[Contact.ADDR_LOCALITY],
                                        address[Contact.ADDR_POSTALCODE],
                                        address[Contact.ADDR_REGION],
                                        address[Contact.ADDR_COUNTRY]);
                        target.setAddress(addr);
                    }
                    else
                        //#endif
                        if (-1 != (idx = indexOf(Contact.ADDR, attr, source))) {
                            String[] address = source.getStringArray(Contact.ADDR, idx);
                            Address addr =
                                    Address.createAddress(
                                            addrSubtypes[i],
                                            attr,
                                            null,
                                            address[Contact.ADDR_STREET],
                                            address[Contact.ADDR_EXTRA],
                                            address[Contact.ADDR_LOCALITY],
                                            address[Contact.ADDR_POSTALCODE],
                                            address[Contact.ADDR_REGION],
                                            address[Contact.ADDR_COUNTRY]);
                            target.setAddress(addr);
                        }
                        else {
                            target.removeAddress(addrMaps[i][j]);
                        }
                }
            }
        }
        //#endif
        
        //code to retrieve the NAB avatar
        /* byte[] data = null;
        if (source.countValues(Contact.PHOTO) > 0) {
           byte[] photoEncoded = source.getBinary(Contact.PHOTO, 0);
           try {
        	   data = Base64InputStream.decode(photoEncoded, 0, photoEncoded.length);
			} catch (IOException e) {
				 //#debug error
				System.out.println("error decoding contact photo for " + source + " : " + e);
				e.printStackTrace();
			}
        }*/
        
        //#debug debug
        System.out.println("After : " + target);
    }

    private static void populateProfileIdentity(int field, boolean supportsPref, int type, int[][][] occMap, int[] subtypes, Contact source, ContactProfile target)
    {
        int oAttr;
        String value;

        for (int i = 0; i < occMap.length; i++) {
            for (int j = 0; j < occMap[i].length; j++) {
                int idx;

                for (int o = 0, oLen = occMap[i][j].length; o < oLen; o++) {
                    oAttr = occMap[i][j][o];

                    if (-1 != (idx = indexOf(field, oAttr, source)) && (null != (value = source.getString(field, idx)) && !EMPTY_STRING.equals(value.trim()))
                            ||
                            (supportsPref
                                    && -1 != (idx = indexOf(field, oAttr | NAB_PREF, source)) && !EMPTY_STRING.equals((value = source.getString(field, idx))))) {
                        //#debug debug
                        System.out.println("setIdentity:" + oAttr + "=" + value);
                        Identity idy = createId(type, subtypes[i], oAttr, value);
                        if (idy != null) {
                            target.setIdentity(idy);
                        }
                        else {
                            //#debug warn
                            System.out.println("Unable to create identity; Invalid value?");
                        }
                    }
                    else {
                        target.removeIdentity(type, oAttr);
                    }
                }
            }
        }
    }

    private static Identity createId(int type, int subtype, int nabAttr, String value)
    {
        switch (type) {
            case Identity.TYPE_PHONE:
                return Identity.createPhoneNumber(subtype, nabAttr, value, false); // TODO preferred

            case Identity.TYPE_EMAIL:
                Identity id = null;

                try {
                    id = Identity.createEmail(subtype, nabAttr, value, false); // TODO preferred
                }
                catch (InvalidValueException e) {
                    //#debug debug
                    System.out.println("Unable to create e-mail : " + e);
                    // TODO
                }

                return id;

            default:
                //#debug debug
                System.out.println("Invalid Identity type");
                throw new IllegalArgumentException("Invalid type.");
        }
    }

    //Todo: remove this methods when "\r\n" works for LINE_SEPARATOR
    private static String[] splitFormattedAddr(String original) {
        String[] address = new String[]{EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING,
                EMPTY_STRING};

        //split using LINE_SEPARATOR to get the street and country
        String[] split = split(original, LINE_SEPARATOR);
        address[Contact.ADDR_STREET] = split[0];
        address[Contact.ADDR_REGION] = split[2];
        address[Contact.ADDR_POSTALCODE] = split[3];
        address[Contact.ADDR_COUNTRY] = split[4];

        String nextString = split[1];

        //split using COMMA_SEPARATOR to get the locality
        split = split(nextString, COMMA_SEPARATOR);
        address[Contact.ADDR_LOCALITY] = split[0];

        return address;
    }

    //Todo: remove this methods when "\r\n" works for LINE_SEPARATOR
    private static String[] split(String original, String separator)
    {
        String[] res = new String[5];
        int index = original.indexOf(separator);
        int counter = 0;

        while (index >= 0) {
            res[counter] = original.substring(0, index);
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
            counter++;
        }

        // Get the last node
        res[counter] = original;
        return res;
    }

    public static void overwrite(Contact target, ContactProfile source)
    {
    	//#debug debug
        System.out.println("Overwrite NAB with CAB");
        
        //#debug debug
        System.out.println("Before\n" + toVcard(target));

        //#if (polish.pim.support.NAME:defined && ${lowercase(polish.pim.support.NAME)} == nab_formatted)
        //#debug debug
        //#= System.out.println("FORMATTED_NAME");        
        //#= StringBuffer fn = new StringBuffer();
        //#= fn.append(source.getLastName()).append(" ");
        //#= fn.append(source.getFirstName());
        //#= if (target.countValues(Contact.FORMATTED_NAME) > 0)
        //#=     target.setString(Contact.FORMATTED_NAME, 0, Contact.ATTR_NONE, fn.toString());
        //#=  else
        //#=     target.addString(Contact.FORMATTED_NAME, Contact.ATTR_NONE, fn.toString());
        //#=
        //#else
        //#debug debug
        System.out.println("Name");
       
        //#if (polish.identifier==Samsung/SGH-U700-ZYB)
        //#debug debug
        //# System.out.println("Name");
        //#= String frstName = source.getFirstName();
        //#= String lstName = source.getLastName();
        //#= if(source.getFirstName()==null || "".equals(source.getFirstName())) {
        //#=  frstName = ONE_SPACE;
        //#= }
        //#=  if(source.getLastName()==null || "".equals(source.getLastName())) {
        //#=	 lstName = ONE_SPACE;
        //#=  }
        //#=  String[] name = new String[]{
        //#=  frstName,
        //#=  lstName
        //#=  };
        //#= StringBuffer fn = new StringBuffer();
        //#= fn.append(source.getFirstName()).append(" ");
        //#= fn.append(source.getLastName());
        //#= if (target.countValues(Contact.FORMATTED_NAME) > 0)
        //#=     target.setString(Contact.FORMATTED_NAME, 0, Contact.ATTR_NONE, fn.toString());
        //#=  else
        //#=     target.addString(Contact.FORMATTED_NAME, Contact.ATTR_NONE, fn.toString());
        //#elif polish.group.S60-ZYB
        //#= String[] name = new String[]{
        //#=      EMPTY_STRING,
        //#=      source.getFirstName(),
        //#=      EMPTY_STRING,
        //#=      source.getMiddleNames(),
        //#=      source.getLastName(),
        //#= 	  EMPTY_STRING,
        //#=      EMPTY_STRING
        //#=  };
        //#else
        
        String[] name = new String[]{
                source.getLastName(),
                source.getFirstName(),
                source.getMiddleNames(),
                EMPTY_STRING,
                EMPTY_STRING
        };
        
        //#endif

        if (target.countValues(Contact.NAME) > 0) {
        	target.removeValue(Contact.NAME, 0);
        }

        target.addStringArray(Contact.NAME, Contact.ATTR_NONE, name);
        //#endif

        //#debug debug
        System.out.println("After name change\n" + toVcard(target));
        
        //#if (polish.pim.support.TITLE:defined || polish.pim.support.ORG:defined)
        //#debug debug
        System.out.println("WorkDetails");

        String[] wd = source.getWorkDetails();

        if (wd == null || wd.length < 3) {
            //#debug debug
            System.out.println("RemoveWorkDetails");

            //#if polish.pim.support.TITLE
            if (target.countValues(Contact.TITLE) > 0)
                target.removeValue(Contact.TITLE, 0);
            //#endif

            //#if polish.pim.support.ORG
            if (target.countValues(Contact.ORG) > 0)
                target.removeValue(Contact.ORG, 0);
            //#endif
        }
        else {
            //#endif
            //#if polish.pim.support.TITLE
            //#debug debug
            System.out.println("TITLE");

            if (EMPTY_STRING.equals(wd[WORK_TITLE])) {
                if (target.countValues(Contact.TITLE) > 0) {
                    target.removeValue(Contact.TITLE, 0);
                }
            }
            else {
                if (target.countValues(Contact.TITLE) > 0) {
                    target.setString(Contact.TITLE, 0, Contact.ATTR_NONE, wd[WORK_TITLE]);
                } else {
                    target.addString(Contact.TITLE, Contact.ATTR_NONE, wd[WORK_TITLE]);
                }
            }
            //#endif

            //#if polish.pim.support.ORG
            //#debug debug
            System.out.println("ORG");

            if (EMPTY_STRING.equals(wd[WORK_COMPANY])) {
                if (target.countValues(Contact.ORG) > 0) {
                    target.removeValue(Contact.ORG, 0);
                }
            }
            else {
                if (target.countValues(Contact.ORG) > 0) {
                    target.setString(Contact.ORG, 0, Contact.ATTR_NONE, wd[WORK_COMPANY]);
                } else {
                    target.addString(Contact.ORG, Contact.ATTR_NONE, wd[WORK_COMPANY]);
                }
            }
            //#endif
            //#if (polish.pim.support.TITLE:defined || polish.pim.support.ORG:defined)
        }
        //#endif

        //#if polish.pim.support.URL
        {
            //#debug debug
            System.out.println("URL");

            Identity url = source.getUrl();
            String val = url != null ? url.getName() : null;

            if (null == url || EMPTY_STRING.equals(url.getName())) {
                if (target.countValues(Contact.URL) > 0) {
                    target.removeValue(Contact.URL, 0);
                }
            }
            else {
                if (target.countValues(Contact.URL) > 0) {
                    target.setString(Contact.URL, 0, Contact.ATTR_NONE, val);
                }
                else {
                    target.addString(Contact.URL, Contact.ATTR_NONE, val);
                }
            }
        }
        //#endif

        //#if polish.pim.support.BIRTHDAY:defined && polish.pim.support.BIRTHDAY
        {
        	//#message Support for BIRTHDAY enabled
        	
            //#debug debug
            System.out.println("BDAY");
            Date bday = source.getDateOfBirth();
            long lbday = bday != null ? bday.getTime() : -1;

            if (target.countValues(Contact.BIRTHDAY) > 0) {
                if (lbday < 0)
                    target.removeValue(Contact.BIRTHDAY, 0);
                else
                    target.setDate(Contact.BIRTHDAY, 0, Contact.ATTR_NONE, lbday);
            }
            else if (0 < lbday) {
                target.addDate(Contact.BIRTHDAY, Contact.ATTR_NONE, lbday);
            }
        }
        //#endif

        //#if polish.pim.support.NICKNAME:defined
        {
            //#debug debug
            System.out.println("NICK");
            String nick = source.getNickname();
            if (EMPTY_STRING.equals(nick)) {
                if (target.countValues(Contact.NICKNAME) > 0) {
                    target.removeValue(Contact.NICKNAME, 0);
                }
            }
            else {
                if (target.countValues(Contact.NICKNAME) > 0) {
                    target.setString(Contact.NICKNAME, 0, Contact.ATTR_NONE, nick);
                }
                else {
                    target.addString(Contact.NICKNAME, Contact.ATTR_NONE, nick);
                }
            }
        }
        //#endif

        //#if polish.pim.support.NOTE:defined
        {
            //#debug debug
            System.out.println("NOTE");
            Note note = source.getNote();

            if ((note == null) || EMPTY_STRING.equals(note.getContent())) {
                if (target.countValues(Contact.NOTE) > 0) {
                    target.removeValue(Contact.NOTE, 0);
                }
            }
            else {
                if (target.countValues(Contact.NOTE) > 0) {
                    target.setString(Contact.NOTE, 0, Contact.ATTR_NONE, note.getContent());
                }
                else {
                    target.addString(Contact.NOTE, Contact.ATTR_NONE, note.getContent());
                }
            }
        }
        //#endif

        //#if polish.pim.support.TEL:defined
        //#debug debug
        System.out.println("TEL");

        populateContactStringField(Contact.TEL, isTelPrefSupported, Identity.TYPE_PHONE, Channel.TYPE_CALL, maxTelCount, telOccMaps, target, source);
        //#endif

        //#if polish.pim.support.EMAIL:defined
        //#debug debug
        System.out.println("EMAIL");

        populateContactStringField(Contact.EMAIL, isEmailPrefSupported, Identity.TYPE_EMAIL, Channel.TYPE_EMAIL, maxEmailCount, emailOccMaps, target, source);
        //#endif

        //#if polish.pim.support.ADDRESS:defined
        //#debug debug
        System.out.println("ADDR");

        {
            int idx;
            int attr;
            Address addr;

            for (int i = 0; i < addrMaps.length; i++) {
                for (int j = 0; j < addrMaps[i].length; j++) {
                    attr = addrMaps[i][j];

                    //#if polish.pim.support.FORMATTED_ADDRESS:defined
                    if (0 != (addrMaps[i][j] & NAB_FORMATTED)) {
                        if (null != (addr = source.getAddress(attr))) {
                            StringBuffer addrBuf = new StringBuffer();
                            addrBuf.append(addr.getStreet()).append(LINE_SEPARATOR).
                                    append(addr.getTown()).append(COMMA_SEPARATOR + " ").
                                    append(addr.getRegion()).append(SPACE_SEPARATOR).
                                    append(addr.getPostcode()).append(LINE_SEPARATOR).
                                    append(addr.getCountry());
                            if (-1 != (idx = indexOf(Contact.FORMATTED_ADDR, (attr = addrMaps[i][j] - NAB_FORMATTED), target)))
                                target.setString(Contact.FORMATTED_ADDR, idx, attr, addrBuf.toString());
                            else
                                target.addString(Contact.FORMATTED_ADDR, attr, addrBuf.toString());
                        }
                        else {
                            if (-1 == (idx = indexOf(Contact.FORMATTED_ADDR, (addrMaps[i][j] - NAB_FORMATTED), target)))
                                target.removeValue(Contact.FORMATTED_ADDR, idx);
                        }
                    } else
                    //#endif
                    {
                        if (null != (addr = source.getAddress(attr))) {
                            
                            //#if (${lowercase(polish.vendor)}==samsung)
                            //#= String[] nAddr = new String[]{
                            //#=        ONE_SPACE,
                            //#=        addr.getStreet2(),
                            //#=        addr.getStreet1(),
                            //#=        addr.getTown(),
                            //#=        addr.getRegion(),
                            //#=        addr.getPostcode(),
                            //#=        addr.getCountry()
                            //#= };
                            
                        	//#elif (${lowercase(polish.vendor)}==nokia)
                            //#= String[] nAddr = new String[]{
                            //#=        null,
                            //#=        addr.getStreet2(),
                            //#=        addr.getStreet1(),
                            //#=        addr.getTown(),
                            //#=        addr.getRegion(),
                            //#=        addr.getPostcode(),
                            //#=        addr.getCountry()
                            //#= };
                          
                        	
                        	//#else
                            String[] nAddr = new String[]{
                                    EMPTY_STRING,
                                    addr.getStreet2(),
                                    addr.getStreet1(),
                                    addr.getTown(),
                                    addr.getRegion(),
                                    addr.getPostcode(),
                                    addr.getCountry()
                            };
                            //#endif
                            
                            if (-1 != (idx = indexOf(Contact.ADDR, attr, target))) {
                                target.setStringArray(Contact.ADDR,
                                        idx,
                                        attr,
                                        nAddr);
                            }
                            else {
                                target.addStringArray(Contact.ADDR,
                                        attr,
                                        nAddr);
                            }
                        }
                        else {
                            if (-1 != (idx = indexOf(Contact.ADDR, attr, target)))
                                target.removeValue(Contact.ADDR, idx);
                        }
                    }
                }
            }
        }
        //#endif
        
        //#debug debug
        System.out.println("After\n" + toVcard(target));
    }

    private static String toVcard(Contact aContact)
    {
        PIM pim = PIM.getInstance();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            pim.toSerialFormat(aContact, baos, null, pim.supportedSerialFormats(PIM.CONTACT_LIST)[0]);
        }
        catch (PIMException e) {        	
            //#debug error
            System.out.println("Failed to serialise contact to vcard" + e);
        }
        catch (UnsupportedEncodingException e) {
            //#debug error
        	System.out.println("Failed to serialise contact to vcard" + e);
        }

        return new String(baos.toByteArray());
    }
    
    private static void populateContactStringField(int field, boolean supportsPref, int type, int channelType, int maxVals, int[][][] occMap, Contact contact, ContactProfile profile)
    {
        //#debug debug
        System.out.println("Existing Attributes:" + ArrayUtils.toString(extractAttributes(field, contact)));

        try {
            int idx, oAttr;
            Identity idy;
            /* Clean-up; Remove all mapped */
            for (int i = 0; i < occMap.length; i++) {
                for (int j = 0; j < occMap[i].length; j++) {
                    for (int o = occMap[i][j].length - 1; o > -1; o--) {
                    	oAttr = occMap[i][j][o];

                    	if (-1 != (idx = indexOf(field, oAttr, contact)) ||
                                (supportsPref && (-1 != (idx = indexOf(field, (oAttr = oAttr | NAB_PREF), contact))))) {
                            //#debug debug
                            System.out.println("Removing " + idx + " : " + contact.getString(field, idx));
                            //#if polish.pim.bug.REMOVE_STRING_VALUE:defined
                            // Overwrite with empty string before using removeValue()
                            contact.setString(field, idx, oAttr, EMPTY_STRING);
                            //#endif
                            
                            //#if polish.pim.bug.EMAIL_ACCESS:defined
                            //#= if (Contact.EMAIL == field)
                            //#=    contact.setString(field, idx, oAttr, ONE_SPACE);
                            //#= else
                            //#=    contact.removeValue(field, idx);
                            //#else
                            contact.removeValue(field, idx);
                            //#endif
                        } // else it's not existing anyway
                    }
                }
            }

            /* SE K850i is the one to blame for this! */
            int howMany = contact.countValues(field);

            if (howMany > maxVals) {
                //#debug debug
                System.out.println("Field has more values than max supported values!" + howMany + " > " + maxVals);
                howMany = maxVals;
            }

            /* Set */
            for (int i = 0; i < occMap.length; i++) {
                for (int j = 0; j < occMap[i].length; j++) {
                    for (int o = 0; o < occMap[i][j].length; o++) {
                        oAttr = occMap[i][j][o];

                        //#debug debug
                        System.out.println("attrMap[" + o + "]=" + oAttr);

                        if (null != (idy = profile.getIdentity(type, oAttr))) {
                            //#debug debug
                            System.out.println("getIdentity( " + oAttr + ") : " + idy.getName());
                            if (howMany < maxVals) {
                                howMany++;
                                //#debug debug
                                System.out.println("AddingNew=" + idy.getName());
                                
                                insertContactStringField(contact, field, oAttr, idy.getName());
                            }
                            else {
                                //#debug debug
                                System.out.println("Field full " + howMany + " >= " + maxVals);

                                break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            //#debug error
            System.out.println("ERROR " + e.getMessage() + " " + e);
        }
    }

    private static void insertContactStringField(Contact contact, int field, int attr, String value)
    {
    	int countValues = contact.countValues(field);

    	for (int i = 0; i < countValues; i++) {
    		if (contact.getAttributes(field, i) == attr) {
    			String v = contact.getString(field, i);
    			contact.setString(field, i, attr, value);
    			value = v;
    		}
    	}

    	contact.addString(field, attr, value);
    }
    
    private static int indexOf(int field, int attr, Contact contact)
    {
    	int countValues = contact.countValues(field);

    	for (int i = 0; i < countValues; i++) {
            if (contact.getAttributes(field, i) == attr)
                return i;
        }

    	return -1;
    }


    public static int[] extractAttributes(int field, Contact contact)
    {
        int[] ret = new int[contact.countValues(field)];

        for (int i = 0; i < ret.length; i++)
            ret[i] = contact.getAttributes(field, i);

        return ret;
    }

    public static String getShortDescription(Contact aContact)
    {
        String desc = EMPTY_STRING;

        //#if (polish.pim.support.NAME:defined && ${lowercase(polish.pim.support.NAME)} == nab_formatted)
        if (aContact.countValues(Contact.FORMATTED_NAME) > 0) {
            desc = aContact.getString(Contact.FORMATTED_NAME, 0);
        }
        //#else
        if (aContact.countValues(Contact.NAME) > 0) {
            String[] names = aContact.getStringArray(Contact.NAME, 0);
            
            //#if (polish.identifier==Samsung/SGH-U700-ZYB) || (polish.identifier==Samsung/SGH-U800-ZYB) || (polish.identifier==Samsung/SGH-U900-ZYB) || (polish.identifier==Samsung/SGH-F330-ZYB) || (polish.identifier==Samsung/SGH-F400-ZYB)
            String[] samsungNames = new String[3];
            System.arraycopy(names, 0, samsungNames, 0, names.length);
            samsungNames[Contact.NAME_OTHER] = EMPTY_STRING;
            names = samsungNames;
            //#endif 
            
            desc = new StringBuffer(names[Contact.NAME_GIVEN] != null ? names[Contact.NAME_GIVEN] : EMPTY_STRING).append(" ").
                    append(names[Contact.NAME_OTHER] != null ? names[Contact.NAME_OTHER] : EMPTY_STRING).append(" ").
                    append(names[Contact.NAME_FAMILY] != null ? names[Contact.NAME_FAMILY] : EMPTY_STRING).append(" ").toString().trim();
        }
        //#endif
        
        //#if (polish.identifier!=Huawei/V735-ZYB) && (polish.identifier!=Huawei/V835-ZYB) && (polish.identifier!=Huawei/V810-ZYB)
        if (EMPTY_STRING.equals(desc))
            desc = "NAB " + aContact.getString(Contact.UID, 0);
        //#endif
        
        //#debug debug
        System.out.println("MOBICA log: contact decription: "+desc);

        return desc;
    }

    public static boolean remap(ContactProfile profile)
    {
        boolean ret = false;

        //#if polish.pim.support.TEL:defined
        //#debug debug
        System.out.println("Remapping TEL");

        ret = remap_identity(telOccMaps, Identity.TYPE_PHONE, telSubtypes, profile) || ret;
        //#endif

        //#if polish.pim.support.EMAIL:defined
        //#debug debug
        System.out.println("Remapping EMAIL");

        ret = remap_identity(emailOccMaps, Identity.TYPE_EMAIL, emailSubtypes, profile) || ret;
        //#endif

        //#if polish.pim.support.ADDRESS:defined
        //#debug debug
        System.out.println("Remapping ADDRESS");

        int[][] cmap = new int[addrMaps.length][];

        for (int i = 0; i < cmap.length; i++) {
            cmap[i] = new int[addrMaps[i].length];
            System.arraycopy(addrMaps[i], 0, cmap[i], 0, cmap[i].length);
        }

        Address[] addresses = profile.getAddresses();

        for (int j = 0; j < addresses.length; j++) {
            Address addr = addresses[j];
            if (-1 != addr.getNabSubtypes()) {
                mark_existing(cmap, addr.getNabSubtypes());
            }
        }

        for (int i = 0; i < addrSubtypes.length; i++) {
            for (int j = 0; j < addresses.length; j++) {
                Address addr = addresses[j];

                if (addrSubtypes[i] == addr.getType()) {
                    if (-1 == addr.getNabSubtypes()) {
                        int attr;

                        if (NAB_UNSUPPORTED != (attr = first_map_available(cmap[i]))) {
                            //#debug debug
                            System.out.println("first_free=" + attr);
                            ret = ret || true;
                            addr.setNabSubtypes(attr);
                        }
                        else
                            break;
                    }
                }
            }
        }
        //#endif

        return ret;
    }

    private static boolean remap_identity(int[][][] occMaps, int cab_type, int[] subtypes, ContactProfile profile)
    {
        boolean ret = false;
        int[][][] cmap = new int[occMaps.length][][];

        for (int i = 0; i < cmap.length; i++) {
            cmap[i] = new int[occMaps[i].length][];

            for (int j = 0; j < cmap[i].length; j++) {
                cmap[i][j] = new int[occMaps[i][j].length];
                System.arraycopy(occMaps[i][j], 0, cmap[i][j], 0, cmap[i][j].length);
            }
        }

        Identity[] identities = profile.getIdentities(cab_type);

        //#debug debug
        System.out.println("Found " + ArrayUtils.toString(identities) + " for type " + cab_type);

        for (int j = 0; j < identities.length; j++) {
            Identity identity = identities[j];
            if (-1 != identity.getNabSubtypes()) {
                //#debug debug
                System.out.println("Existing Map:" + identity.getNabSubtypes() + "=" + identity.getName() + " " + identity.getSubtype());
                mark_existing(cmap, identity.getNabSubtypes());
            }
            else {
                for (int i = 0; i < subtypes.length; i++) {
                    if (subtypes[i] == identity.getSubtype()) {
                        if (-1 == identity.getNabSubtypes()) {
                            int attr;

                            if (NAB_UNSUPPORTED != (attr = first_map_available(cmap[i]))) {
                                ret = ret || true;
                                identity.setNabSubtypes(attr);
                                mark_existing(cmap, attr);

                                //#debug debug
                                System.out.println("New Map:" + identity.getNabSubtypes() + "=" + identity.getName() + " " + identity.getSubtype());
                            }
                            else {
                                //#debug debug
                                System.out.println("All occurencies mapped.");

                                break;
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    private static int first_map_available(int[] cmap) {
        int ret = NAB_UNSUPPORTED;

        for (int j = 0, len = cmap.length;
             j < len && NAB_UNSUPPORTED == (ret = cmap[j]);
             j++)
            ;
        return ret;
    }


    private static int first_map_available(int[][] cmap)
    {
        int ret = NAB_UNSUPPORTED;

        for (int j = 0, len = cmap.length;
             j < len && NAB_UNSUPPORTED == (ret = first_map_available(cmap[j]));
             j++)
            ;

        return ret;
    }

    private static void mark_existing(int[][] cmap, int nabSubtypes)
    {
        for (int i = 0; i < cmap.length; i++)
            for (int j = 0; j < cmap[i].length; j++)
                if (nabSubtypes == cmap[i][j]) {
                    cmap[i][j] = NAB_UNSUPPORTED;
                }
    }

    private static void mark_existing(int[][][] cmap, int nabSubtypes)
    {
        for (int i = 0; i < cmap.length; i++)
            for (int j = 0; j < cmap[i].length; j++)
                for (int k = 0; k < cmap[i][j].length; k++)
                    if (cmap[i][j][k] == nabSubtypes)
                        cmap[i][j][k] = NAB_UNSUPPORTED;
    }
}
