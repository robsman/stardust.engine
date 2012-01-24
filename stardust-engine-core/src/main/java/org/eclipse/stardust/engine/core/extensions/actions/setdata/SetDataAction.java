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
package org.eclipse.stardust.engine.core.extensions.actions.setdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SetDataAction implements EventActionInstance
{
   private Map attributes = Collections.EMPTY_MAP;
   private Map accessPoints = Collections.EMPTY_MAP;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      this.attributes = actionAttributes;
      this.accessPoints = new HashMap();
      while (accessPoints.hasNext())
      {
         AccessPoint point = (AccessPoint) accessPoints.next();
         this.accessPoints.put(point.getId(), point);         
      }
   }

   public Event execute(Event event)
   {
      try
      {
         final String attributePath = (String) attributes
               .get(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT);
         final String attributeName = (String) attributes
               .get(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT);

         AccessPoint accessPoint = (AccessPoint) accessPoints.get(attributeName);

         if (null != accessPoint)
         {
            IProcessInstance processInstance = EventUtils.getProcessInstance(event);

            ExtendedAccessPathEvaluator apEvaluator = SpiUtils.createExtendedAccessPathEvaluator(accessPoint.getType());
            AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(processInstance, null);
            Object bo = apEvaluator.evaluate(accessPoint, EventUtils
                  .getAccessPointValue(accessPoint, event, attributes), attributePath, evaluationContext);

            final String dataId = (String) attributes
                  .get(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT);
            final String dataPath = (String) attributes
                  .get(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT);

            processInstance.setOutDataValue(ModelUtils.getData(processInstance
                  .getProcessDefinition(), dataId), dataPath, bo);
         }
         else
         {
            throw new UnrecoverableExecutionException("Invalid access point '"
                  + attributeName + "'");
         }
      }
      catch (Exception e)
      {
         throw new UnrecoverableExecutionException("Failed executing set data action.", e);
      }

      return event;
   }
}
