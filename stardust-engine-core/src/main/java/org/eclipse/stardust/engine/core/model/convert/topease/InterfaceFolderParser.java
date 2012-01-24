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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class InterfaceFolderParser extends RecursiveFolderParser
{
   Map existingObjects;

   public InterfaceFolderParser(Map existingObjects)
   {
      this.existingObjects = existingObjects;
   }

   Map loadItem(Node item, Package parentPackage)
   {
      String[] sysIdentNameDescr;
      SystemWrapper sysWrapper;
      boolean gui;
      String id;
      String activityId;
      String[] opIdentNameDescr;
      NodeList elements;
      NodeList parameter;
      NodeList references;
      Node actRef;
      OperationWrapper operation = null;
      ParameterWrapper parameterWrapper;
      Map returnValue;

      returnValue = new HashMap();
      sysIdentNameDescr = SimpleTaskUtil.getIdentNameDescr(item);
      gui = (SimpleTaskUtil.getChildByName(item, "gui") != null);
      sysWrapper = new SystemWrapper(sysIdentNameDescr[0], sysIdentNameDescr[1],
            sysIdentNameDescr[2], gui, parentPackage);
      elements = SimpleTaskUtil.getContains(item);

      for (int elementPos=0; elementPos<elements.getLength(); elementPos++)
      {
         if ("Operation".equals(elements.item(elementPos).getNodeName()))
         {
            id = SimpleTaskUtil.getId(elements.item(elementPos));
            opIdentNameDescr = SimpleTaskUtil.getIdentNameDescr(elements.item(elementPos));
            operation = new OperationWrapper(opIdentNameDescr[0], opIdentNameDescr[1],
                  opIdentNameDescr[2], sysWrapper);
            actRef = SimpleTaskUtil.getChildByName(elements.item(elementPos),
                  "supportsAMActivity");

            if (actRef != null)
            {
               references = actRef.getChildNodes();

               if (references != null)
               {
                  for (int i=0; i<references.getLength(); i++)
                  {
                     if ("reference".equals(references.item(i).getNodeName()))
                     {
                        activityId = SimpleTaskUtil.parseReferenceNode(references.item(i));
                        operation.addSupportedActivityId(activityId);
                     }
                  }
               }
            }

            parameter = SimpleTaskUtil.getContains(elements.item(elementPos));

            if (parameter != null)
            {
               for (int paramPos=0; paramPos<parameter.getLength(); paramPos++)
               {
                  if ("OutputParameter".equals(parameter.item(paramPos).getNodeName()))
                  {
                     parameterWrapper = parseParameter(parameter.item(paramPos));
                     operation.addOutputParameter(parameterWrapper);
                  }
                  else if ("InputParameter".equals(parameter.item(paramPos).getNodeName()))
                  {
                     parameterWrapper = parseParameter(parameter.item(paramPos));
                     operation.addInputParameter(parameterWrapper);
                  }
               }
            }

            returnValue.put(id, operation);
         }
      }

      return returnValue;
   }

   private ParameterWrapper parseParameter(Node parameter)
   {
      String[] identNameDescr;
      Node classified;
      Node reference;
      String referenceID;
      ClassWrapper type;
      ParameterWrapper parameterWrapper;

      identNameDescr = SimpleTaskUtil.getIdentNameDescr(parameter);
      classified = SimpleTaskUtil.getChildByName(parameter, "isClassifiedBy");
      reference = SimpleTaskUtil.getChildByName(classified, "reference");
      referenceID = SimpleTaskUtil.parseReferenceNode(reference);
      type = (ClassWrapper) existingObjects.get(referenceID);
      parameterWrapper = new ParameterWrapper(identNameDescr[0], identNameDescr[1],
            identNameDescr[2], type);
      return parameterWrapper;
   }

   String getRootFolderName()
   {
      return "Interface";
   }

   String getItemName()
   {
      return "TEInterface";
   }

   String getContentType()
   {
      return "TEInterface";
   }
}
