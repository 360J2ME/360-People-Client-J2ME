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
package com.zyb.nowplus.presentation.view.items;

//#if activate.virtual.listprovider == true
import com.zyb.nowplus.presentation.view.providers.VirtualListProvider;
//#endif

//#if polish.blackberry.isTouchBuild == true
import de.enough.polish.ui.Command;
import de.enough.polish.ui.ItemCommandListener;
//# import net.rim.device.api.ui.Keypad;
//# import net.rim.device.api.ui.VirtualKeyboard;
//#endif
import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.IconItem;
import de.enough.polish.ui.Item;
//#= import de.enough.polish.ui.Display;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.TextField;
//#if polish.device.requires.custom.textfield:defined
import de.enough.polish.ui.xTextField;
//#endif
import de.enough.polish.ui.UiAccess;

/**
 * Item for displaying NowPlus searchfield.
 * <p>
 * Encapsulates graphical elements and layout.
 * 
 * @author Anders Bo Pedersen, andersbo.pedersen@vodafone.com
 */
public class SearchFieldItem extends Container
//#if polish.blackberry.isTouchBuild == true
 implements ItemCommandListener
//#endif
{
	private IconItem loop;
	
	private TextField textfield;
	
	private String lastSearchString = "";
	private Thread searchThread;
	
	
	public TextField getTextfield()
	{
		return textfield;
	}
	
	boolean inUse;
	
	//#if polish.blackberry.isTouchBuild == true
	
		//#if activate.virtual.listprovider == true
			protected VirtualListProvider provider;
		
			public void setProvider( VirtualListProvider provider){
				this.provider = provider;
			}
		//#endif
	
	//#endif
	
	public SearchFieldItem() 
	{
		this(null);
	}
	
	public SearchFieldItem(Style style)
	{
		super(true, style);
		
		//#style searchfield_loopicon
		loop = new IconItem(null,null);
		loop.setAppearanceMode(Item.PLAIN);
		
		//#if polish.device.requires.custom.textfield:defined
			//#style searchfield_textfield
			textfield = new xTextField("","",100,TextField.ANY);
		//#else
			//#style searchfield_textfield
			textfield = new TextField("","",100,TextField.ANY);
		//#endif
		textfield.setSuppressCommands(true);
		
		//#if polish.blackberry.isTouchBuild == true
			//fix for PBLA-833
			textfield.setNoComplexInput(true);
			
			loop.setDefaultCommand(new Command("searchfield", Command.OK, 0));
			loop.setItemCommandListener(this);
			loop.setAppearanceMode(Item.INTERACTIVE);
		//#else
			UiAccess.setTextfieldHelp(textfield, null);
			loop.setAppearanceMode(Item.PLAIN);
		//#endif
		this.setAppearanceMode(Item.INTERACTIVE);
		
		//add elements
		this.add(loop);
		this.add(textfield);
		
		setInUse(false);
	}
	
	public void setInUse(boolean inUse)
	{
		this.inUse = inUse;
	}
	
	public boolean isInUse()
	{
		return this.inUse;
	}
	
	public String getString()
	{
		return textfield.getString();
	}
	
	public void setString(String txt)
	{
		textfield.setString(txt);
	}

	protected boolean handleKeyPressed(int keyCode, int gameAction) 
	{
		//In search textfiled not put stat
		if (Canvas.KEY_STAR==keyCode)
		{
			return false;
		}
		if(!UiAccess.handleKeyPressed(textfield, keyCode, gameAction))	
			return super.handleKeyPressed(keyCode, gameAction);
		else
			return true;
	}
	
	protected boolean handleKeyRepeated(int keyCode, int gameAction)
	{
		if(!UiAccess.handleKeyRepeated(textfield, keyCode, gameAction))
			return super.handleKeyRepeated(keyCode, gameAction);
		else
			return true;
	}
	
	protected boolean handleKeyReleased(int keyCode, int gameAction)
	{
		if(!UiAccess.handleKeyReleased(textfield, keyCode, gameAction))
			return super.handleKeyReleased(keyCode, gameAction);
		else
			return true;
	}
	
	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Container#hideNotify()
	 */
	public void hideNotify()
	{
		this.textfield.setString("");
		
		//#if polish.blackberry.isTouchBuild == false
		this.inUse = false;
		//#endif
		
		super.hideNotify();
	}

	/**
	 * deletes the character at current postion on a text field associated with this search item
	 */
	public void deleteCharAtCaretPosition() {
		final int caretPosition = this.textfield.getCaretPosition();
		final StringBuffer stringBuffer = new StringBuffer(this.textfield.getString());
		if(caretPosition > 0 && caretPosition <= stringBuffer.length()) {
			stringBuffer.deleteCharAt(caretPosition-1);
			this.textfield.setString(stringBuffer.toString());
            this.textfield.setCaretPosition(caretPosition-1);
		}
	}

	//#if polish.blackberry.isTouchBuild == true
	
    public void commandAction(Command c, Item item)
    {
    	//#if activate.virtual.listprovider == true
    	if(this.provider != null && this.textfield.getText().intern() != null && !this.textfield.getText().intern().equals(""))
    	{
    		String toSearch = this.textfield.getText().intern();
    		this.provider.search(toSearch);
    	}
    	//#endif
    }
    
    //#if activate.virtual.listprovider == true
	public void search()
	{
		if(this.provider != null && this.textfield.getText().intern() != null && !this.textfield.getText().intern().equals(""))
		{
			String toSearch = this.textfield.getText().intern();

			if(!lastSearchString.equals(toSearch))
			{
				lastSearchString = toSearch;
				this.provider.search(toSearch);
			}
		}
		else
			lastSearchString = "";
	}
	
	protected void showNotify()
	{
		UiAccess.defocus(this.textfield,this.textfield.getStyle());
		super.showNotify();
		
		searchThread = new Thread(new Runnable() 
		{
			public void run()
			{
				while(isInUse())
				{
					search();
					
					try 
					{
						//sleep search thread
						Thread.sleep(600);
					} catch (InterruptedException e)
					{
						//do nothing
					}
				}
			}
		});
		searchThread.start();
	}
	//#endif

	public boolean handlePointerTouchDown(int x, int y)
	{
		return handlePointerPressed(x,y);
	}

	public boolean handlePointerTouchUp(int x, int y)
	{
		return handlePointerReleased(x,y);
	}

	protected boolean handlePointerPressed(int relX, int relY) 
	{
		Item[] items = this.getItems();
		
		for (int i = 0; i < items.length; i++)
		{
			if (items[i].isInItemArea(relX, relY))//Any Items in this container selected >>active searching textfield
			{
				UiAccess.focus(this.getScreen(), this.textfield);
				
				//# Display.getInstance().getVirtualKeyboard().setVisibility( VirtualKeyboard.SHOW );
				
				return true;
			}
		}
		
		if(this.isInContentArea(relX, relY))//from screen view(PeopleTabContacts class), this container is selected
		{
			UiAccess.focus(this.getScreen(), this.textfield);
			
			//# Display.getInstance().getVirtualKeyboard().setVisibility( VirtualKeyboard.SHOW );

			return true;
		}

		
		return false;
	}

	protected Style focus(Style focusStyle, int direction)
	{
		UiAccess.defocus(this.textfield,this.textfield.getStyle());
		return super.focus(focusStyle, direction);
	}

	public void focusChild(int index, Item item, int direction, boolean force)
	{
		UiAccess.defocus(this.textfield,this.textfield.getStyle());
		super.focusChild(index, item, direction, force);
	}

	public boolean focusChild(int index)
	{
		UiAccess.defocus(this.textfield,this.textfield.getStyle());
		return super.focusChild(index);
	}   

	public boolean isInItemArea(int relX, int relY, Item child)
	{
		if(child == null)
			return false;
		if(        child.getAbsoluteX() <= this.getAbsoluteX()+relX 
				&& child.getAbsoluteX()+child.getInternalWidth() <= this.getAbsoluteX()+relX
				&& child.getAbsoluteY() <= this.getAbsoluteY()+relY 
				&& child.getAbsoluteY()+child.getInternalHeight() <= this.getAbsoluteY()+relY
		)
			return true;
		return false;
	}
	
	protected boolean handlePointerReleased(int relX, int relY)
	{
		Item[] items = this.getItems();
		for(int i = 0; i < items.length; i++)
		{
			if(items[i].isInItemArea(relX, relY))
			{
				return true;
			}
		}
		
		return false;
	}
 
 //#endif
}
