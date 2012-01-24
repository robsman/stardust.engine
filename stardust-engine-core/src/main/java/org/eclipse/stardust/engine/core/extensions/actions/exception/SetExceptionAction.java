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
package org.eclipse.stardust.engine.core.extensions.actions.exception;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.TimeoutException;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SetExceptionAction implements EventActionInstance
{
   private Map attributes;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      this.attributes = actionAttributes;
   }

   public Event execute(Event event)
   {
      Object attributePath = attributes
            .get(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT);

      if ((null == attributePath) || attributePath instanceof String)
      {
         Object bo;
         try
         {
            bo = JavaDataTypeUtils.evaluate((String) attributePath,
                  new TimeoutException(""));
         }
         catch (InvocationTargetException e)
         {
            throw new UnrecoverableExecutionException(
                  "Skipping set data action for event " + event
                        + " as the requested exception can not be created.",
                  e.getTargetException());
         }

         IProcessInstance processInstance = EventUtils.getProcessInstance(event);
         if (null != processInstance)
         {
            final String dataId = (String) attributes
                  .get(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT);
            final String dataPath = (String) attributes
                  .get(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT);

            processInstance.setOutDataValue(ModelUtils.getData(processInstance
                  .getProcessDefinition(), dataId), dataPath, bo);
         }
         else
         {
            throw new UnrecoverableExecutionException(
                  "Skipping set data action for event " + event
                  + " as the process context is missing.");
         }
      }
      else
      {
         throw new UnrecoverableExecutionException("Skipping set exception action as of"
               + " a wrong configured exception attribute path '" + attributePath + "'");
      }

      return event;
   }
}
