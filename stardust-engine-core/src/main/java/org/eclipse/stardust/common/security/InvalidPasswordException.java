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
package org.eclipse.stardust.common.security;

import java.util.List;

import org.eclipse.stardust.common.error.ErrorCase;
import org.eclipse.stardust.common.error.PublicException;


/**
 * Thrown when a password is not valid for given rules.
 * 
 * @author Barry.Grotjahn
 * @version $Revision: $
 */
public class InvalidPasswordException extends PublicException
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private List<FailureCode> failureCodes;
   
   /**
    * Returns the list of failure codes, describing what is not valid.
    */
   public List<FailureCode> getFailureCodes()
   {
      return failureCodes;
   }
   
   public static enum FailureCode
   {
      MINIMAL_PASSWORD_LENGTH, 
      LETTER, 
      DIGITS,
      MIXED_CASE,
      PUNCTUATION,
      PREVIOUS_PASSWORDS,
      DIFFERENT_CHARACTERS
   };
   
   public InvalidPasswordException(ErrorCase errorCase, List<FailureCode> failureCodes)
   {
      super(errorCase);
      this.failureCodes = failureCodes;
   }
}