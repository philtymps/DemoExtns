package com.custom.diab.demos.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import org.w3c.dom.Document;

import com.custom.yantra.util.YFSUtil;
import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class SESendTextMessage implements YIFCustomApi {

	
	protected			Properties	m_Props;
	protected final 	String USER_AGENT = "Mozilla/5.0";
	protected final		String GET_URL = "https://localhost:9090/SpringMVCExample";
	protected final		String POST_URL = "https://textbelt.com/text";
	protected final		String POST_PARAMS = "key=4a359167bce8fcc4dae5dc9b82c532f3eedfb598mmjH7AHNFchE3k7eFGxMdz2lO";


	public SESendTextMessage() {
		// TODO Auto-generated constructor stub
	}

	public Document SendTextMessage (YFSEnvironment env, Document docIn) throws YFSException
	{
		try { 
			sendPOST (m_Props.getProperty("phone"), m_Props.getProperty("message"));
		} catch (Exception e) {
			throw new YFSException (e.getMessage());
		}
		return docIn;

	}	

	private void sendPOST(String sPhone, String sMessage) throws IOException {
		URL obj = new URL(POST_URL);
		HttpURLConnection		con = (HttpURLConnection) obj.openConnection();
		StringBuilder			sPostVariables = new StringBuilder (POST_PARAMS);
		sPostVariables.append('&');
		sPostVariables.append("phone=" + sPhone);
		sPostVariables.append('&');
		sPostVariables.append("message="+ sMessage);
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		
		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		//os.write(Base64.getEncoder().encode(sPostVariables.toString().getBytes()));
		os.write(sPostVariables.toString().getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request not worked");
		}
	}
	
	private void sendGET() throws IOException {
		URL obj = new URL(GET_URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("GET request not worked");
		}

	}
	
	public	void	setProperties (Properties props)
	{
		m_Props = props;
	}
}
