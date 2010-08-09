package com.zyb.nowplus.data.protocol.hessian;

public interface HessianObjectCallback {
	public void notifyHessianObject(String key, int pos, Object val);
}