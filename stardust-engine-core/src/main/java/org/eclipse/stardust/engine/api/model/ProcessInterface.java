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
package org.eclipse.stardust.engine.api.model;

import java.io.Serializable;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Interface used to separate a process definition from its interface.
 *
 * @author fherian
 * @version $Revision: 44506 $
 */

public interface ProcessInterface extends Serializable
{
   /**
    * Returns the ID of the declaring process definition.
    *
    * @return The ID of the declaring process definition.    
    */
   QName getDeclaringProcessDefinitionId();

   /**
    * Returns an ordered list of formal parameters as defined in the model.
    * This list may be empty (but never null) if the process interface does not define formal parameters.
    *
    * @return List of defined formal parameters.
    * @see FormalParameterType
    */
   List<FormalParameter> getFormalParameters();
}
