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

/**
 * @author Sebastian Woelk
 * @version $Revision$
 */
public abstract class AbstractTableInfo
{

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
      public final String name;
      public final Class type;
      public final int size;
      
      public final boolean isPK;
      
      public FieldInfo(String name, Class type)
      {
         this(name, type, 0);
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
   }
   
   public static class IndexInfo
   {
      public final String name;
      public final FieldInfo[] fields;

      public final boolean unique;
      
      public IndexInfo(final String name, final FieldInfo[] fields)
      {
         this(name, false, fields);
      }

      public IndexInfo(final String name, boolean unique, final FieldInfo[] fields)
      {
         this.name = name;
         this.fields = fields;
         
         this.unique = unique;
      }
   }
}
