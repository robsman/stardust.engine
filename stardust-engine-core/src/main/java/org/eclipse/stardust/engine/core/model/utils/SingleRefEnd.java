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
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SingleRefEnd extends SingleHook implements RefEnd
{
   public SingleRefEnd(ModelElement owner)
   {
      super(owner);
   }

   public void add(ModelElement element)
   {
      throw new UnsupportedOperationException();
   }

   public void __add__(ModelElement element)
   {
      super.add(element);
      element.addReference(this);
   }

   public void __remove__(ModelElement element)
   {
      super.remove(element);
      element.removeReference(this);
   }
}
