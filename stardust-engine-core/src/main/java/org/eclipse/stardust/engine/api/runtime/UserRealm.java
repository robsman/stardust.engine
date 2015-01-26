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

/**
 * Provides a client view on user realms.
 * 
 * @author sborn
 * @author rsauer
 * @version $Revision$
 */
public interface UserRealm extends RuntimeObject
{
   /**
    * Provides the ID of the realm.
    *
    * @return The realm ID.
    */
   public String getId();
   
   /**
    * Provides the human friendly name of the realm.
    * 
    * @return The realm name.
    */
   public String getName();
   
   /**
    * Provides an informal description of the realm.
    * 
    * @return The realm description.
    */
   public String getDescription();

   /**
    * Provides the ID of the partition the realm is associated with.
    * 
    * @return The partition ID.
    */
   public String getPartitionId();

   /**
    * Provides the OID of the partition the realm is associated with.
    * 
    * @return The partition OID.
    */
   public short getPartitionOid();
}
