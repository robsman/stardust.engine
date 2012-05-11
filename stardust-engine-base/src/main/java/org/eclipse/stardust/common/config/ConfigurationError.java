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
package org.eclipse.stardust.common.config;

import org.eclipse.stardust.common.error.ErrorCase;

/**
 * All configuration related errors.
 * 
 * Please take care if you refactor the class name or <code>ConfigurationError</code> fields.
 * It can be that they're loaded per reflection.
 * @author rottstock
 * @version $Revision: $
 */
public class ConfigurationError extends ErrorCase
{
   private static final long serialVersionUID = -727047172288805951L;
   
   // used by DumpReader
   public final static ConfigurationError LIC_WRONG_RELEASE = new ConfigurationError("LIC00001",
         String.copyValueOf(new char[]
         { 'L', 'i', 'c', 'e', 'n', 's', 'e', ' ', 'i', 's', ' ', 'n', 'o', 't', ' ',
           'v', 'a', 'l', 'i', 'd', ' ', 'f', 'o', 'r', ' ', 't', 'h', 'i', 's',
           ' ', 'r', 'e', 'l', 'e', 'a', 's', 'e', '.' }));
   public final static ConfigurationError LIC_EXPIRED = new ConfigurationError("LIC00002",
         String.copyValueOf(new char[]
         { 'Y', 'o', 'u', 'r', ' ', 'l', 'i', 'c', 'e', 'n', 's', 'e', ' ', 'f', 'o',
           'r', ' ', 'r', 'u', 'n', 'n', 'i', 'n', 'g', ' ', 't', 'h', 'e', ' ', 'c',
           'o', 'm', 'p', 'o', 'n', 'e', 'n', 't', ' ', 'o', 'f', ' ', 't', 'h', 'e', ' ', 'I', 'n', 'f', 'i', 'n',
           'i', 't', 'y', ' ', '(', 'T', 'M', ')', ' ', 'P', 'r', 'o', 'c', 'e', 's',
           's', ' ', 'W', 'o', 'r', 'k', 'b', 'e', 'n', 'c', 'h', '\n', 'h', 'a', 's', ' ',
           'e', 'x', 'p', 'i', 'r', 'e', 'd', '.', '\n', '\n', 'P', 'l', 'e', 'a', 's', 'e', ' ', 
           'c', 'o', 'n', 't', 'a', 'c', 't', ' ', 'y', 'o', 'u', 'r', ' ', 'I', 'n', 'f', 'i', 'n',
           'i', 't', 'y', ' ', 's', 'a', 'l', 'e', 's', ' ', 'r', 'e', 'p', 'r',
           'e', 's', 'e', 'n', 't', 'a', 't', 'i', 'v', 'e', ' ', 't', 'o', ' ', 'r', 'e', 'n', 'e', 'w', ' ', 'i', 't', '.' }));
   
   private final String defaultMessage;
   
   protected ConfigurationError(String id, String defaultMessage)
   {
      super(id);
      this.defaultMessage = defaultMessage;
   }

   public String getDefaultMessage()
   {
      return defaultMessage;
   }
}
