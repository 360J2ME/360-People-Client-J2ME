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
package com.zyb.nowplus.presentation.view.backgrounds;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import de.enough.polish.ui.StyleSheet;

/**
 * Class ARGBImage enables basic image processing procedures and drawing routines for int[]
 * image representations. The main purpose of this implementation is to enable transparency 
 * support that is otherwise lost if you create an Image using Image.createImage(width,height).
 * The class follow the same principles as presented by the Graphics object and should hence
 * be easy to use for anybody familiar with J2ME. Using this class will preserve transparency
 * and also enables several other nice Image manipulation routines. 
 * <p>
 * To minimize memory allocation, only the source int[] of an image is kept by an instance of
 * this class. This results in relatively huge memmory allocation when method .getImage() is 
 * called. If getImage() is to be used often, extend this class and maintain an Image class
 * member in extending class.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class ARGBImage {

	//source members
	private int[] argbSource;
	private int imgWidth, imgHeight;

	private int setColor = 0;
	private int transparencyColor = 0;

//	clipping area
	private int clipX,clipY,clipW,clipH;

	public ARGBImage(String imagePath){
		reInit(imagePath);
	}

	public ARGBImage(Image img){
		reInit(img);
	}

	public ARGBImage(int[] argb, int width, int height){
		reInit(argb, width, height);
	}

	public ARGBImage(int width, int height){
		reInit(width, height);
	}

	public void reInit(String imagePath){

		Image img=null;
		try{
			img = Image.createImage(imagePath);
			img =
				//#if polish.classes.ImageLoader:defined
				StyleSheet.getImage(imagePath, null, false);
				//#else
				Image.createImage(imagePath);
				//#endif
			reInit(img);
		}catch(Exception e){
			//#debug error
			System.out.println("ARGBImage: Error loading Image");
		}
	}

	public void reInit(Image img){
		try{
			this.imgWidth = img.getWidth();
			this.imgHeight = img.getHeight();
			this.clipX = 0;
			this.clipY = 0;
			this.clipW = this.imgWidth;
			this.clipH = this.imgHeight;
			argbSource = new int[imgWidth*imgHeight];
			img.getRGB(argbSource, 0, imgWidth, 0, 0, imgWidth, imgHeight);
		}catch(Exception e){
			//#debug error
			System.out.println("ARGBImage: Error fetching rgb values");
		}
	}

	public void reInit(int[] argb, int width, int height){
		this.imgWidth = width;
		this.imgHeight = height;
		this.clipX = 0;
		this.clipY = 0;
		this.clipW = this.imgWidth;
		this.clipH = this.imgHeight;
		this.argbSource = argb;
	}

	public void reInit(int width, int height){
		this.imgWidth = width;
		this.imgHeight = height;
		this.clipX = 0;
		this.clipY = 0;
		this.clipW = this.imgWidth;
		this.clipH = this.imgHeight;
		this.argbSource = new int[imgWidth*imgHeight];
	}

	/**
	 * Sets the clipping area locally on image object.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setClip(int x, int y, int width, int height){

		//is new clip outside source bounds?
		if(width < 0 || height < 0 ||
				x >= imgWidth || y >= imgHeight || (x+width)<0 || (y+height)<0){
			return;
		}

//		should clipping area be cropped?
		if(x < 0){
			width += x;
			x = 0;
		}
		if(y < 0){
			height += y;
			y = 0;
		}
		if((x+width)>imgWidth)
			width = imgWidth - x;
		if((y+height)>imgHeight)
			height = imgHeight - y;

		clipX = x; clipY = y; clipW = width; clipH = height;
	}

	public void setARGBColor(int color){
		this.setColor = color;
	}

	/**
	 * Fills the parameter area with currently set color.
	 * <p>
	 * The method respects currently set clipping area and is hence clipped if parameter area
	 * intersects clipping.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void fillRect(int x, int y, int width, int height){

		//is rect outside clipping area bounds?
		if(width < 0 || height < 0 ||
				x >= clipX+clipW || y >= clipY+clipH || (x+width)<clipX || (y+height)<clipY){
			return;
		}

//		should rect area be cropped according to clipping area?
		if(x < clipX){
			width += x;
			x = clipX;
		}
		if(y < clipY){
			height += y;
			y = clipY;
		}
		if((x+width)>(clipX+clipW))
			width = (clipX+clipW) - x;
		if((y+height)>(clipY+clipH))
			height = (clipY+clipH) - y;

		replaceSourceScanlineArea(setColor, x, y, width, height);
	}

	/**
	 * Replace an area of source image pixels with a particular value.
	 * <p>
	 * Method uses arraycopy to speed things up.
	 * 
	 * @param color
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	private void replaceSourceScanlineArea(int color, int x, int y, int width, int height){

		int start = coordsToOffset(x,y);

		//create new scanline
		int[] newScanLine = new int[width];
		for(int i = newScanLine.length; --i>=0;)
			newScanLine[i] = color;

		for(int i = height; --i>=0;){
			System.arraycopy(newScanLine, 0, argbSource, start, width);
			start += imgWidth;
		}
	}

	private int coordsToOffset(int x, int y){
		return y*imgWidth + x;
	}

	private int coordsToOffset(ARGBImage argb, int x, int y){
		return y*argb.getWidth() + x;
	}

	public void drawARGBImageOnto(Image img, int x, int y){
		ARGBImage argb = new ARGBImage(img);
		drawARGBImageOnto(argb,x,y);
	}

	/**
	 * Draws another ARGBImage onto this. 
	 * <p>
	 * Clipping area is respected and Alpha Composition is handled correctly making 
	 * it possible to draw semi-transparent images ontop this one.
	 * 
	 * @param other
	 * @param x
	 * @param y
	 */
	public void drawARGBImageOnto(ARGBImage other, int x, int y){

		int	localClipX, localClipY, localClipW, localClipH;

		int subX=0,subY=0;

		//is image outside clipping area bounds?
		if(x >= clipX+clipW || y >= clipY+clipH || (x+other.getWidth())<clipX || (y+other.getHeight())<clipY)
			return;

		//is image enclosed by clipping area?
		if(x+other.getWidth() < clipX+clipW && x > clipX && y+other.getHeight() < clipY+clipH && y > clipY){
			localClipX = x; localClipY = y; localClipW = other.getWidth(); localClipH = other.getHeight();
		}
		else
		{
			//else, input image should be subregion'ed according to clipping area
			int width = other.getWidth(), height = other.getHeight();
			if(x < clipX){
				subX = clipX - x;
				localClipX = clipX;
			}else{
				localClipX = x;
			}
			if(y < clipY){
				subY = clipY - y;
				localClipY = clipY;
			}
			else
			{
				localClipY = y;
			}
			if((x+width)>(clipX+clipW)){
				localClipW = (clipX+clipW) - x - subX;
			}else{
				localClipW = width-subX;
			}
			if((y+height)>(clipY+clipH)){
				localClipH = (clipY+clipH) - y - subY;
			}else{
				localClipH = height-subY;
			}
		}

		//fect subregion
		int[] croppedOther = cropImage(other, subX, subY, localClipW, localClipH);

		int sourceOffset = coordsToOffset(localClipX, localClipY),
		targetOffset = 0;

		//apply image using alpha composition 'over' operation
		int j;
		for(int i = 0; i < localClipH; ++i){
			for(j = 0; j < localClipW; ++j ){
				argbSource[sourceOffset+j] = alphaComposition(croppedOther, targetOffset+j,argbSource, sourceOffset+j);
			}
			sourceOffset += imgWidth;
			targetOffset += localClipW;
		}
	}

	/**
	 * Combines two color values. Supports ARGB values.
	 * 
	 * @param argbSource
	 * @param sourceIndex
	 * @param argbTarget
	 * @param targetIndex
	 * @return
	 */
	private int alphaComposition(int[] argbSource, int sourceIndex, int[] argbTarget, int targetIndex){

		int source = argbSource[sourceIndex];
		int target = argbTarget[targetIndex];

		//is source fully transparent?
		if(source == transparencyColor)
			return target;
		
		//is target fully transparent?
		if(target == transparencyColor)
			return source;

		//is source fully opaque?
		if((0xFF&(source>>24)) == 255)
			return source;

		int alphaDestination = 0xFF&(target>>24);

		int alphaResult = overOperator(0xFF&(source>>24),alphaDestination, alphaDestination);
		int redResult = overOperator(0xFF&(source>>16),0xFF&(target>>16), alphaDestination);
		int greenResult = overOperator(0xFF&(source>>8),0xFF&(target>>8), alphaDestination);
		int blueResult = overOperator(0xFF&source, 0xFF&target, alphaDestination);

		return (alphaResult << 24) | (redResult << 16) | (greenResult << 8) | blueResult;
	}

	/**
	 * Alpha Composition Over operation.
	 * <p>
	 * See http://en.wikipedia.org/wiki/Alpha_composition for visuals.
	 * <p>
	 * Cr: Color Result
	 * Cs: Color Source
	 * Cd: Color destination
	 * Ad: Alpha Destination
	 * Cr = Cs*(1-Ad) + Cd
	 * 
	 * @param channelSource
	 * @param channelDestination
	 * @param alphaDestination
	 * @return
	 */
	private int overOperator(int channelSource, int channelDestination, int alphaDestination){
		return ((int)(((long)(channelSource<<16))*((255<<16)-(alphaDestination<<16)) >> 16) + (channelSource<<16)) >> 16;
	}

	/**
	 * Crops the input image according to parameter clipping area.
	 * 
	 * @param argbImg
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	private int[] cropImage(ARGBImage argbImg, int x, int y, int width, int height){

		if(x==0&&y==0&&argbImg.getWidth()==width&&argbImg.getHeight()==height)
			return argbImg.getARGB();

		int sourceOffset = coordsToOffset(argbImg,x, y), targetOffset = 0;
		int[] sourceArgb = argbImg.getARGB();
		int[] newArgb = new int[width*height];

		for(int i = height; --i>=0;){
			System.arraycopy(sourceArgb, sourceOffset, newArgb, targetOffset, width);
			sourceOffset += argbImg.getWidth();
			targetOffset += width;
		}
		return newArgb;
	}
	
	/**
	 * Converts current image to grayscale
	 */
	public Image getGrayScale(){
		if(argbSource!=null){
			for(int i = argbSource.length; --i>=0;)
				argbSource[i]=pixelToGrayScale(argbSource[i]);
		}
		return getImage();
	}
	
	/**
	 * Fixed point number representation of rgb channel desaturation factors
	 * <p>
	 * Using 16 fractional bits
	 */
	private final int TO_GRAY_RED_DESATURATOR_FIX= (int)(65536.0f*0.299);
	private final int TO_GRAY_GREEN_DESATURATOR_FIX= (int)(65536.0f*0.587);
	private final int TO_GRAY_BLUE_DESATURATOR_FIX= (int)(65536.0f*0.114);
	private final int FIX_PRECISION = 16;
	
	/**
	 * Converts a single ARGB pixel to gray scale
	 * <p>
	 * Luminance = 0.299 * R + 0.587 * G + 0.114 * B
	 * <p>
	 * Uses Fixed point math
	 * 
	 * @param pixel
	 * @return int grayscale pixel
	 */
	private int pixelToGrayScale(int pixel){
		
		int L_fix = (int)((((long)(((byte)(pixel>>16))<<FIX_PRECISION))*TO_GRAY_RED_DESATURATOR_FIX)>>FIX_PRECISION) +
			(int)((((long)(((byte)(pixel>>8))<<FIX_PRECISION))*TO_GRAY_GREEN_DESATURATOR_FIX)>>FIX_PRECISION) +
			(int)((((long)(((byte)(pixel))<<FIX_PRECISION))*TO_GRAY_BLUE_DESATURATOR_FIX)>>FIX_PRECISION);
		
		return 
		(pixel&0xFF000000)|(((L_fix>>FIX_PRECISION)&0xFF)<<16)|(((L_fix>>FIX_PRECISION)&0xFF)<<8)|((L_fix>>FIX_PRECISION)&0xFF);
	}

	/**
	 * Draws ARGBImage to screen using parameter Graphics handle.
	 * <p>
	 * Draws with a top left anchor.
	 * 
	 * @param g
	 * @param x
	 * @param y
	 */
	public void draw(Graphics g, int x, int y){
		g.setClip(clipX, clipY, clipW, clipH);
		int tx = x - g.getTranslateX(),
		 	ty = y - g.getTranslateY();
		g.translate(tx, ty);
		g.drawRGB(argbSource, 0, imgWidth, x, y, imgWidth, imgHeight, true);
		g.translate(-tx, -ty);
	}
	
	public void drawAsImage(Graphics g, int x, int y){
		g.drawImage( getImage(), x, y, 0); 
	}

	public Image getImage() {
		return Image.createRGBImage(argbSource, imgWidth, imgHeight, true);
	}

	/**
	 * Returns handle to int[] source container.
	 * <p>
	 * NOTE: Use with caution as chnages to handle affects object.
	 */
	public int[] getARGB() {
		return argbSource;
	}

	public int getWidth() {
		return imgWidth;
	}

	public int getHeight() {
		return imgHeight;
	}
}
