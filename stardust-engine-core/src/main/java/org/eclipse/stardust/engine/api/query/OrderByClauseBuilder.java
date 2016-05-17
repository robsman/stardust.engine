/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;


/**
 * @author rsauer
 * @version $Revision$
 */
public final class OrderByClauseBuilder implements OrderEvaluationVisitor
{
   private static final Logger trace = LogManager.getLogger(OrderByClauseBuilder.class);

   private final Class type;
   private final EvaluationContext evaluationContext;
   private org.eclipse.stardust.engine.core.persistence.OrderCriteria orderCriteria;

   private boolean needingProcessInstanceJoin;
   private final Map /*<Class, List>*/ joins;

   public OrderByClauseBuilder(Class type, EvaluationContext evaluationContext)
   {
      this.type = type;
      this.evaluationContext = evaluationContext;
      this.orderCriteria = null;

      this.needingProcessInstanceJoin = false;
      this.joins = new HashMap();
   }

   public void evaluateOrder(Query query)
   {
      needingProcessInstanceJoin = false;
      joins.clear();

      orderCriteria = (org.eclipse.stardust.engine.core.persistence.OrderCriteria) query.evaluateOrder(this, null);
   }

   public org.eclipse.stardust.engine.core.persistence.OrderCriteria getOrderCriteria()
   {
      return orderCriteria;
   }

   public boolean isNeedingProcessInstanceJoin()
   {
      return needingProcessInstanceJoin;
   }

   public List getJoins()
   {
      return Collections.unmodifiableList(new ArrayList(joins.values()));
   }

   public Object visit(OrderCriteria order, Object context)
   {
      org.eclipse.stardust.engine.core.persistence.OrderCriteria result = new org.eclipse.stardust.engine.core.persistence.OrderCriteria();

      if (0 < order.getCriteria().size())
      {
         for (Iterator itr = order.getCriteria().iterator(); itr.hasNext();)
         {
            OrderCriterion part = (OrderCriterion) itr.next();

            org.eclipse.stardust.engine.core.persistence.OrderCriteria innerResult = (org.eclipse.stardust.engine.core.persistence.OrderCriteria) part
                  .accept(this, null);
            if (null != innerResult)
            {
               result.add(innerResult);
            }
         }
      }

      return result;
   }

   public Object visit(AttributeOrder criterion, Object context)
   {
      final FieldRef fieldRef;
      final TypeDescriptor typeDescriptor = TypeDescriptor.get(type);
      if (criterion.getFilterableAttribute() instanceof IAttributeJoinDescriptor)
      {
         IAttributeJoinDescriptor joinDescriptor = (IAttributeJoinDescriptor) criterion
               .getFilterableAttribute();

         final Class joinRhsType = joinDescriptor.getJoinRhsType();

         Join join = (Join) joins.get(joinRhsType);
         if (null == join)
         {
            join = new Join(joinRhsType);
            for (Iterator iterator = joinDescriptor.getJoinFields().iterator(); iterator
                  .hasNext();)
            {
               final Pair joinFields = (Pair) iterator.next();
               final String lhsField = (String) joinFields.getFirst();
               final String rhsField = (String) joinFields.getSecond();
               
               join.andOn(typeDescriptor.fieldRef(lhsField), rhsField);
            }
            joins.put(joinRhsType, join);
         }

         fieldRef = join.fieldRef(joinDescriptor.getJoinAttributeName());
      }
      else
      {
         fieldRef = typeDescriptor.fieldRef(criterion.getAttributeName());
      }

      return new org.eclipse.stardust.engine.core.persistence.OrderCriteria(fieldRef, criterion.isAscending());
   }

   public Object visit(DataOrder order, Object context)
   {
      boolean useNumericColumn = false;
      boolean useStringColumn = false;
      boolean useDoubleColumn = false;

      Set dataOIDs = new HashSet();
      final org.eclipse.stardust.engine.core.persistence.OrderCriteria orderCriteria = new org.eclipse.stardust.engine.core.persistence.OrderCriteria();
      
      String dataID = order.getDataID();
      if (StringUtils.isNotEmpty(dataID))
      {
         String namespace = null;
         if (dataID.startsWith("{"))
         {
            QName qname = QName.valueOf(dataID);
            namespace = qname.getNamespaceURI();
            dataID = qname.getLocalPart();
         }

         Iterator modelItr = null;
         if (namespace != null)
         {
            modelItr = evaluationContext.getModelManager().getAllModelsForId(namespace);
         }
         else
         {
            modelItr = evaluationContext.getModelManager().getAllModels();
         }

         while (modelItr.hasNext())
         {
            IModel model = (IModel) modelItr.next();
            IData data = model.findData(dataID);
            if (null != data)
            {
               dataOIDs
                     .add(new Long(ModelManagerFactory.getCurrent().getRuntimeOid(data)));

               switch (LargeStringHolderBigDataHandler.classifyTypeForSorting(data))
               {
                  case BigData.NUMERIC_VALUE:

                     useNumericColumn = true;
                     break;

                  case BigData.STRING_VALUE:

                     useStringColumn = true;
                     break;

                  case BigData.DOUBLE_VALUE:

                     useDoubleColumn = true;
                     break;

                  default:

                     useNumericColumn = true;
                     useStringColumn = true;
                     useDoubleColumn = true;
                     break;
               }
            }
         }
      }
      
      if (!dataOIDs.isEmpty())
      {
         // todo use declared links or any other existing meta-information?
         
         List dataJoins = (List) joins.get(DataValueBean.class);
         if (null == dataJoins)
         {
            dataJoins = new ArrayList();
            joins.put(DataValueBean.class, dataJoins);
         }

         if (type.equals(ActivityInstanceBean.class) || type.equals(LogEntryBean.class))
         {
            needingProcessInstanceJoin = true;
         }
         else if ( !type.equals(ProcessInstanceBean.class))
         {
            Assert.lineNeverReached("Unsupported base type for data order: " + type);
         }

         final String alias = "DVO" + (joins.size() + 1);
         Join dvJoin = new Join(DataValueBean.class, alias)//
               .on(ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                     DataValueBean.FIELD__PROCESS_INSTANCE);
         dvJoin.where(Predicates.inList(dvJoin.fieldRef(DataValueBean.FIELD__DATA),
               dataOIDs.iterator()));
         dvJoin.setRequired(false);

         if (useNumericColumn)
         {
            orderCriteria.add(dvJoin.fieldRef(DataValueBean.FIELD__NUMBER_VALUE),
                  order.isAscending());
         }

         if (useStringColumn)
         {
            orderCriteria.add(dvJoin.fieldRef(DataValueBean.FIELD__STRING_VALUE),
                  order.isAscending());
         }

         if (useDoubleColumn)
         {
            orderCriteria.add(dvJoin.fieldRef(DataValueBean.FIELD__DOUBLE_VALUE),
                  order.isAscending());
         }

         dataJoins.add(dvJoin);
      }
      else
      {
         trace.debug("Ignoring request to order by invalid data '" + order.getDataID()
               + "'");
      }

      return orderCriteria;
   }
   
   public Object visit(CustomOrderCriterion order, Object context)
   {
      return new org.eclipse.stardust.engine.core.persistence.OrderCriteria();
   }

   public Object visit(RootProcessDefinitionDescriptor rootProcessDefinitionDescriptor, Object context)
   {
      return null;
   }
}