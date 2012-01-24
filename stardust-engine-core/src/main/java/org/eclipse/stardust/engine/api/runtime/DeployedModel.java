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

import org.eclipse.stardust.engine.api.model.Model;

/**
 * A client view of a Model, which includes deployment information.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface DeployedModel extends DeployedModelDescription, Model
{
   /**
    * Gets whether the model is alive. A model is alive if it is the active model,
    * or if it has active process instances.
    *
    * @return true if this model is alive.
    */
   boolean isAlive();
}
