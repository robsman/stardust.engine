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

/**
 * Classes implementing this interface are able to provide an activity instance context.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface ActivityInstanceContextAware
{
   /**
    * Retrieves the activity instance OID for this context.
    *
    * @return a long value representing the oid of an activity instance.
    */
   public abstract long getActivityInstanceOid();
}
