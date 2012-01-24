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
package org.eclipse.stardust.common.error;

/**
 *
 */
public class ResourceException extends ApplicationException
{
   private static final long serialVersionUID = 2L;
   
   /**
    * This constructor is used for exception conversion.
    */
   public ResourceException(String message, Throwable e)
   {
      super(message, e);
   }
}
