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

import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.benchmark.ConditionParameter.ParameterType;
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
   private static final String CURRENT_TIME_ATTRIBUE = "CURRENT_TIME";
   
   private static final String PROCESS_START_TIME_ATTRIBUTE = "PROCESS_START_TIME";
   
   private static final String ROOT_PROCESS_START_TIME_ATTRIBUTE = "ROOT_PROCESS_START_TIME";
   
   private static Logger trace = LogManager.getLogger(CalendarDaysCondition.class);

   protected String qualifiedDataId;

   protected Comperator comperator;

   protected Offset offset;
   
   protected String dataPath;
   
   protected ConditionParameter lhsParameter;
   
   protected ConditionParameter rhsParameter;
   

   /*
   public CalendarDaysCondition(Comperator comperator, String qualifiedDataId, String dataPath,
         Offset offset)
   {
      this.comperator = comperator;
      this.qualifiedDataId = qualifiedDataId;
      this.offset = offset;
      this.dataPath = dataPath;
   }
   */
   
   public CalendarDaysCondition(ConditionParameter lhsParameter, Comperator comperator, ConditionParameter rhsParameter, Offset offset)
   {
      this.lhsParameter = lhsParameter;
      this.comperator = comperator;
      this.rhsParameter = rhsParameter;
      this.offset = offset;
   }
   

   @Override
   public Boolean evaluate(ActivityInstanceBean ai)
   {
      Date rhsDate = null;
      Date lhsDate = null;

      if (this.rhsParameter.getType().equals(ParameterType.DATA))
      {
         rhsDate = getDateValue((ProcessInstanceBean) ai.getProcessInstance(),
               this.rhsParameter.getParameterId(), this.rhsParameter.getDataPath());
      }
      else if (this.rhsParameter.getType().equals(ParameterType.ATTRIBUTE))
      {
         rhsDate = getAttributeValue((ProcessInstanceBean) ai.getProcessInstance(), this.rhsParameter.getParameterId());
      }
      
      if (this.lhsParameter.getType().equals(ParameterType.DATA))
      {
         lhsDate = getDateValue((ProcessInstanceBean) ai.getProcessInstance(),
               this.lhsParameter.getParameterId(), this.lhsParameter.getDataPath());
      }
      else if (this.lhsParameter.getType().equals(ParameterType.ATTRIBUTE))
      {
         lhsDate = getAttributeValue((ProcessInstanceBean) ai.getProcessInstance(), this.lhsParameter.getParameterId());
      }      

      if (lhsDate == null || rhsDate == null)
      {
         String invalidParam = rhsDate == null
               ? rhsParameter.getParameterId()
               : lhsParameter.getParameterId();
               
         Date invalidDate = rhsDate == null ? rhsDate : lhsDate;               
               
         trace.warn("Data '"
               + invalidParam
               + "' is not initialized or does not exist. Using process instance start time for calculation.");
         throw new InvalidValueException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise(
               this.rhsParameter.getParameterId(), invalidDate));

      }

      return evaluate(lhsDate, rhsDate);
   }

   @Override
   public Boolean evaluate(ProcessInstanceBean pi)
   {
      Date rhsDate = null;
      Date lhsDate = null;

      if (this.rhsParameter.getType().equals(ParameterType.DATA))
      {
         rhsDate = getDateValue(pi, this.rhsParameter.getParameterId(),
               this.rhsParameter.getDataPath());
      }
      else if (this.rhsParameter.getType().equals(ParameterType.ATTRIBUTE))
      {
         rhsDate = getAttributeValue((ProcessInstanceBean) pi,
               this.rhsParameter.getParameterId());
      }

      if (this.lhsParameter.getType().equals(ParameterType.DATA))
      {
         lhsDate = getDateValue(pi, this.lhsParameter.getParameterId(),
               this.lhsParameter.getDataPath());
      }
      else if (this.lhsParameter.getType().equals(ParameterType.ATTRIBUTE))
      {
         lhsDate = getAttributeValue((ProcessInstanceBean) pi,
               this.lhsParameter.getParameterId());
      }

      if (rhsDate == null || lhsDate == null)
      {
         String invalidParam = rhsDate == null
               ? rhsParameter.getParameterId()
               : lhsParameter.getParameterId();
               
         Date invalidDate = rhsDate == null ? rhsDate : lhsDate;
         
         trace.warn("Data or attribute '" + invalidParam
               + "' is not initialized or does not exist.");

         throw new InvalidValueException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise(
      invalidParam, invalidDate));
      }
      return evaluate(lhsDate, rhsDate);
   }

   private Date getAttributeValue(ProcessInstanceBean processInstance, String parameterId)
   {
      if (parameterId.equals(CURRENT_TIME_ATTRIBUE))
      {
         return TimestampProviderUtils.getTimeStamp();
      }
      else if (parameterId.equals(PROCESS_START_TIME_ATTRIBUTE))
      {
         return processInstance.getStartTime();
      }
      else if (parameterId.equals(ROOT_PROCESS_START_TIME_ATTRIBUTE))
      {
         processInstance.getRootProcessInstance().getStartTime();
      }
      return null;
   }


   private Date getDateValue(ProcessInstanceBean pi, String qualifiedDataId, String dataPath)
   {
      Date time = null;

      if (qualifiedDataId != null)
      {
         String dataId = QName.valueOf(qualifiedDataId).getLocalPart();
   
         IModel iModel = (IModel) pi.getProcessDefinition().getModel();
         IData iData = iModel.findData(dataId);
         if (iData != null)
         {
            Object inDataValue;
            if (dataPath != null)
            {
               inDataValue = pi.getInDataValue(iData,  dataPath);
            }
            else
            {
               inDataValue = pi.getInDataValue(iData, dataId);
            }

            if (inDataValue instanceof Calendar)
            {
               time = ((Calendar) inDataValue).getTime();
            }
            else if (inDataValue instanceof Date)
            {
               time = (Date) inDataValue;
            }
            else
            {
               throw new InvalidValueException(
                     BpmRuntimeError.BPMRT_INCOMPATIBLE_TYPE_FOR_DATA.raise(dataId));
            }
         }
         else
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(qualifiedDataId));
         }
      }
      return time;
   }

   private boolean evaluate(Date lhsDate, Date rhsDate)
   {
      Date offsetDate = applyOffset(rhsDate, offset);
      if (offsetDate == null)
      {
         // offset calculation failed, condition is not met.
         return false;
      }

      boolean result = false;
      // Date currentTime = TimestampProviderUtils.getTimeStamp();
      if (Comperator.LATER_THAN.equals(comperator))
      {
         result = lhsDate.after(offsetDate);
      }
      else if (Comperator.NOT_LATER_THAN.equals(comperator))
      {
         result = lhsDate.before(offsetDate);
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
