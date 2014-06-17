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
package org.eclipse.stardust.engine.core.runtime.ejb;

import java.io.Serializable;

import org.eclipse.stardust.engine.core.security.InvokerPrincipal;

/**
 * @author sauer
 * @version $Revision: $
 */
public class TunneledContext implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final InvokerPrincipal invokerPrincipal;

   public TunneledContext(InvokerPrincipal invokerPrincipal)
   {
      this.invokerPrincipal = invokerPrincipal;
   }

   public InvokerPrincipal getInvokerPrincipal()
   {
      return invokerPrincipal;
   }
}
