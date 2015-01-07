/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc.sequence;

import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;

/**
 * SequenceGenerator delivers unique sequence numbers for a specific type.
 * This can be configured with property <code>AuditTrail.SequenceGenerator</code>.
 */
public interface SequenceGenerator
{

   /**
    * Property key to be used for storage in Parameters
    */
   public static String UNIQUE_GENERATOR_PARAMETERS_KEY = "UniqueIdGenerator";
   
   void init(DBDescriptor dbDescriptor, SqlUtils sqlUtils);
   
   /**
    * Returns the next unused sequence number for type designated by typeDescriptor.<br />
    * Note: Numbers returned on successive calls needs to be <b>unique</b> and in <b>ascending order</b>.
    * 
    * @param typeDescriptor type to return the sequence number for
    * @param session session to use for SQL queries
    * @return next unused sequence number returned in ascending order on successive calls
    */
   public long getNextSequence(TypeDescriptor typeDescriptor, Session session);
   
}
