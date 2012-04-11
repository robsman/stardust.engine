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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.IUnaryFunction;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SqlUtils
{
   private String schemaName;
   private DBDescriptor dbDescriptor;

   public static List<FieldRef> getDefaultSelectFieldList(ITypeDescriptor type)
   {
      List<FieldRef> result = newArrayList(type.getPersistentFields().size() + type.getLinks().size());

      List<FieldDescriptor> persistentFields = type.getPersistentFields();
      for (FieldDescriptor fd : persistentFields)
      {
         result.add(type.fieldRef(fd.getField().getName()));
      }

      List<LinkDescriptor> links = type.getLinks();
      for (LinkDescriptor ld : links)
      {
         result.add(type.fieldRef(ld.getField().getName()));
      }

      return result;
   }

   public SqlUtils(String schemaName, DBDescriptor dbDescriptor)
   {
      this.schemaName = schemaName;
      this.dbDescriptor = dbDescriptor;
   }

   String getFieldRef(String alias, String attr)
   {
      if (!StringUtils.isEmpty(alias))
      {
         return alias + "." + attr;
      }
      else
      {
         return attr;
      }
   }

   public void appendTableRef(StringBuffer buffer, ITableDescriptor table)
   {
      appendTableRef(buffer, table, true);
   }

   public void appendTableRef(StringBuffer buffer, ITableDescriptor table,
         boolean useAlias)
   {
      if ( !StringUtils.isEmpty(table.getSchemaName()))
      {
         buffer.append(table.getSchemaName()).append(".");
      }
      else if ( !StringUtils.isEmpty(schemaName))
      {
         buffer.append(schemaName).append(".");
      }

      buffer.append(dbDescriptor.quoteIdentifier(table.getTableName()));

      if (useAlias && !StringUtils.isEmpty(table.getTableAlias()))
      {
         buffer.append(" ").append(table.getTableAlias());
      }
   }

   public void appendFieldRef(StringBuffer buffer, FieldRef fieldRef)
   {
      appendFieldRef(buffer, fieldRef, null);
   }

   public void appendFieldRef(StringBuffer buffer, FieldRef fieldRef, String selectAlias)
   {
      appendFieldRef(buffer, fieldRef, true, selectAlias);
   }

   public void appendFieldRef(StringBuffer buffer, FieldRef fieldRef,
         boolean useAlias)
   {
      appendFieldRef(buffer, fieldRef, useAlias, null);
   }

   public void appendFieldRef(StringBuffer buffer, FieldRef fieldRef,
         boolean useAlias, String selectAlias)
   {
      if (fieldRef instanceof IUnaryFunction)
      {
         buffer.append(((IUnaryFunction) fieldRef).getFunctionName()).append("(");
      }

      if (useAlias)
      {
         // Only use custom select alias for TypeDescriptors, any additions e.g. data prefetch have to be ignored.
         if ( !StringUtils.isEmpty(selectAlias) && fieldRef.getType() instanceof TypeDescriptor)
         {
            buffer.append(selectAlias).append(".");
         }
         else if ( !StringUtils.isEmpty(fieldRef.getType().getTableAlias()))
         {
            buffer.append(fieldRef.getType().getTableAlias()).append(".");
         }
         else
         {
            if ( !StringUtils.isEmpty(fieldRef.getType().getSchemaName()))
            {
               buffer.append(fieldRef.getType().getSchemaName()).append(".");
            }
            else if ( !StringUtils.isEmpty(schemaName))
            {
               buffer.append(schemaName).append(".");
            }

            buffer.append(fieldRef.getType().getTableName()).append(".");
         }
      }

      buffer.append(dbDescriptor.quoteIdentifier(fieldRef.fieldName));

      if (fieldRef instanceof IUnaryFunction)
      {
         buffer.append(")");
      }
   }

   public void appendDefaultSelectList(StringBuffer buffer, QueryDescriptor query)
   {
      String selectAlias = query.getQueryExtension().getSelectAlias();
      appendDefaultSelectList(buffer, query, true, selectAlias);
   }

   public void appendDefaultSelectList(StringBuffer buffer, ITypeDescriptor td)
   {
      appendDefaultSelectList(buffer, td, true);
   }

   public void appendDefaultSelectList(StringBuffer buffer, ITypeDescriptor type,
         boolean useTableAlias)
   {
      appendDefaultSelectList(buffer, type, useTableAlias, null);
   }

   public void appendDefaultSelectList(StringBuffer buffer, ITypeDescriptor type,
         boolean useTableAlias, String selectAlias)
   {
      String joinToken = "";

      List persistentFields = type.getPersistentFields();
      for (int n = 0; n < persistentFields.size(); ++n)
      {
         buffer.append(joinToken);
         joinToken = ", ";

         FieldDescriptor fd = (FieldDescriptor) persistentFields.get(n);

         if (null != fd.getFieldDecryptFunction())
         {
            buffer.append(fd.getFieldDecryptFunction()).append("(");
         }

         if (useTableAlias)
         {
            if ( !StringUtils.isEmpty(selectAlias))
            {
               buffer.append(selectAlias).append(".");
            }
            else if ( !StringUtils.isEmpty(type.getTableAlias()))
            {
               buffer.append(type.getTableAlias()).append(".");
            }
         }

         String fieldName = fd.getField().getName();
         buffer.append(dbDescriptor.quoteIdentifier(fieldName));

         if (null != fd.getFieldDecryptFunction())
         {
            buffer.append(", '").append(type.getDecryptKey()).append("')");
         }
      }

      List links = type.getLinks();
      for (int m = 0; m < links.size(); ++m)
      {
         buffer.append(joinToken);
         joinToken = ", ";

         LinkDescriptor ld = (LinkDescriptor) links.get(m);
         ld.getField().setAccessible(true);

         if (useTableAlias)
         {
            if ( !StringUtils.isEmpty(selectAlias))
            {
               buffer.append(selectAlias).append(".");
            }
            else if ( !StringUtils.isEmpty(type.getTableAlias()))
            {
               buffer.append(type.getTableAlias()).append(".");
            }
         }
         String fieldName = ld.getField().getName();
         buffer.append(dbDescriptor.quoteIdentifier(fieldName));
      }
   }

   /**
    * @return Returns the schemaName.
    */
   public String getSchemaName()
   {
      return schemaName;
   }
}
