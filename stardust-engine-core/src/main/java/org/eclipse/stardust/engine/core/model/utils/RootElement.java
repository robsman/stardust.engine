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

import java.util.Iterator;
import java.util.Set;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface RootElement extends IdentifiableElement
{
   ModelElement lookupElement(int elementOID);

   void fireModelElementsLinked(ModelElement first, ModelElement second);

   void fireModelElementsUnlinked(ModelElement first, ModelElement second);

   void fireModelElementCreated(ModelElement element, ModelElement parent);

   void fireModelElementDeleted(ModelElement element, ModelElement parent);

   void deregister(ModelElement element);

   int getModelOID();

   void fireModelElementChanged(ModelElement element);

   Set getElementOIDs();

   int createElementOID();

   void register(ModelElement element);

   void addToModelListeners(ModelListener listener);

   void removeFromModelListeners(ModelListener listener);

   Iterator getAllModelListeners();

   void setModelOID(int oid);

   RootElement deepCopy();

   void mergeDifferences(Differences diff);

   void setLoading(boolean loading);

   int createTransientElementOID();
}
