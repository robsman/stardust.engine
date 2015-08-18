/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;

public interface IHandleGetDataValue
{
   /**
    * Returns the IData for the correct PI.
    */   
   public IDataValue getDataValue(AccessPoint accessPointDefinition, AbstractInitialDataValueProvider dataValueProvider, AccessPathEvaluationContext evaluationContext);   
   
   /**
    * Evaluates an out data path by applying the <code>outPath</code> expression against
    * the given <code>accessPoint</code> and returning the resulting value.
    * 
    * @param accessPointDefinition
    *           The access point definition.
    * @param outPath
    *           The dereference path to be applied.
    * @param accessPathEvaluationContext
    *           Evaluation context object containing additional information
    * @return The resulting value.
    */   
   Object evaluate(AccessPoint accessPointDefinition, String outPath, AccessPathEvaluationContext accessPathEvaluationContext);   
}