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

import static org.eclipse.stardust.engine.ws.DataFlowUtils.getStructuredTypeName;
import static org.eclipse.stardust.engine.ws.QueryAdapterUtils.unmarshalActivityQuery;
import static org.eclipse.stardust.engine.ws.QueryAdapterUtils.unmarshalLogEntryQuery;
import static org.eclipse.stardust.engine.ws.QueryAdapterUtils.unmarshalProcessQuery;
import static org.eclipse.stardust.engine.ws.QueryAdapterUtils.unmarshalUserGroupQuery;
import static org.eclipse.stardust.engine.ws.QueryAdapterUtils.unmarshalUserQuery;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.*;

import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.LogEntries;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.api.ws.query.DeployedModelQueryXto;
import org.eclipse.stardust.engine.api.ws.query.DocumentQueryXto;
import org.eclipse.stardust.engine.api.ws.query.PreferenceQueryXto;
import org.eclipse.stardust.engine.api.ws.query.ProcessDefinitionQueryXto;
import org.eclipse.stardust.engine.api.ws.query.ProcessQueryXto;
import org.eclipse.stardust.engine.api.ws.query.VariableDefinitionQueryXto;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.xml.sax.InputSource;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@WebService(name = "IQueryService", serviceName = "StardustBpmServices", portName = "QueryServiceEndpoint", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", endpointInterface = "org.eclipse.stardust.engine.api.ws.IQueryService")
public class QueryServiceFacade implements IQueryService
{

   public DepartmentsXto findAllDepartments(DepartmentInfoXto parent,
         OrganizationInfoXto organization) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         List<Department> departments = qs.findAllDepartments(
               XmlAdapterUtils.unmarshalDepartmentInfo(parent),
               (OrganizationInfo) XmlAdapterUtils.unmarshalParticipantInfo(organization));

         return XmlAdapterUtils.marshalDepartmentList(departments);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DepartmentXto findDepartment(DepartmentInfoXto parent,
         OrganizationInfoXto organization, String id) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         Department department = qs.findDepartment(
               XmlAdapterUtils.unmarshalDepartmentInfo(parent), id,
               (OrganizationInfo) XmlAdapterUtils.unmarshalParticipantInfo(organization));

         return toWs(department);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessInstanceQueryResultXto findProcesses(ProcessQueryXto query)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         ProcessInstances pis = qs.getAllProcessInstances(unmarshalProcessQuery(query));

         return toWs(pis);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ActivityQueryResultXto findActivities(
         org.eclipse.stardust.engine.api.ws.query.ActivityQueryXto query) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         // TODO redirect count only queries to getProcessInstanceCount
         ActivityInstances ais = qs.getAllActivityInstances(unmarshalActivityQuery(query));

         return toWs(ais);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserQueryResultXto findUsers(
         org.eclipse.stardust.engine.api.ws.query.UserQueryXto userQuery) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         Users users = qs.getAllUsers(unmarshalUserQuery(userQuery));

         return toWs(users);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public UserGroupQueryResultXto findUserGroups(
         org.eclipse.stardust.engine.api.ws.query.UserGroupQueryXto userGroupQuery)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         UserGroups userGroups = qs.getAllUserGroups(unmarshalUserGroupQuery(userGroupQuery));

         return toWs(userGroups);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public LogEntryQueryResultXto findLogEntries(
         org.eclipse.stardust.engine.api.ws.query.LogEntryQueryXto logEntryQuery) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         LogEntries logEntries = qs.getAllLogEntries(unmarshalLogEntryQuery(logEntryQuery));

         return toWs(logEntries);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ModelDescriptionsXto getAllModelDescriptions() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         List<DeployedModelDescription> ret = qs.getAllModelDescriptions();

         return marshalDeployedModelDescriptionList(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ModelDescriptionsXto getAllAliveModelDescriptions() throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         List<DeployedModelDescription> ret = qs.getAllAliveModelDescriptions();

         return marshalDeployedModelDescriptionList(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ModelDescriptionXto getModelDescription(Long modelOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         DeployedModelDescription ret = modelOid == null
               ? qs.getActiveModelDescription()
               : qs.getModelDescription(modelOid);

         return toWs(ret, new ModelDescriptionXto());
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ModelXto getModel(Long modelOid, Boolean computeAliveness) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         if (null == modelOid)
         {
            modelOid = Long.valueOf(sf.getQueryService()
                  .getActiveModelDescription()
                  .getModelOID());
         }
         DeployedModel model = null;
         if (computeAliveness == null)
         {
            model = sf.getQueryService().getModel(modelOid, false);
         }
         else
         {
            model = sf.getQueryService().getModel(modelOid, computeAliveness);
         }

         return model == null ? null : toWs(model);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public XmlValueXto getModelAsXML(Long modelOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         if (null == modelOid)
         {
            modelOid = Long.parseLong(String.valueOf(wsEnv.getActiveModel().getModelOID()));
         }
         String xmlString = sf.getQueryService().getModelAsXML(modelOid);

         return marshalModelAsXML(xmlString);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessDefinitionXto getProcessDefinition(String processId, Long modelOid)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         ProcessDefinition pd = (null != modelOid) ? sf.getQueryService()
               .getProcessDefinition(modelOid, processId) : sf.getQueryService()
               .getProcessDefinition(processId);

         return toWs(pd, wsEnv.getModel(pd.getModelOID()));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessDefinitionsXto getAllProcessDefinitions(Long modelOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         ServiceFactory sf = wsEnv.getServiceFactory();

         List<ProcessDefinition> pdl = (null != modelOid) ? sf.getQueryService()
               .getAllProcessDefinitions(modelOid) : sf.getQueryService()
               .getAllProcessDefinitions();
         return marshalProcessDefinitionList(pdl);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ParticipantsXto getAllParticipants(Long modelOid) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         List<Participant> ret = null;
         if (modelOid == null)
         {
            ret = qs.getAllParticipants();
         }
         else
         {
            ret = qs.getAllParticipants(modelOid);
         }
         return marshalParticipantList(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ParticipantXto getParticipant(String participantId, Long modelOid)
         throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         ModelParticipant ret = null;
         if (modelOid == null)
         {
            ret = (ModelParticipant) qs.getParticipant(participantId);
         }
         else
         {
            ret = (ModelParticipant) qs.getParticipant(modelOid, participantId);
         }
         return toWs(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public XmlValueXto getSchemaDefinition(QName type, Long modelOid) throws BpmFault
   {
      XmlValueXto xto = null;
      try
      {
         // TODO retrieve from annotation on DocumentXto?
         String namespaceUri = "http://eclipse.org/stardust/ws/v2012a/api";
         if (namespaceUri.equals(type.getNamespaceURI()))
         {
            xto = new XmlValueXto();

            InputSource src = new InputSource(getClass().getClassLoader()
                  .getResourceAsStream("META-INF/wsdl/StardustBpmTypes.xsd"));
            xto.getAny().add(XmlUtils.parseSource(src, null).getDocumentElement());
         }
         else
         {
            WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

            ServiceFactory sf = wsEnv.getServiceFactory();

            if (null == modelOid)
            {
               modelOid = Long.valueOf(sf.getQueryService()
                     .getActiveModelDescription()
                     .getModelOID());
            }

            Model model = wsEnv.getModel(modelOid.intValue());

            @SuppressWarnings("unchecked")
            List<TypeDeclaration> tdl = model.getAllTypeDeclarations();
            for (TypeDeclaration td : tdl)
            {
               QName tdTypeName = getStructuredTypeName(model, td.getId());
               if ((null != tdTypeName) && tdTypeName.equals(type))
               {
                  xto = new XmlValueXto();

                  byte[] schema = sf.getQueryService().getSchemaDefinition(modelOid,
                        td.getId());
                  xto.getAny().add(
                        XmlUtils.parseString(new String(schema)).getDocumentElement());

                  break;
               }
            }
         }

         return xto;
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
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         List<Permission> ret = qs.getPermissions();

         return marshalPermissionList(ret);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DocumentQueryResultXto getAllDocuments(DocumentQueryXto query) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         Documents documents = qs.getAllDocuments(QueryAdapterUtils.unmarshalDocumentQuery(query));

         return toWs(documents);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public ProcessDefinitionQueryResultXto findProcessDefinitions(
         ProcessDefinitionQueryXto query) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         ProcessDefinitions pds = qs.getProcessDefinitions(QueryAdapterUtils.unmarshalProcessDefinitionQuery(query));

         return toWs(pds);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public VariableDefinitionQueryResultXto findVariableDefinitions(
         VariableDefinitionQueryXto query) throws BpmFault
   {
      try
      {
         WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

         QueryService qs = wsEnv.getServiceFactory().getQueryService();

         DataQueryResult data = qs.getAllData(QueryAdapterUtils.unmarshalDataQuery(query));

         return toWs(data);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }


	public PreferencesXto getPreferences(PreferenceScopeXto scope,
			String moduleId, String preferencesId) throws BpmFault 
	{
		WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

		QueryService qs = wsEnv.getServiceFactory()
				.getQueryService();
		try 
		{
			Preferences preferences = qs.getPreferences(
					XmlAdapterUtils.unmarshalPreferenceScope(scope), moduleId,
					preferencesId);

			return XmlAdapterUtils.toWs(preferences);
		} 
		catch (ApplicationException e) 
		{
			XmlAdapterUtils.handleBPMException(e);
		}
		return null;
	}


	public PreferencesListXto findPreferences(PreferenceQueryXto preferenceQuery)
			throws BpmFault 
	{
		WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();

		QueryService qs = wsEnv.getServiceFactory().getQueryService();

		try 
		{
			List<Preferences> preferencesList = qs
					.getAllPreferences(QueryAdapterUtils
							.unmarshalPreferenceQuery(preferenceQuery));

			return XmlAdapterUtils.marshalPreferencesList(preferencesList);
		} 
		catch (ApplicationException e) 
		{
			XmlAdapterUtils.handleBPMException(e);
		}
		return null;
	}


   public ModelsQueryResultXto findModels(DeployedModelQueryXto deployedModelQuery)
         throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      
      QueryService qs = wsEnv.getServiceFactory().getQueryService();
      
      try
      {
         Models models = qs.getModels(QueryAdapterUtils.unmarshalDeployedModelQuery(deployedModelQuery));
         
         return XmlAdapterUtils.marshalModelsQueryResult(models);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

}
