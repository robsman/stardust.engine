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
package org.eclipse.stardust.common;

import org.eclipse.stardust.common.error.PublicException;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface ICallable<T>
{
   /**
    * Computes a result, or throws an exception if unable to do so.
    *
    * @return computed result
    * @throws Exception if unable to compute a result
    */
   T call() throws PublicException;
}
