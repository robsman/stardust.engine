/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.error.PublicException;



/**
 * Thrown to indicate that a deputy user with same oid already exists for user.
 * 
 * @author stephan.born
 * @version $Revision: $
 */
public class DeputyExistsException extends PublicException
{
   private static final long serialVersionUID = 1L;

   public DeputyExistsException(long userOid, long deputyUserOid)
   {
      super(BpmRuntimeError.ATDB_DEPUTY_EXISTS.raise(userOid, deputyUserOid));
   }
}