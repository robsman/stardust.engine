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

import javax.xml.namespace.QName;

/**
 * 
 * @author rsauer
 * @version $Revision$
 */
public class DataSlot
{
   private final QName fqDataId;
   private final String attributeName; 
   
   private final String oidColumn;
   private final String typeColumn;
   private final String nValueColumn;
   private final String sValueColumn;
   private final boolean ignorePreparedStatements;

   public DataSlot(String modelId, String dataId, String attributeName, String oidColumn, String typeColumn,
         String nValueColumn, String sValueColumn, boolean ignorePreparedStatements)
   {
      this.fqDataId = new QName(modelId, dataId);
      this.attributeName = attributeName;
      this.oidColumn = oidColumn;
      this.typeColumn = typeColumn;
      this.nValueColumn = nValueColumn;
      this.sValueColumn = sValueColumn;
      this.ignorePreparedStatements = ignorePreparedStatements;
   }
   
   public String getModelId()
   {
      return fqDataId.getNamespaceURI();
   }

   public String getDataId()
   {
      return fqDataId.getLocalPart();
   }
   
   public String getQualifiedDataId()
   {
      return fqDataId.toString();
   }

   public String getAttributeName()
   {
      return this.attributeName;
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

   public boolean isIgnorePreparedStatements()
   {
      return ignorePreparedStatements;
   }
}
