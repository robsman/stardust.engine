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

import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;

/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public final class TransitionTargetFactory
{
   public static TransitionStep createTransitionStep(IActivityInstance ai)
   {
      return createTransitionStep(ai.getOID(), ai.getActivity());
   }

   public static TransitionStep createTransitionStep(IActivity activity)
   {
      return createTransitionStep(-1, activity);
   }

   private static TransitionStep createTransitionStep(long activityInstanceOid, IActivity activity)
   {
      String activityId = activity.getId();
      String activityName = activity.getName();
      
      IProcessDefinition process = activity.getProcessDefinition();
      String processId = process.getId();
      String processName = process.getName();
      
      IModel model = (IModel) process.getModel();
      String modelId = model.getId();
      String modelName = model.getName();
      
      long modelOid = model.getModelOID();
      ModelManager mm = ModelManagerFactory.getCurrent();
      long activityRuntimeOid = mm.getRuntimeOid(activity);
      
      return new TransitionStep(activityInstanceOid, modelOid, activityRuntimeOid, activityId,
            activityName, processId, processName, modelId, modelName);
   }

   public static TransitionTarget createTransitionTarget(IActivity activity, Stack<TransitionStep> currentSteps, boolean forward)
   {
      String activityId = activity.getId();
      String activityName = activity.getName();
      
      IProcessDefinition process = activity.getProcessDefinition();
      String processId = process.getId();
      String processName = process.getName();
      
      IModel model = (IModel) process.getModel();
      String modelId = model.getId();
      String modelName = model.getName();
      
      long modelOid = model.getModelOID();
      ModelManager mm = ModelManagerFactory.getCurrent();
      long activityRuntimeOid = mm.getRuntimeOid(activity);
      
      TransitionStep first = currentSteps.get(0);
      List<TransitionStep> steps = Collections.emptyList();
      if (currentSteps.size() > 1)
      {
         steps = CollectionUtils.newList(currentSteps.size() - 1);
         steps.addAll(currentSteps.subList(1, currentSteps.size()));
         steps = Collections.unmodifiableList(steps);
      }
      
      return new TransitionTarget(first.getActivityInstanceOid(), modelOid, activityRuntimeOid, activityId,
            activityName, processId, processName, modelId, modelName, steps, forward);
   }
}
