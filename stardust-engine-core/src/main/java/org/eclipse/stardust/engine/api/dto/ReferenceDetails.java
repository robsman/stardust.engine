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

import java.io.Serializable;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IReference;
import org.eclipse.stardust.engine.api.model.Reference;


public class ReferenceDetails implements Reference, Serializable
{
   private static final long serialVersionUID = 2L;

   private final String id;
   private final String namespace;
   private final String qualifiedId;
   private final int modelOid;

   public ReferenceDetails(IReference ref)
   {
      String id = ref.getId();
      IModel model = ref.getExternalPackage().getReferencedModel();
      String namespace = model == null ? "" : model.getId();
      this.qualifiedId = '{' + namespace + '}' + id;
      this.namespace = qualifiedId.substring(1, namespace.length() + 1);
      this.id = qualifiedId.substring(namespace.length() + 2);
      modelOid = model == null ? 0 : model.getModelOID();
   }

   public String getId()
   {
      return id;
   }

   public String getNamespace()
   {
      return namespace;
   }

   public String getQualifiedId()
   {
      return qualifiedId;
   }

   public int getModelOid()
   {
      return modelOid;
   }
}
