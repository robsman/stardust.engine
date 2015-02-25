/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.inferStructDefinition;

import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.core.struct.TypedXPath;

/**
 * Various utility to help {@link WsApiStartProcessCommand} handle all data as java.io.Serializable.
 *
 * @author Roland.Stamm
 */
public class WsApiStartProcessUtils
{

   private WsApiStartProcessUtils()
   {
      // Utility class
   }

   public static void unwrapStartProcessBpmFault(InputDocumentStoreException cause) throws BpmFault
   {
        BpmFaultCodeXto faultCodeXto = BpmFaultCodeXto.valueOf(cause.getFaultCode());
        BpmFaultXto bpmFaultXto = new BpmFaultXto();
        bpmFaultXto.setFaultCode(faultCodeXto);
        throw new BpmFault(cause.getMessage(), bpmFaultXto);
   }

   public static List<InputDocument> unmarshalToSerializable(InputDocumentsXto xto, Model model)
   {
      List<InputDocument> inputDocuments = null;

      if (xto != null)
      {
         inputDocuments = CollectionUtils.newArrayList();

         List<InputDocumentXto> inputDocumentsXto = xto.getInputDocument();
         for (InputDocumentXto inputDocumentXto : inputDocumentsXto)
         {
            InputDocument inputDocument = unmarshalToSerializable(inputDocumentXto, model);
            if (inputDocument != null)
            {
               inputDocuments.add(inputDocument);
            }
         }

      }
      return inputDocuments;
   }

   public static InputDocument unmarshalToSerializable(InputDocumentXto xto, Model model)
   {
      InputDocument inputDocument = null;
      if (xto != null)
      {
      inputDocument = new InputDocument();

      inputDocument.setGlobalVariableId(xto.getGlobalVariableId());
      inputDocument.setTargetFolder(xto.getTargetFolder());

      DataHandler contentHandler = xto.getContent();
      if (contentHandler != null)
      {
         inputDocument.setContent(DmsAdapterUtils.extractContentByteArray(contentHandler));
      }

      DocumentInfoXto documentInfoXto = xto.getDocumentInfo();
      if (documentInfoXto!=null)
      {
         inputDocument.setMetaDataType(documentInfoXto.getMetaDataType());

         Set<TypedXPath> metaDataXPaths = null;
         if ((null != documentInfoXto.getMetaData()) && (null != documentInfoXto.getMetaDataType()))
         {
            metaDataXPaths = inferStructDefinition(documentInfoXto.getMetaDataType(), model);
         }
         inputDocument.setDocumentInfo(XmlAdapterUtils.fromXto(documentInfoXto, metaDataXPaths));
      }

         DocumentVersionInfoXto versionInfo = xto.getVersionInfo();
         if (versionInfo != null)
         {
            inputDocument.setVersion(true);
            inputDocument.setLabel(versionInfo.getLabel());
            inputDocument.setComment(versionInfo.getComment());
         }
      }
      return inputDocument;
   }

}
