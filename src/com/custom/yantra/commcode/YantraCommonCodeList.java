/**
  * YantraCommonCode.java
  *
  **/

// PACKAGE
package com.custom.yantra.commcode;
import	com.custom.yantra.util.*;

import	com.yantra.yfc.dom.*;
import	com.yantra.interop.japi.YIFApi;
import	com.yantra.yfs.japi.YFSEnvironment;

import	java.util.*;

public class YantraCommonCodeList 
{
    public YantraCommonCodeList()
    {
		m_vecCommonCodeList = new Vector<YantraCommonCode>();
    }

	public	int					getCommonCodeCount ()	{ return m_vecCommonCodeList.size(); }
	public	YantraCommonCode	getCommonCode (int i)	{ return (YantraCommonCode)m_vecCommonCodeList.elementAt (i); }
	
	public	String	getCommonCodeList (String sCodeType) throws Exception
	{
		return getCommonCodeList (sCodeType, null);
	}	

	public	String	getCommonCodeList (String sCodeType, String sOrgCode) throws Exception
	{
		YFSEnvironment	env = YFSUtil.getYFSEnv();
		YIFApi			api = YFSUtil.getYIFApi();

		YFCDocument	docCommonCode = YFCDocument.createDocument("CommonCode");
		YFCElement	eleCommonCode = docCommonCode.getDocumentElement();

		// reset the collection		
		m_vecCommonCodeList.clear();
		eleCommonCode.setAttribute ("CodeType", sCodeType);

		if (sOrgCode != null)
			eleCommonCode.setAttribute ("OrganizationCode", sOrgCode);
			
		if (YFSUtil.getDebug())
		{
			System.out.println( "Input to getCommonCodeList() API: ");
			System.out.println( docCommonCode.getString());
		}
		docCommonCode = YFCDocument.getDocumentFor (api.getCommonCodeList (env, docCommonCode.getDocument()));
		if (YFSUtil.getDebug())
		{
			System.out.println( "Output from getCommonCodeList() API:");
			System.out.println( docCommonCode.getString());
		}
		eleCommonCode = docCommonCode.getDocumentElement ();
		Iterator<?>	iCommonCodes = eleCommonCode.getChildren ();
		while (iCommonCodes.hasNext ())
		{
			eleCommonCode = (YFCElement)iCommonCodes.next();
			YantraCommonCode	oCommonCode = new YantraCommonCode ();
			oCommonCode.setCommonCodeKey (eleCommonCode.getAttribute ("CodeKey"));
			oCommonCode.setCommonCodeType (eleCommonCode.getAttribute ("CodeType"));
			oCommonCode.setCommonCodeValue (eleCommonCode.getAttribute ("CodeValue"));
			oCommonCode.setCommonCodeDescription (eleCommonCode.getAttribute ("CodeShortDescription"));
			oCommonCode.setCommonCodeLongDescription (eleCommonCode.getAttribute ("CodeLongDescription"));
			m_vecCommonCodeList.add (oCommonCode);	
		}
		return docCommonCode.getString();
	}
		
	private	Vector<YantraCommonCode>	m_vecCommonCodeList;
}

