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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.core.monitoring.AbstractPartitionMonitor;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author Florin.Herinean
 */
public class WorkCompletionPartitionMonitor extends AbstractPartitionMonitor
{
   Logger trace = LogManager.getLogger(WorkCompletionPartitionMonitor.class);

   @Override
   public void afterModelDeployment(Collection<IModel> models, boolean isOverwrite)
         throws DeploymentException
   {
      for (IModel model : models)
      {
         registerSignalEmitters(model);
      }
   }

   @Override
   public void modelLoaded(IModel model)
   {
      registerSignalEmitters(model);
   }

   private void registerSignalEmitters(IModel model)
   {
      for (IProcessDefinition process : model.getProcessDefinitions())
      {
         for (IActivity activity : process.getActivities())
         {
            for (IEventHandler handler : activity.getEventHandlers())
            {
               registerSignalEmitters(model, handler);
            }
         }
      }
   }

   protected void registerSignalEmitters(IModel model, IEventHandler handler)
   {
      PluggableType type = handler.getType();
      if (type != null && PredefinedConstants.SIGNAL_CONDITION.equals(type.getId()))
      {
         String signalSource = handler.getStringAttribute(SignalMessageAcceptor.BPMN_SIGNAL_SOURCE);
         if (signalSource != null)
         {
            String[] tokens = signalSource.split(":");
            if (tokens.length >= 2)
            {
               for (IModel sourceModel : resolveModel(model, tokens[0]))
               {
                  IProcessDefinition sourceProcess = resolveProcessDefinition(sourceModel, tokens[1]);
                  if (sourceProcess != null)
                  {
                     EventHandlerOwner target = tokens.length > 2 ? resolveActivity(sourceProcess, tokens[2]) : sourceProcess;
                     if (target != null)
                     {
                        String outMatchingParameters = handler.getStringAttribute("stardust:bpmn:signal:outMatchingParameter");
                        registerSignalEmitter(handler.getId(), target, outMatchingParameters);
                        Set<EventHandlerOwner> targets = handler.getRuntimeAttribute(SignalMessageAcceptor.BPMN_SIGNAL_SOURCE);
                        if (targets == null)
                        {
                           targets = CollectionUtils.newSet();
                           handler.setRuntimeAttribute(SignalMessageAcceptor.BPMN_SIGNAL_SOURCE, targets);
                        }
                        targets.add(target);
                     }
                  }
               }
            }
         }
      }
   }

   protected void registerSignalEmitter(String name, EventHandlerOwner target,
         String outMatchingParameters)
   {
      String id = null;
      String dataId = null;
      String dataPath = null;

      if (outMatchingParameters != null && !outMatchingParameters.isEmpty())
      {
         JsonParser jsonParser = new JsonParser();
         JsonElement parsedValue = jsonParser.parse(outMatchingParameters);
         if (parsedValue instanceof JsonObject)
         {
            JsonObject dataMappingJson = (JsonObject) parsedValue;
            id = extract(dataMappingJson, "id");
            dataId = extract(dataMappingJson, "dataFullId");
            dataPath = extract(dataMappingJson, "dataPath");
         }
      }

      WorkCompletionSignalEmitter.register(name, target, id, dataId, dataPath);
   }

   private String extract(JsonObject json, String name)
   {
      JsonElement value = json.get(name);
      return value == null || value.isJsonNull() ? null : value.getAsString();
   }

   private IActivity resolveActivity(IProcessDefinition sourceProcess, String id)
   {
      IActivity sourceActivity = sourceProcess.findActivity(id);
      if (sourceActivity == null)
      {
         trace.info("Could not resolve activity '" + id + "' in " + sourceProcess);
      }
      return sourceActivity;
   }

   private IProcessDefinition resolveProcessDefinition(IModel sourceModel, String id)
   {
      IProcessDefinition sourceProcess = sourceModel.findProcessDefinition(id);
      if (sourceProcess == null)
      {
         trace.info("Could not resolve process definition '" + id + "' in " + sourceModel);
      }
      return sourceProcess;
   }

   private Collection<IModel> resolveModel(IModel targetModel, String id)
   {
      ModelManager manager = ModelManagerFactory.getCurrent();
      Iterator<IModel> models = manager.getAllModelsForId(id);
      if (models.hasNext())
      {
         return CollectionUtils.newListFromIterator(models);
      }
      trace.info("Could not resolve model with id '" + id + "'.");
      return Collections.emptyList();
   }
}
