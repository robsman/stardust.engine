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
import org.eclipse.stardust.engine.core.persistence.IdentifiablePersistent;
import org.eclipse.stardust.engine.core.persistence.PersistenceController;


/**
 * @author Florin.Herinean
 */
public interface IDepartment extends IdentifiablePersistent
{
   String getId();
   
   String getName();

   IDepartment getParentDepartment();
   
   String getDescription();
   
   long getRuntimeOrganizationOID();
   
   public static final IDepartment NULL = new IDepartment()
   {
      public String getDescription() {return null;}
      public String getId() {return null;}
      public String getName() {return null;}
      public IDepartment getParentDepartment() {return null;}
      public long getRuntimeOrganizationOID() {return 0;}
      public long getOID() {return 0;}
      public void lock() throws ConcurrencyException {}
      public void setOID(long oid) {}
      public void delete() {}
      public void delete(boolean writeThrough) {}
      public void disconnectPersistenceController() {}
      public void fetch() {}
      public PersistenceController getPersistenceController() {return null;}
      public void markCreated() {}
      public void markModified() {}
      public void markModified(String fieldName) {}
      public void setPersistenceController(PersistenceController PersistenceController) {}
   };
}
