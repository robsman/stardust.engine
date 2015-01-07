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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.AbstractDataFilter;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.IJoinFactory;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;


/**
 * Extension SPI for searches for a specific data type
 */
public interface DataFilterExtension
{

   /**
    * Create PredicateTerm specific for this data type
    * @param dvJoin join to a data value table 
    * @param dataFilter data filter   
    * @param dataMap IData instances by their IDs 
    * @param dataFilterContext 
    * @return PredicateTerm to use for query
    */
   PredicateTerm createPredicateTerm(Join dvJoin,
         AbstractDataFilter dataFilter, Map<Long,IData> dataMap, DataFilterExtensionContext dataFilterContext);

   /**
    * Extend data order criteria according to the data type
    * @param piJoin
    * @param pisJoin
    * @param orderCriteria orderCriteria to extend
    * @param dvJoin join to a data value table 
    * @param order data order 
    * @param dataMap all data instances matching for this DataOrder hashed by their runtime ids
    * @param dataOrderJoins map to put new joins to
    */
   void extendOrderCriteria(Join piJoin, Join pisJoin, OrderCriteria orderCriteria,
         DataOrder order, Map<Long, IData> dataMap, Map<String, Join> dataOrderJoins);

   /**
    * @param andTerm
    * @param dataIdSet runtime OIDs of all data instances matching for this DataOrder 
    * @param dvJoin join to a data value table 
    */
   void appendDataIdTerm(AndTerm andTerm, Map<Long, IData> dataIdSet, Join dvJoin, AbstractDataFilter dataFilter);

   /**
    * Creates specific joins for this data type 
    * @param query query instance
    * @param dataFilter data filter the joins should be created for  
    * @param idx index to be used with joined tables
    * @param dataFilterExtensionContext context object to access all data filters
    * @param isAndTerm true if the join is for an attribute inside an AND 
    * @param joinFactory to create joins depending on the evaluation profile 
    * @return join to a data value table 
    */
   Join createDvJoin(QueryDescriptor query, AbstractDataFilter dataFilter, int idx, DataFilterExtensionContext dataFilterExtensionContext, boolean isAndTerm, IJoinFactory joinFactory);
   
   List<FieldRef> getPrefetchSelectExtension(ITableDescriptor descriptor);

}
