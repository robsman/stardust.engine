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
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IQualityAssurance;
import org.eclipse.stardust.engine.api.model.IQualityAssuranceCode;


/**
 * Default implementation of {@link IQualityAssurance}
 * 
 * @author barry.grotjahn
 * @version $Revision: $
 */
public class QualityAssuranceBean implements IQualityAssurance, Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 7464972021936769626L; 
   private List<IQualityAssuranceCode> codes = CollectionUtils.newArrayList();

   /**
    * {@inheritDoc}
    */
   public IQualityAssuranceCode createQualityAssuranceCode(String code, String value, String name)
   {
      IQualityAssuranceCode codeBean = new QualityAssuranceCodeBean(code, value, name);      
      codes.add(codeBean);
      
      return codeBean;
   }

   /**
    * {@inheritDoc}
    */
   public IQualityAssuranceCode findQualityAssuranceCode(String code)
   {      
      for(IQualityAssuranceCode tmpCode: codes)
      {
         if(tmpCode != null && tmpCode.getCode().equals(code))
         {
            return tmpCode;
         } 
   }
   
      return null;
   }
   
   /**
    * {@inheritDoc}
    */
   public List<IQualityAssuranceCode> getAllCodes()
   {
      return codes;
   }
}