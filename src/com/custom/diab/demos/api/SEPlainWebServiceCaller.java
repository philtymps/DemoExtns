package com.custom.diab.demos.api;

import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.w3c.dom.Document;

import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;

public class SEPlainWebServiceCaller implements YIFCustomApi {

	public Document callService(Document inDoc) throws Exception {
		SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		SOAPConnection connection = soapConnectionFactory.createConnection();
		MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage message = factory.createMessage();
		message.getSOAPHeader().detachNode();
		SOAPBody body = message.getSOAPBody();
		body.addDocument(inDoc);
		URL endpoint = new URL(getProperties().getProperty("endpoint"));
		SOAPMessage response = connection.call(message, endpoint);
		connection.close();

		SOAPBody soapBody = response.getSOAPBody();
		if(soapBody.hasFault()){
			SOAPFault fault=soapBody.getFault();
			throw new Exception("SOAP Fault:"+fault.getFaultString());
		}
		Document responseDoc=YFCDocument.createDocument().getDocument();
		responseDoc.appendChild(responseDoc.importNode(soapBody.getFirstChild(), true));
		return responseDoc;
	}


	public void setProperties(Properties props) {
        this.props=props;
	}

	public Properties getProperties(){
		return this.props;
	}

	public static void main(String[] args) throws Exception {
		String file=args[0];
		DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder=factory.newDocumentBuilder();
		Document inDoc=builder.parse(file);
		SEPlainWebServiceCaller caller = new SEPlainWebServiceCaller();
		Properties props=new Properties();
		props.setProperty("endpoint", "http://dublr047vm:9080/YIFWebServiceBeanService/YIFWebServiceBean");
		caller.setProperties(props);
		Document outDoc=caller.callService(inDoc);
		System.out.println("OutDoc:"+YFCDocument.getDocumentFor(outDoc).getString());
	}
	
	private	Properties	props;
}
