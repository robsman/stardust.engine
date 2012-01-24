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

import java.lang.reflect.Field;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MultiRef extends MultiHook implements Reference
{
   private String otherRole;
   private String name;

   public MultiRef(ModelElement owner, String name, String otherRole)
   {
      super(owner);
      this.name = name;
      this.otherRole = otherRole;
   }

   public MultiRef(ModelElement owner, String name)
   {
      super(owner);
      this.name = name;
   }

   public void add(ModelElement element)
   {
      // @todo (egypt): assert not null
      getOwner().markModified();

      super.add(element);
      element.addReference(this);
      setOtherRole(element);
      RootElement model = getOwner().getModel();
      // @todo (egypt): there should be a parameter to control firing events
      if (model != null)
      {
         model.fireModelElementsLinked(getOwner(), element);
      }
   }

   public void remove(ModelElement element)
   {
      if (element == null)
      {
         return;
      }
      super.remove(element);
      removeOtherRole(element);
      RootElement model = getOwner().getModel();
      // @todo (egypt): there should be a parameter to control firing events
      if (model != null)
      {
         model.fireModelElementsUnlinked(getOwner(), element);
      }
   }

   private void removeOtherRole(ModelElement element)
   {
      if (otherRole != null)
      {
         try
         {
            Field field = Reflect.getField(element.getClass(), otherRole);
            RefEnd otherEnd = (RefEnd) field.get(element);
            otherEnd.__remove__(getOwner());
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
   }

   void setOtherRole(ModelElement element)
   {
      if (otherRole != null)
      {
         try
         {
            Field field = Reflect.getField(element.getClass(), otherRole);
            RefEnd otherEnd = (RefEnd) field.get(element);
            otherEnd.__add__(getOwner());
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
      }
   }

}
