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

import java.util.Set;

/**
 * User provided class that allows custom conversion of data values during process spawning.
 *
 * @author Florin.Herinean
 */
public interface DataValueConverter
{
   /**
    * Callback method to convert data values during process spawning.
    *
    * @param provider a utility class that allows manipulation of data values.
    *
    * @return a set of data ids that should be excluded from  copying if the process was spawned with copy all data flag.
    */
   public Set<String> convertDataValues(DataValueProvider provider);
}
