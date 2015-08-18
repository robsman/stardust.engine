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

/**
 * Class representing an quality assurance error code
 *
 * @author barry.grotjahn
 * @version $Revision: 43207 $
 */
public interface IQualityAssuranceCode extends Serializable
{
   /**
    * Gets the code
    * @return the code set in the modeler
    */
   String getCode();
   
   /**
    * Gets the description
    * @return the description set in the modeler
    */
   String getDescription();

   /**
    * Gets the name
    * @return the description set in the modeler
    */
   String getName();   
}