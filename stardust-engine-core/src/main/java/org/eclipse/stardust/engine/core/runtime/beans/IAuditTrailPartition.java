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
package org.eclipse.stardust.engine.core.runtime.beans;

import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.core.persistence.Persistent;


/**
 *
 */
public interface IAuditTrailPartition extends Persistent
{
   /**
    * Retrieves the unique 16-bit identifier.
    */
   short getOID();

   void setOID(short oid);

   void lock() throws ConcurrencyException;

   /**
    *
    */
   String getId();

   /**
    *
    */
   void setId(String id);

   /**
    *
    */
   String getDescription();

   /**
    *
    */
   void setDescription(String description);

}
