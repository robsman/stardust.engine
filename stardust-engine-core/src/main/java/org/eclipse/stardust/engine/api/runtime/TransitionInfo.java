/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * Base class containing common information of transition steps and targets.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public abstract class TransitionInfo implements Serializable
{
   private static final long serialVersionUID = 1L;

   private long activityInstanceOid;

   private long modelOid;
   private long activityRuntimeOid;
   
   private String activityId;
   private String activityName;

   private String processId;
   private String processName;

   private String modelId;
   private String modelName;
   
   TransitionInfo(long activityInstanceOid, long modelOid, long activityRuntimeOid, String activityId,
         String activityName, String processId, String processName, String modelId, String modelName)
   {
      this.activityInstanceOid = activityInstanceOid;
      this.modelOid = modelOid;
      this.activityRuntimeOid = activityRuntimeOid;
      this.activityId = activityId;
      this.activityName = activityName;
      this.processId = processId;
      this.processName = processName;
      this.modelId = modelId;
      this.modelName = modelName;
   }

   public long getActivityInstanceOid()
   {
      return activityInstanceOid;
   }
   
   /**
    * Retrieves the OID of the model containing the activity.
    */
   public long getModelOid()
   {
      return modelOid;
   }
   
   public long getActivityRuntimeOid()
   {
      return activityRuntimeOid;
   }
   
   /**
    * Retrieves the activity id.
    */
   public String getActivityId()
   {
      return activityId;
   }

   /**
    * Retrieves the activity name.
    */
   public String getActivityName()
   {
      return activityName;
   }
   
   /**
    * Retrieves the process definition id.
    */
   public String getProcessId()
   {
      return processId;
   }
   
   /**
    * Retrieves the process definition name.
    */
   public String getProcessName()
   {
      return processName;
   }
   
   /**
    * Retrieves the model id.
    */
   public String getModelId()
   {
      return modelId;
   }
   
   /**
    * Retrieves the model name.
    */
   public String getModelName()
   {
      return modelName;
   }

   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (activityInstanceOid ^ (activityInstanceOid >>> 32));
      result = prime * result + (int) (activityRuntimeOid ^ (activityRuntimeOid >>> 32));
      result = prime * result + (int) (modelOid ^ (modelOid >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      TransitionInfo other = (TransitionInfo) obj;
      return activityInstanceOid == other.activityInstanceOid
            && activityRuntimeOid == other.activityRuntimeOid
            && modelOid == other.modelOid;
   }
}
