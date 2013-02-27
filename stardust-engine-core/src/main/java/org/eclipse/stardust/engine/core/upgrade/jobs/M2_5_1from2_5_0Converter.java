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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelItem;
import org.eclipse.stardust.engine.core.upgrade.framework.ModelUpgradeJob;
import org.eclipse.stardust.engine.core.upgrade.framework.UpgradableItem;
import org.w3c.dom.*;
import org.xml.sax.InputSource;


/**
 * @author rsauer
 * @version $Revision$
 */
public class M2_5_1from2_5_0Converter extends ModelUpgradeJob
{
   private static final Logger trace =
         LogManager.getLogger(M2_5_1from2_5_0Converter.class);

   private static final Version MODEL_VERSION = Version.createFixedVersion(2, 5, 1);

   private static final String MODEL_ENCODING = "ISO-8859-1";
   private static final String MODEL_DTD = "WorkflowModel.dtd";

   private static final String CARNOT_XML_VERSION = "carnot_xml_version";

   private static final String ACTIVITY = "ACTIVITY";
   private static final String APPLICATION = "APPLICATION";
   private static final String DATA = "DATA";
   private static final String DATA_MAPPING = "DATA_MAPPING";
   private static final String DESCRIPTION = "DESCRIPTION";
   private static final String DESCRIPTOR = "DESCRIPTOR";
   private static final String DIAGRAM = "DIAGRAM";
   private static final String GENERIC_LINK = "GENERIC_LINK";
   private static final String LINK_TYPE = "LINK_TYPE";
   private static final String NOTIFICATION = "NOTIFICATION";
   private static final String PARTICIPANTS = "PARTICIPANTS";
   private static final String PROCESS = "WORKFLOW";
   private static final String USERDEFINED_PROPERTY = "USERDEFINED_PROPERTY";
   private static final String TRANSITION = "TRANSITION";
   private static final String TRIGGER = "TRIGGER";
   private static final String VIEW = "VIEW";

   private static final String ID_ATT = "id";
   private static final String FORCE_ASSIGNMENT_TO_HUMAN_ATTR =
         "force_assignment_to_human";

   private static final String USER_MANAGEMENT_PROCESS_ID =
         "Predefined_User_Management_Process";

   private Document targetDocument;

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

   private Document upgrade(Document document)
   {
      try
      {
         Element oldModel = document.getDocumentElement();
         targetDocument = XmlUtils.newDocument();

         Element newModel = (Element) convertNode(oldModel, false, true, null);
         newModel.setAttribute(CARNOT_XML_VERSION, MODEL_VERSION.toString());

         targetDocument.appendChild(newModel);

         copySubElements(DESCRIPTION, oldModel, newModel, null);

         copySubElements(DATA, oldModel, newModel, null);

         copySubElements(APPLICATION, oldModel, newModel, null);

         copySubElements(PARTICIPANTS, oldModel, newModel, null);

         convertProcesses(oldModel, newModel);

         copySubElements(DIAGRAM, oldModel, newModel, null);

         copySubElements(LINK_TYPE, oldModel, newModel, null);

         copySubElements(GENERIC_LINK, oldModel, newModel, null);

         copySubElements(USERDEFINED_PROPERTY, oldModel, newModel, null);

         copySubElements(VIEW, oldModel, newModel, null);

         return targetDocument;
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new InternalException(e);
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

         convertActivities(oldProcess, newProcess);

         copySubElements(TRANSITION, oldProcess, newProcess, null);

         copySubElements(DATA_MAPPING, oldProcess, newProcess, null);

         copySubElements(TRIGGER, oldProcess, newProcess, null);

         copySubElements(DESCRIPTOR, oldProcess, newProcess, null);

         copySubElements(DIAGRAM, oldProcess, newProcess, null);

         copySubElements(USERDEFINED_PROPERTY, oldProcess, newProcess, null);

         copySubElements(NOTIFICATION, oldProcess, newProcess, null);
      }
   }

   private void convertActivities(Element oldProcess, Element newProcess)
   {
      NodeList list = oldProcess.getElementsByTagName(ACTIVITY);
      for (int i = 0; i < list.getLength(); i++)
      {
         Node el = list.item(i);
         Element newNode = (Element) convertNode(el, true, true, null);

         if (oldProcess.getAttribute(ID_ATT).equals(USER_MANAGEMENT_PROCESS_ID))
         {
            newNode.setAttribute(FORCE_ASSIGNMENT_TO_HUMAN_ATTR, "false");
         }

         newProcess.appendChild(newNode);
      }
   }

   private Node convertNode(Node source, boolean deep, boolean copyAttributes,
         Map renameMap)
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

   private void copySubElements(String elementName, Element oldNode, Element newNode,
         Map renameMap)
   {
      NodeList list = oldNode.getChildNodes();
      for (int i = 0; i < list.getLength(); i++)
      {
         Node el = list.item(i);
         if (el.getNodeType() == Node.ELEMENT_NODE
               && el.getNodeName().equals(elementName))
         {
            Element newParticipants = (Element) convertNode(el, true, true, renameMap);
            newNode.appendChild(newParticipants);
         }
      }
   }
}
