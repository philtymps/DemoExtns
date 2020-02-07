/**
  * ConvertFlatFileToXmlServlet.java
  *
  **/

// PACKAGE
package com.custom.yantra.servlet;

import javax.servlet.http.*;

import java.io.*;

import com.haht.xml.textdata.*;
import com.custom.yantra.xmlmapper.*;


/**
  * 
  * 
  * The default is extends javax.servlet.http.HttpServlet.
  * You could use the extends javax.servlet.GenericServlet.
  *
  **/
@SuppressWarnings("serial")
public class ConvertFlatFileToXmlServlet extends javax.servlet.http.HttpServlet
{
    public ConvertFlatFileToXmlServlet()
    {
    }
	
	public void doPost (HttpServletRequest aRequest, HttpServletResponse aResponse)
	{
		try {
			PrintWriter	out = aResponse.getWriter();
			String		sParamsDoc = null;
			
			if (aRequest.getParameter ("PARAMS") != null)
				sParamsDoc = aRequest.getParameter ("PARAMS");
			else
				sParamsDoc = new String (new TextData (aRequest.getInputStream()).getBytes());
			ConvertFlatFileToXml conv = new ConvertFlatFileToXml ();
			out.print (conv.convert(sParamsDoc));
		} catch (Exception e) {
			
		}
	}	
}


