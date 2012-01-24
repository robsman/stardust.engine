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
import java.util.Map;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.engine.core.spi.security.PrincipalWithProperties;


/**
 * @author rsauer
 * @version $Revision: 10353 $
 */
public class InvokerPrincipal implements PrincipalWithProperties, Serializable
{
   private static final long serialVersionUID = 1L;

   private final String name;

   private final Map properties;

   public InvokerPrincipal(String name, Map properties)
   {
      this.name = name;
      this.properties = properties;
   }

   public String getName()
   {
      return name;
   }

   public Map getProperties()
   {
      return properties;
   }

   public boolean equals(Object obj)
   {
      return (obj instanceof InvokerPrincipal)
            && CompareHelper.areEqual(getName(), ((InvokerPrincipal) obj).getName())
            && CompareHelper.areEqual(getProperties(),
                  ((InvokerPrincipal) obj).getProperties());
   }

   public String toString()
   {
      return "Invoker Principal: " + getName();
   }

}
