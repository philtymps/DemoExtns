package com.custom.diab.demos.ue;

import com.yantra.pca.bridge.YCDFoundationBridge;
import com.yantra.pca.ycd.japi.ue.YCDProcessReturnCompletionUE;
import com.yantra.shared.dbi.YFS_Order_Header;
import com.yantra.shared.ycp.YFSContext;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.dom.*;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSUserExitException;
import java.util.ArrayList;
import java.util.Iterator;
import org.w3c.dom.Document;

public class SEProcessReturnCompletionUEImpl
    implements YCDProcessReturnCompletionUE
{

    public SEProcessReturnCompletionUEImpl()
    {
        hasReceivableLines = true;
    }

    public Document processReturnCompletion(YFSEnvironment env, Document inDoc)
        throws YFSUserExitException
    {
        YFCDocument dIn = YFCDocument.getDocumentFor(inDoc);
        if(dIn == null)
            return null;
        hasReceivableLines(dIn);
        YFCElement firstMultiApiInputElement = createInputForFirstMultiApi(env, dIn);
        YFCDocument firstMultiApiOutputDocument = YCDFoundationBridge.getInstance().multiApi(env, firstMultiApiInputElement.getOwnerDocument());
        if(YFCCommon.isVoid(firstMultiApiOutputDocument))
            return null;
        if(hasReceivableLines)
        {
            String receiptHeaderKey = getReceiptHeaderKey(firstMultiApiOutputDocument);
            YFCElement secondMultiApiInputElement = createInputForSecondMultiApi(dIn, receiptHeaderKey);
            YFCDocument secondMultiApiOutputDocument = YCDFoundationBridge.getInstance().multiApi(env, secondMultiApiInputElement.getOwnerDocument());
            if(YFCCommon.isVoid(secondMultiApiOutputDocument))
                return null;
        }
        YFCDocument apiSuccessDocument = YFCDocument.createDocument("ApiSuccess");
        return apiSuccessDocument.getDocument();
    }

    private void hasReceivableLines(YFCDocument in)
    {
        YFCElement inElem = in.getDocumentElement();
        YFCElement inOLines = inElem.getChildElement("OrderLines");
        if(!inOLines.getChildren("OrderLine").hasNext())
            hasReceivableLines = false;
    }

    private YFCElement createInputForFirstMultiApi(YFSEnvironment env, YFCDocument inputDocument)
    {
        ArrayList<YFCElement> inputElementList = new ArrayList<YFCElement>();
        ArrayList<String> apiNameList = new ArrayList<String>();
        YFCElement orderElement = inputDocument.getDocumentElement();
        String returnOrderHeaderKey = orderElement.getAttribute("OrderHeaderKey");
        String returnOrderDocumentType = orderElement.getAttribute("DocumentType");
        String receivingNode = orderElement.getAttribute("ReceivingNode");
        String receivingDock = orderElement.getAttribute("ReceivingDock");
        String exchangeOrderHeaderKey = orderElement.getAttribute("ExchangeOrderHeaderKey");
        if(!YFCCommon.isVoid(exchangeOrderHeaderKey))
        {
            YFS_Order_Header oOrder;
            if(!YFCCommon.isVoid(returnOrderHeaderKey))
            {
                oOrder = YCDFoundationBridge.getInstance().getOrderHeader((YFSContext)env, returnOrderHeaderKey, null, null, null, null, false);
                String returnDraftOrderFlag = oOrder.isDraftOrder() ? "Y" : "N";
                if(YFCCommon.equals(returnDraftOrderFlag, "Y"))
                {
                    isReturnDraftOrder = true;
                    YFCElement returnConfirmDraftOrderElement = getConfirmDraftOrderInput(returnOrderHeaderKey);
                    inputElementList.add(returnConfirmDraftOrderElement);
                    apiNameList.add("confirmDraftOrder");
                }
            }
            oOrder = YCDFoundationBridge.getInstance().getOrderHeader((YFSContext)env, exchangeOrderHeaderKey, null, null, null, null, false);
            String exchangeDraftOrderFlag = oOrder.isDraftOrder() ? "Y" : "N";
            if(YFCCommon.equals(exchangeDraftOrderFlag, "Y"))
            {
                YFCElement exchangeConfirmDraftOrderElement = getConfirmDraftOrderInput(exchangeOrderHeaderKey);
                inputElementList.add(exchangeConfirmDraftOrderElement);
                apiNameList.add("confirmDraftOrder");
            }
        } else
        if(!YFCCommon.isVoid(returnOrderHeaderKey))
        {
            YFS_Order_Header oOrder = YCDFoundationBridge.getInstance().getOrderHeader((YFSContext)env, returnOrderHeaderKey, null, null, null, null, false);
            String returnDraftOrderFlag = oOrder.isDraftOrder() ? "Y" : "N";
            if(YFCCommon.equals(returnDraftOrderFlag, "Y"))
            {
                YFCElement returnConfirmDraftOrderElement = getConfirmDraftOrderInput(returnOrderHeaderKey);
                inputElementList.add(returnConfirmDraftOrderElement);
                apiNameList.add("confirmDraftOrder");
            }
        }
        // FIX BUG IN SOM SETTING BOTH RECEIVE_NODE AND SHIP_NODE
        YFCElement orderChangeElement = getChangeOrderInput (returnOrderHeaderKey, returnOrderDocumentType, inputDocument);
        if(!YFCCommon.isVoid(orderChangeElement))
        {
            inputElementList.add(orderChangeElement);
            apiNameList.add("changeOrder");
        }
        
        YFCElement orderStatusChangeElement = getChangeOrderStatusInput(returnOrderHeaderKey, returnOrderDocumentType, inputDocument);
        if(!YFCCommon.isVoid(orderStatusChangeElement))
        {
            inputElementList.add(orderStatusChangeElement);
            apiNameList.add("changeOrderStatus");
        }
        YFCElement scheduleOrderElement = getScheduleOrderInput(returnOrderHeaderKey);
        if(!YFCCommon.isVoid(scheduleOrderElement))
        {
            inputElementList.add(scheduleOrderElement);
            apiNameList.add("scheduleOrder");
        }
        if(hasReceivableLines)
        {
            YFCElement startReceiptElement = getStartRecieptInput(returnOrderHeaderKey, returnOrderDocumentType, receivingNode, receivingDock);
            if(!YFCCommon.isVoid(startReceiptElement))
            {
                inputElementList.add(startReceiptElement);
                apiNameList.add("startReceipt");
            }
        }
        YFCElement multiApiElement = createMultiApiInput(inputElementList, apiNameList);
        return multiApiElement;
    }

    private YFCElement getConfirmDraftOrderInput(String returnOrderHeaderKey)
    {
        YFCElement confirmDraftOrderElement = YFCDocument.createDocument("ConfirmDraftOrder").getDocumentElement();
        confirmDraftOrderElement.setAttribute("OrderHeaderKey", returnOrderHeaderKey);
        return confirmDraftOrderElement;
    }

    private YFCElement getChangeOrderStatusInput(String returnOrderHeaderKey, String returnOrderDocumentType, YFCDocument inputDocument)
    {
        YFCElement orderStatusChangeElement = YFCDocument.createDocument("OrderStatusChange").getDocumentElement();
        orderStatusChangeElement.setAttribute("OrderHeaderKey", returnOrderHeaderKey);
        if(isReturnDraftOrder)
        {
            orderStatusChangeElement.setAttribute("ChangeForAllAvailableQty", "Y");
            orderStatusChangeElement.setAttribute("BaseDropStatus", "1100.01");
        } else
        {
            YFCElement orderElement = inputDocument.getDocumentElement();
            YFCElement orderLinesElement = orderElement.getChildElement("OrderLines");
            if(!YFCCommon.isVoid(orderLinesElement))
            {
                YFCElement orderStatusOrderLinesElement = orderStatusChangeElement.createChild("OrderLines");
                YFCIterable<?> orderLineItr = orderLinesElement.getChildren();
                YFCElement orderStatusOrderLineElement;
                for(Iterator<?> i$ = orderLineItr.iterator(); i$.hasNext(); orderStatusOrderLineElement.setAttribute("BaseDropStatus", "1100.01"))
                {
                    YFCElement orderLineElement = (YFCElement)i$.next();
                    orderStatusOrderLineElement = orderStatusOrderLinesElement.createChild("OrderLine");
                    orderStatusOrderLineElement.setAttribute("OrderLineKey", orderLineElement.getAttribute("OrderLineKey"));
                    orderStatusOrderLineElement.setAttribute("ChangeForAllAvailableQty", "Y");
                }

            }
        }
        String transactionId = (new StringBuilder()).append("AUTHORIZE_RETURN.").append(returnOrderDocumentType).toString();
        orderStatusChangeElement.setAttribute("TransactionId", transactionId);
        return orderStatusChangeElement;
    }

    private YFCElement getChangeOrderInput(String returnOrderHeaderKey, String returnOrderDocumentType, YFCDocument inputDocument)
    {
        YFCElement orderChangeElement = YFCDocument.createDocument("Order").getDocumentElement();
        orderChangeElement.setAttribute("OrderHeaderKey", returnOrderHeaderKey);
        YFCElement orderElement = inputDocument.getDocumentElement();
        YFCElement orderLinesElement = orderElement.getChildElement("OrderLines");
        if(!YFCCommon.isVoid(orderLinesElement))
        {
        	YFCElement orderOrderLinesElement = orderChangeElement.createChild("OrderLines");
            YFCIterable<?> orderLineItr = orderLinesElement.getChildren();
            YFCElement orderOrderLineElement;
            for(Iterator<?> i$ = orderLineItr.iterator(); i$.hasNext(); )
            {
                YFCElement orderLineElement = (YFCElement)i$.next();
                orderOrderLineElement = orderOrderLinesElement.createChild("OrderLine");
                orderOrderLineElement.setAttribute("OrderLineKey", orderLineElement.getAttribute("OrderLineKey"));
                orderOrderLineElement.setAttribute("ReceivingNode", "");
            }
        }
        return orderChangeElement;
    }

    private YFCElement getScheduleOrderInput(String returnOrderHeaderKey)
    {
        YFCElement scheduleOrderElement = YFCDocument.createDocument("ScheduleOrder").getDocumentElement();
        scheduleOrderElement.setAttribute("OrderHeaderKey", returnOrderHeaderKey);
        scheduleOrderElement.setAttribute("ScheduleAndRelease", "Y");
        return scheduleOrderElement;
    }

    private YFCElement getStartRecieptInput(String returnOrderHeaderKey, String returnOrderDocumentType, String receivingNode, String receivingDock)
    {
        YFCElement startRecieptElement = YFCDocument.createDocument("Receipt").getDocumentElement();
        startRecieptElement.setAttribute("DocumentType", returnOrderDocumentType);
        startRecieptElement.setAttribute("ReceivingNode", receivingNode);
        if(!YFCCommon.isVoid(receivingDock))
            startRecieptElement.setAttribute("ReceivingDock", receivingDock);
        YFCElement shipmentElement = startRecieptElement.createChild("Shipment");
        shipmentElement.setAttribute("OrderHeaderKey", returnOrderHeaderKey);
        return startRecieptElement;
    }

    private YFCElement createMultiApiInput(ArrayList<YFCElement> inputElementList, ArrayList<String> apiNameList)
    {
        YFCElement multiApiElement = YFCDocument.createDocument("MultiApi").getDocumentElement();
        for(int i = 0; i < inputElementList.size(); i++)
        {
            YFCElement inputApiElement = (YFCElement)inputElementList.get(i);
            String apiName = (String)apiNameList.get(i);
            YFCElement apiElement = multiApiElement.createChild("API");
            apiElement.setAttribute("Name", apiName);
            YFCElement inputElement = apiElement.createChild("Input");
            inputElement.importNode(inputApiElement);
        }

        return multiApiElement;
    }

    private String getReceiptHeaderKey(YFCDocument multiApiOutputDocument)
    {
        YFCElement multiApiOutputElement = multiApiOutputDocument.getDocumentElement();
        YFCNodeList<?> apiElementList = multiApiOutputElement.getElementsByTagName("API");
        for(int i = 0; i < apiElementList.getLength(); i++)
        {
            YFCElement apiElement = (YFCElement)apiElementList.item(i);
            String apiName = apiElement.getAttribute("Name");
            if(!apiName.equals("startReceipt"))
                continue;
            YFCElement outputElement = apiElement.getChildElement("Output");
            YFCElement receiptElement = outputElement.getChildElement("Receipt");
            if(!YFCCommon.isVoid(receiptElement))
            {
                String receiptHeaderKey = receiptElement.getAttribute("ReceiptHeaderKey");
                return receiptHeaderKey;
            }
        }

        return null;
    }

    private YFCElement createInputForSecondMultiApi(YFCDocument inputDocument, String receiptHeaderKey)
    {
        ArrayList<YFCElement> inputElementList = new ArrayList<YFCElement>();
        ArrayList<String> apiNameList = new ArrayList<String>();
        YFCElement orderElement = inputDocument.getDocumentElement();
        String returnOrderDocumentType = orderElement.getAttribute("DocumentType");
        String returnOrderHeaderKey = orderElement.getAttribute("OrderHeaderKey");
        String receivingDock = orderElement.getAttribute("ReceivingDock");
        YFCElement recordReceiptInOneStepElement = getRecordReceiptInput(orderElement, receiptHeaderKey, receivingDock);
        if(!YFCCommon.isVoid(recordReceiptInOneStepElement))
        {
            inputElementList.add(recordReceiptInOneStepElement);
            apiNameList.add("receiveOrder");
        }
        YFCElement closeReceiptElement = getCloseReceiptInput(receiptHeaderKey, returnOrderDocumentType);
        if(!YFCCommon.isVoid(closeReceiptElement))
        {
            inputElementList.add(closeReceiptElement);
            apiNameList.add("closeReceipt");
        }
        YFCElement createOrderInvoiceElement = getCreateOrderInvoiceInput(returnOrderHeaderKey, returnOrderDocumentType, orderElement);
        if(!YFCCommon.isVoid(createOrderInvoiceElement))
        {
            inputElementList.add(createOrderInvoiceElement);
            apiNameList.add("createOrderInvoice");
        }
        YFCElement multiApiElement = createMultiApiInput(inputElementList, apiNameList);
        return multiApiElement;
    }

    private YFCElement getRecordReceiptInput(YFCElement orderElement, String receiptHeaderKey, String receivingDock)
    {
        String returnOrderDocumentType = orderElement.getAttribute("DocumentType");
        String receivingNode = orderElement.getAttribute("ReceivingNode");
        String createUserId = orderElement.getAttribute("Createuserid");
        YFCElement receiptElement = YFCDocument.createDocument("Receipt").getDocumentElement();
        receiptElement.setAttribute("DocumentType", returnOrderDocumentType);
        receiptElement.setAttribute("ReceivingNode", receivingNode);
        receiptElement.setAttribute("ReceiptHeaderKey", receiptHeaderKey);
        if(!YFCCommon.isVoid(receivingDock))
            receiptElement.setAttribute("ReceivingDock", receivingDock);
        YFCElement receiptLinesElement = receiptElement.createChild("ReceiptLines");
        YFCNodeList<?> orderLineElementList = orderElement.getElementsByTagName("OrderLine");
        for(int i = 0; i < orderLineElementList.getLength(); i++)
        {
            YFCElement orderLineElement = (YFCElement)orderLineElementList.item(i);
            String dispositionCode = orderLineElement.getAttribute("DispositionCode");
            String orderLineKey = orderLineElement.getAttribute("OrderLineKey");
            YFCElement receiptLineElement = receiptLinesElement.createChild("ReceiptLine");
            if(!YFCCommon.isVoid(dispositionCode))
                receiptLineElement.setAttribute("DispositionCode", dispositionCode);
            if(!YFCCommon.isVoid(orderLineKey))
                receiptLineElement.setAttribute("OrderLineKey", orderLineKey);
            if(!YFCCommon.isVoid(createUserId))
                receiptLineElement.setAttribute("InspectedBy", createUserId);
            YFCElement orderLineTranQuantityElement = orderLineElement.getChildElement("OrderLineTranQuantity");
            if(YFCCommon.isVoid(orderLineTranQuantityElement))
                continue;
            String qty = orderLineTranQuantityElement.getAttribute("OrderedQty");
            String transactionalUOM = orderLineTranQuantityElement.getAttribute("TransactionalUOM");
            YFCElement receiptLineTranQuantityElement = receiptLineElement.createChild("ReceiptLineTranQuantity");
            if(!YFCCommon.isVoid(qty))
                receiptLineTranQuantityElement.setAttribute("Quantity", qty);
            if(!YFCCommon.isVoid(transactionalUOM))
                receiptLineTranQuantityElement.setAttribute("TransactionalUOM", transactionalUOM);
        }

        return receiptElement;
    }

    private YFCElement getCloseReceiptInput(String receiptHeaderKey, String returnOrderDocumentType)
    {
        YFCElement receiptElement = YFCDocument.createDocument("Receipt").getDocumentElement();
        receiptElement.setAttribute("ReceiptHeaderKey", receiptHeaderKey);
        receiptElement.setAttribute("DocumentType", returnOrderDocumentType);
        return receiptElement;
    }

    private YFCElement getCreateOrderInvoiceInput(String returnOrderHeaderKey, String returnOrderDocumentType, YFCElement returnOrderElement)
    {
        YFCElement orderElement = YFCDocument.createDocument("Order").getDocumentElement();
        orderElement.setAttribute("OrderHeaderKey", returnOrderHeaderKey);
        String transactionId = (new StringBuilder()).append("CREATE_ORDER_INVOICE.").append(returnOrderDocumentType).toString();
        orderElement.setAttribute("TransactionId", transactionId);
        YFCElement orderLinesElement = orderElement.createChild("OrderLines");
        YFCElement returnOrderLinesElement = returnOrderElement.getChildElement("OrderLines");
        if(!YFCCommon.isVoid(returnOrderLinesElement))
        {
            YFCIterable<?> returnOrderLineItr = returnOrderLinesElement.getChildren();
            YFCElement returnOrderLineElement;
            YFCElement orderLineElement;
            for(Iterator<?> i$ = returnOrderLineItr.iterator(); i$.hasNext(); orderLineElement.setAttribute("Quantity", returnOrderLineElement.getAttribute("OrderedQty")))
            {
                returnOrderLineElement = (YFCElement)i$.next();
                orderLineElement = orderLinesElement.createChild("OrderLine");
                orderLineElement.setAttribute("OrderLineKey", returnOrderLineElement.getAttribute("OrderLineKey"));
            }

        }
        return orderElement;
    }

    private boolean hasReceivableLines;
    private boolean isReturnDraftOrder;
}
