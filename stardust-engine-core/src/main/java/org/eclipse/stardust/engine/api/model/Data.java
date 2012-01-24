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

/**
 * A client side view of a data definition.
 *
 * @version $Revision$
 */
public interface Data extends ModelElement
{
   /**
    * Retrieves the type of the data as defined in the model.
    * 
    * @return a string representing the type of the data
    */
   String getTypeId();
   
   /**
    * Retrieves a reference to an external type declaration.
    * 
    * @return the reference
    */
   Reference getReference();
}
