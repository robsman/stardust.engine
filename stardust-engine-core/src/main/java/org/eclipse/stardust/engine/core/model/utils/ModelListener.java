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

/**
 * Implementors of this interface are listening to changes on an arbitrary
 * object-oriented model.
 */
public interface ModelListener
{
   /**
    * Indicates, that a new workflow model element has been created.
    */
   public void modelElementCreated(ModelElement element, ModelElement parent);

   /**
    * Indicates, that workflow model element has been deleted.
    */
   public void modelElementDeleted(ModelElement element, ModelElement parent);

   /**
    * Indicates, that a workflow model element has changed its state (name, description etc.).
    */
   public void modelElementChanged(ModelElement element);

   /**
    * Indicates, that an association is established between two workflow model elements.
    */
   public void modelElementsLinked(ModelElement first, ModelElement second);

   /**
    * Indicates, that an association is established between two workflow model elements.
    */
   public void modelElementsUnlinked(ModelElement first, ModelElement second);
}
