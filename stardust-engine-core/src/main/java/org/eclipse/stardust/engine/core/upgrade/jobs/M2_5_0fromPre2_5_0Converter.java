/*******************************************************************************
 * Copyright (c) 2011, 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.upgrade.jobs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradableItem;
import org.eclipse.stardust.engine.core.upgrade.utils.OIDProvider;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Model conversion job from all pre 2.1 versions to 2.1
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class M2_5_0fromPre2_5_0Converter extends ModelUpgradeJob
{
   private static final Logger trace = LogManager
         .getLogger(M2_5_0fromPre2_5_0Converter.class);

   private static final Version MODEL_VERSION = Version.createFixedVersion(2, 5, 0);
   private static final String MODEL_ENCODING = "ISO-8859-1";
   private static final String MODEL_DTD = "WorkflowModel.dtd";

   // old element names

   private static final String TRIGGER_FAX = "TRIGGER_FAX";
   private static final String TRIGGER_JMS = "TRIGGER_JMS";
   private static final String TRIGGER_MAIL = "TRIGGER_MAIL";
   private static final String TRIGGER_MANUAL = "TRIGGER_MANUAL";
   private static final String TRIGGER_TIMER_BASED = "TRIGGER_TIMER_BASED";

   // new element names

   private static final String ACTIVITY = "ACTIVITY";
   private static final String APPLICATION = "APPLICATION";
   private static final String DATA = "DATA";
   private static final String DATA_MAPPING = "DATA_MAPPING";
   private static final String DESCRIPTOR = "DESCRIPTOR";
   private static final String DESCRIPTION = "DESCRIPTION";
   private static final String DIAGRAM = "DIAGRAM";
   private static final String GENERIC_LINK = "GENERIC_LINK";
   private static final String LINK_TYPE = "LINK_TYPE";
   private static final String NOTIFICATION = "NOTIFICATION";
   private static final String PARTICIPANTS = "PARTICIPANTS";
   private static final String PROCESS = "WORKFLOW";
   private static final String TRANSITION = "TRANSITION";
   private static final String USERDEFINED_PROPERTY = "USERDEFINED_PROPERTY";
   private static final String TRIGGER = "TRIGGER";
   private static final String ACCESS_POINT = "ACCESS_POINT";
   private static final String VIEW = "VIEW";
   private static final String PARAMETER_MAPPING = "PARAMETER_MAPPING";
   private static final String CONTEXT = "CONTEXT";
   private static final String ATTRIBUTE = "ATTRIBUTE";

   // old attribute names

   private static final String OLD_HTML_HOME = "html_path";
   private static final String OLD_INTERACTIVE_EMULATION = "interactive_emulation";
   private static final String OLD_JNDI_HOME = "jndi_path";
   private static final String OLD_LOCAL = "local";
   private static final String OLD_SCREEN_ID_POS = "screen_id_position";
   private static final String OLD_REFERENCE_CLASS = "class";
   private static final String OLD_REFERENCE_METHOD = "method";
   private static final String OLD_IN_PARAMETER_ID = "in_parameter_id";
   private static final String OLD_OUT_PARAMETER_ID = "out_parameter_id";
   private static final String OLD_APPLICATION_IN_PATH = "application_in_path";
   private static final String OLD_APPLICATION_OUT_PATH = "application_out_path";
   private static final String OLD_DATA_IN_PATH = "data_in_path";
   private static final String OLD_DATA_OUT_PATH = "data_out_path";
   private static final String OLD_USER_DEF_APP_TYPE = "user_definition_type";
   private static final String OLD_PASSWORD_ATT = "password";
   private static final String OLD_SERVER_ATT = "server";
   private static final String OLD_PROTOCOL_ATT = "protocol";
   private static final String OLD_SELECTOR_ATT = "selector_predicate";
   private static final String OLD_USER_ATT = "user";
   private static final String OLD_PERIODICITY_TYPE_ATT = "periodicity_type";
   private static final String OLD_PERIODICITY_FACTOR_ATT = "periodicity_factor";
   private static final String OLD_START_TIMESTAMP_ATT = "start_time_stamp";
   private static final String OLD_IMPLEMENTATION_ATT = "implementation";
   private static final String OLD_APPLICATION_ATT = "application";
   private static final String OLD_PARTICIPANT_ATT = "participant";

   // new attribute names

   private static final String PATH_ATT = "path";
   private static final String ACTIVITY_ATT = "activity";
   private static final String ACTUAL_COST_PER_SECOND = "actual_cost_per_second";
   private static final String CARNOT_XML_VERSION = "carnot_xml_version";
   private static final String COST_CENTER = "cost_center";
   private static final String DATA_ATT = "data";
   private static final String PARAMETER_ATT = "parameter";
   private static final String PARAMETER_PATH_ATT = "parameter_path";
   private static final String ID_ATT = "id";
   private static final String TYPE_ATT = "type";
   private static final String NAME_ATT = "name";
   private static final String CLASS_ATT = "class";
   private static final String VALUE_ATT = "value";
   private static final String APPLICATION_PATH = "application_path";
   private static final String DATA_PATH = "data_path";
   private static final String APPLICATION_ACCESS_POINT = "application_access_point";
   private static final String DATA_ACCESS_POINT = "data_access_point";
   private static final String OID_ATT = "oid";
   private static final String CONTEXT_ATT = "context";
   private static final String DIRECTION_ATT = "direction";
   private static final String INTERACTIVE_ATT = "interactive";

   // old attribute values

   private static final String OLD_TYPE_KEY_BOOLEAN = "Boolean";
   private static final String OLD_TYPE_KEY_CHARACTER = "Character";
   private static final String OLD_TYPE_KEY_BYTE = "Byte";
   private static final String OLD_TYPE_KEY_SHORT = "Short";
   private static final String OLD_TYPE_KEY_INTEGER = "Integer";
   private static final String OLD_TYPE_KEY_LONG = "Long";
   private static final String OLD_TYPE_KEY_FLOAT = "Float";
   private static final String OLD_TYPE_KEY_DOUBLE = "Double";

   private static final String OLD_IN_DIRECTION = "IN";
   private static final String OLD_OUT_DIRECTION = "OUT";
   private static final String OLD_INOUT_DIRECTION = "INOUT";

   private static final String OLD_TYPE_USER_DEFINED = "User Defined";
   private static final String OLD_TYPE_SESSION_BEAN = "Session Bean";
   private static final String OLD_TYPE_CICS = "CICS";

   private static final String OLD_USER_TYPE_JFC = "JFC";
   private static final String OLD_USER_TYPE_HTML = "HTML";
   private static final String OLD_USER_TYPE_BOTH = "Both";

   private static final String OLD_MANUAL_ACTIVITY_TYPE = "Manual";
   private static final String OLD_APPLICATION_TYPE = "Application";

   // new attribute values

   private static final String NEW_TYPE_KEY_BOOLEAN = "boolean";
   private static final String NEW_TYPE_KEY_CHARACTER = "char";
   private static final String NEW_TYPE_KEY_BYTE = "byte";
   private static final String NEW_TYPE_KEY_SHORT = "short";
   private static final String NEW_TYPE_KEY_INTEGER = "int";
   private static final String NEW_TYPE_KEY_LONG = "long";
   private static final String NEW_TYPE_KEY_FLOAT = "float";
   private static final String NEW_TYPE_KEY_DOUBLE = "double";
   private static final String NEW_TYPE_KEY_STRING = "String";
   private static final String NEW_TYPE_KEY_DATE = "Date";
   private static final String NEW_TYPE_KEY_MONEY = "Money";
   private static final String NEW_TYPE_KEY_ENTITY_BEAN = "Entity Bean";
   private static final String NEW_TYPE_KEY_SERIALIZABLE = "Serializable";

   private static final String NEW_TYPE_SESSION_BEAN =
         "ag.carnot.workflow.spi.providers.sessionbean.SessionBeanApplicationType";
   private static final String NEW_TYPE_CICS =
         "ag.carnot.workflow.spi.providers.interactive.HostApplicationType";

   //@todo resolve when moved to workflow package
   private static final String MANUAL_TRIGGER_TYPE_CLASS =
         "ag.carnot.workflow.beans.ManualTriggerType";
   private static final String MAIL_TRIGGER_TYPE_CLASS =
         "ag.carnot.workflow.beans.MailTriggerType";
   private static final String TIMER_TRIGGER_TYPE_CLASS =
         "ag.carnot.workflow.beans.TimerTriggerType";

   private static final String NEW_JNDI_PATH_ATTRIBUTE = "jndiPath";
   private static final String NEW_CLASS_NAME_ATTRIBUTE = "className";
   private static final String METHOD_NAME_PROPERTY = "methodName";
   private static final String ISLOCAL_PROPERTY = "isLocal";

   private static final String HTML_PATH_PROPERTY = "htmlPath";

   private static final String SCREEN_ID_POS_PROPERTY = "screenIdPosition";
   private static final String INTERACTIVE_EMULATION_PROPERTY = "interactiveEmulation";

   private static final String USER_ATTRIBUTE = "user";
   private static final String SERVER_ATTRIBUTE = "host";
   private static final String PASSWORD_ATTRIBUTE = "password";
   private static final String PROTOCOL_ATTRIBUTE = "protocol";
   private static final String PREDICATE_ATTRIBUTE = "selectorPredicate";

   private static final String PERIODICITY_FACTOR_ATT = "periodicityFactor";
   private static final String PERIODICITY_TYPE_ATT = "periodicityType";
   private static final String START_TIMESTAMP_ATT = "startTimestamp";

   private static final String PARTICIPANT_ATTRIBUTE = "participant";

   private static final String NEW_HOME_INTERFACE_ATTRIBUTE = "homeInterface";

   private static final String BOOLEAN_CLASS_NAME = "java.lang.Boolean";
   private static final String INTEGER_CLASS_NAME = "java.lang.Integer";
   private static final String LONG_CLASS_NAME = "java.lang.Long";
   private static final String SHORT_CLASS_NAME = "java.lang.Short";
   private static final String STRING_CLASS_NAME = "java.lang.String";
   private static final String OLD_MAIL_DATA_CLASS = "ag.carnot.mail.Mail";
   private static final String NEW_MAIL_DATA_CLASS = "ag.carnot.workflow.MailDetails";
   private static final String PREDEFINED_MAIL_DATA_ID = "TRIGGER_EMAIL";
   private static final String NEW_PRIMARY_KEY_ATTRIBUTE = "primaryKey";
   private static final String OLD_LOCAL_ATT = "local";
   private static final String IS_PREDEFINED = "is_predefined";
   private static final String DEFAULT_VALUE = "default_value";
   private static final String OLD_JNDI_PATH_ATT = "jndi_path";
   private static final String NEW_IS_PREDEFINED_ATTRIBUTE = "isPredefined";
   private static final String NEW_IS_LOCAL_ATTRIBUTE = "isLocal";
   private static final String NEW_DEFAULT_VALUE_ATTRIBUTE = "defaultValue";

   private Document targetDocument;
   private HashSet manualActivities = new HashSet();
   private HashMap javaApplications = new HashMap();
   private HashSet jfcApplications = new HashSet();
   private HashSet jspApplications = new HashSet();
   private HashMap applicationActivities = new HashMap();
   private HashMap data = new HashMap();

   private int triggerId = 0;
   private int notificationID=0;

   private OIDProvider oidProvider;

   public UpgradableItem run(UpgradableItem item, boolean recover)
   {
      String model = ((ModelItem) item).getModel();
      InputSource inputSource = null;
      Document source = null;

      inputSource = new InputSource(new StringReader(model));
      URL dtd = ModelItem.class.getResource(MODEL_DTD);
      inputSource.setSystemId(dtd.toString());
      DocumentBuilder domBuilder = XmlUtils.newDomBuilder(true);

      try
      {
         source = domBuilder.parse(inputSource);
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new PublicException(e.getMessage());
      }

      Document result = upgrade(source);

      StringWriter writer = new StringWriter();
      XmlUtils.serialize(result, new StreamResult(writer), MODEL_ENCODING, 2, null,
            MODEL_DTD);
      return new ModelItem(writer.getBuffer().toString());
   }

   public Version getVersion()
   {
      return MODEL_VERSION;
   }

   private void copySubElements(String elementName, Element oldNode, Element newNode,
         Map renameMap)
   {
      NodeList list = oldNode.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         Node el = list.item(i);
         if (el.getNodeType() == Node.ELEMENT_NODE && el.getNodeName().equals(elementName))
         {
            Element newParticipants = (Element) convertNode(el, true, true, renameMap);
            newNode.appendChild(newParticipants);
         }
      }
   }

   private void convertProcesses(Element oldModel, Element newModel)
   {
      NodeList nodeList = oldModel.getOwnerDocument().getElementsByTagName(PROCESS);

      for (int i = 0; i < nodeList.getLength(); i++)
      {
         Element oldProcess = (Element) nodeList.item(i);
         Element newProcess = (Element) convertNode(oldProcess, false, true, null);
         newModel.appendChild(newProcess);

         copySubElements(DESCRIPTION, oldProcess, newProcess, null);

         copySubElements(USERDEFINED_PROPERTY, oldProcess, newProcess, null);

         convertActivities(oldProcess, newProcess);

         copySubElements(TRANSITION, oldProcess, newProcess, null);

         convertDataMappings(oldProcess, newProcess);

         convertDescriptors(oldProcess, newProcess);

         convertTriggers(oldProcess, newProcess);

         copySubElements(DIAGRAM, oldProcess, newProcess, null);

         copySubElements(NOTIFICATION, oldProcess, newProcess, null);

         NodeList notificationCandidates = newProcess.getChildNodes();
         for (int j=0;j<notificationCandidates.getLength();j++)
         {
            Node nc = notificationCandidates.item(j);
            if (nc.getNodeType() == Node.ELEMENT_NODE && nc.getNodeName().equals(NOTIFICATION))
            {
                ((Element)nc).setAttribute(ID_ATT, "notification" + notificationID++);
            }
         }

      }
   }

   private void convertTriggers(Element oldProcess, Element newProcess)
   {
      NodeList list = oldProcess.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         Node oldNode = list.item(i);
         if (oldNode.getNodeType() == Node.ELEMENT_NODE && isTrigger(oldNode.getNodeName()))
         {
            Element oldTrigger = (Element) oldNode;
            Element newTrigger = targetDocument.createElement(TRIGGER);
            String id = "trigger" + triggerId++;
            newTrigger.setAttribute(ID_ATT, id);
            newTrigger.setAttribute(NAME_ATT, id);

            newTrigger.setAttribute(OID_ATT, oldTrigger.getAttribute(OID_ATT));

            if (oldTrigger.getNodeName().equals(TRIGGER_MAIL))
            {
               newTrigger.setAttribute(TYPE_ATT, MAIL_TRIGGER_TYPE_CLASS);

               addChildAttribute(OLD_USER_ATT, USER_ATTRIBUTE, STRING_CLASS_NAME,
                     oldTrigger, newTrigger, null);
               addChildAttribute(OLD_PASSWORD_ATT, PASSWORD_ATTRIBUTE, STRING_CLASS_NAME,
                     oldTrigger, newTrigger, null);
               addChildAttribute(OLD_SERVER_ATT, SERVER_ATTRIBUTE, STRING_CLASS_NAME,
                     oldTrigger, newTrigger, null);
               addChildAttribute(OLD_PROTOCOL_ATT, PROTOCOL_ATTRIBUTE, INTEGER_CLASS_NAME,
                     oldTrigger, newTrigger, new Functor()
                     {
                        public Object execute(Object source)
                        {
                           if (source != null && source.equals("IMAP"))
                           {
                              return "1";
                           }
                           else
                           {
                              return "0";
                           }
                        }
                     });
               addChildAttribute(OLD_SELECTOR_ATT, PREDICATE_ATTRIBUTE, STRING_CLASS_NAME,
                     oldTrigger, newTrigger, null);

               Element accessPoint = targetDocument.createElement(ACCESS_POINT);
               accessPoint.setAttribute(ID_ATT, "mail");
               accessPoint.setAttribute(NAME_ATT, "mail");
               accessPoint.setAttribute(CLASS_ATT, NEW_MAIL_DATA_CLASS);
               accessPoint.setAttribute(NAME_ATT, "mail");
               accessPoint.setAttribute(DIRECTION_ATT, "OUT");

               newTrigger.appendChild(accessPoint);

               Element mapping = targetDocument.createElement(PARAMETER_MAPPING);
               mapping.setAttribute(DATA_ATT, PREDEFINED_MAIL_DATA_ID);
               mapping.setAttribute(PARAMETER_ATT, "mail");
               mapping.setAttribute(PARAMETER_PATH_ATT, "");

               newTrigger.appendChild(mapping);
            }
            else if (oldTrigger.getNodeName().equals(TRIGGER_TIMER_BASED))
            {
               newTrigger.setAttribute(TYPE_ATT, TIMER_TRIGGER_TYPE_CLASS);

               addChildAttribute(OLD_PERIODICITY_TYPE_ATT, PERIODICITY_TYPE_ATT,
                     INTEGER_CLASS_NAME, oldTrigger, newTrigger, null);
               addChildAttribute(OLD_PERIODICITY_FACTOR_ATT, PERIODICITY_FACTOR_ATT,
                     SHORT_CLASS_NAME, oldTrigger, newTrigger, null);
               addChildAttribute(OLD_START_TIMESTAMP_ATT, START_TIMESTAMP_ATT,
                     LONG_CLASS_NAME, oldTrigger, newTrigger, null);
            }
            else if (oldTrigger.getNodeName().equals(TRIGGER_MANUAL))
            {
               newTrigger.setAttribute(TYPE_ATT, MANUAL_TRIGGER_TYPE_CLASS);

               addChildAttribute(OLD_PARTICIPANT_ATT, PARTICIPANT_ATTRIBUTE,
                     STRING_CLASS_NAME, oldTrigger, newTrigger, null);
            }
            newProcess.appendChild(newTrigger);
         }
      }
   }

   private boolean isTrigger(String triggerName)
   {
      return TRIGGER_FAX.equals(triggerName) || TRIGGER_JMS.equals(triggerName)
            || TRIGGER_MAIL.equals(triggerName) || TRIGGER_MANUAL.equals(triggerName)
            || TRIGGER_TIMER_BASED.equals(triggerName);
   }

   private void convertDescriptors(Element oldProcess, Element newProcess)
   {
      NodeList list = oldProcess.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         Node el = list.item(i);
         if (el.getNodeType() == Node.ELEMENT_NODE && el.getNodeName().equals(DESCRIPTOR))
         {
            Element newDescriptor = (Element) convertNode(el, false, true, null);
            newDescriptor.removeAttribute(OID_ATT);
            String path = newDescriptor.getAttribute(PATH_ATT);
            StringTokenizer tokenizer = new StringTokenizer(path, ".");
            if (tokenizer.countTokens() <= 1)
            {

            }
            else
            {
               String newPath = tokenizer.nextToken();
               while (tokenizer.hasMoreTokens())
               {
                  String methodPath = tokenizer.nextToken();
                  if (!methodPath.endsWith("()"))
                  {
                     methodPath = methodPath + "()";
                  }
                  newPath += "." + methodPath;
               }
               newDescriptor.setAttribute(PATH_ATT, newPath);
            }
            newProcess.appendChild(newDescriptor);
         }
      }
   }

   private void convertActivities(Element oldProcess, Element newProcess)
   {
      NodeList list = oldProcess.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         Node el = list.item(i);
         if (el.getNodeType() == Node.ELEMENT_NODE && el.getNodeName().equals(ACTIVITY))
         {
            String implementation_type = ((Element) el).getAttribute(OLD_IMPLEMENTATION_ATT);
            if (OLD_MANUAL_ACTIVITY_TYPE.equals(implementation_type))
            {
               manualActivities.add(oldProcess.getAttribute(ID_ATT) + "|||" +
                     ((Element) el).getAttribute(ID_ATT));
            }
            else if (OLD_APPLICATION_TYPE.equals(implementation_type))
            {
               applicationActivities.put(
                     oldProcess.getAttribute(ID_ATT) + "|||" +
                     ((Element) el).getAttribute(ID_ATT),
                     ((Element) el).getAttribute(OLD_APPLICATION_ATT));
            }
            Element newActivity = (Element) convertNode(el, true, true, null);
            newProcess.appendChild(newActivity);
            // notification id patch
            NodeList notificationCandidates = newActivity.getChildNodes();
            for (int j=0;j<notificationCandidates.getLength();j++)
            {
               Node nc = notificationCandidates.item(j);
               if (nc.getNodeType() == Node.ELEMENT_NODE && nc.getNodeName().equals(NOTIFICATION))
               {
                   ((Element)nc).setAttribute(ID_ATT, "notification" + notificationID++);
               }
            }
         }
      }

      if (oldProcess.getAttribute(ID_ATT).equals("Predefined_User_Management_Process"))
      {
         newProcess.appendChild(createUserManagementImpersonationIdMapping(newProcess, "JFC"));
         newProcess.appendChild(createUserManagementImpersonationPwdMapping(newProcess, "JFC"));

         newProcess.appendChild(createUserManagementImpersonationIdMapping(newProcess, "JSP"));
         newProcess.appendChild(createUserManagementImpersonationPwdMapping(newProcess, "JSP"));
      }
   }

   private void convertDataMappings(Element oldProcess, Element newProcess)
   {
      NodeList applications = oldProcess.getElementsByTagName(DATA_MAPPING);
      for (int i = 0; i < applications.getLength(); i++)
      {
         Element oldMapping = (Element) applications.item(i);

         String type = oldMapping.getAttribute(TYPE_ATT);

         String activityId = oldMapping.getAttribute(ACTIVITY_ATT);
         Element parentProcess = (Element) oldMapping.getParentNode();
         String applicationId = (String) applicationActivities.get(
               parentProcess.getAttribute(ID_ATT) + "|||" + activityId);

         boolean jfcApplication = false;
         boolean jspApplication = false;
         if (jfcApplications.contains(applicationId))
         {
            jfcApplication = true;
         }
         if (jspApplications.contains(applicationId))
         {
            jspApplication = true;
         }

         if (OLD_INOUT_DIRECTION.equals(type))
         {
            createInMappings(jfcApplication, jspApplication, oldMapping, newProcess);
            createOutMappings(jfcApplication, jspApplication, oldMapping, newProcess);
         }
         else if (OLD_IN_DIRECTION.equals(type))
         {
            createInMappings(jfcApplication, jspApplication, oldMapping, newProcess);
         }
         else if (OLD_OUT_DIRECTION.equals(type))
         {
            createOutMappings(jfcApplication, jspApplication, oldMapping, newProcess);
         }
      }
   }

   private void createOutMappings(boolean jfcApplication, boolean jspApplication, Element oldMapping, Element newProcess)
   {
      if (jfcApplication || jspApplication)
      {
         if (jfcApplication)
         {
            setOutMapping(oldMapping, createDataMapping(oldMapping, newProcess), "JFC");
         }
         if (jspApplication)
         {
            setOutMapping(oldMapping, createDataMapping(oldMapping, newProcess), "JSP");
         }
      }
      else
      {
         setOutMapping(oldMapping, createDataMapping(oldMapping, newProcess), null);
      }
   }

   private void createInMappings(boolean jfcApplication, boolean jspApplication, Element oldMapping, Element newProcess)
   {
      if (jfcApplication || jspApplication)
      {
         if (jfcApplication)
         {
            setInMapping(oldMapping, createDataMapping(oldMapping, newProcess), "JFC");
         }
         if (jspApplication)
         {
            setInMapping(oldMapping, createDataMapping(oldMapping, newProcess), "JSP");
         }
      }
      else
      {
         setInMapping(oldMapping, createDataMapping(oldMapping, newProcess), null);
      }
   }

   private void warn(String message, String elementName, String modelId)
   {
      String fullMessage = "Conversion warning for model Element '" + elementName +
            "' with id '" + modelId + "': " + message;
      System.out.println(fullMessage);
      trace.warn(fullMessage);
   }

   private Element createDataMapping(Element oldMapping, Element newProcess)
   {
      Element newMapping = (Element) convertNode(oldMapping, true, false, null);

      newProcess.appendChild(newMapping);

      String activityID = oldMapping.getAttribute(ACTIVITY_ATT);

      if (activityID.length() == 0)
      {
         // check if the model use the old name "ACTIVITY"
         activityID = oldMapping.getAttribute("ACTIVITY");
      }
      if (activityID.length() != 0)
      {
         newMapping.setAttribute(ACTIVITY_ATT, activityID);
      }

      String dataID = oldMapping.getAttribute(DATA_ATT);
      if (dataID.length() == 0)
      {
         // check if the model use the old name "DATA"
         dataID = oldMapping.getAttribute("DATA");
      }
      if (dataID.length() == 0)
      {
         newMapping.setAttribute(ID_ATT, "xxx");
         warn("Found datamapping without dataId", DATA_MAPPING, "xxx");
      }
      else
      {
         newMapping.setAttribute(ID_ATT, dataID);
         newMapping.setAttribute(DATA_ATT, dataID);
      }
      return newMapping;
   }

   private void setOutMapping(Element oldMapping, Element newMapping, String context)
   {
      newMapping.setAttribute(DIRECTION_ATT, OLD_OUT_DIRECTION);

      String activityId = newMapping.getAttribute(ACTIVITY_ATT);
      Element parentProcess = (Element) newMapping.getParentNode();
      String applicationId = (String) applicationActivities.get(
            parentProcess.getAttribute(ID_ATT) + "|||" + activityId);

      if (context != null)
      {
         newMapping.setAttribute(CONTEXT_ATT, context);
      }

      newMapping.setAttribute(DATA_ACCESS_POINT, "this");

      Element processName = (Element) newMapping.getParentNode();
      String encodedActivityId = processName.getAttribute(ID_ATT) + "|||" +
            newMapping.getAttribute(ACTIVITY_ATT);

      if (manualActivities.contains(encodedActivityId))
      {
         String dataOutPath = oldMapping.getAttribute(OLD_DATA_OUT_PATH);
         if (dataOutPath.length() != 0)
         {
            warn("Found data out path for manual activity: " + dataOutPath,
                  DATA_MAPPING, newMapping.getAttribute(ID_ATT));
            newMapping.setAttribute(DATA_PATH, oldMapping.getAttribute(OLD_DATA_OUT_PATH));
         }
         return;
      }

      String applicationClassName = (String) javaApplications.get(applicationId);
      String applicationPath = oldMapping.getAttribute(OLD_APPLICATION_OUT_PATH);
      String newDataPath = null;

      String dataPath = oldMapping.getAttribute(OLD_DATA_OUT_PATH);
      if (dataPath == "")
      {
         newDataPath = dataPath;
      }
      else
      {
         try
         {
            Class applicationClass = Class.forName(applicationClassName);
            Class dataClass = (Class) data.get(newMapping.getAttribute(DATA_ATT));

            newDataPath = convertInPath(dataClass, dataPath,
                  applicationClass, applicationPath);
         }
         catch (ClassNotFoundException e)
         {
            warn("Class not found: " + e.getMessage(), DATA_MAPPING,
                  newMapping.getAttribute(ID_ATT));
            newDataPath = dataPath;
         }
         catch (IHadToTakeAWildGuessException e)
         {
            warn(e.getMessage(), DATA_MAPPING, newMapping.getAttribute(ID_ATT));
            newDataPath = e.getPath();
         }
      }

      newMapping.setAttribute(DATA_PATH, newDataPath);

      String inParameterID = oldMapping.getAttribute(OLD_OUT_PARAMETER_ID);

      int index = applicationPath.indexOf('.');
      if (inParameterID.length() != 0)
      {
         newMapping.setAttribute(APPLICATION_ACCESS_POINT, inParameterID);
         if (index == -1)
         {
            newMapping.setAttribute(APPLICATION_PATH, "");
         }
         else
         {
            newMapping.setAttribute(
                  APPLICATION_PATH, applicationPath.substring(index + 1));
         }
      }
      else
      {
         if (index == -1)
         {
            newMapping.setAttribute(APPLICATION_PATH, "");
            newMapping.setAttribute(APPLICATION_ACCESS_POINT, applicationPath);
         }
         else
         {
            newMapping.setAttribute(
                  APPLICATION_PATH, applicationPath.substring(index + 1));
            newMapping.setAttribute(
                  APPLICATION_ACCESS_POINT, applicationPath.substring(0, index));
         }
      }

      if ("JSP".equals(context))
      {
         newMapping.removeAttribute(APPLICATION_PATH);
         newMapping.removeAttribute(APPLICATION_ACCESS_POINT);
      }
   }

   private void setInMapping(Element oldMapping, Element newMapping, String context)
   {
      newMapping.setAttribute(DIRECTION_ATT, OLD_IN_DIRECTION);
      newMapping.setAttribute(DATA_PATH, oldMapping.getAttribute(OLD_DATA_IN_PATH));
      newMapping.setAttribute(DATA_ACCESS_POINT, "this");

      Element parentProcess = (Element) newMapping.getParentNode();
      String activityId = newMapping.getAttribute(ACTIVITY_ATT);

      String encodedActivityId = parentProcess.getAttribute(ID_ATT) + "|||" + activityId;

      if (manualActivities.contains(encodedActivityId))
      {
         return;
      }

      String applicationId = (String) applicationActivities.get(encodedActivityId);

      if (context != null)
      {
         newMapping.setAttribute(CONTEXT_ATT, context);
      }

      String inParameterID = oldMapping.getAttribute(OLD_IN_PARAMETER_ID);

      String applicationPath = oldMapping.getAttribute(OLD_APPLICATION_IN_PATH);
      if (inParameterID.length() != 0)
      {
         newMapping.setAttribute(APPLICATION_ACCESS_POINT, inParameterID);
         int index = applicationPath.indexOf('.');
         if (index == -1)
         {
            newMapping.setAttribute(APPLICATION_PATH, "");
         }
         else
         {
            newMapping.setAttribute(APPLICATION_PATH, applicationPath.substring(index + 1));
         }
      }
      else
      {

         String applicationClassName = (String) javaApplications.get(applicationId);
         String newApplicationPath = null;

         try
         {
            if (applicationClassName == null)
            {
               throw new ClassNotFoundException();
            }
            Class applicationClass = Class.forName(applicationClassName);

            Class dataClass = (Class) data.get(newMapping.getAttribute(DATA_ATT));
            String dataPath = oldMapping.getAttribute(OLD_DATA_IN_PATH);

            newApplicationPath = convertInPath(
                  applicationClass, applicationPath, dataClass, dataPath);
         }
         catch (ClassNotFoundException e)
         {
            warn("Class not found: " + e.getMessage(), DATA_MAPPING,
                  newMapping.getAttribute(ID_ATT));
            newApplicationPath = applicationPath;
         }
         catch (IHadToTakeAWildGuessException e)
         {
            warn(e.getMessage(), DATA_MAPPING, newMapping.getAttribute(ID_ATT));
            newApplicationPath = e.getPath();
         }

         int index = applicationPath.indexOf('.');
         if (index == -1)
         {
            newMapping.setAttribute(APPLICATION_PATH, "");
            newMapping.setAttribute(APPLICATION_ACCESS_POINT, newApplicationPath);

         }
         else
         {
            newMapping.setAttribute(
                  APPLICATION_PATH, newApplicationPath.substring(index + 1));
            newMapping.setAttribute(
                  APPLICATION_ACCESS_POINT, newApplicationPath.substring(0, index));
         }
      }
      if ("JSP".equals(context))
      {
         newMapping.removeAttribute(APPLICATION_PATH);
         newMapping.removeAttribute(APPLICATION_ACCESS_POINT);
      }

   }

   private Element createUserManagementImpersonationIdMapping(Element newProcess, String context)
   {
      Element mapping = targetDocument.createElement(DATA_MAPPING);

      newProcess.appendChild(mapping);

      mapping.setAttribute(ACTIVITY_ATT, "Predefined_Create_Or_Modify_Users");
      if ("JFC".equals(context))
      {
         mapping.setAttribute(APPLICATION_ACCESS_POINT, "setImpersonationID(java.lang.String)");
      }
      mapping.setAttribute(APPLICATION_PATH, "");
      mapping.setAttribute(CONTEXT_ATT, context);
      mapping.setAttribute(DATA_ATT, "STARTING_USER");
      mapping.setAttribute(DATA_ACCESS_POINT, "this");
      mapping.setAttribute(DATA_PATH, "getAccount()");
      mapping.setAttribute(DIRECTION_ATT, "IN");
      mapping.setAttribute(ID_ATT, "IMPERSONATION_ID");
      mapping.setAttribute(OID_ATT, Long.toString(oidProvider.getOID()));

      return mapping;
   }

   private Element createUserManagementImpersonationPwdMapping(Element newProcess, String context)
   {
      Element mapping = targetDocument.createElement(DATA_MAPPING);

      newProcess.appendChild(mapping);

      mapping.setAttribute(ACTIVITY_ATT, "Predefined_Create_Or_Modify_Users");
      if ("JFC".equals(context))
      {
         mapping.setAttribute(APPLICATION_ACCESS_POINT, "setImpersonationPassword(java.lang.String)");
      }
      mapping.setAttribute(APPLICATION_PATH, "");
      mapping.setAttribute(CONTEXT_ATT, context);
      mapping.setAttribute(DATA_ATT, "STARTING_USER");
      mapping.setAttribute(DATA_ACCESS_POINT, "this");
      mapping.setAttribute(DATA_PATH, "getAccount()");
      mapping.setAttribute(DIRECTION_ATT, "IN");
      mapping.setAttribute(ID_ATT, "IMPERSONATION_PWD");
      mapping.setAttribute(OID_ATT, Long.toString(oidProvider.getOID()));

      return mapping;
   }

   private String convertInPath(Class clazz, String path,
         Class dualClazz, String dualPath)
         throws ClassNotFoundException, IHadToTakeAWildGuessException
   {
      if (clazz == null)
      {
         throw new ClassNotFoundException();
      }
      String setterName;
      String getterPath;
      int ix = path.lastIndexOf('.');
      if (ix == -1)
      {
         setterName = path;
         getterPath = "";
      }
      else
      {
         setterName = path.substring(ix + 1);
         getterPath = path.substring(0, ix + 1);
         clazz = getEndClass(clazz, path.substring(0, ix));

      }
      int idx = setterName.indexOf('(');
      if (idx != -1)
      {
         setterName = setterName.substring(0, idx);
      }
      LinkedList matches = new LinkedList();
      Method[] methods = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         if (method.getName().equals(setterName))
         {
            if (method.getParameterTypes().length == 1)
            {
               matches.add(method);
            }
         }
      }
      if (matches.size() == 1)
      {
         return getterPath + encodeMethod((Method) matches.getFirst());
      }
      else if (matches.size() > 1)
      {
         if (dualClazz == null)
         {
            throw new IHadToTakeAWildGuessException("No dual class found.",
                  getterPath + encodeMethod((Method) matches.getFirst()));
         }

         Iterator itr = matches.iterator();
         while (itr.hasNext())
         {
            Method method = (Method) itr.next();
            Class parameter = method.getParameterTypes()[0];

            Class endClass = getEndClass(dualClazz, dualPath);

            if (parameter.isAssignableFrom(endClass))
            {
               return getterPath + encodeMethod(method);
            }
         }
      }
      throw new IHadToTakeAWildGuessException(
            "The application method is not accessible", path);
   }

   private void convertApplications(Element oldModel, Element newModel)
   {
      NodeList nodeList = oldModel.getOwnerDocument().getElementsByTagName(APPLICATION);

      for (int j = 0; j < nodeList.getLength(); j++)
      {
         LinkedList additionalProperties = new LinkedList();

         Element oldApplication = (Element) nodeList.item(j);

         // @todo/belgium (ub): maybe convert screen keys / screen entries
         Element newApplication = (Element) convertNode(oldApplication, true, false, null);
         newModel.appendChild(newApplication);

         String value = null;

         // convert old attributes

         rewriteAttribute(ID_ATT, oldApplication, newApplication);
         rewriteAttribute(OID_ATT, oldApplication, newApplication);
         rewriteAttribute(NAME_ATT, oldApplication, newApplication);
         rewriteAttribute(COST_CENTER, oldApplication, newApplication);
         rewriteAttribute(ACTUAL_COST_PER_SECOND, oldApplication, newApplication);

         String oldContext = oldApplication.getAttribute(OLD_USER_DEF_APP_TYPE);

         value = oldApplication.getAttribute(TYPE_ATT);

         if (value.length() != 0)
         {
            if (value.equals(OLD_TYPE_SESSION_BEAN))
            {
               newApplication.setAttribute(TYPE_ATT, NEW_TYPE_SESSION_BEAN);
               addSessionBeanAttributes(oldApplication, newApplication);

            }
            else if (value.equals(OLD_TYPE_USER_DEFINED))
            {
               newApplication.setAttribute(INTERACTIVE_ATT, "true");

               if (oldContext.equals(OLD_USER_TYPE_HTML))
               {
                  addJSPAttributes(oldApplication, newApplication);
               }
               else if (oldContext.equals(OLD_USER_TYPE_JFC))
               {
                  addJFCAttributes(oldApplication, newApplication);

               }
               else if (oldContext.equals(OLD_USER_TYPE_BOTH))
               {
                  addJSPAttributes(oldApplication, newApplication);
                  addJFCAttributes(oldApplication, newApplication);

               }
            }
            else if (value.equals(OLD_TYPE_CICS))
            {
               newApplication.setAttribute(TYPE_ATT, NEW_TYPE_CICS);
               addHostAttributes(oldApplication, newApplication);
            }
            else
            {
               warn("Found unknown application type : " + value,
                     APPLICATION, newApplication.getAttribute(ID_ATT));
            }
         }

         // @todo/belgium (ub)
         //patch for old models which have false appType if defined as both.

         /*
         if (_application.getUserDefinedApplicationType().equals(UserDefinedApplicationTypeKey.HTML)
               && _application.getApplicationType().equals(ApplicationTypeKey.USER_DEFINED)
               && _application.getClassName() != null)
         {
            _application.setUserDefinedApplicationType(new UserDefinedApplicationTypeKey(
                  UserDefinedApplicationTypeKey.BOTH));
         }
         */


         copySubElements(USERDEFINED_PROPERTY, oldApplication, newApplication, null);

         for (Iterator i = additionalProperties.iterator(); i.hasNext();)
         {
            UserDefinedProperty property = (UserDefinedProperty) i.next();
            addUserDefinedProperty(newApplication, property);
         }

         String type = newApplication.getAttribute(TYPE_ATT);
         String interactive = newApplication.getAttribute(INTERACTIVE_ATT);
         if (NEW_TYPE_SESSION_BEAN.equals(type) || "true".equals(interactive))
         {
            // cache java-like applications for later data mapping conversion
            javaApplications.put(oldApplication.getAttribute(ID_ATT),
                  oldApplication.getAttribute(OLD_REFERENCE_CLASS));
            if ("true".equals(interactive))
            {
               if (oldContext.equals(OLD_USER_TYPE_HTML))
               {
                  jspApplications.add(oldApplication.getAttribute(ID_ATT));
               }
               else if (oldContext.equals(OLD_USER_TYPE_JFC))
               {
                  jfcApplications.add(oldApplication.getAttribute(ID_ATT));
               }
               else if (oldContext.equals(OLD_USER_TYPE_BOTH))
               {
                  jfcApplications.add(oldApplication.getAttribute(ID_ATT));
                  jspApplications.add(oldApplication.getAttribute(ID_ATT));
               }
            }
         }
      }
   }

   private void addHostAttributes(Element oldApplication, Element newApplication)
   {
      addChildAttribute(OLD_SCREEN_ID_POS, SCREEN_ID_POS_PROPERTY,
            STRING_CLASS_NAME, oldApplication, newApplication, null);

      addChildAttribute(OLD_INTERACTIVE_EMULATION, INTERACTIVE_EMULATION_PROPERTY,
            BOOLEAN_CLASS_NAME, oldApplication, newApplication, null);
   }

   private void addSessionBeanAttributes(Element oldApplication, Element newApplication)
   {
      addChildAttribute(OLD_REFERENCE_CLASS, NEW_CLASS_NAME_ATTRIBUTE,
            STRING_CLASS_NAME, oldApplication, newApplication, null);

      addChildAttribute(OLD_REFERENCE_METHOD, METHOD_NAME_PROPERTY,
            STRING_CLASS_NAME, oldApplication, newApplication, null);

      addChildAttribute(OLD_JNDI_HOME, NEW_JNDI_PATH_ATTRIBUTE,
            STRING_CLASS_NAME, oldApplication, newApplication, null);

      addChildAttribute(OLD_LOCAL, ISLOCAL_PROPERTY,
            BOOLEAN_CLASS_NAME, oldApplication, newApplication, null);
   }

   private void addJFCAttributes(Element oldApplication, Element newApplication)
   {
      Element contextElement = targetDocument.createElement(CONTEXT);
      newApplication.appendChild(contextElement);
      contextElement.setAttribute(ID_ATT, "JFC");

      addChildAttribute(OLD_REFERENCE_CLASS, NEW_CLASS_NAME_ATTRIBUTE,
            STRING_CLASS_NAME, oldApplication, contextElement, null);

      addChildAttribute(OLD_REFERENCE_METHOD, METHOD_NAME_PROPERTY,
            STRING_CLASS_NAME, oldApplication, contextElement, null);
   }

   private void addJSPAttributes(Element oldApplication, Element newApplication)
   {
      Element contextElement = targetDocument.createElement(CONTEXT);
      newApplication.appendChild(contextElement);
      contextElement.setAttribute(ID_ATT, "JSP");

      addChildAttribute(OLD_HTML_HOME, HTML_PATH_PROPERTY,
            STRING_CLASS_NAME, oldApplication, contextElement, null);
   }

   private void rewriteAttribute(String name, Element oldElement, Element newElement)
   {
      String value = oldElement.getAttribute(name);
      if (value.length() != 0)
      {
         newElement.setAttribute(name, value);
      }
   }

   private void addUserDefinedProperty(Element element, UserDefinedProperty property)
   {
      Element newProperty = targetDocument.createElement(USERDEFINED_PROPERTY);
      newProperty.setAttribute(NAME_ATT, property.getName());
      newProperty.setAttribute(CLASS_ATT, property.getType());
      newProperty.setAttribute(VALUE_ATT, property.getValue());
      element.appendChild(newProperty);
   }

   private void addChildAttribute(String oldName, String newName, String type,
         Element oldElement, Element targetElement, Functor converter, boolean doInsert)
   {
      String value = oldElement.getAttribute(oldName);

      if (value.length() != 0)
      {
         if (converter != null)
         {
            value = (String) converter.execute(value);
         }

         if (doInsert)
         {
            insertChildAttributeAsFirst(newName, type, value, targetElement);
         }
         else
         {
            addChildAttribute(newName, type, value, targetElement);
         }
      }
   }

   private void addChildAttribute(String oldName, String newName, String type,
         Element oldElement, Element targetElement, Functor converter)
   {
      addChildAttribute(
            oldName, newName, type, oldElement, targetElement, converter, false);
   }

   private void addChildAttribute(String newName, String type,
         Object value, Element targetElement)
   {
      Element propertyElement = targetDocument.createElement(ATTRIBUTE);
      targetElement.appendChild(propertyElement);
      propertyElement.setAttribute(NAME_ATT, newName);
      propertyElement.setAttribute(CLASS_ATT, type);
      propertyElement.setAttribute(VALUE_ATT, value.toString());
   }

   private void insertChildAttributeAsFirst(String newName, String type,
         Object value, Element targetElement)
   {
      Node refChild = targetElement.getFirstChild();
      Element propertyElement = targetDocument.createElement(ATTRIBUTE);
      targetElement.insertBefore(propertyElement, refChild);
      propertyElement.setAttribute(NAME_ATT, newName);
      propertyElement.setAttribute(CLASS_ATT, type);
      propertyElement.setAttribute(VALUE_ATT, value.toString());
   }

   private void convertData(Element oldModel, Element newModel)
   {
      NodeList nodeList = oldModel.getOwnerDocument().getElementsByTagName(DATA);

      for (int i = 0; i < nodeList.getLength(); i++)
      {
         final Element oldData = (Element) nodeList.item(i);

         if ("TRIGGER_XML_CONTENT".equals(oldData.getAttribute(ID_ATT)))
         {
            continue;
         }

         String type = oldData.getAttribute(TYPE_ATT);

         // converting from very old style
         if (OLD_TYPE_KEY_BOOLEAN.equals(type))
         {
            type = NEW_TYPE_KEY_BOOLEAN;
         }
         else if (OLD_TYPE_KEY_CHARACTER.equals(type))
         {
            type = NEW_TYPE_KEY_CHARACTER;
         }
         else if (OLD_TYPE_KEY_BYTE.equals(type))
         {
            type = NEW_TYPE_KEY_BYTE;
         }
         else if (OLD_TYPE_KEY_SHORT.equals(type))
         {
            type = NEW_TYPE_KEY_SHORT;
         }
         else if (OLD_TYPE_KEY_INTEGER.equals(type))
         {
            type = NEW_TYPE_KEY_INTEGER;
         }
         else if (OLD_TYPE_KEY_LONG.equals(type))
         {
            type = NEW_TYPE_KEY_LONG;
         }
         else if (OLD_TYPE_KEY_FLOAT.equals(type))
         {
            type = NEW_TYPE_KEY_FLOAT;
         }
         else if (OLD_TYPE_KEY_DOUBLE.equals(type))
         {
            type = NEW_TYPE_KEY_DOUBLE;
         }

         // fixing trigger_email

         if (NEW_TYPE_KEY_ENTITY_BEAN.equals(type)
               && oldData.getAttribute(CLASS_ATT).equals(OLD_MAIL_DATA_CLASS))
         {
            oldData.setAttribute(CLASS_ATT, NEW_MAIL_DATA_CLASS);
            type = NEW_TYPE_KEY_SERIALIZABLE;
         }

         Element newData = (Element) convertNode(oldData, true, false, null);
         newData.setAttribute(ID_ATT, oldData.getAttribute(ID_ATT));
         newData.setAttribute(NAME_ATT, oldData.getAttribute(ID_ATT));
         newData.setAttribute(OID_ATT, oldData.getAttribute(OID_ATT));
         if (NEW_TYPE_KEY_ENTITY_BEAN.equals(type))
         {
            newData.setAttribute(TYPE_ATT, "ag.carnot.workflow.EntityBeanDataType");
         }
         else
         {
            newData.setAttribute(TYPE_ATT, "ag.carnot.workflow.SerializableDataType");
         }

         newModel.appendChild(newData);

         addChildAttribute(CLASS_ATT, NEW_CLASS_NAME_ATTRIBUTE,
               STRING_CLASS_NAME, oldData, newData, null, true);
         insertChildAttributeAsFirst(TYPE_ATT, "ag.carnot.workflow.TypeKey", type, newData);

         if (NEW_TYPE_KEY_ENTITY_BEAN.equals(type))
         {
            insertChildAttributeAsFirst(NEW_HOME_INTERFACE_ATTRIBUTE, STRING_CLASS_NAME,
                  oldData.getAttribute(CLASS_ATT) + "Home", newData);
            insertChildAttributeAsFirst(NEW_PRIMARY_KEY_ATTRIBUTE, STRING_CLASS_NAME,
                  oldData.getAttribute(CLASS_ATT) + "PK", newData);
            addChildAttribute(OLD_JNDI_PATH_ATT, NEW_JNDI_PATH_ATTRIBUTE,
                  STRING_CLASS_NAME, oldData, newData, null, true);
            addChildAttribute(OLD_LOCAL_ATT, NEW_IS_LOCAL_ATTRIBUTE,
                  BOOLEAN_CLASS_NAME, oldData, newData, null, true);
         }
         else
         {
            addChildAttribute(DEFAULT_VALUE, NEW_DEFAULT_VALUE_ATTRIBUTE,
                  STRING_CLASS_NAME, oldData, newData, null, true);
         }
         addChildAttribute(IS_PREDEFINED, NEW_IS_PREDEFINED_ATTRIBUTE,
                  BOOLEAN_CLASS_NAME, oldData, newData, null, true);


         // cache for usage in data mapping conversion

         Class dataType = null;

         if (NEW_TYPE_KEY_SERIALIZABLE.equals(type)
               || NEW_TYPE_KEY_ENTITY_BEAN.equals(type))
         {
            try
            {
               dataType = Class.forName(oldData.getAttribute(CLASS_ATT));
            }
            catch (ClassNotFoundException e)
            {
               warn("Class not found: " + e.getMessage(), DATA,
                     newData.getAttribute(ID_ATT));
            }
         }
         else if (NEW_TYPE_KEY_BOOLEAN.equals(type))
         {
            dataType = Boolean.class;
         }
         else if (NEW_TYPE_KEY_CHARACTER.equals(type))
         {
            dataType = Character.class;
         }
         else if (NEW_TYPE_KEY_BYTE.equals(type))
         {
            dataType = Byte.class;
         }
         else if (NEW_TYPE_KEY_SHORT.equals(type))
         {
            dataType = Short.class;
         }
         else if (NEW_TYPE_KEY_INTEGER.equals(type))
         {
            dataType = Integer.class;
         }
         else if (NEW_TYPE_KEY_LONG.equals(type))
         {
            dataType = Long.class;
         }
         else if (NEW_TYPE_KEY_FLOAT.equals(type))
         {
            dataType = Float.class;
         }
         else if (NEW_TYPE_KEY_DOUBLE.equals(type))
         {
            dataType = Double.class;
         }
         else if (NEW_TYPE_KEY_STRING.equals(type))
         {
            dataType = String.class;
         }
         else if (NEW_TYPE_KEY_DATE.equals(type))
         {
            dataType = Calendar.class;
         }
         else if (NEW_TYPE_KEY_MONEY.equals(type))
         {
            dataType = Money.class;
         }
         else
         {
            warn("Unknown type: " + type, DATA, newData.getAttribute(ID_ATT));
         }

         data.put(newData.getAttribute(ID_ATT), dataType);
      }
   }

   public Document upgrade(Document document)
   {
      try
      {
         this.oidProvider = new OIDProvider(document);

         Element oldModel = document.getDocumentElement();
         targetDocument = XmlUtils.newDocument();

         Element newModel = (Element) convertNode(oldModel, false, true, null);
         newModel.setAttribute(CARNOT_XML_VERSION, MODEL_VERSION.toString());
         newModel.removeAttribute("wpdl-version");

         targetDocument.appendChild(newModel);

         copySubElements(DESCRIPTION, oldModel, newModel, null);

         copySubElements(USERDEFINED_PROPERTY, oldModel, newModel, null);

         convertData(oldModel, newModel);

         convertApplications(oldModel, newModel);

         HashMap renameMap = new HashMap();
         renameMap.put("HUMAN", "MODELER");

         copySubElements(PARTICIPANTS, oldModel, newModel, renameMap);

         copySubElements(LINK_TYPE, oldModel, newModel, null);

         copySubElements(GENERIC_LINK, oldModel, newModel, null);

         convertProcesses(oldModel, newModel);

         HashMap renameSymbolsMap = new HashMap();
         renameMap.put("HUMAN_SYMBOL", "MODELER_SYMBOL");
         copySubElements(DIAGRAM, oldModel, newModel, renameSymbolsMap);

         copySubElements(VIEW, oldModel, newModel, null);

         return targetDocument;
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   private Node convertNode(Node source, boolean deep, boolean copyAttributes, Map renameMap)
   {
      NodeList children = source.getChildNodes();
      Node result = null;
      String name = source.getNodeName();
      short type = source.getNodeType();
      String value = source.getNodeValue();
      if (type == Node.ELEMENT_NODE)
      {
         String newElementName = null;
         if (renameMap != null && renameMap.containsKey(source.getNodeName()))
         {
            newElementName = (String) renameMap.get(source.getNodeName());
         }

         if (newElementName != null)
         {
            result = targetDocument.createElement(newElementName);
         }
         else
         {
            result = targetDocument.createElement(name);
         }
         if (copyAttributes)
         {
            NamedNodeMap attributes = source.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++)
            {
               Attr attribute = (Attr) attributes.item(i);
               ((Element) result).setAttribute(attribute.getName(), attribute.getValue());
            }
         }
      }
      else if (type == Node.TEXT_NODE)
      {
         result = targetDocument.createTextNode(value);
      }
      else if (type == Node.CDATA_SECTION_NODE)
      {
         result = targetDocument.createCDATASection(value);
      }
      else
      {
         return null;
      }
      if (!deep)
      {
         return result;
      }
      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);
         Node converted = convertNode(child, true, true, renameMap);
         if (converted != null)
         {
            result.appendChild(converted);
         }
      }
      return result;
   }

   private Class getEndClass(Class startType, String encodedPath)
   {

      StringTokenizer tokenizer = new StringTokenizer(encodedPath, ".");
      List pathElements = CollectionUtils.newList();

      while (tokenizer.hasMoreTokens())
      {
         String token = tokenizer.nextToken();
         pathElements.add(token);
      }

      Class currentType = startType;

      Iterator e = pathElements.iterator();

      while (e.hasNext())
      {
         String element = (String) e.next();

         try
         {
            currentType = decodeMethod(currentType, element).getReturnType();
         }
         catch (InternalException x)
         {
            throw new InternalException(
                  "Method '" + element + "' not available or not accessible.");
         }
      }
      return currentType;
   }

   private static Method decodeMethod(Class type, String encodedMethod)
   {
      int _startIndex = encodedMethod.indexOf('(');
      int _endIndex = encodedMethod.indexOf(')');

      if (_startIndex < 0)
      {
         try
         {
            return type.getMethod(encodedMethod);
         }
         catch (Exception e)
         {
            throw new InternalException(
                  "Method " + encodedMethod + " in class " + type.getName()
                  + " cannot be found or accessed.");
         }
      }
      else if (encodedMethod.substring(_startIndex + 1, _endIndex).length() == 0)
      {
         try
         {
            return type.getMethod(encodedMethod.substring(0, _startIndex));
         }
         catch (Exception e)
         {
            throw new InternalException(
                  "Method " + encodedMethod + " in class " + type.getName()
                  + " cannot be found or accessed.");
         }
      }

      String _parameterString = encodedMethod.substring(_startIndex + 1, _endIndex);
      ArrayList _arrayList = new ArrayList();
      String _token = null;

      StringTokenizer tokenizer = new StringTokenizer(_parameterString, ",", false);

      while (tokenizer.hasMoreTokens())
      {
         _token = tokenizer.nextToken();

         try
         {
            String _className = _token.trim();
            if (_className.equals("boolean"))
            {
               _arrayList.add(boolean.class);
            }
            else if (_className.equals("char"))
            {
               _arrayList.add(char.class);
            }
            else if (_className.equals("float"))
            {
               _arrayList.add(float.class);
            }
            else if (_className.equals("double"))
            {
               _arrayList.add(double.class);
            }
            else if (_className.equals("int"))
            {
               _arrayList.add(int.class);
            }
            else if (_className.equals("short"))
            {
               _arrayList.add(short.class);
            }
            else if (_className.equals("byte"))
            {
               _arrayList.add(byte.class);
            }
            else if (_className.equals("long"))
            {
               _arrayList.add(long.class);
            }
            else
            {
               _arrayList.add(Class.forName(_token.trim()));
            }
         }
         catch (ClassNotFoundException e)
         {
            throw new InternalException(
                  "Class " + _token.trim() + " for parameter not found.(" + encodedMethod + ")");
         }
      }

      Class[] _classes = new Class[_arrayList.size()];

      for (int n = 0; n < _arrayList.size(); ++n)
      {
         _classes[n] = (Class) _arrayList.get(n);
      }

      try
      {
         String methodName = encodedMethod.substring(0, _startIndex);
         return type.getMethod(methodName, _classes);
      }
      catch (Exception e)
      {
         throw new InternalException(
               "Method " + encodedMethod + " in class " + type.getName()
               + " cannot be found or accessed.\n" + e.getMessage());
      }
   }

   private static String encodeMethod(Method method)
   {
      Assert.isNotNull(method, "Method is not null.");

      Class[] _parameterTypes = method.getParameterTypes();

      StringBuffer _buffer = new StringBuffer(method.getName());

      _buffer.append('(');

      for (int n = 0; n < _parameterTypes.length; ++n)
      {
         if (n != 0)
         {
            _buffer.append(", ");
         }

         _buffer.append(_parameterTypes[n].getName());
      }

      _buffer.append(')');

      return _buffer.toString();
   }

   /**
    *  A wrapper around the EntityResolver of the parser to find the
    * <code>WorkflowModel.dtd</code> by means of the carnot parameter
    * <code>Model.Dtd</code>.
    */
   class MyEntityResolver implements EntityResolver
   {
      private EntityResolver parent;

      MyEntityResolver(EntityResolver parent_)
      {
         parent = parent_;
      }

      public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
      {
         if (!systemId.endsWith(MODEL_DTD))
         {
            return parent.resolveEntity(publicId, systemId);
         }
         URL dtdURL = M2_5_0fromPre2_5_0Converter.class.getResource(MODEL_DTD);

         if (dtdURL == null)
         {
            throw new InternalException("Unable to find entity '" + MODEL_DTD + "'.");
         }
         InputSource inputSource = new InputSource();
         inputSource.setSystemId(dtdURL.toString());

         return inputSource;
      }
   }

   private static class UserDefinedProperty
   {
      String name;
      String type;
      String value;

      public String getName()
      {
         return name;
      }

      public String getType()
      {
         return type;
      }

      public String getValue()
      {
         return value;
      }

      public UserDefinedProperty(String name, String type, String value)
      {
         this.name = name;
         this.type = type;
         this.value = value;
      }
   }

   private static class IHadToTakeAWildGuessException extends Exception
   {
      private String path;

      public IHadToTakeAWildGuessException(String message, String path)
      {
         super(message);
         this.path = path;
      }

      public String getPath()
      {
         return path;
      }
   }
}
