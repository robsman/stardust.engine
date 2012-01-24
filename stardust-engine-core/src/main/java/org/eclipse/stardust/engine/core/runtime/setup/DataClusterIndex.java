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
package org.eclipse.stardust.engine.core.runtime.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author sborn
 * @version $Revision$
 */
public class DataClusterIndex
{
   private final String tableName;
   private final String indexName;
   private final boolean isUnique;
   private final List columnNames;
   
   public DataClusterIndex(String tableName, String indexName, boolean isUnique, List columnNames)
   {
      this.tableName = tableName;
      this.indexName = indexName;
      this.isUnique = isUnique;
      this.columnNames = new ArrayList(columnNames);
   }

   public List getColumnNames()
   {
      return Collections.unmodifiableList(columnNames);
   }

   public String getIndexName()
   {
      return indexName;
   }

   public boolean isUnique()
   {
      return isUnique;
   }

   public String getTableName()
   {
      return tableName;
   }
}
