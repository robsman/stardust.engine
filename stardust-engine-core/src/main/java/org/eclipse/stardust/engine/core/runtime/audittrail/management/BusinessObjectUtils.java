/*******************************************************************************
 * Copyright (c) 2014, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.audittrail.management;

import java.io.Reader;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectExistsException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.BusinessObjectDetails;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQueryEvaluator.ParsedQueryProcessor;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Definition;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean.DataValueChangeListener;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2Predicate;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.DocumentBuilder;

/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class BusinessObjectUtils
{
   private static final String BUSINESS_OBJECT_ATT = PredefinedConstants.MODEL_SCOPE + "BusinessObject";
   private static final String BUSINESS_OBJECT_RELATIONSHIPS_ATT = BUSINESS_OBJECT_ATT + ":Relationships";

   public static final Predicate dataFilterPredicate = new Predicate()
   {
      @Override
      public boolean accept(Object o)
      {
         return o instanceof DataFilter;
      }
   };

   private static final Predicate sdvPredicate = new Predicate()
   {
      @Override
      public boolean accept(Object o)
      {
         return StructuredDataValueBean.class.equals(o);
      }
   };

   public static BusinessObjects getBusinessObjects(BusinessObjectQuery query)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();

      BusinessObjectQuery.Policy policy = (BusinessObjectQuery.Policy) query.getPolicy(
            BusinessObjectQuery.Policy.class);

      final boolean withDescription = policy == null ? false : policy.hasOption(BusinessObjectQuery.Option.WITH_DESCRIPTION);
      final boolean withValues = policy == null ? false : policy.hasOption(BusinessObjectQuery.Option.WITH_VALUES);

      BusinessObjectQueryPredicate queryEvaluator = new BusinessObjectQueryPredicate(query);
      Set<IData> allData = collectData(modelManager, queryEvaluator);
      if (allData.isEmpty())
      {
         return new BusinessObjects(query, Collections.<BusinessObject>emptyList());
      }

      if (withValues && !isUniqueBusinessObject(allData))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_ARGUMENT.raise("query","Business Object not uniquely specified."));
      }

      final Map<IData, List<BusinessObject.Value>> values = withValues
            ? fetchValues(allData, queryEvaluator.getPkValue(), query.getFilter(), query) : null;
      if (values != null)
      {
         allData = values.keySet();
      }

      Functor<IData, BusinessObject> transformer = new Functor<IData, BusinessObject>()
      {
         public BusinessObject execute(IData source)
         {
            List<Definition> items = null;
            if (withDescription)
            {
               BusinessObject bo = getBusinessObject(source);
               if (!withValues)
               {
                  return bo;
               }
               items = bo.getItems();
            }
            return new BusinessObjectDetails(source.getModel().getModelOID(), source.getModel().getId(),
                  source.getId(), source.getName(), items,
                  values == null ? null : values.get(source));
         }
      };

      return new BusinessObjects(query, CollectionUtils.newListFromIterator(
            new TransformingIterator<IData, BusinessObject>(allData.iterator(), transformer)));
   }

   public static boolean isUniqueBusinessObject(Set<IData> allData)
   {
      String modelId = null;
      String dataId = null;
      for (IData data : allData)
      {
         if (modelId == null)
         {
            modelId = data.getModel().getId();
         }
         if (dataId == null)
         {
            dataId = data.getId();
         }
         else if (!dataId.equals(data.getId()) || !modelId.equals(data.getModel().getId()))
         {
            return false;
         }
      }
      return true;
   }

   public static Set<IData> collectData(final ModelManager modelManager, BusinessObjectQueryPredicate queryEvaluator)
   {
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      Authorization2Predicate auth = runtimeEnvironment == null ? null : runtimeEnvironment.getAuthorizationPredicate();

      Set<IData> allData = CollectionUtils.newSet();

      Long modelOID = queryEvaluator.getModelOid();
      if (modelOID == null)
      {
         modelOID = (long) PredefinedConstants.ALL_MODELS;
      }
      if (modelOID >= 0 || modelOID <= -100) // (fh) see special model oids in PredefinedConstants, i.e. ACTIVE_MODEL
      {
         IModel model = modelManager.findModel(modelOID);
         if (model != null)
         {
            addModelData(allData, model, queryEvaluator, auth);
         }
      }
      else
      {
         Iterator<IModel> allModels;
         switch ((int)(long)modelOID)
         {
         case PredefinedConstants.ACTIVE_MODEL:
            allModels = modelManager.findActiveModels().iterator();
            break;
         case PredefinedConstants.LAST_DEPLOYED_MODEL:
            allModels = modelManager.findLastDeployedModels().iterator();
            break;
         case PredefinedConstants.ALIVE_MODELS:
            allModels = modelManager.getAllAliveModels();
         default:
            allModels = modelManager.getAllModels();
         }
         for (Iterator<IModel> models = allModels; models.hasNext();)
         {
            addModelData(allData, models.next(), queryEvaluator, auth);
            if (modelOID == PredefinedConstants.ANY_MODEL && !allData.isEmpty())
            {
               break;
            }
         }
      }
      return allData;
   }

   private static void addModelData(Set<IData> allData, IModel model, BusinessObjectQueryPredicate queryEvaluator, Authorization2Predicate auth)
   {
      if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
      {
         for (Iterator<IData> data = model.getData().iterator(); data.hasNext();)
         {
            IData item = data.next();
            if (queryEvaluator.accept(item) && (auth == null || auth.accept(item)))
            {
               allData.add(item);
            }
         }
      }
   }

   private static Map<IData, List<Value>> fetchValues(Set<IData> allData, Object pkValue, FilterAndTerm term, Query query)
   {
      Map<IData, List<BusinessObject.Value>> values = CollectionUtils.newMap();
      for (IData data : allData)
      {
         ProcessInstanceQuery pi = ProcessInstanceQuery.findAll();
         if (pkValue != null)
         {
            String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
            pi.where(DataFilter.isEqual(data.getId(), pk, (Serializable) pkValue));
         }
         copyFilters(term, pi.getFilter(), dataFilterPredicate);
         ProcessInstanceQueryEvaluator eval = new ProcessInstanceQueryEvaluator(pi, QueryServiceUtils.getDefaultEvaluationContext());
         ParsedQuery parsedQuery = eval.parseQuery();
         List<Join> predicateJoins = parsedQuery.getPredicateJoins();
         PredicateTerm parsedTerm = filter(parsedQuery.getPredicateTerm());

         QueryDescriptor queryDescriptor = createFetchQuery(data, pkValue, predicateJoins, parsedTerm);
         fetchValues(values, data, queryDescriptor, query);
      }
      return values;
   }

   protected static QueryDescriptor createFetchQuery(IData data, Object pkValue,
         List<Join> predicateJoins, PredicateTerm parsedTerm)
   {
      long modelOID = data.getModel().getModelOID();
      long dataRtOID = ModelManagerFactory.getCurrent().getRuntimeOid(data);
      String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);

      QueryDescriptor desc = QueryDescriptor
            .from(ProcessInstanceBean.class)
            .select(ProcessInstanceBean.FR__OID, ClobDataBean.FR__STRING_VALUE);
      Join dvJoin = desc
            .innerJoin(DataValueBean.class)
            .on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);
      desc
            .innerJoin(ClobDataBean.class)
            .on(dvJoin.fieldRef(DataValueBean.FIELD__NUMBER_VALUE), ClobDataBean.FIELD__OID);
      applyRestrictions(desc, predicateJoins, sdvPredicate);

      List<PredicateTerm> predicates = CollectionUtils.newList();
      predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1));
      predicates.add(Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOID));
      predicates.add(Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), dataRtOID));

      if (pkValue == null && parsedTerm == null)
      {
         Join sdvJoin = desc
               .innerJoin(StructuredDataValueBean.class)
               .on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
         Join sdJoin = desc
               .innerJoin(StructuredDataBean.class)
               .on(sdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
               .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
               .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);
         predicates.add(Predicates.isEqual(sdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), pk));
      }

      if (parsedTerm != null)
      {
         predicates.add(parsedTerm);
      }

      desc.where(new AndTerm(predicates.toArray(new PredicateTerm[predicates.size()])));
      return desc;
   }

   protected static int fetchValues(Map<IData, List<BusinessObject.Value>> values,
         IData data, QueryDescriptor queryDescriptor, Query query)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet resultSet = session.executeQuery(queryDescriptor, QueryUtils.getTimeOut(query));
      SubsetPolicy subsetPolicy = QueryUtils.getSubset(query);
      try
      {
         int skipped = 0;
         int count = 0;
         int total = 0;
         while (resultSet.next())
         {
            total++;
            if (skipped < subsetPolicy.getSkippedEntries())
            {
               skipped++;
            }
            else
            {
               if (count < subsetPolicy.getMaxSize())
               {
                  count++;
                  long piOid = resultSet.getLong(1);
                  Reader clobCharacterStream = resultSet.getCharacterStream(2);

                  Document document = DocumentBuilder.buildDocument(clobCharacterStream);
                  boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);
                  final IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
                  StructuredDataConverter converter = new StructuredDataConverter(xPathMap);

                  List<BusinessObject.Value> list = values.get(data);
                  if (list == null)
                  {
                     list = CollectionUtils.newList();
                     values.put(data, list);
                  }
                  Object value = converter.toCollection(document.getRootElement(), "", namespaceAware);
                  list.add(new BusinessObjectDetails.ValueDetails(piOid, value));
               }
               else
               {
                  if (!subsetPolicy.isEvaluatingTotalCount())
                  {
                     break;
                  }
               }
            }
         }
         return subsetPolicy.isEvaluatingTotalCount() ? total : 0;
      }
      catch (Exception e)
      {
         throw new PublicException(e);
      }
      finally
      {
         org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils.closeResultSet(resultSet);
      }
   }

   public static void applyRestrictions(QueryDescriptor desc, List<Join> predicateJoins, Predicate predicate)
   {
      if (predicateJoins != null)
      {
         for (Join join : predicateJoins)
         {
            ITableDescriptor tDesc = join.getRhsTableDescriptor();
            /*StructuredDataValueBean.class.equals(((TypeDescriptor) tDesc).getType())*/
            if (tDesc instanceof TypeDescriptor)
            {
               Class<?> type = ((TypeDescriptor) tDesc).getType();
               if (predicate.accept(type))
               {
                  Join sdvJoin = null;
                  if (join.isRequired())
                  {
                     sdvJoin = desc
                           .innerJoin(type, join.getTableAlias());
                  }
                  else
                  {
                     sdvJoin = desc
                           .leftOuterJoin(type, join.getTableAlias());
                  }
                  for (JoinElement element : join.getJoinConditions())
                  {
                     Pair<FieldRef, ? > condition = element.getJoinCondition();
                     if (condition.getSecond() instanceof FieldRef)
                     {
                        sdvJoin.addJoinCondition(condition.getFirst(), ((FieldRef) condition.getSecond()).fieldName, element.getJoinConditionType());
                     }
                     else
                     {
                        sdvJoin.addJoinConditionConstant(condition.getFirst(), (String) condition.getSecond(), element.getJoinConditionType());
                     }
                  }
                  AndTerm restriction = join.getRestriction();
                  if (restriction != null)
                  {
                     for (PredicateTerm term : restriction.getParts())
                     {
                        sdvJoin.where(replace(term, join, sdvJoin));
                     }
                  }
               }
            }
         }
      }
   }

   private static PredicateTerm replace(PredicateTerm term, ITableDescriptor source, ITableDescriptor destination)
   {
      if (term instanceof MultiPartPredicateTerm)
      {
         MultiPartPredicateTerm target =
               term instanceof AndTerm ? new AndTerm() :
               term instanceof OrTerm ? new OrTerm() :
               term instanceof AndNotTerm ? new AndNotTerm() :
               term instanceof OrNotTerm ? new OrNotTerm() : null;
         if (target != null)
         {
            for (PredicateTerm part : ((MultiPartPredicateTerm) term).getParts())
            {
               target.add(replace(part, source, destination));
            }
            return target;
         }
      }
      else if (term instanceof ComparisonTerm)
      {
         ComparisonTerm comparisonTerm = (ComparisonTerm) term;
         FieldRef lhsField = comparisonTerm.getLhsField();
         if (lhsField.getType() == source)
         {
            Operator operator = comparisonTerm.getOperator();
            FieldRef fieldRef = destination.fieldRef(lhsField.fieldName, lhsField.isIgnorePreparedStatements());
            if (operator.isUnary())
            {
               return new ComparisonTerm(fieldRef, (Operator.Unary) operator);
            }
            else if (operator.isBinary())
            {
               return new ComparisonTerm(fieldRef, (Operator.Binary) operator, comparisonTerm.getValueExpr());
            }
            else if (operator.isTernary())
            {
               return new ComparisonTerm(fieldRef, (Operator.Ternary) operator, (Pair<?,?>) comparisonTerm.getValueExpr());
            }
         }
      }
      return term;
   }

   private static IProcessInstance findUnboundProcessInstance(IData data, Object pkValue)
   {
      long modelOID = data.getModel().getModelOID();
      long dataRtOID = ModelManagerFactory.getCurrent().getRuntimeOid(data);
      String pk = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
      int typeClassification = LargeStringHolderBigDataHandler.classifyType(data, pk);

      QueryDescriptor desc = QueryDescriptor.from(ProcessInstanceBean.class);
      Join dvJoin = desc.innerJoin(DataValueBean.class).on(ProcessInstanceBean.FR__OID, DataValueBean.FIELD__PROCESS_INSTANCE);
      Join sdvJoin = desc.innerJoin(StructuredDataValueBean.class).on(ProcessInstanceBean.FR__OID, StructuredDataValueBean.FIELD__PROCESS_INSTANCE);
      Join sdJoin = desc.innerJoin(StructuredDataBean.class).on(sdvJoin.fieldRef(StructuredDataValueBean.FIELD__XPATH), StructuredDataBean.FIELD__OID)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__MODEL), StructuredDataBean.FIELD__MODEL)
            .andOn(dvJoin.fieldRef(DataValueBean.FIELD__DATA), StructuredDataBean.FIELD__DATA);

      FieldRef pkValueField = null;
      switch (typeClassification)
      {
      case BigData.STRING_VALUE:
         pkValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__STRING_VALUE);
         break;
      case BigData.NUMERIC_VALUE:
         pkValueField = sdvJoin.fieldRef(StructuredDataValueBean.FIELD__NUMBER_VALUE);
         break;
      default:
         // (fh) throw internal exception ?
      }

      desc.where(Predicates.andTerm(
            Predicates.isEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1),
            Predicates.isEqual(ProcessInstanceBean.FR__MODEL, modelOID),
            Predicates.isEqual(dvJoin.fieldRef(DataValueBean.FIELD__DATA), dataRtOID),
            Predicates.isEqual(sdJoin.fieldRef(StructuredDataBean.FIELD__XPATH), pk),
            pkValue instanceof Number
            ? Predicates.isEqual(pkValueField, ((Number) pkValue).longValue())
                  : Predicates.isEqual(pkValueField, pkValue.toString())));

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      //System.err.println(session.getDMLManager(desc.getType()).prepareSelectStatement(desc, true, null, true));
      return session.findFirst(ProcessInstanceBean.class, desc.getQueryExtension());
   }

   private static Object getNameValue(IData data, Object value)
   {
      if (value instanceof Map)
      {
         String nameExpression = data.getAttribute(PredefinedConstants.BUSINESS_OBJECT_NAMEEXPRESSION);
         return nameExpression == null ? null : ((Map<?, ?>) value).get(nameExpression);
      }
      return null;
   }

   private static Object getPK(IData data, Object value)
   {
      String pkId = data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
      if (value instanceof Map)
      {
         Object pk = ((Map<?, ?>) value).get(pkId);
         if (pk != null)
         {
            return pk;
         }
      }
      throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("primary key"));
   }

   public static BusinessObject createInstance(String businessObjectId, Object initialValue)
         throws ObjectNotFoundException, InvalidArgumentException, ObjectExistsException
   {
      if (initialValue == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("initialValue"));
      }

      IData data = findDataForUpdate(businessObjectId);
      lockData(data);
      IProcessInstance pi = findUnboundProcessInstance(data, getPK(data, initialValue));
      if (pi != null)
      {
         throw new ObjectExistsException(BpmRuntimeError.BPMRT_INVALID_VALUE.raise("initialValue"));
      }
      pi = ProcessInstanceBean.createUnboundInstance((IModel) data.getModel());
      return updateBusinessObjectInstance(pi, data, initialValue);
   }

   private static void lockData(IData data)
   {
      QueryDescriptor lockQuery = QueryDescriptor.from(AuditTrailDataBean.class)
            .where(Predicates.isEqual(AuditTrailDataBean.FR__MODEL, data.getModel().getModelOID()))
            .where(Predicates.isEqual(AuditTrailDataBean.FR__ID, data.getId()));
      AuditTrailDataBean atdb = (AuditTrailDataBean) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
            .findFirst(AuditTrailDataBean.class, lockQuery.getQueryExtension());
      atdb.lock();
   }

   public static BusinessObject updateInstance(String businessObjectId, Object value)
         throws ObjectNotFoundException, InvalidArgumentException
   {
      if (value == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("value"));
      }

      IData data = findDataForUpdate(businessObjectId);
      IProcessInstance pi = findUnboundProcessInstance(data, getPK(data, value));
      if (pi == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PROCESS_INSTANCE_OID.raise(0), 0);
      }
      pi.lock();
      return updateBusinessObjectInstance(pi, data, value);
   }

   public static void deleteInstance(String businessObjectId, Object pkValue)
   {
      IData data = findDataForUpdate(businessObjectId);
      IProcessInstance pi = findUnboundProcessInstance(data, pkValue);
      if (pi == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.ATDB_UNKNOWN_PROCESS_INSTANCE_OID.raise(0), 0);
      }
      pi.lock();
      Map<String, BusinessObjectRelationship> rels = getBusinessObjectRelationships(data);
      if (!rels.isEmpty())
      {
         Map<String, ?> value = (Map<String, ? >)pi.getInDataValue(data, null);
         if (value != null)
         {
            for (BusinessObjectRelationship rel : rels.values())
            {
               Object otherPk = value.get(rel.otherForeignKeyField);
               if (otherPk != null)
               {
                  switch (rel.otherCardinality)
                  {
                  case TO_ONE:
                     cleanup(rel, otherPk, pkValue);
                     break;
                  case TO_MANY:
                     for (Object itemPk : (Collection) otherPk)
                     {
                        cleanup(rel, itemPk, pkValue);
                     }
                     break;
                  }
               }
            }
         }
      }
      ProcessInstanceUtils.deleteProcessInstances(Collections.singletonList(pi.getOID()),
            (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL));
   }

   private static void cleanup(BusinessObjectRelationship rel, Object otherPk, Object pkValue)
   {
      IData data = findDataForUpdate(rel.otherBusinessObject.modelId, rel.otherBusinessObject.id);
      IProcessInstance pi = findUnboundProcessInstance(data, otherPk);
      if (pi != null)
      {
         pi.lock();
         Map<String, ?> value = (Map<String, ? >)pi.getInDataValue(data, null);
         if (value != null)
         {
            boolean changed = false;
            switch (rel.thisCardinality)
            {
            case TO_ONE:
               if (pkValue.equals(value.get(rel.thisForeignKeyField)))
               {
                  value.remove(rel.thisForeignKeyField);
                  changed = true;
               }
               break;
            case TO_MANY:
               changed = ((Collection) value.get(rel.thisForeignKeyField)).remove(pkValue);
               break;
            }
            if (changed)
            {
               pi.setOutDataValue(data, null, value);
            }
         }
      }
   }

   private static BusinessObject updateBusinessObjectInstance(IProcessInstance pi, IData data, Object newValue)
   {
      pi.setOutDataValue(data, null, newValue);
      Object dataValue = pi.getInDataValue(data, null);
      BusinessObjectDetails.Value value = new BusinessObjectDetails.ValueDetails(pi.getOID(), dataValue);
      BusinessObjectDetails detailsObject = new BusinessObjectDetails(data.getModel().getModelOID(), data.getModel().getId(),
            data.getId(), data.getName(), null, Collections.singletonList(value));

      // create departments
      IModel model = (IModel) data.getModel();
      String managedOrganizations = (String) data.getAttribute(PredefinedConstants.BUSINESS_OBJECT_MANAGEDORGANIZATIONS);

      ModelManager current = ModelManagerFactory.getCurrent();
      QueryService queryService = new QueryServiceImpl();

      if(!StringUtils.isEmpty(managedOrganizations))
      {
         String[] managedOrganizationsArray = managedOrganizations.split(",");
         for (String organizationFullId : managedOrganizationsArray)
         {
            IModel activeModel = model;

            String modelId = null;
            organizationFullId = organizationFullId.substring(1, organizationFullId.length() - 1);
            //organizationFullId = organizationFullId.replaceAll("\\[", "");
            //organizationFullId = organizationFullId.replaceAll("\\]", "");
            organizationFullId = organizationFullId.replaceAll("\\\"", "");

            String organizationId = organizationFullId;
            if (organizationFullId.split(":").length > 1)
            {
               modelId = organizationFullId.split(":")[0];
               organizationId = organizationFullId.split(":")[1];
            }

            if(!StringUtils.isEmpty(modelId) && !CompareHelper.areEqual(modelId, model.getId()))
            {
               activeModel = current.findActiveModel(modelId);
            }

            Organization organization = (Organization) queryService.getParticipant(activeModel.getModelOID(), organizationId);
            Object pk = getPK(data, newValue);
            if (pk != null)
            {
               String id = pk.toString();
               if (!id.isEmpty())
               {
                  Object name = getNameValue(data, newValue);
                  DepartmentUtils.createOrModifyDepartment(id, name == null ? id : name.toString(), "", null, organization);
               }
            }
         }
      }

      return detailsObject;
   }

   private static IData findDataForUpdate(String qualifiedBusinessObjectId)
         throws ObjectNotFoundException, InvalidArgumentException
   {
      QName qname = QName.valueOf(qualifiedBusinessObjectId);
      String modelId = qname.getNamespaceURI();
      String businessObjectId = qname.getLocalPart();

      return findDataForUpdate(modelId, businessObjectId);
   }

   protected static IData findDataForUpdate(String modelId, String businessObjectId)
   {
      final ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel model = modelManager.findActiveModel(modelId);
      if (model == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_NO_ACTIVE_MODEL_WITH_ID.raise(modelId));
      }
      IData data = model.findData(businessObjectId);
      if (data == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(businessObjectId));
      }
      return data;
   }

   public static PredicateTerm filter(PredicateTerm source)
   {
      // TODO: provide generic filtering like PredicateTerm.filter(Predicate)
      if (source instanceof MultiPartPredicateTerm)
      {
         if (((MultiPartPredicateTerm) source).getParts().isEmpty())
         {
            return null;
         }

         MultiPartPredicateTerm target = null;
         if (source instanceof AndTerm)
         {
            target = new AndTerm();
         }
         else if (source instanceof OrTerm)
         {
            target = new OrTerm();
         }
         else if (source instanceof AndNotTerm)
         {
            target = new AndNotTerm();
         }
         else if (source instanceof OrNotTerm)
         {
            target = new OrNotTerm();
         }

         for (PredicateTerm term : ((MultiPartPredicateTerm) source).getParts())
         {
            term = filter(term);
            if (term != null)
            {
               target.add(term);
            }
         }

         return target.getParts().isEmpty() ? null : target;
      }
      else // Comparison term
      {
         FieldRef fr = ((ComparisonTerm) source).getLhsField();
         Class<?> type = fr.getBoundType();
         if (ProcessInstanceBean.class.equals(type) || ModelPersistorBean.class.equals(type))
         {
            return null;
         }
      }
      return source;
   }

   public static void copyFilters(FilterTerm source, FilterTerm target, Predicate predicate)
   {
      for (Object part : source.getParts())
      {
         if (part instanceof FilterAndTerm)
         {
            copyFilters((FilterAndTerm) part, target.addAndTerm(), predicate);
         }
         else if (part instanceof FilterAndNotTerm)
         {
            copyFilters((FilterAndNotTerm) part, target.addAndNotTerm(), predicate);
         }
         else if (part instanceof FilterOrTerm)
         {
            copyFilters((FilterOrTerm) part, target.addOrTerm(), predicate);
         }
         else if (part instanceof FilterOrNotTerm)
         {
            copyFilters((FilterOrNotTerm) part, target.addOrNotTerm(), predicate);
         }
         else if (predicate.accept(part))
         {
            target.add((FilterCriterion) part);
         }
      }
   }

   public static BusinessObject getBusinessObject(IData source)
   {
      BusinessObject businessObject = source.getRuntimeAttribute(BUSINESS_OBJECT_ATT);
      if (businessObject == null && hasBusinessObject(source))
      {
         synchronized (source)
         {
            businessObject = source.getRuntimeAttribute(BUSINESS_OBJECT_ATT);
            if (businessObject == null)
            {
               businessObject = createBusinessObject(source);
               source.setRuntimeAttribute(BUSINESS_OBJECT_ATT, businessObject);
            }
         }
      }
      return businessObject;
   }

   private static BusinessObject createBusinessObject(IData source)
   {
      List<Definition> items = null;
      IXPathMap map = DataXPathMap.getXPathMap(source);
      TypedXPath root = map.getRootXPath();
      if (root != null)
      {
         items = createDescriptions(source, root.getChildXPaths(), true);
      }
      return new BusinessObjectDetails(source.getModel().getModelOID(), source.getModel().getId(),
            source.getId(), source.getName(), items, null);
   }

   private static List<Definition> createDescriptions(IData source, List<TypedXPath> xPaths, boolean top)
   {
      List<Definition> items = CollectionUtils.newList(xPaths.size());
      for (TypedXPath xPath : xPaths)
      {
         boolean primaryKey = top ? xPath.getId().equals(source.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT)) : false;
         items.add(new BusinessObjectDetails.DefinitionDetails(xPath.getId(), xPath.getType(),
               StringUtils.isEmpty(xPath.getXsdTypeName())
               ? null
               : StringUtils.isEmpty(xPath.getXsdTypeNs())
                     ? new QName(xPath.getXsdTypeName())
                     : new QName(xPath.getXsdTypeNs(), xPath.getXsdTypeName()),
         xPath.isList(),
         xPath.getAnnotations().isIndexed(), primaryKey ,
         xPath.getType() == BigData.NULL ? createDescriptions(source, xPath.getChildXPaths(), false) : null));
      }
      return items;
   }

   public static boolean hasBusinessObject(IData data)
   {
      if (data == null)
      {
         return false;
      }
      return StructuredTypeRtUtils.isStructuredType(data)
            && data.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT) != null;
   }

   public static class BusinessObjectsListener implements DataValueChangeListener
   {
      private static final Logger trace = LogManager.getLogger(BusinessObjectsListener.class);

      @Override
      public void onDataValueChanged(IDataValue dv)
      {
         IProcessInstance pi = dv.getProcessInstance();
         try
         {
            if (pi.getProcessDefinition() != null)
            {
               IData data = dv.getData();
               if (hasBusinessObject(data))
               {
                  Serializable value = (Serializable) pi.getInDataValue(data, null);
                  Object pk = getPK(data, value);

                  // need to search in memory first for UPIs created in this transaction
                  IProcessInstance upi = null;
                  String key = "BusinessObject:" + data.getModel().getModelOID() + ':' + data.getElementOID() + ':' + pk;
                  BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
                  if (rtEnv != null)
                  {
                     upi = (IProcessInstance) rtEnv.get(key);
                     if (upi == null)
                     {
                        upi = findUnboundProcessInstance(data, pk);
                        if (upi == null)
                        {
                           lockData(data);
                           //System.err.println("Creating BO from regular PI data.");
                           upi = ProcessInstanceBean.createUnboundInstance((IModel) data.getModel());
                           if (trace.isDebugEnabled())
                           {
                              trace.debug("Created new BO process: " + upi.getOID());
                           }
                        }
                        else
                        {
                           upi.lock();
                           //System.err.println("Updating BO from regular PI data.");
                        }
                        rtEnv.setProperty(key, upi);
                     }
                  }
                  upi.setOutDataValue(data, null, value);
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Updated business object: " + dv.getData() + " by " + pi);
                  }
               }
            }
         }
         catch (Exception ex)
         {
            // (fh) do nothing
         }
      }
   }

   public static class BusinessObjectProcessQueryProcessor implements ParsedQueryProcessor
   {
      @Override
      public ParsedQuery processQuery(ParsedQuery parsedQuery)
      {
         // (fh) add clause to exclude business objects synthetic process instances.
         PredicateTerm predicateTerm = parsedQuery.getPredicateTerm();
         if (predicateTerm == null)
         {
            predicateTerm = Predicates.notEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1);
         }
         else
         {
            predicateTerm = Predicates.andTerm(
                  Predicates.notEqual(ProcessInstanceBean.FR__PROCESS_DEFINITION, -1), predicateTerm);
         }

         return new ParsedQuery(parsedQuery.getSelectExtension(), predicateTerm, parsedQuery.getPredicateJoins(),
               parsedQuery.getOrderCriteria(), parsedQuery.getOrderByJoins(), parsedQuery.getFetchPredicate(),
               parsedQuery.useDistinct(), parsedQuery.getSelectAlias());
      }
   }

   public static Map<String, BusinessObjectRelationship> getBusinessObjectRelationships(IData data)
   {
      Map<String, BusinessObjectRelationship> map = data.getRuntimeAttribute(BUSINESS_OBJECT_RELATIONSHIPS_ATT);
      if (map == null)
      {
         BusinessObjectRelationship[] relationships = BusinessObjectRelationship.fromJsonString(
               data.getStringAttribute("carnot:engine:businessObjectRelationships"));
         if (relationships.length == 0)
         {
            map = Collections.emptyMap();
         }
         else
         {
            map = CollectionUtils.newMap();
            for (BusinessObjectRelationship relationship : relationships)
            {
               if (!StringUtils.isEmpty(relationship.otherForeignKeyField))
               {
                  map.put(relationship.otherForeignKeyField, relationship);
               }
            }
         }
         data.setRuntimeAttribute(BUSINESS_OBJECT_RELATIONSHIPS_ATT, map);
      }
      return map;
   }

   public static String getPrimaryKeyFromBusinessObject(BusinessObject bo)
   {
      String primaryKey = null;
      for(BusinessObject.Definition boDef : bo.getItems())
      {
         if(boDef.isPrimaryKey())
         {
            primaryKey = boDef.getName();
            break;
         }
      }

      if(primaryKey == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("primary key"));
      }
      return primaryKey;
   }
}