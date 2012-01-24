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
package org.eclipse.stardust.engine.core.model.utils;

import java.io.Serializable;

import org.eclipse.stardust.common.AttributeHolder;


/**
 * @author fherinean
 * @version $Revision$
 */
public interface ModelElement extends Serializable, AttributeHolder, RuntimeAttributeHolder
{
   String getDescription();

   public void setDescription(String description);

   void delete();

   RootElement getModel();

   void addReference(Hook reference);

   void setParent(ModelElement parent);

   void removeReference(Hook reference);

   ModelElement getParent();

   public int getElementOID();

   void register(int oid);

   /**
    * Retrieves the unique 64-bit identifier.
    */
   public long getOID();

   void setElementOID(int elementOID);

   boolean isTransient();

   boolean isPredefined();

   void setPredefined(boolean predefined);

   /**
    * This id has to be unique in the scope of the parent model element.
    *
    * @return
    */
   String getUniqueId();
}
