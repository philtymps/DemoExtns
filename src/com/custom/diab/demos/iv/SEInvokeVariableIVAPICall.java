package com.custom.diab.demos.iv;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.date.YDate;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class SEInvokeVariableIVAPICall implements YIFCustomApi
{

	private Properties	m_Props;
	private HashMap<String, YFCElement> variables;
	
	public SEInvokeVariableIVAPICall() {
		// TODO Auto-generated constructor stub
	}
	
	
	public Document invokeIVApiFromFile(YFSEnvironment env, Document inputDoc)
	{
		variables = new HashMap<String, YFCElement>();
		YFCDocument dInput = YFCDocument.getDocumentFor(inputDoc);
		YFCElement eInput = dInput.getDocumentElement();
		String sApiName = eInput.getAttribute("ApiName");
		boolean isService = eInput.getBooleanAttribute("IsService");
		String sFileName = eInput.getAttribute("FileName");
		YFCElement eVariables = eInput.getChildElement("Variables");
		loadVariableFile(env);
		if(!YFCCommon.isVoid(eVariables)){
			for(YFCElement eVariable : eVariables.getChildren()){
				addVariable(eVariable.getAttribute("Name"), eVariable);
			}
		}
		if (!YFCCommon.isVoid(sApiName) && !YFCCommon.isVoid(sFileName)){
			File tmp = new File(sFileName);
			if (tmp.exists()){
				YFCDocument dFileInput = YFCDocument.getDocumentForXMLFile(sFileName);
	
				if (YFSUtil.getDebug())
				{
					System.out.println ("XML Before Variables Processed");
					System.out.println (dFileInput.getString());
				}
				dFileInput = replaceVariables(dFileInput);
				if (YFSUtil.getDebug())
				{
					System.out.println ("XML After Variables Processed");
					System.out.println (dFileInput.getString());
				}
				if(isService) {
					return callServiceOrApi(env, dFileInput.getDocument(), sApiName, true);
				} else {
					return callServiceOrApi(env, dFileInput.getDocument(), sApiName, false);
				}
			
			}
		} else if (!YFCCommon.isVoid(eInput.hasChildNodes())) {
			YFCDocument dOutput = YFCDocument.createDocument("Output");
			YFCElement eOutput = dOutput.getDocumentElement();
			for (YFCElement eApi : eInput.getChildren()){
				sApiName = eApi.getAttribute("ApiName");
				isService = eInput.getBooleanAttribute("IsService");
				sFileName = eApi.getAttribute("FileName");
				if (!YFCCommon.isVoid(sApiName) && !YFCCommon.isVoid(sFileName)){
					File tmp = new File(sFileName);
					if (tmp.exists()){
						YFCDocument dFileInput = YFCDocument.getDocumentForXMLFile(sFileName);
						if (YFSUtil.getDebug())
						{
							System.out.println ("XML Before Variables Processed");
							System.out.println (dFileInput.getString());
						}
						dFileInput = replaceVariables(dFileInput);
						if (YFSUtil.getDebug())
						{
							System.out.println ("XML After Variables Processed");
							System.out.println (dFileInput.getString());
						}
						Document temp;
						if(isService) {
							temp = callServiceOrApi(env, dFileInput.getDocument(), sApiName, true);
						} else {
							temp = callServiceOrApi(env, dFileInput.getDocument(), sApiName, false);
						}
						YFCElement eApiOut = eOutput.createChild("Api");
						eApiOut.setAttribute("API", sApiName);
						if (!YFCCommon.isVoid(temp)){
							eApiOut.importNode(YFCDocument.getDocumentFor(temp).getDocumentElement());
						}
					
					}
				}
			}
			return dOutput.getDocument();
		}
		return null;
	}

	public YFCDocument replaceVariables (YFCDocument docInput)
	{
		String		sInput = docInput.getString();

		int	iStart = sInput.indexOf("#{");
		int	iEnd = sInput.indexOf("}", iStart);

		while (iStart > -1 && iEnd > -1) {
			String sVariable = sInput.substring(iStart, iEnd+1).trim();
			String content = sVariable.substring(sVariable.indexOf("#{") + 2, sVariable.indexOf("}")).trim();
			System.out.println ("Variable Expression Found:" + sVariable);
			//System.out.println ("Variable To Evaluate:" + content);
			
			if(content.startsWith("NOW")){
				YDate now = YDate.newDate();
				if(YFCCommon.equals(content, "NOW")){
					sInput = sInput.substring(0, iStart)+now.getString()+sInput.substring(iEnd+1);
					//eParent.setAttribute(sAttribute, now);
				} else if (content.contains("+")) {
					String[] args = content.split("[+]");
					int days = Integer.parseInt(args[1]);
					sInput = sInput.substring(0, iStart)+YDate.newDate(now, days).getString()+sInput.substring(iEnd+1);
					//eParent.setAttribute(sAttribute, YDate.newDate(now, days));
				} else {
					String[] args = content.split("[-]");
					int days = Integer.parseInt(args[1]);
					sInput = sInput.substring(0, iStart)+YDate.newDate(now, days).getString()+sInput.substring(iEnd+1);
					// eParent.setAttribute(sAttribute, YDate.newDate(now, (days * -1)));
				}
			} else if (content.startsWith("TODAY")) {
				Calendar now = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				if (content.contains("+")) {
					String[] args = content.split("[+]");
					int days = Integer.parseInt(args[1]);
					now.add(Calendar.DATE, days);
				} else if(content.contains("-")) {
					String[] args = content.split("[-]");
					int days = Integer.parseInt(args[1]);
					now.add(Calendar.DATE, (days * -1));
				}
				sInput = sInput.substring(0, iStart)+sdf.format(now.getTime())+sInput.substring(iEnd+1);
				//String newValue = sVariable.replaceAll("#\\{" + content + "\\}", sdf.format(now.getTime()));
				//eParent.setAttribute(sAttribute, newValue);
			/*} 
			else if(content.startsWith("MINUTE")){
				Calendar now = Calendar.getInstance();
				if(YFCCommon.equals(content, "MINUTE")){
					eParent.setAttribute(sAttribute, YDate.newDate(now.getTimeInMillis()));
				} else if (content.contains("+")) {
					String[] args = content.split("[+]");
					int days = Integer.parseInt(args[1]);
					eParent.setAttribute(sAttribute, YDate.newDate(now.getTimeInMillis() + (days * 60000)));
				} else {
					String[] args = content.split("[-]");
					int days = Integer.parseInt(args[1]);
					eParent.setAttribute(sAttribute, YDate.newDate(now.getTimeInMillis() - (days * 60000)));
				}
			*/
			} else if (variables.containsKey(content)){
				try {
					System.out.println ("Variable Value: " + variables.get(content).getAttribute("Value"));
					sInput = sInput.substring(0, iStart)+variables.get(content).getAttribute("Value") + sInput.substring(iEnd+1);
					//eParent.setAttribute(sAttribute, newValue);
				} catch (Exception e){
					throw new YFSException (e.getMessage());
				}
			} else {
				sInput = sInput.substring(0, iStart)+content+sInput.substring(iEnd+1);
				//eParent.setAttribute(sAttribute, content);
			}
			iStart = sInput.indexOf("#{");
			iEnd   = sInput.indexOf("}", iStart);
		}
		return YFCDocument.getDocumentFor(sInput);
	}
	
	private void loadVariableFile(YFSEnvironment env){
		YFCDocument temp = YFCDocument.getDocumentForXMLFile(getVariableFile(env));
		for (YFCElement eChild : temp.getDocumentElement().getChildren()){
			addVariable(eChild.getAttribute("Name"), eChild);
		}
	}

	private void addVariable(String sName, YFCElement sValue){
		if(YFCCommon.isVoid(variables)){
			variables = new HashMap<String, YFCElement>();
		}
		variables.put(sName, sValue);
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_Props = props;
	}

	private String getVariableFile(YFSEnvironment env)
	{
		/*if (!YFCCommon.isVoid(getProperty("variableFile"))){
			return (String) getProperty("variableFile");
		}*/
		return getScriptsPath(env) + "/variables.xml";
	}

	protected String getScriptsPath(YFSEnvironment env)
	{
		String path = getPropertyValue (env, "bda.scripts.filepath");
		if (YFCCommon.isVoid(path)) {
			path = "/var/oms/Scripts";
		}
		return path;
	}
	
	protected String getPropertyValue(YFSEnvironment env, String sProperty)
	{
		YFCDocument docInput = YFCDocument.createDocument("GetProperty");
		YFCElement  eleInput = docInput.getDocumentElement();
		eleInput.setAttribute("PropertyName", sProperty);

		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			Document dResponse = api.getProperty(env, docInput.getDocument());
			return dResponse.getDocumentElement().getAttribute("PropertyValue");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected Document callServiceOrApi(YFSEnvironment env, Document docIn, String sServiceName, boolean bIsService)
	{
		try {
			YIFApi		api = YIFClientFactory.getInstance().getLocalApi();
			Document dResponse;
			if (bIsService)
					dResponse = api.executeFlow(env, sServiceName, docIn);
			else
					dResponse = api.invoke(env, sServiceName, docIn);
			return dResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
