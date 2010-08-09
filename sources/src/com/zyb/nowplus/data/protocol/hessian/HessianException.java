package com.zyb.nowplus.data.protocol.hessian;

public class HessianException extends Exception {

	private static final long serialVersionUID = 1L;
	private String code;
	private Object detail;

	public HessianException(String icode, String message, Object detail) {
		super(message);
		code = icode;
		this.detail = detail;
	}

	public HessianException(String icode, String message) {
		super(message);
		code = icode;
	}

	public String getCode() {
		return code;
	}

	public Object getDetail() {
		return detail;
	}

	//#mdebug error
	public String toString() {
		return "Error " + code + ": " + getMessage();
	}
	//#enddebug

}