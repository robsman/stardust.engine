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
package org.eclipse.stardust.engine.core.model.convert.aris;

import java.util.HashMap;
import java.util.List;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.JoinSplitType;
import org.eclipse.stardust.engine.core.model.convert.XMLUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;


/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class SimpleTaskUtil extends XMLUtil
{
   public static String[] getAttrDefValues(Node node, String[] attributeNames)
   {
      String[] retVal;
      NodeList children;
      NamedNodeMap attributes;

      retVal = new String[attributeNames.length];
      children = node.getChildNodes();

      for (int childPos = 0; childPos < children.getLength(); childPos++)
      {
         if (children.item(childPos).getNodeName().equals(Aris61Converter.ATTRIBUTE_NODE_NAME))
         {
            attributes = children.item(childPos).getAttributes();

            if (attributes != null)
            {
               for (int attrPos = 0; attrPos < attributeNames.length; attrPos++)
               {
                  Node attribute = attributes.getNamedItem(Aris61Converter.NAME_ATTRIBUTE);

                  if (attribute != null)
                  {
                     retVal[attrPos] = attribute.getNodeValue();
                  }

               }
            }
         }
      }

      return retVal;
   }

   public static String[] getIdAndName(Element node)
   {
      String[] retVal = new String[2];
      retVal[0] = node.getAttribute(node.getTagName() + "." + Aris61Converter.ID_ATTRIBUTE);

      List attributes = getElementsByName(node, Aris61Converter.ATTRIBUTE_NODE_NAME);
      for (int i = 0; i < attributes.size(); i++)
      {
         Element attrTag = (Element) attributes.get(i);
         String type = attrTag.getAttribute(Aris61Converter.ATTRIBUTE_NODE_TYPE);
         if (Aris61Converter.NAME_ATTRIBUTE.equals(type))
         {
            List nameTags = getElementsByName(attrTag, Aris61Converter.ATTRIBUTE_VALUE_NODE);
            if (nameTags.size() > 0)
            {
               Element name = (Element) nameTags.get(0);
               Node text = name.getFirstChild();
               if (text != null)
               {
                  String value = name.getFirstChild().getNodeValue();
                  if (value != null)
                  {
                     retVal[1] = value.replace('\n', ' ');
                  }
               }
            }
            if (retVal[1] == null)
            {
               retVal[1] = retVal[0];
            }
            break;
         }
      }

      return retVal;
   }

   public static String getAttrDef(Node node, String name)
   {
      NodeList children = node.getChildNodes();
      NamedNodeMap attributes;
      String attrValue = null;

      for (int childPos = 0; childPos < children.getLength(); childPos++)
      {
         if (children.item(childPos).getNodeName().equals(Aris61Converter.ATTRIBUTE_NODE_NAME))
         {
            attributes = children.item(childPos).getAttributes();
            Node attribute = attributes.getNamedItem(Aris61Converter.ATTRIBUTE_NODE_TYPE);

            if ((attribute != null) && (attribute.getNodeValue().equals(name)))
            {
               Node attrVal = getChildByName(children.item(childPos),
                     Aris61Converter.ATTRIBUTE_VALUE_NODE);
               attrValue = attrVal.getFirstChild().getNodeValue();

               if (attrValue != null)
               {
                  attrValue.replace('\n', ' ');
               }

               break;
            }
         }
      }

      return attrValue;
   }

   public static String getAttributeValue(Node node, String attributeName)
   {
      NamedNodeMap attributes;
      Node attribute;
      String attributeValue = null;

      attributes = node.getAttributes();

      if (attributes != null)
      {
         attribute = attributes.getNamedItem(attributeName);

         if (attribute != null)
         {
            attributeValue = attribute.getNodeValue();
         }
      }

      return attributeValue;
   }

   public static Element getObjDefForObjOcc(Node objOcc, HashMap objDefMap)
   {
      String objDefId = getAttributeValue(objOcc, Aris61Converter.OBJDEF_IDREF_ATTRIBUTE);
      return objDefId == null ? null : (Element) objDefMap.get(objDefId);
   }

   public static String getIdForNode(Node node)
   {
      return getAttributeValue(node, node.getNodeName() + "." +  Aris61Converter.ID_ATTRIBUTE);
   }

   public static JoinSplitType getCarnotRuleType(Node rule)
   {
      String ruleSymbolName;
      JoinSplitType ruleType;

      ruleSymbolName = SimpleTaskUtil.getAttributeValue(rule, Aris61Converter.SYMBOLNUM_ATTRIBUTE);

      if (Aris61Converter.AND_SPLIT_TYPE.equals(ruleSymbolName))
      {
         ruleType = JoinSplitType.And;
      }
      else if (Aris61Converter.XOR_SPLIT_TYPE.equals(ruleSymbolName))
      {
         ruleType = JoinSplitType.Xor;
      }
      else if (Aris61Converter.OR_SPLIT_TYPE.equals(ruleSymbolName))
      {
         ruleType = JoinSplitType.Xor;
      }
      else
      {
         throw new PublicException("Only AND and XOR rule types allowed!");
      }

      return ruleType;
   }
}
