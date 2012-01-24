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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class WorkproductFolderParser extends RecursiveFolderParser
{
   private Map existingObjects;

   public WorkproductFolderParser(Map existingObjects)
   {
      this.existingObjects = existingObjects;
   }

   Map loadItem(Node item, Package parentPackage)
   {
      String id;
      NodeList elements;
      String wpName = null;
      String wpIdentifier = null;
      String wpDescription = null;
      ClassWrapper classWrapper = null;
      WorkproductWrapper wrapper;
      HashMap retVal;
      Vector flowIds;

      retVal = new HashMap();
      flowIds = new Vector();
      id = SimpleTaskUtil.getId(item);
      elements = item.getChildNodes();

      for (int i=0; i<elements.getLength(); i++)
      {
         if ("name".equals(elements.item(i).getNodeName()))
         {
            wpName = elements.item(i).getFirstChild().getNodeValue();
         }
         else if ("identifier".equals(elements.item(i).getNodeName()))
         {
            wpIdentifier = elements.item(i).getFirstChild().getNodeValue();
         }
         else if ("descripion".equals(elements.item(i).getNodeName()))
         {
            wpDescription = elements.item(i).getFirstChild().getNodeValue();
         }
         else if ("hasTypeUsage".equals(elements.item(i).getNodeName()))
         {
            String referencedId;
            Node typeUsage;
            Node isClassified;
            Node reference;

            typeUsage = SimpleTaskUtil.getChildByName(elements.item(i), "TypeUsage");
            isClassified = SimpleTaskUtil.getChildByName(typeUsage, "isClassifiedBy");
            reference = SimpleTaskUtil.getChildByName(isClassified, "reference");
            referencedId = SimpleTaskUtil.parseReferenceNode(reference);
            classWrapper = (ClassWrapper) existingObjects.get(referencedId);
         }
         else if ("passesFlow".equals(elements.item(i).getNodeName()))
         {
            String referenceId;
            NodeList referenceNodes = elements.item(i).getChildNodes();

            for (int m = 0; m < referenceNodes.getLength(); ++m)
            {
               if (referenceNodes.item(m).getNodeName().equals("reference"))
               {
                  referenceId = SimpleTaskUtil.parseReferenceNode(referenceNodes.item(m));
                  flowIds.addElement(referenceId);
               }
            }
         }
      }

      wpName = SimpleTaskUtil.getName(item);
      wrapper = new WorkproductWrapper(classWrapper, wpIdentifier, wpName, wpDescription,
            parentPackage);
      retVal.put(id, wrapper);

      for (int i = 0; i < flowIds.size(); i++)
      {
         String flowId = (String) flowIds.elementAt(i);
         retVal.put(flowId, wrapper);
      }

      return retVal;
   }

   String getRootFolderName()
   {
      return "Work Product";
   }

   String getItemName()
   {
      return "WorkProduct";
   }

   String getContentType()
   {
      return "WorkProduct";
   }
}
