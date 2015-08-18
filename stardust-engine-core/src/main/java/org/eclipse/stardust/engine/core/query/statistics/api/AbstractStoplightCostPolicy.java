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
package org.eclipse.stardust.engine.core.query.statistics.api;

import org.eclipse.stardust.engine.core.model.utils.ModelElement;

/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractStoplightCostPolicy extends AbstractStoplightPolicy
{

   protected abstract Number getTargetCost(ModelElement modelElement);

   public AbstractStoplightCostPolicy(float yellowPct, float redPct)
   {
      super(yellowPct, redPct);
   }

   public Status rateCost(float cost, ModelElement modelElement)
   {
      Status status = GREEN;

      Number targetCost = getTargetCost(modelElement);

      if (null != targetCost)
      {
         float redCost = redPct * targetCost.floatValue();
         float yellowCost = yellowPct * targetCost.floatValue();

         if (cost >= redCost)
         {
            status = RED;
         }
         else if (cost >= yellowCost)
         {
            status = YELLOW;
         }
      }

      return status;
   }

}
