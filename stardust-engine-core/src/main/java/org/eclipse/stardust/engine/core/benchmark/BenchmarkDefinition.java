/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.core.spi.artifact.ArtifactManagerFactory;

/**
 *
 * @author Thomas.Wolfram
 *
 */
public class BenchmarkDefinition
{

   long oid;

   Map<String, TreeMap<Integer,ConditionEvaluator>> processConditions;

   Map<Pair<String, String>, TreeMap<Integer,ConditionEvaluator>> activityConditions;

   Map<Integer, Map<String,Serializable>> properties;

   String businessCalendarId;

   public BenchmarkDefinition(long benchmarkOid)
   {
      this.oid = benchmarkOid;
      this.activityConditions = CollectionUtils.newMap();
      this.processConditions = CollectionUtils.newMap();

      this.properties = CollectionUtils.newMap();

      RuntimeArtifact ra = ArtifactManagerFactory.getCurrent().getArtifact(benchmarkOid);
      
      if (ra == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_UNKNOWN_RUNTIME_ARTIFACT_OID.raise(oid));
      }
      
      BenchmarkDefinitionParser.parse(this, ra.getContent());
   }

   public long getOid()
   {
      return oid;
   }


   public TreeMap<Integer, ConditionEvaluator> getProcessConditions(String processId)
   {
      return processConditions.get(processId);
   }

   public TreeMap<Integer, ConditionEvaluator> getActivityConditions(Pair<String,String> activityId)
   {
      return activityConditions.get(activityId);
   }

   public Map<String,Serializable> getProperty(int category)
   {
      return this.properties.get(category);
   }

   public String getBusinessCalendarId()
   {
      return businessCalendarId;
   }

}
