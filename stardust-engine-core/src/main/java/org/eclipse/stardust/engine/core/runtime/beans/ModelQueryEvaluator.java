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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Date;

import org.eclipse.stardust.engine.api.model.IExternalPackage;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.query.ModelPolicy;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;

/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ModelQueryEvaluator extends AbstractQueryPredicate<IModel>
{
	private ModelPolicy policy;

   public ModelQueryEvaluator(Query query)
   {
      super(query);
      this.policy = (ModelPolicy) query.getPolicy(ModelPolicy.class);
   }

   @Override
	public boolean accept(IModel model) {
		if (policy != null) {
			if (policy.isExcludePredefinedModels()
					&& PredefinedConstants.PREDEFINED_MODEL_ID.equals(model
							.getId())) {
				return false;
			}
		}
		return super.accept(model);
	}

   public Object getValue(IModel model, String attribute, Object expected)
   {
      if ("id".equals(attribute))
      {
         return model.getId();
      }
      if ("state".equals(attribute))
      {
         // we have to attempt to return a state that is compatible with the expectations,
         // i.e. a model can be both ALIVE and ACTIVE
         ModelManager manager = ModelManagerFactory.getCurrent();
         String target = expected == null ? null : expected.toString();
         DeployedModelQuery.DeployedModelState state = null;
         if (target != null)
         {
            switch (DeployedModelQuery.DeployedModelState.valueOf(target))
            {
            case ACTIVE:
               if (manager.isActive(model))
               {
                  state = DeployedModelQuery.DeployedModelState.ACTIVE;
               }
               break;
            case ALIVE:
               if (manager.isAlive(model))
               {
                  state = DeployedModelQuery.DeployedModelState.ALIVE;
               }
               break;
            case INACTIVE:
               if (!manager.isActive(model))
               {
                  state = DeployedModelQuery.DeployedModelState.INACTIVE;
               }
               break;
            case DISABLED:
               if (model.getBooleanAttribute(PredefinedConstants.IS_DISABLED_ATT))
               {
                  state = DeployedModelQuery.DeployedModelState.DISABLED;
               }
               break;
            case VALID:
               if (!model.getBooleanAttribute(PredefinedConstants.IS_DISABLED_ATT))
               {
                  Date validFrom = (Date) model.getAttribute(PredefinedConstants.VALID_FROM_ATT);
                  if (validFrom == null || validFrom.before(TimestampProviderUtils.getTimeStamp()))
                  {
                     state = DeployedModelQuery.DeployedModelState.VALID;
                  }
               }
               break;
            default:
               throw new IllegalArgumentException("Unsupported model state: " + target);
            }
         }
         if (state == null)
         {
            if (manager.isActive(model))
            {
               state = DeployedModelQuery.DeployedModelState.ACTIVE;
            }
            else if (model.getBooleanAttribute(PredefinedConstants.IS_DISABLED_ATT))
            {
               state = DeployedModelQuery.DeployedModelState.DISABLED;
            }
            else if (manager.isAlive(model))
            {
               state = DeployedModelQuery.DeployedModelState.ALIVE;
            }
            else
            {
               state = DeployedModelQuery.DeployedModelState.INACTIVE;
            }
         }
         return state.name();
      }
      if ("consumer".equals(attribute))
      {
         ModelManager manager = ModelManagerFactory.getCurrent();
         IModel consumer = manager.findModel((Long) expected);
         if (consumer != null)
         {
            for (IExternalPackage pkg : consumer.getExternalPackages())
            {
               if (model == pkg.getReferencedModel())
               {
                  return expected;
               }
            }
         }
         return 0;
      }
      if ("provider".equals(attribute))
      {
         ModelManager manager = ModelManagerFactory.getCurrent();
         IModel provider = manager.findModel((Long) expected);
         if (provider != null)
         {
            for (IExternalPackage pkg : model.getExternalPackages())
            {
               if (provider == pkg.getReferencedModel())
               {
                  return expected;
               }
            }
         }
         return 0;
      }
      throw new IllegalArgumentException("Unsupported model attribute: " + attribute);
   }
}
