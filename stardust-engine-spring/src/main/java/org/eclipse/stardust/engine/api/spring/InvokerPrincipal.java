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
package org.eclipse.stardust.engine.api.spring;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.stardust.engine.core.spi.security.PrincipalWithProperties;


/**
 * @author rsauer
 * @version $Revision$
 *
 * @deprecated use {@link org.eclipse.stardust.engine.core.security.InvokerPrincipal} instead
 */
@Deprecated
public class InvokerPrincipal implements PrincipalWithProperties, Serializable
{
   private static final long serialVersionUID = 7344238080033531281L;

   private org.eclipse.stardust.engine.core.security.InvokerPrincipal delegate;

   public InvokerPrincipal(String name, Map properties)
   {
      this.delegate = new org.eclipse.stardust.engine.core.security.InvokerPrincipal(name, properties);
   }

   public InvokerPrincipal(String name, Map properties, byte[] signature)
   {
      this.delegate = new org.eclipse.stardust.engine.core.security.InvokerPrincipal(name, properties, signature);
   }

   public String getName()
   {
      return delegate.getName();
   }

   public Map getProperties()
   {
      return delegate.getProperties();
   }

   public byte[] getSignature()
   {
      return delegate.getSignature();
   }

   public boolean equals(Object obj)
   {
      return delegate.equals(obj);
   }

   public String toString()
   {
      return "Spring Invoker Principal: " + getName();
   }
}
