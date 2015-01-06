/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.api.monitoring;



/**
 * <p>
 * This class bundles a database operation with the table's name
 * on which the operation has been performed.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TableOperation
{
   private final Operation op;
   private final String tableName;

   public TableOperation(final Operation op, final String tableName)
   {
      if (op == null)
      {
         throw new NullPointerException("Operation must not be null.");
      }

      if (tableName == null)
      {
         throw new NullPointerException("Table name must not be null.");
      }
      if (tableName.isEmpty())
      {
         throw new IllegalArgumentException("Table name must not be empty.");
      }

      this.op = op;
      this.tableName = tableName.toLowerCase();
   }

   public Operation operation()
   {
      return op;
   }

   public String tableName()
   {
      return tableName;
   }

   public TableOperation[] times(final int times)
   {
      if (times <= 0)
      {
         throw new IllegalArgumentException("Times must be greater than null.");
      }

      final TableOperation[] result = new TableOperation[times];
      for (int i=0; i<times; i++)
      {
         result[i] = this;
      }
      return result;
   }

   @Override
   public int hashCode()
   {
      int result = 17;
      result = 31 * result + op.hashCode();
      result = 31 * result + tableName.hashCode();
      return result;
   }

   @Override
   public boolean equals(final Object obj)
   {
      if ( !(obj instanceof TableOperation))
      {
         return false;
      }

      final TableOperation that = (TableOperation) obj;
      return   this.op.equals(that.op)
            && this.tableName.equals(that.tableName);
   }

   @Override
   public String toString()
   {
      return "{" + op + ", " + tableName + "}";
   }
}
