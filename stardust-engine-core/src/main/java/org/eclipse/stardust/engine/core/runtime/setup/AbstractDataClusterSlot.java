/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.setup;

/**
*
* @author stephan.born
*/
public abstract class AbstractDataClusterSlot
{
   private DataCluster parent;
   private final String oidColumn;
   private final String typeColumn;
   private final String nValueColumn;
   private final String sValueColumn;
   private final String dValueColumn;
   private final boolean ignorePreparedStatements;

   public AbstractDataClusterSlot(String oidColumn,
         String typeColumn, String nValueColumn, String sValueColumn,
         String dValueColumn, boolean ignorePreparedStatements)
   {
      this.oidColumn = oidColumn;
      this.typeColumn = typeColumn;
      this.nValueColumn = nValueColumn;
      this.sValueColumn = sValueColumn;
      this.dValueColumn = dValueColumn;
      this.ignorePreparedStatements = ignorePreparedStatements;
   }

   public String getOidColumn()
   {
      return oidColumn;
   }

   public String getTypeColumn()
   {
      return typeColumn;
   }

   public String getNValueColumn()
   {
      return nValueColumn;
   }

   public String getSValueColumn()
   {
      return sValueColumn;
   }

   public String getDValueColumn()
   {
      return dValueColumn;
   }

   public boolean isIgnorePreparedStatements()
   {
      return ignorePreparedStatements;
   }

   public DataCluster getParent()
   {
      return parent;
   }

   public void setParent(DataCluster parent)
   {
      this.parent = parent;
   }

   public abstract boolean hasPrimitiveData();

   public abstract boolean hasStructuredData();

}
