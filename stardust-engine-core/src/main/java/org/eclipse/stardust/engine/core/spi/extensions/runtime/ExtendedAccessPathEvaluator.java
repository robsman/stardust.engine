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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

/**
 * Extended version of {@link AccessPathEvaluator}, gives access to the
 * {@link AccessPoint} definition (the old-style AccessPathEvaluator only gave 
 * access to its attributes)
 * 
 * @see AccessPathEvaluator
 */
public interface ExtendedAccessPathEvaluator
{
   /**
    * Pseudo value indicating an unmodified access point handle.
    */
   static final Object UNMODIFIED_HANDLE = new Object();

   /**
    * Evaluates an out data path by applying the <code>outPath</code> expression against
    * the given <code>accessPoint</code> and returning the resulting value.
    * 
    * @param accessPointDefinition
    *           The access point definition.
    * @param accessPointInstance
    *           The actual access point.
    * @param outPath
    *           The dereference path to be applied.
    * @param accessPathEvaluationContext
    *           Evaluation context object containing additional information
    * @return The resulting value.
    */
   Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String outPath, AccessPathEvaluationContext accessPathEvaluationContext);

   /**
    * Evaluates an in data path by applying the <code>inPath</code> expression
    * parameterized with the given <code>value</code> against the given
    * <code>accessPoint</code> and returns the result, if appropriate.
    * 
    * @param accessPointDefinition
    *           The access point definition.
    * @param accessPointInstance
    *           The actual access point.
    * @param inPath
    *           The dereference path to be used when applying the given value.
    * @param accessPathEvaluationContext
    *           Evaluation context object containing additional information
    * @param value
    *           The new value to be applied to the access point.
    * @return Either the new access point, or {@link #UNMODIFIED_HANDLE} if the access
    *         point did not change its identity.
    */
   Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String inPath, AccessPathEvaluationContext accessPathEvaluationContext, Object value);

   /**
    * Yields the initial value for a newly instantiated data values.
    * 
    * @param accessPointDefinition
    *           The access point definition.
    * @param accessPathEvaluationContext
    *           Evaluation context object containing additional information
    * @return The requested initial value.
    */
   Object createInitialValue(AccessPoint accessPointDefinition, AccessPathEvaluationContext accessPathEvaluationContext);

   /**
    * Yields the default value used in case of a missing data value.
    * 
    * @param accessPointDefinition
    *           The access point definition.
    * @param accessPathEvaluationContext
    *           Evaluation context object containing additional information
    * @return The requested default value.
    */
   Object createDefaultValue(AccessPoint accessPointDefinition, AccessPathEvaluationContext accessPathEvaluationContext);

}
