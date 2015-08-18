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
package org.eclipse.stardust.engine.core.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.spi.security.PrincipalWithProperties;


/**
 * @author rsauer
 * @version $Revision: 10353 $
 */
public class InvokerPrincipal implements PrincipalWithProperties, Serializable
{
   public static final String PRP_SIGNED_PRINCIPAL = InvokerPrincipal.class.getName() + ".SignedPrincipal";

   private static final long serialVersionUID = 1L;

   private final String name;

   private final Map properties;

   private final byte[] signature;

   public InvokerPrincipal(String name, Map properties)
   {
      this.name = name;
      this.properties = properties;
      this.signature = null;
   }

   public InvokerPrincipal(String name, Map properties, byte[] signature)
   {
      this.name = name;
      this.properties = properties;
      this.signature = signature;
   }

   public String getName()
   {
      return name;
   }

   public Map getProperties()
   {
      return properties;
   }

   public byte[] getSignature()
   {
      return signature;
   }

   public boolean equals(Object obj)
   {
      return (obj instanceof InvokerPrincipal)
            && CompareHelper.areEqual(getName(), ((InvokerPrincipal) obj).getName())
            && CompareHelper.areEqual(getProperties(), ((InvokerPrincipal) obj).getProperties())
            && Arrays.equals(getSignature(), ((InvokerPrincipal) obj).getSignature());
   }

   public String toString()
   {
      return "Invoker Principal: " + getName();
   }
}
