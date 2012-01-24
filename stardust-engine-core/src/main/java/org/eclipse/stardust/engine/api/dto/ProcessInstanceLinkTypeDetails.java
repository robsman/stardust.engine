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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLinkType;

public class ProcessInstanceLinkTypeDetails implements ProcessInstanceLinkType
{
   private static final long serialVersionUID = 1L;
   
   private long oid;
   private String id;
   private String description;
   
   public ProcessInstanceLinkTypeDetails(long oid, String id, String description)
   {
      this.oid = oid;
      this.id = id;
      this.description = description;
   }

   public long getOID()
   {
      return oid;
   }

   public String getId()
   {
      return id;
   }

   public String getDescription()
   {
      return description;
   }
}
