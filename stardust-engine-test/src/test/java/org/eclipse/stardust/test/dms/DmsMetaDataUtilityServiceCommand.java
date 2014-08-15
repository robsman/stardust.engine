/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.dms;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryAuditTrailUtils;

public class DmsMetaDataUtilityServiceCommand implements ServiceCommand
{

   private static final long serialVersionUID = 1L;

   private Document document;

   private boolean write;

   public DmsMetaDataUtilityServiceCommand(Document document, boolean write)
   {
      this.document = document;
      this.write = write;
   }

   @Override
   public Serializable execute(ServiceFactory sf)
   {
      if (write)
      {
         RepositoryAuditTrailUtils.storeDocument(document);
      }
      else
      {
         Document retrievedDoc = RepositoryAuditTrailUtils.retrieveDocument(document.getId());
         return retrievedDoc;
      }
      return null;

   }

}
