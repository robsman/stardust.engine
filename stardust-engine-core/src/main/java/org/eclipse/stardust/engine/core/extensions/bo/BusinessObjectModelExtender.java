/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.extensions.bo;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.BUSINESS_OBJECTS_DATAREF;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PRIMARY_KEY_ATT;

import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.beans.DataPathBean;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.monitoring.AbstractPartitionMonitor;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;

/**
 *
 * @author Thomas.Wolfram
 *
 */
public class BusinessObjectModelExtender extends AbstractPartitionMonitor
{
   private static final String INPUT_PREFERENCES_DESCRIPTOR_KEY = "descriptor";

   private static final String INPUT_PREFERENCES_DESCRIPTOR_KEY_LABEL = "InputPreferences_label";

   Logger trace = LogManager.getLogger(BusinessObjectModelExtender.class);

   @Override
   public void modelLoaded(IModel model)
   {
      ModelElementList<IData> dataList = model.getData();

      for (IData data : dataList)
      {
         // find all Data that are Business Objects
         if (((String) data.getAttribute(PRIMARY_KEY_ATT)) != null
               && StructuredTypeRtUtils.isStructuredType(data.getType().getId()))
         {
            findDescriptorsForBusinessObject(data, model);
         }
      }
   }

   private void findDescriptorsForBusinessObject(IData data, IModel model)
   {
      ITypeDeclaration typeDecl = StructuredTypeRtUtils.getTypeDeclaration(data);

      Set<TypedXPath> xpaths = StructuredTypeRtUtils.getAllXPaths(model, typeDecl);

      for (TypedXPath xpath : xpaths)
      {
         XPathAnnotations annotations = xpath.getAnnotations();

         if (annotations != null)
         {
            String descriptorKeyValue = annotations.getElementValue(
                  XPathAnnotations.IPP_ANNOTATIONS_NAMESPACE, new String[] {
                        "storage", INPUT_PREFERENCES_DESCRIPTOR_KEY});

            String descriptorLabelValue = annotations.getElementValue(
                  XPathAnnotations.IPP_ANNOTATIONS_NAMESPACE, new String[] {
                        "ui", INPUT_PREFERENCES_DESCRIPTOR_KEY_LABEL});

            if (descriptorKeyValue != null && Boolean.parseBoolean(descriptorKeyValue))
            {

               if (descriptorLabelValue == null)
               {
                  descriptorLabelValue = StructuredDataXPathUtils.getLastXPathPart(xpath.getXPath());
               }

               ModelElementList<IProcessDefinition> pds = model.getProcessDefinitions();

               // Add descriptor to all process definitions referencing the BO
               for (IProcessDefinition pd : pds)
               {
                  if (pd.getAttribute(BUSINESS_OBJECTS_DATAREF) != null
                        && pd.getAttribute(BUSINESS_OBJECTS_DATAREF).equals(
                              data.getId()))
                  {
                     addDescriptorsToProcessDefinition(pd, data, xpath.getXPath(),
                           descriptorLabelValue);
                  }
               }
            }
         }
      }
   }

   private IProcessDefinition addDescriptorsToProcessDefinition(IProcessDefinition pd,
         IData data, String xpath, String label)
   {

      IDataPath dataPath = new DataPathBean(xpath, label, data, xpath, Direction.IN);
      dataPath.setDescriptor(true);

      pd.addToDataPaths(dataPath);

      dataPath.register(0);
      return pd;
   }
}
