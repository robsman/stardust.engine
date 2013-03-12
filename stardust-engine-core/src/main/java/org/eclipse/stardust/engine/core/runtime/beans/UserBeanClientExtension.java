/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import static java.util.Collections.emptyMap;

import java.util.Map;

/**
 * Wrapper class to construct a UserBean Object containing the realm
 * @author Thomas.Wolfram
 *
 */
public class UserBeanClientExtension extends UserBean implements AttributedIdentifiablePersistent
{
   
   private static final long serialVersionUID = 1L;
   
   private IUserRealm realm;
   private transient Map cachedProperties = emptyMap();
   
   public UserBeanClientExtension(IUser userBean)
   {      
      this.setAccount(userBean.getAccount());
      this.setDescription(userBean.getDescription());
      this.setEMail(userBean.getEMail());
            
      
      this.setFirstName(userBean.getFirstName());
      this.setId(userBean.getId());
      this.setLastLoginTime(userBean.getLastLoginTime());
      this.setLastName(userBean.getLastName());
      this.setName(userBean.getName());
      this.setOID(userBean.getOID());
      this.setPassword(userBean.getPassword());
      

      this.setValidFrom(userBean.getValidFrom());
      this.setValidTo(userBean.getValidTo());
                              
   }
   
         
   @Override
   public void setRealm(IUserRealm realm)
   {     
      // set the local member variable
      // We are not using the superclass setter as this would potentially modify the realm
      this.realm = realm;
   }
      
   
}
