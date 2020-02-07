/*
 * MSMQJNIApi.java
 *
 * Created on July 17, 2001, 12:04 PM
 */

package com.custom.dal;


public class cashdrawer{
    
	/**
	 *  Load MSMQJNI.dll
	 */
	static{
		System.loadLibrary("ycashdrawer");
	}
    
	/**
	 *  get instance of YFCLogCategory to be passed to the native methods for logging.....
	 */
	public native void init();
    
	/**
	 *  Native method to release the Transaction
	 */
	public native void openDrawer() throws Exception;
   
	/**
	 *  Native method to commit the transaction
	 */
	public native void release() throws Exception;

	public static void main(String[] args) throws Exception{
		cashdrawer c = new cashdrawer();
		c.init();
		c.openDrawer();
		c.release();
	}
}