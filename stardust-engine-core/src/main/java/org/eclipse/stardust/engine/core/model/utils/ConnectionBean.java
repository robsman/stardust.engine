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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.reflect.Reflect;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class ConnectionBean extends ModelElementBean implements Connection
{
   private ModelElement first;
   private ModelElement second;
   private String rightRole;
   private String leftRole;

   protected ConnectionBean()
   {
   }

   protected ConnectionBean(ModelElement first, ModelElement second)
   {
      this.first = first;
      this.second = second;
   }

   public ConnectionBean(ModelElement first)
   {
      this.first = first;
   }

   public void delete()
   {
      if (getModel() != null)
      {
         getModel().fireModelElementDeleted(this, getParent());
         //         getModel().fireModelElementsUnlinked(first, second);
      }
      super.delete();
   }

   public ModelElement getFirst()
   {
      return first;
   }

   public ModelElement getSecond()
   {
      return second;
   }

   public void connect(String leftRole, String rightRole)
   {
      this.leftRole = leftRole;
      this.rightRole = rightRole;
   }

   public void setFirst(ModelElement first)
   {
      markModified();
      detachEndPoint(this.first, leftRole);
      this.first = first;
      attachEndPoint(first, leftRole);
   }

   public void setSecond(ModelElement second)
   {
      markModified();
      detachEndPoint(this.second, rightRole);
      this.second = second;
      attachEndPoint(second, rightRole);
   }

   private void detachEndPoint(ModelElement endPoint, String role)
   {
      if (role == null || endPoint == null)
      {
         return;
      }
      Collection connEnd = (Collection) Reflect.getFieldValue(endPoint, role);
      if (connEnd != null)
      {
         connEnd.remove(this);
      }
   }

   public void attachEndPoint(ModelElement endPoint, String role)
   {
      if (role == null || endPoint == null)
      {
         return;
      }
      Collection connEnd = (Collection) Reflect.getFieldValue(endPoint, role);
      if (connEnd == null)
      {
         connEnd = CollectionUtils.newList();
         Reflect.setFieldValue(endPoint, role, connEnd);
      }
      connEnd.add(this);
   }
}
