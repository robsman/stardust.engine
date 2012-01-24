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

import java.util.List;

import org.eclipse.stardust.engine.api.model.Inconsistency;


/**
 * The <code>ModelReconfigurationInfo</code> class is used to receive information about a
 * model reconfiguration operation. Model reconfiguration operations are all operations which modifies the
 * models in audit trail, their attributes or behavior, e.g. model deployment, configuration variable modification.
 *
 * @author stephan.born
 * @version $Revision: 43208 $
 */
public interface ModelReconfigurationInfo extends ReconfigurationInfo
{
   /**
    * Returns the id of the reconfigured model.
    *
    * @return the model ID.
    */
   String getId();

   /**
    * Returns the OID of the reconfigured model. If the model is for the first time deployed,
    * a new OID will be generated for the model.
    *
    * @return the model OID.
    */
   int getModelOID();

   /**
    * Gets the list of warnings issued during the model reconfiguration operation.
    *
    * @return a List of {@link org.eclipse.stardust.engine.api.model.Inconsistency} objects.
    */
   List<Inconsistency> getWarnings();

   /**
    * Gets the list of errors issued during the model reconfiguration operation.
    *
    * @return a List of {@link org.eclipse.stardust.engine.api.model.Inconsistency} objects.
    */
   List<Inconsistency> getErrors();

   /**
    * Check if the model reconfiguration operation is valid.
    *
    * @return true if there were no errors and no warnings during model reconfiguration.
    */
   boolean isValid();

   /**
    * Checks if there were errors issued during model reconfiguration.
    *
    * @return true if there are errors.
    */
   boolean hasErrors();

   /**
    * Checks if there were warnings issued during model reconfiguration.
    *
    * @return true if there are warnings.
    */
   boolean hasWarnings();
}
