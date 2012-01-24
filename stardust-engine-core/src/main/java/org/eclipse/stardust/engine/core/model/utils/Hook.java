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

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class Hook
{
   private ModelElement owner;

   protected Hook(ModelElement owner)
   {
      this.owner = owner;
   }

   public abstract void remove(ModelElement element);

   public ModelElement getOwner()
   {
      return owner;
   }

   public abstract void add(ModelElement element);

   public abstract Iterator iterator();
}
