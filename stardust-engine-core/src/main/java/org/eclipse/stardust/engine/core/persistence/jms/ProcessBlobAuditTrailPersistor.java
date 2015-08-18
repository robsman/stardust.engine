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
package org.eclipse.stardust.engine.core.persistence.jms;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.persistence.jdbc.BatchStatementWrapper;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.FieldDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.LinkDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptorRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceHistoryBean;
import org.eclipse.stardust.engine.core.runtime.beans.ClobDataBean;
import org.eclipse.stardust.engine.core.runtime.beans.DataValueBean;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceHierarchyBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceScopeBean;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionInstanceBean;


/**
 * @author sauer
 * @version $Revision$
 */
public class ProcessBlobAuditTrailPersistor
{
   private static final Logger trace = LogManager.getLogger(ProcessBlobAuditTrailPersistor.class);

   private final TypeDescriptorRegistry tdRegistry = TypeDescriptorRegistry.current();

   private Map batchInsertStatements = CollectionUtils.newMap();

   public void writeIntoAuditTrail(Session session, int maxSqlBatchSize)
   {
      for (Iterator i = batchInsertStatements.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry entry = (Map.Entry) i.next();

         final TypeDescriptor td = (TypeDescriptor) entry.getKey();

         final List rows = (List) entry.getValue();

         if (ProcessInstanceBean.class == td.getType())
         {
            batchInsertProcessInstances(session, maxSqlBatchSize, td, rows);
         }
         else if (ProcessInstanceScopeBean.class == td.getType())
         {
            batchInsertProcessInstanceScopes(session, maxSqlBatchSize, td, rows);
         }
         else if (ProcessInstanceHierarchyBean.class == td.getType())
         {
            batchInsertProcessInstanceHierarchies(session, maxSqlBatchSize, td, rows);
         }
         else if (ActivityInstanceBean.class == td.getType())
         {
            batchInsertActivityInstances(session, maxSqlBatchSize, td, rows);
         }
         else if (ActivityInstanceHistoryBean.class == td.getType())
         {
            batchInsertActivityInstanceHistories(session, maxSqlBatchSize, td, rows);
         }
         else if (TransitionInstanceBean.class == td.getType())
         {
            batchInsertTransitionInstances(session, maxSqlBatchSize, td, rows);
         }
         else if (DataValueBean.class == td.getType())
         {
            batchInsertDataValues(session, maxSqlBatchSize, td, rows);
         }
         else if (LargeStringHolder.class == td.getType())
         {
            batchInsertLargeStrings(session, maxSqlBatchSize, td, rows);
         }
         else if (ClobDataBean.class == td.getType())
         {
            batchInsertClobs(session, maxSqlBatchSize, td, rows);
         }
         else
         {
            batchInsertRows(session, maxSqlBatchSize, td, rows);
         }
      }
   }

   private void batchInsertProcessInstances(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertProcessInstanceScopes(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertProcessInstanceHierarchies(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertActivityInstances(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertActivityInstanceHistories(Session session,
         int maxSqlBatchSize, TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertTransitionInstances(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertDataValues(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertLargeStrings(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertClobs(Session session, int maxSqlBatchSize,
         TypeDescriptor td, List rows)
   {
      batchInsertRows(session, maxSqlBatchSize, td, rows);
   }

   private void batchInsertRows(Session session, int maxSqlBatchSize, TypeDescriptor td,
         List rows)
   {
      final DmlManager dmlManager = session.getDMLManager(td.getType());

      final List fields = td.getPersistentFields();
      final List links = td.getLinks();

      BatchStatementWrapper insertRowStatement = null;
      PreparedStatement stmt = null;
      try
      {
         insertRowStatement = dmlManager.prepareInsertRowStatement(session.getConnection());

         stmt = insertRowStatement.getStatement();

         int batchSize = 0;
         int rowCounter = 0;
         for (Iterator rowItr = rows.iterator(); rowItr.hasNext();)
         {
            final Object[] rowValues = (Object[]) rowItr.next();

            if (trace.isDebugEnabled())
            {
               trace.debug("Inserting instance " + rowCounter++);
            }

            int slot = 0;

            for (int j = 0; j < fields.size(); ++j)
            {
               final FieldDescriptor field = (FieldDescriptor) fields.get(j);

               if ( !(session.getDBDescriptor().supportsIdentityColumns()
                     && td.requiresPKCreation()
                     && td.isPkField(field.getField())))
               {
                  DmlManager.setSQLValue(stmt, slot + 1, field.getField().getType(),
                        rowValues[slot], session.getDBDescriptor());
                  ++slot;
               }
            }

            for (int j = 0; j < links.size(); ++j)
            {
               final LinkDescriptor link = (LinkDescriptor) links.get(j);

               DmlManager.setSQLValue(stmt, slot + 1, link.getFkField().getType(),
                     rowValues[slot], session.getDBDescriptor());
               ++slot;
            }

            stmt.addBatch();
            ++batchSize;

            if ((batchSize >= maxSqlBatchSize) || !rowItr.hasNext())
            {
               stmt.executeBatch();
               batchSize = 0;
            }
         }
      }
      catch (SQLException sqle)
      {
         String sqlStatement = "<unknown>";
         if (null != insertRowStatement)
         {
            sqlStatement = insertRowStatement.getStatementString();
         }
         throw new PublicException(
               BpmRuntimeError.JMS_FAILED_PERSISTING_PROCESS_BLOB.raise(sqlStatement),
               sqle);
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }
   }

   public void persistBlob(BlobReader blob)
   {
      while (true)
      {
         final byte sectionMarker = blob.readByte();

         if (BlobBuilder.SECTION_MARKER_EOF == sectionMarker)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Reached end of BLOB. ");
            }

            break;
         }
         else if (BlobBuilder.SECTION_MARKER_INSTANCES == sectionMarker)
         {
            final String tableName = blob.readString();
            final int nInstances = blob.readInt();

            TypeDescriptor td = tdRegistry.getDescriptorForTable(tableName);

            final List fields = td.getPersistentFields();
            final List links = td.getLinks();

            try
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Extracting " + nInstances + " of type " + td.getType());
               }

               List rows = (List) batchInsertStatements.get(td);
               if (null == rows)
               {
                  rows = CollectionUtils.newLinkedList();

                  batchInsertStatements.put(td, rows);
               }

               for (int i = 0; i < nInstances; ++i)
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Reading instance " + i);
                  }

                  final Object[] rowValues = new Object[fields.size() + links.size()];

                  for (int j = 0; j < fields.size(); ++j)
                  {
                     FieldDescriptor field = (FieldDescriptor) fields.get(j);

                     Field javaField = field.getField();
                     Object value = readField(blob, javaField.getType());

                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Extracted value " + javaField.getName() + " -> "
                              + value);
                     }

                     rowValues[j] = value;
                  }

                  for (int j = 0; j < links.size(); ++j)
                  {
                     LinkDescriptor link = (LinkDescriptor) links.get(j);

                     Object fkValue = readField(blob, link.getFkField().getType());

                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Extracted foreign key " + link.getField().getName()
                              + " -> " + fkValue);
                     }

                     rowValues[fields.size() + j] = fkValue;
                  }

                  rows.add(rowValues);
               }

               if (trace.isDebugEnabled())
               {
                  trace.debug("Finished reading instances");
               }
            }
            catch (InternalException ie)
            {
               throw new PublicException(
                     BpmRuntimeError.JMS_FAILED_PERSISTING_BLOB_AT_TABLE.raise(td
                           .getTableName()), ie);
            }
         }
         else
         {
            throw new PublicException(
                  BpmRuntimeError.JMS_UNEXPECTED_SECTION_MARKER.raise(sectionMarker));
         }
      }
   }

   private static Object readField(BlobReader blob, Class fieldType)
         throws InternalException
   {
      // ordering by likelihood of occurrence

      if ((Long.TYPE == fieldType) || (Long.class == fieldType))
      {
         return new Long(blob.readLong());
      }
      else if ((Integer.TYPE == fieldType) || (Integer.class == fieldType))
      {
         return new Integer(blob.readInt());
      }
      else if (String.class == fieldType)
      {
         return blob.readString();
      }
      else if ((Boolean.TYPE == fieldType) || (Boolean.class == fieldType))
      {
         return new Boolean(blob.readBoolean());
      }
      else if ((Byte.TYPE == fieldType) || (Byte.class == fieldType))
      {
         return new Byte(blob.readByte());
      }
      else if ((Character.TYPE == fieldType) || (Character.class == fieldType))
      {
         return new Character(blob.readChar());
      }
      else if ((Short.TYPE == fieldType) || (Short.class == fieldType))
      {
         return new Short(blob.readShort());
      }
      else if ((Float.TYPE == fieldType) || (Float.class == fieldType))
      {
         return new Float(blob.readFloat());
      }
      else if ((Double.TYPE == fieldType) || (Double.class == fieldType))
      {
         return new Double(blob.readDouble());
      }
      else if (Date.class == fieldType)
      {
         return new Date(blob.readLong());
      }
      else
      {
         throw new InternalException("Unsupported field type: " + fieldType);
      }
   }

}