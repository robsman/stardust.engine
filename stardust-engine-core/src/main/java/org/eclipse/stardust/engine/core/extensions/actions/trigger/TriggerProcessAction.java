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
package org.eclipse.stardust.engine.core.extensions.actions.trigger;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.AuditTrailLogger;
import org.eclipse.stardust.engine.core.runtime.beans.EventUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.UnrecoverableExecutionException;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TriggerProcessAction implements EventActionInstance
{
   private static final Logger trace = LogManager.getLogger(TriggerProcessAction.class);

   private Map actionAttributes = Collections.EMPTY_MAP;

   public void bootstrap(Map actionAttributes, Iterator accessPoints)
   {
      this.actionAttributes = actionAttributes;
   }

   public Event execute(Event event)
   {
      try
      {
         String processId = (String) actionAttributes
               .get(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);

         IProcessInstance processInstance = EventUtils.getProcessInstance(event);
         try
         {
            // @todo (france, ub): is this kosher?
            boolean synchronously = Parameters.instance().getString(
                  EngineProperties.NOTIFICATION_THREAD_MODE, "asynchronous").compareTo(
                  "synchronous") == 0;

            AdministrationService administrationService = new AdministrationServiceImpl();
            IModel model = (IModel) processInstance.getProcessDefinition().getModel();
            // @todo (france, ub): panel could offer 'real data mappings'?
            administrationService.startProcess(model.getModelOID(), processId,
                  processInstance.getExistingDataValues(false), synchronously);

         }
         catch (Exception x)
         {
            trace.warn("", x);
            AuditTrailLogger.getInstance(LogCode.ENGINE).warn(
                  "Cannot start process '" + processId + "'", x);
         }
      }
      catch (Exception e)
      {
         throw new UnrecoverableExecutionException(
               "Failed triggering process after event " + event, e);
      }

      return event;
   }
}
