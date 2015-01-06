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
package org.eclipse.stardust.engine.core.model.beans;

import java.util.Iterator;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElementBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 *
 * @author ubirkemeyer, jmahmood
 * @version $Revision$
 */
public class ApplicationContextBean extends ModelElementBean
      implements IApplicationContext
{
   // @todo (france, ub): store the type ref here instead
   static final String TYPE_ATT = "Type";
   private String type;

   private Link persistentAccessPoints = new Link(this, "Access Points");
   private transient AccessPointJanitor accessPoints;

   ApplicationContextBean()
   {
   }

   public ApplicationContextBean(String id, boolean isTransient)
   {
      this.type = id;
      this.isTransient = isTransient;
   }

   public String getId()
   {
      return type;
   }

   public String toString()
   {
      return "ApplicationContext: " + type;
   }

   public Iterator getAllAccessPoints()
   {
      return getAccessPointLink().iterator();
   }

   private AccessPointJanitor getAccessPointLink()
   {
      if (accessPoints == null)
      {
         accessPoints = new AccessPointJanitor(persistentAccessPoints);
      }
      return accessPoints;
   }


   public PluggableType getType()
   {
      return ((IModel) getModel()).findApplicationContextType(type);
   }

   public String getName()
   {
      IApplicationContextType ct = (IApplicationContextType) getType();
      return ct == null ? type : ct.getName();
   }

   public void addToPersistentAccessPoints(IAccessPoint accessPoint)
   {
      persistentAccessPoints.add(accessPoint);
   }

   public void removeFromPersistentAccessPoints(IAccessPoint accessPoint)
   {
      persistentAccessPoints.remove(accessPoint);
   }


   public void removeFromAccessPoints(AccessPoint accessPoint)
   {
      getAccessPointLink().remove(accessPoint);
   }

   public AccessPoint findAccessPoint(String id)
   {
      return findAccessPoint(id, null);
   }
   
   public AccessPoint findAccessPoint(String id, Direction direction)
   {
      return getAccessPointLink().findAccessPoint(id, direction);
   }

   public AccessPoint createAccessPoint(String id, String name, Direction direction,
         IDataType type, int elementOID)
   {
      IAccessPoint result = new AccessPointBean(id, name, direction);
      addToPersistentAccessPoints(result);
      result.register(elementOID);
      result.setDataType(type);
      return result;
   }

   public Iterator getAllInAccessPoints()
   {
      return getAccessPointLink().getAllInAccessPoints();
   }

   public Iterator getAllOutAccessPoints()
   {
      return getAccessPointLink().getAllOutAccessPoints();
   }

   public String getProviderClass()
   {
      return getType().getStringAttribute(PredefinedConstants.ACCESSPOINT_PROVIDER_ATT);
   }

   public void addIntrinsicAccessPoint(AccessPoint ap)
   {
      getAccessPointLink().addIntrinsicAccessPoint(ap);
   }

   public Iterator getAllPersistentAccessPoints()
   {
      return persistentAccessPoints.iterator();
   }

   public void markModified()
   {
      super.markModified();
      getAccessPointLink().setDirty();
   }
}
