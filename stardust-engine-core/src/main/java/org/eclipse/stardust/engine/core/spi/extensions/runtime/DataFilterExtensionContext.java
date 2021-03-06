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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.SqlBuilderBase.DataAttributeKey;
import org.eclipse.stardust.engine.core.persistence.Join;


/**
 * Context object to access all data filters relating to the same query
 */
public class DataFilterExtensionContext
{
   private Object content;

   private Map<String, List<AbstractDataFilter>> dataFiltersByDataId;

   private final List<Join> joins = new LinkedList<Join>();

   private boolean useDistinct = false;

   private boolean isFilterUsedInAndTerm = true;
   
   private Set<DataAttributeKey> clusteredFilters = CollectionUtils.newHashSet();

   public DataFilterExtensionContext(List<AbstractDataFilter> dataFilters)
   {
      dataFiltersByDataId = createFiltersByDataIdMap(dataFilters);
   }

   /**
    * Creates DataFilterExtensionContext by searching all AbstractDataFilters in the filterAndTerm
    * @param filterAndTerm
    */
   public DataFilterExtensionContext(FilterAndTerm filterAndTerm)
   {
      List<AbstractDataFilter> dataFilters = new LinkedList<AbstractDataFilter>();
      findDataFilters(filterAndTerm, dataFilters);
      dataFiltersByDataId = createFiltersByDataIdMap(dataFilters);
   }

   private void findDataFilters(FilterTerm filterTerm, List<AbstractDataFilter> result)
   {
      List<FilterCriterion> parts = filterTerm.getParts();
      for (FilterCriterion criterion : parts)
      {
         if (criterion instanceof AbstractDataFilter)
         {
            result.add((AbstractDataFilter) criterion);
         }
         else if (criterion instanceof FilterTerm)
         {
            findDataFilters((FilterTerm) criterion, result);
         }
      }
   }

   private Map<String, List<AbstractDataFilter>> createFiltersByDataIdMap(
         List<AbstractDataFilter> dataFilters)
   {
      Map<String, List<AbstractDataFilter>> filtersByDataId = CollectionUtils.newHashMap();

      for (AbstractDataFilter filter : dataFilters)
      {
         String dataId = filter.getDataID();
         List<AbstractDataFilter> l = filtersByDataId.get(dataId);
         if (l == null)
         {
            l = new LinkedList<AbstractDataFilter>();
            filtersByDataId.put(dataId, l);
         }
         l.add(filter);
      }

      return filtersByDataId;
   }

   /**
    * Set free form context specific to the data type
    * @param content
    */
   public void setContent(Object content)
   {
      this.content = content;
   }

   public <T> T getContent()
   {
      return (T) content;
   }

   public Map<String, List<AbstractDataFilter>> getDataFiltersByDataId()
   {
      return dataFiltersByDataId;
   }

   public void removeJoin(Join join)
   {
      this.joins.remove(join);
   }

   public void addJoin(Join join)
   {
      this.joins.add(join);
   }

   /**
    * @return all additional joins for the query that have been created
    * to handle data type specifics
    */
   public List<Join> getJoins()
   {
      return Collections.unmodifiableList(this.joins);
   }

   public void useDistinct(boolean useDistinct)
   {
      this.useDistinct = useDistinct;
   }

   public boolean useDistinct()
   {
      return useDistinct;
   }

   public boolean isFilterUsedInAndTerm()
   {
      return isFilterUsedInAndTerm;
   }

   public void setFilterUsedInAndTerm(boolean isFilterUsedInAndTerm)
   {
      this.isFilterUsedInAndTerm = isFilterUsedInAndTerm;
   }

   public boolean isClusteredFilter(AbstractDataFilter filter, Set<IData> datas)
   {
      boolean isClusteredFilter = false;
      if (datas.isEmpty())
      {
         isClusteredFilter = clusteredFilters.contains(new DataAttributeKey(filter));
      }
      else
      {
         for (IData data : datas)
         {
            if (clusteredFilters.contains(new DataAttributeKey(data, filter
                  .getAttributeName())))
            {
               isClusteredFilter = true;
               break;
            }
         }
      }
      return isClusteredFilter;
   }
   
   public void setClusteredFilter(Set<DataAttributeKey> clusteredFilters)
   {
      this.clusteredFilters = clusteredFilters;
   }
}
