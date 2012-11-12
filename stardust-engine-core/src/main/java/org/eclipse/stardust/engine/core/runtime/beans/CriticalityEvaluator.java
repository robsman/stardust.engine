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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.core.javascript.CriticalityEvaluationAction;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ICriticalityEvaluator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

/**
 * 
 * @author thomas.wolfram
 * 
 */
public class CriticalityEvaluator implements ICriticalityEvaluator
{

   // defines the upper and lower limits for the criticality e.g.[0:1.0]
   private static final double CRITICALITY_UPPER_LIMIT = 1d;

   private static final double CRITICALITY_LOWER_LIMIT = 0d;

   private static final double CRITICALITY_FAULT_VALUE = -1d;

   private static final Logger trace = LogManager.getLogger(CriticalityEvaluator.class);

   public static double recalculateCriticality(long aiOid)
   {
      ActivityInstanceBean aiBean = ActivityInstanceBean.findByOID(aiOid);
      
      if (ProcessInstanceUtils.isTransientPiSupportEnabled())
      {
         final IProcessInstance pi = aiBean.getProcessInstance();
         if (AuditTrailPersistence.isTransientExecution(pi.getAuditTrailPersistence()))
         {
            /* for transient process instance execution the criticality feature */
            /* does not make any sense, but decreases performance               */
            return CRITICALITY_LOWER_LIMIT;
         }
      }

      return evaluateCriticality(aiBean);

   }

   private static double evaluateCriticality(ActivityInstanceBean aiBean)
   {
      try
      {
         ContextFactory jsContextFactory = ContextFactory.getGlobal();
         Object result = jsContextFactory.call(new CriticalityEvaluationAction(aiBean));
         return validateAndConvertCriticality(result, aiBean);
      }
      catch (Exception e)
      {
         trace.warn("Criticality formula evaluation caused an excpetion. Activity Criticality will be set to fault value <"
               + CRITICALITY_FAULT_VALUE + ">");
      }
      return CRITICALITY_FAULT_VALUE;
   }

   private static double validateAndConvertCriticality(Object resultObject,
         ActivityInstanceBean aiBean)
   {

      if (resultObject != null)
      {
         Double resultDouble = Context.toNumber(resultObject);

         if ( !resultDouble.isNaN())
         {
            return validateBoundaries(resultDouble, aiBean);
         }
      }
      else
      {
         trace.warn("Criticality of activity instance <" + aiBean.getOID()
               + "> is not a number and will be set to fault value <"
               + CRITICALITY_FAULT_VALUE + ">");
      }
      return CRITICALITY_FAULT_VALUE;
   }

   private static double validateBoundaries(double result, ActivityInstanceBean aiBean)
   {
      if (result > CRITICALITY_UPPER_LIMIT)
      {
         trace.warn("Criticality of activity instance <" + aiBean.getOID()
               + "> is greaten than the maximum value and will be set to <"
               + CRITICALITY_UPPER_LIMIT + ">");
         return CRITICALITY_UPPER_LIMIT;
      }
      else if (result < CRITICALITY_LOWER_LIMIT)
      {
         trace.warn("Criticality of activity instance <" + aiBean.getOID()
               + "> is less than the minimum value and will be set to <"
               + CRITICALITY_LOWER_LIMIT + ">");
         return CRITICALITY_LOWER_LIMIT;
      }
      else
      {
         return result;
      }
   }

}
