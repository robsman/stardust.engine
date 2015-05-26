/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    roland.stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.benchmark;

import java.util.Date;

import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;

public class DateCondition implements ConditionEvaluator
{
   protected String qualifiedDataId;

   protected Comperator comperator;

   @Override
   public Boolean evaluate(ActivityInstanceBean ai)
   {
      Date date = getDateDateValue((ProcessInstanceBean) ai.getProcessInstance(), qualifiedDataId);

      return evaluate(date);
   }

   @Override
   public Boolean evaluate(ProcessInstanceBean pi)
   {
      Date date = getDateDateValue(pi, qualifiedDataId);

      return evaluate(date);
   }

   private Date getDateDateValue(ProcessInstanceBean pi, String qualifiedDataId)
   {
        return (Date) pi.getDataValue(qualifiedDataId);
   }

   private boolean evaluate(Date date)
   {
      boolean result = false;
      Date currentTime = new Date();
      if (Comperator.LATER_THAN.equals(comperator))
      {
         result = currentTime.after(date);
      }
      else if (Comperator.NOT_LATER_THAN.equals(comperator))
      {
         result = currentTime.before(date);
      }
      return result;
   }

   public enum Comperator
   {
      LATER_THAN, NOT_LATER_THAN
   }

}
