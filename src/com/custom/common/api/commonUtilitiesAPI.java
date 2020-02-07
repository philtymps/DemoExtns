package com.custom.common.api;

import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.ycp.japi.util.YCPInternalContext;
import com.yantra.interop.japi.YIFCustomApi;
import com.yantra.interop.japi.YIFApi;
import com.custom.yantra.util.*;
import org.w3c.dom.Document;
import java.util.*;

public class commonUtilitiesAPI implements YIFCustomApi {

	private Properties _properties = null;

	public void setProperties(Properties prop) throws Exception {
        _properties = prop;
    }
	public Properties getProperties() {
		return _properties;
	}
    public commonUtilitiesAPI()
    {
    }

	protected class orderVars {
			public	orderVars () {}

			String		sOrderHeaderKey;
	};

	public Document prepareToChangePaymentStatus(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement orderElem = yfcInDoc.getDocumentElement();

		//set orderVars for later...
		orderVars	bVars = new orderVars();
		bVars.sOrderHeaderKey = orderElem.getAttribute("OrderHeaderKey");
		((YCPInternalContext)env).setUserObject(bVars);

		System.out.println(">>>>>>>>>>>>>>>>>> Order document below....");
		System.out.println(orderElem);

		YFCDocument changeOrderDoc = YFCDocument.createDocument ("Order");
		YFCElement changeOrderElem = changeOrderDoc.getDocumentElement();

		changeOrderElem.setAttribute("OrderHeaderKey",orderElem.getAttribute("OrderHeaderKey"));
		changeOrderElem.setAttribute("PaymentStatus","AUTHORIZED");

		System.out.println(">>>>>>>>>>>>>>>>>> Input to changeOrder below....");
		System.out.println(changeOrderElem);

		return changeOrderDoc.getDocument();
	}

	public Document prepareToScheduleOrder(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement orderElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Order document below....");
		System.out.println(orderElem);

		YFCDocument multiApiDoc = YFCDocument.createDocument ("MultiApi");
		YFCElement multiApiElem = multiApiDoc.getDocumentElement();
		YFCElement apiElem = multiApiElem.createChild("API");
		YFCElement inputElem = apiElem.createChild("Input");
		YFCElement scheduleOrderElem = inputElem.createChild("ScheduleOrder");

		apiElem.setAttribute("Name","scheduleOrder");

		scheduleOrderElem.setAttribute("OrderHeaderKey",orderElem.getAttribute("OrderHeaderKey"));
		scheduleOrderElem.setAttribute("ScheduleAndRelease","Y");

		System.out.println(">>>>>>>>>>>>>>>>>> Input to multiApi below....");
		System.out.println(multiApiElem);

		return multiApiDoc.getDocument();
	}

	public Document prepareToGetOrderReleaseList(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement multiApiElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Incomming multiApiDoc is ...");
		System.out.println(multiApiElem);

		String orderHeaderKey = "";
		orderVars	bVars = (orderVars)((YCPInternalContext)env).getUserObject();
		if (bVars != null) {
			orderHeaderKey = bVars.sOrderHeaderKey;
			System.out.println("***************orderHeaderKey is " + orderHeaderKey);
		}

		YFCDocument orderDoc = YFCDocument.createDocument ("OrderRelease");
		YFCElement orderElem = orderDoc.getDocumentElement();

		orderElem.setAttribute("OrderHeaderKey",orderHeaderKey);

		System.out.println(">>>>>>>>>>>>>>>>>> Input to getOrderDetails below....");
		System.out.println(orderElem);

		return orderDoc.getDocument();
	}

	public Document prepareToConsolidateToShipment(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement orderReleaseListElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> OrderReleaseList document below ...");
		System.out.println(orderReleaseListElem);

		YFCDocument multiApiDoc = YFCDocument.createDocument ("MultiApi");
		YFCElement multiApiElem = multiApiDoc.getDocumentElement();

		for (Iterator<?> i = orderReleaseListElem.getChildren(); i.hasNext();) {
			YFCElement orderReleaseElem = (YFCElement)i.next();
			YFCElement apiElem = multiApiElem.createChild("API");
			YFCElement inputElem = apiElem.createChild("Input");
			YFCElement inputOrderReleaseElem = inputElem.createChild("OrderRelease");
			apiElem.setAttribute("Name","consolidateToShipment");
			inputOrderReleaseElem.setAttribute("OrderReleaseKey",orderReleaseElem.getAttribute("OrderReleaseKey"));
			inputOrderReleaseElem.setAttribute("DoNotConsolidate","Y");
			inputOrderReleaseElem.setAttribute("OrderType", orderReleaseElem.getChildElement("Order").getAttribute ("OrderType"));
		}

		System.out.println(">>>>>>>>>>>>>>>>>> Input to multiApi below....");
		System.out.println(multiApiDoc);

		return multiApiDoc.getDocument();
	}

	public Document prepareToCreateTOShipment(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement orderReleaseListElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> OrderReleaseList document below ...");
		System.out.println(orderReleaseListElem);

		YFCDocument multiApiDoc = YFCDocument.createDocument ("MultiApi");
		YFCElement multiApiElem = multiApiDoc.getDocumentElement();

		for (Iterator<?> i = orderReleaseListElem.getChildren(); i.hasNext();) {
			YFCElement orderReleaseElem = (YFCElement)i.next();
			YFCElement apiElem = multiApiElem.createChild("API");
			YFCElement inputElem = apiElem.createChild("Input");
			apiElem.setAttribute("Name","createShipment");

			YFCElement shipmentElem = inputElem.createChild("Shipment");
			shipmentElem.setAttribute("Action","Create");
			shipmentElem.setAttribute("DocumentType","0006");
			shipmentElem.setAttribute("IgnoreOrdering","Y");

			YFCElement inputOrderReleasesElem = shipmentElem.createChild("OrderReleases");
			YFCElement inputOrderReleaseElem = inputOrderReleasesElem.createChild("OrderRelease");

			inputOrderReleaseElem.setAttribute("AssociationAction","Add");
			inputOrderReleaseElem.setAttribute("OrderReleaseKey",orderReleaseElem.getAttribute("OrderReleaseKey"));

		}

		System.out.println(">>>>>>>>>>>>>>>>>> Input to multiApi below....");
		System.out.println(multiApiDoc);

		return multiApiDoc.getDocument();
	}

	public Document prepareToChangeShipmentStatus(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement shipmentElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Shipment document below....");
		System.out.println(shipmentElem);

		YFCDocument changeShipmentStatusDoc = YFCDocument.createDocument ("Shipment");
		YFCElement changeShipmentStatusElem = changeShipmentStatusDoc.getDocumentElement();
		
		changeShipmentStatusElem.setAttribute("TransactionId","SENT_TO_NODE.0001");
		changeShipmentStatusElem.setAttribute("ShipmentKey",shipmentElem.getAttribute("ShipmentKey"));

		System.out.println(">>>>>>>>>>>>>>>>>> Input to changeShipmentStatus below....");
		System.out.println(changeShipmentStatusElem);
	
		return changeShipmentStatusDoc.getDocument();
	}


	public Document prepareToChangeTOShipmentStatus(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement shipmentElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Shipment document below....");
		System.out.println(shipmentElem);

		YFCDocument changeShipmentStatusDoc = YFCDocument.createDocument ("Shipment");
		YFCElement changeShipmentStatusElem = changeShipmentStatusDoc.getDocumentElement();
		
		changeShipmentStatusElem.setAttribute("TransactionId","SENT_TO_NODE.0006");
		changeShipmentStatusElem.setAttribute("ShipmentKey",shipmentElem.getAttribute("ShipmentKey"));

		System.out.println(">>>>>>>>>>>>>>>>>> Input to changeShipmentStatus below....");
		System.out.println(changeShipmentStatusElem);
	
		return changeShipmentStatusDoc.getDocument();
	}

	//this method will be called onsuccess of confirming the return...
	public Document prepareToScheduleReturn(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement orderElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Order document below....");
		System.out.println(orderElem);

		YFCDocument multiApiDoc = YFCDocument.createDocument ("MultiApi");
		YFCElement multiApiElem = multiApiDoc.getDocumentElement();
		YFCElement apiElem = multiApiElem.createChild("API");
		YFCElement inputElem = apiElem.createChild("Input");
		YFCElement scheduleOrderElem = inputElem.createChild("ScheduleOrder");

		apiElem.setAttribute("Name","scheduleOrder");

		scheduleOrderElem.setAttribute("OrderHeaderKey",orderElem.getAttribute("OrderHeaderKey"));
		scheduleOrderElem.setAttribute("ScheduleAndRelease","Y");

		System.out.println(">>>>>>>>>>>>>>>>>> Input to multiApi below....");
		System.out.println(multiApiElem);

		return multiApiDoc.getDocument();
	}

	//this method will be called onsuccess of confirming the shipment...
	public Document prepareToCreateShipmentInvoice(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement passedShipmentElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Passed Shipment document below....");
		System.out.println(passedShipmentElem);

		YFCDocument shipmentDoc = YFCDocument.createDocument ("Shipment");
		YFCElement shipmentElem = shipmentDoc.getDocumentElement();

		shipmentElem.setAttribute("ShipmentKey",passedShipmentElem.getAttribute("ShipmentKey"));

		shipmentElem.setAttribute("TransactionId","CREATE_SHMNT_INVOICE.0001");

		System.out.println(">>>>>>>>>>>>>>>>>> Input to createShipmentInvoice below....");
		System.out.println(shipmentElem);

		return shipmentDoc.getDocument();
	}

	public Document prepareToCreateReturnInvoice(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement inputElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> input Doc below for prepareToCreateReturnInvoice ....");
		System.out.println(inputElem);

		YFCDocument orderDoc = YFCDocument.createDocument ("Order");
		YFCElement orderElem = orderDoc.getDocumentElement();
		orderElem.setAttribute("TransactionId","CREATE_ORDER_INVOICE.0003");

		YFCElement receiptLinesElem = inputElem.getChildElement("ReceiptLines");
		if (receiptLinesElem != null) {
			for (Iterator<?> i = receiptLinesElem.getChildren(); i.hasNext();) {
				YFCElement receiptLineElem = (YFCElement)i.next();
				orderElem.setAttribute("OrderHeaderKey",receiptLineElem.getAttribute("OrderHeaderKey"));

			}
		}

		System.out.println(">>>>>>>>>>>>>>>>>> Input to createOrderInvoice below....");
		System.out.println(orderElem);

		return orderDoc.getDocument();
	}

	public Document prepareToRetrieveShipment(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement passedShipmentElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Passed Shipment document below....");
		System.out.println(passedShipmentElem);

		YFCDocument shipmentDoc = YFCDocument.createDocument ("Shipment");
		YFCElement shipmentElem = shipmentDoc.getDocumentElement();

		shipmentElem.setAttribute("ShipmentKey",passedShipmentElem.getAttribute("ShipmentKey"));

		System.out.println(">>>>>>>>>>>>>>>>>> Input to retrieveShipment below....");
		System.out.println(shipmentElem);

		return shipmentDoc.getDocument();
	}


	public Document prepareToGetTaskList(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement moveRequestElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Move Request document below....");
		System.out.println(moveRequestElem);

		YFCDocument taskDoc = YFCDocument.createDocument ("Task");
		YFCElement taskElem = taskDoc.getDocumentElement();
		YFCElement taskReferencesElem = taskElem.createChild("TaskReferences");

		taskReferencesElem.setAttribute("ShipmentKey",moveRequestElem.getAttribute("ShipmentKey"));

		System.out.println(">>>>>>>>>>>>>>>>>> Input to getTaskList below....");
		System.out.println(taskElem);

		return taskDoc.getDocument();
	}

	public Document prepareToCreateBatch(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement taskListElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> TaskList document below....");
		System.out.println(taskListElem);

		YFCDocument batchDoc = YFCDocument.createDocument ("Batch");
		YFCElement batchElem = batchDoc.getDocumentElement();
		YFCElement tasksElem = batchElem.createChild("Tasks");

		if (taskListElem != null) {
			for (Iterator<?> i = taskListElem.getChildren(); i.hasNext();) {
				YFCElement taskElem = (YFCElement)i.next();
				YFCElement newTaskElem = tasksElem.createChild("Task");
				newTaskElem.setAttribute("TaskId",taskElem.getAttribute("TaskId"));
				batchElem.setAttribute("TaskType",taskElem.getAttribute("TaskType"));
				batchElem.setAttribute("OrganizationCode",taskElem.getAttribute("OrganizationCode"));

				YFCElement	taskReferencesElem = taskElem.getChildElement ("TaskReferences");
				if (taskReferencesElem != null) {
					batchElem.setAttribute("ShipmentNo",taskReferencesElem.getAttribute("ShipmentNo"));
				}
			}
		}

		System.out.println(">>>>>>>>>>>>>>>>>> Input to createBatch below....");
		System.out.println(batchElem);

		return batchDoc.getDocument();
	}

	public Document prepareToPrintTaskList(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement inputBatchElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Incomming Batch document below....");
		System.out.println(inputBatchElem);

		YFCDocument printDoc = YFCDocument.createDocument ("Print");
		YFCElement printElem = printDoc.getDocumentElement();
		YFCElement batchElem = printElem.createChild("Batch");
		YFCElement printerPreferencesElem = printElem.createChild("PrinterPreferences");

		batchElem.setAttribute("BatchNo",inputBatchElem.getAttribute("BatchNo"));
		batchElem.setAttribute("Node",inputBatchElem.getAttribute("Node"));
		batchElem.setAttribute("ActivityGroupId","RETRIEVAL");

		printerPreferencesElem.setAttribute("OrganizationCode",inputBatchElem.getAttribute("OrganizationCode"));
		printerPreferencesElem.setAttribute("UserId",env.getUserId());

		System.out.println(">>>>>>>>>>>>>>>>>> Input to PrintTaskList below....");
		System.out.println(printElem);

		return printDoc.getDocument();
	}

	public Document prepareToPrintTaskList2(YFSEnvironment env, Document inDoc) {

		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement batchListElem = yfcInDoc.getDocumentElement();

		System.out.println(">>>>>>>>>>>>>>>>>> Incomming Batch List document below....");
		System.out.println(batchListElem);
		YFCElement inputBatchElem = batchListElem.getChildElement ("Batch");

		YFCDocument printDoc = YFCDocument.createDocument ("Print");
		YFCElement printElem = printDoc.getDocumentElement();
		YFCElement batchElem = printElem.createChild("Batch");
		YFCElement printerPreferencesElem = printElem.createChild("PrinterPreferences");

		batchElem.setAttribute("BatchNo",inputBatchElem.getAttribute("BatchNo"));
		batchElem.setAttribute("Node",inputBatchElem.getAttribute("Node"));
		batchElem.setAttribute("ActivityGroupId","RETRIEVAL");

		printerPreferencesElem.setAttribute("OrganizationCode",inputBatchElem.getAttribute("OrganizationCode"));
		printerPreferencesElem.setAttribute("UserId",env.getUserId());

		System.out.println(">>>>>>>>>>>>>>>>>> Input to PrintTaskList below....");
		System.out.println(printElem);

		return printDoc.getDocument();
	}

	public Document prepareToCreateOrder(YFSEnvironment env, Document inDoc) throws Exception {
	
		int x;
		
		String file = "C:/yantra/yantra75/createProOrder.xml";
		YFCDocument createOrderDoc = YFCDocument.getDocumentForXMLFile(file);
		// initialize the Bean environment and api
		YFSUtil.setYFSEnv (env);
		YIFApi	api = YFSUtil.getYIFApi (env != null ? true : false);
		env = YFSUtil.getYFSEnv ();

		for (x = 0;x < 48 ; x = x+1)	{
			// call getOrderDetails for the give order document
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to createOrder:");
				System.out.println (YFSXMLUtil.getXMLString (createOrderDoc.getDocument()));
			}
			YFCDocument.getDocumentFor (api.createOrder (env, createOrderDoc.getDocument()));
		}
		
		
		return inDoc;
	}

	public Document prepareToCreateShipment(YFSEnvironment env, Document inDoc) throws Exception {
	
		int x;
		
		String file = "C:/yantra/yantra75/createShipment.xml";
		YFCDocument createShipmentDoc = YFCDocument.getDocumentForXMLFile(file);
		// initialize the Bean environment and api
		YFSUtil.setYFSEnv (env);
		YIFApi	api = YFSUtil.getYIFApi (env != null ? true : false);
		env = YFSUtil.getYFSEnv ();

		for (x = 0;x < 20 ; x = x+1)	{
			if (YFSUtil.getDebug ())
			{
				System.out.println ("Input to createShipment:");
				System.out.println (YFSXMLUtil.getXMLString (createShipmentDoc.getDocument()));
			}
			YFCDocument.getDocumentFor (api.createShipment (env, createShipmentDoc.getDocument()));
		}
		
		
		return inDoc;
	}

	//Call this on ADD_TOO_CONTAINER.ON_CONTAINER_PACK_PROCESS_COMPLETE and then addContainerToManifest API
	public Document prepareAddContainerToManifest(YFSEnvironment env, Document inDoc) {
	
		YFCDocument yfcInDoc = YFCDocument.getDocumentFor(inDoc);
        YFCElement containerElem = yfcInDoc.getDocumentElement();
		System.out.println(">>>>>>>>>>> Preparing to add container to manifest. containerElem is " + containerElem);

		String weight = containerElem.getAttribute("ContainerGrossWeight");
		String weightUOM = containerElem.getAttribute("ContainerGrossWeightUOM");
		String shipmentKey = containerElem.getAttribute("ShipmentKey");
		String shipmentContainerKey = containerElem.getAttribute("ShipmentContainerKey");

		YFCDocument manifestDoc = YFCDocument.createDocument ("Container");
		YFCElement containerInputElem = manifestDoc.getDocumentElement();
		YFCElement printerPreferenceElem = containerInputElem.createChild("PrinterPreference");

		containerInputElem.setAttribute("ActualWeight",weight);
		containerInputElem.setAttribute("ActualWeightUOM",weightUOM);
		containerInputElem.setAttribute("ShipmentKey",shipmentKey);
		containerInputElem.setAttribute("ShipmentContainerKey",shipmentContainerKey);
		containerInputElem.setAttribute("TrackingNo",shipmentContainerKey);
		printerPreferenceElem.setAttribute("Dummy","Dummy");

		System.out.println(">>>>>>>>>>> Ready to add container to manifest. Input to addContainerToManifest is " + containerInputElem);

		return manifestDoc.getDocument();
	}
}

