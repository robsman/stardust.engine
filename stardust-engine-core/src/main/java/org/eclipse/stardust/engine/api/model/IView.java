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
package org.eclipse.stardust.engine.api.model;

import java.util.Iterator;

import org.eclipse.stardust.engine.core.model.utils.ModelElement;


/**
 * @author mgille
 */
public interface IView extends ModelElement
{
   String getName();

   void setName(String name);

   void addToViewables(IViewable viewable);

   void removeFromViewables(IViewable viewable);

   Iterator getAllViewables();

   IView createView(String name, String description, int elementOID);

   void addToViews(IView view);

   void removeFromViews(IView view);

   Iterator getAllViews();
}
