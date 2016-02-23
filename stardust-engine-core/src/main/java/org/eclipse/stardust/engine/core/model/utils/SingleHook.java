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

import java.util.Collections;
import java.util.Iterator;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SingleHook  extends Hook
{
   private ModelElement element;

   public SingleHook(ModelElement owner)
   {
      super(owner);
   }

   public ModelElement getElement()
   {
       return element;
   }

   public void setElement(ModelElement element)
   {
      this.element = element;
   }

   public boolean isEmpty()
   {
      return element == null;
   }

   public void remove(ModelElement element)
   {
      if (element == this.element && element != null)
      {
         this.element = null;
      }
      else
      {
//         Assert.lineNeverReached();
      }
   }

   public void add(ModelElement element)
   {
      //if (!isEmpty())
      //{
      //   throw new InternalException("Trying to add element to reference with cardinality 1");
      //}
      setElement(element);
   }

   public Iterator iterator()
   {
      if (getElement() != null)
      {
         return Collections.singletonList(getElement()).iterator();
      }
      else
      {
         return Collections.EMPTY_LIST.iterator();
      }
   }

}
