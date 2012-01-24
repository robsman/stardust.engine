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

import org.eclipse.xsd.XSDSchema;

/**
 * Type declaration containing embedded schema.
 *
 * @version $Revision$
 */
public interface ExternalReference extends XpdlType
{
   /**
    * Gets the schema location 
    * 
    * @return schema location 
    */
   String getLocation();
   
   /**
    * Gets the schema namespace (XPDL meaning)
    * 
    * @return schema namespace (XPDL meaning)
    */
   String getNamespace();
   
   /**
    * Gets the xref pointing to the referenced element 
    * 
    * @return xref pointing to the referenced element
    */
   String getXref();
   
   /**
    * Retrieves the schema corresponding to the location and namespace attributes.
    * 
    * @param model the Model in which the ExternalReference is contained.
    * 
    * @return the possibly cached schema or null.
    */
   XSDSchema getSchema(Model model);
}