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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PeriodicityTypeKey;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.extensions.mail.trigger.MailProtocol;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


// @todo (france, ub): convert old keys to new keys in the attributes
// @todo (france, ub): last minute remove references to workflow packages
/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class V25Reader
{
   private static final Logger trace = LogManager.getLogger(V25Reader.class);
   // Element names

   private static final String ACTIVITY = "ACTIVITY";
   private static final String APPLICATION = "APPLICATION";
   private static final String ASSOCIATED_PARTICIPANT = "ASSOCIATED_PARTICIPANT";
   private static final String CONDITIONAL_PERFORMER = "CONDITIONAL_PERFORMER";
   private static final String DATA = "DATA";
   private static final String DATA_MAPPING = "DATA_MAPPING";
   private static final String DATA_PATH = "DATA_PATH";
   private static final String DESCRIPTOR = "DESCRIPTOR";
   private static final String DESCRIPTION = "DESCRIPTION";
   private static final String DIAGRAM = "DIAGRAM";
   private static final String EXCEPTION_HANDLER = "EXCEPTION_HANDLER";
   private static final String MODEL = "MODEL";
   private static final String MODELER = "MODELER";
   private static final String LINK_TYPE = "LINK_TYPE";
   private static final String NOTIFICATION = "NOTIFICATION";
   private static final String ORGANISATION = "ORGANISATION";
   private static final String PARTICIPANTS = "PARTICIPANTS";
   private static final String PROCESS = "WORKFLOW";
   private static final String ROLE = "ROLE";
   private static final String TEXT = "TEXT";
   private static final String TRANSITION = "TRANSITION";
   private static final String TRIGGER = "TRIGGER";
   private static final String PARAMETER_MAPPING = "PARAMETER_MAPPING";
   private static final String CONTEXT = "CONTEXT";
   private static final String ATTRIBUTE = "ATTRIBUTE";

   private static final String ACTIVITY_SYMBOL = "ACTIVITY_SYMBOL";
   private static final String ANNOTATION_SYMBOL = "ANNOTATION_SYMBOL";
   private static final String APPLICATION_SYMBOL = "APPLICATION_SYMBOL";
   private static final String CONDITIONAL_PERFORMER_SYMBOL = "CONDITIONAL_PERFORMER_SYMBOL";
   private static final String DATA_SYMBOL = "DATA_SYMBOL";
   private static final String GROUP_SYMBOL = "GROUP_SYMBOL";
   private static final String MODELER_SYMBOL = "MODELER_SYMBOL";
   private static final String ORGANISATION_SYMBOL = "ORGANISATION_SYMBOL";
   private static final String PROCESS_SYMBOL = "PROCESS_SYMBOL";
   private static final String ROLE_SYMBOL = "ROLE_SYMBOL";

   private static final String DATA_MAPPING_CONNECTION = "DATA_MAPPING_CONNECTION";
   private static final String EXECUTED_BY_CONNECTION = "EXECUTED_BY_CONNECTION";
   private static final String GENERIC_LINK_CONNECTION = "GENERIC_LINK_CONNECTION";
   private static final String PART_OF_CONNECTION = "PART_OF_CONNECTION";
   private static final String PERFORMS_CONNECTION = "PERFORMS_CONNECTION";
   private static final String REFERS_TO_CONNECTION = "REFERS_TO_CONNECTION";
   private static final String SUBPROCESS_OF_CONNECTION = "SUBPROCESS_OF_CONNECTION";
   private static final String TRANSITION_CONNECTION = "TRANSITION_CONNECTION";
   private static final String WORKS_FOR_CONNECTION = "WORKS_FOR_CONNECTION";
   private static final String VIEW = "VIEW";
   private static final String VIEWABLE = "VIEWABLE";

   // Attributes names

   private static final String ABORT_PROCESS_ATT = "abortProcess";
   private static final String COMPLETE_ACTIVITY_ATT = "completeActivity";
   private static final String ACTIVITY_ATT = "activity";
   private static final String ALLOWS_ABORT_BY_PERFORMER = "allows_abort_by_performer";
   private static final String APPLICATION_REF = "application";
   private static final String APPLICATION_PATH_ATT = "application_path";
   private static final String CARDINALITY = "cardinality";
   private static final String CLASS_ATT = "class";
   private static final String CONDITION = "condition";
   private static final String DATA_PATH_ATT = "data_path";
   private static final String DATA_REF_ATT = "data";
   private static final String DEFAULT_VALUE_ATT = "default_value";
   private static final String EXCEPTION_PATH_ATT = "exception_path";
   private static final String FROM = "from";
   private static final String FORK_ON_TRAVERSAL = "fork_on_traversal";
   private static final String ID_ATT = "id";
   private static final String IMPLEMENTATION = "implementation";
   private static final String IS_USER = "is_user";
   private static final String JOIN = "join";
   private static final String LINE_COLOR = "line_color";
   private static final String LINE_TYPE = "line_type";
   private static final String LOOP_CONDITION = "loop_condition";
   private static final String LOOP_TYPE = "loop_type";
   private static final String MODEL_VERSION = "model_version";
   private static final String NAME_ATT = "name";
   private static final String NOTIFICATION_ACTION_TYPE = "notification_action_type";
   private static final String OID = "oid";
   private static final String PARAMETER = "parameter";
   private static final String PARAMETER_PATH = "parameter_path";
   private static final String PASSWORD = "password";
   private static final String PATH_ATT = "path";
   private static final String PERFORMER = "performer";
   private static final String POINTS = "points";
   private static final String PROCESS_ID_REF = "implementation_process";
   private static final String SUB_PROCESS_MODE = "implementation_process_mode";
   private static final String RECEIVER_REF = "receiver";
   private static final String SHOW_LINKTYPE_NAME = "show_linktype_name";
   private static final String SHOW_ROLE_NAMES = "show_role_names";
   private static final String SOURCE_CLASS = "source_classname";
   private static final String SOURCE_CARDINALITY = "source_cardinality";
   private static final String SOURCE_ROLE = "source_rolename";
   private static final String SOURCE_SYMBOL = "source_symbol";
   private static final String SPLIT = "split";
   private static final String STATUS = "status";
   private static final String VALUE_ATT = "value";
   private static final String TARGET_CLASS = "target_classname";
   private static final String TARGET_CARDINALITY = "target_cardinality";
   private static final String TARGET_ROLE = "target_rolename";
   private static final String TARGET_SYMBOL = "target_symbol";
   private static final String YEAR_TIMEOUT = "year_timeout";
   private static final String MONTH_TIMEOUT = "month_timeout";
   private static final String TIME_OUT = "timeout_in_sec";
   private static final String TO = "to";
   private static final String TRANSITION_REF = "transition";
   private static final String TRIGGERED_PROCESS_DEFINITION_REF = "triggered_process_definition";
   private static final String TYPE_ATT = "type";
   private static final String USEROBJECT = "refer";
   private static final String VALID_FROM_TIME_STAMP = "valid_from_time_stamp";
   private static final String VALID_TO_TIME_STAMP = "valid_to_time_stamp";
   private static final String X = "x";
   private static final String Y = "y";

   private static final String ACTIVITY_SYMBOL_ID = "activity_symbol";
   private static final String APPLICATION_SYMBOL_ID = "application_symbol";
   private static final String DATA_SYMBOL_ID = "data_symbol";
   private static final String LINK_TYPE_REF = "link_type";
   private static final String ORGANISATION_SYMBOL_ID = "organisation_symbol";
   private static final String PARTICIPANT_SYMBOL_ID = "participant_symbol";
   private static final String PROCESS_SYMBOL_ID = "process_symbol";
   private static final String SOURCE_ACTIVITY_SYMBOL_ID = "source_activity_symbol";
   private static final String SOURCE_SYMBOL_ID = "source_symbol";
   private static final String SUB_ORGANISATION_SYMBOL_ID = "suborganisation_symbol";
   private static final String SUB_PROCESS_SYMBOL_ID = "subprocess_symbol";
   private static final String TARGET_ACTIVITY_SYMBOL_ID = "target_activity_symbol";
   private static final String TARGET_SYMBOL_ID = "target_symbol";
   private static final String DIRECTION_ATT = "direction";
   private static final String BROWSABLE_ATT = "browsable";
   private static final String INTERACTIVE_ATT = "interactive";
   private static final String CONTEXT_ATT = "context";
   private static final String USER_OBJECT_VALUE = "user_object_value";

   private static final String APPLICATION_ACCESS_POINT_ATT = "application_access_point";

   // attribute values
   private static final String ACCESS_POINT = "ACCESS_POINT";
   private static final String STATUS_RELEASED = "released";

   // dynamic atts
   private static final String REFERENCE_CLASS_ATTRIBUTE = "className";
   private static final String HOME_INTERFACE_ATTRIBUTE = "homeInterface";
   private static final String IS_PREDEFINED_ATTRIBUTE = "isPredefined";
   private static final String JNDI_PATH_ATTRIBUTE = "jndiPath";
   private static final String PRIMARY_KEY_ATTRIBUTE = "primaryKey";
   private static final String IS_LOCAL_ATTRIBUTE = "isLocal";
   private static final String DEFAULT_VALUE_ATTRIBUTE = "defaultValue";
   private static final String MESSAGE_ACCEPTOR = "messageAcceptor";
   private static final String MESSAGE_TYPE = "messageType";

   private static final String TYPE_ACTIVITY = "ACTIVITY";
   private static final String TYPE_APPLICATION = "APPLICATION";
   private static final String TYPE_DATA = "DATA";
   private static final String TYPE_PARTICIPANT = "PARTICIPANT";
   private static final String TYPE_WORKFLOW = "WORKFLOW";

   private static final String LEGACY_SCOPE = "legacy:";
   private static final String TIMER_TRIGGER_PERIODICITY_FACTOR_ATT = LEGACY_SCOPE + "periodicityFactor";
   private static final String TIMER_TRIGGER_PERIODICITY_TYPE_ATT = LEGACY_SCOPE + "periodicityType";

   // old predefined access points
   public static final String ENGINE_CONTEXT = "engine";
   public static final String EXCLUDED_PERFORMER_ACCESSPOINT = "excludedPerfomer";
   public static final String PERFORMED_BY_ACCESSPOINT = "performedBy";
   public static final String OID_ACCESSPOINT = "instanceOid";
   
   public static final String DIRECTION_IN = "IN";
   public static final String DIRECTION_OUT = "OUT";


   public static final SimpleDateFormat NONINTERACTIVE_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS");

   private static Map attributeNameMap = new HashMap();

   static
   {
      attributeNameMap.put(HOME_INTERFACE_ATTRIBUTE, PredefinedConstants.HOME_INTERFACE_ATT);
      attributeNameMap.put(JNDI_PATH_ATTRIBUTE, PredefinedConstants.JNDI_PATH_ATT);
      attributeNameMap.put(PRIMARY_KEY_ATTRIBUTE, PredefinedConstants.PRIMARY_KEY_ATT);
      attributeNameMap.put(IS_LOCAL_ATTRIBUTE, PredefinedConstants.IS_LOCAL_ATT);
      attributeNameMap.put(REFERENCE_CLASS_ATTRIBUTE, PredefinedConstants.CLASS_NAME_ATT);
      attributeNameMap.put(TYPE_ATT, PredefinedConstants.TYPE_ATT);
      attributeNameMap.put(DEFAULT_VALUE_ATTRIBUTE, PredefinedConstants.DEFAULT_VALUE_ATT);
      attributeNameMap.put("methodName", PredefinedConstants.METHOD_NAME_ATT);
      attributeNameMap.put("createMethodName", PredefinedConstants.CREATE_METHOD_NAME_ATT);
      attributeNameMap.put("htmlPath", PredefinedConstants.HTML_PATH_ATT);
      attributeNameMap.put("participant", PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
      attributeNameMap.put("periodicityType", TIMER_TRIGGER_PERIODICITY_TYPE_ATT);
      attributeNameMap.put("periodicityFactor", TIMER_TRIGGER_PERIODICITY_FACTOR_ATT);
      attributeNameMap.put("startTimestamp", PredefinedConstants.TIMER_TRIGGER_START_TIMESTAMP_ATT);
      attributeNameMap.put("user", PredefinedConstants.MAIL_TRIGGER_USER_ATT);
      attributeNameMap.put("password", PredefinedConstants.MAIL_TRIGGER_PASSWORD_ATT);
      attributeNameMap.put("selectorPredicate", PredefinedConstants.MAIL_TRIGGER_PREDICATE_BODY_ATT);
      attributeNameMap.put("protocol", PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT);
      attributeNameMap.put("host", PredefinedConstants.MAIL_TRIGGER_SERVER_ATT);
      attributeNameMap.put(MESSAGE_TYPE, PredefinedConstants.MESSAGE_TYPE_ATT);
      // no change. This attributes will not be written
      attributeNameMap.put(IS_PREDEFINED_ATTRIBUTE, IS_PREDEFINED_ATTRIBUTE);
      attributeNameMap.put(MESSAGE_ACCEPTOR, PredefinedConstants.MESSAGE_ACCEPTOR_PROPERTY);
      attributeNameMap.put("queueConnectionFactory.jndiName", PredefinedConstants.QUEUE_CONNECTION_FACTORY_NAME_PROPERTY);
      attributeNameMap.put("queue.jndiName", PredefinedConstants.QUEUE_NAME_PROPERTY);
      attributeNameMap.put("messageProvider", PredefinedConstants.MESSAGE_PROVIDER_PROPERTY);
      attributeNameMap.put("jms.location", PredefinedConstants.JMS_LOCATION_PROPERTY);
      attributeNameMap.put("requestMessageType", PredefinedConstants.REQUEST_MESSAGE_TYPE_PROPERTY);
      attributeNameMap.put("responseMessageType",PredefinedConstants.RESPONSE_MESSAGE_TYPE_PROPERTY);
      attributeNameMap.put("includeOidHeaders", PredefinedConstants.INCLUDE_OID_HEADERS_PROPERTY);
   }

   private static final String NEW_EXECUTED_BY_CONNECTION = XMLConstants.EXECUTED_BY_CONNECTION;
   private static final String NEW_PART_OF_CONNECTION = XMLConstants.PART_OF_CONNECTION;
   private static final String NEW_PERFORMS_CONNECTION = XMLConstants.PERFORMS_CONNECTION;
   private static final String NEW_REFERS_TO_CONNECTION = XMLConstants.REFERS_TO_CONNECTION;
   private static final String NEW_SUBPROCESS_OF_CONNECTION = XMLConstants.SUBPROCESS_OF_CONNECTION;
   private static final String NEW_WORKS_FOR_CONNECTION = XMLConstants.WORKS_FOR_CONNECTION;

   private Model model;

   public void addActivity(ProcessDefinition process, Node node)
   {
      int notificationID = 0;
      NodeReader reader = new NodeReader(node);

      Activity activity = process.createActivity(reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), readElementOID(reader, OID));

      activity.setAllowsAbortByPerformer(reader.getBooleanAttribute(ALLOWS_ABORT_BY_PERFORMER, false));
      activity.setApplicationId(reader.getAttribute(APPLICATION_REF));
      activity.setImplementationType(reader.getAttribute(IMPLEMENTATION));
      activity.setJoinType(reader.getAttribute(JOIN));
      activity.setSplitType(reader.getAttribute(SPLIT));
      activity.setLoopCondition(reader.getAttribute(LOOP_CONDITION));
      activity.setLoopType(reader.getAttribute(LOOP_TYPE));
      activity.setPerformerID(reader.getAttribute(PERFORMER));
      activity.setSubProcessID(reader.getAttribute(PROCESS_ID_REF));
      activity.setSubProcessMode(reader.getAttribute(SUB_PROCESS_MODE));

      NodeList childNodes = node.getChildNodes();
      boolean notificationAdded = false;
      for (int i = 0; i < childNodes.getLength(); i++)
      {
         Node childNode = childNodes.item(i);
         if (NOTIFICATION.equals(childNode.getNodeName()))
         {
            addNotification(activity, childNode, node, "timer" + notificationID++);
            notificationAdded = true;
         }
      }
      if (notificationAdded)
      {
         // we assume here that possible interactive exception handlers are already
         // converted together with the notification.
         return;
      }
      for (int i = 0; i < childNodes.getLength(); i++)
      {
         Node childNode = childNodes.item(i);
         if (EXCEPTION_HANDLER.equals(childNode.getNodeName()))
         {
            createExceptionEventHandler(activity, childNode);
         }
      }
   }

   private void createExceptionEventHandler(Activity activity, Node node)
   {
      NodeReader reader = new NodeReader(node);

      EventHandler handler = activity.createEventHandler(
            reader.getAttribute(TYPE_ATT),
            PredefinedConstants.EXCEPTION_CONDITION,
            readElementOID(reader, OID));

      handler.setAttribute(PredefinedConstants.EXCEPTION_CLASS_ATT, reader.getAttribute(TYPE_ATT));

      EventAction action = handler.createEventAction(PredefinedConstants.SET_DATA_ACTION,
            reader.getAttribute(TYPE_ATT), reader.getAttribute(TYPE_ATT));

      action.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, PredefinedConstants.EXCEPTION_ATT);
      action.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, reader.getAttribute(EXCEPTION_PATH_ATT));
      action.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, reader.getAttribute(DATA_REF_ATT));
      action.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, reader.getAttribute(DATA_PATH_ATT));
   }

   public void addApplication(Model model, Node applicationNode)
   {
      NodeReader reader = new NodeReader(applicationNode);

      String id = reader.getAttribute(ID_ATT);

      if ("Predefined_User_Management_Application".equals(id)
         || "MODIFY_LOGIN_USER_PANEL".equals(id)
         || "Predefined_Terminal_Emulation_Clearing_Application".equals(id))
      {
         return;
      }

      Application application = model.createApplication(id, reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), readElementOID(reader, OID));

      boolean interactive = reader.getBooleanAttribute(INTERACTIVE_ATT, false);
      application.setInteractive(interactive);

      if (interactive)
      {
         NodeList children = applicationNode.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Node contextNode = children.item(i);
            if (CONTEXT.equals(contextNode.getNodeName()))
            {
               String contextName = ((Element) contextNode).getAttribute(ID_ATT);
               ApplicationContext context = application.createContext(convertContextType(contextName));
               model.register(context,  0);

               NodeList contextChildren = contextNode.getChildNodes();
               for (int j = 0; j < contextChildren.getLength(); j++)
               {
                  Node child = contextChildren.item(j);
                  if (ATTRIBUTE.equals(child.getNodeName()))
                  {
                     Attribute attribute = nodeToAttribute(child, attributeNameMap, application);
                     if (attribute != null)
                     {
                        context.setAttribute(attribute);
                     }
                  }
                  else if (ACCESS_POINT.equals(child.getNodeName()))
                  {
                     context.addAccessPoint(nodeToAccessPoint(child));
                  }
               }
            }
         }
      }
      else
      {
         String type = convertApplicationType(reader.getAttribute(TYPE_ATT));
         application.setApplicationTypeId(type);

         NodeList children = applicationNode.getChildNodes();
         for (int i = 0; i < children.getLength(); i++)
         {
            Node child = children.item(i);
            if (ATTRIBUTE.equals(child.getNodeName()))
            {
               Attribute attribute = nodeToAttribute(child, attributeNameMap, application);
               if (attribute != null)
               {
                  // WATCH hardcoded class names to apply refactoring
                  if (PredefinedConstants.SESSIONBEAN_APPLICATION.equals(type)
                  && attribute.getName().equals(PredefinedConstants.CLASS_NAME_ATT))
                  {
                     attribute.setName(PredefinedConstants.REMOTE_INTERFACE_ATT);
                  }
                  else if ("ag.carnot.workflow.spi.providers.jms.JMSApplicationType$DirectionKey".equals(attribute.getClassName()))
                  {
                     attribute.setClassName("ag.carnot.workflow.spi.providers.applications.jms.JMSDirection");
                  }
                  else if ("ag.carnot.workflow.spi.providers.jms.MessageTypeKey".equals(attribute.getClassName()))
                  {
                     attribute.setClassName(
                           "ag.carnot.workflow.spi.providers.applications.jms.MessageType");
                  }
                  else if (attribute.getName().equals(PredefinedConstants.MESSAGE_PROVIDER_PROPERTY))
                  {
                     attribute.setValue("ag.carnot.workflow.spi.providers.applications.jms.DefaultMessageProvider");
                  }
                  else if (attribute.getName().equals(PredefinedConstants.MESSAGE_ACCEPTOR_PROPERTY))
                  {
                     attribute.setValue("ag.carnot.workflow.spi.providers.applications.jms.DefaultMessageAcceptor");
                  }
                  application.setAttribute(attribute);
               }
            }
            else if (ACCESS_POINT.equals(child.getNodeName()))
            {
               application.addAccessPoint(nodeToAccessPoint(child));
            }
         }

      }
   }

   private String convertContextType(String oldType)
   {
      if (oldType == null)
      {
         // @todo (france, ub):  warn
         warn("Old context type is null",null, null);
         return null;
      }
      else if ("JFC".equalsIgnoreCase(oldType))
      {
         return PredefinedConstants.JFC_CONTEXT;
      }
      else if ("JSP".equalsIgnoreCase(oldType))
      {
         return PredefinedConstants.JSP_CONTEXT;
      }
      else if ("default".equalsIgnoreCase(oldType))
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }
      else if ("engine".equalsIgnoreCase(oldType))
      {
         return PredefinedConstants.ENGINE_CONTEXT;
      }
      else
      {
         // @todo (france, ub): warn
         warn("Unknown context type: '" + oldType + "'.", null, null);
         return oldType;
      }
   }

   private String convertApplicationType(String oldType)
   {
      if (oldType == null)
      {
         // @todo (france, ub):  warn
         warn("Old application type is null",null, null);
         return null;
      }
      else if ("ag.carnot.workflow.spi.providers.sessionbean.SessionBeanApplicationType"
            .equals(oldType))
      {
         return PredefinedConstants.SESSIONBEAN_APPLICATION;
      }
      else if ("ag.carnot.workflow.spi.providers.jms.JMSApplicationType".equals(oldType))
      {
         return PredefinedConstants.JMS_APPLICATION;
      }
      else
      {
         // @todo (france, ub): warn
         warn("Unknown application type: '" + oldType + "'.", null, null);
         return null;
      }
   }

   private AccessPoint nodeToAccessPoint(Node node)
   {
      NodeReader reader = new NodeReader(node);

      AccessPoint ap = new AccessPoint(reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getAttribute(CLASS_ATT), reader.getAttribute(DIRECTION_ATT),
            reader.getBooleanAttribute(BROWSABLE_ATT, false),
            reader.getAttribute(USER_OBJECT_VALUE),
            reader.getAttribute(DEFAULT_VALUE_ATT), PredefinedConstants.SERIALIZABLE_DATA);
      model.register(ap, 0);
      return ap;
   }

   public void addData(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      String id = reader.getAttribute(ID_ATT);

      if ("TRIGGER_EMAIL".equals(id))
      {
         return;
      }

      Data data = model.createData(id, reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), readElementOID(reader, OID));

      String type = reader.getAttribute(TYPE_ATT);

      NodeList children = node.getChildNodes();
      Map attributes = new HashMap();
      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);
         if (ATTRIBUTE.equals(child.getNodeName()))
         {
            Attribute attribute = nodeToAttribute(child, attributeNameMap, data);
            if (attribute != null)
            {
               attributes.put(attribute.getName(), attribute);
            }
         }
      }

      Attribute predefined = (Attribute) attributes.get(IS_PREDEFINED_ATTRIBUTE);
      if (predefined != null)
      {
         data.setPredefined("true".equalsIgnoreCase(predefined.getValue()));
      }


      if ("ag.carnot.workflow.EntityBeanDataType".equals(type))
      {
         data.setType(PredefinedConstants.ENTITY_BEAN_DATA);
         data.setAttribute((Attribute) attributes.get(PredefinedConstants.JNDI_PATH_ATT));
         data.setAttribute((Attribute) attributes.get(PredefinedConstants.IS_LOCAL_ATT));
         Attribute remote = (Attribute) attributes.get(PredefinedConstants.CLASS_NAME_ATT);
         if (remote != null)
         {
            remote.setName(PredefinedConstants.REMOTE_INTERFACE_ATT);
            if ("ag.carnot.workflow.runtime.User".equals(remote.getValue()))
            {
               remote.setValue("ag.carnot.workflow.runtime.beans.IUser");
               if (attributes.get(PredefinedConstants.HOME_INTERFACE_ATT) == null)
               {
                  attributes.put(PredefinedConstants.HOME_INTERFACE_ATT,
                        new Attribute(PredefinedConstants.HOME_INTERFACE_ATT,
                                     "ag.carnot.workflow.runtime.User"));
               }
               if (attributes.get(PredefinedConstants.PRIMARY_KEY_ATT) == null)
               {
                  attributes.put(PredefinedConstants.PRIMARY_KEY_ATT,
                        new Attribute(PredefinedConstants.PRIMARY_KEY_ATT,
                                     "ag.carnot.workflow.runtime.UserPK"));
               }
            }
            data.setAttribute(remote);
         }
         data.setAttribute((Attribute) attributes.get(PredefinedConstants.HOME_INTERFACE_ATT));
         data.setAttribute((Attribute) attributes.get(PredefinedConstants.PRIMARY_KEY_ATT));
      }
      else if ("ag.carnot.workflow.SerializableDataType".equals(type))
      {
         Attribute typeAtt = (Attribute) attributes.get(PredefinedConstants.TYPE_ATT);
         if (typeAtt != null)
         {
            String attributeValue = typeAtt.getValue();
            if (!"Serializable".equals(attributeValue))
            {
               data.setType(PredefinedConstants.PRIMITIVE_DATA);
               if ("ag.carnot.workflow.spi.providers.data.java.Type".equals(typeAtt.getClassName())
               && "Date".equals(typeAtt.getValue()))
               {
                  typeAtt.setValue("Calendar");
               }
               data.setAttribute(typeAtt);
               Attribute defaultValue = (Attribute) attributes.get(PredefinedConstants.DEFAULT_VALUE_ATT);
               if (defaultValue != null && !StringUtils.isEmpty(defaultValue.getValue()))
               {
                  if ("Calendar".equals(attributeValue) || "Timestamp".equals(attributeValue))
                  {
                     long lval = Long.parseLong(defaultValue.getValue());
                     Date date = new Date(lval);
                     defaultValue.setValue(NONINTERACTIVE_DATE_FORMAT.format(date));
                  }
                  data.setAttribute(new Attribute(PredefinedConstants.DEFAULT_VALUE_ATT, defaultValue.getValue()));
               }
            }
            else
            {
               data.setType(PredefinedConstants.SERIALIZABLE_DATA);
               
               Attribute className = (Attribute) attributes
                     .get(PredefinedConstants.CLASS_NAME_ATT);

               if ("ag.carnot.workflow.TimeoutException".equals(className.getValue()))
               {
                  className.setValue("ag.carnot.workflow.runtime.TimeoutException");
               }
               else if ("ag.carnot.workflow.MailDetails".equals(className.getValue()))
               {
                  className.setValue("ag.carnot.workflow.runtime.Mail");
               }
               
               data.setAttribute(className);
            }
         }
         else
         {
            // @todo (france, ub): warn
            warn("Type attribute is null.", null, data);
         }
      }
      else
      {
         warn("Unknown type ' " + type + "'.", null, data);
      }
   }

   protected void addSymbols(Node node, SymbolOwner owner)
   {
      NodeList children = node.getChildNodes();

      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);
         NodeSymbol symbol = null;
         String childName = child.getNodeName();
         NodeReader reader = new NodeReader(child);

         if (ACTIVITY_SYMBOL.equals(childName))
         {
            symbol = createActivitySymbol(child);
         }
         else if (ANNOTATION_SYMBOL.equals(childName))
         {
            symbol = createAnnotationSymbol(child);
         }
         else if (APPLICATION_SYMBOL.equals(childName))
         {
            symbol = createApplicationSymbol(child);
         }
         else if (CONDITIONAL_PERFORMER_SYMBOL.equals(childName))
         {
            symbol = createConditionalPerformerSymbol(child);
         }
         else if (DATA_SYMBOL.equals(childName))
         {
            symbol = createDataSymbol(child);
         }
         else if (MODELER_SYMBOL.equals(childName))
         {
            symbol = createModelerSymbol(child);
         }
         else if (ORGANISATION_SYMBOL.equals(childName))
         {
            symbol = createOrganisationSymbol(child);
         }
         else if (PROCESS_SYMBOL.equals(childName))
         {
            symbol = createProcessSymbol(child);
         }
         else if (ROLE_SYMBOL.equals(childName))
         {
            symbol = createRoleSymbol(child);
         }
         else if (GROUP_SYMBOL.equals(childName))
         {
            symbol = createGroupSymbol(child);
         }

         if (symbol != null)
         {
            model.register(symbol, readElementOID(reader, ID_ATT));
            owner.addToNodes(symbol);
         }
      }

      for (int i = 0; i < children.getLength(); i++)
      {
         Connection connection = null;
         Node child = children.item(i);
         String childName = child.getNodeName();
         NodeReader reader = new NodeReader(child);
         if (DATA_MAPPING_CONNECTION.equals(childName))
         {
            connection = createDataMappingConnection(child);
         }
         else if (EXECUTED_BY_CONNECTION.equals(childName))
         {
            connection = createConnection(child, NEW_EXECUTED_BY_CONNECTION,
                  APPLICATION_SYMBOL_ID, ACTIVITY_SYMBOL_ID);
         }
         else if (PART_OF_CONNECTION.equals(childName))
         {
            connection = createConnection(child, NEW_PART_OF_CONNECTION,
                  SUB_ORGANISATION_SYMBOL_ID, ORGANISATION_SYMBOL_ID);
         }
         else if (PERFORMS_CONNECTION.equals(childName))
         {
            connection = createConnection(child, NEW_PERFORMS_CONNECTION,
                  PARTICIPANT_SYMBOL_ID, ACTIVITY_SYMBOL_ID);
         }
         else if (WORKS_FOR_CONNECTION.equals(childName))
         {
            connection = createConnection(child, NEW_WORKS_FOR_CONNECTION,
                  PARTICIPANT_SYMBOL_ID, ORGANISATION_SYMBOL_ID);
         }
         else if (GENERIC_LINK_CONNECTION.equals(childName))
         {

            String linkTypeId = reader.getAttribute(LINK_TYPE_REF);
            connection = createGenericLinkConnection(child, linkTypeId);
         }
         else if (TRANSITION_CONNECTION.equals(childName))
         {
            connection = createTransitionConnection(child);
         }
         else if (SUBPROCESS_OF_CONNECTION.equals(childName))
         {
            connection = createConnection(child, NEW_SUBPROCESS_OF_CONNECTION,
                  SUB_PROCESS_SYMBOL_ID, PROCESS_SYMBOL_ID);
         }
         else if (REFERS_TO_CONNECTION.equals(childName))
         {
            connection = createConnection(child, NEW_REFERS_TO_CONNECTION, FROM, TO);
         }
         if (connection != null)
         {
            model.register(connection, readElementOID(reader, ID_ATT));
            owner.addToConnections(connection);
         }
      }
   }

   private TransitionConnection createTransitionConnection(Node child)
   {
      NodeReader reader = new NodeReader(child);

      TransitionConnection connection = new TransitionConnection(reader.getAttribute(TRANSITION_REF),
            readElementOID(reader, SOURCE_ACTIVITY_SYMBOL_ID),
            readElementOID(reader, TARGET_ACTIVITY_SYMBOL_ID));

      String pointsString = reader.getAttribute(POINTS);

      if (pointsString != null && pointsString.length() != 0)
      {
         StringTokenizer stringTokenizer = new StringTokenizer(pointsString, ",");
         List points = new ArrayList();

         while (stringTokenizer.hasMoreTokens())
         {
            String token = stringTokenizer.nextToken();

            points.add(new Integer(token.trim()));
         }

         connection.setPoints(points);
      }
      return connection;
   }

   private Connection createGenericLinkConnection(Node child, String linkTypeId)
   {
      NodeReader reader = new NodeReader(child);
      return new GenericLinkConnection(linkTypeId,
            readElementOID(reader, SOURCE_SYMBOL_ID),
            readElementOID(reader, TARGET_SYMBOL_ID));
   }

   public void addDiagram(ProcessDefinition process, Node node)
   {
      NodeReader reader = new NodeReader(node);

      Diagram diagram = process.createDiagram(reader.getAttribute(NAME_ATT), readElementOID(reader, ID_ATT));

      addSymbols(node, diagram);
   }

   public void addDiagram(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      Diagram diagram = model.createDiagram(reader.getAttribute(NAME_ATT), readElementOID(reader, ID_ATT));

      addSymbols(node, diagram);
   }

   public void addModeler(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      model.createModeler(reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), reader.getAttribute(PASSWORD),
            readElementOID(reader, OID));
   }

   public void addLinkType(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      model.createLinkType(reader.getAttribute(NAME_ATT),
            convertModelElementName(reader.getAttribute(SOURCE_CLASS)),
            convertModelElementName(reader.getAttribute(TARGET_CLASS)),
            reader.getAttribute(SOURCE_ROLE),
            reader.getAttribute(TARGET_ROLE),
            reader.getAttribute(SOURCE_CARDINALITY),
            reader.getAttribute(TARGET_CARDINALITY),
            reader.getAttribute(SOURCE_SYMBOL),
            reader.getAttribute(TARGET_SYMBOL),
            reader.getAttribute(LINE_COLOR),
            reader.getAttribute(LINE_TYPE),
            reader.getBooleanAttribute(SHOW_LINKTYPE_NAME, false),
            reader.getBooleanAttribute(SHOW_ROLE_NAMES, false),
            readElementOID(reader, OID));
   }

   private String convertModelElementName(String oldName)
   {
      int dot = oldName.lastIndexOf(".");
      return "ag.carnot.workflow.model.I" + oldName.substring(dot+1);
   }

   public void addTrigger(ProcessDefinition process, Node triggerNode, String triggerID)
   {
      NodeReader reader = new NodeReader(triggerNode);

      String type = reader.getAttribute(TYPE_ATT);
      String id = reader.getAttribute(ID_ATT);
      if (StringUtils.isEmpty(id))
      {
         id = triggerID;
      }
      String name = reader.getAttribute(NAME_ATT);
      if (StringUtils.isEmpty(name))
      {
         name = triggerID;
      }
      Trigger trigger = process.createTrigger(convertTriggerType(type), id, name,
            readElementOID(reader, OID));

      NodeList children = triggerNode.getChildNodes();
      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);
         if (ATTRIBUTE.equals(child.getNodeName()))
         {
            Attribute attribute = nodeToAttribute(child, attributeNameMap, trigger);
            if (attribute != null && ! attribute.getName().equals(PredefinedConstants.MESSAGE_ACCEPTOR_PROPERTY))
            {
               if (attribute.getName().equals(PredefinedConstants.MESSAGE_TYPE_ATT))
               {
                  attribute.setClassName(
                        "ag.carnot.workflow.spi.providers.applications.jms.MessageType");
               }
               trigger.setAttribute(attribute);
            }
         }
         else if (ACCESS_POINT.equals(child.getNodeName()))
         {
            trigger.addAccessPoint(nodeToAccessPoint(child));
         }
         else if (PARAMETER_MAPPING.equals(child.getNodeName()))
         {
            addParameterMapping(trigger, child);
         }
      }

      if (trigger.getType().equals(PredefinedConstants.TIMER_TRIGGER))
      {
         String periodKindAttr = (String) trigger.getAttribute(
               TIMER_TRIGGER_PERIODICITY_TYPE_ATT);

         PeriodicityTypeKey periodKind = PeriodicityTypeKey.UNKNOWN;
         if (null != periodKindAttr)
         {
            periodKind = PeriodicityTypeKey.create(Integer.parseInt(periodKindAttr));
         }

         String periodFactorAttr = (String) trigger.getAttribute(
               TIMER_TRIGGER_PERIODICITY_FACTOR_ATT);
         short periodFactor = 0;
         if (null != periodKindAttr)
         {
            Short factor = (Short) Reflect.convertStringToObject(
                  Short.class.getName(), periodFactorAttr);
            periodFactor = (null != factor) ? factor.shortValue() : 0;
         }

         final short ZERO = 0;

         Period periodicity;
         if (null != periodKind)
         {
            switch (periodKind.getValue())
            {
               case PeriodicityTypeKey.ONCE_VALUE:
                  periodicity = null;
                  break;
               case PeriodicityTypeKey.MINUTELY_VALUE:
                  periodicity = new Period(ZERO, ZERO, ZERO, ZERO, periodFactor, ZERO);
                  break;
               case PeriodicityTypeKey.HOURLY_VALUE:
                  periodicity = new Period(ZERO, ZERO, ZERO, periodFactor, ZERO, ZERO);
                  break;
               case PeriodicityTypeKey.DAILY_VALUE:
                  periodicity = new Period(ZERO, ZERO, periodFactor, ZERO, ZERO, ZERO);
                  break;
               case PeriodicityTypeKey.MONTHLY_VALUE:
                  periodicity = new Period(ZERO, periodFactor, ZERO, ZERO, ZERO, ZERO);
                  break;
               case PeriodicityTypeKey.YEARLY_VALUE:
                  periodicity = new Period(periodFactor, ZERO, ZERO, ZERO, ZERO, ZERO);
                  break;
               default:
                  periodicity = null;
            }

            if (null != periodicity)
            {
               trigger.setAttribute(new Attribute(PredefinedConstants
                     .TIMER_TRIGGER_PERIODICITY_ATT, periodicity.getClass().getName(),
                     periodicity.toString()));
            }
         }

         trigger.removeAttribute(TIMER_TRIGGER_PERIODICITY_TYPE_ATT);
         trigger.removeAttribute(TIMER_TRIGGER_PERIODICITY_FACTOR_ATT);
      }
      else if (trigger.getType().equals(PredefinedConstants.MAIL_TRIGGER))
      {
         String protocol = (String) trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT);
         String newProtocol = "pop3";
         if (protocol == null || protocol.equals("0"))
         {
         }
         else if (protocol.equals("1"))
         {
            newProtocol = "imap";
         }
         else
         {
            warn("Unknown mail protocol '" + protocol + "'", null, trigger);
         }
         trigger.removeAttribute(PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT);
         trigger.setAttribute(
               new Attribute(PredefinedConstants.MAIL_TRIGGER_PROTOCOL_ATT, MailProtocol.class.getName(), newProtocol));
         
         for (Iterator i = trigger.getAllAccessPoints(); i.hasNext();)
         {
            AccessPoint ap = (AccessPoint) i.next();
            for (Iterator j = ap.getAllAttributes(); j.hasNext();)
            {
               Attribute attr = (Attribute) j.next();
               
               if (PredefinedConstants.CLASS_NAME_ATT.equals(attr.getName())
                     && "ag.carnot.workflow.MailDetails".equals(attr.getValue()))
               {
                  attr.setValue("ag.carnot.workflow.runtime.Mail");
               }
            }
         }
      }

      convertTriggerName(type, trigger);
   }

   private String convertTriggerType(String oldType)
   {
      if ("ag.carnot.workflow.beans.ManualTriggerType".equals(oldType))
      {
         return PredefinedConstants.MANUAL_TRIGGER;
      }
      if ("ag.carnot.workflow.beans.JMSTriggerType".equals(oldType))
      {
         return PredefinedConstants.JMS_TRIGGER;
      }
      if ("ag.carnot.workflow.beans.MailTriggerType".equals(oldType))
      {
         return PredefinedConstants.MAIL_TRIGGER;
      }
      if ("ag.carnot.workflow.beans.TimerTriggerType".equals(oldType))
      {
         return PredefinedConstants.TIMER_TRIGGER;
      }
      warn("Unknown trigger type '" + oldType + "'.", null, null);
      return oldType;
   }

   private void convertTriggerName(String oldType, Trigger trigger)
   {
      if ("ag.carnot.workflow.beans.ManualTriggerType".equals(oldType))
      {
         trigger.setName((String) trigger.getAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT));
      }
      else if ("ag.carnot.workflow.beans.JMSTriggerType".equals(oldType))
      {
         trigger.setName((String) trigger.getAttribute(PredefinedConstants.MESSAGE_TYPE_ATT));
      }
      else if ("ag.carnot.workflow.beans.MailTriggerType".equals(oldType))
      {
         trigger.setName((String) trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_USER_ATT)
               + "@"+ trigger.getAttribute(PredefinedConstants.MAIL_TRIGGER_SERVER_ATT));
      }
      else if ("ag.carnot.workflow.beans.TimerTriggerType".equals(oldType))
      {
         // @todo (france, ub): make something useful
         trigger.setName("Timer based trigger");
      }
      else
      {
         warn("Unknown trigger type '" + oldType + "'.", null, null);
      }
   }

   private void addParameterMapping(Trigger trigger, Node mappingNode)
   {
      NodeReader reader = new NodeReader(mappingNode);

      trigger.createParameterMapping(reader.getAttribute(DATA_REF_ATT), reader.getAttribute(PARAMETER),
            reader.getAttribute(PARAMETER_PATH), readElementOID(reader, OID));
   }

   public void addConditionalPerformer(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      model.createConditionalPerformer(reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), reader.getAttribute(DATA_REF_ATT),
            reader.getBooleanAttribute(IS_USER, false), readElementOID(reader, OID));

   }

   public void addNotification(EventHandlerOwner owner, Node node, Node ownerNode,
         String id)
   {
      NodeReader reader = new NodeReader(node);

      int oldType = reader.getIntegerAttribute(NOTIFICATION_ACTION_TYPE, 0);

      EventHandler notification = owner.createEventHandler(
            id, PredefinedConstants.TIMER_CONDITION, readElementOID(reader, OID));
      notification.setAutobind(true);
      notification.setUnbindOnMatch(true);

      notification.setAttribute(new Attribute(PredefinedConstants.TIMER_PERIOD_ATT,
            Period.class.getName(), computePeriod(reader)));
      notification.setAttribute(new Attribute(
            PredefinedConstants.TIMER_CONDITION_USE_DATA_ATT, "boolean", "false"));

      EventAction mainAction = notification.createEventAction(getNotificationActionType(oldType), id, id);

      switch (oldType)
      {
         case 0:  // mail
            mainAction.setAttribute(new Attribute(PredefinedConstants.MAIL_ACTION_RECEIVER_ATT,
                  reader.getAttribute(RECEIVER_REF)));
            mainAction.setAttribute(new Attribute(PredefinedConstants.MAIL_ACTION_RECEIVER_TYPE_ATT,
                  "ag.carnot.workflow.spi.providers.actions.mail.ReceiverType", "participant"));
            break;
         case 1: // trigger process
            mainAction.setAttribute(new Attribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT,
                  reader.getAttribute(TRIGGERED_PROCESS_DEFINITION_REF)));
            break;
         case 2: // exception mapping
            attachTimeoutExceptionActionAttribute(mainAction, ownerNode);
            break;
         default:
            trace.warn("Unknown notification type " + oldType);
      }

      if (reader.getBooleanAttribute(ABORT_PROCESS_ATT, false))
      {
         notification.createEventAction(PredefinedConstants.ABORT_PROCESS_ACTION, id + "B", id + "B");
      }
      else if (reader.getBooleanAttribute(COMPLETE_ACTIVITY_ATT, oldType == 2))
      {
         notification.createEventAction(PredefinedConstants.COMPLETE_ACTIVITY_ACTION, id + "B", id + "B");
      }
   }

   private void attachTimeoutExceptionActionAttribute(EventAction action,
         Node ownerNode)
   {
      // create action type

      if (model.findEventActionType(PredefinedConstants.EXCEPTION_ACTION) == null)
      {
        EventActionType exceptionActionType = model.createEventActionType(
            PredefinedConstants.EXCEPTION_ACTION, "On Exception",
            true, false, true, 0);
        exceptionActionType.setAttribute(PredefinedConstants.ACTION_CLASS_ATT,
            PredefinedConstants.EXCEPTION_ACTION_CLASS);
         exceptionActionType.setAttribute(PredefinedConstants.PANEL_CLASS_ATT,
               PredefinedConstants.EXCEPTION_ACTION_PANEL_CLASS);
         exceptionActionType.addSupportedConditionType(PredefinedConstants.TIMER_CONDITION);
      }
      // NOTE convert only the first exception handler here

      NodeList childNodes = ownerNode.getChildNodes();
      boolean eaten = false;
      for (int i = 0; i < childNodes.getLength(); i++)
      {
         if (eaten)
         {
            warn("Only one exception handler is supported for timeout exception conversion.", null, action);
            return;
         }
         Node childNode = childNodes.item(i);
         if (EXCEPTION_HANDLER.equals(childNode.getNodeName()))
         {
            NodeReader reader = new NodeReader(childNode);
            action.setAttribute(PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, reader.getAttribute(EXCEPTION_PATH_ATT));
            action.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, reader.getAttribute(DATA_REF_ATT));
            action.setAttribute(PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, reader.getAttribute(DATA_PATH_ATT));
            eaten = true;
         }
      }

   }

   private String computePeriod(NodeReader reader)
   {
      int yearTO = reader.getIntegerAttribute(YEAR_TIMEOUT, 0);
      int monthTO = reader.getIntegerAttribute(MONTH_TIMEOUT, 0);
      long time = reader.getLongAttribute(TIME_OUT, 0);
      String v;
      if (time == 0)
      {
         v = "0:0:0:0";
      }
      else
      {
         Calendar res = TimestampProviderUtils.getCalendar(new Date(time));
         v = (res.get(Calendar.DAY_OF_MONTH) -1) + ":" + (res.get(Calendar.HOUR_OF_DAY) -1)
              + ":" + res.get(Calendar.MINUTE) + ":" + res.get(Calendar.SECOND);
      }
      return yearTO + ":" + monthTO + ":" + v;
   }

   private String getNotificationActionType(int oldType)
   {
      switch (oldType)
      {
         case 0:
            return PredefinedConstants.MAIL_ACTION;
         case 1:
            return PredefinedConstants.TRIGGER_ACTION;
         case 2:
            return PredefinedConstants.EXCEPTION_ACTION;
         default:
            // @todo (france, ub): warn
            warn("Unknown notification type :" + oldType, null, null);
      }
      return null;
   }

   public void addOrganisation(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      Organization organisation = model.createOrganization(
            reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), readElementOID(reader, OID));

      NodeList children = node.getChildNodes();

      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);

         if (ASSOCIATED_PARTICIPANT.equals(child.getNodeName()))
         {
            NodeReader childReader = new NodeReader(child);
            organisation.addToParticipants(childReader.getAttribute(ID_ATT));
         }
      }
   }

   public void addProcess(Model model, Node node)
   {
      int notificationID = 0;
      int triggerID = 0;
      NodeReader reader = new NodeReader(node);

      String id = reader.getAttribute(ID_ATT);

      if ("Predefined_User_Management_Process".equals(id)
          || "Predefined_Terminal_Emulation_Clearing_Process".equals(id)
          || "MODIFY_LOGIN_USER".equals(id))
      {
         return;
      }
      ProcessDefinition process = model.createProcessDefinition(id, reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), readElementOID(reader, OID));

      NodeList nodeList = node.getChildNodes();

      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Node child = nodeList.item(i);
         if (ACTIVITY.equals(child.getNodeName()))
         {
            addActivity(process, child);
         }
         else if (TRANSITION.equals(child.getNodeName()))
         {
            addTransition(process, child);
         }
         else if (DESCRIPTOR.equals(child.getNodeName()))
         {
            addDescriptor(process, child);
         }
         else if (DATA_PATH.equals(child.getNodeName()))
         {
            addDataPath(process, child);
         }
         else if (DATA_MAPPING.equals(child.getNodeName()))
         {
            addDataMapping(process, child);
         }
         else if (TRIGGER.equals(child.getNodeName()))
         {
            addTrigger(process, child, "trigger" + triggerID++);
         }
         else if (NOTIFICATION.equals(child.getNodeName()))
         {
            addNotification(process, child, node, "timer" + notificationID++);
         }
         else if (DIAGRAM.equals(child.getNodeName()))
         {
            addDiagram(process, child);
         }
      }
   }

   private Attribute nodeToAttribute(Node node, Map nameMap, ModelElement attributeOwner)
   {
      NodeReader reader = new NodeReader(node);

      String oldName = reader.getAttribute(NAME_ATT);
      String newName = (String) nameMap.get(oldName);
      if (newName == null)
      {
         trace.warn("Unknown attribute name: " + oldName + ", Owner: " + attributeOwner);
         return null;
      }
      return new Attribute(newName,
            reader.getAttribute(CLASS_ATT),
            reader.getAttribute(VALUE_ATT));
   }

   public void addRole(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      String id = reader.getAttribute(ID_ATT);

      if ("TERMINAL_EMULATION_CLEARING".equals(id))
      {
         return;
      }

      model.createRole(reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION),
            reader.getIntegerAttribute(CARDINALITY, Unknown.INT),
            readElementOID(reader, OID));
   }

   public void addTransition(ProcessDefinition process, Node node)
   {
      NodeReader reader = new NodeReader(node);

      process.createTransition(reader.getAttribute(ID_ATT), reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION),
            reader.getAttribute(FROM), reader.getAttribute(TO),
            reader.getAttribute(CONDITION, "TRUE"),
            reader.getBooleanAttribute(FORK_ON_TRAVERSAL, false),
            readElementOID(reader, OID));
   }

   public void addDataMapping(ProcessDefinition process, Node node)
   {
      NodeReader reader = new NodeReader(node);

      String activityID = reader.getAttribute(ACTIVITY_ATT);
      Activity activity = process.findActivity(activityID);

      if (activity == null)
      {
         warn("Activity '" + activityID + "' for data mapping not found.", null, process);
         return;
      }

      String context = convertContextType(reader.getAttribute(CONTEXT_ATT));
      String applicationAccesspoint = reader.getAttribute(APPLICATION_ACCESS_POINT_ATT);

      if (ENGINE_CONTEXT.equals(context)
            && !EXCLUDED_PERFORMER_ACCESSPOINT.equals(applicationAccesspoint)
            && !OID_ACCESSPOINT.equals(applicationAccesspoint)
            && !PERFORMED_BY_ACCESSPOINT.equals(applicationAccesspoint))
      {
         context = "application";
      }
      
      final String direction = reader.getAttribute(DIRECTION_ATT);
      String applicationPath = reader.getAttribute(APPLICATION_PATH_ATT);
      
      if ("OUT".equals(direction) && ENGINE_CONTEXT.equals(context))
      {
         if (OID_ACCESSPOINT.equals(applicationAccesspoint))
         {
            applicationAccesspoint = "activityInstance";
            applicationPath = "getOID()";
         }
         else if (PERFORMED_BY_ACCESSPOINT.equals(applicationAccesspoint))
         {
            applicationAccesspoint = "activityInstance";
            applicationPath = "getPerformedByOID()";
         }
      }

      activity.createDataMapping(reader.getAttribute(ID_ATT),
            reader.getAttribute(DATA_REF_ATT),
            direction,
            context,
            applicationAccesspoint,
            reader.getAttribute(DATA_PATH_ATT),
            applicationPath,
            readElementOID(reader, OID));
   }

   public void addDescriptor(ProcessDefinition processDefinition, Node node)
   {
      NodeReader reader = new NodeReader(node);

      processDefinition.createDataPath(reader.getAttribute(ID_ATT),
            reader.getAttribute(NAME_ATT),
            reader.getAttribute(PATH_ATT),
            DIRECTION_IN, true,
            readElementOID(reader, OID));
   }

   public void addDataPath(ProcessDefinition processDefinition, Node node)
   {
      NodeReader reader = new NodeReader(node);

      processDefinition.createDataPath(reader.getAttribute(ID_ATT),
            reader.getAttribute(NAME_ATT), reader.getAttribute(PATH_ATT),
            reader.getAttribute(DIRECTION_ATT), false, readElementOID(reader, OID));
   }

   public void addViewable(Model model, View view, Node node)
   {
      NodeReader reader = new NodeReader(node);

      String id = reader.getAttribute(ID_ATT);
      String type = reader.getAttribute(TYPE_ATT);

      // fixes silently a bug here, activity id is not globally unique
      // 3.0 models will refer by oid
      ModelElement viewable = null;
      if (TYPE_ACTIVITY.equals(type))
      {
         Iterator i = model.getAllProcessDefinitions();
         while (i.hasNext() && viewable == null)
         {
            viewable = ((ProcessDefinition) i.next()).findActivity(id);
         }
      }
      else if (TYPE_APPLICATION.equals(type))
      {
         viewable = model.findApplication(id);
      }
      else if (TYPE_DATA.equals(type))
      {
         viewable = model.findData(id);
      }
      else if (TYPE_PARTICIPANT.equals(type))
      {
         viewable = model.findParticipant(id);
      }
      else if (TYPE_WORKFLOW.equals(type))
      {
         viewable = model.findProcessDefinition(id);
      }
      else
      {
         warn("Unkown type of viewable: " + type, null, view);
      }

      if (viewable != null)
      {
         view.addViewable(viewable);
      }
   }

   public void addView(Model model, Node node)
   {
      NodeReader reader = new NodeReader(node);

      View view = model.createView(reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION), readElementOID(reader, OID));

      NodeList nodeList = node.getChildNodes();
      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Node child = nodeList.item(i);
         if (VIEW.equals(child.getNodeName()))
         {
            addView(model, view, child);
         }
         else if (VIEWABLE.equals(child.getNodeName()))
         {
            addViewable(model, view, child);
         }
      }
   }

   public void addView(Model model, View parentView, Node node)
   {
      NodeReader reader = new NodeReader(node);

      View view = new View(
            reader.getAttribute(NAME_ATT),
            reader.getChildValue(DESCRIPTION),
            readElementOID(reader, OID), model);

      parentView.addView(view);

      NodeList nodeList = node.getChildNodes();
      if (nodeList != null)
      {
         int length = nodeList.getLength();
         for (int i = 0; i < length; i++)
         {
            Node child = nodeList.item(i);
            if (VIEW.equals(child.getNodeName()))
            {
               addView(model, view, child);
            }
            else if (VIEWABLE.equals(child.getNodeName()))
            {
               addViewable(model, view, child);
            }
         }
      }
   }

   private int readElementOID(NodeReader reader, String attributeName)
   {
      boolean patchElementOids = Parameters.instance().getBoolean(
            "carnot.upgrade.m30.patchElementOids", true);
      
      long oldElementOID = reader.getLongAttribute(attributeName, 0);
      int baseElementOID = (int) oldElementOID;
      return patchElementOids
            ? (int) ((oldElementOID - baseElementOID >> 15) + baseElementOID)
            : (int) oldElementOID;
   }

   public NodeSymbol createActivitySymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new ActivitySymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0), reader.getIntegerAttribute(Y, 0));
   }

   public AnnotationSymbol createAnnotationSymbol(Node node)
   {

      NodeReader reader = new NodeReader(node);
      return new AnnotationSymbol(reader.getChildValue(TEXT),
            reader.getIntegerAttribute(X, 0), reader.getIntegerAttribute(Y, 0));

   }

   public ApplicationSymbol createApplicationSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new ApplicationSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0),
            reader.getIntegerAttribute(Y, 0));
   }

   public DataMappingConnection createDataMappingConnection(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new DataMappingConnection(readElementOID(reader, DATA_SYMBOL_ID),
            readElementOID(reader, ACTIVITY_SYMBOL_ID));
   }

   public DataSymbol createDataSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new DataSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0),
            reader.getIntegerAttribute(Y, 0));
   }

   public GroupSymbol createGroupSymbol(Node node)
   {
      GroupSymbol symbol = new GroupSymbol();

      addSymbols(node, symbol);

      return symbol;
   }

   public ModelerSymbol createModelerSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new ModelerSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0),
            reader.getIntegerAttribute(Y, 0));
   }

   public OrganizationSymbol createOrganisationSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new OrganizationSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0), reader.getIntegerAttribute(Y, 0));
   }

   public Connection createConnection(Node node, String name,
         String firstSymbolName, String secondSymbolName)
   {
      NodeReader reader = new NodeReader(node);

      return new Connection(name,
            readElementOID(reader, firstSymbolName),
            readElementOID(reader, secondSymbolName));
   }

   public ProcessDefinitionSymbol createProcessSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new ProcessDefinitionSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0), reader.getIntegerAttribute(Y, 0));
   }

   public ConditionalPerformerSymbol createConditionalPerformerSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new ConditionalPerformerSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0), reader.getIntegerAttribute(Y, 0));
   }

   public RoleSymbol createRoleSymbol(Node node)
   {
      NodeReader reader = new NodeReader(node);

      return new RoleSymbol(reader.getAttribute(USEROBJECT),
            reader.getIntegerAttribute(X, 0), reader.getIntegerAttribute(Y, 0));
   }

   private int getMaxId(Element node, String prefix, String attributeName,
         String elementFilter)
   {
      int max = 0;
      NodeList elements = node.getElementsByTagName(elementFilter);
      for (int i = 0; i < elements.getLength(); i++)
      {
         Element element = (Element) elements.item(i);
         if (element.hasAttribute(attributeName))
         {
            if (prefix != null)
            {
               String att = element.getAttribute(attributeName);
               if (att.startsWith(prefix))
               {
                  try
                  {
                     max = Math.max(max,
                           (int) Long.parseLong(element.getAttribute(attributeName).substring(prefix.length())));
                  }
                  catch (NumberFormatException e)
                  {
                     trace.debug("Rejected: " + element.getAttribute(attributeName));
                  }
               }
            }
            else
            {
               max = Math.max(max,
                     (int) Long.parseLong(element.getAttribute(attributeName)));
            }
         }
      }
      return max;
   }

   private void warn(String message, Exception exception, ModelElement scope)
   {
      if (exception != null)
      {
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message, exception);
      }
      else
      {
         trace.debug("", new Exception());
         trace.warn("Conversion Warning: scope = '" + scope + "': " + message);
      }
   }

   public Model read(Document source)
   {
      NodeList elements = source.getElementsByTagName(MODEL);
      Element rootNode = (Element) elements.item(0);
      NodeReader reader = new NodeReader(rootNode);

      long oid = reader.getLongAttribute(OID, 0);
      model = new Model(oid >> 32, reader.getAttribute(ID_ATT),
            reader.getAttribute(NAME_ATT), reader.getChildValue(DESCRIPTION));

      int maxOID = getMaxId(rootNode, null, OID, "*");
      NodeList diagrams = rootNode.getElementsByTagName("DIAGRAM");
      for (int i = 0; i < diagrams.getLength(); i++)
      {
         Element diagram = (Element) diagrams.item(i);
         maxOID = Math.max(maxOID, getMaxId(diagram, null, ID_ATT, "*"));
      }

      model.setCurrentElementOID(maxOID + 1);
      String modelVersion = reader.getAttribute(MODEL_VERSION);
      if (null != modelVersion)
      {
         model.setAttribute(new Attribute(PredefinedConstants.VERSION_ATT,
            modelVersion));
      }
      long validFrom = reader.getLongAttribute(VALID_FROM_TIME_STAMP, 0);
      if (validFrom > 0)
      {
         model.setAttribute(new Attribute(PredefinedConstants.VALID_FROM_ATT,
            Date.class.getName(), DateUtils.getNoninteractiveDateFormat().format(
                  new Date(validFrom))));
      }
      long validTo = reader.getLongAttribute(VALID_TO_TIME_STAMP, 0);
      if (validTo > 0)
      {
         model.setAttribute(new Attribute(PredefinedConstants.VALID_TO_ATT,
            Date.class.getName(), DateUtils.getNoninteractiveDateFormat().format(
                  new Date(validTo))));
      }
      model.setAttribute(new Attribute(PredefinedConstants.IS_RELEASED_ATT,
            Boolean.TYPE.getName(),
            STATUS_RELEASED.equals(reader.getAttribute(STATUS))
                  ? Boolean.TRUE.toString()
                  : Boolean.FALSE.toString()));

      NodeList data = rootNode.getOwnerDocument().getElementsByTagName(DATA);

      for (int i = 0; i < data.getLength(); i++)
      {
         addData(model, data.item(i));
      }

      NodeList applications = rootNode.getOwnerDocument().getElementsByTagName(APPLICATION);

      for (int i = 0; i < applications.getLength(); i++)
      {
         addApplication(model, applications.item(i));
      }

      NodeList participants = rootNode.getOwnerDocument().getElementsByTagName(PARTICIPANTS);

      for (int i = 0; i < participants.getLength(); i++)
      {
         NodeList children = participants.item(i).getChildNodes();

         for (int j = 0; j < children.getLength(); j++)
         {
            Node child = children.item(j);
            if (MODELER.equals(child.getNodeName()))
            {
               addModeler(model, child);
            }
            if (ROLE.equals(child.getNodeName()))
            {
               addRole(model, child);
            }
            else if (CONDITIONAL_PERFORMER.equals(child.getNodeName()))
            {
               addConditionalPerformer(model, child);
            }
            else if (ORGANISATION.equals(child.getNodeName()))
            {
               addOrganisation(model, child);
            }
         }
      }

      NodeList linkTypes = rootNode.getOwnerDocument().getElementsByTagName(LINK_TYPE);

      for (int i = 0; i < linkTypes.getLength(); i++)
      {
         addLinkType(model, linkTypes.item(i));
      }

      NodeList processes = rootNode.getOwnerDocument().getElementsByTagName(PROCESS);

      for (int i = 0; i < processes.getLength(); i++)
      {
         addProcess(model, processes.item(i));
      }

      NodeList diagrams1 = rootNode.getOwnerDocument().getElementsByTagName(DIAGRAM);

      for (int i = 0; i < diagrams1.getLength(); i++)
      {
         Node child = diagrams1.item(i);

         if (rootNode.equals(child.getParentNode()))
         {
            addDiagram(model, child);
         }
      }

      NodeList views = rootNode.getChildNodes();
      for (int i = 0; i < views.getLength(); i++)
      {
         Node child = views.item(i);
         if (VIEW.equals(child.getNodeName()))
         {
            addView(model, child);
         }
      }
      return model;
   }

   private class NodeReader
   {
      private Node node = null;

      NodeReader(Node node)
      {
         this.node = node;
      }

      public String getAttribute(String name)
      {
         NamedNodeMap attributes = node.getAttributes();

         if (attributes != null)
         {
            Node node = attributes.getNamedItem(name);

            if (node != null)
            {
               return node.getNodeValue();
            }
         }

         return null;
      }

      public boolean getBooleanAttribute(String name, boolean defaultValue)
      {
         String value = getAttribute(name);

         if (value == null || value.length() == 0)
         {
            return defaultValue;
         }
         return "true".equalsIgnoreCase(value);
      }

      public int getIntegerAttribute(String name, int defaultValue)
      {
         String value = getAttribute(name);

         if (value != null)
         {
            return Integer.parseInt(value);
         }

         return defaultValue;
      }

      public long getLongAttribute(String name, long defaultValue)
      {
         String value = getAttribute(name);

         if (value != null)
         {
            return Long.parseLong(value);
         }

         return defaultValue;
      }

      public String getNodeValue()
      {
         return node.getNodeValue();
      }

      /**
       * Returns the value of the childnode from the DOM-Node or
       * null if there isn't such subnode.
       */
      public String getChildValue(String childName)
      {
         Assert.isNotNull(node, "the associated node is not null");

         Node _childNode;
         String _text;

         try
         {
            _childNode = node.getFirstChild();

            while ((_childNode != null)
                  //               && (_childNode.getNodeType() != Node.ELEMENT_NODE) //kwinkler: why ? removed because not clear
                  && (!childName.equals(_childNode.getNodeName()))
                  )
            {
               _childNode = _childNode.getNextSibling();
            }
            _text = _childNode.getFirstChild().getNodeValue();
         }
         catch (NullPointerException _ex)
         {
            // Ignore exception because the searched value could be optional

            _text = null;
         }

         return _text;
      }

      public void setNode(Node node)
      {
         this.node = node;
      }

      public String getAttribute(String name, String defaultValue)
      {
         String result = getAttribute(name);
         if (StringUtils.isEmpty(result))
         {
            return defaultValue;
         }
         return result;
      }
   }
}
