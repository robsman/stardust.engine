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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.model.IQualityAssuranceCode;
import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;

public class QualityAssuranceCodeDetails implements QualityAssuranceCode
{
   /**
    * 
    */
   private static final long serialVersionUID = 445700648025238776L;
   private String code;
   private String description;

   public QualityAssuranceCodeDetails(IQualityAssuranceCode code)
   {
      this.code = code.getCode();
      description = code.getDescription();   
   }

   public String getCode()
   {
      return code;
   }

   public String getDescription()
   {
      return description;
   }
}