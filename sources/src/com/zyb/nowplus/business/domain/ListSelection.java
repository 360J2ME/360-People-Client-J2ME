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
package com.zyb.nowplus.business.domain;

/**
 * Used to store and identify a selection of list
 * @author Andre
 *
 */
public class ListSelection {
	/**
	 * the entries of this selection
	 */
	private final Object[] entries;
	
	/**
	 * the start index within the original list
	 */
	private final int start;
	
	/**
	 * the end index within the original list
	 */
	private final int end;
	
	/**
	 * the total entry count of the original list
	 */
	private final int total;
	
	/**
	 * 
	 */
	private final int size;

	/**
	 * Creates a new ListPart instance
	 * @param entries the entries for this instance
	 * @param start the start index
	 * @param end the end index
	 * @param total the total count
	 */
	public ListSelection(final Object[] entries, final int start, final int end, final int total) 
	{
		if(total > 0) { 
			this.entries = new Object[end - start + 1];
			System.arraycopy(entries, start, this.entries, 0, this.entries.length);
	
			this.start = start;
			this.end = (end > 0) ? end : 0;
			this.total = total;
			this.size = this.entries.length;
		} else {
			this.entries = new Object[0];
			
			this.start = 0;
			this.end = 0;
			this.total = 0;
			this.size = 0;
		}
	}

	/**
	 * Returns the entries
	 * @return the entries
	 */
	public Object[] getEntries() 
	{
		return this.entries;
	}

	/**
	 * Returns the start index
	 * @return the start index
	 */
	public int getStart() 
	{
		return this.start;
	}

	/**
	 * Returns the end index
	 * @return the end index
	 */
	public int getEnd() 
	{
		return this.end;
	}

	/**
	 * Returns the total count
	 * @return the total count
	 */
	public int getTotal() 
	{
		return this.total;
	}
	
	public int size() {
		return this.size;
	}
	
	//#mdebug error
	public String toString()
	{
		return "ListSelection[start=" + start 
			+ ",end=" + end 
			+ ",total=" + total 
			+ "]";
	}
	//#enddebug
}
