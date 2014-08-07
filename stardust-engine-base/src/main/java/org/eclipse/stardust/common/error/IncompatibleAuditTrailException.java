/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
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
 * Thrown when the AuditTrail is not compatible with the program.
 */
public class IncompatibleAuditTrailException extends PublicException
{
   private static final long serialVersionUID = 1L;

   public IncompatibleAuditTrailException(String message)
   {
      super(message);
   }
}
