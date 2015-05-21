/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
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

import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalDataValues;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalInitialDataValues;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.api.ws.DeleteProcesses.OidsXto;
import org.eclipse.stardust.engine.api.ws.GetSupportedRuntimeArtifactTypesResponse.ArtifactTypesXto;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@WebService(name = "IAdministrationService", serviceName = "StardustBpmServices", portName = "AdministrationServiceEndpoint", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", endpointInterface = "org.eclipse.stardust.engine.api.ws.IAdministrationService")
public class AdministrationServiceFacade implements IAdministrationService
{
   private static final Logger trace = LogManager.getLogger(AdministrationServiceFacade.class);

   public PasswordRulesXto getPasswordRules() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         PasswordRules ret = as.getPasswordRules();

         return toWs(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void setPasswordRules(PasswordRulesXto passwordRules) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.setPasswordRules(fromWs(passwordRules));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public DepartmentXto createDepartment(String id, String name, String description,
         DepartmentInfoXto parent, OrganizationInfoXto organization) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         Department dep = as.createDepartment(id, name, description,
               XmlAdapterUtils.unmarshalDepartmentInfo(parent),
               (OrganizationInfo) XmlAdapterUtils.unmarshalParticipantInfo(organization));

         return toWs(dep);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DepartmentXto getDepartment(long oid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         Department dep = as.getDepartment(oid);

         return toWs(dep);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DepartmentXto modifyDepartment(long oid, String name, String description)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         Department dep = as.modifyDepartment(oid, name, description);

         return toWs(dep);

      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void removeDepartment(long oid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.removeDepartment(oid);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public DeploymentInfoXto deployModel(Integer predecessorOid, Date validFrom,
         Date validTo, String comment, Boolean disabled, Boolean ignoreWarnings,
         String configuration, XmlValueXto model) throws BpmFault
   {
      try
      {
         String modelAsXmlString = unmarshalModelAsXML(model);

         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         predecessorOid = mapModelPredecessorOid(predecessorOid);

         DeploymentInfo deploymentInfo = null;
         if (disabled == null || ignoreWarnings == null)
         {
            deploymentInfo = as.deployModel(modelAsXmlString, predecessorOid);
         }
         else
         {
            deploymentInfo = as.deployModel(modelAsXmlString, configuration,
                  predecessorOid, validFrom, validTo, comment, disabled, ignoreWarnings);
         }

         // clear ModelCache
         wsEnv.clearModelCache();
         return toWs(deploymentInfo);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DeploymentInfoXto overwriteModel(int modelOid, Date validFrom, Date validTo,
         String comment, Boolean disabled, Boolean ignoreWarnings, String configuration,
         XmlValueXto model) throws BpmFault
   {
      try
      {
         String modelAsXmlString = unmarshalModelAsXML(model);

         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         DeploymentInfo deploymentInfo = null;
         if (disabled == null || ignoreWarnings == null)
         {
            deploymentInfo = as.overwriteModel(modelAsXmlString, modelOid);
         }
         else
         {
            deploymentInfo = as.overwriteModel(modelAsXmlString, configuration, modelOid,
                  validFrom, validTo, comment, disabled, ignoreWarnings);
         }

         // clear ModelCache
         wsEnv.clearModelCache();
         return toWs(deploymentInfo);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DeploymentInfoXto deleteModel(int modelOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         DeploymentInfo deploymentInfo = as.deleteModel(modelOid);

         // clear ModelCache
         wsEnv.clearModelCache();
         return toWs(deploymentInfo);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   private Integer mapModelPredecessorOid(Integer predecessorOid)
   {
      if (predecessorOid == null)
      {
         predecessorOid = 0;
      }
      return predecessorOid;
   }

   public UserXto getSessionUser() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         User user = as.getUser();

         return toWs(user);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void deleteProcesses(OidsXto oids) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.deleteProcesses(oids != null ? oids.getOid() : null);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public void cleanupRuntime(boolean keepUsers) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.cleanupRuntime(keepUsers);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public void cleanupRuntimeAndModels() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.cleanupRuntimeAndModels();

         // clear ModelCache
         wsEnv.clearModelCache();
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public ProcessInstanceXto setProcessInstancePriority(long oid, int priority,
         Boolean propagateToSubProcesses) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         ProcessInstance pi = null;
         if (propagateToSubProcesses == null)
         {
            pi = as.setProcessInstancePriority(oid, priority);
         }
         else
         {
            pi = as.setProcessInstancePriority(oid, priority, propagateToSubProcesses);
         }
         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceXto abortProcessInstance(long oid, AbortScopeXto abortScope) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         ProcessInstance pi = as.abortProcessInstance(oid);

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void recoverProcessInstances(
         org.eclipse.stardust.engine.api.ws.RecoverProcessInstances.OidsXto oids) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.recoverProcessInstances(oids != null ? oids.getOid() : null);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public DaemonsXto getDaemonStatus(DaemonParametersXto daemonParameters)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         DaemonsXto ret = null;
         if (daemonParameters == null || daemonParameters.getDaemonParameter().isEmpty())
         {
            // TODO introduce boolean for acknowledge
            ret = marshalDaemons(as.getAllDaemons(true));
         }
         else
         {
            List<Daemon> daemons = new ArrayList<Daemon>();
            for (DaemonParameterXto daemonQueryXto : daemonParameters.getDaemonParameter())
            {
               daemons.add(as.getDaemon(daemonQueryXto.getType(),
                     daemonQueryXto.isAcknowledge()));
            }
            ret = marshalDaemons(daemons);
         }
         return ret;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DaemonsXto stopDaemon(DaemonParametersXto daemonParameters) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         DaemonsXto ret = null;
         if (daemonParameters != null)
         {
            List<Daemon> daemons = new ArrayList<Daemon>();
            for (DaemonParameterXto daemonQueryXto : daemonParameters.getDaemonParameter())
            {
               daemons.add(as.stopDaemon(daemonQueryXto.getType(),
                     daemonQueryXto.isAcknowledge()));
            }
            ret = marshalDaemons(daemons);
         }
         return ret;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DaemonsXto startDaemon(DaemonParametersXto daemonParameters) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         DaemonsXto ret = null;
         if (daemonParameters != null)
         {
            List<Daemon> daemons = new ArrayList<Daemon>();
            for (DaemonParameterXto daemonQueryXto : daemonParameters.getDaemonParameter())
            {
               daemons.add(as.startDaemon(daemonQueryXto.getType(),
                     daemonQueryXto.isAcknowledge()));
            }
            ret = marshalDaemons(daemons);
         }
         return ret;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public AuditTrailHealthReportXto getAuditTrailHealthReport() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return toWs(as.getAuditTrailHealthReport());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void recoverRuntimeEnvironment() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.recoverRuntimeEnvironment();
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public ProcessInstanceXto startProcessForModel(long modelOid, String processId,
         ParametersXto parameters, Boolean startSynchronously,
         InputDocumentsXto attachments) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         DeployedModel model = sf.getQueryService().getModel(modelOid);

         if (null != attachments)
         {
            checkProcessAttachmentSupport(processId, model);
         }

         ProcessInstance pi = sf.getAdministrationService().startProcess(modelOid,
               processId, unmarshalInitialDataValues(processId, parameters, wsEnv),
               Boolean.TRUE.equals(startSynchronously));

         List<Document> theAttachments = unmarshalInputDocuments(attachments, sf,
               model, pi);

         if (!theAttachments.isEmpty() && theAttachments != null)
         {
            sf.getWorkflowService().setOutDataPath(pi.getOID(), "PROCESS_ATTACHMENTS",
                  theAttachments);
         }

         return toWs(pi);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;

   }

   public ActivityInstanceXto forceCompletion(long activityOid,
         ParametersXto outDataValues) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         ActivityInstance ai = sf.getWorkflowService().getActivityInstance(activityOid);

         Map<String, ? extends Serializable> outDataMappings = unmarshalDataValues(
               wsEnv.getModel(ai.getModelOID()), Direction.OUT, ai.getActivity(),
               PredefinedConstants.APPLICATION_CONTEXT, outDataValues, wsEnv);

         ApplicationContext applicationContext = ai.getActivity().getApplicationContext(
               PredefinedConstants.APPLICATION_CONTEXT);

         Map<String, Serializable> outDataAccessPoints = new HashMap<String, Serializable>();
         if (applicationContext != null)
         {
            @SuppressWarnings("unchecked")
            List<DataMapping> allOutDataMappings = applicationContext.getAllOutDataMappings();

            if (outDataMappings != null)
            {
               for (DataMapping dataMapping : allOutDataMappings)
               {
                  Serializable value = outDataMappings.get(dataMapping.getId());

                  if (value != null)
                  {
                     AccessPoint applicationAccessPoint = dataMapping.getApplicationAccessPoint();
                     if (applicationAccessPoint != null)
                     {
                        outDataAccessPoints.put(applicationAccessPoint.getId(), value);
                     }
                  }
               }
            }
         }

         validateOutDataMappings(outDataMappings, outDataValues);

         ActivityInstance ret = sf.getAdministrationService().forceCompletion(
               activityOid, outDataAccessPoints);

         return toWs(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   private void validateOutDataMappings(
         Map<String, ? extends Serializable> outDataMappings, ParametersXto outDataValues)
   {
      if (outDataValues != null && outDataMappings != null)
      {
         List<ParameterXto> parameter = outDataValues.getParameter();
         for (ParameterXto parameterXto : parameter)
         {
            if ( !outDataMappings.containsKey(parameterXto.getName()))
            {
               trace.info("No Out Data Mapping was found for the specified ID '"
                     + parameterXto.getName() + "'.");
            }
         }
      }

   }

   public ActivityInstanceXto forceSuspend(long activityOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return toWs(as.forceSuspendToDefaultPerformer(activityOid));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void flushCaches() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.flushCaches();
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public PermissionsXto getPermissions() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         List<Permission> ret = as.getPermissions();

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
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

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

         as.writeLogEntry(XmlAdapterUtils.unmarshalLogType(logType), contextType,
               contextOid, message, null);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

	public PreferencesXto getPreferences(PreferenceScopeXto scope,
			String moduleId, String preferencesId) throws BpmFault
	{
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();
         Preferences preferences = as.getPreferences(
               XmlAdapterUtils.unmarshalPreferenceScope(scope), moduleId, preferencesId);

			return XmlAdapterUtils.toWs(preferences);
		}
		catch (ApplicationException e)
		{
			XmlAdapterUtils.handleBPMException(e);
		}
		return null;

	}

	public void savePreferences(PreferencesListXto preferenceList)
			throws BpmFault
	{
		try
		{
			WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

			AdministrationService as = wsEnv.getServiceFactory()
					.getAdministrationService();

			as.savePreferences(XmlAdapterUtils.unmarshalPreferenceList(preferenceList));

		}
		catch (ApplicationException e)
		{
			XmlAdapterUtils.handleBPMException(e);
		}

	}


	public ConfigurationVariablesListXto getConfigurationVariables(
			StringListXto modelIds, XmlValueXto modelXml) throws BpmFault
	{
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

			List<ConfigurationVariables> configList = CollectionUtils.newList();

			if (modelIds != null)
			{
				configList.addAll(as.getConfigurationVariables(modelIds.getValue()));
			}
			else if (modelXml != null)
			{
				ConfigurationVariables configVariables = as
						.getConfigurationVariables((unmarshalModelAsXML(modelXml)).getBytes());
				configList.add(configVariables);
			}
			else
			{
				throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("modelIds","null"));
			}
			return XmlAdapterUtils.marshalConfigurationVariablesList(configList);
		}
		catch (ApplicationException e)
		{
			XmlAdapterUtils.handleBPMException(e);
		}
		return null;
	}


	public ModelReconfigurationInfoListXto saveConfigurationVariables(
			ConfigurationVariablesXto configurationVariables, boolean force)
			throws BpmFault
	{

	   try
	   {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();
         List<ModelReconfigurationInfo> modelReconfigInfoList = CollectionUtils.newList();

			modelReconfigInfoList = as
					.saveConfigurationVariables(
							XmlAdapterUtils
									.unmarshalConfigurationVariables(configurationVariables),
							force);

			return XmlAdapterUtils.marshalReconfigurationInfoList(modelReconfigInfoList);
		}
		catch (ApplicationException e)
		{
			XmlAdapterUtils.handleBPMException(e);
		}

		return null;
	}

	public RuntimePermissionsXto getGlobalPermissions() throws BpmFault
	{
	   try
	   {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return XmlAdapterUtils.marshalRuntimePermissions((RuntimePermissionsDetails) as
               .getGlobalPermissions());
		}
		catch (ApplicationException e)
		{
			XmlAdapterUtils.handleBPMException(e);
		}

		return null;
	}

	public void setGlobalPermissions(RuntimePermissionsXto runtimePermissions)
			throws BpmFault
	{
	   try
	   {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.setGlobalPermissions(XmlAdapterUtils
               .unmarshalRuntimePermissions(runtimePermissions));
		}
		catch (ApplicationException e)
		{
			XmlAdapterUtils.handleBPMException(e);
		}

	}

   @Override
   public RuntimeArtifactXto getRuntimeArtifact(long oid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return toWs(as.getRuntimeArtifact(oid));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   @Override
   public DeployedRuntimeArtifactXto deployRuntimeArtifact(
         RuntimeArtifactXto runtimeArtifact) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return toWs(as.deployRuntimeArtifact(fromWs(runtimeArtifact)));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   @Override
   public DeployedRuntimeArtifactXto overwriteRuntimeArtifact(long oid,
         RuntimeArtifactXto runtimeArtifact) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return toWs(as.overwriteRuntimeArtifact(oid, fromWs(runtimeArtifact)));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   @Override
   public void deleteRuntimeArtifact(long oid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         as.deleteRuntimeArtifact(oid);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   @Override
   public ArtifactTypesXto getSupportedRuntimeArtifactTypes() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         AdministrationService as = wsEnv.getServiceFactory().getAdministrationService();

         return XmlAdapterUtils.marshalArtifactTypes(as.getSupportedRuntimeArtifactTypes());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

}
