package org.eclipse.stardust.engine.core.runtime.command.impl;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.EnumSet;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.DeployedModelDescriptionDetails;
import org.eclipse.stardust.engine.api.dto.DeployedModelDescriptionDetails.LevelOfDetail;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

public class RetrieveModelDetailsCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private final String modelId;

   private final long modelOid;

   public static RetrieveModelDetailsCommand retrieveModelByOid(long modelOid)
   {
      return new RetrieveModelDetailsCommand(null, modelOid);
   }

   public static RetrieveModelDetailsCommand retrieveActiveModelById(String modelId)
   {
      return new RetrieveModelDetailsCommand(modelId, 0L);
   }

   private RetrieveModelDetailsCommand(String modelId, long modelOid)
   {
      this.modelId = modelId;
      this.modelOid = modelOid;
   }

   @Override
   public Model execute(ServiceFactory sf)
   {
      ParametersFacade.pushLayer(singletonMap(
            DeployedModelDescriptionDetails.LevelOfDetail.class.getName(),
            EnumSet.of(LevelOfDetail.NoProvidersNorConsumers)));
      try
      {
         long modelOid = this.modelOid;
         if (!isEmpty(modelId))
         {
            Models models = sf.getQueryService().getModels(
                  DeployedModelQuery.findActiveForId(modelId));
            if (0 < models.size())
            {
               modelOid = models.get(0).getModelOID();
            }
            else
            {
               throw new ObjectNotFoundException(
                     BpmRuntimeError.MDL_NO_ACTIVE_MODEL_WITH_ID.raise(modelId));
            }
         }

         return sf.getQueryService().getModel(modelOid);
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }

}
