/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalPrimitiveValue;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.ws.query.*;
import org.eclipse.stardust.engine.core.persistence.EvaluationOption;
import org.eclipse.stardust.engine.core.persistence.Operator;



public class OperatorFilterAdapterUtils
{
   @SuppressWarnings("unused")
   private static final Logger trace = LogManager.getLogger(OperatorFilterAdapterUtils.class);

   public static FilterCriterion unmarshalUnaryOperatorFilter(
         UnaryPredicateXto unaryPredicate, Class< ? > clazz)
   {
      Operator.Unary operator = inferOperator(unaryPredicate);

      if (null != unaryPredicate.getVariable())
      {
         throw new IllegalArgumentException(
               "Error unmarshaling UnaryOperatorFilterXto: Not supported for data filters.");
      }
      else if (null != unaryPredicate.getAttribute())
      {
         // TODO unaryOperatorFilter.getScope() not needed?
         AttributeReferenceXto attrRef = unaryPredicate.getAttribute();
         if ( !isEmpty(attrRef.getEntity()))
         {
            return getReferenceAttributeFilter(clazz, operator, attrRef, null);

         }
         else
         {
            return newAttributeFilter(clazz, operator, attrRef, null);
         }
      }
      else
      {
         // TODO
         throw new IllegalArgumentException("Missing operand for unary predicate.");
      }
   }

   public static FilterCriterion unmarshalBinaryOperatorFilter(
         BinaryPredicateBaseXto binaryPredicate, Class< ? > clazz)
   {
      Operator operator = inferOperator(binaryPredicate);

      Object value = null;
      if (binaryPredicate instanceof BinaryPredicateXto)
      {
         if (null != ((BinaryPredicateXto) binaryPredicate).getRhsValue())
         {
            value = unmarshalValueLiteral(((BinaryPredicateXto) binaryPredicate).getRhsValue());
         }
      }
      else if (binaryPredicate instanceof BinaryListValuedPredicateXto)
      {
         List<Object> values = unmarshalValuesLiteral(((BinaryListValuedPredicateXto) binaryPredicate).getRhsValues());

         if (operator instanceof Operator.Ternary)
         {
            // convert list to pair
            if (2 != values.size())
            {
               throw new IllegalArgumentException("The " + operator
                     + " operator requires exactly 2 arguments.");
            }

            value = new Pair<Object, Object>(values.get(0), values.get(1));
         }
         else
         {
            value = values;
         }
      }

      if (null != binaryPredicate.getLhsVariable())
      {
         // TODO translate to some DataFilter instance
         VariableReferenceXto varReference = binaryPredicate.getLhsVariable();

         if ((binaryPredicate instanceof BinaryPredicateXto)
               && (null != ((BinaryPredicateXto) binaryPredicate).getRhsAttribute()))
         {
            // TODO comparing variables to attributes is not supported
            throw new IllegalArgumentException(
                  "Process variables can only be compared to value literals.");
         }

         if (null == value)
         {
            // TODO no value?
         }

         AbstractDataFilter dataFilter = newDataFilter(operator, varReference, value);

         handleCaseSensitiveFlag(dataFilter, binaryPredicate);

         return dataFilter;
      }
      else if (null != binaryPredicate.getLhsAttribute())
      {
         // TODO binaryOperatorFilter.getScope() not needed?
         AttributeReferenceXto attrRef = binaryPredicate.getLhsAttribute();

         if (null == value)
         {
            // TODO no value?
         }

         if ( !isEmpty(attrRef.getEntity()))
         {
            return getReferenceAttributeFilter(clazz, operator, attrRef, value);

         }
         else
         {
            return newAttributeFilter(clazz, operator, attrRef, value);
         }
      }
      else
      {
         // TODO
         throw new IllegalArgumentException("Missing LHS operand for binary predicate.");
      }
   }

   private static void handleCaseSensitiveFlag(AbstractDataFilter dataFilter,
         BinaryPredicateBaseXto binaryPredicate)
   {
      Boolean caseSensitive = unmarshalCaseSensitiveAttribute(binaryPredicate);
      if (caseSensitive != null
            && dataFilter.getFilterMode() == AbstractDataFilter.MODE_ALL_FROM_SCOPE)
      {
         @SuppressWarnings("unchecked")
         Map<EvaluationOption, Serializable> optionsMap = (Map<EvaluationOption, Serializable>) Reflect.getFieldValue(
               dataFilter, "options");

         if (optionsMap == null)
         {
            optionsMap = CollectionUtils.newMap();
         }
         optionsMap.put(EvaluationOption.CASE_SENSITIVE, caseSensitive);

         Reflect.setFieldValue(dataFilter, "options", optionsMap);
      }
   }

   private static Boolean unmarshalCaseSensitiveAttribute(
         BinaryPredicateBaseXto binaryPredicate)
   {
      Boolean isCaseSensitive = null;

      if (binaryPredicate instanceof IsEqualPredicateXto)
      {
         isCaseSensitive = ((IsEqualPredicateXto) binaryPredicate).isCaseSensitive();
      }
      else if (binaryPredicate instanceof IsLikePredicateXto)
      {
         isCaseSensitive = ((IsLikePredicateXto) binaryPredicate).isCaseSensitive();
      }

      return isCaseSensitive;
   }

   private static FilterCriterion getReferenceAttributeFilter(Class< ? > clazz,
         Operator operator, AttributeReferenceXto attrRef, Object value)
   {
      FilterableAttribute referenceAttribute = null;

      String atr = AttributeFilterUtils.unmarshalFilterableAttribute(attrRef.getValue(),
            clazz).getAttributeName();
      if ("processInstance".equals(attrRef.getEntity()))
      {
         if (WorklistQuery.class.equals(clazz))
         {
            if (WorklistQuery.PROCESS_INSTANCE_PRIORITY.getAttributeName().equals(atr))
            {
               referenceAttribute = WorklistQuery.PROCESS_INSTANCE_PRIORITY;
            }
         }
         else if (ActivityInstanceQuery.class.equals(clazz))
         {
            if (ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.getAttributeName()
                  .equals(atr))
            {
               referenceAttribute = ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY;
            }
         }
      }
      else if ("userRealm".equals(attrRef.getEntity()))
      {
         if (UserQuery.class.equals(clazz))
         {
            if (UserQuery.REALM_ID.getAttributeName().equals(atr))
            {
               referenceAttribute = UserQuery.REALM_ID;
            }
         }
      }
      if (referenceAttribute == null)
      {
         throw new UnsupportedOperationException(
               "No allowed ReferenceAttributeFilter for entity: " + attrRef.getEntity()
                     + ":" + attrRef.getValue());
      }

      FilterCriterion f = newAttributeFilter(clazz, operator, attrRef, value);

      return newReferenceAttributeFilter(referenceAttribute, f);
   }

   private static FilterCriterion newReferenceAttributeFilter(
         FilterableAttribute referenceAttribute, FilterCriterion f)
   {
      List<Class< ? >> ctorTypes = newArrayList();
      List<Object> ctorArgs = newArrayList();

      // TODO make getInterfaces code safer
      ctorTypes.add(f.getClass().getInterfaces()[0]);
      ctorArgs.add(f);

      // TODO make getInterfaces code safer
      ctorTypes.add(referenceAttribute.getClass().getSuperclass().getInterfaces()[0]);
      ctorArgs.add(referenceAttribute);

      String className = null;
      if (f instanceof UnaryOperatorFilter)
      {
         className = "org.eclipse.stardust.engine.api.query.ReferenceAttribute$ReferenceUnaryOperatorFilter";
      }
      else if (f instanceof BinaryOperatorFilter)
      {
         className = "org.eclipse.stardust.engine.api.query.ReferenceAttribute$ReferenceBinaryOperatorFilter";
      }
      else if (f instanceof TernaryOperatorFilter)
      {
         className = "org.eclipse.stardust.engine.api.query.ReferenceAttribute$ReferenceTernaryOperatorFilter";
      }
      else
      {
         throw new UnsupportedOperationException(
               "No ReferanceAttributeFilter supported for type: " + f.getClass());
      }
      FilterCriterion ret = (FilterCriterion) createInstance(className,
            ctorTypes.toArray(new Class[0]), //
            ctorArgs.toArray(new Object[0])//
      );
      return ret;
   }

   private static FilterCriterion newAttributeFilter(Class< ? > clazz, Operator operator,
         AttributeReferenceXto attrRef, Object filterValue)
   {
      // build filter, use reflection to invoke private ctor

      List<Class< ? >> ctorTypes = newArrayList();
      List<Object> ctorArgs = newArrayList();

      // attribute scope
      ctorTypes.add(Class.class);
      ctorArgs.add(clazz);

      // operator
      ctorTypes.add(operator.getClass());
      ctorArgs.add(operator);

      // attribute name
      ctorTypes.add(String.class);
      ctorArgs.add(AttributeFilterUtils.unmarshalFilterableAttribute(attrRef.getValue(),
            clazz).getAttributeName());

      // operands
      String filterClazzName;
      if (operator instanceof Operator.Unary)
      {
         filterClazzName = "org.eclipse.stardust.engine.api.query.UnaryOperatorFilterImpl";
      }
      else if (operator instanceof Operator.Binary)
      {
         filterClazzName = "org.eclipse.stardust.engine.api.query.BinaryOperatorFilterImpl";

         ctorTypes.add(Object.class);

         // unwrap time value because engine does not support Date.
         if (filterValue instanceof Date)
         {
            filterValue = new Long(((Date) filterValue).getTime());
         }
         ctorArgs.add(filterValue);
      }
      else if (operator instanceof Operator.Ternary)
      {
         filterClazzName = "org.eclipse.stardust.engine.api.query.TernaryOperatorFilterImpl";

         ctorTypes.add(Pair.class);

         // unwrap time value because engine does not support Date.
         if (filterValue instanceof Pair)
         {
            Pair filterValuePair = (Pair) filterValue;
            if (filterValuePair.getFirst() instanceof Date)
            {
               filterValue = new Pair<Object, Object>(new Long(
                     ((Date) filterValuePair.getFirst()).getTime()), new Long(
                     ((Date) filterValuePair.getSecond()).getTime()));
            }
         }

         ctorArgs.add(filterValue);
      }
      else
      {
         throw new IllegalArgumentException("Unsupported operator: " + operator);
      }

      return (FilterCriterion) createInstance(filterClazzName, //
            ctorTypes.toArray(new Class[0]), //
            ctorArgs.toArray(new Object[0]));
   }

   private static AbstractDataFilter newDataFilter(Operator operator,
         VariableReferenceXto varReference, Object filterValue)
   {
      // split data id and trailing attribute reference
      String dataId = varReference.getValue();
      String attribute = null;

      dataId = dataId.replace('.', '/');

      if ( !isEmpty(dataId) && dataId.contains("/"))
      {
         int splitIdx = dataId.indexOf("/");
         attribute = dataId.substring(splitIdx + 1);
         dataId = dataId.substring(0, splitIdx);
      }

      Class< ? extends AbstractDataFilter> dataFilterClazz;
      boolean supportsAttribute = false;
      int filterMode;
      if (VariableReferenceScopeXto.ANY_PARENT == varReference.getScope())
      {
         dataFilterClazz = SubProcessDataFilter.class;
         filterMode = AbstractDataFilter.MODE_SUBPROCESSES;

         // TODO
         if ( !isEmpty(attribute))
         {
            throw new IllegalArgumentException("Attribute references are not supported.");
         }
      }
      else if (VariableReferenceScopeXto.ANY_PARENT_OR_CHILD == varReference.getScope())
      {
         dataFilterClazz = HierarchyDataFilter.class;
         filterMode = AbstractDataFilter.MODE_ALL_FROM_HIERARCHY;

         // TODO
         if ( !isEmpty(attribute))
         {
            throw new IllegalArgumentException("Attribute references are not supported.");
         }
      }
      else
      {
         dataFilterClazz = DataFilter.class;
         filterMode = AbstractDataFilter.MODE_ALL_FROM_SCOPE;

         supportsAttribute = true;
      }

      // build filter, use reflection to invoke private ctor

      List<Class< ? >> ctorTypes = newArrayList();
      List<Object> ctorArgs = newArrayList();

      // data ID
      ctorTypes.add(String.class);
      ctorArgs.add(dataId);

      // attribute reference(optional)
      if (supportsAttribute)
      {
         ctorTypes.add(String.class);
         ctorArgs.add(attribute);
      }

      // operator
      ctorTypes.add(operator.getClass());
      ctorArgs.add(operator);

      // operands
      if (operator instanceof Operator.Binary)
      {
         ctorTypes.add(Serializable.class);
         ctorArgs.add(filterValue);
      }
      else if (operator instanceof Operator.Ternary)
      {
         Pair< ? , ? > filterValues = (Pair< ? , ? >) filterValue;

         ctorTypes.add(Serializable.class);
         ctorArgs.add(filterValues.getFirst());

         ctorTypes.add(Serializable.class);
         ctorArgs.add(filterValues.getSecond());
      }

      // filter mode
      ctorTypes.add(Integer.TYPE);
      ctorArgs.add(filterMode);

      return createInstance(dataFilterClazz, //
            ctorTypes.toArray(new Class[0]), //
            ctorArgs.toArray(new Object[0]));
   }

   private static Operator.Unary inferOperator(UnaryPredicateXto predicateXto)
   {
      Operator.Unary operator;

      if (predicateXto instanceof IsNullPredicateXto)
      {
         operator = Operator.IS_NULL;
      }
      else if (predicateXto instanceof NotNullPredicateXto)
      {
         operator = Operator.IS_NOT_NULL;
      }
      else
      {
         // TODO unsupported operator
         throw new IllegalArgumentException("Unsupported unary predicate: "
               + predicateXto);
      }

      return operator;
   }

   private static Operator inferOperator(BinaryPredicateBaseXto predicateXto)
   {
      Operator operator;

      if (predicateXto instanceof IsEqualPredicateXto)
      {
         operator = Operator.IS_EQUAL;
      }
      else if (predicateXto instanceof NotEqualPredicateXto)
      {
         operator = Operator.NOT_EQUAL;
      }
      else if (predicateXto instanceof LessThanPredicateXto)
      {
         operator = Operator.LESS_THAN;
      }
      else if (predicateXto instanceof LessOrEqualPredicateXto)
      {
         operator = Operator.LESS_OR_EQUAL;
      }
      else if (predicateXto instanceof GreaterOrEqualPredicateXto)
      {
         operator = Operator.GREATER_OR_EQUAL;
      }
      else if (predicateXto instanceof GreaterThanPredicateXto)
      {
         operator = Operator.GREATER_THAN;
      }
      else if (predicateXto instanceof IsLikePredicateXto)
      {
         operator = Operator.LIKE;
      }
      else if (predicateXto instanceof InListPredicateXto)
      {
         operator = Operator.IN;
      }
      else if (predicateXto instanceof NotInListPredicateXto)
      {
         operator = Operator.NOT_IN;
      }
      else if (predicateXto instanceof NotAnyOfPredicateXto)
      {
         operator = Operator.NOT_ANY_OF;
      }
      else if (predicateXto instanceof BetweenPredicateXto)
      {
         operator = Operator.BETWEEN;
      }
      else
      {
         // TODO unsupported operator
         throw new IllegalArgumentException("Unsupported predicate: " + predicateXto);
      }

      return operator;
   }

   private static Object unmarshalValueLiteral(ValueLiteralXto valueXto)
   {
      return (null != valueXto) //
            ? unmarshalValueLiteral(valueXto.getType(), valueXto.getValue())
            : null;
   }

   private static Object unmarshalValueLiteral(QName type, String literal)
   {
      Object value = null;

      if ( !isEmpty(literal))
      {
         value = unmarshalPrimitiveValue(type, literal);
      }

      // if (value instanceof Date)
      // {
      // value = Long.valueOf(((Date) value).getTime());
      // }

      return value;
   }

   private static List<Object> unmarshalValuesLiteral(ValuesLiteralXto valuesXto)
   {
      List<Object> values = newArrayList();

      if (null != valuesXto)
      {
         for (String literal : valuesXto.getValue())
         {
            values.add(unmarshalValueLiteral(valuesXto.getType(), literal));
         }
      }

      return values;
   }

   public static Object createInstance(String className, Class< ? >[] argTypes,
         Object[] args)
   {
      Class< ? > clazz = Reflect.getClassFromClassName(className);

      return createInstance(clazz, argTypes, args);
   }

   public static <T> T createInstance(Class<T> clazz, Class< ? >[] argTypes, Object[] args)
   {
      try
      {
         if (null == argTypes)
         {
            return clazz.newInstance();
         }
         else
         {
            Constructor<T> ctor = clazz.getDeclaredConstructor(argTypes);
            if ( !ctor.isAccessible())
            {
               ctor.setAccessible(true);
            }

            return ctor.newInstance(args);
         }
      }
      catch (Exception e)
      {
         throw new InternalException("Cannot instantiate class '" + clazz.getName()
               + "'.", e);
      }
   }

}
