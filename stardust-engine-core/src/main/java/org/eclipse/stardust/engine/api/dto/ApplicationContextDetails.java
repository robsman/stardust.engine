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
package org.eclipse.stardust.engine.api.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.ConcatenatedList;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ApplicationContextDetails extends ModelElementDetails
      implements ApplicationContext
{
   private static final long serialVersionUID = 2L;
   
   private final List inMappings;
   private final List outMappings;
   
   private Map typeAttributes = Collections.EMPTY_MAP;

   private AccessPointDetailsEvaluator apEvaluator;

   public ApplicationContextDetails(IApplicationContext context, IActivity activity)
   {
      super(context, context.getId(), context.getName(), context.getDescription());
      
      inMappings = DetailsFactory.createCollection(
            new FilteringIterator(activity.getAllDataMappings(), new Predicate()
            {
               public boolean accept(Object o)
               {
                  IDataMapping mapping = ((IDataMapping) o);
                  return getId().equals(mapping.getContext()) && mapping.getDirection() == Direction.IN;
               }
            }),
            IDataMapping.class, DataMappingDetails.class);

      outMappings = DetailsFactory.createCollection(
            new FilteringIterator(activity.getAllDataMappings(), new Predicate()
            {
               public boolean accept(Object o)
               {
                  IDataMapping mapping = ((IDataMapping) o);
                  return getId().equals(mapping.getContext()) && mapping.getDirection() == Direction.OUT;
               }
            }),
            IDataMapping.class, DataMappingDetails.class);

      IApplicationContextType contextType = (IApplicationContextType) context.getType();

      if (null != contextType)
      {
         typeAttributes = new HashMap(contextType.getAllAttributes());
      }
      
      boolean isInteractive = AccessPointDetailsEvaluator.isInteractive(activity);
      apEvaluator = new AccessPointDetailsEvaluator(context, isInteractive,
            getAllAttributes(), getAllTypeAttributes());
   }

   public List getAllDataMappings()
   {
      return new ConcatenatedList(inMappings, outMappings);
   }

   public List getAllInDataMappings()
   {
      return Collections.unmodifiableList(inMappings);
   }

   public List getAllOutDataMappings()
   {
      return Collections.unmodifiableList(outMappings);
   }

   public DataMapping getDataMapping(Direction direction, String id)
   {
      if (direction == Direction.IN)
      {
         return (DataMapping) ModelApiUtils.firstWithId(inMappings.iterator(), id);
      }
      else if (direction == Direction.OUT)
      {
         return (DataMapping) ModelApiUtils.firstWithId(outMappings.iterator(), id);
      }
      else
      {
         return (DataMapping) ModelApiUtils.firstWithId(getAllDataMappings().iterator(), id);
      }
   }

   public List getAllAccessPoints()
   {
      return Collections.unmodifiableList(apEvaluator.getAccessPoints());
   }

   public org.eclipse.stardust.engine.api.model.AccessPoint getAccessPoint(String id)
   {
      for (Iterator source = apEvaluator.getAccessPoints().iterator(); source.hasNext();)
      {
         org.eclipse.stardust.engine.api.model.AccessPoint item =
               (org.eclipse.stardust.engine.api.model.AccessPoint) source.next();
         if (CompareHelper.areEqual(id, item.getId()))
         {
            return item;
         }
      }
      return null;
   }

   public Map getAllTypeAttributes()
   {
      return Collections.unmodifiableMap(typeAttributes);
   }

   public Object getTypeAttribute(String name)
   {
      return typeAttributes.get(name);
   }
}
