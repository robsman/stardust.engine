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
package org.eclipse.stardust.engine.core.model.convert.topease;

import org.eclipse.stardust.engine.core.model.convert.XMLUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class SimpleTaskUtil extends XMLUtil
{
   public static String getId(Node node)
   {
      NamedNodeMap attributes;
      String id = null;
      attributes = node.getAttributes();

      for (int i = 0; i < attributes.getLength(); i++)
      {
         if (attributes.item(i).getNodeName() == null)
         {
            continue;
         }
         else if (attributes.item(i).getNodeName().equals("ID"))
         {
            id = attributes.item(i).getNodeValue();
         }
      }
      return id;
   }

   public static String getName(Node node)
   {
      NodeList attributes;
      String name = null;

      attributes = node.getChildNodes();

      for (int i=0; i<attributes.getLength(); i++)
      {
         if ("name".equals(attributes.item(i).getNodeName()))
         {
            name = attributes.item(i).getFirstChild().getNodeValue();
            break;
         }
      }

      return name;
   }

   public static String getIdentifier(Node node)
   {
      NodeList attributes;
      String name = null;

      attributes = node.getChildNodes();

      for (int i=0; i<attributes.getLength(); i++)
      {
         if ("identifier".equals(attributes.item(i).getNodeName()))
         {
            name = attributes.item(i).getFirstChild().getNodeValue();
            break;
         }
      }

      return name;
   }

   /**
    *
    * @param node The node containing a identifier, name and description child node.
    * @return an array of three strings. position 0 is filled with the identifier
    * child node value, 1 with the name child node value and 2 with the description
    * child node value.
    */
   public static String[] getIdentNameDescr(Node node)
   {
      NodeList attributes;
      String[] identNameDescr = new String[3];

      attributes = node.getChildNodes();

      for (int i=0; i<attributes.getLength(); i++)
      {
         if ("identifier".equals(attributes.item(i).getNodeName()))
         {
            identNameDescr[0] = attributes.item(i).getFirstChild().getNodeValue();
            if ((identNameDescr[1] != null) && (identNameDescr[2] != null))
               break;
         }
         else if ("name".equals(attributes.item(i).getNodeName()))
         {
            identNameDescr[1] = attributes.item(i).getFirstChild().getNodeValue();
            if ((identNameDescr[0] != null) && (identNameDescr[2] != null))
               break;
         }
         else if ("description".equals(attributes.item(i).getNodeName()))
         {
            identNameDescr[2] = attributes.item(i).getFirstChild().getNodeValue();
            if ((identNameDescr[1] != null) && (identNameDescr[0] != null))
               break;
         }
      }

      return identNameDescr;

   }

   public static NodeList getContains(Node node)
   {
      NodeList retVal = null;
      NodeList elements = node.getChildNodes();

      for (int n = 0; n < elements.getLength(); ++n)
      {
         if (elements.item(n).getNodeName().equals("contains"))
         {
            retVal = elements.item(n).getChildNodes();
         }
      }

      return retVal;
   }

   /**
    * Parses a node which contains reference (attribute <code>href</code>) and returns
    * its value.
    *
    * @param node
    * @return
    */
   public static String parseReferenceNode(Node node)
   {
      NamedNodeMap attributes = node.getAttributes();

      for (int i = 0; i < attributes.getLength(); i++)
      {
         if (attributes.item(i).getNodeName() == null)
         {
            continue;
         }
         else if (attributes.item(i).getNodeName().equals("href"))
         {
            return attributes.item(i).getNodeValue();
         }
      }

      return null;
   }
}
