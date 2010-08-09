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
package com.zyb.nowplus.presentation.view.providers;

import de.enough.polish.ui.Canvas;

public class VirtualListRange {
	static int FACTOR_BUFFER = 3;

	static int FACTOR_LIMIT = 3;

	int start;

	int end;

	int total;

	final int range;

	int offset;

	final int referenceHeight;

	final int availableHeight;

	int totalHeight;

	int listHeight;

	int maximumY;

	int minimumY;
	
	final int bufferHeight;
	
	final int limitHeight;

	public VirtualListRange(int referenceHeight, int availableHeight,
			int minimumItems) {
		//#debug debug
		System.out.println("initialising range with : referenceHeight : " + referenceHeight + " : availableHeight : " + availableHeight + " : minimumItems : " + minimumItems); 
		
		this.referenceHeight = referenceHeight;
		this.availableHeight = availableHeight;
		
		this.range = getRange(minimumItems);
		
		this.limitHeight = this.range / 5 * this.referenceHeight;
		this.bufferHeight = this.availableHeight * FACTOR_BUFFER;
		
		this.offset = -1;
	}
	
	/**
	 * Sets the minimum and maximum threshold using the given start,
	 * end and total
	 * @param start the start
	 * @param end the end
	 * @param total the total
	 */
	void setRange(int start, int end, int total) {
		//#debug debug
		System.out.println("updating range : start : " + start + " : end : " + end + " : total : " + total);
		
		this.start = start;
		this.end = end;
		this.total = total;

		this.totalHeight = total * this.referenceHeight;
		this.listHeight = (end - start + 1) * this.referenceHeight;

		this.minimumY = (start * this.referenceHeight) + getLimitHeight();
		this.maximumY = ((start * this.referenceHeight) + this.listHeight)
				- getLimitHeight();

		// handle list start / end / overlapping
		if (start == 0) {
			this.minimumY = Integer.MIN_VALUE;
		}

		if (end == total - 1) {
			this.maximumY = Integer.MAX_VALUE;
		}
	}

	/**
	 * Returns true if y is below the minimum threshold
	 * @param y the y offset
	 * @return true if y is below the minimum threshold otherwise false
	 */
	public boolean belowRange(long y) {
		boolean result = y < this.minimumY;
		//#mdebug debug
		if (result) {
			System.out.println("list selection is below range, updating");
		}
		//#enddebug
		return result;
	}

	/**
	 * Returns true if the y offset is over the maximum threshold
	 * @param y the y offset
	 * @return true if y is over the maximum threshold otherwise false
	 */
	public boolean overRange(long y) {
		boolean result = (y + this.availableHeight) > this.maximumY;
		//#mdebug debug
		if (result) {
			System.out.println("list selection is over range, updating");
		}
		//#enddebug
		return result;
	}

	/**
	 * Update the range according to the specified y offset
	 * @param y the y offset
	 * @param direction the direction of the scrolling
	 * @param total the total number of items
	 */
	public void update(long y, int direction, int total) {
		int start = 0;
		int end = 0;
		long startY = 0;
		long endY = 0;

		if (direction == Canvas.DOWN) {
			endY = y + this.availableHeight;
			startY = endY - (this.range * this.referenceHeight);
			endY += getLimitHeight();
		} else {
			startY = y;
			endY = startY + this.range * this.referenceHeight;
			startY -= getLimitHeight();
		}

		start = (int) startY / this.referenceHeight;
		end = (int) endY / this.referenceHeight;

		if (start <= 0) {
			start = 0;
		}

		if (end > this.total - 1) {
			end = this.total - 1;
		}

		setRange(start, end, total);
	}

	int getRange(int minimum) {
		int neededRange = getBufferHeight() / this.referenceHeight;
		
		if (this.availableHeight % this.referenceHeight > 0) {
			neededRange++;
		}

		if (minimum > neededRange) {
			//#debug debug
			System.out.println("setting range to minimum : " + minimum);
			return minimum;
		} else {
			//#debug debug
			System.out.println("setting range : " + neededRange);
			return neededRange;
		}
	}

	public void setVisibleOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getStart() {
		return this.start;
	}

	public int getEnd() {
		return this.end;
	}

	public int getTotal() {
		return this.total;
	}

	public int getRange() {
		return this.range;
	}

	public int getReferenceHeight() {
		return referenceHeight;
	}

	public int getAvailableHeight() {
		return availableHeight;
	}

	public int getTotalHeight() {
		return totalHeight;
	}

	public int getListHeight() {
		return listHeight;
	}

	public int getMinimumY() {
		return maximumY;
	}

	public int getMaximumY() {
		return minimumY;
	}
	
	int getLimitHeight() {
		return this.limitHeight;
	}

	int getBufferHeight() {
		return this.bufferHeight;
	}
}
