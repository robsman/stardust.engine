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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Sebastian Woelk
 * @version $Revision$
 */
public abstract class AbstractTableInfo
{
   private List<FieldInfo> fields = new ArrayList<FieldInfo>();
   private List<IndexInfo> indexes = new ArrayList<IndexInfo>();
   
   /**
    * The name of the table this info object belongs to.
    */
   private String tableName = null;

   /**
    * When <code>true</true> try to drop the object before it will created.
    */
   private boolean tryDrop = false;


   /**
    *
    */
   public AbstractTableInfo(String tableName)
   {
      this(tableName, false);
   }

   public FieldInfo[] getFields()
   {
      return fields.toArray(new FieldInfo[fields.size()]);
   }

   public IndexInfo[] getIndexes()
   {
      return indexes.toArray(new IndexInfo[indexes.size()]);
   }
   
   /**
    *
    */
   public AbstractTableInfo(String tableName, boolean tryDrop)
   {
      if (tableName == null)
      {
         throw new NullPointerException("TableName my not be null");
      }

      this.tableName = tableName;
      this.tryDrop = tryDrop;
   }

   /**
    *
    */
   public String getTableName()
   {
      return tableName;
   }

   /**
    *
    */
   protected boolean tryDrop()
   {
      return tryDrop;
   }

   /**
    *
    */
   public void create(RuntimeItem item) throws SQLException 
   {
      if (tryDrop())
      {
         try
         {
            drop(item);
         }
         catch(SQLException se)
         {
            if (se.getErrorCode() != 
                      DatabaseHelper.ORACLE_ERROR_TABLE_NOT_EXIST)
            {
               throw se;
            }
         }
      }
      
      doCreate(item);
   }

   /**
    *
    */
   public abstract void doCreate(RuntimeItem item) throws SQLException;

   /**
    *
    */
   public abstract void drop(RuntimeItem item) throws SQLException;

   public static class FieldInfo
   {
      private final String name;
      public final Class type;
      public final int size;
      
      public final boolean isPK;
      
      public FieldInfo(String name, Class type)
      {
         this(name, type, 0);
      }

      public FieldInfo(String name, Class type, boolean isPK)
      {
         this(name, type, 0, isPK);
      }

      public FieldInfo(String name, Class type, int size)
      {
         this(name, type, size, false);
      }

      public FieldInfo(String name, Class type, int size, boolean isPK)
      {
         this.name = name;
         this.type = type;
         this.size = size;
         this.isPK = isPK;
      }

      public String getName()
      {
         return name;
      }
   }
   
   public static class IndexInfo
   {
      public final String name;
      public final FieldInfo[] fields;

      public final boolean unique;
      
      public IndexInfo(final String name, final FieldInfo...fields)
      {
         this(name, false, fields);
      }

      public IndexInfo(final String name, boolean unique, final FieldInfo...fields)
      {
         this.name = name;
         this.fields = fields;
         
         this.unique = unique;
      }

      public String getName()
      {
         return name;
      }

      public FieldInfo[] getFields()
      {
         return fields;
      }

      public boolean isUnique()
      {
         return unique;
      }
   }
   
   public void addField(FieldInfo info)
   {
      fields.add(info);
   }
   
   public void removeField(FieldInfo info)
   {
      Iterator<FieldInfo> fieldIterator = fields.iterator();
      while(fieldIterator.hasNext())
      {
         FieldInfo tmp = fieldIterator.next();
         if(tmp.equals(info))
         {
            fieldIterator.remove();
            return;
         }
      }
   }
   
   public void addIndex(IndexInfo info)
   {
      indexes.add(info);
   }
   
   public void removeIndex(IndexInfo info)
   {
      Iterator<IndexInfo> indexIterator = indexes.iterator();
      while(indexIterator.hasNext())
      {
         IndexInfo tmp = indexIterator.next();
         if(tmp.equals(info))
         {
            indexIterator.remove();
            return;
         }
      }
   }

   public static class IndexWithTableInfo extends IndexInfo
   {
      private String tableName;

      public IndexWithTableInfo(final String name, String tableName, final FieldInfo...fields)
      {
         this(name, tableName, false, fields);
      }

      public IndexWithTableInfo(final String name, String tableName, boolean unique, final FieldInfo...fields)
      {
         super(name, unique, fields);
         this.tableName = tableName;
      }

      public String getTableName()
      {
         return tableName;
      }
   }
}
