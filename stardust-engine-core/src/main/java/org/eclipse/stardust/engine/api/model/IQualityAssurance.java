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

/**
 * Base interface for creating and storing {@link IQualityAssuranceCode}
 * objects
 */
public interface IQualityAssurance extends Serializable
{
   /**
    * Finds a specific {@link IQualityAssuranceCode} based on the passed code
    * The codes created by {@link IQualityAssurance#createQualityAssuranceCode(String, String)}
    * are taken as data
    * 
    * @param code
    * @return
    */
   
   IQualityAssuranceCode findQualityAssuranceCode(String code);
   
   /**
    * Gets all the codes created by {@link IQualityAssurance#createQualityAssuranceCode(String, String)}
    * 
    * @return the codes created by {@link IQualityAssurance#createQualityAssuranceCode(String, String)}
    */
   List<IQualityAssuranceCode> getAllCodes();

   /**
    * Creates a {@link IQualityAssuranceCode} object based on the code and 
    * description
    * 
    * @param code - the code
    * @param description - the description
    * @return the {@link IQualityAssuranceCode} object created
    */
   IQualityAssuranceCode createQualityAssuranceCode(String code, String description, String name);
}