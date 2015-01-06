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
package org.eclipse.stardust.engine.core.persistence;

import org.eclipse.stardust.common.error.ConcurrencyException;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface IdentifiablePersistent extends Persistent
{
   /**
    * Retrieves the unique 64-bit identifier.
    */
   public long getOID();

   public void setOID(long oid);

   void lock() throws ConcurrencyException;

   void lock(int timeout) throws ConcurrencyException;
}
