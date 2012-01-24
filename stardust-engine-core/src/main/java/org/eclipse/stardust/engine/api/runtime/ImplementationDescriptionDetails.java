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

import java.io.Serializable;

public class ImplementationDescriptionDetails implements ImplementationDescription, Serializable
{
   private static final long serialVersionUID = 1L;

   private String processInterfaceId;

   private long implementationModelOid;

   private String implementationProcessId;

   private boolean isPrimaryImplementation;

   private long interfaceModelOid;

   private boolean isActive;

   public ImplementationDescriptionDetails(String processInterfaceId,
         long implementationModelOid, String implementationProcessId,
         boolean isPrimaryImplementation, long interfaceModelOid, boolean isActive)
   {
      super();
      this.processInterfaceId = processInterfaceId;
      this.implementationModelOid = implementationModelOid;
      this.implementationProcessId = implementationProcessId;
      this.isPrimaryImplementation = isPrimaryImplementation;
      this.interfaceModelOid = interfaceModelOid;
      this.isActive = isActive;
   }

   protected ImplementationDescriptionDetails(ImplementationDescriptionDetails template)
   {
      super();
      this.processInterfaceId = template.processInterfaceId;
      this.implementationModelOid = template.implementationModelOid;
      this.implementationProcessId = template.implementationProcessId;
      this.isPrimaryImplementation = template.isPrimaryImplementation;
      this.interfaceModelOid = template.interfaceModelOid;
      this.isActive = template.isActive;
   }

   public String getProcessInterfaceId()
   {
      return processInterfaceId;
   }

   public long getImplementationModelOid()
   {
      return implementationModelOid;
   }

   public boolean isPrimaryImplementation()
   {
      return isPrimaryImplementation;
   }

   public boolean isActive()
   {
      return isActive;
   }

   public long getInterfaceModelOid()
   {
      return interfaceModelOid;
   }

   public String getImplementationProcessId()
   {
      return implementationProcessId;
   }

}
