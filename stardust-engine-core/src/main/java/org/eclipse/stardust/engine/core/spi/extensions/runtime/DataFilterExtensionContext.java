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
import org.eclipse.stardust.engine.api.query.AbstractDataFilter;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.core.persistence.Join;


/**
 * Context object to access all data filters relating to the same query 
 */
public class DataFilterExtensionContext
{
   private Object content;

   private Map<String,List<AbstractDataFilter>> dataFiltersByDataId;

   private final List<Join> joins = new LinkedList<Join>();

   private boolean useDistinct = false;

   public DataFilterExtensionContext(List<AbstractDataFilter> dataFilters)
   {
      this.dataFiltersByDataId = createFiltersByDataIdMap(dataFilters);
   }

   /**
    * Creates DataFilterExtensionContext by searching all AbstractDataFilters in the filterAndTerm
    * @param filterAndTerm
    */
   public DataFilterExtensionContext(FilterAndTerm filterAndTerm)
   {
      List<AbstractDataFilter> dataFilters = new LinkedList<AbstractDataFilter>();
      findDataFilters(filterAndTerm, dataFilters);
      this.dataFiltersByDataId = createFiltersByDataIdMap(dataFilters);
   }

   private void findDataFilters(FilterTerm filterTerm, List<AbstractDataFilter> result)
   {
      for (Iterator itr = filterTerm.getParts().iterator(); itr.hasNext();)
      {
         FilterCriterion criterion = (FilterCriterion) itr.next();
         if (criterion instanceof AbstractDataFilter)
         {
            result.add((AbstractDataFilter)criterion);
         }
         if (criterion instanceof FilterTerm)
         {
            FilterTerm term = (FilterTerm) criterion;
            findDataFilters(term, result);
         }
      }
   }

   private Map<String,List<AbstractDataFilter>> createFiltersByDataIdMap(
         List<AbstractDataFilter> dataFilters)
   {
      Map<String,List<AbstractDataFilter>> filtersByDataId = CollectionUtils.newHashMap();

      for (Iterator<AbstractDataFilter> i = dataFilters.iterator(); i.hasNext();)
      {
         AbstractDataFilter filter = (AbstractDataFilter) i.next();

         String dataId = filter.getDataID();
         if (filtersByDataId.containsKey(dataId) == false)
         {
            filtersByDataId.put(dataId, new LinkedList<AbstractDataFilter>());
         }
         List<AbstractDataFilter> l = (List<AbstractDataFilter>) filtersByDataId.get(dataId);
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

   public Object getContent()
   {
      return this.content;
   }

   public Map<String,List<AbstractDataFilter>> getDataFiltersByDataId()
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

}
