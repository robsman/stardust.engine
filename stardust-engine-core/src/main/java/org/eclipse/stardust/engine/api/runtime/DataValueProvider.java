/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * Utility class to allow manipulation of data values during process spawning.
 *
 * @author Florin.Herinean
 */
public interface DataValueProvider
{
   /**
    * Retrieves a data value from the target process instance.
    *
    * @param id the data id.
    * @return the value or if there is no such value.
    */
   Object getValue(String id);

   /**
    * Retrieves a data value from the source process instance.
    *
    * @param id the data id.
    * @return the value or if there is no such value.
    */
   Object getSourceValue(String id);

   /**
    * Writes a data value to the traget process instance.
    *
    * @param id the data id.
    * @param value the value.
    */
   void setValue(String id, Serializable value);
}
