package com.custom.dal;

//import com.yantra.omp.api.YFSApiImpl;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import java.util.Properties;

import org.w3c.dom.Document;
import com.custom.dal.cashdrawer;

public class dalTileAPI implements YIFCustomApi {

	public Document openCashDrawer(YFSEnvironment env, Document inDoc) throws YFSException{
		System.out.println("########### openCashDrawer Called. #############");

		cashdrawer c = new cashdrawer();
		c.init();
		try {
			c.openDrawer();
			c.release();
		} catch (Exception e) {
			e.printStackTrace();
			throw new YFSException (e.getMessage());
		}
		return inDoc;
	}

	@Override
	public void setProperties(Properties props) throws Exception {
		// TODO Auto-generated method stub
		m_props = props;
	}
	
	public Properties getProperties() {
		return m_props;
	}

	private Properties m_props;
}

	

