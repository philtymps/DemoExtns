/**
  * DocumentContext.java
  *
  **/

// PACKAGE
package com.custom.yantra.api;

import java.util.Properties;
import java.util.Stack;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.ycp.japi.util.YCPInternalContext;
import com.yantra.yfc.dom.*;


import com.custom.yantra.util.*;
import org.w3c.dom.Document;

public class DocumentContext implements YIFCustomApi
{
	private Properties mProp;
		
	public Document pushDocument(YFSEnvironment env, Document docIn) throws Exception
	{
		DocumentStack	oDocumentStack = null;
		
		if (((YCPInternalContext)env).getUserObject () == null)		
		{
			oDocumentStack = new DocumentStack ();
			((YCPInternalContext)env).setUserObject ((Object)oDocumentStack);
		}
		else
			oDocumentStack = (DocumentStack)((YCPInternalContext)env).getUserObject();
		if (YFSUtil.getDebug())
		{
			System.out.println ("Pushing Document Onto Document Stack in YFSEnvironment's User Object:");
			System.out.println (YFCDocument.getDocumentFor (docIn).getString());
		}	
		return oDocumentStack.pushDocument(docIn);
	}

	public Document	popDocument (YFSEnvironment env, Document docIn) throws Exception
	{
		Document	docOut = null;
		if (((YCPInternalContext)env).getUserObject () != null)
		{
			DocumentStack oDocumentStack = (DocumentStack)((YCPInternalContext)env).getUserObject();
			docOut = oDocumentStack.popDocument();
		}
		if (docOut != null)
		{
			if (YFSUtil.getDebug())
			{
				System.out.println ("Popping Document From Document Stack in YFSEnvironment's User Object:");
				System.out.println (YFCDocument.getDocumentFor (docOut).getString());
			}			
		}
		else
		{
			// return an empty order document
			docOut = YFCDocument.createDocument ("Order").getDocument();
			if (YFSUtil.getDebug())
			{
				System.out.println ("WARNING - No Document On Document Stack and trying to Pop Document");
				System.out.println (YFCDocument.getDocumentFor (docOut).getString());
			}			
		}
		return docOut;
	}

	public Document	clearDocument (YFSEnvironment env, Document docIn) throws Exception
	{
		DocumentStack oDocumentStack =  (DocumentStack)((YCPInternalContext)env).getUserObject ();
		if (oDocumentStack != null)
			((YCPInternalContext)env).setUserObject (null);
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
	
	@SuppressWarnings({ "serial", "rawtypes" })
	protected class DocumentStack extends Stack
	{	
		@SuppressWarnings("unchecked")
		public	Document	pushDocument	(Document docIn) {
			return (Document)push ((Object)docIn);
			
		}
		
		public	Document	popDocument	() {
			return (Document) pop ();
		}
	}	
}
