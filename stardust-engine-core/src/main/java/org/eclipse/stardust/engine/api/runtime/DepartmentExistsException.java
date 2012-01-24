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
 * Thrown to indicate that a department with same id already exists.
 * 
 * @author sborn
 * @version $Revision: 31061 $
 */
public class DepartmentExistsException extends PublicException
{
   private static final long serialVersionUID = 1L;

   public DepartmentExistsException(String id)
   {
      super(BpmRuntimeError.ATDB_DEPARTMENT_EXISTS.raise(id));
   }
}