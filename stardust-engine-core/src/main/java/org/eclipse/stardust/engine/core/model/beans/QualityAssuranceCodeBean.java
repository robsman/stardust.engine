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
package org.eclipse.stardust.engine.core.model.beans;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.IQualityAssuranceCode;


/**
 * Default implementation for {@link IQualityAssuranceCode}
 * 
 * @author barry.grotjahn
 * @version $Revision: $
 */
public class QualityAssuranceCodeBean implements IQualityAssuranceCode, Serializable
{
   private static final long serialVersionUID = 1L;
   
   private String code;
   private String description;
   private String name;
   
   public QualityAssuranceCodeBean(String code, String description, String name)
   {
      super();
      this.code = code;
      this.description = description;
      this.name = name;
   }

   public String getCode()
   {
      return code;
   }

   public String getDescription()
   {
      return description;
   }

   public String getName()
   {
      return name;
   }

   @Override
   public String toString()
   {
      return code + "(" + description + ")";
   }
   
   
}