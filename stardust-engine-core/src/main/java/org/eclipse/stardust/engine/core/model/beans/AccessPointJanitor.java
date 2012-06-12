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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.reflect.MethodDescriptor;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPointOwner;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Typeable;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ModelAware;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AccessPointJanitor
{
   private AtomicReference transientPoints = new AtomicReference();

   private final Link persistentPoints;

   public AccessPointJanitor(Link persistentPoints)
   {
      this.persistentPoints = persistentPoints;
   }

   public Iterator iterator()
   {
      recalculateAccessPoints();
      
      final List transientAps = (List) transientPoints.get();
      if (0 == persistentPoints.size())
      {
         return transientAps.iterator();
      }
      else if (transientAps.isEmpty())
      {
         return persistentPoints.iterator();
      }
      else
      {
         return new SplicingIterator(persistentPoints.iterator(),
               CollectionUtils.newList(transientAps).iterator());
      }
   }

   private void recalculateAccessPoints()
   {
      if (null == transientPoints.get())
      {
         List aps = CollectionUtils.newList();

         AccessPointOwner owner = (AccessPointOwner) persistentPoints.getOwner();
         String providerClass = owner.getProviderClass();
         if ( !StringUtils.isEmpty(providerClass))
         {
            AccessPointProvider provider = (AccessPointProvider)
                  Reflect.getInstance(providerClass);
            
            if ((provider instanceof ModelAware) && (owner instanceof ModelElement))
            {
               ((ModelAware) provider).setModel((IModel) ((ModelElement) owner).getModel());
            }
            
            Map typeProps = owner instanceof Typeable ?
                  ((Typeable) owner).getType().getAllAttributes() : null;
            for (Iterator i = provider.createIntrinsicAccessPoints(
                  ((AttributeHolder) owner).getAllAttributes(), typeProps); i.hasNext();)
            {
               AccessPoint ap = (AccessPoint) i.next();
               String id = ap.getId();
               
               aps.add(ap);
            }
         }
         
         transientPoints.compareAndSet(null, aps);
      }
   }

   public Iterator getAllInAccessPoints()
   {
      recalculateAccessPoints();

      return new FilteringIterator(iterator(), new Predicate()
      {
         public boolean accept(Object point)
         {
            AccessPoint candidate = (AccessPoint) point;
            return candidate.getDirection() == Direction.IN
                  || candidate.getDirection() == Direction.IN_OUT;
         }
      });
   }

   public AccessPoint findAccessPoint(String accesspointId, Direction direction)
   {
      final String simpleId;
      MethodDescriptor descriptor = Reflect.describeEncodedMethod(accesspointId);
      if(descriptor != null)
      {
         simpleId = descriptor.toString();
      }
      else
      {
         simpleId = accesspointId;
      }

      recalculateAccessPoints();

      for (int i=0; i<persistentPoints.size(); i++)
      {
         AccessPoint point = (AccessPoint) persistentPoints.get(i);
         if (point.getId().equals(simpleId) 
               && (direction == null || 
                     point.getDirection().equals(Direction.IN_OUT) || 
                     point.getDirection().equals(direction)))
         {
            return point;
         }
      }
        
      final List transientAps = (List) transientPoints.get();
      for (int i = 0; i < transientAps.size(); ++i)
      {
         AccessPoint point = (AccessPoint) transientAps.get(i);
         
         String pointId = point.getId();
         System.out.println("point id: "+pointId);
         
         if (point.getId().equals(simpleId)
               && (direction == null || 
                   point.getDirection().equals(Direction.IN_OUT) || 
                   point.getDirection().equals(direction)))
         {
            return point;
         }
      }
      return null;
   }

   public Iterator getAllOutAccessPoints()
   {
      recalculateAccessPoints();

      return new FilteringIterator(iterator(), new Predicate()
      {
         public boolean accept(Object point)
         {
            AccessPoint candidate = (AccessPoint) point;
            return candidate.getDirection() == Direction.OUT
                  || candidate.getDirection() == Direction.IN_OUT;
         }
      });
   }

   public synchronized void addIntrinsicAccessPoint(AccessPoint ap)
   {      
      List aps = (List) transientPoints.get();
      if (null == aps)
      {
         transientPoints.compareAndSet(null, CollectionUtils.newList());
         aps = (List) transientPoints.get();
      }
      
      aps.add(ap);
   }

   public synchronized void remove(AccessPoint accessPoint)
   {
      List transientAps = (List) transientPoints.get();
      
      if ( !transientAps.remove(accessPoint) && accessPoint instanceof ModelElement)
      {
         persistentPoints.remove((ModelElement) accessPoint);
      }
   }
   
   public void setDirty()
   {
      transientPoints.set(null);
   }
}
