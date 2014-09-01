/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.engine.api.dto;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.PermissionState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLink;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;

public class LazilyLoadingProcessInstanceDetails extends RuntimeObjectDetails implements ProcessInstance
{
   private static final long serialVersionUID = 2605158724691591949L;

   private static final String PI_DETAILS_PROPERTIES_KEY = LazilyLoadingProcessInstanceDetails.class.getName();

   /** hold an instance of {@link IProcessInstance} for deferred calculation */
   private final IProcessInstance processInstance;

   /** hold the parameters relevant for deferred {@link ProcessInstanceDetails} creation */
   private final Map<String, Object> piDetailsParameters;


   /** lazily created {@link ProcessInstance} object to delegate to for complex requests */
   private ProcessInstance processInstanceDetails;

   public LazilyLoadingProcessInstanceDetails(final IProcessInstance processInstance)
   {
      super(processInstance, processInstance.getProcessDefinition());

      this.processInstance = processInstance;

      Map<String, Object> tmpPiDetailsParameters = (Map<String, Object>) PropertyLayerProviderInterceptor.getCurrent().get(PI_DETAILS_PROPERTIES_KEY);
      if (tmpPiDetailsParameters == null)
      {
         tmpPiDetailsParameters = initPiDetailsParameters(Parameters.instance());
         PropertyLayerProviderInterceptor.getCurrent().setProperty(PI_DETAILS_PROPERTIES_KEY, tmpPiDetailsParameters);
      }
      piDetailsParameters = tmpPiDetailsParameters;

      /* do not create anything eagerly, but only as soon as it's requested */
   }

   @Override
   public Object getDescriptorValue(final String id)
   {
      return getProcessInstanceDetails().getDescriptorValue(id);
   }

   @Override
   public List<DataPath> getDescriptorDefinitions()
   {
      return getProcessInstanceDetails().getDescriptorDefinitions();
   }

   @Override
   public String getProcessID()
   {
      return processInstance.getProcessDefinition().getId();
   }

   @Override
   public String getProcessName()
   {
      return processInstance.getProcessDefinition().getName();
   }

   @Override
   public long getRootProcessInstanceOID()
   {
      return processInstance.getRootProcessInstanceOID();
   }

   @Override
   public long getScopeProcessInstanceOID()
   {
      return processInstance.getScopeProcessInstanceOID();
   }

   @Override
   public ProcessInstance getScopeProcessInstance()
   {
      return getProcessInstanceDetails().getScopeProcessInstance();
   }

   @Override
   public int getPriority()
   {
      return processInstance.getPriority();
   }

   @Override
   public Date getStartTime()
   {
      return processInstance.getStartTime();
   }

   @Override
   public Date getTerminationTime()
   {
      return processInstance.getTerminationTime();
   }

   @Override
   public User getStartingUser()
   {
      if (processInstance.getStartingUser() == null)
      {
         return null;
      }

      return getProcessInstanceDetails().getStartingUser();
   }

   @Override
   public ProcessInstanceState getState()
   {
      return processInstance.getState();
   }

   @Override
   public ProcessInstanceDetailsLevel getDetailsLevel()
   {
      ProcessInstanceDetailsLevel piDetailsLevel = (ProcessInstanceDetailsLevel) piDetailsParameters.get(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL);
      if (piDetailsLevel == null)
      {
         piDetailsLevel = ProcessInstanceDetailsLevel.Default;
      }
      return piDetailsLevel;
   }

   @Override
   public EnumSet<ProcessInstanceDetailsOptions> getDetailsOptions()
   {
      EnumSet<ProcessInstanceDetailsOptions> piDetailsOptions = (EnumSet<ProcessInstanceDetailsOptions>) piDetailsParameters.get(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS);
      if (piDetailsOptions == null)
      {
         piDetailsOptions = EnumSet.noneOf(ProcessInstanceDetailsOptions.class);
      }
      return piDetailsOptions;
   }

   @Override
   public ProcessInstanceAttributes getAttributes()
   {
      return getProcessInstanceDetails().getAttributes();
   }

   @Override
   public Map<String, Object> getRuntimeAttributes()
   {
      return getProcessInstanceDetails().getRuntimeAttributes();
   }

   @Override
   public List<HistoricalEvent> getHistoricalEvents()
   {
      return getProcessInstanceDetails().getHistoricalEvents();
   }

   @Override
   public PermissionState getPermission(String permissionId)
   {
      return getProcessInstanceDetails().getPermission(permissionId);
   }

   @Override
   public long getParentProcessInstanceOid()
   {
      return getProcessInstanceDetails().getParentProcessInstanceOid();
   }

   @Override
   public List<ProcessInstanceLink> getLinkedProcessInstances()
   {
      return getProcessInstanceDetails().getLinkedProcessInstances();
   }

   @Override
   public boolean isCaseProcessInstance()
   {
      return processInstance.isCaseProcessInstance();
   }

   private Map<String, Object> initPiDetailsParameters(final Parameters params)
   {
      final Map<String, Object> result = newHashMap();

      final Object userDetailsLevel = params.get(UserDetailsLevel.PRP_USER_DETAILS_LEVEL);
      if (userDetailsLevel != null)
      {
         result.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, userDetailsLevel);
      }

      final Object provideDescriptors = params.get(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS);
      if (provideDescriptors != null)
      {
         result.put(IDescriptorProvider.PRP_PROPVIDE_DESCRIPTORS, provideDescriptors);
      }

      final Object piDetailsLevel = params.get(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL);
      if (piDetailsLevel != null)
      {
         result.put(ProcessInstanceDetailsLevel.PRP_PI_DETAILS_LEVEL, piDetailsLevel);
      }

      final Object piDetailsOptions = params.get(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS);
      if (piDetailsOptions != null)
      {
         result.put(ProcessInstanceDetails.PRP_PI_DETAILS_OPTIONS, piDetailsOptions);
      }

      final Object provideEventTypes = params.get(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES);
      if (provideEventTypes != null)
      {
         result.put(HistoricalEventPolicy.PRP_PROPVIDE_EVENT_TYPES, provideEventTypes);
      }

      return result;
   }

   private ProcessInstance getProcessInstanceDetails()
   {
      if (processInstanceDetails == null)
      {
         ParametersFacade.pushLayer(piDetailsParameters);
         try
         {
            processInstanceDetails = new ProcessInstanceDetails(processInstance);
         }
         finally
         {
            ParametersFacade.popLayer();
         }
      }
      return processInstanceDetails;
   }
}