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
 * Client side view of an {@link IQualityAssuranceCode}
 * 
 * @author holger.prause
 * @version $Revision: $
 */
public interface QualityAssuranceCode extends IQualityAssuranceCode
{
   /**
    * Gets the code
    * @return the code set in the modeler
    */
   String getCode();
   
   /**
    * Get the description
    * @return the description set in the modeler
    */
   String getDescription();
}