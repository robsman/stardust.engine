/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import java.io.IOException;
import java.util.Map;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.Resource;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsDocumentBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsFolderBean;
import org.eclipse.stardust.engine.extensions.dms.data.DmsResourceBean;

import com.google.gson.Gson;

/**
 * This utility allows to store, retrieve and delete {@link Document} and {@link Folder}
 * beans as {@link ClobDataBean} in the AuditTrail using a json format. The entries are
 * stored uniquely by {@link Document#getId()}, {@link Document#getRevisionId()} and
 * {@link Folder#getId()}.
 *
 * @author roland.stamm
 *
 */
public class RepositoryAuditTrailUtils
{

   private RepositoryAuditTrailUtils()
   {
      // Utility class
   }

   /**
    * Stores a document in the AuditTrail by {@link Document#getId()} and
    * {@link Document#getRevisionId()} if a revesionId exists and is not
    * {@link RepositoryConstants#VERSION_UNVERSIONED}.
    *
    * @param document
    *           The document to persist in the AuditTrail.
    */
   public static void storeDocument(Document document)
   {
      if (document != null)
      {
         storeResource(document.getId(), document, DmsDocumentBean.class);

         // store entry for revision
         if (document.getRevisionId() != null
               && !RepositoryConstants.VERSION_UNVERSIONED.equals(document.getRevisionId())
               && !RepositoryConstants.VERSION_VERSIONED.equals(document.getRevisionId()))
         {
            storeResource(document.getRevisionId(), document, DmsDocumentBean.class);
         }
      }
   }

   /**
    * Fetches the document from the AuditTrail.
    *
    * @param documentId
    *           The document {@link Document#getId()} or {@link Document#getRevisionId()}
    *           to look up the document for.
    * @return The fetched {@link Document} from the AuditTrail.
    */
   public static Document retrieveDocument(String documentId)
   {
      Map legoMap = retrieveResource(documentId, DmsDocumentBean.class);
      DmsDocumentBean dmsDocumentBean = legoMap == null ? null : new DmsDocumentBean(
            legoMap);
      return dmsDocumentBean;
   }

   /**
    * Deletes the entry from the AuditTrail if it exists.
    *
    * @param documentId
    *           The document {@link Document#getId()} or {@link Document#getRevisionId()}
    *           to delete the AuditTrail entry for.
    */
   public static void removeDocument(String documentId)
   {
      removeResource(documentId, DmsDocumentBean.class);
   }

   /**
    * Stores a folder in the AuditTrail by {@link Folder#getId()}
    *
    * @param folder
    *           The folder to persist in the AuditTrail.
    */
   public static void storeFolder(Folder folder)
   {
      if (folder != null)
      {
         storeResource(folder.getId(), folder, DmsFolderBean.class);
      }
   }

   /**
    * Fetches the folder from the AuditTrail.
    *
    * @param folderId
    *           The folder {@link Folder#getId()} to look up the folder for.
    * @return The fetched {@link Folder} from the AuditTrail.
    */
   public static Folder retrieveFolder(String folderId)
   {
      Map legoMap = retrieveResource(folderId, DmsFolderBean.class);
      DmsFolderBean dmsFolderBean = legoMap == null ? null : new DmsFolderBean(
            legoMap);
      return dmsFolderBean;
   }

   /**
    * Deletes the entry from the AuditTrail if it exists.
    *
    * @param folderId
    *           The folder {@link Folder#getId()} to delete the AuditTrail entry for.
    */
   public static void removeFolder(String folderId)
   {
      removeResource(folderId, DmsFolderBean.class);
   }

   private static void storeResource(String resourceId, Resource resource,
         Class< ? > clazz)
   {
      if (resource != null)
      {
         ClobDataBean documentBlob = ClobDataBean.find(generateResourceHash(resourceId),
               clazz, generateValueIdentifierPrefix(resourceId) + "%");
         if (documentBlob == null)
         {
            documentBlob = new ClobDataBean(generateResourceHash(resourceId), clazz,
                  new DmsResourceHolder(resourceId, resource));
            Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
            session.cluster(documentBlob);
         }
         else
         {
            documentBlob.setStringValueProvider(new DmsResourceHolder(resourceId,
                  resource), true);
         }
      }
   }

   private static Map retrieveResource(String resourceId, Class< ? > clazz)
   {
      ClobDataBean documentBlob = ClobDataBean.find(generateResourceHash(resourceId),
            clazz, generateValueIdentifierPrefix(resourceId) + "%");
      if (documentBlob != null)
      {
         String stringValue = documentBlob.getStringValue();

         // remove prefix
         stringValue = stringValue.replace(generateValueIdentifierPrefix(resourceId), "");

         try
         {
            Map legoMap = deserialize(stringValue);
            return legoMap;
         }
         catch (IOException e)
         {
            throw new PublicException(e);
         }
         catch (ClassNotFoundException e)
         {
            throw new PublicException(e);
         }
      }
      return null;
   }

   private static void removeResource(String resourceId, Class< ? > clazz)
   {
      ClobDataBean documentBlob = ClobDataBean.find(generateResourceHash(resourceId),
            clazz, generateValueIdentifierPrefix(resourceId) + "%");
      if (documentBlob != null)
      {
         documentBlob.delete();
      }
   }

   private static long generateResourceHash(String id)
   {
      return id.hashCode();
   }

   private static String generateValueIdentifierPrefix(String id)
   {
      StringBuilder sb = new StringBuilder();

      sb.append(id.length()).append(':').append(id).append(':');

      return sb.toString();
   }

   public static Map deserialize(String stringValue) throws IOException,
         ClassNotFoundException
   {
      return new Gson().fromJson(stringValue, StringMap.class).toMap();
   }

   public static String serialize(Map map) throws IOException
   {
      return new Gson().toJson(new StringMap(map));
   }

   private static class DmsResourceHolder implements ClobDataBean.StringValueProvider
   {

      private DmsResourceBean resource;

      private String resourceId;

      public DmsResourceHolder(String resourceId, Resource resource)
      {
         this.resource = (DmsResourceBean) resource;
         this.resourceId = resourceId;
      }

      @Override
      public String getStringValue()
      {
         Map vfsResource = resource.vfsResource();
         String stringValue = null;
         try
         {
            stringValue = serialize(vfsResource);
         }
         catch (IOException e)
         {
            throw new PublicException(e);
         }

         // add prefix
         stringValue = generateValueIdentifierPrefix(resourceId) + stringValue;

         return stringValue;
      }
   }

}
