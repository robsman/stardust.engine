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
package org.eclipse.stardust.engine.api.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.ImplementationDescription;
import org.eclipse.stardust.engine.api.runtime.ImplementationDescriptionDetails;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ModelRefBean;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeployedModelDescriptionDetails extends ModelElementDetails
      implements DeployedModelDescription
{
   private static final long serialVersionUID = 9122671017836602371L;

   private final boolean active;

   private final Date validFrom;

   private final String deploymentComment;

   private final String version;

   private int revision;

   private Date deploymentTime;

   private final List<Long> consumerModels;

   private final List<Long> providerModels;

   private final Map<String, List<ImplementationDescription>> implementationProcesses;

   public DeployedModelDescriptionDetails(IModel model)
   {
      super(model);

      ModelManager modelManager = ModelManagerFactory.getCurrent();

      active = modelManager.isActive(model);
      validFrom = (Date) model.getAttribute(PredefinedConstants.VALID_FROM_ATT);
      deploymentComment = (String) model
            .getAttribute(PredefinedConstants.DEPLOYMENT_COMMENT_ATT);
      version = (String) model.getAttribute(PredefinedConstants.VERSION_ATT);
      deploymentTime = (Date) model.getAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT);
      revision = model.getIntegerAttribute(PredefinedConstants.REVISION_ATT);

      // Retrieving Consumer Models
      consumerModels = new ArrayList<Long>();
      List<IModel> usingModels = new ArrayList<IModel>();
      for (Iterator<IModel> i = modelManager.getAllModels(); i.hasNext();)
      {
         IModel usingModel = i.next();
         List<IModel> usedModels = ModelRefBean.getUsedModels(usingModel);
         for (Iterator<IModel> j = usedModels.iterator(); j.hasNext();)
         {
            IModel usedModel = j.next();
            if (model.getModelOID() != usingModel.getModelOID())
            {
               if (model.getModelOID() == usedModel.getModelOID())
               {
                  consumerModels.add(new Integer(usingModel.getModelOID()).longValue());
                  usingModels.add(usingModel);
               }
            }
         }
      }

      // Retrieving Provider Models
      providerModels = new ArrayList<Long>();
      List<IModel> usedModels = ModelRefBean.getUsedModels(model);
      for (Iterator<IModel> i = usedModels.iterator(); i.hasNext();)
      {
         int oid = i.next().getModelOID();
         if (oid != model.getModelOID())
         {
            providerModels.add(new Integer(oid).longValue());
         }
      }

      // Retrieving ImplementationProcesses
      implementationProcesses = new HashMap();
      List<IProcessDefinition> processInterfaces = ModelRefBean
            .getProcessInterfaces(model);
      for (Iterator<IProcessDefinition> i = processInterfaces.iterator(); i.hasNext();)
      {
         IProcessDefinition interfaceProcess = i.next();
         IProcessDefinition primaryImplementation = ModelRefBean
               .getPrimaryImplementation(interfaceProcess, null, null);
         List<ImplementationDescription> details = new ArrayList<ImplementationDescription>();
         for (Iterator<IModel> j = usingModels.iterator(); j.hasNext();)
         {
            IModel usingModel = j.next();
            ModelElementList modelElementList = usingModel.getProcessDefinitions();
            for (int k = 0; k < modelElementList.size(); k++)
            {
               IProcessDefinition process = (IProcessDefinition) modelElementList.get(k);
               if (process.getExternalReference() != null)
               {
                  if (interfaceProcess.getId().equals(
                        process.getExternalReference().getId()))
                  {
                     boolean primary = false;
                     if (primaryImplementation != null)
                     {
                        if (primaryImplementation.equals(process))
                        {
                           primary = true;
                        }
                     }
                     ImplementationDescription detail = new ImplementationDescriptionDetails(
                           interfaceProcess.getId(),
                           new Integer(usingModel.getModelOID()).longValue(),
                           process.getId(), primary,
                           new Integer(model.getModelOID()).longValue(),
                           modelManager.isActive(usingModel));
                     details.add(detail);
                  }
               }
            }
         }
         // As the model itself provides the default implementation, add a details object
         // for the providing model as well
         ImplementationDescription detail = new ImplementationDescriptionDetails(
               interfaceProcess.getId(), new Integer(model.getModelOID()).longValue(),
               interfaceProcess.getId(), primaryImplementation.equals(interfaceProcess),
               new Integer(model.getModelOID()).longValue(), modelManager.isActive(model));
         details.add(0, detail);
         implementationProcesses.put(interfaceProcess.getId(), details);
      }
   }

   protected DeployedModelDescriptionDetails(DeployedModelDescriptionDetails template)
   {
      super(template);

      this.active = template.active;

      this.validFrom = template.validFrom;
      this.deploymentComment = template.deploymentComment;
      this.version = template.version;
      this.deploymentTime = template.deploymentTime;
      this.revision = template.revision;

      this.providerModels = template.providerModels;
      this.consumerModels = template.consumerModels;
      this.implementationProcesses = template.implementationProcesses;

   }

   public boolean isActive()
   {
      return active;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public Date getDeploymentTime()
   {
      return deploymentTime;
   }

   public String getDeploymentComment()
   {
      return deploymentComment;
   }

   public int getRevision()
   {
      return revision;
   }

   public String getVersion()
   {
      return version;
   }

   public List<Long> getConsumerModels()
   {
      return consumerModels;
   }

   public Map<String, List<ImplementationDescription>> getImplementationProcesses()
   {
      return implementationProcesses;
   }

   public List<Long> getProviderModels()
   {
      return providerModels;
   }
}
