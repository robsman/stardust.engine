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
package org.eclipse.stardust.engine.core.upgrade.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class FileModel
{
   protected Document doc;

   protected abstract void createPrimitiveDataType(String id, String name, String description, long oid, Type calendar);

   protected abstract void setPrimitiveDataType(String id, Type type);

   protected List<Element> getElements(String... path)
   {
      List<Element> result = Collections.emptyList();
      Element node = doc.getDocumentElement();
      if (path[0].equals(node.getLocalName()))
      {
         for (int i = 1; i < path.length; i++)
         {
            result = getElementsByTagName(node, path[i]);
            if (result.size() > 0)
            {
               node = result.get(0);
            }
         }
      }
      return result;
   }

   protected List<Element> getElements(Element node, String... path)
   {
      List<Element> result = Collections.emptyList();
      for (int i = 0; i < path.length; i++)
      {
         result = getElementsByTagName(node, path[i]);
         if (result.size() > 0)
         {
            node = result.get(0);
         }
      }
      return result;
   }

   private List<Element> getElementsByTagName(Element root, String tag)
   {
      List<Element> result = new ArrayList<Element>();
      NodeList nodes = root.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         if (node instanceof Element && tag.equals(((Element) node).getLocalName()))
         {
            result.add((Element) node);
         }
      }
      return result;
   }

   public String toString()
   {
      return XmlUtils.toString(doc);
   }

   public static final FileModel create(String model)
   {
      FileModel fileModel = null;

      Document doc = XmlUtils.parseString(model);
      Element root = doc.getDocumentElement();
      if ("Package".equals(root.getLocalName()))
      {
         fileModel = new XpdlModel();
      }
      else if ("model".equals(root.getLocalName()))
      {
         fileModel = new CwmModel();
      }
      else
      {
         throw new PublicException("Unsupported model type");
      }
      fileModel.doc = doc;
      return fileModel;
   }

   public static class CwmModel extends FileModel
   {
      @Override
      protected void setPrimitiveDataType(String id, Type type)
      {
         List<Element> dataFields = getElements("model", "data");
         for (Element dataField : dataFields)
         {
            if (id.equals(dataField.getAttribute("id")))
            {
               List<Element> attributes = getElements(dataField, "attribute");
               for (Element attribute : attributes)
               {
                  if ("carnot:engine:type".equals(attribute.getAttribute("name")))
                  {
                     attribute.setAttribute("value", type.toString());
                  }
               }
            }
         }
      }

      @Override
      protected void createPrimitiveDataType(String id, String name, String description,
            long oid, Type calendar)
      {
         // TODO Auto-generated method stub

      }
   }

   public static class XpdlModel extends FileModel
   {
      @Override
      protected void createPrimitiveDataType(String id, String name, String description,
            long oid, Type type)
      {
         Element dataField = doc.createElementNS(XMLConstants.NS_XPDL_2_1, "DataField");
         dataField.setAttribute("Id", id);
         dataField.setAttribute("Name", name);
         dataField.setAttribute("IsArray", "FALSE");
         Element dataType = doc.createElementNS(XMLConstants.NS_XPDL_2_1, "DataType");
         Element basicType = doc.createElementNS(XMLConstants.NS_XPDL_2_1, "BasicType");
         basicType.setAttribute("Type", getXpdlBasicType(type));
         dataType.appendChild(basicType);
         dataField.appendChild(dataType);
         Element extendedAttributes = doc.createElementNS(XMLConstants.NS_XPDL_2_1, "ExtendedAttributes");
         dataField.appendChild(extendedAttributes);

         List<Element> dataFields = getElements("Package", "DataFields");
         Element parent = dataFields.get(0);
         parent.appendChild(dataField);
         //parent.
      }

      @Override
      protected void setPrimitiveDataType(String id, Type type)
      {
         List<Element> dataFields = getElements("Package", "DataFields", "DataField");
         for (Element dataField : dataFields)
         {
            if (id.equals(dataField.getAttribute("Id")))
            {
               List<Element> attributes = getElements(dataField, "ExtendedAttributes", "ExtendedAttribute", "DataField", "Attributes", "Attribute");
               for (Element attribute : attributes)
               {
                  if ("carnot:engine:type".equals(attribute.getAttribute("Name")))
                  {
                     attribute.setAttribute("Value", type.toString());
                  }
               }
            }
         }
      }

      private String getXpdlBasicType(Type type)
      {
         if (Type.Timestamp == type)
         {
            return "DATETIME";
         }
         else if (Type.Calendar == type)
         {
            return "DATETIME";
         }
         // TODO: (fh) support other conversions
         return "";
      }
   }
}
