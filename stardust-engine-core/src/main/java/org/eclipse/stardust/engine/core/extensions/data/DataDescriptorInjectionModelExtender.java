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
package org.eclipse.stardust.engine.core.extensions.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.beans.DataPathBean;
import org.eclipse.stardust.engine.core.model.utils.IdFactory;
import org.eclipse.stardust.engine.core.model.utils.Identifiable;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.monitoring.AbstractPartitionMonitor;

/**
 *
 * @author Barry.Grotjahn
 *
 */
public class DataDescriptorInjectionModelExtender extends AbstractPartitionMonitor
{
   Logger trace = LogManager.getLogger(DataDescriptorInjectionModelExtender.class);
   
   private String prefix = "BusinessDate";
   private String name = "Business Date";

   @Override
   public void modelLoaded(IModel model)
   {
      ModelElementList<IData> dataList = model.getData();      
      boolean isSimpleModeler = model.getBooleanAttribute("stardust:model:simpleModel");
      
      if(isSimpleModeler)
      {
         for (IData data : dataList)
         {
            if (PredefinedConstants.BUSINESS_DATE.equals(data.getId())
                  && data.isPredefined())
            {
               findDescriptorsForData(data, model);
            }
         }
      }
   }

   private void findDescriptorsForData(IData data, IModel model)
   {
      ModelElementList<IProcessDefinition> pds = model.getProcessDefinitions();
      for (IProcessDefinition pd : pds)
      {         
         List<Identifiable> list = new ArrayList<Identifiable>();
         boolean match = false;
         for (Iterator i = pd.getAllDescriptors(); i.hasNext();)
         {
            IDataPath descriptor = (IDataPath) i.next();
            list.add(descriptor);
            
            // we have already a descriptor
            if(descriptor.getData().equals(data))
            {
               match = true;
               break;
            }             
         }

         if(match)
         {
            match = false;
            continue;
         }
         
         IdFactory factory = new IdFactory(prefix);
         factory.computeNames(list);
         
         String id = factory.getId();
         IDataPath dataPath = new DataPathBean(id, name, data, null, Direction.IN);
         dataPath.setDescriptor(true);
         pd.addToDataPaths(dataPath);
         dataPath.register(0);
      }
   }
}