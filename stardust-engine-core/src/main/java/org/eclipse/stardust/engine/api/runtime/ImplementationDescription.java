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

/**
 * The <code>ImplementationDescription</code> provides information concerning a specific
 * implementation of a Process Interface.
 *
 * @author florin.herinean
 * @version $Revision: $
 */
public interface ImplementationDescription
{
   
   /**
    * Returns the ID of the model which provides the Process Interface.
    * 
    * @return the ID of the model which provides the Process Interface
    */
   long getInterfaceModelOid();

   /**
    * Returns the local ID of the implemented Process Interface.
    * 
    * @return the local ID of the implemented Process Interface.
    */
   String getProcessInterfaceId();

   /**
    * Returns the ID of the model providing an interface implementation 
    * 
    * @return the ID of the model providing an interface implementation
    */
   long getImplementationModelOid();

   /**
    * Returns the local ID of the implementing Process Definition.
    * 
    * @return the local ID of the implementing Process Definition.
    */
   String getImplementationProcessId();

   /**
    * Returns <b>true</b> if the this implementation is the primary implementation. 
    * 
    * @return <b>true</b> or <b>false</b> depending on primary implementation or not.
    */
   boolean isPrimaryImplementation();

   /**
    * Indicates if this is currently considered an Implementation Alternative or not. If not, typically
    * a more recent deployment of a model with the same Model ID exists and is offering an active Implementation.
    * 
    * @return  <b>true</b> or <b>false</b> depending on the existence of a more recent implementation alternative.
    */
   boolean isActive();
}
