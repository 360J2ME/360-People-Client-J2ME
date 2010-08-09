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

package com.zyb.util;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * @author Jens Vesti
 */
public class ImageUtil {

    public static Image createThumbnail(Image image, int width, int height) {
        final int sourceWidth = image.getWidth();
        final int sourceHeight = image.getHeight();

        if (width == -1)
            width = (int) (image.getWidth() * ((float) height / (float) sourceHeight));

        Image thumb = Image.createImage(width, height);
        Graphics g = thumb.getGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                g.setClip(x, y, 1, 1);
                int dx = x * sourceWidth / width;
                int dy = y * sourceHeight / height;
                g.drawImage(image, x - dx, y - dy, Graphics.LEFT | Graphics.TOP);
            }
        }

        Image thumbImage = Image.createImage(thumb);

        return thumbImage;
    }


    public static int[] getRGB(Image img) {
        int[] rgbArr = new int[img.getHeight() * img.getWidth()];
        img.getRGB(rgbArr, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        return rgbArr;
    }
}
