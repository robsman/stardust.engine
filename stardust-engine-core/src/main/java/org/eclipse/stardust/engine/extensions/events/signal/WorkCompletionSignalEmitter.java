/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.extensions.events.signal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.Event;

/**
 *
 * @author Florin.Herinean
 */
public final class WorkCompletionSignalEmitter
{
   private String signalName;
   private String mappingId;
   private String dataId;
   private String dataPath;

   private WorkCompletionSignalEmitter(String signalName, String mappingId, String dataId, String dataPath)
   {
      this.signalName = signalName;
      this.mappingId = mappingId;
      int ix = dataId.indexOf(':');
      this.dataId = ix < 0 ? dataId : dataId.substring(ix + 1);
      this.dataPath = dataPath;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((dataId == null) ? 0 : dataId.hashCode());
      result = prime * result + ((dataPath == null) ? 0 : dataPath.hashCode());
      result = prime * result + ((mappingId == null) ? 0 : mappingId.hashCode());
      result = prime * result + ((signalName == null) ? 0 : signalName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      WorkCompletionSignalEmitter other = (WorkCompletionSignalEmitter) obj;
      if (dataId == null)
      {
         if (other.dataId != null)
            return false;
      }
      else if (!dataId.equals(other.dataId))
         return false;
      if (dataPath == null)
      {
         if (other.dataPath != null)
            return false;
      }
      else if (!dataPath.equals(other.dataPath))
         return false;
      if (mappingId == null)
      {
         if (other.mappingId != null)
            return false;
      }
      else if (!mappingId.equals(other.mappingId))
         return false;
      if (signalName == null)
      {
         if (other.signalName != null)
            return false;
      }
      else if (!signalName.equals(other.signalName))
         return false;
      return true;
   }

   private void sendWorkCompleteSignal(int emitterType, int modelOid, long runtimeOid, IProcessInstance processInstance)
   {
      IModel model = (IModel) processInstance.getProcessDefinition().getModel();
      IData data = model.findData(dataId);
      Object value = data == null ? null : processInstance.getInDataValue(data, dataPath);

      Event event = new Event(Event.ENGINE_EVENT, Event.OID_UNDEFINED, Event.OID_UNDEFINED, Event.OID_UNDEFINED, emitterType);
      event.setAttribute("id", mappingId);
      event.setAttribute("dataValue", value);
      event.setAttribute("modelOid", modelOid);
      event.setAttribute("runtimeOid", runtimeOid);

      SendSignalEventAction action = new SendSignalEventAction();
      action.bootstrap(Collections.singletonMap(SignalMessageAcceptor.BPMN_SIGNAL_CODE, signalName), null);
      action.execute(event);
   }

   public static void register(String signalName, EventHandlerOwner target, String mappingId, String dataId, String dataPath)
   {
      synchronized (target)
      {
         Set<WorkCompletionSignalEmitter> emitters = target.getRuntimeAttribute(WorkCompletionSignalEmitter.class.getName());
         if (emitters == null)
         {
            emitters = CollectionUtils.newSet();
            target.setRuntimeAttribute(WorkCompletionSignalEmitter.class.getName(), emitters);
         }
         emitters.add(new WorkCompletionSignalEmitter(signalName, mappingId, dataId, dataPath));
      }
   }

   public static void activityCompleted(IActivityInstance activity)
   {
      sendSignal(Event.ACTIVITY_INSTANCE, activity.getActivity(), activity.getProcessInstance());
   }

   public static void processCompleted(IProcessInstance process)
   {
      sendSignal(Event.PROCESS_INSTANCE, process.getProcessDefinition(), process);
   }

   private static void sendSignal(int emitterType, EventHandlerOwner target, IProcessInstance processInstance)
   {
      Set<WorkCompletionSignalEmitter> emitters = target.getRuntimeAttribute(WorkCompletionSignalEmitter.class.getName());
      if (emitters != null)
      {
         ModelManager manager = ModelManagerFactory.getCurrent();
         IModel model = (IModel) target.getModel();
         int modelOID = model.getModelOID();
         long runtimeOid = manager.getRuntimeOid((IdentifiableElement) target);
         for (WorkCompletionSignalEmitter emitter : emitters)
         {
            emitter.sendWorkCompleteSignal(emitterType, modelOID, runtimeOid, processInstance);
         }
      }
   }
}
