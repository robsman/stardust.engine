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

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.copyMap;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.TYPE_ATT;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.getDmsTypeName;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.getStructuredTypeName;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isDmsType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isEntityBeanType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isPrimitiveType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isSerializableType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.isStructuredType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalInstanceProperties;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalPrimitiveType;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalProcessInstanceProperties;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.marshalStructValue;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalStructValue;
import static org.eclipse.stardust.engine.ws.DmsAdapterUtils.ensureFolderExists;
import static org.eclipse.stardust.engine.ws.DmsAdapterUtils.storeDocumentIntoDms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.ConfigurationError;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.common.utils.xml.StaticNamespaceContext;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ConditionalPerformerInfoDetails;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.HistoricalState;
import org.eclipse.stardust.engine.api.dto.ModelParticipantDetails;
import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.dto.ModelReconfigurationInfoDetails;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.dto.PasswordRulesDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.dto.RoleInfoDetails;
import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.dto.UserGroupInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserInfoDetails;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.ConditionalPerformerInfo;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.DynamicParticipantInfo;
import org.eclipse.stardust.engine.api.model.EventAction;
import org.eclipse.stardust.engine.api.model.EventHandler;
import org.eclipse.stardust.engine.api.model.ExternalReference;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelElement;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ParameterMapping;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.model.SchemaType;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.model.XpdlType;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.LogEntries;
import org.eclipse.stardust.engine.api.query.ParticipantWorklist;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.api.ws.ActivityDefinitionXto.InteractionContextsXto;
import org.eclipse.stardust.engine.api.ws.DeploymentInfoXto.ErrorsXto;
import org.eclipse.stardust.engine.api.ws.DeploymentInfoXto.WarningsXto;
import org.eclipse.stardust.engine.api.ws.DocumentTypeResultsXto.DocumentTypeResultXto;
import org.eclipse.stardust.engine.api.ws.DocumentXto.VersionLabelsXto;
import org.eclipse.stardust.engine.api.ws.GetUserRealmsResponse.UserRealmsXto;
import org.eclipse.stardust.engine.api.ws.GrantsXto.GrantXto;
import org.eclipse.stardust.engine.api.ws.ModelXto.GlobalVariablesXto;
import org.eclipse.stardust.engine.api.ws.ModelXto.ProcessesXto;
import org.eclipse.stardust.engine.api.ws.PermissionStatesXto.PermissionStateXto;
import org.eclipse.stardust.engine.api.ws.PermissionsXto.PermissionXto;
import org.eclipse.stardust.engine.api.ws.PreferenceEntryXto.ValueListXto;
import org.eclipse.stardust.engine.api.ws.ProcessDefinitionXto.ActivitiesXto;
import org.eclipse.stardust.engine.api.ws.UserQueryResultXto.UsersXto;
import org.eclipse.stardust.engine.api.ws.WorklistXto.SharedWorklistsXto;
import org.eclipse.stardust.engine.api.ws.WorklistXto.UserWorklistXto;
import org.eclipse.stardust.engine.api.ws.WorklistXto.SharedWorklistsXto.SharedWorklistXto;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableDefinition;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.extensions.dms.data.*;
import org.eclipse.stardust.engine.extensions.dms.data.emfxsd.DmsSchemaProvider;
import org.eclipse.stardust.engine.ws.processinterface.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDParser;
import org.eclipse.xsd.util.XSDResourceImpl;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class XmlAdapterUtils
{
   private static final Logger trace = LogManager.getLogger(XmlAdapterUtils.class);

   private final static int PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH = 1;

   public final static long CURRENT_USER_OID = -10;

   private static final String PROPERTY_NAME_LITERAL = "name";

   private static final String PROPERTY_STRING_VALUE_LITERAL = "stringValue";

   private static final String PROPERTY_TYPE_KEY_LITERAL = "typeKey";

   @SuppressWarnings("unchecked")
   private static final ProcessInstanceState[] ALL_PI_STATES = (ProcessInstanceState[]) ProcessInstanceState.getKeys(
         ProcessInstanceState.class)
         .toArray(new ProcessInstanceState[0]);

   @SuppressWarnings("unchecked")
   private static final ActivityInstanceState[] ALL_AI_STATES = (ActivityInstanceState[]) ActivityInstanceState.getKeys(
         ActivityInstanceState.class)
         .toArray(new ActivityInstanceState[0]);

   public static Date parseDateTime(String s)
   {
      if (s == null)
      {
         return null;
      }
      return DatatypeConverter.parseDateTime(s).getTime();
   }

   public static String printDateTime(Date dt)
   {
      if (dt == null)
      {
         return null;
      }
      Calendar cal = new GregorianCalendar();
      cal.setTime(dt);

      return DatatypeConverter.printDateTime(cal);
   }

   public static Direction parseDirection(String s)
   {
      if ("In".equals(s))
      {
         return Direction.IN;
      }
      else if ("Out".equals(s))
      {
         return Direction.OUT;
      }
      else if ("InOut".equals(s))
      {
         return Direction.IN_OUT;
      }

      throw new PublicException("Illegal direction: " + s);
   }

   public static String printDirection(Direction direction)
   {
      if (Direction.IN == direction)
      {
         return "In";
      }
      else if (Direction.OUT == direction)
      {
         return "Out";
      }
      else if (Direction.IN_OUT == direction)
      {
         return "InOut";
      }

      return "";
   }

   public static ProcessInstanceState parseProcessInstanceState(String s)
   {
      ProcessInstanceState result = null;

      for (ProcessInstanceState state : ALL_PI_STATES)
      {
         if (state.getName().equals(s))
         {
            result = state;
            break;
         }
      }

      if (null == result)
      {
         throw new PublicException("Illegal process instance state: " + s);
      }

      return result;
   }

   public static String printProcessInstanceState(ProcessInstanceState pis)
   {
      return (null != pis) ? pis.getName() : "";
   }

   public static ActivityInstanceState parseActivityInstanceState(String s)
   {
      ActivityInstanceState result = null;

      for (ActivityInstanceState state : ALL_AI_STATES)
      {
         if (state.getName().equals(s))
         {
            result = state;
            break;
         }
      }

      if (null == result)
      {
         throw new PublicException("Illegal activity instance state: " + s);
      }

      return result;
   }

   public static String printActivityInstanceState(ActivityInstanceState ais)
   {
      return (null != ais) ? ais.getName() : "";
   }

   public static UserXto toWs(User u)
   {
      UserXto xto = null;
      if (null != u)
      {

         xto = toWs(u, new UserXto());
      }

      return xto;
   }

   public static <T extends UserXto> T toWs(User u, T xto)
   {
      xto.setOid(u.getOID());

      xto.setAccountId(u.getAccount());

      xto.setFirstName(u.getFirstName());
      xto.setLastName(u.getLastName());
      // xto.setPassword(null);

      xto.setPreviousLoginTime(u.getPreviousLoginTime());
      xto.setEMail(u.getEMail());

      xto.setValidFrom(u.getValidFrom());
      xto.setValidTo(u.getValidTo());

      xto.setDescription(u.getDescription());

      xto.setDetailsLevel(u.getDetailsLevel().getValue());

      xto.setUserRealm(toWs(u.getRealm()));

      xto.setUserGroups(marshalUserGroupList(u.getAllGroups()));

      xto.setGrants(marshalGrants(u.getAllGrants()));

      // TODO marshal attributes for User
      xto.setAttributes(marshalAttributes(u.getAllAttributes()));

      xto.setPasswordExpired(u.isPasswordExpired());

      return xto;
   }

   private static GrantsXto marshalGrants(List<Grant> allGrants)
   {
      GrantsXto ret = null;
      if (allGrants != null && !allGrants.isEmpty())
      {
         ret = new GrantsXto();
         for (Grant grant : allGrants)
         {
            ret.getGrant().add(marshalGrant(grant));
         }
      }
      return ret;
   }

   private static GrantXto marshalGrant(Grant grant)
   {
      // TODO: update
      GrantXto ret = null;
      if (grant != null)
      {
         ret = new GrantXto();
         ret.setId(grant.getId());
         ret.setName(grant.getName());
         ret.setOrganization(grant.isOrganization());
         ret.setDepartment(toWs(grant.getDepartment()));
      }
      return ret;
   }

   public static User unmarshalUser(UserXto user, UserService us)
   {
      if (user != null)
      {
         if (user.getOid() != null)
         {
            User ret = us.getUser(user.getOid());
            ret.setAccount(user.getAccountId());
            ret.setDescription(user.getDescription());
            ret.setEMail(user.getEMail());
            ret.setFirstName(user.getFirstName());
            ret.setLastName(user.getLastName());
            ret.setValidFrom(user.getValidFrom());
            ret.setValidTo(user.getValidTo());
            ret.setPassword(user.getPassword());

            unmarshalGrants(user.getGrants(), ret);

            unmarshalUserGroups(user.getUserGroups(), ret);

            ret.setAllProperties(unmarshalAttributes(user.getAttributes()));

            Reflect.setFieldValue(ret, "passwordExpired", user.isPasswordExpired());

            return ret;
         }
         else
         {
            throw new NullPointerException("Field UserXto.oid may not be null");
         }
      }
      throw new NullPointerException("Parameter UserXto may not be null");
   }

   private static void unmarshalUserGroups(UserGroupsXto userGroups, User ret)
   {
      if (userGroups != null)
      {
         List<UserGroup> allUserGroups = ret.getAllGroups();
         List<UserGroup> removeList = new ArrayList<UserGroup>();
         if (userGroups.getUserGroup().isEmpty())
         {
            removeList.addAll(allUserGroups);
         }
         else
         {

            Map<String, UserGroupXto> addMap = new HashMap<String, UserGroupXto>();
            for (UserGroupXto ug : userGroups.getUserGroup())
            {
               if (isEmpty(ug.getId()))
               {
                  throw new NullPointerException(
                        "UserGroup 'id' may not be empty or null");
               }
               else
               {
                  addMap.put(ug.getId(), ug);
               }
            }

            for (UserGroup ug : allUserGroups)
            {
               // all target elements that don't exist in source will be
               // removed
               if (addMap.get(ug.getId()) == null)
               {
                  removeList.add(ug);
               }

               // all elements that exist in target and source don't need
               // to be added
               addMap.remove(ug.getId());
            }

            // join groups
            for (Entry<String, UserGroupXto> entry : addMap.entrySet())
            {
               UserGroupXto ug = entry.getValue();
               ret.joinGroup(ug.getId());
            }
         }
         // leave groups
         for (UserGroup ug : removeList)
         {
            ret.leaveGroup(ug.getId());
         }
      }

   }

   private static void unmarshalGrants(GrantsXto grants, User ret)
   {
      if (grants != null)
      {
         List<Grant> allGrants = ret.getAllGrants();
         List<Grant> removeGrants = new ArrayList<Grant>();
         if (grants.getGrant().isEmpty())
         {
            removeGrants.addAll(allGrants);
         }
         else
         {
            List<GrantXto> addGrants = new ArrayList<GrantXto>();
            for (GrantXto grantXto : grants.getGrant())
            {
               if (isEmpty(grantXto.getId()))
               {
                  throw new NullPointerException("Grant 'id' may not be empty or null");
               }
               else
               {
                  addGrants.add(grantXto);
               }
            }

            for (Grant grant : allGrants)
            {
               // get elements that need to be removed because they are not
               // in the
               // targetList
               if ( !isInGrants(grant, addGrants, true))
               {
                  removeGrants.add(grant);
               }
            }
            // add grants that are new in xto
            for (GrantXto grantXto : addGrants)
            {
               ret.addGrant(grantXto.getId());
            }
         }
         // remove grants not existing in xto
         for (Grant remGrant : removeGrants)
         {
            // TODO: use qualified id
            ret.removeGrant(remGrant.getId());
         }
      }
   }

   private static boolean isInGrants(Grant grant, List<GrantXto> addGrants,
         boolean removeContainedTargetGrants)
   {
      boolean ret = false;

      for (Iterator< ? > iterator = addGrants.iterator(); iterator.hasNext();)
      {
         GrantXto gr = (GrantXto) iterator.next();
         // TODO: use qualified id
         if (gr.getId().equals(grant.getId()))
         {
            ret = true;
            if (removeContainedTargetGrants)
            {
               iterator.remove();
            }
         }
      }
      return ret;
   }

   public static ModelDescriptionsXto marshalDeployedModelDescriptionList(
         List<DeployedModelDescription> mdList)
   {
      ModelDescriptionsXto ret = new ModelDescriptionsXto();

      for (DeployedModelDescription md : mdList)
      {
         ret.getModelDescription().add(toWs(md, new ModelDescriptionXto()));
      }
      return ret;
   }

   private static <T extends ModelElementXto> T toWs(ModelElement me, T ret)
   {
      if (me != null)
      {
         ret.setModelOid(me.getModelOID());
         ret.setName(me.getName());
         ret.setId(me.getId());
         ret.setDescription(me.getDescription());
         // ret.setElementOid(md.getElementOID());
         ret.setPartitionId(me.getPartitionId());
         ret.setPartitionOid(me.getPartitionOID());
         ret.setAttributes(marshalAttributes(me.getAllAttributes()));
      }
      return ret;
   }

   public static <T extends ModelDescriptionXto> T toWs(DeployedModelDescription md, T ret)
   {
      ret = toWs((ModelElement) md, ret);

      if (md != null)
      {
         ret.setActive(md.isActive());
         ret.setDeploymentComment(md.getDeploymentComment());
         ret.setDeploymentTime(md.getDeploymentTime());
         ret.setRevision(md.getRevision());
         ret.setValidFrom(md.getValidFrom());
         ret.setVersion(md.getVersion());
      }
      return ret;
   }

   @SuppressWarnings("unchecked")
   public static ModelXto toWs(DeployedModel model)
   {
      ModelXto xto = toWs(model, new ModelXto());

      Boolean alive = null;
      try
      {
         alive = model.isAlive();
      }
      catch (UnsupportedOperationException e)
      {
         alive = null;
      }
      if (alive != null)
      {
         xto.setAlive(alive);
      }

      xto.setRoles(marshalRoleList(model.getAllRoles(),
            PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));

      xto.setOrganizations(marshalOrganizationList(model.getAllOrganizations(),
            PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));

      if ( !model.getAllData().isEmpty())
      {
         xto.setGlobalVariables(new GlobalVariablesXto());

         for (int i = 0; i < model.getAllData().size(); ++i)
         {
            Data data = (Data) model.getAllData().get(i);

            xto.getGlobalVariables().getGlobalVariable().add(toWs(data, model));
         }
      }

      if ( !model.getAllProcessDefinitions().isEmpty())
      {
         xto.setProcesses(new ProcessesXto());

         for (int i = 0; i < model.getAllProcessDefinitions().size(); ++i)
         {
            ProcessDefinition pd = (ProcessDefinition) model.getAllProcessDefinitions()
                  .get(i);

            xto.getProcesses().getProcess().add(toWs(pd, model));
         }
      }

      if ( !model.getAllTypeDeclarations().isEmpty())
      {
         xto.setTypeDeclarations(new TypeDeclarationsXto());

         List< ? > typeDeclarations = model.getAllTypeDeclarations();
         for (int i = 0; i < typeDeclarations.size(); ++i)
         {
            TypeDeclaration typeDec = (TypeDeclaration) typeDeclarations.get(i);
            xto.getTypeDeclarations().getTypeDeclaration().add(toWs(typeDec, model));
         }
      }

      return xto;
   }

   public static TypeDeclarationXto toWs(TypeDeclaration typeDec, Model model)
   {
      TypeDeclarationXto ret = toWs(typeDec, new TypeDeclarationXto());

      ret.setXpdlType(toWs(typeDec.getXpdlType(), model));

      return ret;
   }

   private static XpdlTypeXto toWs(XpdlType xpdlType, Model model)
   {
      XpdlTypeXto ret = new XpdlTypeXto();
      if (xpdlType != null)
      {
         if (xpdlType instanceof ExternalReference)
         {
            ret.setExternalReference(toWs((ExternalReference) xpdlType, model));
         }
         else if (xpdlType instanceof SchemaType)
         {
            ret.setSchemaType(toWs((SchemaType) xpdlType));
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Marshaling of XpdlType not supported: " + xpdlType.getClass());
         }
      }
      else
      {
         throw new NullPointerException("Cannot marshal XpdlType: xpdlType is null");
      }

      return ret;
   }

   private static ExternalReferenceXto toWs(ExternalReference exRef, Model model)
   {
      ExternalReferenceXto ret = new ExternalReferenceXto();

      ret.setLocation(exRef.getLocation());
      ret.setNamespace(exRef.getNamespace());
      ret.setXref(exRef.getXref());

      ret.setXml(marshalXsdSchema(exRef.getSchema(model)));

      return ret;
   }

   private static SchemaTypeXto toWs(SchemaType schema)
   {
      SchemaTypeXto ret = new SchemaTypeXto();

      ret.setXml(marshalXsdSchema(schema.getSchema()));

      return ret;
   }

   private static XmlValueXto marshalXsdSchema(XSDSchema schema)
   {
      XmlValueXto ret = null;

      if (schema != null)
      {
         ret = new XmlValueXto();

         // Serialize the schema
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         XSDResourceImpl.serialize(bout, schema.getElement());
         byte[] schemaBytes = bout.toByteArray();

         // byte[] schemaBytes2 =
         // StructuredTypeRtUtils.serializeSchema(schema);
         String xmlString = new String(schemaBytes);

         Element element = XmlUtils.parseString(xmlString).getDocumentElement();

         ret.getAny().add(element);
      }
      return ret;
   }

   public static ProcessDefinitionsXto marshalProcessDefinitionList(
         List<ProcessDefinition> pdl)
   {
      ProcessDefinitionsXto ret = new ProcessDefinitionsXto();

      for (ProcessDefinition processDefinition : pdl)
      {
         Model model = WebServiceEnv.currentWebServiceEnvironment().getModel(
               processDefinition.getModelOID());
         ret.getProcessDefinition().add(toWs(processDefinition, model));
      }
      return ret;
   }

   @SuppressWarnings("unchecked")
   public static ProcessDefinitionXto toWs(ProcessDefinition pd, Model model)
   {
      ProcessDefinitionXto res = toWs(pd, new ProcessDefinitionXto());

      res.setRtOid(pd.getRuntimeElementOID());

      res.setTriggers(marshalTriggers(pd.getAllTriggers()));

      if ( !pd.getAllDataPaths().isEmpty())
      {
         DataPathsXto dps = marshalDataPathList(pd.getAllDataPaths(), model);
         res.setDataPaths(dps);
      }

      if ( !pd.getAllActivities().isEmpty())
      {
         res.setActivities(new ActivitiesXto());

         for (int i = 0; i < pd.getAllActivities().size(); ++i)
         {
            Activity ad = (Activity) pd.getAllActivities().get(i);

            res.getActivities().getActivity().add(toWs(ad, model));
         }
      }
      res.setEventHandlers(toWs(pd.getAllEventHandlers(),
            new EventHandlerDefinitionsXto()));
      return res;
   }

   private static DataPathsXto marshalDataPathList(List<DataPath> dataPathList,
         Model model)
   {
      DataPathsXto dps = null;
      if (dataPathList != null)
      {
         dps = new DataPathsXto();
         for (DataPath dp : dataPathList)
         {
            dps.getDataPath().add(toWs(dp, model));
         }
      }
      return dps;
   }

   private static TriggersXto marshalTriggers(List<Trigger> allTriggers)
   {
      TriggersXto ret = null;

      if (allTriggers != null)
      {
         ret = new TriggersXto();

         for (Trigger t : allTriggers)
         {
            ret.getTrigger().add(marshalTrigger(t));
         }
      }
      return ret;
   }

   private static TriggerXto marshalTrigger(Trigger trigger)
   {
      TriggerXto ret = null;
      if (trigger != null)
      {
         ret = toWs(trigger, new TriggerXto());

         ret.setRuntimeElementOid(trigger.getRuntimeElementOID());
         ret.setSynchronous(trigger.isSynchronous());
         ret.setType(trigger.getType());

         ret.setAccessPoints(marshalAccessPointList(trigger.getAllAccessPoints()));
         ret.setParameterMappings(marshalParameterMappingList(trigger.getAllParameterMappings()));

      }
      return ret;
   }

   private static ParameterMappingsXto marshalParameterMappingList(
         List<ParameterMapping> allParameterMappings)
   {
      ParameterMappingsXto ret = null;

      if (allParameterMappings != null)
      {
         ret = new ParameterMappingsXto();

         for (ParameterMapping parameterMapping : allParameterMappings)
         {
            ret.getParameterMapping().add(marshalParameterMapping(parameterMapping));
         }
      }
      return ret;
   }

   private static ParameterMappingXto marshalParameterMapping(
         ParameterMapping parameterMapping)
   {
      ParameterMappingXto ret = null;

      if (parameterMapping != null)
      {
         ret = toWs(parameterMapping, new ParameterMappingXto());

         ret.setDataId(parameterMapping.getDataId());
         ret.setParameter(marshalAccessPoint(parameterMapping.getParameter()));
         ret.setParameterPath(parameterMapping.getParameterPath());
      }

      return ret;
   }

   @SuppressWarnings("unchecked")
   public static ActivityDefinitionXto toWs(Activity ad, Model model)
   {
      ActivityDefinitionXto res = toWs(ad, new ActivityDefinitionXto());

      res.setRtOid(ad.getRuntimeElementOID());

      res.setAbortable(ad.isAbortable());
      res.setInteractive(ad.isInteractive());
      res.setDefaultPerformer(toWs(ad.getDefaultPerformer()));
      res.setImplementationType(ad.getImplementationType().getName());

      res.setApplication(toWs(ad.getApplication()));

      List<ApplicationContext> applicationContexts = ad.getAllApplicationContexts();
      res.setInteractionContexts(new InteractionContextsXto());
      if ( !isEmpty(applicationContexts))
      {
         for (int i = 0; i < applicationContexts.size(); ++i)
         {
            org.eclipse.stardust.engine.api.model.ApplicationContext ac = applicationContexts.get(i);

            res.getInteractionContexts().getInteractionContext().add(toWs(ac, model));
         }
      }
      res.setEventHandlers(toWs(ad.getAllEventHandlers(),
            new EventHandlerDefinitionsXto()));

      return res;
   }

   private static ApplicationXto toWs(Application application)
   {
      ApplicationXto ret = null;
      if (application != null)
      {
         ret = toWs(application, new ApplicationXto());

         ret.setAccessPoints(marshalAccessPointList(application.getAllAccessPoints()));

         ret.setTypeAttributes(marshalAttributes(application.getAllTypeAttributes()));
      }
      return ret;
   }

   private static AccessPointsXto marshalAccessPointList(List<AccessPoint> allAccessPoints)
   {
      AccessPointsXto ret = null;
      if (allAccessPoints != null)
      {
         ret = new AccessPointsXto();

         for (AccessPoint accessPoint : allAccessPoints)
         {
            ret.getAccessPoint().add(marshalAccessPoint(accessPoint));
         }
      }
      return ret;
   }

   private static AccessPointXto marshalAccessPoint(AccessPoint accessPoint)
   {
      AccessPointXto ret = null;
      if (accessPoint != null)
      {
         ret = new AccessPointXto();
         ret.setAttributes(marshalAttributes(accessPoint.getAllAttributes()));
         ret.setId(accessPoint.getId());
         ret.setName(accessPoint.getName());
         ret.setDirection(accessPoint.getDirection());
         ret.setAccessPathEvaluatorClass(accessPoint.getAccessPathEvaluatorClass());
      }
      return ret;
   }

   @SuppressWarnings("unchecked")
   public static EventHandlerDefinitionsXto toWs(List<EventHandler> eventHandlers,
         EventHandlerDefinitionsXto eventHandlersXto)
   {
      if (eventHandlers != null)
      {
         for (EventHandler eh : eventHandlers)
         {
            EventHandlerDefinitionXto xto = new EventHandlerDefinitionXto();
            xto.setAttributes(marshalAttributes(eh.getAllAttributes()));

            if (eh.getAllBindActions() != null)
            {
               BindActionDefinitionsXto bindActions = new BindActionDefinitionsXto();
               bindActions.getBindAction().addAll(marshalActions(eh.getAllBindActions()));
               xto.setBindActions(bindActions);
            }

            if (eh.getAllEventActions() != null)
            {
               EventActionDefinitionsXto eventActions = new EventActionDefinitionsXto();
               eventActions.getEventAction().addAll(
                     marshalActions(eh.getAllEventActions()));
               xto.setEventActions(eventActions);
            }

            if (eh.getAllUnbindActions() != null)
            {
               UnbindActionDefinitionsXto unbindActions = new UnbindActionDefinitionsXto();
               unbindActions.getUnbindAction().addAll(
                     marshalActions(eh.getAllUnbindActions()));
               xto.setUnbindActions(unbindActions);
            }
            // TODO xto.setType(eh.getAllTypeAttributes());

            xto.setId(eh.getId());
            xto.setName(eh.getName());
            xto.setModelOid(eh.getModelOID());

            xto.setRtOid(eh.getRuntimeElementOID());
            xto.setDescription(eh.getDescription());
            xto.setPartitionId(eh.getPartitionId());
            xto.setPartitionOid(eh.getPartitionOID());

            eventHandlersXto.getEventHandler().add(xto);
         }
      }
      return eventHandlersXto;
   }

   private static List<EventActionDefinitionXto> marshalActions(
         List<EventAction> allBindActions)
   {
      List<EventActionDefinitionXto> ret = new ArrayList<EventActionDefinitionXto>();
      for (EventAction eventAction : allBindActions)
      {
         ret.add(marshalEventAction(eventAction));
      }

      return ret;
   }

   private static EventActionDefinitionXto marshalEventAction(EventAction eventAction)
   {
      EventActionDefinitionXto ret = new EventActionDefinitionXto();

      ret.setId(eventAction.getId());
      ret.setName(eventAction.getName());
      ret.setModelOid(eventAction.getModelOID());
      // TODO verify use of getElementOID as RtOid ?
      ret.setRtOid(eventAction.getElementOID());
      // TODO ret.setType(eventAction.getAllTypeAttributes());

      ret.setDescription(eventAction.getDescription());
      ret.setPartitionId(eventAction.getPartitionId());
      ret.setPartitionOid(eventAction.getPartitionOID());
      ret.setAttributes(marshalAttributes(eventAction.getAllAttributes()));
      return ret;
   }

   public static InteractionContextXto toWs(ApplicationContext ac, Model model)
   {
      return toWs(ac, new InteractionContextXto(), model);
   }

   public static <T extends InteractionContextXto> T toWs(ApplicationContext ac, T xto,
         Model model)
   {
      xto = toWs(ac, xto);

      // TODO ac.getAllAccessPoints();

      // TODO ac.getAllTypeAttributes();

      @SuppressWarnings("unchecked")
      final List<DataMapping> inMappings = ac.getAllInDataMappings();
      if ( !inMappings.isEmpty())
      {
         xto.setInDataFlows(new DataFlowsXto());

         for (int i = 0; i < inMappings.size(); ++i)
         {
            DataMapping dm = inMappings.get(i);

            xto.getInDataFlows().getDataFlow().add(toWs(dm, model));
         }
      }

      @SuppressWarnings("unchecked")
      final List<org.eclipse.stardust.engine.api.model.DataMapping> outMappings = ac.getAllOutDataMappings();
      if ( !outMappings.isEmpty())
      {
         xto.setOutDataFlows(new DataFlowsXto());

         for (int i = 0; i < outMappings.size(); ++i)
         {
            DataMapping dm = outMappings.get(i);

            xto.getOutDataFlows().getDataFlow().add(toWs(dm, model));
         }
      }

      return xto;
   }

   public static VariableDefinitionXto toWs(Data data)
   {
      Model model = WebServiceEnv.currentWebServiceEnvironment().getModel(
            data.getModelOID());

      return toWs(data, model);
   }

   public static VariableDefinitionXto toWs(Data data, Model model)
   {
      VariableDefinitionXto xto = toWs(data, new VariableDefinitionXto());

      xto.setTypeId(DataFlowUtils.getTypeId(data));

      if (isPrimitiveType(model, data))
      {
         Type primitveType = (Type) data.getAttribute(TYPE_ATT);
         xto.setType(marshalPrimitiveType(primitveType));
      }
      else if (isStructuredType(model, data))
      {
         String typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         xto.setType(getStructuredTypeName(model, typeDeclarationId));
      }
      else if (isDmsType(model, data))
      {
         xto.setType(getDmsTypeName(model, data));

         // TODO metadata type
         String metaDataSchema = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         if ( !isEmpty(metaDataSchema))
         {
            AttributeXto metaData = new AttributeXto();
            metaData.setName("metaDataType");
            metaData.setType(QNameConstants.QN_QNAME.toString());
            metaData.setValue(getStructuredTypeName(model, metaDataSchema).toString());
            xto.getAttributes().getAttribute().add(metaData);
         }
      }
      else if (isSerializableType(model, data))
      {
         xto.setType(QNameConstants.QN_BASE64BINARY);
      }
      else if (isEntityBeanType(model, data))
      {
         // xto.setType();
      }
      else
      {
         // throw new UnsupportedOperationException(
         trace.warn( //
         "Error marshaling VariableDefinition: dataType not supported. DataId: "
               + data.getId());
      }

      return xto;
   }

   public static DataPathXto toWs(DataPath dp, Model model)
   {
      DataPathXto res = toWs(dp, new DataPathXto());

      res.setAccessPath(dp.getAccessPath());
      res.setDataId(dp.getData());
      res.setDescriptor(dp.isDescriptor());
      res.setDirection(dp.getDirection());

      res.setMappedJavaType(dp.getMappedType().getName());
      res.setKeyDescriptor(dp.isKeyDescriptor());

      // res.setProcessDefinitionId(dp.getProcessDefinitionId());

      if (null != model)
      {
         // TODO review type marshaling
         Data data = model.getData(dp.getData());

         if (isPrimitiveType(model, dp))
         {
            Type primitveType = (Type) data.getAttribute(TYPE_ATT);
            res.setType(marshalPrimitiveType(primitveType));
         }
         else if (isStructuredType(model, dp))
         {
            res.setType(getStructuredTypeName(model, dp));
         }
         else if (isDmsType(model, dp))
         {
            res.setType(getDmsTypeName(model, dp));
         }
         else if (isSerializableType(model, data))
         {
            res.setType(QNameConstants.QN_BASE64BINARY);
         }
         else if (isEntityBeanType(model, data))
         {
            // xto.setType();
         }
         else
         {
            // throw new UnsupportedOperationException(
            trace.warn( //
            "Error marshaling DataPath: dataType not supported. DataId: " + dp.getData());
         }
      }

      return res;
   }

   public static DataFlowXto toWs(DataMapping dm, Model model)
   {
      DataFlowXto res = toWs(dm, new DataFlowXto());

      res.setDirection(dm.getDirection());
      res.setDataId(dm.getDataId());
      res.setDataPath(dm.getDataPath());

      res.setMappedJavaType(dm.getMappedType().getName());

      if (/* isEmpty(dm.getDataPath()) && */null != model)
      {
         Data data = model.getData(dm.getDataId());
         if (isPrimitiveType(model, dm))
         {
            Type primitveType = (Type) data.getAttribute(TYPE_ATT);
            res.setType(marshalPrimitiveType(primitveType));
         }
         else if (isStructuredType(model, dm))
         {
            res.setType(getStructuredTypeName(model, dm));
         }
         else if (isDmsType(model, dm))
         {
            res.setType(getDmsTypeName(model, dm));
         }
         else if (isSerializableType(model, data))
         {
            res.setType(QNameConstants.QN_BASE64BINARY);
         }
         else if (isEntityBeanType(model, data))
         {
            // xto.setType();
         }
         else
         {
            // throw new UnsupportedOperationException(
            trace.warn( //
            "Error marshaling DataFlow: dataType not supported. DataId: "
                  + dm.getDataId());
         }
      }

      try
      {
         AccessPoint appAp = dm.getApplicationAccessPoint();
         if (null != appAp)
         {
            res.setApplicationAccessPointId(appAp.getId());
         }
         res.setApplicationPath(dm.getApplicationPath());
      }
      catch (NullPointerException npe)
      {
         // ignore, will most probably happen due to inconsistent model
         trace.info("Failed evaluating application access point.");
      }

      return res;
   }

   public static UserQueryResultXto toWs(Users users)
   {
      UserQueryResultXto ret = new UserQueryResultXto();
      ret.setTotalCount(mapTotalCount(users));

      ret.setUsers(new UsersXto());
      for (Iterator< ? > iterator = users.iterator(); iterator.hasNext();)
      {
         User user = (User) iterator.next();
         ret.getUsers().getUser().add(toWs(user));
      }
      return ret;
   }

   public static UserGroupQueryResultXto toWs(UserGroups userGroups)
   {
      UserGroupQueryResultXto ret = new UserGroupQueryResultXto();
      ret.setTotalCount(mapTotalCount(userGroups));

      UserGroupsXto ug = new UserGroupsXto();
      for (Iterator< ? > iterator = userGroups.iterator(); iterator.hasNext();)
      {
         UserGroup userGroup = (UserGroup) iterator.next();
         ug.getUserGroup().add(toWs(userGroup));
      }
      if (userGroups.isEmpty())
      {
         ug = null;
      }
      ret.setUserGroups(ug);
      return ret;
   }

   public static UserGroupsXto marshalUserGroupList(List<UserGroup> userGroups)
   {
      UserGroupsXto ret = null;

      if (userGroups != null && !userGroups.isEmpty())
      {
         ret = new UserGroupsXto();
         for (Iterator< ? > iterator = userGroups.iterator(); iterator.hasNext();)
         {
            UserGroup userGroup = (UserGroup) iterator.next();
            ret.getUserGroup().add(toWs(userGroup));
         }
      }
      return ret;
   }

   public static UserGroupXto toWs(UserGroup userGroup)
   {
      UserGroupXto ret = new UserGroupXto();
      ret.setOid(userGroup.getOID());
      ret.setDescription(userGroup.getDescription());
      ret.setId(userGroup.getId());
      ret.setName(userGroup.getName());
      ret.setValidFrom(userGroup.getValidFrom());
      ret.setValidTo(userGroup.getValidTo());
      ret.setDetailsLevel(userGroup.getDetailsLevel().getValue());

      ret.setAttributes(marshalAttributes(userGroup.getAllAttributes()));
      return ret;
   }

   public static UserGroup unmarshalUserGroup(UserGroupXto userGroup, UserService us)
   {
      UserGroup ret = us.getUserGroup(userGroup.getOid());

      ret.setDescription(userGroup.getDescription());
      ret.setName(userGroup.getName());
      ret.setValidFrom(userGroup.getValidFrom());
      ret.setValidTo(userGroup.getValidTo());

      // TODO set userGroup attributes?
      // ret.setAttribute(name, value)
      if (userGroup.getAttributes() != null)
      {
         for (AttributeXto element : userGroup.getAttributes().getAttribute())
         {
            // Class clazz =
            // Reflect.getClassFromAbbreviatedName(element.getType());
            Serializable value = (Serializable) Reflect.convertStringToObject(
                  element.getType(), element.getValue());
            ret.setAttribute(element.getName(), value);
         }
      }

      return ret;
   }

   public static UserRealmsXto marshalUserRealmList(List<UserRealm> urList)
   {
      UserRealmsXto ret = new UserRealmsXto();
      for (UserRealm userRealm : urList)
      {
         ret.getUserRealms().add(toWs(userRealm));
      }
      return ret;
   }

   public static UserRealmXto toWs(UserRealm ur)
   {
      UserRealmXto ret = null;
      if (ur != null)
      {
         ret = new UserRealmXto();
         ret.setId(ur.getId());
         ret.setName(ur.getName());
         ret.setOid(ur.getOID());
         ret.setDescription(ur.getDescription());
         ret.setPartitionId(ur.getPartitionId());
         ret.setPartitionOid(ur.getPartitionOid());
      }

      return ret;
   }

   // *** PARTICIPANT MARSHALING ***

   public static ParticipantsXto marshalParticipantList(List<Participant> pList)
   {
      ParticipantsXto ret = new ParticipantsXto();
      for (Participant participant : pList)
      {

         if (participant instanceof Organization)
         {
            if (ret.getOrganizations() == null)
            {
               ret.setOrganizations(new OrganizationsXto());
            }
            ret.getOrganizations()
                  .getOrganization()
                  .add(toWs((Organization) participant,
                        PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));
         }
         else if (participant instanceof Role)
         {
            if (ret.getRoles() == null)
            {
               ret.setRoles(new RolesXto());
            }
            ret.getRoles()
                  .getRole()
                  .add(toWs((Role) participant, PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));
         }
         else if (participant instanceof ConditionalPerformer)
         {
            throw new UnsupportedOperationException(
                  "Marshaling of Participant type not supported: "
                        + participant.getClass());
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Marshaling of Participant type not supported: "
                        + participant.getClass());
         }
      }
      return ret;
   }

   public static ParticipantXto toWs(Participant p)
   {
      ParticipantXto xto = null;
      if (p != null)
      {
         xto = new ParticipantXto();
         if (p instanceof Role)
         {
            xto.setRole(toWs((Role) p, PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));
         }
         else if (p instanceof Organization)
         {
            xto.setOrganization(toWs((Organization) p,
                  PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));
         }
         else if (p instanceof User)
         {
            xto.setUser(toWs((User) p));
         }
         else if (p instanceof ConditionalPerformer)
         {
            // xto.setConditionalPerformer(toWs((ConditionalPerformer) p));
            trace.warn("Error marshaling Participant: participantType not supported: "
                  + p.getClass());
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Error marshaling Participant: participantType not supported: "
                        + p.getClass());
         }
      }
      return xto;
   }

   @SuppressWarnings("unchecked")
   private static RoleXto toWs(Role role, int traversalDepth)
   {
      RoleXto xto = toWs(role, new RoleXto(), traversalDepth);
      if (traversalDepth > 0)
      {
         traversalDepth-- ;
         xto.setClientOrganizations(marshalOrganizationList(
               role.getClientOrganizations(), traversalDepth));
         xto.setTeams(marshalOrganizationList(role.getTeams(), traversalDepth));
      }
      return xto;

   }

   private static RolesXto marshalRoleList(List<Role> roles, int traversalDepth)
   {
      RolesXto xto = null;
      if (roles != null && !roles.isEmpty())
      {
         xto = new RolesXto();
         for (Role Role : roles)
         {
            xto.getRole().add(toWs(Role, traversalDepth));
         }
      }
      return xto;
   }

   @SuppressWarnings("unchecked")
   private static OrganizationXto toWs(Organization org, int roleDepth)
   {
      OrganizationXto xto = null;

      if (org != null)
      {
         xto = toWs(org, new OrganizationXto(), roleDepth);
         if (roleDepth > 0)
         {
            roleDepth-- ;
            xto.setTeamLeadRole(toWs(org.getTeamLead(), roleDepth));
            xto.setAllSubOrganizations(marshalOrganizationList(
                  org.getAllSubOrganizations(), roleDepth));
            xto.setAllSubRoles(marshalRoleList(org.getAllSubRoles(), roleDepth));
         }
      }

      return xto;
   }

   private static OrganizationsXto marshalOrganizationList(List<Organization> orgs,
         int traversalDepth)
   {
      OrganizationsXto xto = null;

      if (orgs != null && !orgs.isEmpty())
      {
         xto = new OrganizationsXto();

         for (Organization organization : orgs)
         {
            xto.getOrganization().add(toWs(organization, traversalDepth));
         }
      }
      return xto;
   }

   @SuppressWarnings("unchecked")
   private static <T extends ModelParticipantXto> T toWs(ModelParticipant p, T xto,
         int traversalDepth)
   {

      xto = toWs(p, xto);

      if (p != null)
      {
         xto.setRuntimeElementOid(p.getRuntimeElementOID());
         if (p instanceof ModelParticipantDetails)
         {
            xto.setDepartment(marshalDepartmentInfo(p.getDepartment()));
            xto.setDescription(((ModelParticipantDetails) p).getDescription());
         }
         if (traversalDepth > 0)
         {
            traversalDepth-- ;
            xto.setAllSuperOrganizations(marshalOrganizationList(
                  p.getAllSuperOrganizations(), traversalDepth));
         }
      }
      return xto;
   }

   // *** END OF PARTICIPANT MARSHALING ***

   @SuppressWarnings("unchecked")
   public static ProcessInstanceQueryResultXto toWs(ProcessInstances pis)
   {
      ProcessInstanceQueryResultXto ret = new ProcessInstanceQueryResultXto();
      ret.setTotalCount(mapTotalCount(pis));
      ret.setHasMore(pis.hasMore());

      ret.setProcessInstances(new ProcessInstancesXto());
      for (Iterator iterator = pis.iterator(); iterator.hasNext();)
      {
         ProcessInstance pi = (ProcessInstance) iterator.next();
         ret.getProcessInstances().getProcessInstance().add(toWs(pi, pis.getQuery()));
      }
      return ret;
   }

   public static ProcessDefinitionQueryResultXto toWs(ProcessDefinitions pds)
   {
      ProcessDefinitionQueryResultXto ret = new ProcessDefinitionQueryResultXto();
      ret.setTotalCount(mapTotalCount(pds));
      ret.setHasMore(pds.hasMore());

      ret.setProcessDefinitions(new ProcessDefinitionsXto());
      for (Iterator iterator = pds.iterator(); iterator.hasNext();)
      {
         ProcessDefinition pd = (ProcessDefinition) iterator.next();

         Model model = WebServiceEnv.currentWebServiceEnvironment().getModel(
               pd.getModelOID());

         ret.getProcessDefinitions().getProcessDefinition().add(toWs(pd, model));
      }
      return ret;
   }

   /**
    * Only use in WebService environment.
    * Model lookup is performed via {@link WebServiceEnv}.
    * 
    * Also adds NO descriptors to calls that cannot contain DescriptorPolicy
    * because descriptors are NOT delivered by default for ProcessInstances
    * 
    * @param pi
    * @return
    */
   public static ProcessInstanceXto toWs(ProcessInstance pi)
   {
      Model model = WebServiceEnv.currentWebServiceEnvironment().getModel(pi.getModelOID());
      
      // this adds NO descriptors to calls that cannot contain
      // DescriptorPolicy
      // because descriptors are NOT delivered by default for ProcessInstances
      return toWs(pi, null, model);
   }
   
   /**
    * Usable even if {@link WebServiceEnv} is not available.
    * Because of this the model has to be specified.
    * 
    * Also adds NO descriptors to calls that cannot contain DescriptorPolicy
    * because descriptors are NOT delivered by default for ProcessInstances
    * 
    * @param pi
    * @param model model for lookups
    * @return
    */
   public static ProcessInstanceXto toWs(ProcessInstance pi, Model model)
   {
      return toWs(pi, null, model);
   }

   /**
    * Only use in WebService environment.
    * Model lookup is performed via {@link WebServiceEnv}.
    * 
    * @param pi
    * @param query
    * @return
    */
   public static ProcessInstanceXto toWs(ProcessInstance pi, Query query)
   {
      Model model = WebServiceEnv.currentWebServiceEnvironment().getModel(pi.getModelOID());
      
      return toWs(pi,query, model);
   }
   
   /**
    * Usable even if {@link WebServiceEnv} is not available.
    * Because of this the model has to be specified.
    * 
    * @param pi
    * @param query
    * @param model model for lookups
    * @return
    */
   public static ProcessInstanceXto toWs(ProcessInstance pi, Query query, Model model)
   {
      ProcessInstanceXto res = new ProcessInstanceXto();

      res.setOid(pi.getOID());

      res.setModelOid(pi.getModelOID());
      res.setModelElementId(pi.getModelElementID());
      res.setModelElementOid(pi.getModelElementOID());

      res.setProcessDefinitionId(pi.getProcessID());
      res.setProcessDefinitionName(pi.getProcessName());
      res.setRootProcessOid(pi.getRootProcessInstanceOID());
      res.setScopeProcessOid(pi.getScopeProcessInstanceOID());
      res.setState(pi.getState());
      res.setPriority(pi.getPriority());

      res.setDetailsLevel(marshalProcessInstanceDetailsLevel(pi.getDetailsLevel()));
      res.setDetailsOptions(marshalProcessInstanceDetailsOptions(pi.getDetailsOptions()));

      // TODO res.setStartingUser(marshal(pi.getStartingUser()));
      if (pi.getStartingUser() != null)
      {
         res.setStartingUser(toWs(pi.getStartingUser()));
      }

      res.setStartTime(pi.getStartTime());
      res.setTerminationTime(pi.getTerminationTime());

      DescriptorPolicy dp = (query == null)
            ? null
            : (DescriptorPolicy) query.getPolicy(DescriptorPolicy.class);
      boolean includeDescriptors = (dp == null) ? false : dp.includeDescriptors();

      if (model != null)
      {
         res.setInstanceProperties(marshalInstanceProperties(pi, includeDescriptors,
               model));
      }
      
      // TODO pi.getAttributes()

      res.setHistoricalEvents(marshalHistoricalEvents(pi.getHistoricalEvents()));

      res.setCaseProcessInstance(pi.isCaseProcessInstance());

      res.setLinkedProcessInstances(marshalProcessInstanceLinks(pi.getLinkedProcessInstances()));

      return res;
   }

   private static ProcessInstanceLinksXto marshalProcessInstanceLinks(
         List<ProcessInstanceLink> linkedProcessInstances)
   {
      ProcessInstanceLinksXto xto = null;
      if (linkedProcessInstances != null)
      {
         xto = new ProcessInstanceLinksXto();

         for (ProcessInstanceLink processInstanceLink : linkedProcessInstances)
         {
            xto.getProcessInstanceLink().add(
                  marshalProcessInstanceLink(processInstanceLink));
         }

      }
      return xto;
   }

   private static ProcessInstanceLinkXto marshalProcessInstanceLink(
         ProcessInstanceLink pil)
   {
      ProcessInstanceLinkXto xto = null;

      if (pil != null)
      {
         xto = new ProcessInstanceLinkXto();

         xto.setSourceOid(pil.getSourceOID());
         xto.setTargetOid(pil.getTargetOID());
         xto.setComment(pil.getComment());
         xto.setCreateTime(pil.getCreateTime());
         xto.setLinkType(marshalLinkType(pil.getLinkType()));
         xto.setCreatingUser(toWs(pil.getCreatingUser()));
      }

      return xto;
   }

   private static ProcessInstanceLinkTypeXto marshalLinkType(
         ProcessInstanceLinkType linkType)
   {
      ProcessInstanceLinkTypeXto xto = null;
      if (linkType != null)
      {
         xto = new ProcessInstanceLinkTypeXto();

         xto.setId(linkType.getId());
         xto.setOid(linkType.getOID());
         xto.setDescription(linkType.getDescription());
      }
      return xto;
   }

   public static ProcessInstanceDetailsLevelXto marshalProcessInstanceDetailsLevel(
         ProcessInstanceDetailsLevel detailsLevel)
   {
      if (ProcessInstanceDetailsLevel.Core.equals(detailsLevel))
      {
         return ProcessInstanceDetailsLevelXto.CORE;
      }
      else if (ProcessInstanceDetailsLevel.Default.equals(detailsLevel))
      {
         return ProcessInstanceDetailsLevelXto.DEFAULT;
      }
      else if (ProcessInstanceDetailsLevel.Full.equals(detailsLevel))
      {
         return ProcessInstanceDetailsLevelXto.FULL;
      }
      else if (ProcessInstanceDetailsLevel.WithProperties.equals(detailsLevel))
      {
         return ProcessInstanceDetailsLevelXto.WITH_PROPERTIES;
      }
      else if (ProcessInstanceDetailsLevel.WithResolvedProperties.equals(detailsLevel))
      {
         return ProcessInstanceDetailsLevelXto.WITH_RESOLVED_PROPERTIES;
      }
      throw new UnsupportedOperationException(
            "Marshaling unsupported for ProcessInstanceDetailsLevel: "
                  + detailsLevel.getName());
   }

   public static ProcessInstanceDetailsOptionsXto marshalProcessInstanceDetailsOptions(
         EnumSet<ProcessInstanceDetailsOptions> detailsOptions)
   {
      ProcessInstanceDetailsOptionsXto xto = null;
      if (detailsOptions != null)
      {
         xto = new ProcessInstanceDetailsOptionsXto();
         for (ProcessInstanceDetailsOptions processInstanceDetailsOptions : detailsOptions)
         {
            switch (processInstanceDetailsOptions)
            {
            case WITH_HIERARCHY_INFO:
               xto.getProcessInstanceDetailsOption().add(
                     ProcessInstanceDetailsOptionXto.WITH_HIERARCHY_INFO);
               break;
            case WITH_LINK_INFO:
               xto.getProcessInstanceDetailsOption().add(
                     ProcessInstanceDetailsOptionXto.WITH_LINK_INFO);
               break;
            case WITH_NOTES:
               xto.getProcessInstanceDetailsOption().add(
                     ProcessInstanceDetailsOptionXto.WITH_NOTES);
               break;
            }
         }
      }
      return xto;
   }

   @SuppressWarnings("unchecked")
   public static ActivityQueryResultXto toWs(ActivityInstances ais)
   {
      ActivityQueryResultXto ret = new ActivityQueryResultXto();
      ret.setTotalCount(mapTotalCount(ais));
      ret.setHasMore(ais.hasMore());

      ret.setActivityInstances(new ActivityInstancesXto());
      for (Iterator iterator = ais.iterator(); iterator.hasNext();)
      {
         ActivityInstance ai = (ActivityInstance) iterator.next();

         ret.getActivityInstances().getActivityInstance().add(toWs(ai, ais.getQuery()));
      }
      return ret;
   }

   public static LogEntryQueryResultXto toWs(LogEntries le)
   {
      LogEntryQueryResultXto ret = new LogEntryQueryResultXto();
      ret.setTotalCount(mapTotalCount(le));
      ret.setHasMore(le.hasMore());

      ret.setLogEntries(new LogEntriesXto());
      for (Iterator< ? > iterator = le.iterator(); iterator.hasNext();)
      {
         LogEntry l = (LogEntry) iterator.next();

         ret.getLogEntries().getLogEntry().add(toWs(l, le.getQuery()));
      }
      return ret;
   }

   private static LogEntryXto toWs(LogEntry l, Query query)
   {
      LogEntryXto ret = new LogEntryXto();

      ret.setActivityOid(l.getActivityInstanceOID());
      ret.setCode(toWs(l.getCode()));
      ret.setContext(l.getContext());
      ret.setOid(l.getOID());
      ret.setProcessOid(l.getProcessInstanceOID());
      ret.setSubject(l.getSubject());
      ret.setTimeStamp(l.getTimeStamp());
      ret.setType(toWs(l.getType()));
      ret.setUser(toWs(l.getUser()));
      ret.setUserOid(l.getUserOID());

      return ret;
   }

   private static LogCodeXto toWs(LogCode lc)
   {
      if (lc.getValue() == LogCode.ADMINISTRATION.getValue())
      {
         return LogCodeXto.ADMINISTRATION;
      }
      else if (lc.getValue() == LogCode.DAEMON.getValue())
      {
         return LogCodeXto.DAEMON;
      }
      else if (lc.getValue() == LogCode.ENGINE.getValue())
      {
         return LogCodeXto.ENGINE;
      }
      else if (lc.getValue() == LogCode.EVENT.getValue())
      {
         return LogCodeXto.EVENT;
      }
      else if (lc.getValue() == LogCode.EXTERNAL.getValue())
      {
         return LogCodeXto.EXTERNAL;
      }
      else if (lc.getValue() == LogCode.PWH.getValue())
      {
         return LogCodeXto.PROCESS_WAREHOUSE;
      }
      else if (lc.getValue() == LogCode.RECOVERY.getValue())
      {
         return LogCodeXto.RECOVERY;
      }
      else if (lc.getValue() == LogCode.SECURITY.getValue())
      {
         return LogCodeXto.SECURITY;
      }
      else if (lc.getValue() == LogCode.UNKNOWN.getValue())
      {
         return LogCodeXto.UNKNOWN;
      }
      else if (lc.getValue() == LogCode.WFXML.getValue())
      {
         return LogCodeXto.WF_XML;
      }
      else
      {
         trace.error("Error marshaling LogCode: LogCode mapping not implemented for: "
               + lc.getName());
         throw new UnsupportedOperationException(
               "Error marshaling LogCode: LogCode mapping not implemented for: "
                     + lc.getName());
      }
   }

   public static LogTypeXto toWs(LogType lt)
   {
      if (lt.getValue() == LogType.DEBUG)
      {
         return LogTypeXto.DEBUG;
      }
      else if (lt.getValue() == LogType.ERROR)
      {
         return LogTypeXto.ERROR;
      }
      else if (lt.getValue() == LogType.FATAL)
      {
         return LogTypeXto.FATAL;
      }
      else if (lt.getValue() == LogType.INFO)
      {
         return LogTypeXto.INFO;
      }
      else if (lt.getValue() == LogType.UNKNOWN)
      {
         return LogTypeXto.UNKNOWN;
      }
      else if (lt.getValue() == LogType.WARN)
      {
         return LogTypeXto.WARN;
      }
      else
      {
         trace.error("Error marshaling LogType: LogType mapping not implemented for: "
               + lt.getName());
         throw new UnsupportedOperationException(
               "Error marshaling LogType: LogType mapping not implemented for: "
                     + lt.getName());
      }
   }

   public static LogType unmarshalLogType(LogTypeXto type)
   {
      if (type == null)
      {
         return LogType.Unknwon;
      }
      switch (type)
      {
      case DEBUG:
         return LogType.Debug;
      case ERROR:
         return LogType.Error;
      case FATAL:
         return LogType.Fatal;
      case INFO:
         return LogType.Info;
      case UNKNOWN:
         return LogType.Unknwon;
      case WARN:
         return LogType.Warn;
      }
      throw new UnsupportedOperationException(
            "unmarshalLogType failed: mapping not implemented for " + type.name());
   }

   public static WorklistXto toWs(Worklist wl)
   {
      WorklistXto res = new WorklistXto();

      UserWorklistXto uwl = new UserWorklistXto();
      uwl.setOwner(marshalUserInfo(wl));
      uwl.setTotalCount(mapTotalCount(wl));
      uwl.setHasMore(wl.hasMore());

      uwl.setWorkItems(new ActivityInstancesXto());

      for (int i = 0; i < wl.size(); ++i)
      {
         uwl.getWorkItems()
               .getActivityInstance()
               .add(toWs(
                     (org.eclipse.stardust.engine.api.runtime.ActivityInstance) wl.get(i),
                     wl.getQuery()));
      }

      res.setUserWorklist(uwl);

      res.setSharedWorklists(new SharedWorklistsXto());
      for (@SuppressWarnings("unchecked")
      Iterator<ParticipantWorklist> subWorklistItr = wl.getSubWorklists(); subWorklistItr.hasNext();)
      {
         ParticipantWorklist pwl = subWorklistItr.next();

         res.getSharedWorklists().getSharedWorklist().add(toWs(pwl));
      }

      return res;
   }

   private static UserInfoXto marshalUserInfo(Worklist wl)
   {
      UserInfoXto ret = new UserInfoXto();
      ret.setId(wl.getOwnerID());
      ret.setName(wl.getOwnerName());
      ret.setOid(wl.getOwnerOID());

      return ret;

   }

   public static SharedWorklistXto toWs(ParticipantWorklist pwl)
   {
      SharedWorklistXto swl = new SharedWorklistXto();
      swl.setOwner(marshalUserInfo(pwl));
      swl.setTotalCount(mapTotalCount(pwl));
      swl.setHasMore(pwl.hasMore());

      swl.setWorkItems(new ActivityInstancesXto());

      for (int i = 0; i < pwl.size(); ++i)
      {
         swl.getWorkItems()
               .getActivityInstance()
               .add(toWs((ActivityInstance) pwl.get(i), pwl.getQuery()));
      }

      return swl;
   }

   private static Long mapTotalCount(QueryResult< ? > qr)
   {
      Long totalCount = null;
      try
      {
         totalCount = qr.getTotalCount();
      }
      catch (UnsupportedOperationException e)
      {
         totalCount = null;
      }
      return totalCount;
   }

   public static ActivityInstancesXto toWs(ActivityCompletionLog acl)
   {
      ActivityInstancesXto xto = new ActivityInstancesXto();
      xto.getActivityInstance().add(toWs(acl.getCompletedActivity(), null));
      if (acl.getNextForUser() != null)
      {
         xto.getActivityInstance().add(toWs(acl.getNextForUser(), null));
      }
      return xto;
   }

   public static ActivityInstanceXto toWs(ActivityInstance ai)
   {
      // this adds descriptors to calls that cannot contain DescriptorPolicy
      // because descriptors are delivered by default for ActivityInstances
      return toWs(ai, null);
   }

   public static ActivityInstanceXto toWs(ActivityInstance ai, Query query)
   {
      ActivityInstanceXto res = null;
      if (ai != null)
      {
         res = new ActivityInstanceXto();
         res.setOid(ai.getOID());

         res.setModelOid(ai.getModelOID());
         res.setModelElementId(ai.getModelElementID());
         res.setModelElementOid(ai.getModelElementOID());

         res.setActivityId(ai.getActivity().getId());
         res.setActivityName(ai.getActivity().getName());
         res.setProcessDefinitionId(ai.getProcessDefinitionId());

         // TODO replace with regular getter
         if (ai instanceof ActivityInstanceDetails)
         {
            res.setProcessDefinitionName((String) Reflect.getFieldValue(ai,
                  "processDefinitionName"));
         }
         if (ai instanceof ActivityInstanceDetails)
         {
            res.setPermissionStates(marshalPermissionStates((Map<String, PermissionState>) Reflect.getFieldValue(
                  ai, "permissions")));
         }

         res.setProcessOid(ai.getProcessInstanceOID());
         // res.setProcessPriority(getProcessPriority(ai.getProcessInstanceOID()));
         // res.setScopeProcessOid(ai.getScopeProcessInstanceOID());
         res.setState(ai.getState());

         if (ai.getPerformedBy() != null)
         {
            res.setPerformedBy((UserInfoXto) marshalParticipantInfo(ai.getPerformedBy(),
                  new UserInfoXto()));
         }

         if (ai.getCurrentPerformer() != null)
         {
            res.setCurrentPerformer(toWs(ai.getCurrentPerformer()));
         }

         if (ai.getUserPerformer() != null)
         {
            res.setUserPerformer(toWs(ai.getUserPerformer()));
         }

         res.setAssignedToModelParticipant(ai.isAssignedToModelParticipant());
         res.setAssignedToUser(ai.isAssignedToUser());
         res.setAssignedToUserGroup(ai.isAssignedToUserGroup());
         res.setScopeProcessInstanceNoteAvailable(ai.isScopeProcessInstanceNoteAvailable());

         res.setStartTime(ai.getStartTime());
         res.setLastModificationTime(ai.getLastModificationTime());

         // TODO add ActivityInstance attributes?

         DescriptorPolicy dp = (query == null)
               ? null
               : (DescriptorPolicy) query.getPolicy(DescriptorPolicy.class);
         if (dp == null || dp.includeDescriptors())
         {
            res.setInstanceProperties(marshalInstanceProperties(ai));

            Model model = WebServiceEnv.currentWebServiceEnvironment().getModel(
                  ai.getModelOID());
            res.setDescriptorDefinitions(marshalDataPathList(
                  ai.getDescriptorDefinitions(), model));
         }

         res.setHistoricalStates(marshalHistoricalStates(ai.getHistoricalStates()));

         res.setHistoricalEvents(marshalHistoricalEvents(ai.getHistoricalEvents()));

         final ModelParticipant defaultPerformer = ai.getActivity().getDefaultPerformer();
         if (defaultPerformer instanceof ConditionalPerformer)
         {
            final ConditionalPerformer conditionalPerformer = (ConditionalPerformer) defaultPerformer;
            final Participant resolvedPerformer = conditionalPerformer.getResolvedPerformer();
            if (resolvedPerformer != null)
            {
               res.setConditionalPerformerId(resolvedPerformer.getId());
               res.setConditionalPerformerName(resolvedPerformer.getName());
            }
         }
      }
      return res;
   }

   private static PermissionStatesXto marshalPermissionStates(
         Map<String, PermissionState> permissions)
   {
      PermissionStatesXto ret = null;
      if (permissions != null)
      {
         ret = new PermissionStatesXto();
         for (Entry<String, PermissionState> entry : permissions.entrySet())
         {
            PermissionStateXto xto = new PermissionStateXto();
            xto.setPermissionId(entry.getKey());
            xto.setState(marshalPermissionState(entry.getValue()));

            ret.getPermissionState().add(xto);
         }
      }
      return ret;
   }

   private static org.eclipse.stardust.engine.api.ws.PermissionStateXto marshalPermissionState(
         PermissionState value)
   {
      org.eclipse.stardust.engine.api.ws.PermissionStateXto ret = null;

      if (value != null)
      {
         if (PermissionState.Denied.equals(value))
         {
            ret = org.eclipse.stardust.engine.api.ws.PermissionStateXto.DENIED;
         }
         else if (PermissionState.Granted.equals(value))
         {
            ret = org.eclipse.stardust.engine.api.ws.PermissionStateXto.GRANTED;
         }
         else if (PermissionState.Unknown.equals(value))
         {
            ret = org.eclipse.stardust.engine.api.ws.PermissionStateXto.UNKNOWN;
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Marshaling of PermissionState not supported for: " + value.getName());
         }
      }

      return ret;
   }

   public static Map<String, PermissionState> unmarshalPermissionStates(
         PermissionStatesXto permissions)
   {
      Map<String, PermissionState> ret = null;
      if (permissions != null)
      {
         ret = CollectionUtils.newHashMap();
         for (PermissionStateXto entry : permissions.getPermissionState())
         {
            ret.put(entry.getPermissionId(), unmarshalPermissionState(entry.getState()));
         }
      }
      return ret;
   }

   private static PermissionState unmarshalPermissionState(
         org.eclipse.stardust.engine.api.ws.PermissionStateXto value)
   {
      PermissionState ret = null;

      if (value != null)
      {
         if (org.eclipse.stardust.engine.api.ws.PermissionStateXto.DENIED.equals(value))
         {
            ret = PermissionState.Denied;
         }
         else if (org.eclipse.stardust.engine.api.ws.PermissionStateXto.GRANTED.equals(value))
         {
            ret = PermissionState.Granted;
         }
         else if (org.eclipse.stardust.engine.api.ws.PermissionStateXto.UNKNOWN.equals(value))
         {
            ret = PermissionState.Unknown;
         }
         else
         {
            throw new UnsupportedOperationException(
                  "Marshaling of PermissionState not supported for: " + value.name());
         }
      }

      return ret;
   }

   private static HistoricalStatesXto marshalHistoricalStates(
         List<HistoricalState> historicalStates)
   {
      HistoricalStatesXto ret = null;
      if (historicalStates != null && !historicalStates.isEmpty())
      {
         ret = new HistoricalStatesXto();
         for (HistoricalState historicalState : historicalStates)
         {
            ret.getHistoricalState().add(toWs(historicalState));
         }
      }
      return ret;
   }

   private static HistoricalStateXto toWs(HistoricalState state)
   {
      HistoricalStateXto ret = null;
      if (state != null)
      {
         ret = new HistoricalStateXto();
         ret.setActivityDefinitionId(state.getActivityId());
         ret.setActivityOid(state.getActivityInstanceOID());
         ret.setProcessDefinitionId(state.getProcessDefinitionId());
         ret.setProcessInstanceOid(state.getProcessInstanceOID());
         ret.setFrom(state.getFrom());
         ret.setUntil(ret.getUntil());
         ret.setActivityState(state.getState());

         ret.setParticipant(toWs(state.getParticipant()));
         ret.setOnBehalfOfParticipant(toWs(state.getOnBehalfOfParticipant()));
         ret.setUser(toWs(state.getUser()));
      }
      return ret;
   }

   public static PasswordRulesXto toWs(PasswordRules passwordRules)
   {
      PasswordRulesXto ret = null;

      if (passwordRules != null)
      {
         ret = new PasswordRulesXto();

         ret.setDifferentCharacters(passwordRules.getDifferentCharacters());
         ret.setDigits(passwordRules.getDigits());
         ret.setDisableUserTime(passwordRules.getDisableUserTime());
         ret.setExpirationTime(passwordRules.getExpirationTime());
         ret.setLetters(passwordRules.getLetters());
         ret.setMinimalPasswordLength(passwordRules.getMinimalPasswordLength());
         ret.setMixedCase(passwordRules.getMixedCase());
         ret.setPasswordTracking(passwordRules.getPasswordTracking());
         ret.setPunctuation(passwordRules.getPunctuation());
         ret.setNotificationMails(passwordRules.getNotificationMails());
      }

      return ret;
   }

   public static PasswordRules fromWs(PasswordRulesXto passwordRules)
   {
      PasswordRules ret = null;

      if (passwordRules != null)
      {
         ret = new PasswordRulesDetails();

         ret.setDifferentCharacters(passwordRules.getDifferentCharacters());
         ret.setDigits(passwordRules.getDigits());
         ret.setDisableUserTime(passwordRules.getDisableUserTime());
         ret.setExpirationTime(passwordRules.getExpirationTime());
         ret.setLetters(passwordRules.getLetters());
         ret.setMinimalPasswordLength(passwordRules.getMinimalPasswordLength());
         ret.setMixedCase(passwordRules.getMixedCase());
         ret.setPasswordTracking(passwordRules.getPasswordTracking());
         ret.setPunctuation(passwordRules.getPunctuation());
         ret.setNotificationMails(passwordRules.getNotificationMails());
      }

      return ret;
   }

   public static ParticipantInfoXto toWs(ParticipantInfo participantInfo)
   {
      ParticipantInfoXto ret;

      if (participantInfo instanceof UserInfo)
      {
         ret = marshalParticipantInfo(participantInfo, new UserInfoXto());
      }
      else if (participantInfo instanceof UserGroupInfo)
      {
         ret = marshalParticipantInfo(participantInfo, new UserGroupInfoXto());
      }
      else if (participantInfo instanceof RoleInfo)
      {
         ret = marshalParticipantInfo(participantInfo, new RoleInfoXto());
      }
      else if (participantInfo instanceof OrganizationInfo)
      {
         ret = marshalParticipantInfo(participantInfo, new OrganizationInfoXto());
      }
      else if (participantInfo instanceof ConditionalPerformerInfo)
      {
         ret = marshalParticipantInfo(participantInfo, new ConditionalPerformerInfoXto());
      }
      else if (participantInfo == null)
      {
         ret = null;
      }
      else if (participantInfo instanceof ModelParticipantInfo)
      {
         // workaround because of
         // org.eclipse.stardust.engine.api.query.PerformingParticipantFilter$LegacyModelParticipant
         ret = marshalParticipantInfo(participantInfo, new ModelParticipantInfoXto());
      }
      else if (participantInfo instanceof DynamicParticipantInfo)
      {
         // fallback workaround just in case, should not be used
         ret = marshalParticipantInfo(participantInfo, new DynamicParticipantInfoXto());
      }
      else if (participantInfo instanceof ParticipantInfo)
      {
         // fallback workaround just in case, should not be used
         ret = marshalParticipantInfo(participantInfo, new ParticipantInfoXto());
      }
      else
      {

         throw new UnsupportedOperationException(
               "Marshaling of ParticipantInfo type not supported: " + participantInfo
                     + " class: " + participantInfo.getClass());
      }

      return ret;

   }

   private static ParticipantInfoXto marshalParticipantInfo(
         ParticipantInfo participantInfo, ParticipantInfoXto xto)
   {
      xto.setId(participantInfo.getId());
      xto.setName(participantInfo.getName());

      if (participantInfo instanceof ModelParticipantInfo)
      {
         xto = marshalModelParticipantInfo((ModelParticipantInfo) participantInfo,
               (ModelParticipantInfoXto) xto);
      }
      else if (participantInfo instanceof DynamicParticipantInfo)
      {
         xto = marshalDynamicParticipantInfo((DynamicParticipantInfo) participantInfo,
               (DynamicParticipantInfoXto) xto);
      }
      // else
      // {
      // throw new UnsupportedOperationException(
      // "Marshaling of ParticipantInfo type not supported: " +
      // participantInfo);
      // }

      return xto;
   }

   private static ParticipantInfoXto marshalDynamicParticipantInfo(
         DynamicParticipantInfo participantInfo, DynamicParticipantInfoXto xto)
   {
      xto.setOid(participantInfo.getOID());
      return xto;
   }

   private static ModelParticipantInfoXto marshalModelParticipantInfo(
         ModelParticipantInfo participantInfo, ModelParticipantInfoXto xto)
   {
      xto.setRuntimeElementOid(participantInfo.getRuntimeElementOID());
      xto.setDefinesDepartmentScope(participantInfo.definesDepartmentScope());
      xto.setDepartment(marshalDepartmentInfo(participantInfo.getDepartment(),
            new DepartmentInfoXto()));
      return xto;
   }

   public static DepartmentsXto marshalDepartmentList(List<Department> departments)
   {
      DepartmentsXto ret = new DepartmentsXto();

      for (Department dep : departments)
      {
         ret.getDepartment().add(toWs(dep));
      }
      return ret;
   }

   public static DepartmentXto toWs(Department department)
   {
      DepartmentXto ret = null;

      if (department != null && department != Department.DEFAULT)
      {
         ret = marshalDepartmentInfo(department, new DepartmentXto());
         ret.setDescription(department.getDescription());
         ret.setOrganization(toWs(department.getOrganization(),
               PARTICIPANT_MARSHALING_TRAVERSAL_DEPTH));
         ret.setParentDepartment(toWs(department.getParentDepartment()));
      }
      return ret;
   }

   public static DepartmentInfoXto marshalDepartmentInfo(DepartmentInfo department)
   {
      return marshalDepartmentInfo(department, new DepartmentInfoXto());
   }

   private static <T extends DepartmentInfoXto> T marshalDepartmentInfo(
         DepartmentInfo department, T xto)
   {
      if (department != null)
      {
         xto.setId(department.getId());
         xto.setName(department.getName());
         xto.setOid(department.getOID());
         xto.setRuntimeOrganizationOid(department.getRuntimeOrganizationOID());
      }
      else
      {
         xto = null;
      }
      return xto;
   }

   public static ParticipantInfo unmarshalParticipantInfo(ParticipantInfoXto pInfo)
   {
      ParticipantInfo ret;

      if (pInfo instanceof UserInfoXto)
      {
         ret = new UserInfoDetails(((DynamicParticipantInfoXto) pInfo).getOid(),
               pInfo.getId(), pInfo.getName());
      }
      else if (pInfo instanceof UserGroupInfoXto)
      {
         ret = new UserGroupInfoDetails(((DynamicParticipantInfoXto) pInfo).getOid(),
               pInfo.getId(), pInfo.getName());
      }
      else if (pInfo instanceof RoleInfoXto)
      {
         ret = new RoleInfoDetails(
               ((ModelParticipantInfoXto) pInfo).getRuntimeElementOid(), pInfo.getId(),
               pInfo.getName(), ((ModelParticipantInfoXto) pInfo).isDepartmentScoped(),
               ((ModelParticipantInfoXto) pInfo).isDefinesDepartmentScope(),
               unmarshalDepartmentInfo(((ModelParticipantInfoXto) pInfo).getDepartment()));
      }
      else if (pInfo instanceof OrganizationInfoXto)
      {
         ret = new OrganizationInfoDetails(
               ((ModelParticipantInfoXto) pInfo).getRuntimeElementOid(), pInfo.getId(),
               pInfo.getName(), ((ModelParticipantInfoXto) pInfo).isDepartmentScoped(),
               ((ModelParticipantInfoXto) pInfo).isDefinesDepartmentScope(),
               unmarshalDepartmentInfo(((ModelParticipantInfoXto) pInfo).getDepartment()));
      }
      else if (pInfo instanceof ConditionalPerformerInfoXto)
      {
         ret = new ConditionalPerformerInfoDetails(
               ((ModelParticipantInfoXto) pInfo).getRuntimeElementOid(), pInfo.getId(),
               pInfo.getName(),
               unmarshalDepartmentInfo(((ModelParticipantInfoXto) pInfo).getDepartment()));
      }
      else if (pInfo == null)
      {
         ret = null;
      }
      else if (pInfo instanceof ModelParticipantInfoXto)
      {
         // workaround because of
         // org.eclipse.stardust.engine.api.query.PerformingParticipantFilter$LegacyModelParticipant
         ret = new LegacyModelParticipant(pInfo.getId());
      }

      else
      {
         throw new UnsupportedOperationException(
               "Unmarshaling of ParticipantInfo type not supported: " + pInfo);
      }
      return ret;
   }

   private static final class LegacyModelParticipant extends ModelParticipantInfoDetails
   {
      private static final long serialVersionUID = 1L;

      public LegacyModelParticipant(String id)
      {
         super(0, id, null, false, false, null);
      }
   }

   public static Collection< ? extends ParticipantInfo> unmarshalParticipantInfoList(
         List<ParticipantInfoXto> participant)
   {
      List<ParticipantInfo> ret = CollectionUtils.newArrayList();
      for (ParticipantInfoXto xto : participant)
      {
         ret.add(unmarshalParticipantInfo(xto));
      }
      return ret;
   }

   public static DepartmentInfo unmarshalDepartmentInfo(DepartmentInfoXto department)
   {
      return department == null ? null : new DepartmentInfoDetails(department.getOid(),
            department.getId(), department.getName(),
            department.getRuntimeOrganizationOid());
   }

   private static HistoricalEventsXto marshalHistoricalEvents(
         List<HistoricalEvent> historicalEvents)
   {
      HistoricalEventsXto ret = null;
      if (historicalEvents != null && !historicalEvents.isEmpty())
      {
         ret = new HistoricalEventsXto();
         for (HistoricalEvent historicalEvent : historicalEvents)
         {
            ret.getHistoricalEvent().add(toWs(historicalEvent));
         }
      }
      return ret;
   }

   private static HistoricalEventXto toWs(HistoricalEvent event)
   {
      HistoricalEventXto ret = null;
      if (event != null)
      {
         ret = new HistoricalEventXto();
         ret.setEventTime(event.getEventTime());
         ret.setEventType(marshalHistoricalEventType(event.getEventType()));
         ret.setUser(toWs(event.getUser()));
         ret.setEventDetails(marshalHistoricalEventDetails(event.getDetails()));
      }
      return ret;
   }

   private static HistoricalEventDetailsXto marshalHistoricalEventDetails(
         Serializable details)
   {
      HistoricalEventDetailsXto ret = new HistoricalEventDetailsXto();
      if (details == null)
      {
         ret = null;
      }
      else if (details instanceof HistoricalEventDescriptionStateChange)
      {
         ret.setFromState(((HistoricalEventDescriptionStateChange) details).getFromState());
         ret.setToState(((HistoricalEventDescriptionStateChange) details).getToState());
         ret.setToPerformer(toWs(((HistoricalEventDescriptionStateChange) details).getToPerformer()));
      }
      else if (details instanceof HistoricalEventDescriptionDelegation)
      {
         ret.setFromPerformer(toWs(((HistoricalEventDescriptionDelegation) details).getFromPerformer()));
         ret.setToPerformer(toWs(((HistoricalEventDescriptionDelegation) details).getToPerformer()));

      }
      else if (details instanceof String)
      {
         ret.setText((String) details);
      }
      else
      {
         throw new UnsupportedOperationException(
               "Error marshaling HistoricalEventDetails: marshaling not supported for: "
                     + details.getClass());
      }
      return ret;
   }

   private static HistoricalEventTypeXto marshalHistoricalEventType(
         HistoricalEventType eventType)
   {
      if (HistoricalEventType.StateChange.equals(eventType))
      {
         return HistoricalEventTypeXto.STATE_CHANGE;
      }
      else if (HistoricalEventType.Delegation.equals(eventType))
      {
         return HistoricalEventTypeXto.DELEGATION;
      }
      else if (HistoricalEventType.Note.equals(eventType))
      {
         return HistoricalEventTypeXto.NOTE;
      }
      else if (HistoricalEventType.Exception.equals(eventType))
      {
         return HistoricalEventTypeXto.EXCEPTION;
      }
      else
      {
         throw new UnsupportedOperationException(
               "Marshaling of HistoricalEventType not supported for: "
                     + eventType.getName());
      }
   }

   public static HistoricalEventType unmarshalHistoricalEventType(
         HistoricalEventTypeXto eventTypeXto)
   {
      if (HistoricalEventTypeXto.STATE_CHANGE.equals(eventTypeXto))
      {
         return HistoricalEventType.StateChange;
      }
      else if (HistoricalEventTypeXto.DELEGATION.equals(eventTypeXto))
      {
         return HistoricalEventType.Delegation;
      }
      else if (HistoricalEventTypeXto.NOTE.equals(eventTypeXto))
      {
         return HistoricalEventType.Note;
      }
      else if (HistoricalEventTypeXto.EXCEPTION.equals(eventTypeXto))
      {
         return HistoricalEventType.Exception;
      }
      else
      {
         throw new UnsupportedOperationException(
               "Unarshaling of HistoricalEventType not supported for: "
                     + eventTypeXto.name());
      }
   }

   public static <T extends EventBindingBaseXto> T toWs(EventHandlerBinding binding, T xto)
   {
      if (binding != null)
      {
         xto.setHandlerId(binding.getHandler().getId());
         xto.setBound(binding.isBound());

         Map< ? , ? > bindingAttributes = binding.getAllAttributes();
         if (null != binding.getAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT))
         {
            // copy attributes so we can safely remove the timestamp
            bindingAttributes = copyMap(bindingAttributes);

            Object targetTimestamp = bindingAttributes.remove(PredefinedConstants.TARGET_TIMESTAMP_ATT);
            if (targetTimestamp instanceof Date)
            {
               xto.setTimeout((Date) targetTimestamp);
            }
            else if (targetTimestamp instanceof Long)
            {
               xto.setTimeout(new Date(((Long) targetTimestamp).longValue()));
            }
            else
            {
               if (targetTimestamp != null)
               {
                  trace.warn("Error marshaling  EventBinding: timestamp type unsupported: "
                        + targetTimestamp.getClass());
               }
            }
         }
         if ( !isEmpty(bindingAttributes))
         {
            xto.setBindingAttributes(XmlAdapterUtils.marshalAttributes(bindingAttributes));
         }
      }
      else
      {
         xto = null;
      }

      return xto;
   }

   public static EventHandlerBinding fromWs(EventBindingBaseXto xto,
         EventHandlerBinding binding)
   {
      Map<String, Object> bindingAttributes = unmarshalAttributes(xto.getBindingAttributes());
      if ( !isEmpty(bindingAttributes))
      {
         for (Entry<String, Object> entry : bindingAttributes.entrySet())
         {
            binding.setAttribute(entry.getKey(), entry.getValue());
         }
      }

      if (null != xto.getTimeout())
      {
         binding.setAttribute(PredefinedConstants.TARGET_TIMESTAMP_ATT,
               Long.valueOf(xto.getTimeout().getTime()));
      }

      return binding;
   }

   public static InstancePropertiesXto toWs(Long processInstanceOid,
         Map<String, Serializable> dataPaths)
   {
      return marshalProcessInstanceProperties(processInstanceOid, dataPaths);
   }

   public static DocumentXto toWs(Document doc, QName metaDataType,
         Set<TypedXPath> metaDataXPaths)
   {
      DocumentXto xto = marshalDocumentXto(doc, new DocumentXto());

      DocumentType documentType = doc == null ? null : doc.getDocumentType();
      marshalDmsMetaData(doc, xto, metaDataType, metaDataXPaths, documentType);

      return xto;
   }

   public static DocumentXto toWs(Document doc, Model model, String metaDataTypeId)
   {
      DocumentXto xto = marshalDocumentXto(doc, new DocumentXto());

      DocumentType documentType = doc == null ? null : doc.getDocumentType();
      marshalDmsMetaData(doc, xto, model, metaDataTypeId, documentType);

      return xto;
   }

   public static DocumentQueryResultXto toWs(Documents documents)
   {
      DocumentQueryResultXto ret = new DocumentQueryResultXto();
      ret.setTotalCount(mapTotalCount(documents));
      ret.setHasMore(documents.hasMore());

      ret.setDocuments(new DocumentsXto());
      for (Iterator iterator = documents.iterator(); iterator.hasNext();)
      {
         Document document = (Document) iterator.next();

         ret.getDocuments()
               .getDocument()
               .add(toWs(document, (QName) null, (Set<TypedXPath>) null));
      }
      return ret;
   }

   public static DocumentsXto toWs(List< ? extends Document> documents,
         QName metaDataType, Set<TypedXPath> metaDataXPaths)
   {
      DocumentsXto ret = new DocumentsXto();
      for (Document document : documents)
      {
         ret.getDocument().add(toWs(document, metaDataType, metaDataXPaths));
      }
      return ret;
   }

   @SuppressWarnings("unchecked")
   private static DocumentXto marshalDocumentXto(Document doc, DocumentXto xto)
   {
      marshalDocumentInfoXto(doc, xto);

      xto.setId(doc.getId());
      xto.setPath(doc.getPath());
      xto.setSize(doc.getSize());

      xto.setRevisionId(doc.getRevisionId());
      xto.setRevisionName(doc.getRevisionName());
      xto.setRevisionComment(doc.getRevisionComment());
      xto.setVersionLabels(new VersionLabelsXto());
      if ( !isEmpty(doc.getVersionLabels()))
      {
         xto.getVersionLabels().getVersionLabel().addAll(doc.getVersionLabels());
      }

      xto.setEncoding(doc.getEncoding());

      return xto;
   }

   public static Document fromXto(DocumentXto xto, Model model, String metaDataTypeId,
         Document doc)
   {
      unmarshalDocumentXto(xto, doc);

      unmarshalDmsMetaData(xto, doc, model, metaDataTypeId);

      return doc;
   }

   private static Document unmarshalDocumentXto(DocumentXto xto, Document doc)
   {
      unmarshalDocumentInfoXto(xto, doc);

      if (doc instanceof DmsDocumentBean)
      {
         @SuppressWarnings("unchecked")
         Map<String, Object> internals = ((DmsDocumentBean) doc).vfsResource();

         if ( !isEmpty(xto.getId()))
         {
            internals.put(AuditTrailUtils.RES_ID, xto.getId());
         }
         if ( !isEmpty(xto.getPath()))
         {
            internals.put(AuditTrailUtils.RES_PATH, xto.getPath());
         }
         internals.put(AuditTrailUtils.FILE_SIZE, Long.valueOf(xto.getSize()));
         if ( !isEmpty(xto.getRevisionId()))
         {
            internals.put(AuditTrailUtils.FILE_REVISION_ID, xto.getRevisionId());
         }
         if ( !isEmpty(xto.getRevisionName()))
         {
            internals.put(AuditTrailUtils.FILE_REVISION_NAME, xto.getRevisionName());
         }

         if ( !isEmpty(xto.getVersionLabels().getVersionLabel()))
         {
            internals.put(AuditTrailUtils.FILE_VERSION_LABELS, xto.getVersionLabels()
                  .getVersionLabel());
         }

         if ( !isEmpty(xto.getEncoding()))
         {
            internals.put(AuditTrailUtils.FILE_ENCODING, xto.getEncoding());
         }
      }

      return doc;
   }

   public static FoldersXto toWs(List<Folder> folders, Model model,
         QName documentMetaDataType, QName folderMetaDataType)
   {
      FoldersXto ret = new FoldersXto();

      for (Folder folder : folders)
      {
         ret.getFolder().add(
               toWs(folder, model, documentMetaDataType, folderMetaDataType));
      }

      return ret;
   }

   public static FolderXto toWs(Folder folder, Model model, String metaDataTypeId)
   {
      QName metaDataType = null;
      if (metaDataTypeId != null)
      {
         metaDataType = getStructuredTypeName(model, metaDataTypeId);
      }
      return toWs(folder, model, null, metaDataType);
   }

   public static FolderXto toWs(Folder folder, Model model, QName documentMetaDataType,
         QName folderMetaDataType)
   {
      FolderXto xto = null;

      if (folder != null)
      {

         xto = new FolderXto();

         marshalFolderInfoXto(folder, xto);

         Set<TypedXPath> folderMetaDataXPaths = null;
         if (null != folderMetaDataType)
         {
            folderMetaDataXPaths = inferStructDefinition(folderMetaDataType, model);
         }

         marshalDmsMetaData(folder, xto, folderMetaDataType, folderMetaDataXPaths, null);

         xto.setId(folder.getId());
         xto.setPath(folder.getPath());

         switch (folder.getLevelOfDetail())
         {
         case Folder.LOD_NO_MEMBERS:
            xto.setLevelOfDetail(FolderLevelOfDetailXto.NO_MEMBERS);
            break;

         case Folder.LOD_LIST_MEMBERS:
            xto.setLevelOfDetail(FolderLevelOfDetailXto.DIRECT_MEMBERS);
            break;

         case Folder.LOD_LIST_MEMBERS_OF_MEMBERS:
            xto.setLevelOfDetail(FolderLevelOfDetailXto.MEMBERS_OF_MEMBERS);
            break;

         default:
            trace.error("Unsupported folder level of detail: "
                  + folder.getLevelOfDetail());
            break;
         }

         xto.setDocumentCount(folder.getDocumentCount());
         if ( !isEmpty(folder.getDocuments()))
         {
            Set<TypedXPath> docMetaDataXPaths = null;
            if (null != documentMetaDataType)
            {
               docMetaDataXPaths = inferStructDefinition(documentMetaDataType, model);
            }

            xto.setDocuments(new DocumentsXto());
            @SuppressWarnings("unchecked")
            List<Document> docs = folder.getDocuments();
            for (Document doc : docs)
            {
               xto.getDocuments()
                     .getDocument()
                     .add(toWs(doc, documentMetaDataType, docMetaDataXPaths));
            }
         }

         xto.setFolderCount(folder.getFolderCount());
         if ( !isEmpty(folder.getFolders()))
         {
            xto.setFolders(new FoldersXto());
            @SuppressWarnings("unchecked")
            List<Folder> subFolders = folder.getFolders();
            for (Folder subFolder : subFolders)
            {
               xto.getFolders()
                     .getFolder()
                     .add(toWs(subFolder, model, documentMetaDataType, folderMetaDataType));
            }
         }
      }
      return xto;
   }

   public static RepositoryMigrationReportXto toWs(RepositoryMigrationReport report)
   {
      RepositoryMigrationReportXto ret = null;
      if (report != null)
      {
         ret = new RepositoryMigrationReportXto();

         ret.setTotalCount(report.getTotalCount());
         ret.setResourcesDone(report.getResourcesDone());
         ret.setTargetRepositoryVersion(report.getTargetRepositoryVersion());
         ret.setCurrentRepositoryVersion(ret.getCurrentRepositoryVersion());

         if (report.getCurrentMigrationJob() != null)
         {
            RepositoryMigrationJobInfoXto jobXto = new RepositoryMigrationJobInfoXto();
            RepositoryMigrationJobInfo job = report.getCurrentMigrationJob();

            jobXto.setName(job.getName());
            jobXto.setDescription(job.getDescription());
            jobXto.setFromVersion(job.getFromVersion());
            jobXto.setToVersion(job.getToVersion());
            ret.setCurrentMigrationJob(jobXto);
         }
      }
      return ret;
   }

   public static Folder unmarshalFolder(FolderXto xto, Model model)
   {
      Folder folder = new DmsFolderBean();

      unmarshalFolderInfoXto(xto, folder);

      Set<TypedXPath> metaDataXPaths = null;
      if ((null != xto.getMetaData()) && (null != xto.getMetaDataType()))
      {
         metaDataXPaths = inferStructDefinition(xto.getMetaDataType(), model);
      }

      unmarshalDmsMetaData(xto, folder, metaDataXPaths, null);

      if (folder instanceof DmsFolderBean)
      {
         @SuppressWarnings("unchecked")
         Map<String, Object> internals = ((DmsFolderBean) folder).vfsResource();

         if ( !isEmpty(xto.getId()))
         {
            internals.put(AuditTrailUtils.RES_ID, xto.getId());
         }
         if ( !isEmpty(xto.getPath()))
         {
            internals.put(AuditTrailUtils.RES_PATH, xto.getPath());
         }
         internals.put(AuditTrailUtils.FOLDER_DOCUMENT_COUNT, xto.getDocumentCount());
         internals.put(AuditTrailUtils.FOLDER_DOCUMENTS, fromXto(xto.getDocuments(), model, null));

         internals.put(AuditTrailUtils.FOLDER_FOLDER_COUNT, xto.getFolderCount());
         internals.put(AuditTrailUtils.FOLDER_SUB_FOLDERS, fromXto(xto.getFolders(), model, null));
      }

      return folder;
   }

   public static Folder fromXto(FolderXto xto, Model model, String metaDataTypeId,
         Folder folder)
   {
      unmarshalFolderInfoXto(xto, folder);

      unmarshalDmsMetaData(xto, folder, model, metaDataTypeId);

      if (folder instanceof DmsFolderBean)
      {
         @SuppressWarnings("unchecked")
         Map<String, Object> internals = ((DmsFolderBean) folder).vfsResource();

         if ( !isEmpty(xto.getId()))
         {
            internals.put(AuditTrailUtils.RES_ID, xto.getId());
         }
         if ( !isEmpty(xto.getPath()))
         {
            internals.put(AuditTrailUtils.RES_PATH, xto.getPath());
         }
         internals.put(AuditTrailUtils.FOLDER_DOCUMENT_COUNT,
               Integer.valueOf(xto.getDocumentCount()));
         internals.put(AuditTrailUtils.FOLDER_DOCUMENTS, fromXto(xto.getDocuments(), model, metaDataTypeId));

         internals.put(AuditTrailUtils.FOLDER_FOLDER_COUNT,
               Integer.valueOf(xto.getFolderCount()));
         internals.put(AuditTrailUtils.FOLDER_SUB_FOLDERS, fromXto(xto.getFolders(), model, metaDataTypeId));
      }

      return folder;
   }
   
   private static List<Map> fromXto(DocumentsXto documents, Model model,
         String metaDataTypeId)
   {
      List<Map> ret = null;

      if (documents != null)
      {
         ret = CollectionUtils.newArrayList();

         for (DocumentXto document : documents.getDocument())
         {
            DmsDocumentBean folderBean = (DmsDocumentBean) fromXto(document, model, metaDataTypeId, new DmsDocumentBean());
            ret.add(folderBean.vfsResource());
         }
      }

      return ret;
   }

   public static List<Map> fromXto(FoldersXto folders, Model model, String metaDataTypeId)
   {
      List<Map> ret = null;

      if (folders != null)
      {
         ret = CollectionUtils.newArrayList();

         for (FolderXto folder : folders.getFolder())
         {
            DmsFolderBean folderBean = (DmsFolderBean) fromXto(folder, model, metaDataTypeId, new DmsFolderBean());
            ret.add(folderBean.vfsResource());
         }
      }

      return ret;
   }

   private static DocumentInfoXto marshalDocumentInfoXto(DocumentInfo doc,
         DocumentInfoXto xto)
   {
      marshalResourceInfoXto(doc, xto);

      xto.setDocumentType(marshalDocumentType(doc.getDocumentType()));
      xto.setContentType(doc.getContentType());

      return xto;
   }

   public static DocumentInfo fromXto(DocumentInfoXto xto, Set<TypedXPath> metaDataXPaths)
   {
      DocumentInfo result = null;

      if (xto != null)
      {
         result = DmsUtils.createDocumentInfo(xto.getName());

         unmarshalDocumentInfoXto(xto, result);

         unmarshalDmsMetaData(xto, result, metaDataXPaths, unmarshalDocumentType(xto.getDocumentType()));
      }

      return result;
   }

   private static DocumentInfo unmarshalDocumentInfoXto(DocumentInfoXto xto,
         DocumentInfo doc)
   {
      unmarshalResourceInfoXto(xto, doc);

      if ( !isEmpty(xto.getContentType()))
      {
         doc.setContentType(xto.getContentType());
      }
      if (xto.getDocumentType() != null)
      {
         doc.setDocumentType(unmarshalDocumentType(xto.getDocumentType()));
      }

      return doc;
   }

   public static FolderInfoXto marshalFolderInfoXto(FolderInfo doc, FolderInfoXto xto)
   {
      marshalResourceInfoXto(doc, xto);

      return xto;
   }

   public static FolderInfo unmarshalFolderInfo(FolderInfoXto xto)
   {
      FolderInfo folderInfo = unmarshalFolderInfoXto(xto, new DmsFolderBean());
      
      unmarshalDmsMetaData(xto, folderInfo, null,(DocumentType)null);
      
      return folderInfo;
   }

   private static FolderInfo unmarshalFolderInfoXto(FolderInfoXto xto, FolderInfo folder)
   {
      unmarshalResourceInfoXto(xto, folder);

      return folder;
   }

   private static ResourceInfoXto marshalResourceInfoXto(ResourceInfo doc,
         ResourceInfoXto xto)
   {
      xto.setName(doc.getName());
      xto.setDescription(doc.getDescription());
      xto.setOwner(doc.getOwner());

      xto.setDateCreated(doc.getDateCreated());
      xto.setDateLastModified(doc.getDateLastModified());

      return xto;
   }

   private static ResourceInfo unmarshalResourceInfoXto(ResourceInfoXto xto,
         ResourceInfo res)
   {
      if ( !isEmpty(xto.getName()))
      {
         res.setName(xto.getName());
      }
      if ( !isEmpty(xto.getDescription()))
      {
         res.setDescription(xto.getDescription());
      }
      if ( !isEmpty(xto.getOwner()))
      {
         res.setOwner(xto.getOwner());
      }

      if (res instanceof DmsResourceBean)
      {
         // TODO review
         @SuppressWarnings("unchecked")
         Map<String, Object> internals = ((DmsResourceBean) res).vfsResource();

         if (null != xto.getDateCreated())
         {
            internals.put(AuditTrailUtils.RES_DATE_CREATED, xto.getDateCreated());
         }
         if (null != xto.getDateLastModified())
         {
            internals.put(AuditTrailUtils.RES_DATE_LAST_MODIFIED,
                  xto.getDateLastModified());
         }
      }

      return res;
   }

   /**
    * Document used in workflow, metaDataTypeId and model are known. Mainly used by
    * WorkflowService calls.
    * @param documentType Specifying documentType leads to a dms call in {@link WebServiceEnv}.
    */
   public static ResourceInfoXto marshalDmsMetaData(ResourceInfo doc,
         ResourceInfoXto xto, Model model, String metaDataTypeId, DocumentType documentType)
   {
      if (isEmpty(doc.getProperties()))
      {
         return xto;
      }

      if ((null != model) && !isEmpty(metaDataTypeId))
      {
         xto.setMetaDataType(getStructuredTypeName(model, metaDataTypeId));
         xto.setMetaData(marshalStructValue(model, metaDataTypeId, null,
               (Serializable) doc.getProperties()));
      }
      else
      {
         if (documentType != null && !isEmpty(documentType.getSchemaLocation()))
         {
            xto.setMetaDataType(QName.valueOf(documentType.getDocumentTypeId()));

            Set<TypedXPath> docTypeXPaths = retrieveXPathsFromDms(documentType);
            xto.setMetaData(marshalStructValue(new ClientXPathMap(docTypeXPaths), null,
                  (Serializable) doc.getProperties()));
         }
         else
         {
            trace.debug("Marshal properties based on default DMS meta data type.");
            marshalDmsMetaDataDefaultType(xto, doc.getProperties());
         }
      }

      return xto;
   }

   /**
    * Document not used in workflow, no model or data information, XPaths and QName have
    * to be supplied. Mainly used by DocumentManagementService calls.
    * @param documentType Specifying documentType leads to a dms call in {@link WebServiceEnv}.
    */
   public static ResourceInfoXto marshalDmsMetaData(ResourceInfo doc,
         ResourceInfoXto xto, QName typeId, Set<TypedXPath> xPaths,
         DocumentType documentType)
   {
      if (isEmpty(doc.getProperties()))
      {
         return xto;
      }

      if ( !isEmpty(xPaths))
      {
         xto.setMetaDataType(typeId);
         xto.setMetaData(marshalStructValue(new ClientXPathMap(xPaths), null,
               (Serializable) doc.getProperties()));
      }
      else
      {
         if (documentType != null && !isEmpty(documentType.getSchemaLocation()))
         {
            xto.setMetaDataType(QName.valueOf(documentType.getDocumentTypeId()));

            Set<TypedXPath> docTypeXPaths = retrieveXPathsFromDms(documentType);
            xto.setMetaData(marshalStructValue(new ClientXPathMap(docTypeXPaths), null,
                  (Serializable) doc.getProperties()));
         }
         else
         {
            trace.debug("Marshal properties based on default DMS meta data type.");
            marshalDmsMetaDataDefaultType(xto, doc.getProperties());
         }

      }

      return xto;
   }

   private static ResourceInfoXto marshalDmsMetaDataDefaultType(
         final ResourceInfoXto xto, final Map<String, Serializable> metaData)
   {
      final XSDSchema schema = DmsSchemaProvider.loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);

      final String namespace = schema.getTargetNamespace();
      xto.setMetaDataType(new QName(namespace,
            DmsSchemaProvider.RESOURCE_PROPERTIES_COMPLEX_TYPE_NAME));

      @SuppressWarnings("unchecked")
      final Set<TypedXPath> xPaths = XPathFinder.findAllXPaths(schema,
            DmsSchemaProvider.RESOURCE_PROPERTIES_COMPLEX_TYPE_NAME, false);
      final Map<String, List<Map<String, Serializable>>> schemaAwareMetaData = createSchemaAwareMetaData(metaData);
      xto.setMetaData(marshalStructValue(new ClientXPathMap(xPaths), null,
            (Serializable) schemaAwareMetaData));

      return xto;
   }

   private static Map<String, List<Map<String, Serializable>>> createSchemaAwareMetaData(
         final Map<String, Serializable> metaData)
   {
      final List<Map<String, Serializable>> propertyList = newArrayList();
      for (final Entry<String, Serializable> entry : metaData.entrySet())
      {
         if (entry.getValue() != null)
         {
            try
            {
               int typeKey = TypeKey.getTypeKeyFor(entry.getValue().getClass());
               final Map<String, Serializable> property = newHashMap();
               property.put(PROPERTY_NAME_LITERAL, entry.getKey());

               if (entry.getValue() instanceof java.util.Date)
               {
                  property.put(PROPERTY_STRING_VALUE_LITERAL,
                        printDateTime((Date) entry.getValue()));
               }
               else
               {
                  property.put(PROPERTY_STRING_VALUE_LITERAL, entry.getValue().toString());
               }

               property.put(PROPERTY_TYPE_KEY_LITERAL, typeKey);
               propertyList.add(property);
            }
            catch (IllegalArgumentException e)
            {
               trace.warn("Ignoring default meta data entry '" + entry.getKey()
                     + "'. Marshaling of type not supported: "
                     + entry.getValue().getClass());
            }
         }
      }

      final Map<String, List<Map<String, Serializable>>> schemaAwareMetaData = newHashMap();
      schemaAwareMetaData.put(DmsSchemaProvider.PROPERTIES_ELEMENT_NAME, propertyList);
      return schemaAwareMetaData;
   }

   public static ResourceInfo unmarshalDmsMetaData(ResourceInfoXto xto, ResourceInfo res,
         Model model, String metaDataTypeId)
   {
      if (xto.getMetaData() == null || xto.getMetaData().getAny() == null)
      {
         return res;
      }

      if ((null != model) && !isEmpty(metaDataTypeId))
      {
         // TODO verify meta data type passed in?

         Serializable metaData = unmarshalStructValue(model, metaDataTypeId, null,
               xto.getMetaData().getAny());
         if ((metaData instanceof Map) && !isEmpty((Map< ? , ? >) metaData))
         {
            res.setProperties((Map< ? , ? >) metaData);
         }
      }
      else
      {
         trace.debug("Unmarshal properties based on default DMS meta data type.");
         unmarshalDmsMetaDataDefaultType(xto.getMetaData(), res);
      }

      return res;
   }

   public static ResourceInfo unmarshalDmsMetaData(ResourceInfoXto xto, ResourceInfo res,
         Set<TypedXPath> xPaths, DocumentType documentType)
   {
      if (xto.getMetaData() == null || xto.getMetaData().getAny() == null)
      {
         return res;
      }

      Serializable metaData = null;
      if ( !isEmpty(xPaths))
      {
         // TODO verify meta data type passed in?

         metaData = unmarshalStructValue(xPaths, null, xto.getMetaData().getAny());
      }
      else
      {
         if (documentType != null && !isEmpty(documentType.getSchemaLocation()))
         {
            Set<TypedXPath> docTypeXPaths = retrieveXPathsFromDms(documentType);
            metaData = unmarshalStructValue(docTypeXPaths, null, xto.getMetaData()
                  .getAny());
         }
         else
         {

            trace.debug("Unmarshal properties based on default DMS meta data type.");
            unmarshalDmsMetaDataDefaultType(xto.getMetaData(), res);
         }
      }
      if ((metaData instanceof Map) && !isEmpty((Map< ? , ? >) metaData))
      {
         res.setProperties((Map< ? , ? >) metaData);
      }

      return res;
   }

   private static ResourceInfo unmarshalDmsMetaDataDefaultType(final XmlValueXto xto,
         final ResourceInfo resource)
   {
      final XSDSchema schema = DmsSchemaProvider.loadExternalSchema(DmsConstants.MONTAUK_SCHEMA_XSD);

      @SuppressWarnings("unchecked")
      final Set<TypedXPath> xPaths = XPathFinder.findAllXPaths(schema,
            DmsSchemaProvider.RESOURCE_PROPERTIES_COMPLEX_TYPE_NAME, false);
      final Serializable metaData = unmarshalStructValue(xPaths, null, xto.getAny());
      if ((metaData instanceof Map) && !isEmpty((Map< ? , ? >) metaData))
      {
         final Map<String, Serializable> schemaUnawareMetaData = createSchemaUnawareMetaData((Map<String, List<Map<String, Serializable>>>) metaData);
         resource.setProperties(schemaUnawareMetaData);
      }

      return resource;
   }

   private static Map<String, Serializable> createSchemaUnawareMetaData(
         final Map<String, List<Map<String, Serializable>>> schemaAwareMetaData)
   {
      final Map<String, Serializable> schemaUnawareMetaData = newHashMap();
      for (final Map<String, Serializable> property : schemaAwareMetaData.get(DmsSchemaProvider.PROPERTIES_ELEMENT_NAME))
      {
         final String key = (String) property.get(PROPERTY_NAME_LITERAL);
         final Serializable value = getSerializableFrom(
               (String) property.get(PROPERTY_STRING_VALUE_LITERAL),
               (Integer) property.get(PROPERTY_TYPE_KEY_LITERAL));
         schemaUnawareMetaData.put(key, value);
      }
      return schemaUnawareMetaData;
   }

   private static Serializable getSerializableFrom(final String stringValue,
         final int typeKey)
   {
      Serializable value;

      if (TypeKey.STRING.getTypeKey() == typeKey)
      {
         value = stringValue;
      }
      else if (TypeKey.SHORT.getTypeKey() == typeKey)
      {
         value = Short.valueOf(stringValue);
      }
      else if (TypeKey.INTEGER.getTypeKey() == typeKey)
      {
         value = Integer.valueOf(stringValue);
      }
      else if (TypeKey.LONG.getTypeKey() == typeKey)
      {
         value = Long.valueOf(stringValue);
      }
      else if (TypeKey.FLOAT.getTypeKey() == typeKey)
      {
         value = Float.valueOf(stringValue);
      }
      else if (TypeKey.DOUBLE.getTypeKey() == typeKey)
      {
         value = Double.valueOf(stringValue);
      }
      else if (TypeKey.BYTE.getTypeKey() == typeKey)
      {
         value = Byte.valueOf(stringValue);
      }
      else if (TypeKey.CHARACTER.getTypeKey() == typeKey)
      {
         value = stringValue.charAt(0);
      }
      else if (TypeKey.BOOLEAN.getTypeKey() == typeKey)
      {
         value = Boolean.valueOf(stringValue);
      }
      else if (TypeKey.DATE.getTypeKey() == typeKey)
      {
         value = parseDateTime(stringValue);
      }
      else
      {
         throw new IllegalArgumentException(
               "Only primitive types and strings are supported as values.");
      }

      return value;
   }

   @SuppressWarnings("unchecked")
   public static Set<TypedXPath> inferStructDefinition(QName typeName, Model model)
   {
      Set<TypedXPath> xPaths = null;

      if (model != null)
      {
         for (TypeDeclaration type : (List<TypeDeclaration>) model.getAllTypeDeclarations())
         {
            XSDSchema schema = null;
            if (type.getXpdlType() instanceof SchemaType)
            {
               SchemaType schemaType = (SchemaType) type.getXpdlType();

               schema = schemaType.getSchema();
            }
            else if (type.getXpdlType() instanceof ExternalReference)
            {
               ExternalReference refType = (ExternalReference) type.getXpdlType();

               if (typeName.toString().equals(refType.getXref()))
               {
                  schema = refType.getSchema(model);
               }
            }

            xPaths = getXPathsFromSchema(typeName, schema);
            if ( !isEmpty(xPaths))
            {
               break;
            }
         }
      }
      return xPaths;
   }

   public static Set<TypedXPath> retrieveXPathsFromDms(DocumentType documentType)
   {
      WebServiceEnv currentWebServiceEnvironment = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = currentWebServiceEnvironment.getServiceFactory()
            .getDocumentManagementService();

      Set<TypedXPath> xPaths = Collections.emptySet();

      try
      {
         byte[] schemaBytes = dms.getSchemaDefinition(documentType.getSchemaLocation());
         if (schemaBytes != null)
         {

            XSDParser parser = new XSDParser(null);
            parser.parse(new InputSource(new ByteArrayInputStream(schemaBytes)));
            XSDSchema schema = parser.getSchema();

            xPaths = getXPathsFromSchema(QName.valueOf(documentType.getDocumentTypeId()),
                  schema);

         }
      }
      catch (Throwable t)
      {
         trace.warn("Schema lookup for documentType failed: "
               + documentType.getDocumentTypeId() + " "
               + documentType.getSchemaLocation() + " Cause: " + t.getMessage());
      }
      return xPaths;
   }

   private static Set<TypedXPath> getXPathsFromSchema(QName typeName, XSDSchema schema)
   {
      Set<TypedXPath> xPaths = CollectionUtils.newSet();
      if (null != schema)
      {
         XSDNamedComponent metaDataType = XPathFinder.findTypeDefinition(schema,
               typeName.toString());

         if (null == metaDataType)
         {
            XSDElementDeclaration element = XPathFinder.findElement(schema,
                  typeName.toString());
            if ((null != element) && (null != element.getAnonymousTypeDefinition()))
            {
               // matching element name defining an anonymous type
               metaDataType = element;
            }
         }

         if (null != metaDataType)
         {
            xPaths = XPathFinder.findAllXPaths(schema, metaDataType);
         }
      }
      return xPaths;
   }

   public static int umarshalFolderLOD(FolderLevelOfDetailXto folderLOD)
   {
      if (folderLOD != null)
      {
         switch (folderLOD)

         {
         case NO_MEMBERS:
            return Folder.LOD_NO_MEMBERS;
         case DIRECT_MEMBERS:
            return Folder.LOD_LIST_MEMBERS;
         case MEMBERS_OF_MEMBERS:
            return Folder.LOD_LIST_MEMBERS_OF_MEMBERS;
         }

      }
      return Folder.LOD_LIST_MEMBERS;
   }

   public static DaemonsXto marshalDaemons(List<Daemon> allDaemons)
   {
      DaemonsXto xto = new DaemonsXto();

      for (Daemon daemon : allDaemons)
      {
         xto.getDaemon().add(marshalDaemon(daemon));
      }

      return xto;
   }

   private static DaemonXto marshalDaemon(Daemon daemon)
   {
      DaemonXto xto = new DaemonXto();

      xto.setType(daemon.getType());
      xto.setStartTime(daemon.getStartTime());
      xto.setLastExecutionTime(daemon.getLastExecutionTime());
      xto.setRunning(daemon.isRunning());

      xto.setAcknowledgementState(marshalAcknowledgementState(daemon.getAcknowledgementState()));
      xto.setDaemonExecutionState(marshalDaemonExecutionState(daemon.getDaemonExecutionState()));
      return xto;
   }

   private static AcknowledgementStateXto marshalAcknowledgementState(
         AcknowledgementState acknowledgementState)
   {
      if (acknowledgementState == null)
      {
         return null;
      }
      else if (AcknowledgementState.RespondedOK.equals(acknowledgementState))
      {
         return AcknowledgementStateXto.OK;
      }
      else if (AcknowledgementState.Requested.equals(acknowledgementState))
      {
         return AcknowledgementStateXto.RESPONSE_REQUESTED;
      }
      else if (AcknowledgementState.RespondedFailure.equals(acknowledgementState))
      {
         return AcknowledgementStateXto.FAILURE;
      }
      throw new UnsupportedOperationException("Marshaling not implemented for state: "
            + acknowledgementState.getName());

   }

   private static DaemonExecutionStateXto marshalDaemonExecutionState(
         DaemonExecutionState daemonExecutionState)
   {
      if (daemonExecutionState == null)
      {
         return null;
      }
      else if (DaemonExecutionState.OK.equals(daemonExecutionState))
      {
         return DaemonExecutionStateXto.OK;
      }
      else if (DaemonExecutionState.Warning.equals(daemonExecutionState))
      {
         return DaemonExecutionStateXto.WARNING;
      }
      else if (DaemonExecutionState.Fatal.equals(daemonExecutionState))
      {
         return DaemonExecutionStateXto.FATAL;
      }

      throw new UnsupportedOperationException("Marshaling not implemented for state: "
            + daemonExecutionState.getName());
   }

   public static AcknowledgementState unmarshalAcknowledgementState(
         AcknowledgementStateXto acknowledgementState)
   {
      if (acknowledgementState == null)
      {
         return null;
      }
      else if (AcknowledgementStateXto.OK.equals(acknowledgementState))
      {
         return AcknowledgementState.RespondedOK;
      }
      else if (AcknowledgementStateXto.RESPONSE_REQUESTED.equals(acknowledgementState))
      {
         return AcknowledgementState.Requested;
      }
      else if (AcknowledgementStateXto.FAILURE.equals(acknowledgementState))
      {
         return AcknowledgementState.RespondedFailure;
      }
      throw new UnsupportedOperationException("Unmarshaling not implemented for state: "
            + acknowledgementState.name());

   }

   public static DaemonExecutionState unmarshalDaemonExecutionState(
         DaemonExecutionStateXto daemonExecutionState)
   {
      if (daemonExecutionState == null)
      {
         return null;
      }
      else if (DaemonExecutionStateXto.OK.equals(daemonExecutionState))
      {
         return DaemonExecutionState.OK;
      }
      else if (DaemonExecutionStateXto.WARNING.equals(daemonExecutionState))
      {
         return DaemonExecutionState.Warning;
      }
      else if (DaemonExecutionStateXto.FATAL.equals(daemonExecutionState))
      {
         return DaemonExecutionState.Fatal;
      }

      throw new UnsupportedOperationException("Unmarshaling not implemented for state: "
            + daemonExecutionState.name());
   }

   public static XmlValueXto marshalModelAsXML(String xmlString)
   {
      XmlValueXto ret = new XmlValueXto();

      // TODO evaluate use of XmlUtils.parseString()
      ret.getAny().add(XmlUtils.parseString(xmlString).getDocumentElement());

      return ret;
   }

   public static String unmarshalModelAsXML(XmlValueXto modelAsXml)
   {
      String ret = "";
      if (modelAsXml.getAny() != null && !modelAsXml.getAny().isEmpty())
      {
         Element modelElement = modelAsXml.getAny().get(0);
         if (modelElement != null)
         {
            ret = XmlUtils.toString(fixModelSchema(modelElement.getOwnerDocument()));
         }
      }
      return ret;
   }

   private static Node fixModelSchema(org.w3c.dom.Document element)
   {
      StaticNamespaceContext xpathContext = new StaticNamespaceContext(
            Collections.singletonMap("xsd", "http://www.w3.org/2001/XMLSchema"));
      NodeList queryResult = DomUtils.retrieveElementsByXPath(element, "//xsd:schema",
            xpathContext);

      for (int i = 0; i < queryResult.getLength(); i++ )
      {
         Node schemaNode = queryResult.item(i);
         if (schemaNode instanceof Element)
         {
            Element schemaElement = (Element) schemaNode;

            // if namespace for schema element is missing, rebuild it.
            if (getNamespaceDeclarationCount(schemaElement) == 1)
            {
               NodeList schemaChildNodes = schemaElement.getChildNodes();
               for (int j = 0; j < schemaChildNodes.getLength(); j++ )
               {
                  Node childNode = schemaChildNodes.item(j);
                  if (childNode instanceof Element)
                  {
                     Element childElement = (Element) childNode;
                     String typeAttributeValue = childElement.getAttribute("type");
                     if ( !StringUtils.isEmpty(typeAttributeValue))
                     {
                        int indexOfSeperator = typeAttributeValue.indexOf(':');
                        if (indexOfSeperator > -1)
                        {
                           String prefix = typeAttributeValue.substring(0,
                                 indexOfSeperator);

                           String targetNamespace = schemaElement.getAttribute("targetNamespace");
                           if ( !StringUtils.isEmpty(targetNamespace))
                              schemaElement.setAttributeNS(
                                    XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:"
                                          + prefix, targetNamespace);
                        }
                     }
                  }
               }
            }
         }
      }

      return element;
   }

   private static int getNamespaceDeclarationCount(Element schemaElement)
   {
      int count = 0;
      NamedNodeMap attributes = schemaElement.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++ )
      {
         Node att = attributes.item(i);

         if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(att.getNamespaceURI()))
         {
            count++ ;
         }

      }
      return count;
   }

   public static DeploymentInfoXto toWs(DeploymentInfo deploymentInfo)
   {
      DeploymentInfoXto xto = new DeploymentInfoXto();

      xto.setId(deploymentInfo.getId());
      xto.setModelOid(deploymentInfo.getModelOID());
      xto.setRevision(deploymentInfo.getRevision());

      WarningsXto warnings = new WarningsXto();
      marshalInconsistencies(deploymentInfo.getWarnings(), warnings.getWarning());
      xto.setWarnings(warnings);

      ErrorsXto errors = new ErrorsXto();
      marshalInconsistencies(deploymentInfo.getErrors(), errors.getError());
      xto.setErrors(errors);

      xto.setComment(deploymentInfo.getDeploymentComment());

      xto.setDeploymentTime(deploymentInfo.getDeploymentTime());
      xto.setValidFrom(deploymentInfo.getValidFrom());

      xto.setSuccess(deploymentInfo.success());

      return xto;
   }

   private static void marshalInconsistencies(List<Inconsistency> incon,
         List<InconsistencyXto> ret)
   {
      for (Inconsistency inconsistency : incon)
      {
         InconsistencyXto xto = new InconsistencyXto();
         xto.setSourceElementOid(inconsistency.getSourceElementOID());
         xto.setMessage(inconsistency.getMessage());
         ret.add(xto);
      }
   }

   private static void unmarshalInconsistencies(List<InconsistencyXto> inconXto,
         List<Inconsistency> incon, int severity)
   {
      for (InconsistencyXto xto : inconXto)
      {
         Inconsistency inc = new Inconsistency(xto.getMessage(),
               xto.getSourceElementOid(), severity);
         incon.add(inc);
      }
   }

   public static PermissionsXto marshalPermissionList(List<Permission> permissions)
   {
      PermissionsXto ret = new PermissionsXto();

      for (Permission permission : permissions)
      {
         ret.getPermission().add(marshalPermission(permission));
      }

      return ret;
   }

   private static PermissionXto marshalPermission(Permission permission)
   {
      PermissionXto ret = new PermissionXto();

      ret.setPermissionId(permission.getPermissionId());

      marshalPermissionScopes(permission.getScopes(), ret.getScopes());

      return ret;
   }

   private static void marshalPermissionScopes(List< ? extends Scope> scopes,
         List<PermissionScopeXto> scopesXto)
   {
      for (Scope scope : scopes)
      {
         scopesXto.add(marshalPermissionScope(scope));
      }
   }

   private static PermissionScopeXto marshalPermissionScope(Scope scope)
   {
      PermissionScopeXto xto = new PermissionScopeXto();
      if (scope instanceof ModelScope)
      {
         xto.setModelOid(((ModelScope) scope).getModelOid());
         xto.setScopeType(PermissionScopeTypeXto.MODEL);
      }
      else if (scope instanceof ProcessScope)
      {
         xto.setId(((ProcessScope) scope).getProcessId());
         xto.setScopeType(PermissionScopeTypeXto.PROCESS);
         xto.setParentScope(marshalPermissionScope(scope.getParent()));
      }
      else if (scope instanceof ActivityScope)
      {
         xto.setId(((ActivityScope) scope).getActivityId());
         xto.setScopeType(PermissionScopeTypeXto.ACTIVITY);
         xto.setParentScope(marshalPermissionScope(scope.getParent()));
      }
      else
      {
         throw new UnsupportedOperationException(
               "Cannot marshal Permission scope. Unhandled Scope type: "
                     + scope.getClass());
      }
      return xto;
   }

   public static List<Permission> unmarshalPermissions(PermissionsXto perm)
   {
      List<Permission> ret = newArrayList();
      if (perm != null)
      {
         for (PermissionXto permission : perm.getPermission())
         {
            ret.add(unmarshalPermission(permission));
         }
      }
      return ret;
   }

   private static Permission unmarshalPermission(PermissionXto permission)
   {
      return new Permission(permission.getPermissionId(),
            unmarshalPermissionScopes(permission.getScopes()));
   }

   private static List<Scope> unmarshalPermissionScopes(List<PermissionScopeXto> scopes)
   {
      List<Scope> ret = newArrayList();
      for (PermissionScopeXto permissionScopeXto : scopes)
      {
         ret.add(unmarshalPermissionScope(permissionScopeXto));
      }
      return ret;
   }

   private static Scope unmarshalPermissionScope(PermissionScopeXto pScope)
   {
      if (PermissionScopeTypeXto.MODEL.equals(pScope.getScopeType()))
      {
         return new ModelScope(pScope.getModelOid());
      }
      else if (PermissionScopeTypeXto.PROCESS.equals(pScope.getScopeType()))
      {
         return new ProcessScope(
               (ModelScope) unmarshalPermissionScope(pScope.getParentScope()),
               pScope.getId());
      }
      else if (PermissionScopeTypeXto.ACTIVITY.equals(pScope.getScopeType()))
      {
         return new ActivityScope(
               (ProcessScope) unmarshalPermissionScope(pScope.getParentScope()),
               pScope.getId());
      }
      else
      {
         throw new UnsupportedOperationException(
               "Cannot unmarshal Scope. Mapping not implemented for: "
                     + pScope.getScopeType());
      }
   }

   public static List<Document> unmarshalInputDocuments(InputDocumentsXto attachments,
         ServiceFactory sf, Model model, ProcessInstance pi) throws BpmFault
   {
      List<Document> theAttachments = emptyList();

      if ((null != attachments) && !isEmpty(attachments.getInputDocument()))
      {
         theAttachments = newArrayList();

         for (InputDocumentXto attachmentXto : attachments.getInputDocument())
         {
            Document doc = unmarshalInputDocument(attachmentXto, sf, model, pi);

            theAttachments.add(doc);
         }
      }

      return theAttachments;
   }

   public static Document unmarshalInputDocument(InputDocumentXto attachmentXto,
         ServiceFactory sf, Model model, ProcessInstance pi) throws BpmFault
   {
      if (isEmpty(attachmentXto.getTargetFolder()))
      {
         assert (null != pi);

         // use PI-OID based folder
         StringBuilder defaultPath = new StringBuilder(DmsUtils.composeDefaultPath(
               pi.getScopeProcessInstanceOID(), pi.getStartTime())).append("/");

         String dataId = attachmentXto.getGlobalVariableId();
         if (isEmpty(dataId))
         {
            defaultPath.append(DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER);
         }
         else
         {
            defaultPath.append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER);
         }

         attachmentXto.setTargetFolder(defaultPath.toString());
      }

      ensureFolderExists(sf.getDocumentManagementService(),
            attachmentXto.getTargetFolder());

      Document doc = storeDocumentIntoDms(sf.getDocumentManagementService(), model,
            attachmentXto);
      return doc;
   }

   public static void checkProcessAttachmentSupport(String processId, Model model)
         throws BpmFault
   {
      ProcessDefinition processDefinition = model.getProcessDefinition(processId);
      if (processDefinition == null)
      {
         BpmFaultXto faultInfo = new BpmFaultXto();
         faultInfo.setFaultCode(BpmFaultCodeXto.OBJECT_NOT_FOUND_EXCEPTION);

         throw new BpmFault("The process with ID '" + processId
               + "' was not found in the model.", faultInfo);
      }
      DataPath attachmentsDefinition = processDefinition.getDataPath("PROCESS_ATTACHMENTS");
      if ((null == attachmentsDefinition)
            || !attachmentsDefinition.getDirection().isCompatibleWith(Direction.IN))
      {
         BpmFaultXto faultInfo = new BpmFaultXto();
         faultInfo.setFaultCode(BpmFaultCodeXto.INVALID_CONFIGURATION);

         throw new BpmFault("The process with ID '" + processId
               + "' does not support attachments.", faultInfo);
      }
   }

   public static Map<String, Object> unmarshalAttributes(AttributesXto attributes)
   {
      Map<String, Object> ret = new HashMap<String, Object>();
      if (attributes != null)
      {
         for (AttributeXto xto : attributes.getAttribute())
         {
            ret.put(xto.getName(),
                  Reflect.convertStringToObject(xto.getType(), xto.getValue()));
         }
      }
      return ret;
   }

   public static AttributesXto marshalAttributes(Map< ? , ? > attributes)
   {
      // FIXME attributes
      // DefaultXMLWriter //588
      // DefaultXMLReader // 1592
      // name value type
      AttributesXto xto = null;
      if (attributes != null && !attributes.isEmpty())
      {
         xto = new AttributesXto();
         for (Object obj : attributes.entrySet())
         {
            AttributeXto at = new AttributeXto();
            @SuppressWarnings("unchecked")
            Entry<String, Object> entry = (Entry<String, Object>) obj;

            at.setName(entry.getKey());
            Object val = entry.getValue();
            if (null != val)
            {
               at.setType(Reflect.getAbbreviatedName(val.getClass()));
               at.setValue(Reflect.convertObjectToString(val));
            }
            xto.getAttribute().add(at);
         }
      }
      return xto;
   }

   public static AuditTrailHealthReportXto toWs(AuditTrailHealthReport report)
   {
      AuditTrailHealthReportXto xto = new AuditTrailHealthReportXto();

      xto.setNumberOfActivityInstancesLackingAbortion(report.getNumberOfActivityInstancesLackingAbortion());
      xto.setNumberOfProcessInstancesHavingCrashedActivities(report.getNumberOfProcessInstancesHavingCrashedActivities());
      xto.setNumberOfProcessInstancesHavingCrashedThreads(report.getNumberOfProcessInstancesHavingCrashedThreads());
      xto.setNumberOfProcessInstancesHavingCrashedEventBindings(report.getNumberOfProcessInstancesHavingCrashedEventBindings());
      xto.setNumberOfProcessInstancesLackingAbortion(report.getNumberOfProcessInstancesLackingAbortion());
      xto.setNumberOfProcessInstancesLackingCompletion(report.getNumberOfProcessInstancesLackingCompletion());
      return xto;
   }

   public static void handleBPMException(ApplicationException e) throws BpmFault
   {
      BpmFaultXto xto = new BpmFaultXto();
      ErrorCase errorCase = e.getError();

      BpmFaultCodeXto marshaledBpmFaultCode = marshalBpmFaultCode(e);
      xto.setFaultCode(marshaledBpmFaultCode);

      if (errorCase instanceof BpmRuntimeError)
      {
         BpmRuntimeError err = (BpmRuntimeError) errorCase;

         xto.setFaultId(err.getId());
         xto.setFaultDescription(MessageFormat.format(err.getDefaultMessage(),
               err.getMessageArgs()));

      }
      else if (errorCase instanceof ConfigurationError)
      {
         ConfigurationError err = (ConfigurationError) errorCase;

         xto.setFaultId(err.getId());
         xto.setFaultDescription(err.getDefaultMessage());
      }
      throw new BpmFault(e.getMessage(), xto);
   }

   private static BpmFaultCodeXto marshalBpmFaultCode(ApplicationException e)
   {
      try
      {
         return BpmFaultCodeXto.fromValue(e.getClass().getSimpleName());
      }
      catch (IllegalArgumentException ex)
      {
         throw new UnsupportedOperationException(
               "Marshaling of BpmFaultCode not supported for " + e.getClass(), ex);
      }
   }

   /**
    * Replaces Constants used in WebServiceAPI with values available on the JavaAPI
    *
    * @param participant
    *           the participantInfo that can contain WsConstants
    * @param wfs
    *           a WorkflowService
    * @return the participantInfo with replaced constants
    */
   public static ParticipantInfoXto handleWsConstants(ParticipantInfoXto participant,
         WorkflowService wfs)
   {
      if (participant instanceof UserInfoXto)
      {
         if (((UserInfoXto) participant).getOid() == CURRENT_USER_OID)
         {
            ((UserInfoXto) participant).setOid(wfs.getUser().getOID());
         }
      }
      return participant;
   }

   public static PrivilegesXto marshalPrivilegeList(Set<Privilege> privileges)
   {
      PrivilegesXto ret = null;
      if (privileges != null)
      {
         ret = new PrivilegesXto();

         for (Privilege priv : privileges)
         {
            ret.getPrivilege().add(marshalPrivilege(priv));
         }
      }
      return ret;
   }

   public static PrivilegeXto marshalPrivilege(final Privilege privilege)
   {
      if (DmsPrivilege.ALL_PRIVILEGES.equals(privilege))
      {
         return PrivilegeXto.ALL;
      }
      else if (DmsPrivilege.CREATE_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.CREATE;
      }
      else if (DmsPrivilege.DELETE_CHILDREN_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.DELETE_CHILDREN;
      }
      else if (DmsPrivilege.DELETE_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.DELETE;
      }
      else if (DmsPrivilege.MODIFY_ACL_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.MODIFY_ACL;
      }
      else if (DmsPrivilege.MODIFY_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.MODIFY;
      }
      else if (DmsPrivilege.READ_ACL_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.READ_ACL;
      }
      else if (DmsPrivilege.READ_PRIVILEGE.equals(privilege))
      {
         return PrivilegeXto.READ;
      }
      else
      {
         throw new UnsupportedOperationException("Unknown Privilege: " + privilege);
      }
   }

   public static Privilege unmarshalPrivilege(final PrivilegeXto xto)
   {
      if (PrivilegeXto.ALL.equals(xto))
      {
         return DmsPrivilege.ALL_PRIVILEGES;
      }
      else if (PrivilegeXto.CREATE.equals(xto))
      {
         return DmsPrivilege.CREATE_PRIVILEGE;
      }
      else if (PrivilegeXto.DELETE.equals(xto))
      {
         return DmsPrivilege.DELETE_PRIVILEGE;
      }
      else if (PrivilegeXto.DELETE_CHILDREN.equals(xto))
      {
         return DmsPrivilege.DELETE_CHILDREN_PRIVILEGE;
      }
      else if (PrivilegeXto.MODIFY.equals(xto))
      {
         return DmsPrivilege.MODIFY_PRIVILEGE;
      }
      else if (PrivilegeXto.MODIFY_ACL.equals(xto))
      {
         return DmsPrivilege.MODIFY_ACL_PRIVILEGE;
      }
      else if (PrivilegeXto.READ.equals(xto))
      {
         return DmsPrivilege.READ_PRIVILEGE;
      }
      else if (PrivilegeXto.READ_ACL.equals(xto))
      {
         return DmsPrivilege.READ_ACL_PRIVILEGE;
      }
      else
      {
         throw new UnsupportedOperationException("Unknown Privilege: " + xto);
      }
   }

   public static AccessControlPoliciesXto marshalAccessControlPolicyList(
         Set<AccessControlPolicy> acps)
   {
      AccessControlPoliciesXto ret = null;

      if (acps != null)
      {
         ret = new AccessControlPoliciesXto();
         for (AccessControlPolicy acp : acps)
         {
            ret.getAccessControlPolicy().add(toWs(acp));
         }
      }
      return ret;
   }

   public static AccessControlPolicyXto toWs(AccessControlPolicy acp)
   {
      AccessControlPolicyXto ret = null;
      if (acp != null)
      {
         ret = new AccessControlPolicyXto();
         ret.setAccessControlEntries(marshalAccessControlEntryList(acp.getAccessControlEntries()));
      }
      return ret;
   }

   private static AccessControlEntriesXto marshalAccessControlEntryList(
         Set<AccessControlEntry> accessControlEntries)
   {
      AccessControlEntriesXto ret = null;
      if (accessControlEntries != null)
      {
         ret = new AccessControlEntriesXto();
         for (AccessControlEntry ace : accessControlEntries)
         {
            ret.getAccessControlEntry().add(toWs(ace));
         }
      }
      return ret;
   }

   private static AccessControlEntryXto toWs(AccessControlEntry ace)
   {
      AccessControlEntryXto ret = null;
      if (ace != null)
      {
         ret = new AccessControlEntryXto();
         ret.setPrincipal(ace.getPrincipal().getName());
         ret.setPrivileges(marshalPrivilegeList(ace.getPrivileges()));
      }
      return ret;
   }

   public static AccessControlPolicy fromWs(AccessControlPolicyXto acp)
   {
      AccessControlPolicy ret = null;

      if (acp != null)
      {
         ret = new DmsAccessControlPolicy(
               unmarshalAccessControlEntries(acp.getAccessControlEntries()), false, false);
      }
      return ret;
   }

   public static Set<AccessControlEntry> unmarshalAccessControlEntries(
         AccessControlEntriesXto accessControlEntries)
   {
      Set<AccessControlEntry> ret = null;
      if (accessControlEntries != null)
      {
         ret = CollectionUtils.newHashSet();
         for (AccessControlEntryXto aceXto : accessControlEntries.getAccessControlEntry())
         {
            ret.add(unmarshalAccessControlEntry(aceXto));
         }
      }

      return ret;

   }

   public static AccessControlEntry unmarshalAccessControlEntry(
         AccessControlEntryXto aceXto)
   {
      AccessControlEntry ret = null;
      if (aceXto != null)
      {
         ret = new DmsAccessControlEntry(new DmsPrincipal(aceXto.getPrincipal()),
               unmarshalPrivileges(aceXto.getPrivileges()));
      }
      return ret;
   }

   public static Set<Privilege> unmarshalPrivileges(PrivilegesXto privileges)
   {
      Set<Privilege> ret = null;

      if (privileges != null)
      {
         ret = CollectionUtils.newHashSet();

         for (final PrivilegeXto privilege : privileges.getPrivilege())
         {
            ret.add(unmarshalPrivilege(privilege));
         }
      }
      return ret;
   }

   public static <T, U> Map<T, U> unmarshalMap(final MapXto xto, Class<T> keyClass,
         Class<U> valueClass)
   {
      if (xto == null)
      {
         return Collections.emptyMap();
      }

      final Map<T, U> map = new HashMap<T, U>();
      for (final ItemXto item : xto.getItem())
      {
         map.put((T) item.getKey(), (U) item.getValue());
      }
      return map;
   }

   private static enum TypeKey {
      INTEGER (0, Integer.class), SHORT (1, Short.class), LONG (2, Long.class), FLOAT (3,
            Float.class), DOUBLE (4, Double.class), CHARACTER (5, Character.class), BYTE (
            6, Byte.class), BOOLEAN (7, Boolean.class), STRING (8, String.class), DATE (
            9, Date.class);

      private final int typeKey;

      private final Class< ? > clazz;

      public final int getTypeKey()
      {
         return typeKey;
      }

      public final Class< ? > getTypeKeyClass()
      {
         return clazz;
      }

      private TypeKey(final int typeKey, final Class< ? > clazz)
      {
         if (clazz == null)
         {
            throw new NullPointerException("Class must not be null");
         }
         this.typeKey = typeKey;
         this.clazz = clazz;
      }

      public static int getTypeKeyFor(final Class< ? > clazz)
      {
         for (final TypeKey t : values())
         {
            if (t.getTypeKeyClass().equals(clazz))
            {
               return t.getTypeKey();
            }
         }

         throw new IllegalArgumentException("Unknown class: " + clazz);
      }
   }

   public static List<SubprocessSpawnInfo> unmarshalProcessSpawnInfos(
         ProcessSpawnInfosXto processSpawnInfos)
   {
      List<SubprocessSpawnInfo> ret = null;
      if (processSpawnInfos != null)
      {
         ret = CollectionUtils.newArrayList();
         List<ProcessSpawnInfoXto> processSpawnInfoList = processSpawnInfos.getProcessSpawnInfo();
         for (ProcessSpawnInfoXto processSpawnInfoXto : processSpawnInfoList)
         {
            if (processSpawnInfoXto != null)
            {
               ret.add(unmarshalProcessSpawnInfo(processSpawnInfoXto));
            }
         }
      }
      return ret;
   }

   private static SubprocessSpawnInfo unmarshalProcessSpawnInfo(ProcessSpawnInfoXto xto)
   {
      String processId = xto.getProcessId();
      Map<String, ? > data = DataFlowUtils.unmarshalInitialDataValues(processId,
            xto.getParameters());
      return new SubprocessSpawnInfo(processId, xto.isCopyData(), data);
   }

   public static ProcessInstancesXto marshalProcessInstanceList(List<ProcessInstance> pis)
   {
      ProcessInstancesXto xto = null;
      if (pis != null)
      {
         xto = new ProcessInstancesXto();

         for (ProcessInstance processInstance : pis)
         {
            if (processInstance != null)
            {
               xto.getProcessInstance().add(toWs(processInstance));
            }
         }
      }
      return xto;
   }

   public static long[] unmarshalOidList(OidListXto memberOids)
   {
      long[] ret = null;
      if (memberOids != null)
      {
         List<Long> oids = memberOids.getOid();
         ret = new long[oids.size()];
         for (int i = 0; i < oids.size(); i++ )
         {
            ret[i] = oids.get(i);
         }
      }
      return ret;
   }

   public static AbortScope unmarshalAbortScope(AbortScopeXto abortScope)
   {
      if (abortScope == null)
      {
         return null;
      }
      switch (abortScope)
      {
      case ROOT_HIERARCHY:
         return AbortScope.RootHierarchy;
      case SUB_HIERARCHY:
         return AbortScope.SubHierarchy;
      }
      return null;
   }

   public static VariableDefinitionQueryResultXto toWs(DataQueryResult dataQueryResult)
   {
      VariableDefinitionQueryResultXto xto = new VariableDefinitionQueryResultXto();

      xto.setTotalCount(mapTotalCount(dataQueryResult));
      xto.setHasMore(dataQueryResult.hasMore());

      VariableDefinitionsXto varDefs = new VariableDefinitionsXto();
      for (Iterator iterator = dataQueryResult.iterator(); iterator.hasNext();)
      {
         Data data = (Data) iterator.next();

         varDefs.getVariableDefinition().add(toWs(data));
      }
      xto.setVariableDefinitions(varDefs);
      return xto;
   }

   public static PreferenceScope unmarshalPreferenceScope(PreferenceScopeXto scope)
   {
      if (scope != null)
      {
         return PreferenceScope.valueOf(scope.name());
      }
      return null;
   }

   public static PreferenceScopeXto marshalPreferenceScope(PreferenceScope scope)
   {
      if (scope != null)
      {
         return PreferenceScopeXto.valueOf(scope.name());
      }
      return null;
   }

   public static PreferencesXto toWs(Preferences preferences)
   {
      PreferencesXto xto = null;

      if (preferences != null)
      {
         xto = new PreferencesXto();

         xto.setModuleId(preferences.getModuleId());
         xto.setPreferencesId(preferences.getPreferencesId());
         xto.setPartitionId(preferences.getPartitionId());
         xto.setRealmId(preferences.getRealmId());
         xto.setUserId(preferences.getUserId());

         xto.setPreferenceScope(marshalPreferenceScope(preferences.getScope()));
         xto.setPreferencesMap(marshalPreferencesMap(preferences.getPreferences()));
      }
      return xto;
   }

   public static Preferences unmarshalPreferences(PreferencesXto preferencesXto)
   {
      Preferences ret = null;

      if (preferencesXto != null)
      {
         ret = new Preferences(
               unmarshalPreferenceScope(preferencesXto.getPreferenceScope()),
               preferencesXto.getModuleId(), preferencesXto.getPreferencesId(),
               unmarshalPreferencesMap(preferencesXto.getPreferencesMap()));
      }

      return ret;
   }

   private static Map<String, Serializable> unmarshalPreferencesMap(
         PreferencesMapXto mapXto)
   {
      Map<String, Serializable> ret = null;

      if (mapXto != null)
      {

         ret = CollectionUtils.newHashMap();

         for (PreferenceEntryXto prefEntryXto : mapXto.getPreference())
         {
            Class clazz = Reflect.getClassFromAbbreviatedName(prefEntryXto.getType());

            if (List.class.isAssignableFrom(clazz))
            {

               ArrayList<Serializable> valueList = CollectionUtils.newArrayList();
               for (ValueListXto listXto : prefEntryXto.getValueList())
               {

                  Serializable value = unmarshalSimplePreferenceValue(listXto.getType(),
                        listXto.getValue());
                  valueList.add(value);
               }

               ret.put(prefEntryXto.getName(), valueList);
            }
            else
            {
               if ( !CollectionUtils.isEmpty(prefEntryXto.getValueList()))
               {
                  ret.put(
                        prefEntryXto.getName(),
                        unmarshalSimplePreferenceValue(prefEntryXto.getType(),
                              prefEntryXto.getValueList().get(0).getValue()));
               }
               else
               {
                  ret.put(prefEntryXto.getName(), null);
               }
            }

         }

      }
      return ret;
   }

   private static Serializable unmarshalSimplePreferenceValue(String type, String value)
   {

      Serializable pValue = value.toString();
      try
      {
         Class typeClass = Reflect.getClassFromAbbreviatedName(type);
         if (Boolean.class.equals(typeClass))
         {
            pValue = Boolean.valueOf(value.toString());
         }
         else if (Double.class.equals(typeClass))
         {
            pValue = Double.valueOf(value.toString());
         }
         else if (Float.class.equals(typeClass))
         {
            pValue = Float.valueOf(value.toString());
         }
         else if (Integer.class.equals(typeClass))
         {
            pValue = Integer.valueOf(value.toString());
         }
         else if (Long.class.equals(typeClass))
         {
            pValue = Long.valueOf(value.toString());
         }
      }
      catch (Exception e)
      {
         pValue = value;
      }
      return pValue;
   }

   private static PreferencesMapXto marshalPreferencesMap(
         Map<String, Serializable> preferences)
   {
      PreferencesMapXto xto = null;

      if (preferences != null)
      {
         xto = new PreferencesMapXto();
         for (Map.Entry<String, Serializable> entry : preferences.entrySet())
         {
            PreferenceEntryXto preferenceXto = new PreferenceEntryXto();
            preferenceXto.setName(entry.getKey());

            Serializable value = entry.getValue();
            if (value != null)
            {
               preferenceXto.setType(Reflect.getAbbreviatedName(value.getClass()));

               if (value instanceof List)
               {
                  List<Serializable> valueList = (List<Serializable>) value;

                  for (Serializable serializable : valueList)
                  {
                     if (serializable != null)
                     {
                        ValueListXto valueListXto = new ValueListXto();

                        valueListXto.setType(Reflect.getAbbreviatedName(serializable.getClass()));
                        valueListXto.setValue(marshalSimplePreferenceValue(serializable));

                        preferenceXto.getValueList().add(valueListXto);
                     }
                  }
               }
               else
               {
                  ValueListXto valueListXto = new ValueListXto();

                  valueListXto.setType(Reflect.getAbbreviatedName(value.getClass()));
                  valueListXto.setValue(marshalSimplePreferenceValue(value));

                  preferenceXto.getValueList().add(valueListXto);
               }

               xto.getPreference().add(preferenceXto);

            }

         }
      }
      return xto;
   }

   private static String marshalSimplePreferenceValue(Serializable serializable)
   {
      return serializable.toString();
   }

   public static PreferencesListXto marshalPreferencesList(
         List<Preferences> preferencesList)
   {
      PreferencesListXto ret = null;
      if (preferencesList != null)
      {
         ret = new PreferencesListXto();

         for (Preferences preferences : preferencesList)
         {
            ret.getPreferenceList().add(toWs(preferences));
         }
      }
      return ret;
   }

   public static List<Preferences> unmarshalPreferenceList(
         PreferencesListXto preferencesListXto)
   {
      List<Preferences> ret = newArrayList();
      if (preferencesListXto != null)
      {

         Iterator<PreferencesXto> prefIter = preferencesListXto.getPreferenceList()
               .iterator();

         while (prefIter.hasNext())
         {
            PreferencesXto prefXto = prefIter.next();
            ret.add(unmarshalPreferences(prefXto));
         }

      }
      return ret;
   }

   public static ConfigurationVariablesListXto marshalConfigurationVariablesList(
         List<ConfigurationVariables> configVariablesList)
   {
      ConfigurationVariablesListXto listXto = null;

      if (configVariablesList != null)
      {
         listXto = new ConfigurationVariablesListXto();

         for (ConfigurationVariables configVariables : configVariablesList)
         {
            listXto.getConfigurationVariables().add(
                  marshalConfigurationVariables(configVariables));

         }
      }

      return listXto;
   }

   public static ConfigurationVariablesXto marshalConfigurationVariables(
         ConfigurationVariables variables)
   {
      ConfigurationVariablesXto variablesXto = null;

      if (variables != null)
      {
         variablesXto = new ConfigurationVariablesXto();

         variablesXto.setModelId(variables.getModelId());

         for (ConfigurationVariable variable : variables.getConfigurationVariables())
         {
            ConfigurationVariableXto variableXto = marshalConfigurationVariable(variable);
            variablesXto.getConfigurationVariable().add(variableXto);
         }

      }

      return variablesXto;
   }

   public static ConfigurationVariableXto marshalConfigurationVariable(
         ConfigurationVariable variable)
   {
      ConfigurationVariableXto variableXto = null;

      if (variable != null)
      {
         variableXto = new ConfigurationVariableXto();

         variableXto.setDefaultValue(variable.getDefaultValue());
         variableXto.setDescription(variable.getDescription());
         variableXto.setModelOid(variable.getModelOid());
         variableXto.setName(variable.getName());
         variableXto.setValue(variable.getValue());

      }
      return variableXto;
   }

   public static List<ConfigurationVariables> unmarshalConfigurationVariablesList(
         ConfigurationVariablesListXto listXto)
   {
      List<ConfigurationVariables> ret = null;

      if (listXto != null)
      {
         ret = CollectionUtils.newList();
         for (ConfigurationVariablesXto variablesXto : listXto.getConfigurationVariables())
         {
            ConfigurationVariables confVariables = unmarshalConfigurationVariables(variablesXto);
            ret.add(confVariables);
         }
      }
      return ret;
   }

   public static ConfigurationVariables unmarshalConfigurationVariables(
         ConfigurationVariablesXto confVariablesXto)
   {
      ConfigurationVariables ret = null;

      if (confVariablesXto != null)
      {

         ret = new ConfigurationVariables(confVariablesXto.getModelId());

         List<ConfigurationVariableXto> configVariablesXto = confVariablesXto.getConfigurationVariable();

         List<ConfigurationVariable> configVariables = CollectionUtils.newList();

         for (ConfigurationVariableXto variableXto : configVariablesXto)
         {
            configVariables.add(unmarshalConfigurationVariable(variableXto));
         }
         ret.setConfigurationVariables(configVariables);

      }

      return ret;
   }

   public static ConfigurationVariable unmarshalConfigurationVariable(
         ConfigurationVariableXto variableXto)
   {
      ConfigurationVariable ret = null;

      if (variableXto != null)
      {
         ConfigurationVariableDefinition variableDefinition = new ConfigurationVariableDefinition(
               variableXto.getName(), variableXto.getDefaultValue(),
               variableXto.getDescription(), variableXto.getModelOid());

         ret = new ConfigurationVariable(variableDefinition, variableXto.getValue());
      }

      return ret;
   }

   public static ModelReconfigurationInfoListXto marshalReconfigurationInfoList(
         List<ModelReconfigurationInfo> modelReconfigInfoList)
   {
      ModelReconfigurationInfoListXto xto = null;

      if (modelReconfigInfoList != null)
      {
         xto = new ModelReconfigurationInfoListXto();

         for (ModelReconfigurationInfo info : modelReconfigInfoList)
         {
            xto.getModelReconfigurationInfo().add(marshalModelReconfigurationInfo(info));
         }

      }
      return xto;
   }

   public static List<ModelReconfigurationInfo> unmarshalReconfigurationInfoList(
         ModelReconfigurationInfoListXto listXto)
   {
      List<ModelReconfigurationInfo> reconfigurationInfo = null;

      if (listXto != null)
      {
         reconfigurationInfo = CollectionUtils.newList();

         for (ModelReconfigurationInfoXto reconfigInfoXto : listXto.getModelReconfigurationInfo())
         {
            reconfigurationInfo.add(unmarshalModelReconfigurationInfo(reconfigInfoXto));
         }
      }

      return reconfigurationInfo;
   }

   public static ModelReconfigurationInfo unmarshalModelReconfigurationInfo(
         ModelReconfigurationInfoXto reconfigInfoXto)
   {
      ModelReconfigurationInfoDetails ret = null;

      if (reconfigInfoXto != null)
      {
         ret = new ModelReconfigurationInfoDetails(reconfigInfoXto.getId());
         ret.setModelOID(reconfigInfoXto.getModelOid());

         List<Inconsistency> errors = CollectionUtils.newList();
         List<Inconsistency> warnings = CollectionUtils.newList();

         unmarshalInconsistencies(reconfigInfoXto.getErrors(), errors,
               Inconsistency.ERROR);
         unmarshalInconsistencies(reconfigInfoXto.getWarnings(), warnings,
               Inconsistency.WARNING);

         ret.addInconsistencies(errors);
         ret.addInconsistencies(warnings);
      }
      return ret;
   }

   public static ModelReconfigurationInfoXto marshalModelReconfigurationInfo(
         ModelReconfigurationInfo reconfigurationInfo)
   {
      ModelReconfigurationInfoXto xto = null;

      if (reconfigurationInfo != null)
      {
         xto = new ModelReconfigurationInfoXto();

         xto.setId(reconfigurationInfo.getId());
         xto.setModelOid(reconfigurationInfo.getModelOID());

         List<InconsistencyXto> errors = CollectionUtils.newList();
         List<InconsistencyXto> warnings = CollectionUtils.newList();

         marshalInconsistencies(reconfigurationInfo.getErrors(), errors);
         marshalInconsistencies(reconfigurationInfo.getWarnings(), warnings);

         xto.getErrors().addAll(errors);
         xto.getWarnings().addAll(warnings);
      }

      return xto;
   }

   public static RuntimePermissionsXto marshalRuntimePermissions(
         RuntimePermissionsDetails permissions)
   {
      RuntimePermissionsXto xto = null;

      if (permissions != null)
      {
         xto = new RuntimePermissionsXto();

         RuntimePermissionsMapXto mapXto = new RuntimePermissionsMapXto();

         Map<String, List<String>> permissionMap = permissions.getPermissionMap();

         Set<String> keys = permissionMap.keySet();

         for (String key : keys)
         {
            RuntimePermissionsEntryXto mapEntry = new RuntimePermissionsEntryXto();

            StringListXto stringListXto = new StringListXto();
            if (permissionMap != null && permissionMap.get(key) != null)
            {
               stringListXto.getValue().addAll(permissionMap.get(key));
            }

            mapEntry.setName(key);
            mapEntry.setValueList(stringListXto);
            mapXto.getRuntimePermissionsEntry().add(mapEntry);
         }

         xto.setRuntimePermissionsMap(mapXto);
      }
      return xto;
   }

   public static RuntimePermissions unmarshalRuntimePermissions(
         RuntimePermissionsXto permissionsXto)
   {
      Map<String, List<String>> globalPermissions = CollectionUtils.newMap();

      for (RuntimePermissionsEntryXto entryXto : permissionsXto.getRuntimePermissionsMap()
            .getRuntimePermissionsEntry())
      {

         globalPermissions.put(entryXto.getName(), entryXto.getValueList().getValue());
      }
      RuntimePermissionsDetails runtimePermissions = DetailsFactory.create(
            globalPermissions, Map.class, RuntimePermissionsDetails.class);

      return runtimePermissions;

   }

   public static ModelsQueryResultXto marshalModelsQueryResult(Models models)
   {
      ModelsQueryResultXto xto = new ModelsQueryResultXto();

      ModelsXto modelsXto = new ModelsXto();

      for (DeployedModelDescription modelDescription : models)
      {
         DeployedModelDescriptionXto modelDescriptionXto = new DeployedModelDescriptionXto();

         modelDescriptionXto.setActive(modelDescription.isActive());

         OidListXto consumerModels = new OidListXto();
         consumerModels.getOid().addAll(modelDescription.getConsumerModels());

         modelDescriptionXto.setConsumerModels(consumerModels);
         modelDescriptionXto.setDeploymentComment(modelDescription.getDeploymentComment());
         modelDescriptionXto.setDeploymentTime(modelDescription.getDeploymentTime());
         modelDescriptionXto.setDescription(modelDescription.getDescription());
         modelDescriptionXto.setId(modelDescription.getId());
         modelDescriptionXto.setModelOid(modelDescription.getModelOID());
         modelDescriptionXto.setName(modelDescription.getName());
         modelDescriptionXto.setPartitionOid(modelDescription.getPartitionOID());
         modelDescriptionXto.setPartitionId(modelDescription.getPartitionId());

         OidListXto providerModels = new OidListXto();
         providerModels.getOid().addAll(modelDescription.getProviderModels());

         modelDescriptionXto.setProvidersModel(providerModels);
         modelDescriptionXto.setRevision(modelDescription.getRevision());
         modelDescriptionXto.setValidFrom(modelDescription.getValidFrom());
         modelDescriptionXto.setVersion(modelDescription.getVersion());

         modelDescriptionXto.setImplementationProcesses(marshalImplementationProcesses(modelDescription.getImplementationProcesses()));

         modelsXto.getDeployedModelDescription().add(modelDescriptionXto);
      }

      xto.setDeployedModels(modelsXto);
      return xto;
   }

   public static ImplementationProcessesMapXto marshalImplementationProcesses(
         Map<String, List<ImplementationDescription>> implementationProcesses)
   {
      ImplementationProcessesMapXto mapXto = new ImplementationProcessesMapXto();

      Set<String> keys = implementationProcesses.keySet();

      for (String key : keys)
      {
         ImplementationProcessesMapEntryXto entryXto = new ImplementationProcessesMapEntryXto();
         entryXto.setName(key);
         entryXto.getImplementationDescriptionList().addAll(
               marshalImplementationDescriptions(implementationProcesses.get(key)));

         mapXto.getImplementationProcessMapEntry().add(entryXto);
      }

      return mapXto;
   }

   public static List<ImplementationDescriptionXto> marshalImplementationDescriptions(
         List<ImplementationDescription> implDesc)
   {
      List<ImplementationDescriptionXto> listXto = CollectionUtils.newList();

      for (ImplementationDescription descr : implDesc)
      {
         ImplementationDescriptionXto implXto = new ImplementationDescriptionXto();

         implXto.setActive(descr.isActive());
         implXto.setImplementationModelOid(descr.getImplementationModelOid());
         implXto.setImplementationProcessId(descr.getImplementationProcessId());
         implXto.setInterfaceModelOid(descr.getInterfaceModelOid());
         implXto.setPrimaryImplementation(descr.isPrimaryImplementation());
         implXto.setProcessInterfaceId(descr.getProcessInterfaceId());

         listXto.add(implXto);
      }

      return listXto;
   }

   public static Map<String, List<ImplementationDescription>> unmarshalImplementationProcesses(
         ImplementationProcessesMapXto mapXto)
   {
      Map<String, List<ImplementationDescription>> implMap = CollectionUtils.newMap();

      for (ImplementationProcessesMapEntryXto entryXto : mapXto.getImplementationProcessMapEntry())
      {
         implMap.put(
               entryXto.getName(),
               unmarshalImplementationDescriptionList(entryXto.getImplementationDescriptionList()));
      }

      return implMap;

   }

   public static List<ImplementationDescription> unmarshalImplementationDescriptionList(
         List<ImplementationDescriptionXto> listXto)
   {
      List<ImplementationDescription> ret = CollectionUtils.newList();

      for (ImplementationDescriptionXto xto : listXto)
      {
         ImplementationDescription desc = new ImplementationDescriptionDetails(
               xto.getProcessInterfaceId(), xto.getImplementationModelOid(),
               xto.getImplementationProcessId(), xto.isPrimaryImplementation(),
               xto.getInterfaceModelOid(), xto.isActive());
         ret.add(desc);
      }

      return ret;
   }

   public static void marshalDocumentTypeResult(DocumentTypeResultsXto results,
         String modelId, Integer modelOid, List<DocumentType> declaredDocumentTypes)
   {
      DocumentTypeResultXto result = new DocumentTypeResultXto();
      result.setModelId(modelId);
      result.setModelOid(modelOid);
      result.setDocumentTypes(marshalDocumentTypes(declaredDocumentTypes));

      results.getDocumentTypeResult().add(result);

   }

   private static DocumentTypesXto marshalDocumentTypes(List<DocumentType> documentTypes)
   {
      DocumentTypesXto xto = null;
      if (documentTypes != null)
      {
         xto = new DocumentTypesXto();
         for (DocumentType documentType : documentTypes)
         {
            xto.getDocumentType().add(marshalDocumentType(documentType));
         }
      }
      return xto;
   }

   public static DocumentTypeXto marshalDocumentType(DocumentType documentType)
   {
      DocumentTypeXto xto = null;
      if (documentType != null)
      {
         xto = new DocumentTypeXto();
         xto.setDocumentTypeId(documentType.getDocumentTypeId());
         xto.setSchemaLocation(documentType.getSchemaLocation());
      }

      return xto;
   }

   public static DocumentType unmarshalDocumentType(DocumentTypeXto xto)
   {
      DocumentType documentType = null;
      if (xto != null)
      {
         documentType = new DocumentType(xto.getDocumentTypeId(), xto.getSchemaLocation());
      }

      return documentType;
   }

}
