/**
  * setDebug.java
  *
  **/

// PACKAGE
package com.custom.yantra.api;

import	org.w3c.dom.*;
import	java.util.*;
import com.custom.yantra.util.*;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.*;

public class DemoExtnsDebugger implements YIFCustomApi
{
    public DemoExtnsDebugger()
    {
    }
	
	public Document setDebug (YFSEnvironment env, Document docIn)
	{
		YFCDocument	docDebugger = YFCDocument.getDocumentFor (docIn);
		YFCElement eleDebugger = docDebugger.getDocumentElement();
		YFSUtil.setDebug (eleDebugger.getBooleanAttribute ("DebugOn"));
		eleDebugger.setAttribute("DebugOn", YFSUtil.getDebug() ? "Y" : "N");
		return docDebugger.getDocument();
	}

	public Document getDebug (YFSEnvironment env, Document docIn)
	{
		YFCDocument	docDebugger = YFCDocument.getDocumentFor (docIn);
		YFCElement eleDebugger = docDebugger.getDocumentElement();
		eleDebugger.setAttribute ("DebugOn", YFSUtil.getDebug() ? "Y" : "N");
		return docDebugger.getDocument();
	}
	
	public Document getProperty (YFSEnvironment env, Document docIn)
	{
		YFCDocument	docProperty = YFCDocument.getDocumentFor(docIn);
		YFCElement	eleProperty = docProperty.getDocumentElement();
		eleProperty.setAttribute("Value", System.getProperty(eleProperty.getAttribute("Property")));
		return docIn;
	}

	/* (non-Javadoc)
	 * @see com.yantra.interop.japi.YIFCustomApi#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties pProp) throws Exception
	{
		mProp = pProp;
	}

	public Properties getProperties()
	{
		return mProp;
	}

	private Properties mProp;	
}

