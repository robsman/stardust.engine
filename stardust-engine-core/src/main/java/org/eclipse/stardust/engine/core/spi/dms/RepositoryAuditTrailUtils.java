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
 * @author roland.stamm
 *
 */
public class RepositoryAuditTrailUtils
{

   private RepositoryAuditTrailUtils()
   {
      // Utility class
   }

   public static void storeDocument(Document document)
   {
      storeResource(document, DmsDocumentBean.class);
   }

   public static Document retrieveDocument(String documentId)
   {
      Map legoMap = retrieveResource(documentId, DmsDocumentBean.class);
      DmsDocumentBean dmsDocumentBean = new DmsDocumentBean(legoMap);
      return dmsDocumentBean;
   }

   public static void storeFolder(Folder folder)
   {
      storeResource(folder, DmsFolderBean.class);
   }

   public static Folder retrieveFolder(String folderId)
   {
      Map legoMap = retrieveResource(folderId, DmsFolderBean.class);
      DmsFolderBean dmsFolderBean = new DmsFolderBean(legoMap);
      return dmsFolderBean;
   }

   private static void storeResource(Resource resource, Class<?> clazz)
   {
      ClobDataBean documentBlob = ClobDataBean.find(clazz,
            resource.getId());
      if (documentBlob == null)
      {
         documentBlob = new ClobDataBean(0, clazz, new DmsResourceHolder(
               resource), resource.getId());
         Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
         session.cluster(documentBlob);
      }
      else
      {
         documentBlob.setStringValueProvider(new DmsResourceHolder(resource), true);
      }
   }

   private static Map retrieveResource(String resourceId, Class<?> clazz)
   {
      ClobDataBean documentBlob = ClobDataBean.find(clazz, resourceId);
      if (documentBlob != null)
      {
         String stringValue = documentBlob.getStringValue();
         try
         {
            Map legoMap = deserialize(stringValue);
            return legoMap;
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         catch (ClassNotFoundException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return null;
   }

   private static Map deserialize(String stringValue) throws IOException,
         ClassNotFoundException
   {
      return new Gson().fromJson(stringValue, StringMap.class).toMap();
   }

   private static String serialize(Map map) throws IOException
   {
      return new Gson().toJson(new StringMap(map));
   }

   private static class DmsResourceHolder implements ClobDataBean.StringValueProvider
   {

      private DmsResourceBean resource;

      public DmsResourceHolder(Resource resource)
      {
         this.resource = (DmsResourceBean) resource;
      }

      @Override
      public String getStringValue()
      {
         Map vfsResource = resource.vfsResource();
         String serializeObject = null;
         try
         {
            serializeObject = serialize(vfsResource);
         }
         catch (IOException e)
         {
            // TODO
            e.printStackTrace();
         }
         return serializeObject;
      }
   }

}
