package de.enough.polish.ui;

import javax.microedition.lcdui.Canvas;

/**
 * xTextField is extended de.enough.polish.ui.TextField class which provides 
 * additional handling of '+' character, which is located on star key together
 * with input mode change key. This made it impossible to enter plus using key
 * that is indicated visually. This class corrects this.
 * 
 * Use
 * <capability name="polish.device.requires.custom.textfield" value="true" />
 * to indicate your device needs to use this class
 * 
 * Use
 * <capability name="polish.device.requires.plus.on.starkey" value="true" />
 * to be able to add plus with starkey
 * 
 * @author jakub.kalisiak@mobica.com
 *
 */
public class xTextField extends de.enough.polish.ui.TextField {
	
	public xTextField (String label, String text, int maxSize, int constraints) {
		this( label, text, maxSize, constraints, null );
	}

	public xTextField( String label, String text, int maxSize, int constraints, Style style) {
		super(label,text,maxSize,constraints,style);
	}

	private boolean isNumeric(){
		int constraints = getConstraints();
		constraints = constraints & 0xffff;
		return (constraints == PHONENUMBER || constraints == DECIMAL || constraints == NUMERIC);
	}
	
	//#ifdef polish.device.requires.plus.on.starkey
	/**
	 * Adds '+' when at the beginning of a numeric field while input mode is numbers
	 * if Canvas.Key_Star is pressed, if key was not previously handled.
	 * 
	 */
	protected boolean handleKeyPressed( int keyCode, int gameAction ) {
		boolean handled = super.handleKeyPressed( keyCode, gameAction ); //invoke super
		if ((!handled)								//procede only if key was not yet handled
			&& this.isNumeric() 					//only allows to add plus on numeric fields
			&& this.inputMode == MODE_NUMBERS
			&& keyCode==Canvas.KEY_STAR				//when star is pressed
			&& getCaretPosition()==0				//on zero-th position
			){
			insertCharacter( '+', true, true );		//adding '+'
			handled=true;							//we signal input was handled							
		}
		return handled;
	}
	
	/**
	 * Adds '+' when Canvas.key_star is held over numeric field.
	 * Suppress '+' when canvas.key_num0 is held over numeric field.
	 * 
	 */
	protected boolean handleKeyRepeated( int keyCode, int gameAction ) {
		
		//Suppress TextField handling of '+' with zero here
		if ( this.isNumeric() || this.inputMode == MODE_NUMBERS ) {
			/*
				Initially '+' was added on long key press, 
				but new implementation is closer to the 
				native behaviour of the devices.
				Uncommented lines remain to suppress long
				key press of zero adding '+' (TextField Method)
			if (keyCode == Canvas.KEY_STAR && this.inputMode == PHONENUMBER){
				if (getCaretPosition() == 0){
					String str = getString();
					if (str.length()>0){
						if (str.charAt(0)!='+'){
							setString( "+"+str);
							return true;
						}
						//else
						//{
						//
						//}
					}
					else
					{
						setString( "+"+str);
						return true;
					}
				}
			}previous method of adding plus */
			if (keyCode == Canvas.KEY_NUM0)			//suppress '+' adding on repeat in TextField
				return false;
		}
		
		return super.handleKeyRepeated(keyCode,gameAction);
	}
	//#endif
	
	private boolean skipKeyReleasedEvent = false;
    
    private boolean isUneditable() {
        int constraints = getConstraints();
        return (constraints & UNEDITABLE) == UNEDITABLE;
    }
    
    //#if (polish.identifier==Nokia/6131-ZYB) || (polish.identifier==Nokia/6233-ZYB) || (polish.identifier==Nokia/6270-ZYB) || (polish.identifier==Nokia/6288-ZYB)
    public void commandAction(Command cmd, Displayable box) {
        super.commandAction(cmd, box);
        
        if (cmd == StyleSheet.CANCEL_CMD) {
            this.skipKeyReleasedEvent = true;
        } else if (!this.isUneditable()) {
            this.skipKeyReleasedEvent = true;
        }
    }
    //#endif
    
    protected boolean handleKeyReleased( int keyCode, int gameAction ) {
        //#if (polish.identifier==Nokia/6131-ZYB) || (polish.identifier==Nokia/6233-ZYB) || (polish.identifier==Nokia/6270-ZYB) || (polish.identifier==Nokia/6288-ZYB)
            if(this.skipKeyReleasedEvent) {
                this.skipKeyReleasedEvent = false;
                super.handleKeyReleased(keyCode,gameAction); 
            }
        //#endif

        //#if polish.vendor == LG
        /* workaround for the LGs which keeps caret on a correct position after KEY_STAR press */
        if (keyCode == Canvas.KEY_STAR && super.getInputMode() == MODE_NUMBERS) {
        	lastInputTime = 1001;
    	}
        //#endif
        
    	return super.handleKeyReleased(keyCode,gameAction); 
    }
}
