/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.compatibility.extensions.dms.data;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluator;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DocumentSetEvaluator implements AccessPathEvaluator
{
   private static final Logger trace = LogManager.getLogger(DocumentSetEvaluator.class);

   public Object evaluate(Map attribs, Object accessPoint, String outPath)
   {
      DocumentSetStorageBean storage = findDocumentSetMemento(attribs, accessPoint);

      Object value;
      try
      {
         value = JavaDataTypeUtils.evaluate(outPath, storage);
      }
      catch (InvocationTargetException e)
      {
         throw new PublicException(
               BpmRuntimeError.DMS_FAILED_READING_ENTITY_BEAN_ATTRIBUTE.raise(),
               e.getTargetException());
      }

      return value;
   }

   public Object evaluate(Map attribs, Object accessPoint, String inPath, Object value)
   {
      Object result = UNMODIFIED_HANDLE;

      DocumentSetStorageBean storage = findDocumentSetMemento(attribs, accessPoint);

      if (null == storage)
      {
         // implicitly create new instance
         storage = new DocumentSetStorageBean();
      }

      if (StringUtils.isEmpty(inPath))
      {
         if (value instanceof DocumentSetStorageBean)
         {
            if (null != storage)
            {
               // Do NOT merge oid. If oid == 0 then the document set needs to be saved with a new oid (copy).
               storage.mergeDocumentSets((DocumentSetStorageBean) value, false);
            }
            else
            {
               storage = (DocumentSetStorageBean) value;
            }

            result = writeDocumentSetMemento(attribs, accessPoint, storage);
         }
         else if (value instanceof Long)
         {
            // Find original storage by its referencing oid provided in value.
            storage = findDocumentSetMemento(attribs, value);
            // In order to create a new copy for this storage the oid has to be set to 0.
            storage.setOid(0);
            // The storage will be written as copy with a new oid.
            result = writeDocumentSetMemento(attribs, accessPoint, storage);
         }
         else if (null == value)
         {
            // left empty intentionally.
         }
         else
         {
            trace.warn(MessageFormat.format(
                  "Could not evaluate a DocumentSet for value {0} of type {1}.",
                  new Object[] { value, value.getClass() }));
         }
      }
      else
      {
         try
         {
            JavaDataTypeUtils.evaluate(inPath, storage, value);

            result = writeDocumentSetMemento(attribs, accessPoint, storage);
         }
         catch (InvocationTargetException e)
         {
            throw new PublicException(
                  BpmRuntimeError.DMS_FAILED_SETTING_DOCUMENT_ATTRIBUTE.raise(),
                  e.getTargetException());
         }
      }

      return result;
   }

   public Object createInitialValue(Map data)
   {
      return null;
   }

   public Object createDefaultValue(Map attributes)
   {
      return null;
   }

   private DocumentSetStorageBean findDocumentSetMemento(Map attributes, Object handle)
   {
      DocumentSetStorageBean result = null;

      if (null != handle)
      {
         if (handle instanceof DocumentSetStorageBean)
         {
            result = (DocumentSetStorageBean) handle;
         }
         else if (handle instanceof Long)
         {
            // Fetch from cache only
            String memento = LargeStringHolder.getLargeString(
                  ((Long) handle).longValue(), DocumentSetTypeDescription.class, false);

            if ( !StringUtils.isEmpty(memento))
            {
               // Consider disk only if not found in cache
               memento = LargeStringHolder.getLargeString(((Long) handle).longValue(),
                     DocumentSetTypeDescription.class, true);
            }

            if ( !StringUtils.isEmpty(memento))
            {
               try
               {
                  result = DocumentStorageMediator.deserializeDocumentSet(memento);
               }
               catch (Exception e)
               {
                  // TODO
               }
            }
         }
         else
         {
            // TODO warn
         }
      }

      return result;
   }

   private Long writeDocumentSetMemento(Map attributes, Object handle,
         DocumentSetStorageBean doc)
   {
      Long result;

      Long oldOid;
      if (handle instanceof DocumentSetStorageBean)
      {
         oldOid = new Long(((DocumentSetStorageBean) handle).getOid());
      }
      else if (handle instanceof Long)
      {
         oldOid = (Long) handle;
      }
      else if ((null == handle) && (null != doc))
      {
         oldOid = null;
      }
      else
      {
         // TODO warn
         throw new PublicException(BpmRuntimeError.DMS_INVALID_HANLDE.raise(handle));
      }

      if (null != doc)
      {
         if ((null != oldOid) && (oldOid.longValue() != doc.getOid()))
         {
            LargeStringHolder.deleteAllForOID(oldOid.longValue(),
                  DocumentSetTypeDescription.class);
         }

         if (0 == doc.getOid())
         {
            // TODO get new OID (leverage empty record in string data table)
            LargeStringHolder lsh = new LargeStringHolder();
            SessionFactory.getSession(SessionProperties.DS_NAME_AUDIT_TRAIL).cluster(
                  lsh);
            lsh.delete();
            doc.setOid(lsh.getOID());
         }

         String memento = DocumentStorageMediator.serializeDocumentSet(doc);

         LargeStringHolder.setLargeString(doc.getOid(), DocumentSetTypeDescription.class,
               memento);

         result = new Long(doc.getOid());
      }
      else
      {
         if (null != oldOid)
         {
            LargeStringHolder.deleteAllForOID(oldOid.longValue(),
                  DocumentSetTypeDescription.class);
         }

         result = null;
      }

      return result;
   }

   public static class DocumentSetTypeDescription
   {
      public static final String TABLE_NAME = DocumentSetStorageBean.TABLE_NAME;
      public static final String PK_FIELD = IdentifiablePersistentBean.FIELD__OID;

      long oid;
   }
}
