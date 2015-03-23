/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.command.impl;


/**
 * Is thrown if {@link StartProcessWithDocumentsCommand} fails to store an {@link StartProcessInputDocument}.
 *
 * @author Roland.Stamm
 */
public class StartProcessCommandException extends RuntimeException
{

   private static final long serialVersionUID = -431772655763932301L;

   private String faultCode;

   public StartProcessCommandException(String message, String faultCode)
   {
      super(message);
      this.faultCode = faultCode;
   }

   public String getFaultCode()
   {
      return faultCode;
   }

}
