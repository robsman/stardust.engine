/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.api.model.Transition;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;

/**
 * The client view of a workflow transition.
 *
 * @author fherinean
 */
public class TransitionDetails implements Transition
{
   private static final long serialVersionUID = 1L;

   private String id;
   private String sourceActivityId;
   private String targetActivityId;
   private ConditionType conditionType;
   private String condition;
   //private boolean forkOnTraversal;

   TransitionDetails(ITransition transition, DetailsCache cache)
   {
      id = transition.getId();
      sourceActivityId = transition.getFromActivity().getId();
      targetActivityId = transition.getToActivity().getId();
      conditionType = ConditionType.Condition;
      condition = transition.getCondition();
      if (XMLConstants.CONDITION_OTHERWISE_VALUE.equals(condition))
      {
         conditionType = ConditionType.Otherwise;
         condition = null;
      }
      //forkOnTraversal = transition.getForkOnTraversal();
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getSourceActivityId()
   {
      return sourceActivityId;
   }

   @Override
   public String getTargetActivityId()
   {
      return targetActivityId;
   }

   @Override
   public String getCondition()
   {
      return condition;
   }

   @Override
   public ConditionType getConditionType()
   {
      return conditionType;
   }

   /*
   @Override
   public boolean isForkOnTraversal()
   {
      return forkOnTraversal;
   }*/
}
