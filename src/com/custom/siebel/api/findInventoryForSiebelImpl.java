/**
  * findInventoryForSiebel.java
  *
  **/

// PACKAGE
package com.custom.siebel.api;

import com.custom.yantra.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSException;

import com.custom.siebel.shoppingcart.*;

import org.w3c.dom.*;
import java.util.*;

public class findInventoryForSiebelImpl implements YIFCustomApi
{
    public findInventoryForSiebelImpl()
    {
    }

	public Document findInventoryForSiebel (YFSEnvironment env, Document inDoc) throws YFSException
	{
		try {
			if (YFSUtil.getDebug())
			{
				System.out.println ("Input to findInventoryForSiebel() API is: ");
				System.out.println (YFSXMLUtil.getXMLString (inDoc));
			}
			SiebelShoppingCart	oShoppingCart = new SiebelShoppingCart();
			Document outDoc = oShoppingCart.findInventory (env, inDoc);						
			if (YFSUtil.getDebug())
			{
				System.out.println ("Output from findInventoryForSiebel() API is: ");
				System.out.println (YFSXMLUtil.getXMLString (outDoc));
			}
			return outDoc;
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
	}
		
	public void setProperties(Properties pProp) throws Exception
	{
		mProp = pProp;
	}
	
	public Properties getProperties() {
		return mProp;
	}

	private Properties mProp;
}

