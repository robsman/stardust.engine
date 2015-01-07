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

import java.util.Map;

// @todo (france, ub): rename?
/**
 * @author rsauer
 * @version $Revision$
 */
public interface AccessPathEvaluator
{
   /**
    * Pseudo value indicating an unmodified access point handle.
    */
   static final Object UNMODIFIED_HANDLE = new Object();

   /**
    * Evaluates an out data path by applying the <code>outPath</code> expression against
    * the given <code>accessPoint</code> and returning the resulting value.
    *
    * @param attributes The access point definition's attributes.
    * @param accessPoint The actual access point.
    * @param outPath The dereference path to be applied.
    * @return The resulting value.
    */
   Object evaluate(Map attributes, Object accessPoint, String outPath);

   /**
    * Evaluates an in data path by applying the <code>inPath</code> expression
    * parametrized with the given <code>value</code> against the given
    * <code>accessPoint</code> and returns the result, if appropriate.
    *
    * @param attributes The access point definition's attributes.
    * @param accessPoint The actual access point.
    * @param inPath The dereference path to be used when applying the given value.
    * @param value The new value to be applied to the access point.
    * @return Either the new access point, or {@link #UNMODIFIED_HANDLE} if the
    *         access point did not change its identity.
    */
   Object evaluate(Map attributes, Object accessPoint, String inPath, Object value);

   /**
    * Yields the initial value for a newly instantiated data values.
    *
    * @param attributes The access point definition's attributes.
    * @return The requested initial value.
    */
   Object createInitialValue(Map attributes);

   /**
    * Yields the default value used in case of a missing data value.
    *
    * @param attributes The access point definition's attributes.
    * @return The requested default value.
    */
   Object createDefaultValue(Map attributes);
}
