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

public interface DepartmentInfo extends Serializable
{
   /**
    * Gets the OID of this department info.
    * 
    * @return The OID of this object
    */
   long getOID();
   
   /**
    * Gets the id Id of this department info. 
    * 
    * @return The Id.
    */
   String getId();
   
   /**
    * Gets the name of this department info.
    * 
    * @return The name.
    */
   String getName();

   /**
    * Returns the runtime OID of the bound organization.
    * 
    * @return The OID.
    */
   long getRuntimeOrganizationOID();
}