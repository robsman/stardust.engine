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

import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;


public abstract class AuditTrailModelElementDetails extends ModelElementDetails
{
   private final long runtimeElementOid;

   protected AuditTrailModelElementDetails(IdentifiableElement element)
   {
      super(element);

      this.runtimeElementOid = ModelManagerFactory.getCurrent().getRuntimeOid(element);
   }

   public AuditTrailModelElementDetails(AuditTrailModelElementDetails template)
   {
      super(template);
      this.runtimeElementOid = template.runtimeElementOid;
   }

   public long getRuntimeElementOID()
   {
      return runtimeElementOid;
   }
}
