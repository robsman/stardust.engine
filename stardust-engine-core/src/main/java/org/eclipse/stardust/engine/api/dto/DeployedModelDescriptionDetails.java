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

import static java.util.Collections.emptySet;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.config.Parameters;
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

   public static enum LevelOfDetail
   {
      NoProvidersNorConsumers
   }

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

      this.active = modelManager.isActive(model);
      this.validFrom = (Date) model.getAttribute(PredefinedConstants.VALID_FROM_ATT);
      this.deploymentComment = (String) model
            .getAttribute(PredefinedConstants.DEPLOYMENT_COMMENT_ATT);
      this.version = (String) model.getAttribute(PredefinedConstants.VERSION_ATT);
      this.deploymentTime = (Date) model.getAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT);
      this.revision = model.getIntegerAttribute(PredefinedConstants.REVISION_ATT);

      Set<LevelOfDetail> levelOfDetail = (Set<LevelOfDetail>) Parameters.instance().get(LevelOfDetail.class.getName());
      if (null == levelOfDetail)
      {
         levelOfDetail = emptySet();
      }

      this.consumerModels = newArrayList();
      this.providerModels = newArrayList();
      this.implementationProcesses = newHashMap();

      if ( !levelOfDetail.contains(LevelOfDetail.NoProvidersNorConsumers))
      {
         List<IModel> usingModels = ModelRefBean.getUsingModels(model);
         List<IModel> usedModels = ModelRefBean.getUsedModels(model);

         // Retrieving Consumer Models
         for (IModel usingModel : usingModels)
         {
            if (model.getModelOID() != usingModel.getModelOID())
            {
               consumerModels.add(new Integer(usingModel.getModelOID()).longValue());
            }
         }

         // Retrieving Provider Models
         for (IModel usedModel : usedModels)
         {
            if (usedModel.getModelOID() != model.getModelOID())
            {
               providerModels.add(new Integer(usedModel.getModelOID()).longValue());
            }
         }

         // Retrieving ImplementationProcesses
         List<IProcessDefinition> processInterfaces = ModelRefBean.getProcessInterfaces(model);
         for (IProcessDefinition interfaceProcess : processInterfaces)
         {
            IProcessDefinition primaryImplementation = ModelRefBean
                  .getPrimaryImplementation(interfaceProcess, null, null);

            List<ImplementationDescription> implementations = newArrayList();

            // As the model itself provides the default implementation, add a details object
            // for the providing model as well
            implementations.add(new ImplementationDescriptionDetails(
                  interfaceProcess.getId(), new Integer(model.getModelOID()).longValue(),
                  interfaceProcess.getId(), primaryImplementation.equals(interfaceProcess),
                  new Integer(model.getModelOID()).longValue(), modelManager.isActive(model)));

            // append external providers
            for (IModel usingModel : usingModels)
            {
               if (model.getModelOID() == usingModel.getModelOID())
               {
                  continue;
               }

               ModelElementList modelElementList = usingModel.getProcessDefinitions();
               for (int i = 0; i < modelElementList.size(); i++)
               {
                  IProcessDefinition process = (IProcessDefinition) modelElementList.get(i);
                  if (process.getExternalReference() != null)
                  {
                     if (interfaceProcess.getId().equals(
                           process.getExternalReference().getId()))
                     {
                        boolean isPrimary = (primaryImplementation != null)
                              && primaryImplementation.equals(process);

                        ImplementationDescription detail = new ImplementationDescriptionDetails(
                              interfaceProcess.getId(),
                              new Integer(usingModel.getModelOID()).longValue(),
                              process.getId(), isPrimary,
                              new Integer(model.getModelOID()).longValue(),
                              modelManager.isActive(usingModel));
                        implementations.add(detail);
                     }
                  }
               }
            }
            implementationProcesses.put(interfaceProcess.getId(), implementations);
         }
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
