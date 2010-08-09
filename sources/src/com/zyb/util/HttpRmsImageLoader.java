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
/* //condition false polish.classes.ImageLoader == "com.zyb.util.HttpRmsImageLoader" */

//#condition false 
package com.zyb.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * Implementation of polish.classes.ImageLoader.
 * 
 * It downloads the image from the server instead of fetching it from the JAR.
 * It also has a capability to cache images in rms (define polish.device.supports.imageloader.rms).
 * 
 * @author marek.defecinski@mobica.com
 * @author tomasz.szer@mobica.com
 */
public class HttpRmsImageLoader {
	
	private static final String BASE_URL = 
	//#if polish.device.imageloader.url:defined
	//#= ${polish.device.imageloader.url};
	//#else
	"http://boat.mobica.pl/tosz/people";
	//#endif
	
	private static final String RMS_NAME =
	//#if polish.device.imageloader.rmsname:defined
	//#= ${polish.device.imageloader.rmsname};	
	//#else
		"images_rs";
	//#endif

	//in case image cannot be loaded this will be used
	private static Image FALLBACK_IMAGE = Image.createImage(2, 2);
	
	static {
		FALLBACK_IMAGE.getGraphics().setColor(0xff0000);
		FALLBACK_IMAGE.getGraphics().fillRect(0, 0, 1, 1);
	}

	private static boolean isInitialized;

	//maps urls to record id
	private static Hashtable url2RecordId = new Hashtable();

	/**
	 * Helper methods to close record store, stream & connection without causing an exception 
	 *
	 */
	private static void closeWithExceptionSuppression(
			final Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void closeWithExceptionSuppression(
			final InputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void closeWithExceptionSuppression(
			final OutputStream outputStream) {
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void closeWithExceptionSuppression(final RecordStore rs) {
		if (rs != null) {
			try {
				rs.closeRecordStore();
			} catch (final RecordStoreNotOpenException e) {
				e.printStackTrace();
			} catch (final RecordStoreException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Inits the map of url and record id of already stored image
	 */
	private synchronized static void init() {
		//#if polish.device.imageloader.usesrms == true
		
		//#debug debug
		System.out.println("HttpRmsImageLoader.init");
		RecordStore recordStore = null;
		try {
			recordStore = RecordStore.openRecordStore(RMS_NAME, true);
			final RecordEnumeration re = recordStore.enumerateRecords(null,
					null, false);

			while (re.hasNextElement()) {
				final int recordId = re.nextRecordId();

				final byte[] bytes = recordStore.getRecord(recordId);

				ByteArrayInputStream byteArrayInputStream = null;
				DataInputStream dataInputStream = null;

				try {
					byteArrayInputStream = new ByteArrayInputStream(bytes);
					dataInputStream = new DataInputStream(byteArrayInputStream);

					final String url = dataInputStream.readUTF();

					url2RecordId.put(url, new Integer(recordId));
				} catch (final Exception e) {
					e.printStackTrace();
				} finally {
					closeWithExceptionSuppression(dataInputStream);

					closeWithExceptionSuppression(byteArrayInputStream);
				}
			}
		} catch (final RecordStoreNotOpenException e) {
			e.printStackTrace();
		} catch (final InvalidRecordIDException e) {
			e.printStackTrace();
		} catch (final RecordStoreException e) {
			e.printStackTrace();
		} finally {
			closeWithExceptionSuppression(recordStore);
		}
		//#endif
		isInitialized = true;
	}

	/**
	 * Loads image from rms based on id
	 * 
	 * @param intValue
	 * @return
	 */
	private static synchronized Image loadFromRms(final int intValue) {
		//#debug debug
		System.out.println("HttpRmsImageLoader.loadFromRms " + intValue);
		Image returned = null;
		RecordStore recordStore = null;
		ByteArrayInputStream byteOutputStream = null;
		DataInputStream dataOutputStream = null;
		try {
			recordStore = RecordStore.openRecordStore(RMS_NAME, false);
			final byte[] bytes = recordStore.getRecord(intValue);

			byteOutputStream = new ByteArrayInputStream(bytes);
			dataOutputStream = new DataInputStream(byteOutputStream);

			dataOutputStream.readUTF();

			returned = Image.createImage(dataOutputStream);
		} catch (final RecordStoreException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			closeWithExceptionSuppression(dataOutputStream);

			closeWithExceptionSuppression(byteOutputStream);

			closeWithExceptionSuppression(recordStore);
		}
		return returned;
	}

	/**
	 * Loads the resource with the given url. Base url is appended.
	 * 
	 * @param url
	 * @return
	 * @throws IOException never but it is required for polish to compile
	 */
	public static Image loadImage(String url) throws IOException {
		if (!isInitialized) {
			init();
		}
		
		url = BASE_URL + url;
		
		//#debug info
		System.out.println("loadImage(\'"+url+"\')");
		
		Image ret = null;

		//#if polish.device.imageloader.usesrms == true
		final Integer id = (Integer) url2RecordId.get(url);
		
		if (id != null) {
			ret = loadFromRms(id.intValue());
		}

		if (ret != null) {
			return ret;
		}
		//#endif

		ret = saveAndReturn(url);

		if (ret != null) {
			return ret;
		}

		return getFallBackImage(url);
	}

	private static Image getFallBackImage(String url) {
		//#debug error
		System.out.println("Failed to get image for " + url);
		return FALLBACK_IMAGE;
	}

	/**
	 * 
	 * 
	 * 
	 * @param url
	 * @return Image that was retrieved
	 */
	private synchronized static Image saveAndReturn(final String url) {
		//#debug debug
		System.out.println("HttpRmsImageLoader.saveAndReturn " + url);

		Image ret = null;

		HttpConnection httpConnection = null;

		DataInputStream httpDataInputStream = null;
		ByteArrayInputStream imageCopyInputStream = null;

		//#if polish.device.imageloader.usesrms == true
		ByteArrayOutputStream rmsByteOutputStream = null;
		DataOutputStream rmsDataOutputStream = null;
		RecordStore recordStoreUrlAndImageData = null;
		//#endif
		ByteArrayOutputStream imageCopyOutputStream = null;

		try {
			httpConnection = (HttpConnection) Connector.open(url, Connector.READ, true);
			
			int responseCode = httpConnection.getResponseCode();
			if(responseCode != HttpConnection.HTTP_OK)
				throw new IOException("Expected HTTP_OK got " + responseCode);

			httpDataInputStream = httpConnection.openDataInputStream();

			//#if polish.device.imageloader.usesrms == true
			rmsByteOutputStream = new ByteArrayOutputStream();
			rmsDataOutputStream = new DataOutputStream(rmsByteOutputStream);
			recordStoreUrlAndImageData = RecordStore.openRecordStore(RMS_NAME,
					true);
			//#endif

			imageCopyOutputStream = new ByteArrayOutputStream();

			
			//#if polish.device.imageloader.usesrms == true
			rmsDataOutputStream.writeUTF(url);
			//#endif

			while (true) {
				int read = -1;
				try {
					read = httpDataInputStream.read();
				} catch (final IOException e) {
					e.printStackTrace();
					read = -1;
				}
				if (read == -1) {
					break;
				}
				// #if polish.device.imageloader.usesrms == true
				rmsByteOutputStream.write(read);
				// #endif
				imageCopyOutputStream.write(read);
			}

			//#if polish.device.imageloader.usesrms == true
			final byte[] bytes = rmsByteOutputStream.toByteArray();
			try {
				url2RecordId.put(url, new Integer(recordStoreUrlAndImageData
						.addRecord(bytes, 0, bytes.length)));
			} catch (RecordStoreException ignore) {
			}
			//#endif
			
			imageCopyInputStream = new ByteArrayInputStream(
					imageCopyOutputStream.toByteArray());

			ret = Image.createImage(imageCopyInputStream);
		} catch (final IOException e) {
			e.printStackTrace();
		//#if polish.device.imageloader.usesrms == true
		} catch (final RecordStoreException e) {
			e.printStackTrace();
		//#endif
		} finally {
			closeWithExceptionSuppression(imageCopyOutputStream);
		
			closeWithExceptionSuppression(imageCopyInputStream);
			
			//#if polish.device.imageloader.usesrms == true
			closeWithExceptionSuppression(rmsDataOutputStream);

			closeWithExceptionSuppression(rmsByteOutputStream);
			
			closeWithExceptionSuppression(recordStoreUrlAndImageData);
			//#endif

			closeWithExceptionSuppression(httpDataInputStream);

			closeWithExceptionSuppression(httpConnection);
		}
		return ret;
	}
}
