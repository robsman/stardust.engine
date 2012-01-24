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

public interface Reference
{
   String getId();

   /**
    * Gets the namespace of the referenced element.
    *
    * @return the namespace (the Id of the model containing containing the referenced element).
    */
   String getNamespace();

   /**
    * Gets the qualified ID of the referenced element.
    * 
    * @return the qualified id in the form "{<namespace>}<id>"
    */
   String getQualifiedId();

   int getModelOid();
}
