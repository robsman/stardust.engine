/*******************************************************************************
 * Copyright (c) 2011, 2014 SunGard CSA LLC and others.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.UnresolvedExternalReference;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


/**
 * @author rainer.pielmann
 */
public class ModelRefBean extends PersistentBean implements Serializable
{
   public static enum TYPE
   {
      USES, IMPLEMENTS
   }

   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ModelRefBean.class);

   public static final String FIELD__CODE = "code";
   public static final String FIELD__MODEL_OID = "modelOid";
   public static final String FIELD__ID = "id";
   public static final String FIELD__REF_OID = "refOid";
   public static final String FIELD__DEPLOYMENT = "deployment";

   public static final FieldRef FR__CODE = new FieldRef(ModelRefBean.class, FIELD__CODE);
   public static final FieldRef FR__MODEL_OID = new FieldRef(ModelRefBean.class, FIELD__MODEL_OID);
   public static final FieldRef FR__ID = new FieldRef(ModelRefBean.class, FIELD__ID);
   public static final FieldRef FR__REF_OID = new FieldRef(ModelRefBean.class, FIELD__REF_OID);
   public static final FieldRef FR__DEPLOYMENT = new FieldRef(ModelRefBean.class, FIELD__DEPLOYMENT);

   public static final String TABLE_NAME = "model_ref";
   public static final String DEFAULT_ALIAS = "mr";

   public static final String[] PK_FIELD = new String[] {FIELD__CODE, FIELD__MODEL_OID, FIELD__ID, FIELD__REF_OID, FIELD__DEPLOYMENT};
   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] model_ref_idx1_UNIQUE_INDEX = new String[]{FIELD__CODE, FIELD__MODEL_OID, FIELD__ID, FIELD__DEPLOYMENT};
   public static final String[] model_ref_idx2_UNIQUE_INDEX = new String[]{FIELD__CODE, FIELD__MODEL_OID, FIELD__REF_OID, FIELD__DEPLOYMENT};

   private static final boolean DEPLOY_AGAINST_ACTIVE_MODEL = false;

   //private static final boolean DEPLOY_AGAINST_ACTIVE_MODEL = false;

   /**
    * Contains the entry type, either USES or IMPLEMENTS
    */
   private int code;

   /**
    * Depending on the code, it is either the using model oid or the interface oid
    */
   private long modelOid;

   /**
    * Depending on the code, it is either the used model id, or the id of the model providing the primary implementation.
    */
   private String id;

   /**
    * Depending on the code, it is either the resolved model oid, or the runtime oid of the process definition defining the process interface.
    */
   private long refOid;

   /**
    * The deployment sequence number. Foreign key into the ModelDeploymentBean.
    */
   private long deployment;

   public ModelRefBean()
   {
   }

   private ModelRefBean(TYPE type, long modelOid, String id, long refOid, long deployment)
   {
      this.code = type.ordinal();
      this.modelOid = modelOid;
      this.id = id;
      this.refOid = refOid;
      this.deployment = deployment;
   }

   public String toString()
   {
      if (code == 0)
      {
         return "Model reference: " + modelOid + '[' + id + "]=" + refOid + '/' + deployment;
      }
      else
      {
         return "Primary implementation: " + modelOid + '[' + refOid + "]=" + id + '/' + deployment;
      }
   }

   static Map<Long, Set<Long>> getModelReferences()
   {
      Map<Long, Set<Long>> usage = new HashMap<Long, Set<Long>>();
      PredicateTerm typePredicate = Predicates.isEqual(FR__CODE, TYPE.USES.ordinal());
      QueryDescriptor query = QueryDescriptor.from(ModelRefBean.class)
            .select(FR__MODEL_OID, FR__REF_OID).where(typePredicate);
      // Using eclipse model debugger results in ClassCastException when casting retrieved
      // session object to org.eclipse.stardust.engine.core.persistence.jdbc.Session as it
      // is an instance of DebugSession.
      // Therefore the way how to retrieve model_oid and model_ref differs when session is
      // an instance of DebugSession.
      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL);
      // jdbc session
      if (session instanceof Session)
      {
         Session jdbcSession = (Session) session;
         ResultSet resultSet = jdbcSession.executeQuery(query);
         try
         {
            while (resultSet.next())
            {
               long model = resultSet.getLong(1);
               long ref = resultSet.getLong(2);
               Set<Long> used = usage.get(model);
               if (used == null)
               {
                  used = new HashSet<Long>();
                  usage.put(model, used);
               }
               used.add(ref);
            }
         }
         catch (SQLException e)
         {
            trace.warn("Failed executing query.", e);
            throw new PublicException(e);
         }
         finally
         {
            QueryUtils.closeResultSet(resultSet);
         }
      }
      else
      {
         // debug session
         ResultIterator<ModelRefBean> resultIterator = session.getIterator(
               ModelRefBean.class, QueryExtension.where(typePredicate));
         try
         {
            while (resultIterator.hasNext())
            {
               ModelRefBean refBean = resultIterator.next();
               long model = refBean.getModelOid();
               long ref = refBean.getRefOid();
               Set<Long> used = usage.get(model);
               if (used == null)
               {
                  used = new HashSet<Long>();
                  usage.put(model, used);
               }
               used.add(ref);
            }
         }
         finally
         {
            resultIterator.close();
         }
      }
      return usage;
   }

   public static void setResolvedModel(IExternalPackage reference, IModel used, long deployment)
   {
      if (reference == null)
      {
         // self reference
         SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(new ModelRefBean(
               TYPE.USES, used.getModelOID(), used.getId(),
               used.getModelOID(), deployment));
      }
      else
      {
         // reference to another model
         SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(new ModelRefBean(
            TYPE.USES, reference.getModel().getModelOID(), reference.getHref(),
            used.getModelOID(), deployment));
      }
   }

   public static boolean providesUniquePrimaryImplementation(IModel model)
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ComparisonTerm typePredicate = Predicates.isEqual(FR__CODE, TYPE.IMPLEMENTS.ordinal());
      ComparisonTerm modelPredicate = Predicates.isEqual(FR__ID, model.getId());
      QueryDescriptor query = QueryDescriptor.from(ModelRefBean.class).select(
            FIELD__MODEL_OID, FIELD__REF_OID).where(
            Predicates.andTerm(typePredicate, modelPredicate));
      ResultSet resultSet = session.executeQuery(query);
      try
      {
         ModelManager manager = ModelManagerFactory.getCurrent();
         List<IModel> models = manager.getModelsForId(model.getId());
         // restrict to previous versions
         models = models.subList(models.indexOf(model) + 1, models.size());
         while (resultSet.next())
         {
            long mOid = resultSet.getLong(1);
            long pRtOid = resultSet.getLong(2);
            IProcessDefinition interfaceProcess = manager.findProcessDefinition(mOid, pRtOid);
            QName qname = new QName(interfaceProcess.getModel().getId(), interfaceProcess.getId());
            if (model.getImplementedInterfaces().contains(qname))
            {
               boolean hasPrevious = false;
               for (IModel iModel : models)
               {
                  if (iModel.getImplementedInterfaces().contains(qname))
                  {
                     hasPrevious = true;
                     break;
                  }
               }
               if (!hasPrevious)
               {
                  return true;
               }
            }
         }
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
      return false;
   }

   public static List<IProcessDefinition> getProcessInterfaces(IModel model)
   {
      List<IProcessDefinition> result = CollectionUtils.newList();
      ModelElementList list = model.getProcessDefinitions();
      for (int i = 0; i < list.size(); i++)
      {
         IProcessDefinition process = (IProcessDefinition) list.get(i);
         if (process.getDeclaresInterface())
         {
            result.add(process);
         }
      }
      return result;
   }

   public static void setPrimaryImplementation(IProcessDefinition process, String implementationId, long deployment)
   {
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(new ModelRefBean(
            TYPE.IMPLEMENTS, process.getModel().getModelOID(), implementationId,
            ModelManagerFactory.getCurrent().getRuntimeOid(process), deployment
      ));
   }

   public static List<IModel> getUsedModels(IModel model)
   {
      return CollectionUtils.newListFromIterator(
            new TransformingIterator<IExternalPackage, IModel>(model.getExternalPackages().iterator(),
                  new Functor<IExternalPackage, IModel>()
                  {
                     @Override
                     public IModel execute(IExternalPackage pkg)
                     {
                        return pkg.getReferencedModel();
                     }
                  }));
   }

   public static List<IModel> getUsingModels(final IModel model)
   {
      return CollectionUtils.newListFromIterator(
            new FilteringIterator<IModel>(ModelManagerFactory.getCurrent().getAllModels(),
                  new Predicate<IModel>()
                  {
                     @Override
                     public boolean accept(IModel other)
                     {
                        for (IExternalPackage pkg : other.getExternalPackages())
                        {
                           if (model == pkg.getReferencedModel())
                           {
                              return true;
                           }
                        }
                        return false;
                     }
                  }));
   }

   public long getModelOid()
   {
      return modelOid;
   }

   public long getRefOid()
   {
      return refOid;
   }

   public static IModel resolveModel(IExternalPackage reference) throws UnresolvedExternalReference
   {
      BpmRuntimeEnvironment env = PropertyLayerProviderInterceptor.getCurrent();
      if (env != null)
      {
         Map<String, IModel> overrides = env.getModelOverrides();
         if (overrides != null)
         {
            IModel model = overrides.get(reference.getHref());
            if (model != null)
            {
               return model;
            }
         }
      }
      try
      {
         //If resolution fails - try to resolve by querying the corresponding table entries (using Query Service)
         return resolveModel(reference.getModel().getModelOID(), reference.getHref());
      }
      catch (Throwable t)
      {
         return null;
      }
   }

   private static IModel resolveModel(long modelOid, String refId)
   {
      ModelManager manager = ModelManagerFactory.getCurrent();
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ComparisonTerm typePredicate = Predicates.isEqual(FR__CODE, TYPE.USES.ordinal());
      ComparisonTerm modelPredicate = Predicates.isEqual(FR__MODEL_OID, modelOid);
      ComparisonTerm refIdPredicate = Predicates.isEqual(FR__ID, refId);
      QueryDescriptor query = QueryDescriptor.from(ModelRefBean.class)
            .select(FIELD__REF_OID)
            .where(Predicates.andTerm(typePredicate, modelPredicate, refIdPredicate));
      ResultSet resultSet = session.executeQuery(query);
      try
      {
         if (resultSet.next())
         {
            return manager.findModel(resultSet.getLong(1));
         }
         else
         {
            return DEPLOY_AGAINST_ACTIVE_MODEL
               ? manager.findActiveModel(refId)
               : manager.findLastDeployedModel(refId);
         }
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   public static IProcessDefinition getPrimaryImplementation(IProcessDefinition process, IData data, String dataPath)
   {
      BpmRuntimeEnvironment env = PropertyLayerProviderInterceptor.getCurrent();
      if (env == null)
      {
         return process;
      }
      org.eclipse.stardust.engine.core.persistence.Session dbSession = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (!(dbSession instanceof Session))
      {
         return process;
      }

      IProcessInstance processInstance = null;
      long referenceTime = 0;
      long referenceDeployment = 0;
      IActivityInstance currentAi = env.getCurrentActivityInstance();
      if (currentAi == null)
      {
         referenceDeployment = -1;
         referenceTime = System.currentTimeMillis();
      }
      else
      {
         processInstance = currentAi.getProcessInstance();
         IProcessInstance rootProcessInstance = processInstance.getRootProcessInstance();
         referenceTime = rootProcessInstance.getStartTime().getTime();
         referenceDeployment = rootProcessInstance.getReferenceDeployment();
      }
      int interfaceOid = process.getModel().getModelOID();
      QName processQID = new QName(process.getModel().getId(), process.getId());

      String implementationId = null;
      // 1. get the implementation model id from the data value
      if (data != null && processInstance != null)
      {
         IProcessInstance scopeProcessInstance = processInstance.getScopeProcessInstance();
         implementationId = (String) scopeProcessInstance.getInDataValue(data, dataPath);
      }
      // 2. get the implementation model id from the primary implementation
      if (StringUtils.isEmpty(implementationId))
      {
         long runtimeProcessOid = ModelManagerFactory.getCurrent().getRuntimeOid(process);
         if (runtimeProcessOid == 0)
         {
            // element is not yet registered.
            return process;
         }
         implementationId = getPrimaryImplementationId(referenceDeployment, interfaceOid, runtimeProcessOid);
      }
      // 3. fallback to the default implementation
      if (StringUtils.isEmpty(implementationId))
      {
         implementationId = process.getModel().getId();
      }
      IProcessDefinition primaryImplementation = getPrimaryImplementation(process, referenceTime, referenceDeployment,
            interfaceOid, processQID, implementationId);
      if (primaryImplementation == null && data != null && processInstance != null)
      {
         throw new NonInteractiveApplicationException("Unable to find a valid implementation for '" + processQID + "'.",
               new UnresolvedExternalReference(implementationId));
      }
      return primaryImplementation == null ? process : primaryImplementation;
   }

   public static String getPrimaryImplementationId(long referenceDeployment, int interfaceOid, long runtimeProcessOid)
   {
      Session jdbcSession = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ComparisonTerm typePredicate = Predicates.isEqual(FR__CODE, TYPE.IMPLEMENTS.ordinal());
      ComparisonTerm modelPredicate = Predicates.isEqual(FR__MODEL_OID, interfaceOid);
      ComparisonTerm refIdPredicate = Predicates.isEqual(FR__REF_OID, runtimeProcessOid);
      ComparisonTerm deploymentPredicate = referenceDeployment < 0 ? null : Predicates.lessOrEqual(FR__DEPLOYMENT, referenceDeployment);
      QueryDescriptor query = QueryDescriptor.from(ModelRefBean.class)
            .select(FIELD__ID)
            .where(referenceDeployment < 0
                  ? Predicates.andTerm(typePredicate, modelPredicate, refIdPredicate)
                  : Predicates.andTerm(typePredicate, modelPredicate, refIdPredicate, deploymentPredicate));
      query.getQueryExtension().addOrderBy(FR__DEPLOYMENT, false);
      ResultSet resultSet = jdbcSession.executeQuery(query);
      try
      {
         if (resultSet.next())
         {
            return resultSet.getString(1);
         }
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
      return null;
   }

   private static IProcessDefinition getPrimaryImplementation(IProcessDefinition interfaceProcess, long referenceTimestamp, long referenceDeployment,
         long interfaceOid, QName processId, String implementationId)
   {
      QName qname = QName.valueOf(implementationId);
      String mId = qname.getNamespaceURI();
      String pId = null;
      if (StringUtils.isEmpty(mId))
      {
         mId = qname.getLocalPart();
      }
      else
      {
         pId = qname.getLocalPart();
      }

      boolean sameModel = mId.equals(processId.getNamespaceURI());
      String localPart = processId.getLocalPart();
      ModelManager manager = ModelManagerFactory.getCurrent();

      if (referenceDeployment < 0)
      {
         IProcessDefinition implementation = null;
         for (IModel candidate : getUsingModels((IModel) interfaceProcess.getModel()))
         {
            if (mId.equals(candidate.getId()))
            {
               Date from = (Date) candidate.getAttribute(PredefinedConstants.VALID_FROM_ATT);
               if (from == null || from.getTime() <= referenceTimestamp)
               {
                  IProcessDefinition process = findImplementation(candidate, processId, pId, localPart, sameModel);
                  if (process != null)
                  {
                     implementation = process;
                  }
               }
            }
         }
         return implementation;
      }

      ComparisonTerm typePredicate = Predicates.isEqual(FR__CODE, TYPE.USES.ordinal());
      ComparisonTerm modelPredicate = Predicates.isEqual(FR__REF_OID, interfaceOid);
      ComparisonTerm deploymentPredicate = Predicates.lessOrEqual(FR__DEPLOYMENT, referenceDeployment);
      ComparisonTerm validFromPredicate = Predicates.lessOrEqual(ModelDeploymentBean.FR__VALID_FROM, referenceTimestamp);
      QueryDescriptor query = QueryDescriptor.from(ModelRefBean.class)
            .select(FIELD__MODEL_OID)
            .where(Predicates.andTerm(typePredicate, modelPredicate, deploymentPredicate, validFromPredicate));
      query.getQueryExtension().addOrderBy(FR__DEPLOYMENT, false);
      query.innerJoin(ModelDeploymentBean.class).on(FR__DEPLOYMENT, ModelDeploymentBean.FIELD__OID);

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ResultSet resultSet = session.executeQuery(query);
      try
      {
         while (resultSet.next())
         {
            long candidateOid = resultSet.getLong(1);
            IModel candidate = manager.findModel(candidateOid);
            if (candidate != null && mId.equals(candidate.getId()))
            {
               IProcessDefinition process = findImplementation(candidate, processId, pId, localPart, sameModel);
               if (process != null)
               {
                  return process;
               }
            }
         }
         return null;
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   protected static IProcessDefinition findImplementation(IModel candidate,
         QName processId, String pId, String localPart, boolean sameModel)
   {
      IProcessDefinition process = null;
      if (sameModel)
      {
         process = candidate.findProcessDefinition(pId == null ? localPart : pId);
      }
      else
      {
         List<IProcessDefinition> impls = candidate.getAllImplementingProcesses(processId);
         if (impls != null && !impls.isEmpty())
         {
            if (pId == null)
            {
               process = impls.get(0);
            }
            else
            {
               for (IProcessDefinition impl : impls)
               {
                  if (pId.equals(impl.getId()))
                  {
                     process = impl;
                     break;
                  }
               }
            }
         }
      }
      return process;
   }

   public static void deleteForModel(long modelOid, Session session)
   {
      ComparisonTerm typePredicate = Predicates.isEqual(FR__CODE, TYPE.USES.ordinal());
      ComparisonTerm modelPredicate = Predicates.isEqual(FR__MODEL_OID, modelOid);
      ComparisonTerm refIdPredicate = Predicates.isEqual(FR__REF_OID, modelOid);
      QueryDescriptor query = QueryDescriptor.from(ModelRefBean.class)
            .select(FIELD__DEPLOYMENT)
            .where(Predicates.andTerm(typePredicate, modelPredicate, refIdPredicate));
      ResultSet resultSet = session.executeQuery(query);
      long deployment = -1;
      try
      {
         if (resultSet.next())
         {
            deployment = resultSet.getLong(1);
         }
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }

      session.delete(ModelRefBean.class, Predicates.andTerm(typePredicate, modelPredicate), false);

      if (deployment >= 0)
      {
         ComparisonTerm deploymentPredicate = Predicates.isEqual(FR__DEPLOYMENT, deployment);
         query = QueryDescriptor.from(ModelRefBean.class)
            .select(FIELD__DEPLOYMENT)
            .where(Predicates.andTerm(typePredicate, deploymentPredicate));
         resultSet = session.executeQuery(query);
         try
         {
            if (!resultSet.next())
            {
               // no more models from this deployment
               ModelDeploymentBean.delete(deployment, session);
            }
         }
         catch (SQLException e)
         {
            trace.warn("Failed executing query.", e);
            throw new PublicException(e);
         }
         finally
         {
            QueryUtils.closeResultSet(resultSet);
         }
      }
   }
}

