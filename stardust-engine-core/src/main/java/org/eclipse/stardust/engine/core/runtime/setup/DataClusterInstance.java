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

import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;

public class DataClusterInstance
{
   private DataCluster clusterDefinition;
   private long processInstanceOid;
   
   public DataClusterInstance(DataCluster clusterDefinition, long piOid)
   {
      this.clusterDefinition = clusterDefinition;
      processInstanceOid = piOid;
      
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this); 
   }
   
   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }
   
   public String getTableName()
   {
      return clusterDefinition.getTableName();
   }
   
   public String getProcessInstanceColumn()
   {
      return clusterDefinition.getProcessInstanceColumn();
   }
}
