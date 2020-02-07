package com.custom.diab.demos.ue;

import com.custom.yantra.util.YFSUtil;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.ycs.japi.ue.YCSshipCartonUserExit;
import com.yantra.yfs.japi.YFSUserExitException;

public class SEShipCartonUEImpl implements YCSshipCartonUserExit {

	@Override
	public String shipCarton(YFSContext arg0, String arg1)
			throws YFSUserExitException {
		// TODO Auto-generated method stub
		if (YFSUtil.getDebug()){
			System.out.println("SEShipCartonUserExitImpl: in Method shipCarton: Argument="+arg1);
		}
		return null;
	}

	@Override
	public boolean shipCartonContinue(YFSContext arg0, String arg1)
			throws YFSUserExitException {
		// TODO Auto-generated method stub
		if (YFSUtil.getDebug()){
			System.out.println("SEShipCartonUserExitImpl : in Method shipCartonContinue: Argument="+arg1);
		}
		return false;
	}

	@Override
	public String shipCartonOutXML(YFSContext arg0, String arg1)
			throws YFSUserExitException {
		if (YFSUtil.getDebug()){
			System.out.println("SEShipCartonUserExitImpl : in Method shipCartonOutputXML: Argument="+arg1);
		}
		String sTrackingNumber = String.valueOf(System.currentTimeMillis());
		// TODO Auto-generated method stub
		String strOut = "<ShipCarton BilledWeight='3' NetCharge='3' TrackingNumber='"+sTrackingNumber+"' PierbridgeLabelURL='http://sterlingbda:7002/diab/images/CartonLabel.pdf'></ShipCarton>";

		return strOut;
	}

}
