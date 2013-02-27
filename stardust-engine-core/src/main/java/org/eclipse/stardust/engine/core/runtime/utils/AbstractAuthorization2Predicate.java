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
package org.eclipse.stardust.engine.core.runtime.utils;

import static org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties.QUERY_EVALUATION_PROFILE_LEGACY;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.EventType;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IEventAction;
import org.eclipse.stardust.engine.api.model.IEventConditionType;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.PluggableType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.DataPrefetchHint;
import org.eclipse.stardust.engine.api.query.EvaluationContext;
import org.eclipse.stardust.engine.api.query.ExcludeUserPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryServiceUtils;
import org.eclipse.stardust.engine.api.query.RuntimeInstanceQueryEvaluator;
import org.eclipse.stardust.engine.api.query.SqlBuilder.ParsedQuery;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.UserPK;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.persistence.AndTerm;
import org.eclipse.stardust.engine.core.persistence.Column;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.JoinElement;
import org.eclipse.stardust.engine.core.persistence.MultiPartPredicateTerm;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.persistence.Operator.Ternary;
import org.eclipse.stardust.engine.core.persistence.Operator.Unary;
import org.eclipse.stardust.engine.core.persistence.OrTerm;
import org.eclipse.stardust.engine.core.persistence.PredicateTerm;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.ITableDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.runtime.beans.IDataValue;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;
import org.eclipse.stardust.engine.core.struct.DataXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

/**
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public abstract class AbstractAuthorization2Predicate implements Authorization2Predicate
{
   private static final Logger trace = LogManager.getLogger(Authorization2Predicate.class);

   private static FieldRef[] EMPTY = {};

   AuthorizationContext context;

   FetchPredicate delegate;

   private int extensionIndex;

   private List<FieldRef> selectExtension;

   private List<Pair<String, String>> orderedPrefetchData = Collections.EMPTY_LIST;
   
   private Set<Pair<String, String>> distinctData = CollectionUtils.newHashSet();

   private ModelManager modelManager;

   Map<String, DataPrefetchHint> dataPrefetchHintFilter = CollectionUtils.newMap();

   public AbstractAuthorization2Predicate(AuthorizationContext context)
   {
      this.context = context;
   }

   public ModelManager getModelManager()
   {
      if (modelManager == null)
      {
         modelManager = ModelManagerFactory.getCurrent();
      }
      return modelManager;
   }   
   
   public void setFetchPredicate(FetchPredicate delegate)
   {
      this.delegate = delegate;
   }

   public FieldRef[] getReferencedFields()
   {
      Collection<FieldRef> fields = CollectionUtils.newHashSet();

      FieldRef[] localFields = getLocalFields();
      if (localFields != null)
      {
         fields.addAll(Arrays.asList(localFields));
      }
      if (delegate != null)
      {
         FieldRef[] delegateFields = delegate.getReferencedFields();
         if (delegateFields != null)
         {
            fields.addAll(Arrays.asList(delegateFields));
         }
      }

      // append to the end of column list
      if (selectExtension != null)
      {
         fields = CollectionUtils.newArrayList(fields);
         fields.addAll(selectExtension);
      }

      return fields.toArray(new FieldRef[fields.size()]);
   }

   public FieldRef[] getLocalFields()
   {
      return EMPTY;
   }
   
   public boolean hasDataPrefetchHintFilter()
   {
      return dataPrefetchHintFilter != null && !dataPrefetchHintFilter.isEmpty();
   }

   public boolean addPrefetchDataHints(Query query)
   {
      String evaluationProfile = RuntimeInstanceQueryEvaluator.getEvaluationProfile(query);
      boolean isLegacyEvaluation = QUERY_EVALUATION_PROFILE_LEGACY
            .equals(evaluationProfile);      
            
      if (!isLegacyEvaluation)
      {         
         this.orderedPrefetchData = CollectionUtils.newArrayList();
         FilterAndTerm queryFilter = query.getFilter();      
         
         if(query instanceof ActivityInstanceQuery)
         {
            if (query.getPolicy(ExcludeUserPolicy.class) != null)
            {
               getExcludeUserFilter(queryFilter);
            }      
         }      
         
         Collection<IOrganization> restricted = context.getRestricted();
         for (IOrganization organization : restricted)
         {
            String dataId = organization
                  .getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
            if ( !StringUtils.isEmpty(dataId))
            {
               IModel model = (IModel) organization.getModel();
               String modelId = model.getId();
               
               String dataPath = organization
                     .getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);
               Pair<String, String> dataKey = new Pair("{" + modelId + "}" + dataId, dataPath);
               if ( !distinctData.contains(dataKey))
               {
                  distinctData.add(dataKey);
                  orderedPrefetchData.add(dataKey);

                  DataPrefetchHint filter = new DataPrefetchHint("{" + modelId + "}" + dataId, StringUtils
                        .isEmpty(dataPath) ? null : dataPath);
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Adding prefetch filter: " + filter);
                  }
                  if (!queryFilter.getParts().contains(filter))
                  {
                     queryFilter.and(filter);
                  }
               }
            }
         }
         context.setPrefetchDataAvailable( !distinctData.isEmpty());
      }
      else
      {
         Collection<IOrganization> restricted = context.getRestricted();
         if ( !restricted.isEmpty())
         {
            trace.warn("Prefetching of department data not applied as selected evaluation profile '"
                  + evaluationProfile + "' does not support it.");
         }
      }      
      
      return isLegacyEvaluation;
   }

   public void setSelectionExtension(int extensionIndex, List<FieldRef> selectExtension)
   {
      this.extensionIndex = extensionIndex;
      this.selectExtension = selectExtension;
   }

   public void check(Object o)
   {
      if (!accept(o))
      {
         throw new AccessForbiddenException(BpmRuntimeError.AUTHx_AUTH_MISSING_GRANTS.raise(
               context.getUser().getOID(), String.valueOf(context.getPermission())));
      }
   }

   public boolean accept(Object o)
   {
      if (o instanceof ResultSet)
      {
         ResultSet rs = (ResultSet) o;
         if (selectExtension != null && !selectExtension.isEmpty() && resultSetHasExtension(rs))
         {
            try
            {
               Map<Pair<String, String>, IOrganization> orgLookup = initTracing();

               int absExtIndex = translateExtensionIndex(rs);
               for (int i = 0; i < orderedPrefetchData.size(); i++)
               {
                  Pair<String, String> prefetchData = orderedPrefetchData.get(i);
                  int type = rs.getInt(absExtIndex + i * 3);
                  Object value = null;
                  if ( !rs.wasNull())
                  {
                     if (type != BigData.NULL)
                     {
                        value = rs.getObject(absExtIndex + i * 3 + 1);
                     }
                     if (trace.isDebugEnabled())
                     {
                        IOrganization org = orgLookup.get(prefetchData);
                        trace.debug("Prefetched value: " + org + " = " + value);
                     }
                  }
                  String dataId = prefetchData.getFirst();
                  String dataPath = prefetchData.getSecond();
                  context.setPrefetchedDataValue(dataId, dataPath, value == null ? null : value.toString());
                  
                  addExcludeUserDataPrefetchColIdx(dataId, absExtIndex + i * 3 + 2);
               }
            }
            catch (SQLException e)
            {
               // just ignore
               trace.warn("", e);
            }
         }
      }
      return true;
   }

   private void addExcludeUserDataPrefetchColIdx(String dataId, int colIdx)
   {
      int idx = dataId.indexOf("}");
      dataId = dataId.substring(idx + 1);
      DataPrefetchHint dataPrefetchHint = dataPrefetchHintFilter.get(dataId);
      if (dataPrefetchHint != null)
      {
         dataPrefetchHint.setPrefetchNumberValueColumnIdx(colIdx);
      }
   }

   private Map<Pair<String, String>, IOrganization> initTracing()
   {
      if (trace.isDebugEnabled())
      {
         // preparation for later tracing on debug level
         Map<Pair<String, String>, IOrganization> orgLookup = CollectionUtils
               .newHashMap();
         Collection<IOrganization> restricted = context.getRestricted();
         for (IOrganization org : restricted)
         {
            String dataId = org
                  .getStringAttribute(PredefinedConstants.BINDING_DATA_ID_ATT);
            String dataPath = org
                  .getStringAttribute(PredefinedConstants.BINDING_DATA_PATH_ATT);
            orgLookup.put(new Pair(dataId, dataPath), org);
         }
         return orgLookup;
      }
      else
      {
         return Collections.EMPTY_MAP;
      }
   }

   private boolean resultSetHasExtension(ResultSet rs)
   {
      try
      {
         int absExtIndex = translateExtensionIndex(rs);
         return absExtIndex < rs.getMetaData().getColumnCount();
      }
      catch (SQLException e)
      {
         // just ignore
         trace.warn("", e);
      }
      return false;
   }

   /**
    * If extensionIndex is negative it is interpreted as relative from the end of the columns in the result set,
    * otherwise normal indext to the result set.
    *
    * @param rs
    * @return
    * @throws SQLException
    */
   private int translateExtensionIndex(ResultSet rs) throws SQLException
   {
      if (extensionIndex < 0)
      {
         return extensionIndex + rs.getMetaData().getColumnCount() + 1;
      }
      return extensionIndex;
   }

   public void addRawPrefetch(QueryDescriptor sqlQuery, FieldRef frScopeProcessInstance)
   {
      // prepare and parse dummy query
      ActivityInstanceQuery dummyAiQuery = ActivityInstanceQuery.findAll();
      addPrefetchDataHints(dummyAiQuery);

      if (!context.isPrefetchDataAvailable())
      {
         // no prefetch necessary
         return;
      }

      final EvaluationContext evaluationContext = QueryServiceUtils.getDefaultEvaluationContext();
      ParsedQuery parsedQuery = new ActivityInstanceQueryEvaluator(dummyAiQuery, evaluationContext)
            .parseQuery();

      // build up join map (orig join -> new join) based on prefetch extension
      Map<Join, Join> prefetchJoinMap = CollectionUtils.newHashMap();
      List<FieldRef> origPrefetchExtension = parsedQuery.getSelectExtension();
      for (FieldRef fieldRef : origPrefetchExtension)
      {
         ITableDescriptor rawTableDescriptor = fieldRef.getType();
         if (rawTableDescriptor instanceof Join)
         {
            prefetchJoinMap.put((Join) rawTableDescriptor, null);
         }
      }

      // create new joins and map them to the original joins
      for (Join origJoin : prefetchJoinMap.keySet())
      {
         Join newJoin = new Join(origJoin.getRhsTableDescriptor(), origJoin.getTableAlias());
         newJoin.setRequired(origJoin.isRequired());
         prefetchJoinMap.put(origJoin, newJoin);

         List<JoinElement> joinConditions = origJoin.getJoinConditions();

         // add all join conditions with new LHS field references
         for (JoinElement joinElement : joinConditions)
         {
            Pair<FieldRef, ? > frPair = joinElement.getJoinCondition();
            Object rhs = frPair.getSecond();
            if (rhs instanceof FieldRef)
            {
               newJoin.on(frScopeProcessInstance, ((FieldRef) rhs).fieldName);
            }
         }

         // add new restriction
         PredicateTerm newRestriction = copyPredicate(origJoin.getRestriction(), origJoin,
               newJoin);
         if (newRestriction != null)
         {
            newJoin.where(newRestriction);
         }

         // add joins
         sqlQuery.getQueryExtension().addJoin(newJoin);
      }

      // rebind field references for prefetch extension
      FieldRef[] additionalCols = new FieldRef[origPrefetchExtension.size()];
      int idx = 0;
      for (FieldRef fieldRef : origPrefetchExtension)
      {
         Join join = prefetchJoinMap.get(fieldRef.getType());
         additionalCols[idx] = join.fieldRef(fieldRef.fieldName);
         idx++;
      }

      // add field references to "group by"
      Collection groupCriteria = sqlQuery.getQueryExtension().getGroupCriteria();
      if (groupCriteria != null && !groupCriteria.isEmpty())
      {
         sqlQuery.groupBy(additionalCols);
      }

      // add additional columns to select list
      Column[] origColumns = sqlQuery.getQueryExtension().getSelection();
      Column[] newColumns = new Column[origColumns.length + additionalCols.length];
      System.arraycopy(origColumns, 0, newColumns, 0, origColumns.length);
      System.arraycopy(additionalCols, 0, newColumns, origColumns.length, additionalCols.length);
      sqlQuery.select(newColumns);

      setSelectionExtension(origColumns.length + 1, Arrays.asList(additionalCols));
   }

   private PredicateTerm copyPredicate(PredicateTerm predicate, Join source, Join target)
   {
      final PredicateTerm newTerm;

      if (predicate == null)
      {
         return null;
      }
      else if (predicate instanceof MultiPartPredicateTerm)
      {
         MultiPartPredicateTerm newMulti = predicate instanceof AndTerm ? new AndTerm() : new OrTerm();

         MultiPartPredicateTerm multiTerm = (MultiPartPredicateTerm) predicate;
         for (PredicateTerm predicateTerm : multiTerm.getParts())
         {
            newMulti.add(copyPredicate(predicateTerm, source, target));
         }

         newTerm = newMulti;
      }
      else
      {
         ComparisonTerm origComp = (ComparisonTerm) predicate;
         FieldRef lhsField = origComp.getLhsField();
         if(lhsField.getType().equals(source))
         {
            lhsField = target.fieldRef(lhsField.fieldName);
         }

         if (origComp.getOperator().isUnary())
         {
            newTerm = new ComparisonTerm(lhsField, (Unary) origComp.getOperator());
         }
         else if (origComp.getOperator().isBinary())
         {
            newTerm = new ComparisonTerm(lhsField, (Binary) origComp.getOperator(), origComp.getValueExpr());
         }
         else // if (origComp.getOperator().isTernary())
         {
            newTerm = new ComparisonTerm(lhsField, (Ternary) origComp.getOperator(), (Pair) origComp.getValueExpr());
         }
      }

      return newTerm;
   }
   
   public void getExcludeUserFilter(FilterAndTerm queryFilter)
   {
      if (isExcludeUserEvaluationEnabled())
      {
         List<IModel> activeModels = ModelManagerFactory.getCurrent().findActiveModels();
         for (IModel model : activeModels)
         {
            String modelId = model.getId();
            for (IProcessDefinition process : model.getProcessDefinitions())
            {
               for (IActivity activity : process.getActivities())
               {
                  if (activity
                        .hasEventHandlers(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
                  {
                     for (int k = 0; k < activity.getEventHandlers().size(); ++k)
                     {
                        IEventHandler handler = (IEventHandler) activity
                              .getEventHandlers().get(k);
                        if (((IEventConditionType) handler.getType()).getImplementation() != EventType.Pull)
                        {
                           for (Iterator l = handler.getAllEventActions(); l.hasNext();)
                           {
                              IEventAction action = (IEventAction) l.next();
                              PluggableType type = action.getType();
                              String instanceName = type
                                    .getStringAttribute(PredefinedConstants.ACTION_CLASS_ATT);
                              String excludeUserAction = PredefinedConstants.EXCLUDE_USER_ACTION_CLASS;
                              Class classFromClassName = Reflect.getClassFromClassName(
                                    excludeUserAction, false);
                              if (classFromClassName != null)
                              {
                                 excludeUserAction = classFromClassName.getName();
                              }

                              if (instanceName.equals(excludeUserAction))
                              {
                                 Map<String, Object> attributes = action
                                       .getAllAttributes();
                                 String dataId = (String) attributes
                                       .get(PredefinedConstants.EXCLUDED_PERFORMER_DATA);
                                 String dataPath = (String) attributes
                                       .get(PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH);

                                 IData data = getData(model, dataId);
                                 if (data != null && !isStructuredType(data))
                                 {
                                    dataPath = null;
                                 }

                                 Pair<String, String> dataKey = new Pair("{" + modelId
                                       + "}" + dataId, dataPath);
                                 if (!distinctData.contains(dataKey))
                                 {
                                    distinctData.add(dataKey);
                                    orderedPrefetchData.add(dataKey);

                                    DataPrefetchHint filter = new DataPrefetchHint("{"
                                          + modelId + "}" + dataId,
                                          StringUtils.isEmpty(dataPath) ? null : dataPath);
                                    if (trace.isDebugEnabled())
                                    {
                                       trace.debug("Adding prefetch filter: " + filter);
                                    }

                                    if (!queryFilter.getParts().contains(filter))
                                    {
                                       queryFilter.and(filter);
                                       dataPrefetchHintFilter.put(dataId, filter);
                                    }
                                    else
                                    {
                                       int idx = queryFilter.getParts().indexOf(filter);
                                       filter = idx < 0
                                             ? filter
                                             : (DataPrefetchHint) queryFilter.getParts()
                                                   .get(idx);
                                       dataPrefetchHintFilter.put(dataId, filter);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
   
   boolean isExcludeUserEvaluationEnabled()
   {
      return Parameters.instance().getBoolean(
            KernelTweakingProperties.ENGINE_EXCLUDE_USER_EVALUATION, false);
   }

   private IData getData(IModel model, String id)
   {
      for(IData data : model.getData())
      {
         if(!StringUtils.isEmpty(data.getId()) && data.getId().equals(id))
         {
            return data;
         }         
      }
    
      return null;
   }   
   
   private boolean isStructuredType(IData data)
   {
      if (data.getType().getId().equals(org.eclipse.stardust.engine.core.compatibility.extensions.dms.DmsConstants.DATA_TYPE_ID_DOCUMENT)
            || data.getType().getId().equals(org.eclipse.stardust.engine.core.compatibility.extensions.dms.DmsConstants.DATA_TYPE_ID_DOCUMENT_SET)
            || data.getType().getId().equals(DmsConstants.DATA_TYPE_DMS_DOCUMENT)
            || data.getType().getId().equals(DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST)
            || data.getType().getId().equals(DmsConstants.DATA_TYPE_DMS_FOLDER)
            || data.getType().getId().equals(DmsConstants.DATA_TYPE_DMS_FOLDER_LIST)
            || data.getType().getId().equals(PredefinedConstants.STRUCTURED_DATA))
      {
         return true;
      }      
      
      return false;
   }
   
   public boolean isExcludedUser(long activityRtOid, long processInstanceOID,
         long modelOid, Map<String, Long> dataValueOids)
   {
      if (isExcludeUserEvaluationEnabled())
      {
         if (processInstanceOID == 0)
         {
            return false;
         }

         long dataValueOid = 0;
         IUser currentUser = SecurityProperties.getUser();
         long currentPerformer = currentUser.getOID();

         ModelManager mm = getModelManager();
         IActivity activity = mm.findActivity(modelOid, activityRtOid);

         if (activity
               .hasEventHandlers(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
         {
            for (int k = 0; k < activity.getEventHandlers().size(); ++k)
            {
               IEventHandler handler = (IEventHandler) activity.getEventHandlers().get(k);
               if (((IEventConditionType) handler.getType()).getImplementation() != EventType.Pull)
               {
                  for (Iterator l = handler.getAllEventActions(); l.hasNext();)
                  {
                     IEventAction action = (IEventAction) l.next();
                     PluggableType type = action.getType();
                     String instanceName = type
                           .getStringAttribute(PredefinedConstants.ACTION_CLASS_ATT);
                     String excludeUserAction = PredefinedConstants.EXCLUDE_USER_ACTION_CLASS;
                     Class classFromClassName = Reflect.getClassFromClassName(
                           excludeUserAction, false);
                     if (classFromClassName != null)
                     {
                        excludeUserAction = classFromClassName.getName();
                     }

                     if (instanceName.equals(excludeUserAction))
                     {
                        Map<String, Object> attributes = action.getAllAttributes();
                        String dataId = (String) attributes
                              .get(PredefinedConstants.EXCLUDED_PERFORMER_DATA);
                        String dataPath = (String) attributes
                              .get(PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH);
                        IData data = ModelUtils.getData(activity.getProcessDefinition(),
                              dataId);
                        if (dataValueOids.containsKey(dataId))
                        {
                           dataValueOid = dataValueOids.get(dataId);
                        }
                        if (PredefinedConstants.LAST_ACTIVITY_PERFORMER.equals(data
                              .getId()))
                        {
                           IUser lastActivityPerformer = ActivityInstanceBean
                                 .getLastActivityPerformer(processInstanceOID);
                           Object value = lastActivityPerformer != null
                                 ? lastActivityPerformer.getPrimaryKey()
                                 : null;
                           dataValueOid = getDataValueOid(value);
                        }
                        if (PredefinedConstants.STARTING_USER.equals(data.getId()))
                        {
                           IProcessInstance processInstance = ProcessInstanceBean
                                 .findByOID(processInstanceOID);
                           IDataValue dataValue = processInstance.getDataValue(data);
                           Object value = dataValue.getValue();
                           dataValueOid = getDataValueOid(value);
                        }
                        boolean isPrimitiveStructType = false;
                        if (StructuredTypeRtUtils
                              .isStructuredType(data.getType().getId()))
                        {
                           IXPathMap xPathMap = DataXPathMap.getXPathMap(data);
                           TypedXPath typedXPath = xPathMap.getXPath(dataPath);
                           isPrimitiveStructType = StructuredDataXPathUtils
                                 .isPrimitiveType(typedXPath);
                        }
                        if (!isPrimitiveStructType && !StringUtils.isEmpty(dataPath))
                        {
                           IProcessInstance processInstance = ProcessInstanceBean
                                 .findByOID(processInstanceOID);
                           IDataValue dataValue = processInstance.getDataValue(data);
                           ExtendedAccessPathEvaluator evaluator = SpiUtils
                                 .createExtendedAccessPathEvaluator(data, dataPath);
                           AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
                                 processInstance, null, null, null);
                           Object value = evaluator.evaluate(data, dataValue.getValue(),
                                 dataPath, evaluationContext);
                           dataValueOid = getDataValueOid(value);
                        }

                        if (currentPerformer == dataValueOid)
                        {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }
      return false;
   }

   private long getDataValueOid(Object value)
   {
      long oid = 0;
      if (value instanceof Long)
      {
         oid = (Long) value;
      }
      else if (value instanceof UserPK)
      {
         try
         {
            oid = Long.parseLong(value.toString());
         }
         catch (NumberFormatException e)
         {
         }
      }
      return oid;
   }
}