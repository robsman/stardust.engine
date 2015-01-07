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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.security.Principal;

/**
 * @author rsauer
 * @version $Revision: 24736 $
 */
public class PrincipalAdapter implements Principal, Serializable
{

   private static final long serialVersionUID = 1L;
   
   private final Principal principal;

   public PrincipalAdapter(Principal principal)
   {
      this.principal = principal;
   }

   public String getName()
   {
      return this.principal.getName();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((principal.getName() == null) ? 0 : principal.getName().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if ( !(obj instanceof Principal))
         return false;
      Principal other = (Principal) obj;
      if (getName() == null)
      {
         if (other.getName() != null)
            return false;
      }
      else if ( !getName().equals(other.getName()))
         return false;
      return true;
   }
   
}
