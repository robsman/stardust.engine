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
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.error.PublicException;



/**
 * Thrown if another user group with the specified id already exists.
 * 
 * @author sborn
 * @version $Revision$
 */
public class UserGroupExistsException extends PublicException
{
   public UserGroupExistsException(String id)
   {
      super(BpmRuntimeError.ATDB_USER_GROUP_ID_EXISTS.raise(id));
   }
}
