/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalInDataValues;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalDataValues;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalProcessInstanceProperties;
import static org.eclipse.stardust.engine.ws.QueryAdapterUtils.unmarshalWorklistQuery;
import static org.eclipse.stardust.engine.ws.WebServiceEnv.currentWebServiceEnvironment;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.fromWs;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.marshalPermissionList;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.marshalProcessDefinitionList;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.toWs;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.unmarshalAttributes;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.api.ws.GetActivityInData.DataIdsXto;
import org.eclipse.stardust.engine.api.ws.GetProcessProperties.PropertyIdsXto;
import org.eclipse.stardust.engine.api.ws.query.WorklistQueryXto;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@WebService(name = "IWorkflowService", serviceName = "StardustBpmServices", portName = "WorkflowServiceEndpoint", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", endpointInterface = "org.eclipse.stardust.engine.api.ws.IWorkflowService")
public class WorkflowServiceFacade implements IWorkflowService
{

   public ProcessInstanceXto startProcess(String processId, ParametersXto parameters,
         Boolean startSynchronously, InputDocumentsXto attachments) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();
         ServiceFactory sf = wsEnv.getServiceFactory();

         WsApiStartProcessCommand command = new WsApiStartProcessCommand(processId,
               parameters, startSynchronously, attachments);

         ProcessInstance pi = null;
         try
         {
            pi = (ProcessInstance) sf.getWorkflowService().execute(command);
         }
         catch (ServiceCommandException e)
         {
            // unwrap
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof RuntimeException)
            {
               throw (RuntimeException) cause;
            }
            else
            {
               throw e;
            }
         }
         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public WorklistXto findWorklist(WorklistQueryXto worklist) throws BpmFault
   {
      try
      {
         ServiceFactory sf = currentWebServiceEnvironment().getServiceFactory();

         WorklistQuery query = unmarshalWorklistQuery(worklist);

         Worklist wl = sf.getWorkflowService().getWorklist(query);

         return toWs(wl);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto activateActivity(long activityOid) throws BpmFault
   {
      try
      {
         ServiceFactory sf = currentWebServiceEnvironment().getServiceFactory();

         ActivityInstance ai = sf.getWorkflowService().activate(activityOid);

         return toWs(ai);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto activateNextActivity(Long activityOid, WorklistQueryXto worklistQuery) throws BpmFault
   {
      try
      {
         ServiceFactory sf = currentWebServiceEnvironment().getServiceFactory();

         if (activityOid != null)
         {
            return toWs(sf.getWorkflowService().activateNextActivityInstance(activityOid));
         }
         else
         {
            WorklistQuery query = unmarshalWorklistQuery(worklistQuery);

            return toWs(sf.getWorkflowService().activateNextActivityInstance(query));
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto activateNextActivityForProcess(long processOid)
         throws BpmFault
   {
      try
      {
         ServiceFactory sf = currentWebServiceEnvironment().getServiceFactory();

         ActivityInstance ai = sf.getWorkflowService()
               .activateNextActivityInstanceForProcessInstance(processOid);

         if (ai != null)
         {
            return toWs(ai);
         }
         else
         {
            // TODO remove?
            throw new NullPointerException(
                  "Activity could not be activated: activateNextActivityInstanceForProcessInstance returned null value. Check Persmissions/UserRoles?");
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto getActivity(long activityOid) throws BpmFault
   {
      try
      {
         ServiceFactory sf = currentWebServiceEnvironment().getServiceFactory();

         return toWs(sf.getWorkflowService().getActivityInstance(activityOid));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto getProcess(long processOid) throws BpmFault
   {
      try
      {
         ServiceFactory sf = currentWebServiceEnvironment().getServiceFactory();

         return toWs(sf.getWorkflowService().getProcessInstance(processOid));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public InstancePropertiesXto getProcessProperties(long processInstanceOid,
         PropertyIdsXto ids) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         HashSet idsSet = null;
         // TODO allow empty List?
         if (ids != null && !ids.getPropertyId().isEmpty())
         {
            idsSet = new HashSet(ids.getPropertyId());
         }
         Map<String, Serializable> inDataPaths = wfs.getInDataPaths(processInstanceOid,
               idsSet);

         return toWs(processInstanceOid, inDataPaths);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void setProcessProperties(long processInstanceOid,
         InstancePropertiesXto processProperties) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         wfs.setOutDataPaths(processInstanceOid, unmarshalProcessInstanceProperties(
               processInstanceOid, processProperties.getInstanceProperty()));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public ParametersXto getActivityInData(long activityOid, String context, DataIdsXto ids)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ActivityInstance ai = wfs.getActivityInstance(activityOid);

         @SuppressWarnings("unchecked")
         Map<String, ? extends Serializable> inDataValues = wfs.getInDataValues(
               activityOid, context, ids != null ? new HashSet(ids.getDataId()) : null);

         return marshalInDataValues(wsEnv.getModel(ai.getModelOID()), ai.getActivity(),
               context, inDataValues);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ParametersXto activateActivityAndGetInData(long activityOid, String context)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         ActivityInstance ai = sf.getWorkflowService().activate(activityOid);

         Map<String, ? extends Serializable> inDataValues = sf.getWorkflowService()
               .getInDataValues(activityOid, context, null);

         return marshalInDataValues(wsEnv.getModel(ai.getModelOID()), ai.getActivity(),
               context, inDataValues);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto suspendActivity(long activityOid,
         ParticipantInfoBaseXto participantBaseXto, String context,
         ParametersXto outDataValues) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();
         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();
         ActivityInstance ai;

         ParticipantInfoXto participantXto = null;
         boolean useSuspendToLast = false;

         if (participantBaseXto == null)
         {
            useSuspendToLast = true;
         }
         else
         {
            if (participantBaseXto instanceof ParticipantInfoXto)
            {
               participantXto = (ParticipantInfoXto) participantBaseXto;
            }
            // else instanceof DefaultPerformerXto ? participantXto = null
         }

         if ( !useSuspendToLast)
         {
            ParticipantInfoXto pXto = XmlAdapterUtils.handleWsConstants(participantXto,
                  wfs);
            ParticipantInfo participant = XmlAdapterUtils.unmarshalParticipantInfo(pXto);

            if ( !isEmpty(context))
            {
               ActivityInstance tempAi = wfs.getActivityInstance(activityOid);

               Map<String, ? extends Serializable> outData = unmarshalDataValues(
                     wsEnv.getModel(tempAi.getModelOID()), Direction.OUT,
                     tempAi.getActivity(), context, outDataValues);

               ai = wfs.suspendToParticipant(activityOid, participant, new ContextData(
                     context, outData));
            }
            else
            {
               ai = wfs.suspendToParticipant(activityOid, participant, null);
            }
         }
         else
         {
            if ( !isEmpty(context))
            {
               ActivityInstance tempAi = wfs.getActivityInstance(activityOid);

               Map<String, ? extends Serializable> outData = unmarshalDataValues(
                     wsEnv.getModel(tempAi.getModelOID()), Direction.OUT,
                     tempAi.getActivity(), context, outDataValues);

               ai = wfs.suspend(activityOid, new ContextData(context, outData));
            }
            else
            {
               ai = wfs.suspend(activityOid, null);
            }
         }
         return toWs(ai);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstancesXto completeActivityAndActivateNext(long activityOid,
         String context, ParametersXto outDataValues, Boolean activate) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ActivityInstance ai = wfs.getActivityInstance(activityOid);

         Map<String, ? extends Serializable> outData = unmarshalDataValues(
               wsEnv.getModel(ai.getModelOID()), Direction.OUT, ai.getActivity(),
               context, outDataValues);

         if (activate == null || !activate)
         {
            return toWs(wfs.complete(activityOid, context, outData,
                  WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE));
         }
         else
         {
            return toWs(wfs.activateAndComplete(activityOid, context, outData,
                  WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE));
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto completeActivity(long activityOid, String context,
         ParametersXto outDataValues, Boolean activate) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ActivityInstance ai = wfs.getActivityInstance(activityOid);

         Map<String, ? extends Serializable> outData = unmarshalDataValues(
               wsEnv.getModel(ai.getModelOID()), Direction.OUT, ai.getActivity(),
               context, outDataValues);

         if (activate == null || !activate)
         {
            return toWs(wfs.complete(activityOid, context, outData));
         }
         else
         {
            return toWs(wfs.activateAndComplete(activityOid, context, outData));
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto abortActivity(long activityOid, AbortScopeXto abortScope)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ActivityInstance ai = null;
         if (abortScope == null)
         {
            ai = wfs.abortActivityInstance(activityOid);
         }
         else
         {
            ai = wfs.abortActivityInstance(activityOid, XmlAdapterUtils.unmarshalAbortScope(abortScope));
         }

         return toWs(ai);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto delegateActivity(long activityOid,
         ParticipantInfoBaseXto participantBaseXto) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ParticipantInfoXto participantXto;

         if (participantBaseXto instanceof ParticipantInfoXto)
         {
            participantXto = (ParticipantInfoXto) participantBaseXto;
         }
         else if (participantBaseXto instanceof DefaultParticipantXto)
         {
            participantXto = null;
         }
         else
         {
            throw new PublicException(
                  "null is not a valid argument for ParticipantInfoBaseXto");
         }

         ParticipantInfo participant = null;
         if (participantXto != null)
         {
            participant = XmlAdapterUtils.unmarshalParticipantInfo(participantXto);
         }

         ActivityInstance ai = null;
         ai = wfs.delegateToParticipant(activityOid, participant);

         return toWs(ai);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityInstanceXto hibernateActivity(long activityOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ActivityInstance ai = wfs.hibernate(activityOid);

         return toWs(ai);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityEventBindingXto getActivityEventBinding(long activityOid,
         String eventHandlerId) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         EventHandlerBinding binding = wfs.getActivityInstanceEventHandler(activityOid,
               eventHandlerId);

         return toWs(binding, new ActivityEventBindingXto());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityEventBindingXto createActivityEventBinding(long activityOid,
         ActivityEventBindingXto bindingInfo) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         ActivityEventBindingXto xto = null;
         if (null != bindingInfo)
         {
            WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

            EventHandlerBinding binding;

            // TODO unbind already existing binding first?

            Map<String, Object> bindingAttributes = unmarshalAttributes(bindingInfo.getBindingAttributes());
            if ((null == bindingInfo.getTimeout()) && isEmpty(bindingAttributes))
            {
               wfs.bindActivityEventHandler(activityOid, bindingInfo.getHandlerId());
            }
            else
            {
               binding = wfs.getActivityInstanceEventHandler(activityOid,
                     bindingInfo.getHandlerId());

               wfs.bindActivityEventHandler(activityOid, fromWs(bindingInfo, binding));
            }

            // read binding info after created
            binding = wfs.getActivityInstanceEventHandler(activityOid,
                  bindingInfo.getHandlerId());

            xto = toWs(binding, new ActivityEventBindingXto());
         }

         return xto;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityEventBindingXto removeActivityEventBinding(long activityOid,
         String eventHandlerId) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         EventHandlerBinding binding = wfs.getActivityInstanceEventHandler(activityOid,
               eventHandlerId);
         if (null != binding)
         {
            wfs.unbindActivityEventHandler(activityOid, eventHandlerId);
         }

         return toWs(binding, new ActivityEventBindingXto());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessEventBindingXto getProcessEventBinding(long processOid,
         String eventHandlerId) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         EventHandlerBinding binding = wfs.getProcessInstanceEventHandler(processOid,
               eventHandlerId);

         return toWs(binding, new ProcessEventBindingXto());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessEventBindingXto createProcessEventBinding(long processOid,
         ProcessEventBindingXto bindingInfo) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         ProcessEventBindingXto xto = null;
         if (null != bindingInfo)
         {
            WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

            EventHandlerBinding binding;

            // TODO unbind already existing binding first?

            Map<String, Object> bindingAttributes = unmarshalAttributes(bindingInfo.getBindingAttributes());
            if ((null == bindingInfo.getTimeout()) && isEmpty(bindingAttributes))
            {
               wfs.bindProcessEventHandler(processOid, bindingInfo.getHandlerId());
            }
            else
            {
               binding = wfs.getProcessInstanceEventHandler(processOid,
                     bindingInfo.getHandlerId());

               wfs.bindProcessEventHandler(processOid, fromWs(bindingInfo, binding));
            }

            // read binding info after created
            binding = wfs.getProcessInstanceEventHandler(processOid,
                  bindingInfo.getHandlerId());

            xto = toWs(binding, new ProcessEventBindingXto());
         }

         return xto;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessEventBindingXto removeProcessEventBinding(long processOid,
         String eventHandlerId) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         EventHandlerBinding binding = wfs.getProcessInstanceEventHandler(processOid,
               eventHandlerId);
         if (null != binding)
         {
            wfs.unbindProcessEventHandler(processOid, eventHandlerId);
         }

         return toWs(binding, new ProcessEventBindingXto());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserXto getSessionUser() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         return toWs(wfs.getUser());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public PermissionsXto getPermissions() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         List<Permission> ret = wfs.getPermissions();

         return marshalPermissionList(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void writeLogEntry(LogTypeXto logType, Long activityOid, Long processOid,
         String message) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ContextKind contextType = null;
         long contextOid = 0;
         if (activityOid != null)
         {
            contextOid = activityOid;
            contextType = ContextKind.ActivityInstance;
         }
         else if (processOid != null)
         {
            contextOid = processOid;
            contextType = ContextKind.ProcessInstance;
         }

         wfs.writeLogEntry(XmlAdapterUtils.unmarshalLogType(logType), contextType,
               contextOid, message, null);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public ProcessDefinitionsXto getStartableProcessDefinitions() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         List<ProcessDefinition> pds = wfs.getStartableProcessDefinitions();

         return marshalProcessDefinitionList(pds);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstancesXto spawnSubprocessInstances(long processInstanceOid,
         ProcessSpawnInfosXto processSpawnInfos) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         List<ProcessInstance> pis = wfs.spawnSubprocessInstances(processInstanceOid,
               XmlAdapterUtils.unmarshalProcessSpawnInfos(processSpawnInfos));

         return XmlAdapterUtils.marshalProcessInstanceList(pis);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto spawnPeerProcessInstance(long processInstanceOid,
         String spawnProcessId, boolean copyData, ParametersXto parameters,
         boolean abortProcessInstance, String comment) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         Map<String, ? extends Serializable> data = DataFlowUtils.unmarshalInitialDataValues(spawnProcessId, parameters);
         ProcessInstance pi = wfs.spawnPeerProcessInstance(processInstanceOid, spawnProcessId, copyData, data, abortProcessInstance, comment);

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto createCase(String name, String description,
         OidListXto memberOids) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.createCase(name, description, XmlAdapterUtils.unmarshalOidList(memberOids));

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto joinCase(long caseOid, OidListXto memberOids)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.joinCase(caseOid, XmlAdapterUtils.unmarshalOidList(memberOids));

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto leaveCase(long caseOid, OidListXto memberOids)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.leaveCase(caseOid, XmlAdapterUtils.unmarshalOidList(memberOids));

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto mergeCases(long targetCaseOid, OidListXto sourceCaseOids,
         String comment) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.mergeCases(targetCaseOid, XmlAdapterUtils.unmarshalOidList(sourceCaseOids), comment);

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto delegateCase(long caseOid, ParticipantInfoXto participantInfo)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.delegateCase(caseOid, XmlAdapterUtils.unmarshalParticipantInfo(participantInfo));

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto joinProcessInstance(long processInstanceOid,
         long targetProcessInstanceOid, String comment) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.joinProcessInstance(processInstanceOid, targetProcessInstanceOid, comment);

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto abortProcessInstance(long oid, AbortScopeXto abortScope)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = currentWebServiceEnvironment();

         WorkflowService wfs = wsEnv.getServiceFactory().getWorkflowService();

         ProcessInstance pi = wfs.abortProcessInstance(oid, XmlAdapterUtils.unmarshalAbortScope(abortScope));

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }
}
