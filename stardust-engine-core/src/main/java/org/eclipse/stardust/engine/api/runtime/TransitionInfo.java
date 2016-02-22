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
public abstract class TransitionInfo implements Serializable, ActivityInstanceContextAware
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

   /**
    * Retrieves the source activity instance OID.
    * <br>
    * For the case of a TransitionTarget, this represents the activity instance from where the ad-hoc
    * transition is performed.
    * <br>
    * For the case of a TransitionStep, this is either the OID of the activity instance that started the subprocess
    * or -1 if there is no subprocess started yet.
    * <br>
    * This value is necessary to perform the actual transition, but is not relevant for clients.
    *
    * @return a long representing the OID of the activity instance or -1 if there is no corresponding activity instance.
    */
   @Override
   public long getActivityInstanceOid()
   {
      return activityInstanceOid;
   }

   /**
    * Retrieves the OID of the model containing the activity that represents the target of this operation.
    * <br>
    * This value is necessary to perform the actual transition, but is not relevant for clients.
    *
    * @return a long representing the OID of the deployed model.
    */
   public long getModelOid()
   {
      return modelOid;
   }

   /**
    * Retrieves the runtime OID of the activity that represents the target of this operation.
    * <br>
    * This value is necessary to perform the actual transition, but is not relevant for clients.
    * Together with the model OID is used to uniquely identify a specific activity in the model repository.
    *
    * @return a long representing the runtimeOID of the activity.
    */
   public long getActivityRuntimeOid()
   {
      return activityRuntimeOid;
   }

   /**
    * Retrieves the Id of the activity that represents the target of this operation.
    *
    * @return a string containing the id of the activity.
    */
   public String getActivityId()
   {
      return activityId;
   }

   /**
    * Retrieves the Name of the activity that represents the target of this operation.
    *
    * @return a string containing the name of the activity.
    */
   public String getActivityName()
   {
      return activityName;
   }

   /**
    * Retrieves the Id of the process definition containing the activity that represents the target of this operation.
    *
    * @return a string containing the id of the process definition.
    */
   public String getProcessId()
   {
      return processId;
   }

   /**
    * Retrieves the Name of the process definition containing the activity that represents the target of this operation.
    *
    * @return a string containing the name of the process definition.
    */
   public String getProcessName()
   {
      return processName;
   }

   /**
    * Retrieves the Id of the model containing the activity that represents the target of this operation.
    *
    * @return a string containing the id of the model.
    */
   public String getModelId()
   {
      return modelId;
   }

   /**
    * Retrieves the Name of the model containing the activity that represents the target of this operation.
    *
    * @return a string containing the name of the model.
    */
   public String getModelName()
   {
      return modelName;
   }

   @Override
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
