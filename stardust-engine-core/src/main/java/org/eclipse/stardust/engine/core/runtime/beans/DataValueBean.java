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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.model.beans.DataBean;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.setup.DataCluster;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterHelper;
import org.eclipse.stardust.engine.core.runtime.setup.DataSlot;


/**
 * Describes workflow data values being created and modified during process execution.
 */
public class DataValueBean extends IdentifiablePersistentBean
      implements IDataValue, BigData, IProcessInstanceAware
{
   static final long serialVersionUID = 4318266384828760674L;

   private static final Logger trace = LogManager.getLogger(DataValueBean.class);

   /**
    * Providing this instance will result in default initialization.
    */
   public static final Object USE_DEFAULT_INITIAL_VALUE = new Object();

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__DATA = "data";
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__TYPE_KEY = "type_key";
   public static final String FIELD__STRING_VALUE = "string_value";
   public static final String FIELD__NUMBER_VALUE = "number_value";
   public static final String FIELD__DOUBLE_VALUE = "double_value";

   public static final FieldRef FR__OID = new FieldRef(DataValueBean.class, FIELD__OID);
   public static final FieldRef FR__MODEL = new FieldRef(DataValueBean.class, FIELD__MODEL);
   public static final FieldRef FR__DATA = new FieldRef(DataValueBean.class, FIELD__DATA);
   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(DataValueBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__TYPE_KEY = new FieldRef(DataValueBean.class, FIELD__TYPE_KEY);
   public static final FieldRef FR__STRING_VALUE = new FieldRef(DataValueBean.class, FIELD__STRING_VALUE);
   public static final FieldRef FR__NUMBER_VALUE = new FieldRef(DataValueBean.class, FIELD__NUMBER_VALUE);
   public static final FieldRef FR__DOUBLE_VALUE = new FieldRef(DataValueBean.class, FIELD__DOUBLE_VALUE);

   public static final String TABLE_NAME = "data_value";
   public static final String DEFAULT_ALIAS = "dv";
   public static final String LOCK_TABLE_NAME = "data_value_lck";
   public static final String LOCK_INDEX_NAME = "data_value_lck_idx";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "data_value_seq";
   public static final boolean TRY_DEFERRED_INSERT = true;
   public static final String[] data_values_index1_UNIQUE_INDEX = new String[] {FIELD__OID};
   public static final String[] data_values_index2_INDEX = new String[] {FIELD__PROCESS_INSTANCE};
   public static final String[] data_values_index3_INDEX = new String[] {FIELD__TYPE_KEY};
   public static final String[] data_values_index4_INDEX = new String[] {FIELD__NUMBER_VALUE};
   public static final String[] data_values_index5_INDEX = new String[] {FIELD__STRING_VALUE};

   public static final String[] data_values_index6_UNIQUE_INDEX = new String[] {
         FIELD__DATA, FIELD__PROCESS_INSTANCE};

   // @todo in the end, COLUMN_LENGTH should be the metadata and not COLUMN_TYPE
   private static final int string_value_COLUMN_LENGTH = 128;

   @ForeignKey (modelElement=ModelBean.class)
   public long model;
   @ForeignKey (modelElement=DataBean.class)
   public long data;

   public static final String processInstance_REGISTRAR = "addDataValue";
   public ProcessInstanceBean processInstance;
   public String string_value;
   public long number_value;
   public double double_value;

   public int type_key = BigData.NULL;

   private transient BigDataHandler dataHandler;

   /**
    * Evaluates, depending on the data type, if the value can be represented inline in the
    * <code>data_value</code> table) or if the value's representations has to be sliced
    * for storage.
    *
    * @param value
    *           The generic representation of the data value to match with.
    * @return <code>true</code> if the value can be stored inline, <code>false</code>
    *         if the value is large and has to be sliced.
    * @see #matchDataInstancesPredicate
    */
   public static boolean isLargeValue(Object value)
   {
      return LargeStringHolderBigDataHandler.canonicalizeDataValue(
            string_value_COLUMN_LENGTH, value).isLarge();
   }

   /**
    * Returns the maximum string length a (@link DataValueBean} can hold when its type
    * equals {@link BigData#STRING_VALUE}
    *
    * @return The fitting and may be truncated string
    *
    * @see #getShortStringColumnLength()
    */
   public static int getStringValueMaxLength()
   {
      return string_value_COLUMN_LENGTH;
   }

   public static List<IDataValue> findAllForProcessInstance(long processInstanceOID, IModel model, Set<IData> data)
   {
      List<IDataValue> result = CollectionUtils.newList(data.size());
      ProcessInstanceBean pi = null;
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (session.existsInCache(ProcessInstanceBean.class, new Long(processInstanceOID)))
      {
         pi = (ProcessInstanceBean) session.findByOID(ProcessInstanceBean.class, processInstanceOID);
      }

      ModelManager manager = ModelManagerFactory.getCurrent();
      List<Long> oids = CollectionUtils.newList();
      List<IData> uncachedData = CollectionUtils.newList();
      for (IData dataObject : data)
      {
         if (pi != null)
         {
            IDataValue dv = pi.getCachedDataValue(dataObject.getId());
            if (dv != null)
            {
               result.add(dv);
               continue;
            }
         }
         oids.add(manager.getRuntimeOid(dataObject));
         uncachedData.add(dataObject);
      }

      if (!oids.isEmpty())
      {
         PredicateTerm modelPredicate = Predicates.isEqual(FR__MODEL, model.getModelOID());
         PredicateTerm dataPredicate = Predicates.inList(FR__DATA, oids);
         PredicateTerm processInstancePredicate = Predicates.isEqual(ProcessInstanceBean.FR__OID, processInstanceOID);
         QueryExtension queryExtension = QueryExtension
               .where(Predicates.andTerm(modelPredicate, dataPredicate))
               .addJoin(new Join(ProcessInstanceBean.class)
                  .on(DataValueBean.FR__PROCESS_INSTANCE, ProcessInstanceBean.FIELD__SCOPE_PROCESS_INSTANCE)
                  .where(processInstancePredicate));

         ResultIterator iterator = session.getIterator(DataValueBean.class, queryExtension);
         while (iterator.hasNext())
         {
            DataValueBean dv = (DataValueBean) iterator.next();
            if (pi != null)
            {
               pi.addDataValue(dv);
            }
            result.add(dv);
            uncachedData.remove(dv.getData());
         }
         if (!uncachedData.isEmpty())
         {
            if (pi == null)
            {
               // oops, cache miss ! and data maybe not yet created.
               pi = ProcessInstanceBean.findByOID(processInstanceOID);
            }
            for (int i = 0; i < uncachedData.size(); i++)
            {
               DataValueBean dv = (DataValueBean) pi.getDataValue(uncachedData.get(i));
               if (dv != null)
               {
                  result.add(dv);
               }
            }
         }
      }
      return result;
   }

   /**
    * Builds a predicate fragment for matching data instances having the given value.
    * Depending on the data type this predicate may result in an exact match (if the value
    * can be represented inline in the <code>data_value</code> table) or just match a
    * set of candidate instances (if the value's representations has to be sliced for
    * storage).
    * @param value
    *           The generic representation of the data value to match with.
    * @param evaluationOptions
    *          Additional options relevant for predicate evaluation
    * @return A predicate term for matching data instances possibly having the given
    *         value.
    * @see #isLargeValue
    */
   public static PredicateTerm matchDataInstancesPredicate(ITableDescriptor dvTable,
         Operator operator, Object value, final IEvaluationOptionProvider evaluationOptions)
   {
      final LargeStringHolderBigDataHandler.Representation canonicalValue = LargeStringHolderBigDataHandler.canonicalizeDataValue(
            string_value_COLUMN_LENGTH, value);

      String columnName;
      Object matchValue = canonicalValue.getRepresentation();

      switch (canonicalValue.getClassificationKey())
      {
      case BigData.NULL_VALUE:
         columnName = null;
         break;

      case BigData.NUMERIC_VALUE:
         columnName = FIELD__NUMBER_VALUE;
         break;

      case BigData.DOUBLE_VALUE:
         columnName = FIELD__DOUBLE_VALUE;
         break;

      case BigData.STRING_VALUE:
         columnName = FIELD__STRING_VALUE;
         break;

      default:
         throw new InternalException("Unsupported BigData type classification: "
               + canonicalValue.getClassificationKey());
      }

      final AndTerm resultTerm = new AndTerm();

      if (operator instanceof Operator.Unary)
      {
         resultTerm.add(new ComparisonTerm(dvTable.fieldRef(FIELD__TYPE_KEY),
               (Operator.Unary) operator));
      }
      else
      {
         if (BigData.NULL_VALUE == canonicalValue.getClassificationKey())
         {
            if (Operator.IS_EQUAL.equals(operator) || Operator.NOT_EQUAL.equals(operator))
            {
               OrTerm orTerm = new OrTerm();
               if (Operator.IS_EQUAL.equals(operator))
               {
                  orTerm.add(new ComparisonTerm(dvTable.fieldRef(FIELD__TYPE_KEY),
                        Operator.IS_NULL));
               }
               orTerm.add(new ComparisonTerm(dvTable.fieldRef(FIELD__TYPE_KEY),
                     (Operator.Binary) operator, new Integer(BigData.NULL)));
               resultTerm.add(orTerm);
            }
            else
            {
               throw new PublicException(
                     BpmRuntimeError.MDL_NULL_VALUES_ARE_NOT_SUPPORTED_WITH_OPERATOR
                           .raise(operator));
            }
         }
         else
         {
            Assert.isNotNull(columnName);

            if (Operator.LIKE.equals(operator)
                  && (BigData.STRING == canonicalValue.getTypeKey()))
            {
               resultTerm.add(Predicates.inList(dvTable.fieldRef(FIELD__TYPE_KEY),
                     new int[] {BigData.STRING, BigData.BIG_STRING}));
            }
            else
            {
               resultTerm.add(Predicates.isEqual(dvTable.fieldRef(FIELD__TYPE_KEY),
                     canonicalValue.getTypeKey()));
            }

            FieldRef lhsOperand = dvTable.fieldRef(columnName);

            if ( !EvaluationOptions.isCaseSensitive(evaluationOptions))
            {
               // ignore case by applying LOWER(..) SQL function
               lhsOperand = Functions.strLower(lhsOperand);
            }

            if (operator.isBinary())
            {
               if (matchValue instanceof Collection)
               {
                  List<List< ? >> subLists = CollectionUtils.split(
                        (Collection) matchValue, 1000);

                  MultiPartPredicateTerm mpTerm = new OrTerm();
                  for (List< ? > subList : subLists)
                  {
                     Iterator valuesIter = new TransformingIterator(subList.iterator(),
                           new Functor()
                     {
                        public Object execute(Object source)
                        {
                           return getInlineComparisonValue(source,
                                 evaluationOptions);
                        }
                     });

                     if (operator.equals(Operator.NOT_ANY_OF))
                     {
                        Assert.lineNeverReached("TODO: Still to be implemented");
                     }

                     if(operator.equals(Operator.NOT_IN))
                     {
                        mpTerm.add(Predicates.notInList(lhsOperand, valuesIter));
                     }
                     else
                     {
                        mpTerm.add(Predicates.inList(lhsOperand, valuesIter));
                     }
                  }

                  resultTerm.add(mpTerm);
               }
               else
               {
                  resultTerm.add(new ComparisonTerm(lhsOperand,
                        (Operator.Binary) operator, getInlineComparisonValue(matchValue,
                              evaluationOptions)));
               }
            }
            else if (operator.isTernary())
            {
               if ( !(matchValue instanceof Pair))
               {
                  throw new PublicException(
                        BpmRuntimeError.MDL_INCONSISTENT_OPERATOR_USE.raise(operator,
                              matchValue));
               }

               Pair pair = (Pair) matchValue;
               resultTerm.add(new ComparisonTerm(lhsOperand,
                     (Operator.Ternary) operator, new Pair(
                           getInlineComparisonValue(pair.getFirst(), evaluationOptions),
                           getInlineComparisonValue(pair.getSecond(), evaluationOptions))));
            }
         }
      }

      return resultTerm;
   }

   /**
    * Copy the given source data value to the data value of the same data of the target
    * process instance.
    *
    * @param targetProcessInstance  Process instance which receives the data value
    * @param srcValue Source data value
    * @throws PublicException
    */
   public static void copyDataValue(IProcessInstance targetProcessInstance,
         final IDataValue srcValue) throws PublicException
   {
      final DefaultInitialDataValueProvider dvProvider = new DefaultInitialDataValueProvider(
            srcValue.getSerializedValue());
      IDataValue targetValue = targetProcessInstance.getDataValue(srcValue.getData(),
            dvProvider);

      if (targetValue == null)
      {
         throw new PublicException(
               BpmRuntimeError.MDL_NO_WORKFLOW_DATA_DEFINED_WITH_ID_IN_THIS_MODEL_VERSION
                     .raise(srcValue.getData().getId()));
      }

      if ( !dvProvider.isUsedForInitialization())
      {
         targetValue.setValue(dvProvider.getEvaluatedValue().getValue(), false);
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Copied data value '" + targetValue.getData().getName() + "'.");
      }
   }

   /**
    *
    */
   public DataValueBean()
   {
      dataHandler = new LargeStringHolderBigDataHandler(this);
   }

   /**
    * Creates an instance of the workflow data. This object manages data created
    * or retrieved during workflow processing.
    * <p/>
    * If the type of the data object is a literal, a literal
    * PersistenceController is created.
    * <p/>
    * If the type is an entity bean reference, a serializable
    * PersistenceController is created
    * to hold the primary key of the entity bean.
    */
   public DataValueBean(IProcessInstance processInstance, IData data,
         AbstractInitialDataValueProvider dataValueProvider)
   {
      this.processInstance = (ProcessInstanceBean) processInstance
            .getScopeProcessInstance();
      this.model = data.getModel().getModelOID();
      this.data = ModelManagerFactory.getCurrent().getRuntimeOid(data);

      if (this.data == 0)
      {
         throw new InternalException(
               MessageFormat
                     .format(
                           "DataValueBean for process instance {0} and data {1} cannot be created as the data reference cannot be resolved.",
                           new Object[] { processInstance.getOID(),
                                 data.getId() }));
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Data value created for '" + data + "' and '" + processInstance
               + "'.");
      }

      Object initialValue;
      if (null == dataValueProvider)
      {
         initialValue = DataValueUtils.createNewValueInstance(data, processInstance);
      }
      else
      {
         initialValue = dataValueProvider.getEvaluatedValue().getValue();
         if (USE_DEFAULT_INITIAL_VALUE == initialValue)
         {
            initialValue = DataValueUtils.createNewValueInstance(data, processInstance);
         }
         else
         {
            dataValueProvider.setUsedForInitialization();
         }
      }

      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      if (session.isReadOnly())
      {
         // for read-only audit trails, create a handler
         // that does not modify the database
         dataHandler = new TransientBigDataHandler();
      }
      else if ((processInstance instanceof Persistent) && ((Persistent) processInstance).getPersistenceController().isCreated())
      {
         // defer large string serialization as much as possible
         dataHandler = new LazilyPersistingBigDataHandler();
      }
      else
      {
         dataHandler = new LargeStringHolderBigDataHandler(this);
      }

      // explicitly asking for an atomic data value, as we need to know for the actual
      // value of the data, considered a blob at worst (canonicalizeDataValue treats pairs
      // an collections special as they might be used for BETWEEN or IN predicates)
      if (LargeStringHolderBigDataHandler.canonicalizeAtomicDataValue(
            string_value_COLUMN_LENGTH, initialValue).isLarge())
      {
         if ( !session.isReadOnly())
         {
            session.cluster(this);
         }
         setValue(initialValue, true);
         // Update of data cluster will be done in {@link Session#flush()}
      }
      else
      {
         setValue(initialValue, false);
         if ( !session.isReadOnly())
         {
            session.cluster(this);

            // Update of data cluster has to be done here because it is possible that
            // no update on this data value is necessary.
            if (session instanceof org.eclipse.stardust.engine.core.persistence.jdbc.Session)
            {
               org.eclipse.stardust.engine.core.persistence.jdbc.Session jdbcSession = (org.eclipse.stardust.engine.core.persistence.jdbc.Session) session;
               if (jdbcSession.isUsingDataClusters())
               {
                  if (!jdbcSession.getDBDescriptor().supportsSequences())
                  {
                     Map<Pair<Long, DataCluster>, List<Pair<PersistenceController, DataSlot>>> piToDv = CollectionUtils
                           .newHashMap();
                     DataClusterHelper.prepareDataValueUpdate(getPersistenceController(),
                           piToDv, null, true);
                     try
                     {
                        if (!piToDv.isEmpty())
                        {
                           DataClusterHelper.completeDataValueUpdate(piToDv, jdbcSession);
                        }
                     }
                     catch (InternalException e)
                     {
                        throw new InternalException(MessageFormat.format(
                              "Update of cluster tables for {0} and {1} failed.",
                              new Object[] {data, processInstance}), e);
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Returns the data, this data value is instantiated from.
    */
   public IData getData()
   {
      fetch();
      return ModelManagerFactory.getCurrent().findData(model, data);
   }

   public void triggerSerialization()
   {
      if (dataHandler instanceof LazilyPersistingBigDataHandler)
      {
         Object transientValue = dataHandler.read();
         this.dataHandler = new LargeStringHolderBigDataHandler(this);
         dataHandler.write(transientValue, true);
      }
   }

   /**
    * Retrieves the value of the data value.
    *
    * @return If the type of the data value's data is a literal, the java wrapper object (<code>Integer</code>,
    *         <code>Long</code> etc.) is returned. If the type is an (entity bean)
    *         reference, the entity bean is returned.
    */
   public Object getValue()
   {
      return fromPersistedValue(getData().getId(), dataHandler.read());
   }

   // @todo (france, ub): how the forceRefresh semantics precisely works
   /**
    * Sets the PersistenceController of this data value either to the literal provided as
    * a wrapping object in <code>value</code> or the primary key of the entity bean
    * referenced by <code>value</code>.
    */
   public void setValue(Object value, boolean forceRefresh)
   {
      lock();
      dataHandler.write(toPersistedValue(getData().getId(), value), forceRefresh);
   }

   public double getDoubleValue()
   {
      fetch();
      return double_value;
   }

   @Override
   public void setDoubleValue(double value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.double_value, value))
      {
         markModified(FIELD__DOUBLE_VALUE);
         double_value = value;
      }
   }

   // @todo (france, ub): investigate usage of this method in the context of plethora
   /**
    * Retrieves the serialized value of the data value.
    *
    * @return If the type of the data value's data is a literal, the java wrapper object (<code>Integer</code>,
    *         <code>Long</code> etc.) is returned. If the type is an (entity bean)
    *         reference, the pk of the entity bean is returned.
    */
   public Serializable getSerializedValue()
   {
      return (Serializable) getValue();
   }

   // BigData interface implementation

   public String getShortStringValue()
   {
      fetch();
      return string_value == null && type_key == BigData.STRING ? "" : string_value;
   }

   public void setShortStringValue(String value)
   {
      fetch();
      if ( !CompareHelper.areEqual(this.string_value, value))
      {
         markModified(FIELD__STRING_VALUE);
         string_value = value;
      }
   }

   public long getLongValue()
   {
      fetch();
      return number_value;
   }

   public void setLongValue(long value)
   {
      fetch();
      if (this.number_value != value)
      {
         markModified(FIELD__NUMBER_VALUE);
         number_value = value;
      }
   }

   public int getType()
   {
      fetch();
      return type_key;
   }

   public void setType(int type)
   {
      fetch();
      if (this.type_key != type)
      {
         markModified(FIELD__TYPE_KEY);
         type_key = type;
      }
   }

   public int getShortStringColumnLength()
   {
      return string_value_COLUMN_LENGTH;
   }

   public IProcessInstance getProcessInstance()
   {
      fetch();

      return processInstance;
   }

   public void refresh()
   {
      dataHandler.refresh();
   }

   public static Object fromPersistedValue(String dataId, Object value)
   {
      if (PredefinedConstants.BUSINESS_DATE.equals(dataId))
      {
         if (value instanceof Long)
         {
            value = DateUtils.timestampToBusinessDate((Long) value);
         }
      }
      return value;
   }

   public static Object toPersistedValue(String dataId, Object value)
   {
      if (dataId != null && dataId.length() > 0
            && (PredefinedConstants.BUSINESS_DATE.equals(dataId)
                  || dataId.charAt(0) == '{' && PredefinedConstants.BUSINESS_DATE.equals(QName.valueOf(dataId).getLocalPart())))
      {
         if (value instanceof Calendar)
         {
            value = DateUtils.businessDateToTimestamp((Calendar) value);
         }
         else if (value instanceof Pair<?, ?>)
         {
            Object first = ((Pair<?, ?>) value).getFirst();
            Object second = ((Pair<?, ?>) value).getSecond();
            if (first instanceof Calendar || second instanceof Calendar)
            {
               value = new Pair(
                     first instanceof Calendar ? DateUtils.businessDateToTimestamp((Calendar) first) : first,
                     second instanceof Calendar ? DateUtils.businessDateToTimestamp((Calendar) second) : second);
            }
         }
      }
      return value;
   }

   private static final Object getInlineComparisonValue(Object value,
         IEvaluationOptionProvider options)
   {
      Object result;

      if (value instanceof String
            && ((String) value).length() > string_value_COLUMN_LENGTH)
      {
         // strip for maximum inline slice size

         result = ((String) value).substring(0, string_value_COLUMN_LENGTH);
      }
      else
      {
         result = value;
      }

      if ( !EvaluationOptions.isCaseSensitive(options) && (result instanceof String))
      {
         result = ((String) result).toLowerCase();
      }

      return result;
   }
}
