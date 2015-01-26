package org.eclipse.stardust.engine.core.runtime.command.impl;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.EnumSet;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.dto.DeployedModelDescriptionDetails;
import org.eclipse.stardust.engine.api.dto.DeployedModelDescriptionDetails.LevelOfDetail;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;

public class RetrieveModelDetailsCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private final String modelId;

   private final long modelOid;

   private boolean throwIfMissing = true;

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

   public RetrieveModelDetailsCommand notThrowing()
   {
      this.throwIfMissing = false;

      return this;
   }

   @Override
   public DeployedModel execute(ServiceFactory sf)
   {
      ParametersFacade.pushLayer(singletonMap(
            DeployedModelDescriptionDetails.LevelOfDetail.class.getName(),
            EnumSet.of(LevelOfDetail.NoProvidersNorConsumers)));
      try
      {
         long modelOid = this.modelOid;
         if (!isEmpty(modelId))
         {
            // find modelOid of active model
            Models models = sf.getQueryService().getModels(
                  DeployedModelQuery.findActiveForId(modelId));
            if (0 < models.size())
            {
               modelOid = models.get(0).getModelOID();
            }
            else
            {
               if (throwIfMissing)
               {
                  throw new ObjectNotFoundException(
                        BpmRuntimeError.MDL_NO_ACTIVE_MODEL_WITH_ID.raise(modelId));
               }
               else
               {
                  return null;
               }
            }
         }

         // resolve model by modelOid
         try
         {
            return sf.getQueryService().getModel(modelOid, false);
         }
         catch (ObjectNotFoundException onfe)
         {
            if (throwIfMissing)
            {
               throw onfe;
            }
            else
            {
               return null;
            }
         }
      }
      finally
      {
         ParametersFacade.popLayer();
      }
   }
}
