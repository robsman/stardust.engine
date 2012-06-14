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

import org.eclipse.stardust.common.AttributeHolder;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.SplicingIterator;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.reflect.MethodDescriptor;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.AccessPointOwner;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Typeable;
import org.eclipse.stardust.engine.core.model.utils.Link;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
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

   public AccessPoint findAccessPoint(String id, Direction direction)
   {
      recalculateAccessPoints();

      AccessPoint ap = findAccessPoint(id, direction, persistentPoints);
      if(ap == null)
      {
         final List transientAps = (List) transientPoints.get();
         ap = findAccessPoint(id, direction, transientAps);
      }
      
      return ap;
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
   
   private AccessPoint findAccessPoint(String id, Direction direction, Iterable accessPoints)
   {
      Iterator i = accessPoints.iterator(); 
      while(i.hasNext())
      {
         String tmpId = id;
         AccessPoint point = (AccessPoint) i.next();
           
         //if the access point is an access point for a method,
         //expect a non generic method name
         if(isMethodAccessPoint(point))
         {
            tmpId = getSimpleMethodName(tmpId);
         }
         
         if (point.getId().equals(tmpId)
               && (direction == null || 
                   point.getDirection().equals(Direction.IN_OUT) || 
                   point.getDirection().equals(direction)))
         {
            return point;
         }
      }
      
      return null;
   }
   
   private boolean isMethodAccessPoint(AccessPoint point)
   {
      Object characteristics = point.getAttribute(PredefinedConstants.FLAVOR_ATT);
      if(JavaAccessPointType.METHOD.equals(characteristics))
      {
         return true;
      }
      
      return false;
   }
   
   private String getSimpleMethodName(String methodName)
   {
      MethodDescriptor md = Reflect.describeEncodedMethod(methodName);
      if(md != null)
      {
         return md.toString();
      }
      
      return methodName;
   }
}
