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
package com.zyb.nowplus.presentation.view.forms;

import javax.microedition.lcdui.Graphics;

import com.zyb.nowplus.NowPlus;
import com.zyb.nowplus.business.Model;

import com.zyb.nowplus.presentation.controller.Controller;
import com.zyb.util.DebugUtils;
import com.zyb.util.TextUtilities;
import com.zyb.util.event.Event;
import com.zyb.util.event.EventListener;

import de.enough.polish.ui.Canvas;
import de.enough.polish.ui.Command;
import de.enough.polish.ui.CommandListener;
import de.enough.polish.ui.Container;
import de.enough.polish.ui.Displayable;
import de.enough.polish.ui.FramedForm;
import de.enough.polish.ui.Item;
import de.enough.polish.ui.ItemCommandListener;
import de.enough.polish.ui.ItemStateListener;
import de.enough.polish.ui.Screen;
import de.enough.polish.ui.Style;
import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.UiAccess;
import de.enough.polish.util.Debug;
import de.enough.polish.util.Locale;

//#if polish.blackberry
import net.rim.device.api.ui.Keypad;
import com.zyb.nowplus.presentation.BlackBerryOptionsHandler;
import de.enough.polish.util.DeviceControl;
//#endif

//#if ${visualverification}==true
//# import com.zyb.utils.TestUtil;
//#endif

/**
 * @author Andre Schmidt
 */
public abstract class BaseFramedForm extends FramedForm 
implements 
	CommandListener, 
	ItemCommandListener, 
	ItemStateListener,
	ContextForm, EventListener
{
	
	//#if polish.ignoreoddplatformrequest:defined
	// blocking odd multiple calls on samsung
		private static boolean performedKeyPressed = false;
	//#endif
	
	//#if polish.blackberry
	//these keys are defined as constants since bb 4.7 or 5.0 but are needed in earlier versions.
	final static int KEY_LOCK = 4099; 		// Keypad.KEY_LOCK
	final static int KEY_CAMERA_FOCUS= 211; // Keypad.KEY_CAMERA_FOCUS
	final static int KEY_FORWARD = 4100;	//KEY_FORWARD
	final static int KEY_BACKWARD = 4101; 	//KEY_BACKWARD
	//#endif
		
		
		
	/**
	 * handle to parent Screen if any
	 */
	private Screen parent;
	
	Model model;
	
	Controller controller;
	
	/**
	 * Items that are to remain static, placed top most in frame
	 */
	protected Container staticTopItems;
	
	/**
	 * Single Item placed in top frame under static items if any
	 */
	protected Item topItem;
	
	/**
	 * Single Item placed in bottom frame
	 */
	protected Item bottomItem;
	
	/**
	 * Flag used to control 'delayed construction' pattern.
	 * Is true if showNotify() has been called once, false otherwise
	 */
	protected volatile boolean isContentCreated = false;

		
	public static Object lock = new Object();
	
	/**
	 * Commands common to all BaseFramedForm
	 */
	public final static Command cmdBack = new Command(Locale.get("polish.command.back"), Command.BACK, 0);
	public final static Command cmdGreenCallKey = new Command("", Command.OK, 10);
	public final static Command cmdClearKey = new Command("", Command.OK, 10);
	//Used to suppress key handling for inactive items
	public final static Command cmdFake = new Command("", Command.ITEM, 0);
	
	public BaseFramedForm( Model model, Controller controller,  String title)
	{
		this(model, controller, title, null);
	}
	
	public BaseFramedForm( Model model, Controller controller, String title, Style style)
	{
		super(title, style);
		this.setCommandListener(this);
		this.model = model;
		this.controller = controller;
		this.setItemStateListener(this);
	}
	
	protected Controller getController()
	{
		return this.controller;
	}
	
	protected Model getModel()
	{
		return this.model;
	}
	
	public abstract byte getContext();
	
	public void handleEvent( byte context, int event, Object data)
	{
		//default this does nothing
		
		//#debug debug
		System.out.println("handleEvent:"+context+"/"+event+"/"+data);
	}
	
	public void commandAction(Command c, Displayable d) 
	{
		//#debug debug
		System.out.println("command:"+c.getLabel());
		
		if(c == cmdBack)
		{
			/*Should not go back to last checkpoint by default!!!*/
			getController().notifyEvent(Event.Context.NAVIGATION, Event.Navigation.BACK);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.ItemCommandListener#commandAction(de.enough.polish.ui.Command, de.enough.polish.ui.Item)
	 */
	public void commandAction(Command c, Item item)
	{
		commandAction(c, this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.FramedForm#handleKeyPressed(int, int)
	 */
	protected boolean handleKeyPressed(int keyCode, int gameAction)
	{
		//#if ${output.keystrokes}==true
		System.out.println(DebugUtils.addKeystrokePressed(keyCode));
		//#endif

		//#debug debug
		System.out.println("keyCode: "+keyCode+", gameAction: "+gameAction);
		
		/* NOTE: Do NOT use list index this.container.focusedIndex for input
		handling. The FramedForm super class handles this member specially and
		FramedForm.getCurrentItem() should therefore be used instead*/
		
		boolean handled = super.handleKeyPressed(keyCode, gameAction);

		//handle press of green call key, fires cmdGreenCallKey command
		
		//#ifdef polish.key.greencallkey:defined
		//#message support for green call key is ${polish.key.greencallkey}
		//#= if(keyCode == ${polish.key.greencallkey} && !handled){
		//#	return UiAccess.handleCommand(this, this.cmdGreenCallKey);
		//#= }else	
		//#endif
		
		//handle press of clear key, fires cmdClearKey command
		
		//#ifdef polish.key.clearkey:defined
		//#message support for clear call key is ${polish.key.clearkey}
		//#= if(keyCode == ${polish.key.clearkey} && !handled){
		//# UiAccess.handleCommand(this, this.cmdClearKey);
		//#= }
		//#elif polish.blackberry
		//#= if(keyCode == Keypad.KEY_DELETE && !handled){
		//# UiAccess.handleCommand(this, this.cmdClearKey);
		//#= }
		//#endif

		//pass input handling to 'topitem' if focused
		if( this.topItem != null && getCurrentItem() == this.topItem && !handled)
			if(UiAccess.handleKeyPressed(this.topItem, keyCode, gameAction))
				return true;
		
		//pass input handling to 'bottomitem' if focused
		if( this.bottomItem != null && getCurrentItem() == this.bottomItem && !handled)
			if(UiAccess.handleKeyPressed(this.bottomItem, keyCode, gameAction))
				return true;
		
		//Let FramedForm handle active frame switch 
		return handled;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.FramedForm#handleKeyRepeated(int, int)
	 */
	protected boolean handleKeyRepeated(int keyCode, int gameAction)
	{
		//#if ${output.keystrokes}==true
		//#debug keystroke
		System.out.println(DebugUtils.addKeystrokeRepeated(keyCode));
		//#endif
		
		
		//#ifdef testversion:defined
		if(keyCode == KEY_POUND)
		{
			Debug.showLog(StyleSheet.display);
			return true;
		}
		//#endif
		
		return super.handleKeyRepeated(keyCode, gameAction);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.FramedForm#handleKeyReleased(int, int)
	 */
	protected boolean handleKeyReleased(int keyCode, int gameAction)
	{
		NowPlus.notifyUserInteraction();
		
		//#if ${output.keystrokes}==true
		//#debug keystroke
		System.out.println(DebugUtils.addKeystrokeReleased(keyCode));
		//#endif
		//#if polish.blackberry
        if(Keypad.key(keyCode) == Keypad.KEY_MENU){
        	if(this instanceof BlackBerryOptionsHandler){
                if(((BlackBerryOptionsHandler)this).handleShowOptions()){
                	return true;
                }
        	}
        }
        //#endif
		return super.handleKeyReleased(keyCode, gameAction);
	}
	
	protected boolean handlePointerReleased(int x, int y) {
		NowPlus.notifyUserInteraction();
		return super.handlePointerReleased(x, y);
	}

	public boolean handlePointerTouchUp(int x, int y) {
		NowPlus.notifyUserInteraction();
		return super.handlePointerTouchUp(x, y);
	}

	/**
	 * Called when Screen is showed on screen the first time.
	 * This design ensures that extending classes have initialized
	 * local members before abstract construction methods createStaticItems(),
	 * createTopItems() and createBottomItems() are called.
	 * 
	 * Note: Do not overwrite this method unless you know what you are doing. Use abstract
	 * createContent() method to populate the Form instead.
	 */
	protected void create()
	{
		synchronized (this)
		{
			if(!this.isContentCreated)
			{
				this.staticTopItems = new Container(false);
				
				Item[] items = createStaticItems();
				
				if(items != null)
				{
					for (int i = 0; i < items.length; i++) {
						if(items[i] != null)
							this.staticTopItems.add(items[i]);
					}
				}
				
				//#debug debug
				System.out.println("Setting new static top items");
				
				append(Graphics.TOP, this.staticTopItems);
				
				this.topItem = createTopItem();
				
				this.bottomItem = createBottomItem();
				
				if(this.topItem != null)
				{
					append(Graphics.TOP, this.topItem);
					this.topItem.setItemCommandListener(this);
				}
				
				if(this.bottomItem != null)
				{
					append(Graphics.BOTTOM, this.bottomItem);
					this.bottomItem.setItemCommandListener(this);
				}
				
				//#if polish.blackberry
				//# setFocus(null);
				//#endif
				
				this.isContentCreated = true;
			}
		}
	}
	
	/**
	 * Overwrite showNotify to ensure that delayed construction is initiated
	 * when screen is showed on display the first time.
	 */
	public void showNotify() 
	{
		create();
		
		super.showNotify();
		
		//#if polish.blackberry.isTouchBuild == true
		DeviceControl.hideSoftKeyboard();
		//#endif
		
		//#if !polish.blackberry
		TextUtilities.loadTextFieldCharacterset(null); 
		//#endif

		//attach this screen as listener because we want notifications so we can update the UI
		this.model.attach(this);
	}
	
	/* moved to releaseResources()
	 * 
	public void hideNotify()
	{
		super.hideNotify();

		//Detach this screen as listener because we are no longer interested in notifications
		this.model.detach(this);
	}
	*/

	//FIXME remove for BlackBarry
	// //#if !polish.blackberry
	private void setFocus( Item item ) {
		forwardEventToNativeField(this, 0); // just a dummy call, so that the method is flagged as being used by an IDE
	// //#else
		// //#	public void setFocus( Item item ) {
		// //#	if (isMenuOpened() && item != null)
		// //#	{
		// //#		super.setFocus( item );
		// //#	}
		// //#	else
		// //#	{
		// //#
		// //#		if (getBottomItem() != null && 
		// //#			getBottomItem().getAppearanceMode() != Item.PLAIN) {
		// //#			super.setFocus( getBottomItem() );
		// //#		} 
		// //#		
		// //#		if (getTopItem() != null && 
		// //#				   getTopItem().getAppearanceMode() != Item.PLAIN) {
		// //#			super.setFocus( getTopItem() );
		// //#		}
		// //#	}
	// //#endif
	}
	
	//FIXME remove for BlackBarry
	// //#if !polish.blackberry
    private boolean forwardEventToNativeField(Screen screen, int keyCode) {
    	return false;
    // //#else
    // //#	protected boolean forwardEventToNativeField(Screen screen, int keyCode) {
	// //#	boolean forward = true;
	// //#	forward = super.forwardEventToNativeField( screen, keyCode );
	// //#	return forward && (getGameAction( keyCode ) != FIRE);
	// //#endif
    }
    
    protected abstract Item[] createStaticItems();
	
	protected abstract Item createTopItem();

	protected abstract Item createBottomItem();
	
	protected boolean isBottomItemAccessible()
	{
		return true;
	}

	public Item getBottomItem() {
		return this.bottomItem;
	}

	public Item getTopItem() {
		return this.topItem;
	}
	
	protected abstract String createCssSelector();
	
	public void add(Item item)
	{
		this.container.add(item);
	}

	public void remove(final Item item)
	{
		this.container.remove(item);
	}

	public Container content()
	{
		return this.container;
	}
	
	public Screen getParent() {
		return this.parent;
	}

	public void setParent(Screen parent) {
		this.parent = parent;
	}

	public boolean isInitialized() {
		return this.isContentCreated;
	}

	public void itemStateChanged(Item item) 
	{
		// just a placeholder
	}

	/* (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#requestInit()
	 */
	protected void requestInit() {
		this.isInitRequested = true;
	}
	
	public boolean isActive(Container container)
	{
		return this.currentlyActiveContainer == container;
	}

	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#releaseResources()
	 */
	public void releaseResources()
	{
		/* 
		 * Remember to lock Screen.paintLock to block painting routines
		 * while resources are being released
		 */
		synchronized (getPaintLock())
		{
			if (this.topItem!=null)
			{
				this.topItem.releaseResources();
			}
			if (this.bottomItem!=null ) 
			{
				this.bottomItem.releaseResources();
			}
			if (this.staticTopItems!=null)
			{
				this.staticTopItems.releaseResources();
			}
			
			//Detach this screen as listener because we are no longer interested in notifications
			this.model.detach(this);
			
			//make sure the class re-inits if shown again
			this.isContentCreated = false;
		}
		
		super.releaseResources();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.enough.polish.ui.Screen#releaseResources()
	 */
	public void destroy()
	{
		/* 
		 * Remember to lock Screen.paintLock to block painting routines
		 * while resources are being released
		 */
		synchronized (getPaintLock())
		{
			if (this.topItem!=null)
			{
				this.topItem.destroy();
				this.topItem = null;
			}
			if (this.bottomItem!=null ) 
			{
				this.bottomItem.destroy();
				this.bottomItem = null;
			}
			if (this.staticTopItems!=null) 
			{
				this.staticTopItems.destroy();
				this.staticTopItems=null;
			}
		}
		
		super.destroy();
	}
	
	/**
	 * Determines if the given keycode belongs to a keypad key
	 * @param keyCode the key code
	 * @param gameAction the associated game action
	 * @return true when the given key is a keycode	
	 */
	public boolean isKeyPadKey(int keyCode, int gameAction)
	{
		//#if polish.blackberry

		//# return isKeyPadKeyBlackBerry(keyCode, gameAction);
		
		//#else
		
		return(
			(keyCode >= 32 && gameAction == 0) ||
			(keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9) ||
			keyCode == Canvas.KEY_STAR ||
			keyCode == Canvas.KEY_POUND
			
			
			//#ifdef polish.key.clearsoftkey:defined
			//#= || keyCode == ${polish.key.clearsoftkey}
			//#endif			
			//#ifdef polish.key.backspace:defined
			//#= || keyCode == ${polish.key.backspace}
			//#endif
			);
		
		//#endif
		
		/*
		return 
			keyCode == KEY_NUM0 ||
			keyCode == KEY_NUM1 ||
			keyCode == KEY_NUM2 ||
			keyCode == KEY_NUM3 ||
			keyCode == KEY_NUM4 ||
			keyCode == KEY_NUM5 ||
			keyCode == KEY_NUM6 ||
			keyCode == KEY_NUM7 ||
			keyCode == KEY_NUM8 ||
			keyCode == KEY_NUM9 ||
			keyCode == KEY_STAR ||
			keyCode == KEY_POUND
			
			//#ifdef polish.key.clearkey:defined
			//#= || keyCode == ${polish.key.clearkey}
			//#endif
			//#ifdef polish.key.clearsoftkey:defined
			//#= || keyCode == ${polish.key.clearsoftkey}
			//#endif			
			//#ifdef polish.key.backspace:defined
			//#= || keyCode == ${polish.key.backspace}
			//#endif
			
			;
			*/
	}
	
	
	
	
	//#if polish.blackberry
	/**
	 * Checks if the parameter keys input belong to keypad.
	 * 
	 * Make sure to address the net.rim.device.api.ui.Keupad class of all supported
	 * BB version when expanding this method.
	 * 
	 * @param keyCode
	 * @param gameAction
	 * @return
	 */
	private boolean isKeyPadKeyBlackBerry(final int keyCode, final int gameAction)
	{
		//Handle J2ME specific keys
		if( keyCode >= Canvas.KEY_NUM0 && keyCode <= Canvas.KEY_NUM9)
			return true;
		else
			//Make sure directional keys are properly suppresed, needed to support arrow keys while emulating build
			if( (gameAction >= Canvas.UP && gameAction <= Canvas.FIRE)
					
					//supress delete key, handled by baseforms handleKeyPressed() methods
					//#ifdef polish.key.clearkey:defined
					//#= || keyCode == ${polish.key.clearkey}
					//#endif
					//#ifdef polish.key.clearsoftkey:defined
					//#= || keyCode == ${polish.key.clearsoftkey}
					//#endif			
					//#ifdef polish.key.backspace:defined
					//#= || keyCode == ${polish.key.backspace}
					//#endif
					
					)
				return false;
		
		int key = Keypad.key(keyCode);
		char c = Keypad.map(keyCode);
		
		//Handling BB keys that should be supressed
		if(
			
			key == KEY_LOCK ||	
			key == KEY_CAMERA_FOCUS ||
			key == KEY_FORWARD ||
			key == KEY_BACKWARD ||
				
			key == Keypad.KEY_SEND || //green call key
			key == Keypad.KEY_END || //red hangup key
			
			key == Keypad.KEY_ENTER ||
			key == Keypad.KEY_DELETE ||
			key == Keypad.KEY_BACKSPACE ||				
			
			key == Keypad.KEY_SPACE ||
		    key == Keypad.KEY_SHIFT_RIGHT ||
		  	key == Keypad.KEY_SHIFT_X ||
		  	key == Keypad.KEY_SHIFT_LEFT ||
			key == Keypad.KEY_ALT ||
			
			key == Keypad.KEY_MENU ||
			
			key ==  Keypad.KEY_ESCAPE ||
			key ==  Keypad.KEY_SPEAKERPHONE ||
			key ==  Keypad.KEY_BACKLIGHT ||
			
			key ==  Keypad.KEY_CONVENIENCE_1 ||
			key ==  Keypad.KEY_CONVENIENCE_2 ||
			
			key ==  Keypad.KEY_VOLUME_DOWN ||
			key ==  Keypad.KEY_VOLUME_UP
			)
		return false;
		
		//Check is key is char on BB keypad
		if(Keypad.isOnKeypad(c))
			return true;
		
		return false;
	}
	//#endif
	
//	/**
//	 * Temporary hack to make content behind menubar visible. Enough is 
//	 * working on an extension to make this possible via preprocessing.
//	 */
//	protected void calculateContentArea(int x, int y, int width, int height) 
//	{
//		super.calculateContentArea(x, y, width, height);
//		
//		this.contentHeight += Display.getScreenHeight() >> 2;
//	}
	
	//Following snippet of code makes a screen dump of what is being displayed and saves it as a file in default graphics directory
	//#if ${visualverification}==true
	//#public void paint(Graphics g)
	//#{
	//#	if(TestUtil.doPaint())
	//#	{
	//#		super.paint(TestUtil.getGraphics(g));
	//#		TestUtil.flush();
	//#	}
	//#	else
	//#		super.paint(g);
	//#}
	//#endif

	//#if polish.ignoreoddplatformrequest:defined
	// blocking odd multiple calls on samsung
	public void keyPressed(int key) {
		performedKeyPressed = true;
		super.keyPressed(key);
	}
	
	public void keyReleased(int key) {
		if (performedKeyPressed) {
			super.keyReleased(key);
			performedKeyPressed = false;
		}
	}
	//#endif
	
	//#if polish.Bugs.GreenCallKeyKeyMappedToFire 
	public int getGameAction(int keyCode) {
		int gameAction = super.getGameAction(keyCode);
		//#= if( gameAction == FIRE && keyCode == ${polish.key.greencallkey}){
			gameAction=0;
		//#= }
		return gameAction;
	}
	//#endif
}
