/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.newLinkedList;
import static org.eclipse.stardust.common.CompareHelper.areEqual;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.runtime.DmsUtils.createFolderInfo;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.fromXto;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.inferStructDefinition;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Set;

import javax.activation.DataHandler;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.core.struct.TypedXPath;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class DmsAdapterUtils
{

   public static void updateDocumentFromInfo(Document doc, DocumentInfo docInfo)
   {
      updateDocumentInfo(doc, docInfo);
   }

   private static void updateDocumentInfo(DocumentInfo lhs, DocumentInfo rhs)
   {
      updateResourceInfo(lhs, rhs);

      if ( !areEqual(lhs.getContentType(), rhs.getContentType()))
      {
         lhs.setContentType(rhs.getContentType());
      }
   }

   private static void updateResourceInfo(ResourceInfo lhs, ResourceInfo rhs)
   {
      if ( !areEqual(lhs.getDescription(), rhs.getDescription()))
      {
         lhs.setDescription(rhs.getDescription());
      }

      if ( !areEqual(lhs.getOwner(), rhs.getOwner()))
      {
         lhs.setOwner(rhs.getOwner());
      }

      if ( !areEqual(lhs.getProperties(), rhs.getProperties()))
      {
         lhs.setProperties(rhs.getProperties());
      }

      if ( !areEqual(lhs.getName(), rhs.getName()))
      {
         lhs.setName(rhs.getName());
      }

   }

   public static DataHandler wrapContentByteArray(byte[] content, String contentType)
   {
      DocumentInfo docInfo = DmsUtils.createDocumentInfo("anonymous");
      docInfo.setContentType(contentType);

      return new DataHandler(new DocumentContentDataSource(docInfo, content));
   }

   public static DataHandler wrapContentByteArray(byte[] content, DocumentInfo docInfo)
   {
      return new DataHandler(new DocumentContentDataSource(docInfo, content));
   }

   public static byte[] extractContentByteArray(DataHandler contentHandler)
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      byte[] buffer = new byte[4096];

      try
      {
         InputStream from = contentHandler.getInputStream();

         try
         {
            int bytesRead;
            while (0 < (bytesRead = from.read(buffer)))
            {
               baos.write(buffer, 0, bytesRead);
            }
         }
         finally
         {
            from.close();
         }
      }
      catch (IOException ioe)
      {
         throw new PublicException("Failed retrieving document content.", ioe);
      }

      return baos.toByteArray();
   }

   public static void ensureFolderExists(DocumentManagementService dms, String folderId)
         throws DocumentManagementServiceException
   {
      if ( !isEmpty(folderId) && folderId.startsWith("/"))
      {
         // try to create folder
         String[] segments = folderId.substring(1).split("/");

         // walk backwards to find existing path prefix, then go forward again creating missing segments

         Folder folder = null;
         LinkedList<String> missingSegments = newLinkedList();
         for (int i = segments.length - 1; i >= 0; --i)
         {
            StringBuilder path = new StringBuilder();
            for (int j = 0; j <= i; ++j)
            {
               path.append("/").append(segments[j]);
            }

            folder = dms.getFolder(path.toString(), Folder.LOD_NO_MEMBERS);
            if (null != folder)
            {
               // found existing prefix
               break;
            }
            else
            {
               // folder missing?
               missingSegments.add(0, segments[i]);
            }
         }

         String currentPath = (null != folder) ? folder.getPath() : "";
         while ( !missingSegments.isEmpty())
         {
            String parentFolderId = isEmpty(currentPath)
                  ? "/"
                  : currentPath;

            String segment = missingSegments.remove(0);

            // create missing sub folder
            folder = dms.createFolder(parentFolderId, createFolderInfo(segment));
            currentPath = folder.getPath();
         }
      }
   }

   public static Document storeDocumentIntoDms(DocumentManagementService dms,
         Model model, InputDocumentXto inputDoc) throws BpmFault
   {
      return storeDocumentIntoDms(dms, model, inputDoc.getTargetFolder(),
            inputDoc.getDocumentInfo(), inputDoc.getContent(), inputDoc.getVersionInfo());
   }

   public static Document storeDocumentIntoDms(DocumentManagementService dms,
         Model model, String folderId, DocumentInfoXto xto, DataHandler contentHandler,
         DocumentVersionInfoXto versionXto) throws BpmFault
   {
      Set<TypedXPath> metaDataXPaths = null;
      if ((null != xto.getMetaData()) && (null != xto.getMetaDataType()))
      {
         metaDataXPaths = inferStructDefinition(xto.getMetaDataType(), model);
      }

      return storeDocumentIntoDms(dms, folderId, xto, metaDataXPaths, contentHandler,
            versionXto);
   }

   public static Document storeDocumentIntoDms(DocumentManagementService dms,
         String folderId, DocumentInfoXto xto, Set<TypedXPath> metaDataXPaths,
         DataHandler contentHandler, DocumentVersionInfoXto versionXto) throws BpmFault
   {
      DocumentInfo docInfo = fromXto(xto, metaDataXPaths);

      String documentPath = folderId;
      if ( !folderId.endsWith("/"))
      {
         documentPath += "/";
      }
      documentPath += docInfo.getName();

      try
      {
         // TODO use streaming API
         Document doc = (null != contentHandler)
               ? dms.createDocument(folderId, docInfo,
                     extractContentByteArray(contentHandler), /* TODO encoding? */null)
               : dms.createDocument(folderId, docInfo);
         if (null != versionXto)
         {
            doc = dms.versionDocument(doc.getId(), versionXto.getLabel());
         }

         return doc;
      }
      catch (DocumentManagementServiceException dmse)
      {
         BpmFaultXto faultInfo = new BpmFaultXto();
         if (BpmRuntimeError.DMS_ITEM_EXISTS.raise().getId().equals(dmse.getError().getId()))
         {
            faultInfo.setFaultCode(BpmFaultCodeXto.fromValue("ItemAlreadyExists"));

            throw new BpmFault("There already exists a file at " + documentPath,
                  faultInfo);
         }
         else if (BpmRuntimeError.DMS_FAILED_PATH_RESOLVE.raise(null).getId().equals(dmse.getError().getId()))
         {
            faultInfo.setFaultCode(BpmFaultCodeXto.fromValue("InvalidName"));

            throw new BpmFault(dmse.getMessage(),
                  faultInfo);
         }
         else if (BpmRuntimeError.DMS_UNKNOWN_FOLDER_ID.raise(null).getId().equals(dmse.getError().getId()))
         {
            faultInfo.setFaultCode(BpmFaultCodeXto.fromValue("ItemDoesNotExist"));

            throw new BpmFault(dmse.getMessage(),
                  faultInfo);
         }
         else if (BpmRuntimeError.DMS_DOCUMENT_TYPE_INVALID.raise(null).getId().equals(dmse.getError().getId()))
         {
            faultInfo.setFaultCode(BpmFaultCodeXto.fromValue("DocumentManagementServiceException"));

            throw new BpmFault(dmse.getMessage(),
                  faultInfo);
         }
         else if (!isEmpty(dmse.getError().getId()) && !isEmpty(dmse.getMessage()))
         {
            // marshal as DocumentManagementServiceException if error ID exists.
            faultInfo.setFaultCode(BpmFaultCodeXto.fromValue("DocumentManagementServiceException"));

            throw new BpmFault(dmse.getMessage(),
                  faultInfo);
         }
         else
         {
            faultInfo.setFaultCode(BpmFaultCodeXto.fromValue("UnknownError"));

            throw new BpmFault("Failed storing file at " + documentPath,
                  faultInfo);
         }
      }
   }

}
