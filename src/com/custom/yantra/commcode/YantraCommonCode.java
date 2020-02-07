/**
  * YantraCommonCode.java
  *
  **/

// PACKAGE
package com.custom.yantra.commcode;
import	java.io.*;

@SuppressWarnings("serial")
public class YantraCommonCode implements Serializable 
{
    public YantraCommonCode()
    {
		m_sCodeKey = "";
		m_sCodeType = "";
		m_sCodeValue = "";
		m_sCodeDescription = "";
		m_sCodeLongDescription = "";
    }

	public	String	getCommonCodeType()								{ return m_sCodeType; }
	public	void	setCommonCodeType(String sCodeType)				{ m_sCodeType = sCodeType; }
	public	String	getCommonCodeValue()							{ return m_sCodeValue; }
	public	void	setCommonCodeValue(String sCodeValue)			{ m_sCodeValue = sCodeValue; }
	public	String	getCommonCodeKey()								{ return m_sCodeKey; }
	public	void	setCommonCodeKey(String sCodeKey)				{ m_sCodeKey = sCodeKey; }
	public	String	getCommonCodeDescription()						{ return m_sCodeDescription; }
	public	void	setCommonCodeDescription(String sCodeDesc)		{ m_sCodeDescription = sCodeDesc; }
	public	String	getCommonCodeLongDescription()					{ return m_sCodeLongDescription; }
	public	void	setCommonCodeLongDescription(String sCodeDesc)	{ m_sCodeLongDescription = sCodeDesc; }
	
	private	String	m_sCodeKey;	
	private	String	m_sCodeType;
	private	String	m_sCodeValue;
	private	String	m_sCodeDescription;
	private String	m_sCodeLongDescription;
}

