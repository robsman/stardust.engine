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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * The <code>ReconfigurationInfo</code> class is used to receive information about a
 * reconfiguration operation. Reconfiguration operations are all operations which modifies the
 * overall behavior of the runtime engine, e.g. model deployment, configuration variable modification.
 *
 * @author stephan.born
 * @version $Revision: 43208 $
 */
public interface ReconfigurationInfo extends Serializable
{
   /**
    * Checks if the reconfiguration operation was performed.
    *
    * @return true if the reconfiguration operation was performed.
    */
   boolean success();
}
