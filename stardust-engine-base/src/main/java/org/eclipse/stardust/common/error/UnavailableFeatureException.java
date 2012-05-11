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
public class UnavailableFeatureException extends Exception
{
   private static final long serialVersionUID = -3678715655934133237L;

   /**
    *
    */
   public UnavailableFeatureException(String feature, String reason)
   {
      super("The product feature \"" + feature + "\" is not available: "
            + reason + ".");
   }
}