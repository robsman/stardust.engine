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

import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * Condition evaluator for calendar days.
 *
 * @author Roland.Stamm
 */
public class CalendarDaysCondition implements ConditionEvaluator
{
   private static Logger trace = LogManager.getLogger(CalendarDaysCondition.class);

   protected String qualifiedDataId;

   protected Comperator comperator;

   protected Offset offset;

   public CalendarDaysCondition(Comperator comperator, String qualifiedDataId,
         Offset offset)
   {
      this.comperator = comperator;
      this.qualifiedDataId = qualifiedDataId;
      this.offset = offset;
   }

   @Override
   public Boolean evaluate(ActivityInstanceBean ai)
   {
      Date date;
      try
      {
         date = getDateDateValue((ProcessInstanceBean) ai.getProcessInstance(),
               qualifiedDataId);
      }
      catch (ObjectNotFoundException e)
      {
         date = null;
      }

      if (date == null)
      {
         trace.warn("Could not resolve data with dataId '" + qualifiedDataId
               + "'. Using process instance start time.");
         date = ai.getProcessInstance().getStartTime();
      }

      return evaluate(date);
   }

   @Override
   public Boolean evaluate(ProcessInstanceBean pi)
   {
      Date date;
      try
      {
         date = getDateDateValue(pi, qualifiedDataId);
      }
      catch (ObjectNotFoundException e)
      {
         date = null;
      }

      if (date == null)
      {
         trace.warn("Could not resolve data with dataId '" + qualifiedDataId
               + "'. Using process instance start time.");
         date = pi.getStartTime();
      }
      return evaluate(date);
   }

   private Date getDateDateValue(ProcessInstanceBean pi, String qualifiedDataId)
   {
      Calendar calendar = null;

      if (qualifiedDataId != null)
      {
         String dataId = QName.valueOf(qualifiedDataId).getLocalPart();
         IModel iModel = (IModel) pi.getProcessDefinition().getModel();
         IData iData = iModel.findData(dataId);
         if (iData != null)
         {
            calendar = (Calendar) pi.getInDataValue(iData, dataId);
         }
      }
      return calendar == null ? null : calendar.getTime();
   }

   private boolean evaluate(Date date)
   {
      Date offsetDate = applyOffset(date, offset);
      if (offsetDate == null)
      {
         // offset calculation failed, condition is not met.
         return false;
      }

      boolean result = false;
      Date currentTime = TimestampProviderUtils.getTimeStamp();
      if (Comperator.LATER_THAN.equals(comperator))
      {
         result = currentTime.after(offsetDate);
      }
      else if (Comperator.NOT_LATER_THAN.equals(comperator))
      {
         result = currentTime.before(offsetDate);
      }
      return result;
   }

   protected Date applyOffset(Date date, Offset offset)
   {
      if (offset != null)
      {
         Calendar calendar = Calendar.getInstance();

         calendar.setTime(date);

         switch (offset.getUnit())
         {
            case DAYS:
               calendar.add(Calendar.DAY_OF_YEAR, offset.getAmount());
               break;
            case WEEKS:
               calendar.add(Calendar.WEEK_OF_YEAR, offset.getAmount());
               break;
            case MONTHS:
               calendar.add(Calendar.MONTH, offset.getAmount());
               break;
         }

         // apply offset time
         if (offset.getHour() != null && offset.getMinute() != null)
         {
            calendar.set(Calendar.HOUR_OF_DAY, offset.getHour());
            calendar.set(Calendar.MINUTE, offset.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
         }

         return calendar.getTime();
      }
      return date;
   }

   public enum Comperator
   {
      LATER_THAN, NOT_LATER_THAN
   }

}
