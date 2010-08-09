
/*
 * Created on 20-Apr-2004 at 01:30:49.
 *
 * Copyright (c) 2004-2005 Robert Virkus / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package de.enough.polish.util;

//#if polish.midp || polish.usePolishGui
	import javax.microedition.lcdui.Font;
//#endif


/**
 * <p>Provides some usefull String methods.</p>
 *
 * <p>Copyright Enough Software 2004 - 2009</p>

 * <pre>
 * history
 *        20-Apr-2004 - rob creation
 * </pre>
 * @author Robert Virkus, robert@enough.de
 */
public final class TextUtil {
	
	/**
	 * standard maximum lines number for text wrapping 
	 */
	public static final int MAXLINES_UNLIMITED = Integer.MAX_VALUE;
	
	public static final int MAXLINES_APPENDIX_POSITION_AFTER = 0x00;
	
	public static final int MAXLINES_APPENDIX_POSITION_BEFORE = 0x01;
	
	/**
	 * the default appendix to attach to a truncated text
	 */
	public static final String MAXLINES_APPENDIX = "...";
	
	private static final String UNRESERVED = "-_.!~*'()\"";
	
	/**
	 * Splits the given String around the matches defined by the given delimiter into an array.
	 * Example:
	 * <code>TextUtil.split("one;two;three", ';')</code> results into the array
	 * <code>{"one", "two", "three"}</code>.<br />
	 *
	 * @param value the String which should be split into an array
	 * @param delimiter the delimiter which marks the boundaries of the array 
	 * @return an array, when the delimiter was not found, the array will only have a single element.
	 */
	public static String[] split(String value, char delimiter) {
		char[] valueChars = value.toCharArray();
		int lastIndex = 0;
		ArrayList strings = null;
		for (int i = 0; i < valueChars.length; i++) {
			char c = valueChars[i];
			if (c == delimiter) {
				if (strings == null) {
					strings = new ArrayList();
				}
				strings.add( new String( valueChars, lastIndex, i - lastIndex ) );
				lastIndex = i + 1;
			}
		}
		if (strings == null) {
			return new String[]{ value };
		}
		// add tail:
		strings.add( new String( valueChars, lastIndex, valueChars.length - lastIndex ) );
		return (String[]) strings.toArray( new String[ strings.size() ] );
	}
	

	/**
	 * Splits the given String around the matches defined by the given delimiter into an array.
	 * Example:
	 * <code>TextUtil.splitAndTrim(" one; two; three", ';')</code> results into the array
	 * <code>{"one", "two", "three"}</code>.<br />
	 *
	 * @param value the String which should be split into an array
	 * @param delimiter the delimiter which marks the boundaries of the array 
	 * @return an array, when the delimiter was not found, the array will only have a single element.
	 */
	public static String[] splitAndTrim(String value, char delimiter) 
	{
		String[] result = split(value, delimiter);
		for (int i = 0; i < result.length; i++)
		{
			result[i] = result[i].trim();
			
		}
		return result;
	}
	
	/**
	 * Splits the given String around the matches defined by the given delimiter into an array.
	 * Example:
	 * <code>TextUtil.split("one;two;three", ';', 3)</code> results into the array
	 * <code>{"one", "two", "three"}</code>.<br />
	 * <code>TextUtil.split("one;two;three", ';', 4)</code> results into the array
	 * <code>{"one", "two", "three", null}</code>.<br />
	 * <code>TextUtil.split("one;two;three", ';', 2)</code> results into the array
	 * <code>{"one", "two"}</code>.<br />
	 * This method is less resource intensive compared to the other split method, since
	 * no temporary list needs to be created
	 *
	 * @param value the String which should be split into an array
	 * @param delimiter the delimiter which marks the boundaries of the array 
	 * @param numberOfChunks the number of expected matches
	 * @return an array with the length of numberOfChunks, when not enough elements are found, the array will contain null elements
	 */
	public static String[] split(String value, char delimiter, int numberOfChunks) {
		char[] valueChars = value.toCharArray();
		int lastIndex = 0;
		String[] chunks = new String[ numberOfChunks ];
		int chunkIndex = 0;
		for (int i = 0; i < valueChars.length; i++) {
			char c = valueChars[i];
			if (c == delimiter) {
				chunks[ chunkIndex ] = value.substring( lastIndex, i );
				lastIndex = i + 1;
				chunkIndex++;
				if (chunkIndex == numberOfChunks ) {
					break;
				}
			}
		}
		if (chunkIndex < numberOfChunks) {
			// add tail:
			chunks[chunkIndex] = value.substring( lastIndex, valueChars.length );
		}
		return chunks;
	}

	//#if polish.midp || polish.usePolishGui
	/**
	 * Wraps the given string so it fits on the specified lines.
	 * First of all it is split at the line-breaks ('\n'), subsequently the substrings
	 * are split when they do not fit on a single line.
	 *  
	 * @param value the string which should be split
	 * @param font the font which is used to display the font
	 * @param firstLineWidth the allowed width for the first line
	 * @param lineWidth the allowed width for all other lines, lineWidth >= firstLineWidth
	 * @return the array containing the substrings
	 * @deprecated please use wrap instead
	 * @see #wrap(String, Font, int, int)
	 */
	public static String[] split( String value, Font font, int firstLineWidth, int lineWidth ) {
		return wrap(value, font, firstLineWidth, lineWidth, MAXLINES_UNLIMITED, (String)null, TextUtil.MAXLINES_APPENDIX_POSITION_AFTER);
	}
	//#endif
	
	//#if polish.midp || polish.usePolishGui
	/**
	 * Wraps the given string so it fits on the specified lines.
	 * First of all it is split at the line-breaks ('\n'), subsequently the substrings
	 * are split when they do not fit on a single line.
	 *  
	 * @param value the string which should be split
	 * @param font the font which is used to display the font
	 * @param firstLineWidth the allowed width for the first line
	 * @param lineWidth the allowed width for all other lines, lineWidth >= firstLineWidth
	 * @return the array containing the substrings
	 */
	public static String[] wrap( String value, Font font, int firstLineWidth, int lineWidth) {
		return wrap(value, font, firstLineWidth, lineWidth, MAXLINES_UNLIMITED, (String)null, TextUtil.MAXLINES_APPENDIX_POSITION_AFTER);
	}
	
	/**
	 * Wraps the given string so it fits on the specified lines.
	 * First of al it is splitted at the line-breaks ('\n'), subsequently the substrings
	 * are splitted when they do not fit on a single line.
	 *  
	 * @param value the string which should be splitted
	 * @param font the font which is used to display the font
	 * @param firstLineWidth the allowed width for the first line
	 * @param lineWidth the allowed width for all other lines, lineWidth >= firstLineWidth
	 * @param maxLines the maximum number of lines
	 * @param maxLinesAppendix the appendix that should be added to the last line when the line number is greater than maxLines
	 * @return the array containing the substrings
	 */
	public static String[] wrap( String value, Font font, int firstLineWidth, int lineWidth, int maxLines, String maxLinesAppendix ) {
		return wrap( value, font, firstLineWidth, lineWidth, maxLines, maxLinesAppendix, MAXLINES_APPENDIX_POSITION_AFTER );
	}

	/**
	 * Wraps the given string so it fits on the specified lines.
	 * First of al it is splitted at the line-breaks ('\n'), subsequently the substrings
	 * are splitted when they do not fit on a single line.
	 *  
	 * @param value the string which should be splitted
	 * @param font the font which is used to display the font
	 * @param firstLineWidth the allowed width for the first line
	 * @param lineWidth the allowed width for all other lines, lineWidth >= firstLineWidth
	 * @param maxLines the maximum number of lines
	 * @param maxLinesAppendix the appendix that should be added to the last line when the line number is greater than maxLines
	 * @param maxLinesAppendixPosition either MAXLINES_APPENDIX_POSITION_AFTER or MAXLINES_APPENDIX_POSITION_BEFORE
	 * @return the array containing the substrings
	 */
	public static String[] wrap( String value, Font font, int firstLineWidth, int lineWidth, int maxLines, String maxLinesAppendix, int maxLinesAppendixPosition ) {
		if (firstLineWidth <= 0 || lineWidth <= 0) {
			//#debug error
			System.out.println("INVALID LINE WIDTH FOR SPLITTING " + firstLineWidth + " / " + lineWidth + " ( for string " + value + ")");
			//#if polish.debug.error
				try { throw new RuntimeException("INVALID LINE WIDTH FOR SPLITTING " + firstLineWidth + " / " + lineWidth + " ( for string " + value + ")"); } catch (Exception e) {System.out.println(e.getMessage()); e.printStackTrace(); }	
			//#endif
			return new String[]{ value };
		}
		boolean hasLineBreaks = (value.indexOf('\n') != -1);
		int completeWidth = font.stringWidth(value);
		if ( (completeWidth <= firstLineWidth && !hasLineBreaks) ) { // || (value.length() <= 1) ) {
			// the given string fits on the first line:
			//if (hasLineBreaks) {
			//	return split( "complete/linebreaks:" + completeWidth + "> " + value, '\n');
			//} else {
				return new String[]{ value };
			//}
		}
		// the given string does not fit on the first line:
		ArrayList lines = new ArrayList();
		if (!hasLineBreaks) {
			wrap( value, font, completeWidth, firstLineWidth, lineWidth, lines, maxLines, maxLinesAppendixPosition);
		} else {
			// now the string will be split at the line-breaks and
			// then each line is processed:
			char[] valueChars = value.toCharArray();
			int lastIndex = 0;
			char c =' ';
			int lineBreakCount = 0;
			for (int i = 0; i < valueChars.length; i++) {
				c = valueChars[i];
				boolean isCRLF = (c == 0x0D && i < valueChars.length -1 &&  valueChars[i +1] == 0x0A);
				if (c == '\n' || i == valueChars.length -1 || isCRLF ) {
					lineBreakCount++;
					String line = null;
					if (i == valueChars.length -1) {
						line = new String( valueChars, lastIndex, (i + 1) - lastIndex );
						//System.out.println("wrap: adding last line " + line );
					} else {
						line = new String( valueChars, lastIndex, i - lastIndex );
						//System.out.println("wrap: adding " + line );
					}
					completeWidth = font.stringWidth(line);
					if (completeWidth <= firstLineWidth ) {
						lines.add( line );
						
						if(lines.size() == maxLines)
						{
							// break if the maximum number of lines is reached
							break;
						}
					} else {
						wrap(line, font, completeWidth, firstLineWidth, lineWidth, lines, maxLines, maxLinesAppendixPosition);
					}
					if (isCRLF) {
						i++;
					}
					lastIndex = i + 1;
					// after the first line all line widths are the same:
					firstLineWidth = lineWidth;
				} // for each line
			} // for all chars
			// special case for lines that end with \n: add a further line
			if (lineBreakCount > 1 && (c == '\n' || c == 10) && lines.size() != maxLines) {
				lines.add("");
			}
		}
		
		if(lines.size() >= maxLines)
		{
			String line = (String)lines.get(maxLines - 1);
			if (maxLinesAppendix == null) {
				maxLinesAppendix = MAXLINES_APPENDIX;
			}
			line = addAppendix(line, font, firstLineWidth, maxLinesAppendix, maxLinesAppendixPosition);
			lines.set(maxLines - 1, line);
			while (lines.size() > maxLines) {
				lines.remove( lines.size() -1 );
			}
		}
		
		//#debug
		System.out.println("Wrapped [" + value + "] into " + lines.size() + " rows.");
		return (String[]) lines.toArray( new String[ lines.size() ]);
	}
	//#endif
	
	//#if polish.midp || polish.usePolishGui
	/**
	 * Wraps the given string so that the substrings fit into the the given line-widths.
	 * It is expected that the specified lineWidth >= firstLineWidth.
	 * The resulting substrings will be added to the given ArrayList.
	 * When the complete string fits into the first line, it will be added
	 * to the list.
	 * When the string needs to be splited to fit on the lines, it is tried to
	 * split the string at a gab between words. When this is not possible, the
	 * given string will be splitted in the middle of the corresponding word. 
	 * 
	 * 
	 * @param value the string which should be splitted
	 * @param font the font which is used to display the font
	 * @param completeWidth the complete width of the given string for the specified font.
	 * @param firstLineWidth the allowed width for the first line
	 * @param lineWidth the allowed width for all other lines, lineWidth >= firstLineWidth
	 * @param list the list to which the substrings will be added.
	 * @deprecated please use wrap instead
	 * @see #wrap(String, Font, int, int, int, ArrayList, int, int)
	 */
	public static void split( String value, Font font, 
			int completeWidth, int firstLineWidth, int lineWidth, 
			ArrayList list ) 
	{
		wrap(value, font, completeWidth, firstLineWidth, lineWidth, list, MAXLINES_UNLIMITED, MAXLINES_APPENDIX_POSITION_AFTER);
	}
	//#endif
	
	//#if polish.midp || polish.usePolishGui
	/**
	 * Wraps the given string so that the substrings fit into the the given line-widths.
	 * It is expected that the specified lineWidth >= firstLineWidth.
	 * The resulting substrings will be added to the given ArrayList.
	 * When the complete string fits into the first line, it will be added
	 * to the list.
	 * When the string needs to be splited to fit on the lines, it is tried to
	 * split the string at a gab between words. When this is not possible, the
	 * given string will be splitted in the middle of the corresponding word. 
	 * 
	 * 
	 * @param value the string which should be splitted
	 * @param font the font which is used to display the font
	 * @param completeWidth the complete width of the given string for the specified font.
	 * @param firstLineWidth the allowed width for the first line
	 * @param lineWidth the allowed width for all other lines, lineWidth >= firstLineWidth
	 * @param maxLines the maximum number of lines 
	 * @param maxLinesAppendixPosition either MAXLINES_APPENDIX_POSITION_AFTER or MAXLINES_APPENDIX_POSITION_BEFORE
	 * @param list the list to which the substrings will be added.
	 */
	public static void wrap( String value, Font font, 
			int completeWidth, int firstLineWidth, int lineWidth, 
			ArrayList list,
			int maxLines, int maxLinesAppendixPosition ) 
	{
		//TODO extend wrapping : bottom line - based wrapping 
		if(maxLinesAppendixPosition == MAXLINES_APPENDIX_POSITION_BEFORE && maxLines == 1) {
			list.add(value);
			return;
		}
		
		int lastLineIndex = maxLines - 1;
		char[] valueChars = value.toCharArray();
		int startPos = 0;
		int lastSpacePos = -1;
		int lastSpacePosLength = 0;
		int currentLineWidth = 0;
		for (int i = 0; i < valueChars.length; i++) {
			char c = valueChars[i];
			currentLineWidth += font.charWidth( c );
			if (c == '\n') {
				list.add( new String( valueChars, startPos, i - startPos ) );
				lastSpacePos = -1;
				startPos = i+1;
				currentLineWidth = 0;
				firstLineWidth = lineWidth; 
				i = startPos;
			} else if (currentLineWidth >= firstLineWidth && i > 0) {
				if(list.size() == lastLineIndex)
				{
					// add the remainder of the value
					list.add( new String( valueChars, startPos, valueChars.length - startPos ) );
					break;
				}
				
				// need to create a line break:
				if (c == ' ' || c == '\t') { 
					String line = new String( valueChars, startPos, i - startPos );
					if (lastSpacePos != -1 && (font.stringWidth(line) > currentLineWidth) ) {
						// adding the widths of characters does not always yield a correct result,
						// so using stringWidth here ensures that we really break at the correct position:
						if (i > startPos + 1) {
							i--;
						}
						//System.out.println("value=" + value + ", i=" + i + ", startPos=" + startPos);
						list.add( new String( valueChars, startPos, lastSpacePos - startPos ) );
						startPos =  lastSpacePos;
						currentLineWidth -= lastSpacePosLength;
						lastSpacePos = i;
						lastSpacePosLength = currentLineWidth;
					} else {
						//line += font.stringWidth(line) + "[" + currentLineWidth + "]";
						list.add( line );
						startPos =  ++i;
						currentLineWidth = 0;
						lastSpacePos = -1;
					}
				} else if ( lastSpacePos == -1) {
					/**/
					//System.out.println("value=" + value + ", i=" + i + ", startPos=" + startPos);
					list.add( new String( valueChars, startPos, i - startPos ) );
					startPos =  i;
					currentLineWidth = font.charWidth(valueChars[i]);
				} else {
					currentLineWidth -= lastSpacePosLength;
					String line = new String( valueChars, startPos, lastSpacePos - startPos );
					//line += font.stringWidth(line) + "<" + lastSpacePosLength + ">";
					list.add( line );
					startPos =  lastSpacePos + 1;
					lastSpacePos = -1;
				}
				
				firstLineWidth = lineWidth; 
			} else if (c == ' ' || c == '\t') {
				lastSpacePos = i;
				lastSpacePosLength = currentLineWidth;
			}
			
		} 
		
		if(list.size() != maxLines)
		{
			// add tail:
			list.add( new String( valueChars, startPos, valueChars.length - startPos ) );
		}
		
		/*
		try {
		int widthPerChar = (completeWidth * 100) / valueChars.length;
		int startIndex = 0;
		while (true) {
			int index = (firstLineWidth * 100) / widthPerChar;
			int i = index + startIndex;
			if (i >= valueChars.length) {
				// reached the end of the given string:
				list.add( new String( valueChars, startIndex, valueChars.length - startIndex ) );
				break;
			}
			// find the last word gap:
			while (valueChars[i] != ' ') {
				i--;
				if (i < startIndex ) {
					// unable to find a gap:
					i = index + startIndex;
					break;
				}
			}
			//TODO rob maybe check whether the found string really fits into the line
			list.add( new String( valueChars, startIndex, i - startIndex ) );
			if (valueChars[i] == ' ' || valueChars[i] == '\t') {
				startIndex = i + 1;
			} else {				
				startIndex = i;
			}
			firstLineWidth = lineWidth;
		}
		} catch (ArithmeticException e) {
			System.out.println("unable to split: " + e);
			System.out.println("complete width=" + completeWidth + " number of chars=" + value.length());
		}
		*/
	}
	//#endif
	
	//#if polish.midp || polish.usePolishGui
	
	/**
	 * Adds the specified appendix to the give line of text
	 * and shortens the text to fit the available width 
	 * @param line the textline
	 * @param font the font used
	 * @param availWidth the available width
	 * @param appendix the appendix to be added
	 * @return the shortened line with the appendix
	 */
	private static String addAppendix(String line, Font font, int availWidth, String appendix, int position)
	{
		try
		{
			int appendixWidth = font.stringWidth(appendix);
			int lineWidth = font.stringWidth(line);
			int completeWidth = lineWidth +  appendixWidth;
			if(availWidth < appendixWidth)
			{
				line = appendix;
				completeWidth = appendixWidth;
				while(completeWidth > availWidth)
				{
					if(position == TextUtil.MAXLINES_APPENDIX_POSITION_AFTER) {
						line = line.substring(0,line.length() - 1);
					} else if(position == TextUtil.MAXLINES_APPENDIX_POSITION_BEFORE) {
						line = line.substring(1);
					}
					completeWidth = font.stringWidth(line);
				}
				
				return line;
			}
			else
			{
				if(lineWidth > availWidth) {
					while(completeWidth > availWidth)
					{
						if(position == TextUtil.MAXLINES_APPENDIX_POSITION_AFTER) {
							line = line.substring(0,line.length() - 1);
						} else if(position == TextUtil.MAXLINES_APPENDIX_POSITION_BEFORE) {
							line = line.substring(1);
						}
							
						completeWidth = font.stringWidth(line) +  appendixWidth;
					}
					
					if(position == TextUtil.MAXLINES_APPENDIX_POSITION_AFTER) {
						return line + appendix;
					} else {
						return appendix + line;
					}
				} else {
					return line;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			return "";
		}
	}
	//#endif
	
//	 Unreserved punctuation mark/symbols

    /**
     * Converts Hex digit to a UTF-8 "Hex" character
     * 
     * @param digitValue digit to convert to Hex
     * @return the converted Hex digit
     */
    private static char toHexChar(int digitValue) {
        if (digitValue < 10)
            // Convert value 0-9 to char 0-9 hex char
            return (char)('0' + digitValue);
        else
            // Convert value 10-15 to A-F hex char
            return (char)('A' + (digitValue - 10));
    }

    /**
     * Encodes a URL string.
     * This method assumes UTF-8 usage.
     * 
     * @param url URL to encode
     * @return the encoded URL
     */
    public static String encodeUrl(String url) {
        StringBuffer encodedUrl = new StringBuffer(); // Encoded URL
        int len = url.length();
        // Encode each URL character
        for (int i = 0; i < len; i++) {
            char c = url.charAt(i); // Get next character
            if ((c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z')) {
                // Alphanumeric characters require no encoding, append as is
                encodedUrl.append(c);
            } else {
                int imark = UNRESERVED.indexOf(c);
                if (imark >=0) {
                    // Unreserved punctuation marks and symbols require
                    //  no encoding, append as is
                    encodedUrl.append(c);
                } else {
                    // Encode all other characters to Hex, using the format "%XX",
                    //  where XX are the hex digits
                    encodedUrl.append('%'); // Add % character
                    // Encode the character's high-order nibble to Hex
                    encodedUrl.append(toHexChar((c & 0xF0) >> 4));
                    // Encode the character's low-order nibble to Hex
                    encodedUrl.append(toHexChar (c & 0x0F));
                }
            }
        }
        return encodedUrl.toString(); // Return encoded URL
    }
    
    /**
     * Encodes a string for parsing using an XML parser.
     * &amp, &lt; and &gt; are replaced.
     * 
     * @param text the text
     * @return the cleaned text
     */
    public static String encodeForXmlParser( String text ) {
    	int len = text.length();
    	StringBuffer buffer = new StringBuffer(len + 10);
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c == '&') {
            	buffer.append("&amp;");
            } else if (c == '<') {
            	buffer.append("&lt;");
            } else if (c == '>') {
            	buffer.append("&gt;");
            } else {
            	buffer.append(c);
            }
        }
    	return buffer.toString();
    }

    /**
     * Replaces the all matches within a String.
     * 
     * @param input the input string
     * @param search the string that should be replaced
     * @param replacement the replacement
     * @return the input string where the search string has been replaced
     * @throws NullPointerException when one of the specified strings is null
     */
    public static String replace( String input, String search, String replacement ) {
    	int pos = input.indexOf( search );
    	if (pos != -1) {
    		StringBuffer buffer = new StringBuffer();
    		int lastPos = 0;
    		do {
    			buffer.append( input.substring( lastPos, pos ) )
    			      .append( replacement );
    			lastPos = pos + search.length();
    			pos = input.indexOf( search, lastPos );
    		} while (pos != -1);
    		buffer.append( input.substring( lastPos ));
    		input = buffer.toString();
    	}
    	return input;
    }

    
    /**
     * Replaces the first match in a String.
     * 
     * @param input the input string
     * @param search the string that should be replaced
     * @param replacement the replacement
     * @return the input string where the first match of the search string has been replaced
     * @throws NullPointerException when one of the specified strings is null
     */
    public static String replaceFirst( String input, String search, String replacement ) {
    	int pos = input.indexOf( search );
    	if (pos != -1) {
    		input = input.substring(0, pos) + replacement + input.substring( pos + search.length() );
    	}
    	return input;
    }

    /**
     * Replaces the last match in a String.
     * 
     * @param input the input string
     * @param search the string that should be replaced
     * @param replacement the replacement
     * @return the input string where the last match of the search string has been replaced
     * @throws NullPointerException when one of the specified strings is null
     */
    public static String replaceLast( String input, String search, String replacement ) {
    	int pos = input.indexOf( search );
    	if (pos != -1) {
	    	int lastPos = pos;
	    	while (true) {
	    		pos = input.indexOf( search, lastPos + 1 );
	    		if (pos == -1) {
	    			break;
	    		} else {
	    			lastPos = pos;
	    		}
	    	}
    		input = input.substring(0, lastPos) + replacement + input.substring( lastPos + search.length() );
    	}
    	return input;
    }

    /**
     * Retrieves the last index of the given match in the specified text.
     * 
     * @param text the text in which the match is given
     * @param match the match within the text
     * @return the last index of the match or -1 when the match is not found in the given text
     * @throws NullPointerException when text or match is null
     */
	public static int lastIndexOf(String text, String match) {
		int lastIndex = -1;
		int index = text.indexOf( match );
		while (index != -1) {
			lastIndex = index;
			index = text.indexOf( match, lastIndex + 1 );
		}
		return lastIndex;
	}
    

  /**
   * Compares two strings in a case-insensitive way. Both strings are lower cased and
   * then compared. If both are equal this method returns <code>true</code>,
   * <code>false</code> otherwise.
   *    
   * @param str1 the string to compare
   * @param str2 the string to compare to
   * 
   * @return <code>true</code> if both strings are equals except case,
   * <code>false</code>
   * 
   * @throws NullPointerException if <code>str1</code> is <code>null</code>
   * 
   * @see String#equals(Object)
   * @see String#equalsIgnoreCase(String)
   */
	public static boolean equalsIgnoreCase(String str1, String str2)
	{
	//#if polish.cldc1.1
		//# return str1.equalsIgnoreCase(str2);
	//#else
		if (str2 == null || str1.length() != str2.length() )
		{
			return false;
		}
		return str1.toLowerCase().equals(str2.toLowerCase());
	//#endif
	}


	/**
	 * Reverses the given text while keeping English texts and numbers in the normal position.
	 * 
	 * @param input the text
	 * @return the reveresed text
	 */
	public static String reverseForRtlLanguage(String input)
	{
		StringBuffer output = new StringBuffer( input.length() );
		StringBuffer ltrCharacters = new StringBuffer();
		boolean isCurrRTL = true;
		
		int size = input.length();
		for(int index = size - 1; index >= 0;)
		{
			while(isCurrRTL && index >= 0) // while we are in hebrew
			{
				char curr = input.charAt(index); 
				char nextChr = '\0';
				if(index > 0) {
					nextChr = input.charAt(index-1);
				} else {
					nextChr = curr;
				}
				
				if(isEnglishChar(curr) || isEnglishChar(nextChr))
				{
					isCurrRTL = false;
				}
				else
				{
					if(curr == '(')
					{
						output.append( ')' );
					}
					else if(curr == ')')
					{
						output.append( '(' );
					}
					else 
					{
						output.append( curr ); //left to right language - save the chars
					}
					
					index--;
				}
				
			}
			ltrCharacters.delete(0, ltrCharacters.length() );
			while(!isCurrRTL && index >= 0) // English....
			{
				char curr = input.charAt(index);
				char nextChr = '\0';
				if(index > 0) 
				{
					nextChr = input.charAt(index-1);
				}
				else 
				{
					nextChr = curr;
				}
				if(isEnglishChar(curr) || isEnglishChar(nextChr))
				{
					ltrCharacters.insert( 0, curr );
					index--;
				}
				else 
				{
					isCurrRTL = true;
				}
			}
			 
			output.append( ltrCharacters );
		 }
		return output.toString();
	}
	
	 private static boolean isEnglishChar(char chr)
	 {
		 if ( chr < 128 && (chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z' || chr >= '0' && chr <= '9' || chr == '+' ) )
		 {
			 return true;
		 }
		 else 
		 {
			 return false;
		 }
	 } 

}
