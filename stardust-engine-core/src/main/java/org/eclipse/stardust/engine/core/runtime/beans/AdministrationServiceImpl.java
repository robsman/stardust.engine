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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.*;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.*;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.*;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery.DeployedModelState;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.cache.CacheHelper;
import org.eclipse.stardust.engine.core.model.beans.DefaultXMLReader;
import org.eclipse.stardust.engine.core.model.beans.NullConfigurationVariablesProvider;
import org.eclipse.stardust.engine.core.model.parser.info.ExternalPackageInfo;
import org.eclipse.stardust.engine.core.model.parser.info.ModelInfo;
import org.eclipse.stardust.engine.core.model.parser.info.ModelInfoRetriever;
import org.eclipse.stardust.engine.core.model.utils.ModelUtils;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.preferences.*;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;
import org.eclipse.stardust.engine.core.preferences.permissions.PermissionUtils;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerBean.ModelManagerPartition;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.DaemonUtils;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.runtime.utils.AuthorizationContext;
import org.eclipse.stardust.engine.core.runtime.utils.ExecutionPermission;
import org.eclipse.stardust.engine.core.security.utils.SecurityUtils;

/**
 * This class implements <code>java.io.Serializable</code>, because it might be desirable
 * to achieve manual activation/passivation in local usage.
 *
 * @author mgille
 * @version $Revision$
 */
public class AdministrationServiceImpl
      implements Serializable, AdministrationService
{
   private static final String DEPLOY_MODEL_MESSAGE = "Deployed model ''{0}'' (oid: {1})";
   private static final String OVERWRITE_MODEL_MESSAGE = "Overwritten model ''{0}'' (oid: {1})";
   private static final String DUPLICATE_MODEL_ID = "Duplicate model id ''{0}''.";

   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(AdministrationServiceImpl.class);

   public void setPasswordRules(PasswordRules rules)
   {
      SecurityUtils.setPasswordRules(rules);
   }

   public PasswordRules getPasswordRules()
   {
      return SecurityUtils.getPasswordRules(SecurityProperties.getPartitionOid());
   }

   private void checkProductive(String operation)
   {
      if (isProductive())
      {
         throw new AccessForbiddenException(
               BpmRuntimeError.ATDB_INVALID_PRODUCTION_AUDIT_TRAIL_OPERATION.raise(operation));
      }
   }

   /**
    * @deprecated
    */
   public DeploymentInfo deployModel(String modelXml, String configuration,
         int predecessorOID, Date validFrom, Date validTo, String comment,
         boolean disabled, boolean ignoreWarnings)
         throws DeploymentException
   {
      DeploymentElement element = null;
      try
      {
         element = new DeploymentElement(encode(modelXml));
      }
      catch (Exception ex)
      {
         deploymentError(ex, null);
      }
      DeploymentOptions options = new DeploymentOptions();
      options.setComment(comment);
      options.setValidFrom(validFrom);
      options.setIgnoreWarnings(ignoreWarnings);
      return deployModel(Collections.singletonList(element), options).get(0);
   }

   /**
    * validFrom will be ignored for overwrite as specified in Surge FS, section "Overwriting Models".
    *
    * @deprecated
    */
   public DeploymentInfo overwriteModel(String modelXml, String configuration,
         int modelOID, Date validFrom, Date validTo, String comment,
         boolean disabled, boolean ignoreWarnings)
         throws DeploymentException
   {
      DeploymentElement element = null;
      try
      {
         element = new DeploymentElement(encode(modelXml));
      }
      catch (Exception ex)
      {
         deploymentError(ex, null);
      }
      DeploymentOptions options = new DeploymentOptions();
      options.setComment(comment);
      options.setIgnoreWarnings(ignoreWarnings);
      return overwriteModel(element, modelOID, options);
   }

   public DeploymentInfo overwriteModel(DeploymentElement deploymentElement,
         int modelOID, DeploymentOptions options) throws DeploymentException
   {
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      try
      {
         ModelManager manager = ModelManagerFactory.getCurrent();
         Map<String, IModel> overrides = CollectionUtils.newMap();
         List<IModel> lastDeployedModels = manager.findLastDeployedModels();
         for (IModel model : lastDeployedModels)
         {
            overrides.put(model.getId(), model);
         }
         runtimeEnvironment.setModelOverrides(overrides);

         ParsedDeploymentUnit element = null;
         try
         {
            element = new ParsedDeploymentUnit(deploymentElement, modelOID);
            IModel model = element.getModel();
            overrides.put(model.getId(), model);
         }
         catch (Exception ex)
         {
            deploymentError(ex, null);
         }
         return doOverwriteModel(element, options == null ? new DeploymentOptions() : options);
      }
      finally
      {
         runtimeEnvironment.setModelOverrides(null);
      }
   }

   private static byte[] encode(String content)
   {
      try
      {
         String encoding = Parameters.instance().getObject(
               PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
         return content.getBytes(encoding);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new PublicException(e);
      }
   }

   private DeploymentInfo deploymentError(Exception e, Date validFrom)
   {
      // @todo (france, ub): more gracefully fill the deployment info
      LogUtils.traceException(e, false);
      throw new DeploymentException(e.getMessage(),
            Collections.<DeploymentInfo>singletonList(new DeploymentInfoDetails(validFrom, null, null)));
   }

   private void reportDeploymentState(DeploymentInfo info)
   {
      if (info.hasErrors())
      {
         trace.error("Deployment errors:");
         for (Inconsistency inconsistency : info.getErrors())
         {
            trace.error("  " + inconsistency.getMessage()
                  + "; element oid = " + inconsistency.getSourceElementOID());
         }
      }
      if (info.hasWarnings())
      {
         trace.warn("Deployment warnings:");
         for (Inconsistency inconsistency : info.getWarnings())
         {
            trace.warn("  " + inconsistency.getMessage()
                  + "; element oid = " + inconsistency.getSourceElementOID());
         }
      }
      if (info.success())
      {
         trace.info("Model '" + info.getId() + "', model oid = " + info.getModelOID()
               +  " deployed.");
      }
      else
      {
         trace.error("Model '" + info.getId() + "', model oid = " + info.getModelOID()
               +  " not deployed.");

      }
   }

   private void deleteModelRuntimePart(long modelOid, boolean ignoreReferences)
   {
      // @todo (paris, ub): isolate every single delete operation?
      ModelManager modelManager = null;
      IModel model = null;
      try
      {
         modelManager = ModelManagerFactory.getCurrent();
         model = modelManager.findModel(modelOid);

         if (model == null)
         {
            throw new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_MODEL_OID
                  .raise(modelOid), modelOid);
         }
         if (!ignoreReferences && ModelRefBean.providesUniquePrimaryImplementation(model))
         {
            throw new PublicException(
                  "Unable to delete model. It is providing a primary implementation.");
   }
         if (!ignoreReferences)
         {
            List<IModel> referingModels = new ArrayList<IModel>();
            for (Iterator<IModel> i = modelManager.getAllModels(); i.hasNext();)
            {
               IModel usingModel = i.next();
               List<IModel> usedModels = ModelRefBean.getUsedModels(usingModel);
               for (Iterator<IModel> j = usedModels.iterator(); j.hasNext();)
               {
                  IModel usedModel = j.next();
                  if (model.getOID() != usingModel.getOID())
                  {
                     if (model.getOID() == usedModel.getOID())
                     {
                        referingModels.add(usingModel);
                     }
                  }
               }
            }
            if (!referingModels.isEmpty())
            {
               throw new PublicException(
                     "Unable to delete model. It is referenced by at least one other model.");
            }
         }
      }
      catch (Exception e)
      {
         deploymentError(e, null);
      }

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      AdminServiceUtils.deleteModelRuntimePart(modelOid, session);
   }

   private DeploymentInfo deleteModelModelingPart(long modelOid)
   {
      // @todo (paris, ub): isolate every single delete operation?
      ModelManager modelManager = null;
      IModel model = null;
      try
      {
         modelManager = ModelManagerFactory.getCurrent();
         model = modelManager.findModel(modelOid);

         if (model == null)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(modelOid), modelOid);
         }
      }
      catch (Exception e)
      {
         deploymentError(e, null);
      }

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      AdminServiceUtils.deleteModelModelingPart(modelOid, session);

      return ModelManagerFactory.getCurrent().deleteModel(model);
   }

   public DeploymentInfo deleteModel(long modelOid)
   {
      try
      {
         checkDaemonStopState(false);
         IModel model = ModelManagerFactory.getCurrent().findModel(modelOid);
         assertNotPredefinedModel(model);
      }
      catch (Exception e)
      {
         deploymentError(e, null);
      }

      try
      {

         deleteModelRuntimePart(modelOid, false);
         return deleteModelModelingPart(modelOid);
      }
      finally
      {
         flushCaches();
      }
   }

   public List<DeploymentInfo> deployModel(List<DeploymentElement> deploymentElements,
         DeploymentOptions options) throws DeploymentException, ConcurrencyException
   {
      IAuditTrailPartition partition = SecurityProperties.getPartition();
      if(partition != null)
      {
         AuditTrailPartitionBean foundByOID = AuditTrailPartitionBean.findByOID(partition.getOID());
         if(foundByOID != null)
         {
            foundByOID.lock();
         }
      }
      
      if(deploymentElements == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("deploymentElements"));
      }

      // use default options if no options are specified
      if (options == null)
      {
         options = DeploymentOptions.DEFAULT;
      }
      // parse the models and check for duplicates
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      List<ParsedDeploymentUnit> elements = CollectionUtils.newList(deploymentElements.size());
      try
      {
         // 1. Collect model references.
         Map<String, DeploymentElement> fileMap = CollectionUtils.newMap();
         Map<String, ModelInfo> infoMap = CollectionUtils.newMap();
         for (DeploymentElement deploymentElement : deploymentElements)
         {
            ModelInfo info = ModelInfoRetriever.get(deploymentElement.getContent());
            if (fileMap.containsKey(info.id))
            {
               throw new DeploymentException(null, DUPLICATE_MODEL_ID, info.id);
            }
            infoMap.put(info.id, info);
            fileMap.put(info.id, deploymentElement);
         }

         // 2. order models and prepare overrides
         ModelManager manager = ModelManagerFactory.getCurrent();
         Map<String, IModel> overrides = CollectionUtils.newMap();
         List<IModel> lastDeployedModels = manager.findLastDeployedModels();
         for (IModel model : lastDeployedModels)
         {
            overrides.put(model.getId(), model);
         }
         runtimeEnvironment.setModelOverrides(overrides);

         for (String modelId : orderModels(infoMap))
         {
            ParsedDeploymentUnit parsedUnit = new ParsedDeploymentUnit(fileMap.get(modelId), 0);
            elements.add(parsedUnit);
            overrides.put(modelId, parsedUnit.getModel());
         }
         
         
         
         // deploy the models
         return doDeployModel(elements, options);
      }
      catch (Exception e)
      {
         return Collections.singletonList(deploymentError(e, options.getValidFrom()));
      }
      finally
      {
         runtimeEnvironment.setModelOverrides(null);
      }
   }

   private List<String> orderModels(Map<String, ModelInfo> infos)
   {
      List<String> orderedModelIds = CollectionUtils.newList();
      Set<ModelInfo> visited = CollectionUtils.newSet();
      for (ModelInfo info : infos.values())
      {
         addModel(orderedModelIds, info, infos, visited);
      }
      return orderedModelIds;
   }

   private void addModel(List<String> orderedModelIds, ModelInfo info, Map<String, ModelInfo> infos, Set<ModelInfo> visited)
   {
      if (info != null && !visited.contains(info))
      {
         visited.add(info);
         if (info.externalPackages != null)
         {
            for (ExternalPackageInfo ref : info.externalPackages)
            {
               addModel(orderedModelIds, infos.get(ref.href), infos, visited);
            }
         }
         orderedModelIds.add(info.id);
      }
   }

   public DeploymentInfo setPrimaryImplementation(long interfaceOid, String processId,
         String implementationModelId, LinkingOptions options)
         throws DeploymentException
   {
      ModelManager manager = ModelManagerFactory.getCurrent();
      IModel interfaceModel = manager.findModel(interfaceOid);
      if (interfaceModel == null)
      {
         deploymentError(new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(interfaceOid)), null);
      }
      IProcessDefinition interfaceProcess = interfaceModel.findProcessDefinition(processId);
      if (interfaceProcess == null)
      {
         deploymentError(new ObjectNotFoundException(BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(processId)), null);
      }
      if (implementationModelId == null)
      {
         implementationModelId = interfaceModel.getId();
      }
      else if (!implementationModelId.equals(interfaceModel.getId()))
      {
         DeployedModelQuery query = DeployedModelQuery.findUsing(interfaceOid);
         query.getFilter().and(DeployedModelQuery.STATE.isEqual(DeployedModelState.VALID.name()));
         FilteringIterator<IModel> matchingModels = new FilteringIterator(
               manager.getAllModelsForId(implementationModelId),
               new ModelQueryEvaluator(query));
         if (!matchingModels.hasNext())
         {
            deploymentError(new ObjectNotFoundException(BpmRuntimeError.MDL_NO_MATCHING_MODEL_WITH_ID.raise(implementationModelId)), null);
         }
         IProcessDefinition implementationProcess = null;
         QName interfaceProcessQName = new QName(interfaceModel.getId(), interfaceProcess.getId());
         while (matchingModels.hasNext())
         {
            IModel implementationModel = matchingModels.next();
            implementationProcess = implementationModel.getImplementingProcess(interfaceProcessQName);
            if (implementationProcess != null)
            {
               break;
            }
         }
         if (implementationProcess == null)
         {
            deploymentError(new ObjectNotFoundException(BpmRuntimeError.MDL_NO_IMPLEMENTATION_PROCESS.raise(interfaceProcessQName, implementationModelId)), null);
         }
      }
      String comment = options == null ? null : options.getComment();
      ModelDeploymentBean deployment = new ModelDeploymentBean(comment);
      ModelRefBean.setPrimaryImplementation(interfaceProcess, implementationModelId, deployment.getOID());

      trace.info("Primary implementation for process '{" + interfaceModel.getId() + "}" + processId
            + " [modelOid: " + interfaceOid + "] set to '" + implementationModelId + "'.");
      return new DeploymentInfoDetails(
            (Date) interfaceModel.getAttribute(PredefinedConstants.VALID_FROM_ATT),
            interfaceModel.getId(), comment);
   }

   /**
    * @deprecated
    */
   public DeploymentInfo deployModel(String modelXml, int predecessorOID)
   {
      DeploymentElement element = null;
      try
      {
         element = new DeploymentElement(encode(modelXml));
      }
      catch (Exception ex)
      {
         deploymentError(ex, null);
      }
      return deployModel(Collections.singletonList(element), new DeploymentOptions()).get(0);
   }

   /**
    * @deprecated
    */
   public DeploymentInfo overwriteModel(String modelXml, int modelOID)
   {
      DeploymentElement element = null;
      try
      {
         element = new DeploymentElement(encode(modelXml));
      }
      catch (Exception ex)
      {
         deploymentError(ex, null);
      }
      return overwriteModel(element, modelOID, new DeploymentOptions());
   }

   /**
    * Sets a property of the runtime environment.
    */
   public void setProperty(String name, String value)
   {
      PropertyPersistor property = PropertyPersistor.findByName(name);

      if (property == null)
      {
         new PropertyPersistor(name, value);
      }
      else
      {
         property.setValue(value);
      }
   }

   /**
    * Gets the value of a property of the runtime environment.
    */
   public String getProperty(String name)
   {
      PropertyPersistor persistor = PropertyPersistor.findByName(name);

      if (persistor != null)
      {
         return persistor.getValue();
      }
      else
      {
         return null;
      }
   }

   /**
    * Marks that the runtime environment of this session is productive
    * and operations like audit trail or model cleanup may not be performed.
    */
   public void setProductive(boolean value)
   {
      if (value)
      {
         setProperty("Production.Mode", "Production");
      }
   }

   /**
    * Checks, wether the runtime environment of this session is productive and
    * operations like audit trail or model cleanup may not be performed.
    */
   public boolean isProductive()
   {
      return ("Production".equals(getProperty("Production.Mode")));
   }

   protected void checkDaemonStopState(boolean ignoreWarnings)
   {
      for (Iterator<IDaemon> i = DaemonFactory.instance().getAllDaemons(); i.hasNext();)
      {
         IDaemon daemon = i.next();
         Daemon daemonState = getDaemon(daemon.getType(), true);
         if (daemonState.isRunning())
         {
            throw new PublicException(
                  BpmRuntimeError.BPMRT_DAEMON_IS_RUNNING.raise(daemon.getType()));
         }
         if (!ignoreWarnings &&
               daemonState.getAcknowledgementState() != AcknowledgementState.RespondedOK)
         {
            throw new PublicException(
                  BpmRuntimeError.BPMRT_DAEMON_IS_NOT_RESPONDING.raise(daemon.getType()));
         }
      }
   }

   public AuditTrailHealthReport getAuditTrailHealthReport()
   {
      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      final String schemaPrefix = StringUtils.isEmpty(session.getSchemaName())
            ? ""
            : session.getSchemaName() + ".";

      long nPendingPiCompletes = 0;
      long nPendingPiAborts = 0;
      long nPendingAiAborts = 0;
      long nPisHavingCrashedAis = 0;
      long nPisHavingCrashedThreads = 0;
      long nPisHavingCrashedEventBindings = 0;

      Statement stmt = null;
      try
      {
         stmt = session.getConnection().createStatement();
         ResultSet rs = null;
         try
         {
            nPisHavingCrashedEventBindings = EventUtils.countDeactiveEventBindings();

            rs = stmt.executeQuery(
                  "SELECT COUNT(pi." + ProcessInstanceBean.FIELD__OID + ")"
                + "  FROM " + schemaPrefix + ProcessInstanceBean.TABLE_NAME + " pi"
                + " WHERE pi." + ProcessInstanceBean.FIELD__STATE + " NOT IN (" + ProcessInstanceState.COMPLETED + ", " + ProcessInstanceState.ABORTED + ", " + ProcessInstanceState.ABORTING + ")"
                + "   AND NOT EXISTS ("
                + "    SELECT tk." + TransitionTokenBean.FIELD__PROCESS_INSTANCE
                + "      FROM " + schemaPrefix + TransitionTokenBean.TABLE_NAME + " tk"
                + "     WHERE tk." + TransitionTokenBean.FIELD__PROCESS_INSTANCE + " = pi." + ProcessInstanceBean.FIELD__OID
                + "       AND tk." + TransitionTokenBean.FIELD__IS_CONSUMED + " = 0"
                + "    )");

            if (rs.next())
            {
               nPendingPiCompletes = rs.getLong(1);
            }
            QueryUtils.closeResultSet(rs);

            rs = stmt.executeQuery(
                  "SELECT COUNT(pi." + ProcessInstanceBean.FIELD__OID + ")"
                + "  FROM " + schemaPrefix + ProcessInstanceBean.TABLE_NAME + " pi"
                + " WHERE pi." + ProcessInstanceBean.FIELD__STATE + " = " + ProcessInstanceState.ABORTING);

            if (rs.next())
            {
               nPendingPiAborts = rs.getLong(1);
            }
            QueryUtils.closeResultSet(rs);

            rs = stmt.executeQuery(
                  "SELECT COUNT(ai." + ActivityInstanceBean.FIELD__OID + ")"
                  + " FROM " + schemaPrefix + ActivityInstanceBean.TABLE_NAME + " ai"
                  + " WHERE ai." + ActivityInstanceBean.FIELD__STATE + " = " + ActivityInstanceState.ABORTING);

            if (rs.next())
            {
               nPendingAiAborts = rs.getLong(1);
            }
            QueryUtils.closeResultSet(rs);

            rs = stmt.executeQuery(
                  "SELECT COUNT(pi." + ProcessInstanceBean.FIELD__OID + ")"
                  + "  FROM " + schemaPrefix + ProcessInstanceBean.TABLE_NAME + " pi"
                  + " WHERE pi." + ProcessInstanceBean.FIELD__STATE + " NOT IN (" + ProcessInstanceState.COMPLETED + ", " + ProcessInstanceState.ABORTED + ")"
                  + "   AND EXISTS ("
                  + "    SELECT tk." + TransitionTokenBean.FIELD__PROCESS_INSTANCE
                  + "      FROM " + schemaPrefix + TransitionTokenBean.TABLE_NAME + " tk,"
                  + "           " + schemaPrefix + ActivityInstanceBean.TABLE_NAME + " ai"
                  + "     WHERE tk." + TransitionTokenBean.FIELD__PROCESS_INSTANCE + " = pi." + ProcessInstanceBean.FIELD__OID
                  + "       AND tk." + TransitionTokenBean.FIELD__IS_CONSUMED + " = 0"
                  + "       AND tk." + TransitionTokenBean.FIELD__TARGET + " = ai." + ActivityInstanceBean.FIELD__OID
                  + "       AND ai." + ActivityInstanceBean.FIELD__STATE + " IN (" + ActivityInstanceState.CREATED + ", " + ActivityInstanceState.INTERRUPTED + ")"
                  + ")");
            if (rs.next())
            {
               nPisHavingCrashedAis = rs.getLong(1);
            }
            QueryUtils.closeResultSet(rs);

            nPisHavingCrashedThreads = 0;
            rs = stmt.executeQuery(
                  "SELECT pi." + ProcessInstanceBean.FIELD__OID + ", tk." + TransitionTokenBean.FIELD__TRANSITION + ", tk." + TransitionTokenBean.FIELD__MODEL
                  + "  FROM " + schemaPrefix + ProcessInstanceBean.TABLE_NAME + " pi, "+ schemaPrefix + TransitionTokenBean.TABLE_NAME + " tk"
                  + " WHERE pi." + ProcessInstanceBean.FIELD__STATE + " NOT IN (" + ProcessInstanceState.COMPLETED + ", " + ProcessInstanceState.ABORTED + ")"
                  + "   AND tk." + TransitionTokenBean.FIELD__PROCESS_INSTANCE + " = pi." + ProcessInstanceBean.FIELD__OID
                  + "   AND tk." + TransitionTokenBean.FIELD__TARGET + " = 0"
                  );
            HashMap<Long, Set<Long>> zeroTargetTokensPerProcessInstance = new HashMap<Long, Set<Long>>();
            HashMap<Long,Long> transitionOidToModel = new HashMap<Long,Long>();
            Set<Long> involvedTransitionOids = new HashSet<Long>();
            while (rs.next())
            {
              Long oid =  rs.getLong(1);
              Long transitionOid = rs.getLong(2);
              Long modelOid = rs.getLong(3);

              involvedTransitionOids.add(transitionOid);
              transitionOidToModel.put(transitionOid, modelOid);

              Set<Long> transitionOids = zeroTargetTokensPerProcessInstance.get(oid);
               if (transitionOids == null)
               {
                  transitionOids = new HashSet<Long>();
               }
               transitionOids.add(transitionOid);
               zeroTargetTokensPerProcessInstance.put(oid, transitionOids);
            }
            QueryUtils.closeResultSet(rs);

            // Retrieve targetActivityOid from transitionOid.
            Map<Long, Long> targetActivityOidPerTransitionOid = new HashMap<Long,Long>();
            for (Long transitionOid : involvedTransitionOids)
            {
               AuditTrailTransitionBean transition = AuditTrailTransitionBean.findByOid(
                     transitionOid, transitionOidToModel.get(transitionOid));
               if (transition != null)
               {
                  targetActivityOidPerTransitionOid.put(transitionOid,
                        transition.getTargetActivity());
               }
            }

            // Retrieve count of all source activities of a target activity.
            Map<Long, Long> countOfTransitionSourcesPerTargetActivity = new HashMap<Long,Long>();
            for (Long targetActivityOid : targetActivityOidPerTransitionOid.values())
            {
            rs = stmt.executeQuery(
                  "SELECT COUNT(tr." + AuditTrailTransitionBean.FIELD__OID + ")"
                  + "  FROM " + schemaPrefix + AuditTrailTransitionBean.TABLE_NAME + " tr"
                  + " WHERE tr." + AuditTrailTransitionBean.FIELD__TGT_ACTIVITY + " = " + targetActivityOid
                  );

            if (rs.next())
               {
                  long transitionCount = rs.getLong(1);

                  countOfTransitionSourcesPerTargetActivity.put(targetActivityOid,
                        transitionCount);
               }
            }
            QueryUtils.closeResultSet(rs);

            // Iterate over all existing trans_token entries having '0' targetActivityInstance to see if ALL source activities are contained.
            Set<Long> crashedPIs = new HashSet<Long>();
            for (Map.Entry<Long, Set<Long>> entry : zeroTargetTokensPerProcessInstance.entrySet())
            {
               Set<Long> transitionOids = entry.getValue();

               Map<Long, Long> countOfInvolvedActivityOids = new HashMap<Long,Long>();
               for (Long transitionOid : transitionOids)
               {
                  Long activityOid = targetActivityOidPerTransitionOid.get(transitionOid);
                  Long count = countOfInvolvedActivityOids.get(activityOid);
                  if (count == null)
                  {
                     count = Long.valueOf(0);
                  }
                  count++;
                  countOfInvolvedActivityOids.put(activityOid, count);
               }

               for (Map.Entry<Long,Long> entry2 : countOfInvolvedActivityOids.entrySet())
               {
                  // Only crashed candidate if trans_token targetActivityInstanceOids of ALL source activities are '0'
                  if (countOfTransitionSourcesPerTargetActivity.get(entry2.getKey()) == entry2.getValue())
                  {
                     crashedPIs.add(entry.getKey());
                  }
               }
            }
            nPisHavingCrashedThreads = crashedPIs.size();
         }
         finally
         {
            QueryUtils.closeResultSet(rs);
         }
      }
      catch (SQLException e)
      {
         throw new PublicException("Failed evaluating recovery status.", e);
      }
      finally
      {
         QueryUtils.closeStatement(stmt);
      }

      return new AuditTrailHealthReport(nPendingPiCompletes, nPendingPiAborts,
            nPendingAiAborts, nPisHavingCrashedAis, nPisHavingCrashedThreads,
            nPisHavingCrashedEventBindings);
   }

   public void recoverRuntimeEnvironment()
   {
      AuditTrailLogger.getInstance(LogCode.RECOVERY).info("Recovery Started.");

      QueryExtension extension = QueryExtension.where( //
            Predicates.inList(ProcessInstanceBean.FR__STATE, new int[] {
                  ProcessInstanceState.ACTIVE, //
                  ProcessInstanceState.INTERRUPTED, //
                  ProcessInstanceState.ABORTING }));

      extension.addJoin(new Join(ModelPersistorBean.class) //
            .on(ProcessInstanceBean.FR__MODEL, ModelPersistorBean.FIELD__OID) //
            .andWhere(
                  Predicates.isEqual(ModelPersistorBean.FR__PARTITION, SecurityProperties
                        .getPartitionOid())));

      List processes = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getVector(
            ProcessInstanceBean.class, extension);

      // TODO consider using a certain maximum batch size
      for (Iterator i = processes.iterator(); i.hasNext();)
      {
         recoverProcessInstance((IProcessInstance) i.next());
      }

      AuditTrailLogger.getInstance(LogCode.RECOVERY).info("Recovery Completed.");
   }

   public ProcessInstance recoverProcessInstance(long oid) throws ObjectNotFoundException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(oid);
      List<IProcessInstance> children = ProcessInstanceHierarchyBean.findChildren(processInstance);

      recoverProcessInstance(processInstance);
      for (IProcessInstance childProcessInstance : children)
      {
         recoverProcessInstance(childProcessInstance);
      }

      return DetailsFactory.create(processInstance);
   }

   public void recoverProcessInstances(List<Long> oids) throws ObjectNotFoundException
   {
      Map<Long, IProcessInstance> toRecover = new LinkedHashMap<Long, IProcessInstance>();
      for (Iterator<Long> i = oids.iterator(); i.hasNext();)
      {
         Number oid = (Number) i.next();

         IProcessInstance processInstance = ProcessInstanceBean.findByOID(oid.longValue());

         toRecover.put(processInstance.getOID(), processInstance);

         List<IProcessInstance> children = ProcessInstanceHierarchyBean.findChildren(processInstance);
         for (IProcessInstance childProcessInstance : children)
         {
            if ( !toRecover.containsKey(childProcessInstance.getOID()))
            {
               toRecover.put(childProcessInstance.getOID(), childProcessInstance);
            }
         }
      }
      for (IProcessInstance processInstance : toRecover.values())
      {
         recoverProcessInstance(processInstance);
      }
   }

   private void recoverProcessInstance(IProcessInstance processInstance)
   {
      final ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance()
            .get(EngineProperties.FORKING_SERVICE_HOME);

      final long piOid = processInstance.getOID();
      ForkingService service = null;

      try
      {
         if (processInstance.isAborting())
         {
            service = factory.get();

            // first try is synchronous.
            service
                  .isolate(new ProcessAbortionJanitor(new AbortionJanitorCarrier(piOid)));
         }
         else if ( !processInstance.isCompleted())
         {
            service = factory.get();

            Boolean spawned = (Boolean) service
                  .isolate(new ActivityThreadsRecoveryAction(piOid));

            if ( !spawned.booleanValue())
            {
               service.isolate(new JanitorCarrier(piOid).createAction());
            }
         }
      }
      finally
      {
         if (null != service)
         {
            factory.release(service);
         }
      }
   }

   public void deleteProcesses(List<Long> piOids) throws PublicException
   {
      if(piOids.isEmpty())
      {
         return;
      }

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      // check if PIs are strictly terminated root PIs
      QueryDescriptor qFindRootPis = QueryDescriptor
         .from(ProcessInstanceBean.class)
         .select(new String[] {
               ProcessInstanceBean.FIELD__OID,
               ProcessInstanceBean.FIELD__MODEL,
               ProcessInstanceBean.FIELD__PROCESS_DEFINITION,
               ProcessInstanceBean.FIELD__ROOT_PROCESS_INSTANCE,
               ProcessInstanceBean.FIELD__STATE})
         .where(Predicates.inList(ProcessInstanceBean.FR__OID, piOids));
      Set nonrootPiOids = new TreeSet();
      Set nonterminatedPiOids = new TreeSet();
      List<Long> models = new ArrayList<Long>(piOids.size());
      List<Long> definitions = new ArrayList<Long>(piOids.size());
      ResultSet rsCheckPreconditions = session.executeQuery(qFindRootPis, -1);
      try
      {
         while (rsCheckPreconditions.next())
         {
            long piOid = rsCheckPreconditions.getLong(1);
            models.add(new Long(rsCheckPreconditions.getLong(2)));
            definitions.add(new Long(rsCheckPreconditions.getLong(3)));
            long rootPiOid = rsCheckPreconditions.getLong(4);
            int piState = rsCheckPreconditions.getInt(5);
            if (piOid != rootPiOid)
            {
               nonrootPiOids.add(new Long(piOid));
            }
            if ((ProcessInstanceState.COMPLETED != piState)
                  && (ProcessInstanceState.ABORTED != piState))
            {
               nonterminatedPiOids.add(new Long(piOid));
            }
         }
      }
      catch (SQLException sqle)
      {
         throw new PublicException("Failed verifying preconditions.", sqle);
      }
      finally
      {
         QueryUtils.closeResultSet(rsCheckPreconditions);
      }

      if ( !nonrootPiOids.isEmpty())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.ATDB_ARCHIVE_UNABLE_TO_DELETE_NON_ROOT_PI
                     .raise(nonrootPiOids));
      }
      if ( !nonterminatedPiOids.isEmpty())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.ATDB_ARCHIVE_UNABLE_TO_DELETE_NON_TERMINATED_PI
                     .raise(nonterminatedPiOids));
      }

      if (Parameters.instance().getBoolean("AdministrationService.Guarded", true))
      {
         // after that part, the models and definitions may be out of sync with the piOids
         AuthorizationContext ctx = AuthorizationContext.create(AdministrationService.class, "deleteProcesses", List.class);
         if (!ctx.isAdminOverride())
         {
            ModelManager modelManager = ModelManagerFactory.getCurrent();
            for (int i = piOids.size() - 1; i >= 0; i--)
            {
               long model = models.get(i);
               long definition = definitions.get(i);
               IProcessDefinition pd = modelManager.findProcessDefinition(model, definition);
               if (pd != null)
               {
                  ctx.setModelElementData(pd);
                  if (!Authorization2.hasPermission(ctx))
                  {
                     piOids.remove(i);
                  }
               }
            }
         }
      }

      // resolve full PI closure of root PIs
      QueryDescriptor qPiClosure = QueryDescriptor
         .from(ProcessInstanceBean.class)
         .select(ProcessInstanceBean.FR__OID)
         .where(Predicates.inList(ProcessInstanceBean.FR__ROOT_PROCESS_INSTANCE, piOids));

      Set resolvedPiOids = new TreeSet();
      ResultSet rsPiOids = session.executeQuery(qPiClosure, -1);
      try
      {
         while (rsPiOids.next())
         {
            resolvedPiOids.add(new Long(rsPiOids.getLong(1)));
         }
      }
      catch (SQLException sqle)
      {
         throw new PublicException("Failed resolving process instance closure.", sqle);
      }
      finally
      {
         QueryUtils.closeResultSet(rsPiOids);
      }

      ProcessInstanceUtils.deleteProcessInstances(new ArrayList(resolvedPiOids), session);
   }

   /**
    * Removes all CARNOT-specific tables from the audit trail database.
    * All audit trail information is lost after calling this method.
    *
    * @param keepUsers
    */
   public void cleanupRuntime(boolean keepUsers)
   {
      checkProductive("Cleanup Audit Trail Information");
      checkDaemonStopState(false);

      try
      {
         cleanupRuntime(keepUsers, true);
      }
      finally
      {
         flushCaches();
      }
   }

   private void cleanupRuntime(boolean keepUsers, boolean keepLoginUser)
   {
      ModelManager manager = ModelManagerFactory.getCurrent();
      if (null == manager)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_MODEL_MANAGER_UNAVAILABLE.raise());
      }

      // delete runtime data for all models contained in the current partition
      for (Iterator iter = manager.getAllModels(); iter.hasNext();)
      {
         IModel model = (IModel) iter.next();

         long modelElementOid = model.getModelOID();
         deleteModelRuntimePart(modelElementOid, true);
      }

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      long userOID = SecurityProperties.getUserOID();
      long partitionOid = SecurityProperties.getPartitionOid();

      AdminServiceUtils.deleteModelIndependentRuntimeData(keepUsers, keepLoginUser,
            session, userOID, partitionOid);

      String storeLocation = Parameters.instance().getString(
            PreferenceStorageManager.PRP_PREFERENCES_STORE,
            PreferenceStorageManager.PREFERENCES_STORE_AUDIT_TRAIL);
      if (storeLocation.equals(PreferenceStorageManager.PREFERENCES_STORE_DMS))
      {
         ServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();

         PreferenceStoreUtils.cleanupAllPreferencesFromDms(userOID, keepLoginUser, sf);
      }
   }

   /**
    * Removes all runtime environment and modeling tables from the
    * audit trail database.
    * <p/>
    * All audit trail and (deployed) model information is lost after calling this method.
    * It is also recommended that you disconnect
    * the client after invocation.
    */
   public void cleanupRuntimeAndModels()
   {
      checkProductive("Cleanup Entire Runtime Environment");
      checkDaemonStopState(false);

      try
      {
         // Cleanup all runtime tables including the login user

         ModelManager manager = ModelManagerFactory.getCurrent();
         if (null == manager)
         {
            throw new ObjectNotFoundException(
                  BpmRuntimeError.MDL_MODEL_MANAGER_UNAVAILABLE.raise());
         }

         cleanupRuntime(false, false);

         Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

         cleanupDeployments(session);
         cleanupModelReferences(session);

         // delete all models contained in the current partition
         Iterator iter = manager.getAllModels();
         while (iter.hasNext())
         {
            IModel model = (IModel) iter.next();

            long modelElementOid = model.getModelOID();
            deleteModelModelingPart(modelElementOid);

            AdminServiceUtils.deletePartitionPreferences(SecurityProperties.getPartitionOid(), session);


            // In order to prevent an {@link ConcurrentModificationException} the
            // iterator has to be reinitialized after deleting a model.
            iter = manager.getAllModels();
         }

         SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

         // On internal authentication motu has to be preserved
         // It is just reset to the initial state.
         if (SecurityProperties.isInternalAuthentication())
         {
            UserRealmBean carnotRealm = new UserRealmBean(
                  PredefinedConstants.DEFAULT_REALM_ID,
                  PredefinedConstants.DEFAULT_REALM_NAME,
                  (AuditTrailPartitionBean) SecurityProperties.getPartition(false));
            IUser motu = new UserBean(PredefinedConstants.MOTU,
                  PredefinedConstants.MOTU_FIRST_NAME, PredefinedConstants.MOTU_LAST_NAME,
                  carnotRealm);
            motu.setPassword(motu.getId());
         }

         ModelManagerFactory.setDirty();

         trace.info("Entire Runtime and Modeling Environment cleaned up.");
      }
      finally
      {
         flushCaches();
      }
   }

   private void cleanupDeployments(Session session)
   {
      DeleteDescriptor delete = DeleteDescriptor.from(ModelDeploymentBean.class);

      QueryExtension queryExtension = delete.getQueryExtension();
      Join modelRefJoin = new Join(ModelRefBean.class)
         .on(ModelDeploymentBean.FR__OID, ModelRefBean.FIELD__DEPLOYMENT);
      queryExtension.addJoin(modelRefJoin);

      Join modelsJoin = new Join(ModelPersistorBean.class)
         .on(modelRefJoin.fieldRef(ModelRefBean.FIELD__MODEL_OID), ModelPersistorBean.FIELD__OID);
      queryExtension.addJoin(modelsJoin);

      PredicateTerm deletePredicate = Predicates.isEqual(
            modelsJoin.fieldRef(ModelPersistorBean.FIELD__PARTITION),
            SecurityProperties.getPartitionOid());
      session.executeDelete(delete.where(deletePredicate));
   }

   private void cleanupModelReferences(Session session)
   {
      DeleteDescriptor delete = DeleteDescriptor.from(ModelRefBean.class);

      QueryExtension queryExtension = delete.getQueryExtension();
      Join modelsJoin = new Join(ModelPersistorBean.class)
         .on(ModelRefBean.FR__MODEL_OID, ModelPersistorBean.FIELD__OID);
      queryExtension.addJoin(modelsJoin);

      PredicateTerm deletePredicate = Predicates.isEqual(
            modelsJoin.fieldRef(ModelPersistorBean.FIELD__PARTITION),
            SecurityProperties.getPartitionOid());
      session.executeDelete(delete.where(deletePredicate));
   }

   /**
    * Changes the process instance priority.
    * Equivalent with setProcessInstancePriority(oid, priority, false).
    *
    * @param oid the OID of the process instance to be aborted.
    * @param priority the new priority of the process instance.
    *
    * @return the process instance that was changed.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified oid.
    */
   public ProcessInstance setProcessInstancePriority(long oid, int priority)
      throws ObjectNotFoundException
   {
         return setProcessInstancePriority(oid, priority, false);
   }

   /**
    * Changes the process instance priority.
    *
    * @param oid the OID of the process instance to be aborted.
    * @param priority the new priority of the process instance.
    * @param propagateToSubProcesses if true, the priority will be propagated to all subprocesses.
    *
    * @return the process instance that was changed.
    *
    * @throws ObjectNotFoundException if there is no process instance with the specified oid.
    */
   public ProcessInstance setProcessInstancePriority(long oid, int priority,
         boolean propagateToSubProcesses) throws ObjectNotFoundException
   {
      IProcessInstance target = null;

      ProcessInstanceQuery query = new ProcessInstanceQuery();
      query.getFilter().add(new ProcessInstanceFilter(oid, propagateToSubProcesses));

      ResultIterator rawResult = new ProcessInstanceQueryEvaluator(query,
            QueryServiceUtils.getDefaultEvaluationContext()).executeFetch();
      try
      {
         RawQueryResult queryResult = ProcessQueryPostprocessor.findMatchingProcessInstances(query, rawResult);
         for (Iterator i = queryResult.iterator(); i.hasNext();)
         {
            IProcessInstance processInstance = (IProcessInstance) i.next();
            processInstance.lock();
            processInstance.setPriority(priority);
            if (processInstance.getOID() == oid)
            {
               target = processInstance;
            }
         }
      }
      finally
      {
         rawResult.close();
      }
      if (target == null)
      {
         target = ProcessInstanceBean.findByOID(oid);
         target.lock();
         target.setPriority(priority);
      }

      return DetailsFactory.create(target);
   }

   /**
    * Terminates a process independently of all activities being performed or
    * having been performed.
    */
   public ProcessInstance abortProcessInstance(long oid)
         throws ObjectNotFoundException, IllegalOperationException
   {
      IProcessInstance processInstance = ProcessInstanceBean.findByOID(oid);

      if (processInstance.isCaseProcessInstance())
      {
         throw new IllegalOperationException(BpmRuntimeError.BPMRT_PI_IS_CASE.raise(oid));
      }

      IProcessInstance rootProcessInstance = ProcessInstanceUtils.getActualRootPI(processInstance);

      if (rootProcessInstance != processInstance)
      {
         trace.info("Aborting subprocess, starting from root process instance "
               + rootProcessInstance + ".");
      }
      else
      {
         trace.info("Aborting process instance " + rootProcessInstance + ".");
      }

      if (!rootProcessInstance.isTerminated() && !rootProcessInstance.isAborting())
      {
         ProcessInstanceUtils.abortProcessInstance(rootProcessInstance);
      }
      else
      {
         if(rootProcessInstance.isTerminated())
         {
            trace.info("Skipping abort of already terminated process instance "
               + rootProcessInstance + ".");
         }
         else
         {
            trace.info("Skipping abort of already aborting process instance "
                  + rootProcessInstance + ".");
         }
      }

      return DetailsFactory.create(processInstance);
   }

   public ProcessInstance startProcess(long modelOID, String id, Map<String, ? > data, boolean synchronously)
   {
      IModel model = ModelManagerFactory.getCurrent().findModel(modelOID);
      if (model == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_MODEL_OID.raise(modelOID), modelOID);
      }

      ModelManagerFactory.getCurrent().reanimate(model);
      IProcessDefinition processDefinition = model.findProcessDefinition(id);

      if (processDefinition == null)
      {
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_PROCESS_DEFINITION_ID.raise(id));
      }

      IProcessInstance processInstance = ProcessInstanceBean.createInstance(processDefinition,
            SecurityProperties.getUser(), data);

      trace.info("Starting process '" + processDefinition.getId() + ", model version = '"
            + modelOID + "', oid = " + processInstance.getOID() + ", synchronous = " + synchronously);

      IActivity rootActivity = processDefinition.getRootActivity();

      ActivityThread.schedule(processInstance, rootActivity, null,
            synchronously, null, Collections.EMPTY_MAP, false);

      return DetailsFactory.create(processInstance);
   }

   public List<Daemon> getAllDaemons(boolean acknowledge)
   {
      List daemons = new ArrayList();

      for (Iterator i = DaemonFactory.instance().getAllDaemons(); i.hasNext();)
      {
         IDaemon daemon = (IDaemon) i.next();
         daemons.add(getDaemon(daemon.getType(), acknowledge));
      }
      return daemons;
   }

   public Daemon startDaemon(String type, boolean acknowledge)
   {
      return DaemonUtils.startDaemon(type, acknowledge);
   }

   /**
    * Stops the daemon asynchronously.
    * <b>Must</b> idempotent if the daemon is not running.
    */
   public Daemon stopDaemon(String type, boolean acknowledge)
   {
      return DaemonUtils.stopDaemon(type, acknowledge);
   }

   public Daemon getDaemon(String type, boolean acknowledge)
   {
      return DaemonUtils.getDaemon(type, acknowledge);
   }

   public ActivityInstance forceCompletion(long activityInstanceOID, Map<String, ? > accessPoints)
      throws ObjectNotFoundException, IllegalStateChangeException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);
      if (activityInstance.getActivity().isInteractive())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_INTERACTIVE_AI_CAN_NOT_BE_FORCED_TO_COMPLETION
                     .raise(activityInstanceOID));
      }

      if (!activityInstance.isTerminated() && !activityInstance.isAborting())
      {
         activityInstance.lock();
         try
         {
            ((IdentifiablePersistentBean) activityInstance).reloadAttribute("state");
         }
         catch (PhantomException e)
         {
            throw new PublicException("Activity instance was deleted", e);
         }

         if (!activityInstance.isTerminated() && !activityInstance.isAborting())
         {
            activityInstance.activate();
            AuditTrailLogger.getInstance(LogCode.RECOVERY, activityInstance).
                  info("Forced completion of activity instance");
            ActivityThread.schedule(null, null, activityInstance,
                  true, null, accessPoints, false);
            return (ActivityInstance) DetailsFactory.create(
                  activityInstance, IActivityInstance.class, ActivityInstanceDetails.class);
         }
         else
         {
            throw new IllegalStateChangeException(activityInstance.toString(), ActivityInstanceState.Completed, activityInstance.getState());
         }
      }
      else
      {
         throw new IllegalStateChangeException(activityInstance.toString(), ActivityInstanceState.Completed, activityInstance.getState());
      }
   }

   public ActivityInstance forceSuspendToDefaultPerformer(long activityInstanceOID)
         throws ObjectNotFoundException, ConcurrencyException, IllegalStateChangeException
   {
      IActivityInstance activityInstance = ActivityInstanceBean.findByOID(activityInstanceOID);
      if (!activityInstance.isTerminated() && !activityInstance.isAborting())
      {
         activityInstance.lock();
         try
         {
            ((IdentifiablePersistentBean) activityInstance).reloadAttribute("state");
         }
         catch (PhantomException e)
         {
            throw new PublicException("Activity instance was deleted", e);
         }
         if (!activityInstance.isTerminated() && !activityInstance.isAborting())
         {
            activityInstance.suspend();
            AuditTrailLogger.getInstance(LogCode.RECOVERY, activityInstance).info(
                  "Forced suspend of activity instance");
            activityInstance.delegateToDefaultPerformer();
         }
         else
         {
            throw new IllegalStateChangeException(activityInstance.toString(),
                  ActivityInstanceState.Suspended, activityInstance.getState());
         }
      }
      else
      {
         throw new IllegalStateChangeException(activityInstance.toString(),
               ActivityInstanceState.Suspended, activityInstance.getState());
      }

      return (ActivityInstance) DetailsFactory.create(
            activityInstance, IActivityInstance.class, ActivityInstanceDetails.class);
   }

   public User getUser()
   {
      return (User) DetailsFactory.create(SecurityProperties.getUser(), IUser.class,
            UserDetails.class);
   }

   private void checkInternalAuthentified()
   {
      if ( !isInternalAuthentication())
      {
         throw new IllegalOperationException(
               BpmRuntimeError.AUTHx_OPERATION_FAILED_REQUIRES_INTERNAL_AUTH.raise());
      }
   }

   public boolean isInternalAuthentication()
   {
      return SecurityProperties.isInternalAuthentication();
   }

   public void flushCaches()
   {
      SynchronizationService.flush();
      Parameters.instance().flush();
      CacheHelper.flushCaches();
      getPreferenceStore().flushCaches();
      reloadModelManagerAfterModelOperation();
   }

   public List<Permission> getPermissions()
   {
      return CollectionUtils.union(
            Authorization2.getPermissions(AdministrationService.class), 
            Authorization2.getPermissions(GlobalPermissionSpecificService.class));
   }

   public Map<String, ? > getProfile(ProfileScope scope)
   {
      if (ProfileScope.UserScope == scope)
      {
         final IUser currentUser = SecurityProperties.getUser();
         Map<String, Object> result = CollectionUtils.newHashMap();

         Map profile = currentUser.getProfile();
         for (Iterator iterator = profile.values().iterator(); iterator.hasNext();)
         {
            UserProperty entry = (UserProperty) iterator.next();
            result.put(entry.getName(), entry.getValue());
         }

         return result;
      }

      return Collections.emptyMap();
   }

   public void setProfile(ProfileScope scope, Map<String, ? > rawProfile)
   {
      final IUser currentUser = SecurityProperties.getUser();

      if (ProfileScope.UserScope == scope)
      {
         Map<String, Object> newProfile = CollectionUtils.newHashMap();

         // delete no longer existing profile properties
         Map currentProfile = currentUser.getProfile();
         for (Iterator iterator = currentProfile.keySet().iterator(); iterator.hasNext();)
         {
            String name = (String) iterator.next();
            if(!rawProfile.containsKey(name))
            {
               ((AbstractProperty)currentProfile.get(name)).delete();
            }
         }

         // add or update other profile attributes
         for (Iterator iterator = rawProfile.entrySet().iterator(); iterator.hasNext();)
         {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            Serializable value = (Serializable) entry.getValue();

            UserProperty profileProperty = (UserProperty) currentProfile.get(key);
            if (null == profileProperty)
            {
               profileProperty = (UserProperty) currentUser.createProperty(key,
                     value);
               profileProperty.setScope(UserProperty.PROFILE_SCOPE);
            }
            else
            {
               profileProperty.setValue(value);
            }

            newProfile.put(profileProperty.getName(), profileProperty);
         }

         currentUser.setProfile(newProfile);
      }
      else
      {
         throw new PublicException(MessageFormat.format(
               "User {0} is not allowed to change profile for scope {1}.", new Object[] {
                     currentUser, scope }));
      }
   }

   /**
    * Deploys a new model to the audit trail.
    *
    * @param model The in memory representation of the model.
    * @param carnotModelXml The string representation of the model, assumed to be in CWM,
    *       format encoded in ISO-8859-1.
    * @param configuration
    * @param predecessorOID
    * @param validFrom
    * @param validTo
    * @param comment
    * @param disabled
    * @param ignoreWarnings
    * @return
    * @throws DeploymentException
    */
   private List<DeploymentInfo> doDeployModel(List<ParsedDeploymentUnit> elements, DeploymentOptions options)
         throws DeploymentException
   {
      try
      {
         checkDaemonStopState(options.isIgnoreWarnings());
         for (ParsedDeploymentUnit parsedDeploymentUnit : elements)
         {
            assertNotPredefinedModel(parsedDeploymentUnit.getModel());
         }
      }
      catch (Exception e)
      {
         deploymentError(e, options.getValidFrom());
      }

      try
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();

         IModel predefinedModel = modelManager.findActiveModel(PredefinedConstants.PREDEFINED_MODEL_ID);
         if (predefinedModel == null)
         {
            List<ParsedDeploymentUnit> predefinedModelElement = ModelUtils.getPredefinedModelElement();
            if (predefinedModelElement != null)
            {
               modelManager.deployModel(predefinedModelElement, DeploymentOptions.DEFAULT);
            }
            else
            {
               trace.warn("Could not load PredefinedModel.xpdl");
            }
         }

         List<DeploymentInfo> infos = modelManager.deployModel(elements, options);
         boolean success = true;
         for (DeploymentInfo info : infos)
         {
            success = success && info.success();
         }
         if (success)
         {
            for (ParsedDeploymentUnit unit : elements)
            {
               try
               {
                  DocumentTypeUtils.synchronizeDocumentTypeSchema(unit.getModel());
               }
               catch (InternalException e)
               {
                  throw new PublicException(BpmRuntimeError.DMS_DOCUMENT_TYPE_DEPLOY_ERROR.raise(), e.getCause());
               }
            }
         }
         for (DeploymentInfo info : infos)
         {
            reportDeploymentState(info);
         }
         if (!success)
         {
            throw new DeploymentException(getMessage(infos), infos);
         }
         for (ParsedDeploymentUnit unit : elements)
         {
            IModel model = unit.getModel();
            AuditTrailLogger.getInstance(LogCode.ENGINE).info(MessageFormat.format(DEPLOY_MODEL_MESSAGE,
                  model.getName(), model.getModelOID()));
         }
         return infos;
      }
      finally
      {
         reloadModelManagerAfterModelOperation();
      }
   }

   private String getMessage(List<DeploymentInfo> infos)
   {
      for (DeploymentInfo info : infos)
      {
         if (!info.success())
         {
            if (info.hasErrors())
            {
               return getMessage(info, info.getErrors().get(0));
            }
            if (info.hasWarnings())
            {
               return getMessage(info, info.getWarnings().get(0));
            }
         }
      }
      return "Deployment error.";
   }

   private String getMessage(DeploymentInfo info, Inconsistency inconsistency)
   {
      return inconsistency.getMessage() + " ; model: '" + info.getId() + "'; element oid: " + inconsistency.getSourceElementOID();
   }

   /**
    * Overwrites an existing model in the audit trail.
    *
    * @param model The in memory representation of the model.
    * @param carnotModelXml The string representation of the model, assumed to be in CWM,
    *       format encoded in ISO-8859-1.
    * @param configuration
    * @param modelOID
    * @param validFrom
    * @param validTo
    * @param comment
    * @param disabled
    * @param ignoreWarnings
    * @return
    * @throws DeploymentException
    */
   private DeploymentInfo doOverwriteModel(ParsedDeploymentUnit element, DeploymentOptions options)
         throws DeploymentException
   {
      IModel model = element.getModel();
      try
      {
         checkDaemonStopState(options.isIgnoreWarnings());
         assertNotPredefinedModel(model);
      }
      catch (Exception e)
      {
         return deploymentError(e, null);
      }

      try
      {
         ModelManager modelManager = ModelManagerFactory.getCurrent();

         DeploymentInfo info = modelManager.overwriteModel(element, options);

         try
         {
            DocumentTypeUtils.synchronizeDocumentTypeSchema(model);
         }
         catch (Exception e)
         {
            throw new PublicException(e);
         }

         reportDeploymentState(info);
         if (!info.success())
         {
            throw new DeploymentException(null, Collections.singletonList(info));
         }
         AuditTrailLogger.getInstance(LogCode.ENGINE).info(MessageFormat.format(OVERWRITE_MODEL_MESSAGE,
               model.getName(), info.getModelOID()));
         return info;
      }
      finally
      {
         reloadModelManagerAfterModelOperation();
      }
   }

   public void assertNotPredefinedModel(IModel model)
   {
      if (PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
      {
         throw new IllegalOperationException(
               BpmRuntimeError.BPMRT_NO_CHANGES_TO_MODEL.raise(
                     PredefinedConstants.PREDEFINED_MODEL_ID));
      }
   }

   private void reloadModelManagerAfterModelOperation()
   {
      BpmRuntimeEnvironment runtimeEnvironment = PropertyLayerProviderInterceptor.getCurrent();
      runtimeEnvironment.setModelOverrides(null);
      if (Parameters.instance().getBoolean(
            KernelTweakingProperties.RELOAD_MODEL_MANAGER_AFTER_MODEL_OPERATION, true))
      {
         new FlushModelManagerAction(ModelManagerFactory.getCurrent()).execute();
      }
   }

   private static final class FlushModelManagerAction implements Action, Serializable
   {
      private static final long serialVersionUID = 1L;

      private final ModelManager partitionLocalModelManager;

      public FlushModelManagerAction(ModelManager modelManager)
      {
         if (modelManager instanceof ModelManagerPartition)
         {
            this.partitionLocalModelManager = modelManager;
         }
         else if (modelManager instanceof ModelManagerBean)
         {
            this.partitionLocalModelManager = ((ModelManagerBean) modelManager)
                  .getModelManagerPartition();
         }
         else
         {
            this.partitionLocalModelManager = null;
         }
      }

      public Object execute()
      {
         if ((null != partitionLocalModelManager))
         {
            ((ModelManagerPartition) partitionLocalModelManager).release();
         }
         return null;
      }
   }

   public void writeLogEntry(LogType logType, ContextKind contextType, long contextOid, String message,
         Throwable throwable) throws ObjectNotFoundException
   {
      Object context = null;
      if (ContextKind.ActivityInstance.equals(contextType))
      {
         context = ActivityInstanceBean.findByOID(contextOid);
      }
      else if (ContextKind.ProcessInstance.equals(contextType))
      {
         context = ProcessInstanceBean.findByOID(contextOid);
      }

      AuditTrailLogger logger = AuditTrailLogger.getInstance(LogCode.ADMINISTRATION, context);

      if (logType == null)
      {
         logType = LogType.Unknwon;
      }
      switch (logType.getValue())
      {
         case LogType.DEBUG:
            logger.debug(message, throwable);
            break;
         case LogType.ERROR:
            logger.error(message, throwable);
            break;
         case LogType.FATAL:
            logger.fatal(message, throwable);
            break;
         case LogType.INFO:
            logger.info(message, throwable);
            break;
         default:
            /* WARN and UNKNOWN are logged as warnings */
            logger.warn(message, throwable);
            break;
      }
   }

   public Department createDepartment(String id, String name, String description,
         DepartmentInfo parent, OrganizationInfo organization)
         throws DepartmentExistsException, ObjectNotFoundException, InvalidArgumentException, IllegalOperationException
   {
      checkInternalAuthentified();
      ProcessInstanceGroupUtils.assertNotCasePerformer(organization);

      if (StringUtils.isEmpty(id))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("id"));
      }
      if (StringUtils.isEmpty(name))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("name"));
      }
      if (organization == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("organization"));
      }
      AuditTrailPartitionBean partition = (AuditTrailPartitionBean) SecurityProperties.getPartition(false);
      DepartmentBean parentDepartment = parent == null ? null : DepartmentBean.findByOID(parent.getOID());
      IOrganization org = (IOrganization) ModelManagerFactory.getCurrent().findModelParticipant(organization);

      try
      {
         DepartmentBean.findById(id, parentDepartment, org);
         throw new DepartmentExistsException(id);
      }
      catch (ObjectNotFoundException x)
      {
      }

      DepartmentBean department = new DepartmentBean(id, name, partition, parentDepartment, description, organization);

      trace.info("Created department '" + id + "', oid = " + department.getOID());

      return DetailsFactory.create(department, IDepartment.class, DepartmentDetails.class);
   }

   public Department modifyDepartment(long oid, String name, String description)
         throws ObjectNotFoundException, InvalidArgumentException, IllegalOperationException
   {
      checkInternalAuthentified();

      if (StringUtils.isEmpty(name))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("name"));
      }
      DepartmentBean department = DepartmentBean.findByOID(oid);
      department.lock();
      department.setName(name);
      department.setDescription(description);
      return DetailsFactory.create(department, IDepartment.class, DepartmentDetails.class);
   }

   public void removeDepartment(long oid) throws ObjectNotFoundException, InvalidArgumentException, IllegalOperationException
   {
      checkInternalAuthentified();
      DepartmentBean.findByOID(oid).delete();
   }

   public Department getDepartment(long oid) throws ObjectNotFoundException
   {
      return DetailsFactory.create(DepartmentBean.findByOID(oid), IDepartment.class, DepartmentDetails.class);
   }

   public Preferences getPreferences(PreferenceScope scope,
         String moduleId, String preferencesId)
   {
      return getPreferenceStore().getPreferences(scope, moduleId, preferencesId);
   }

   public void savePreferences(Preferences preferences)
   {
      if(preferences == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("preferences"));
      }
      if(preferences.getPreferences() == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_SPEC.raise("preferences", "preferences"));
      }
      if(StringUtils.isEmpty(preferences.getModuleId()))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_SPEC.raise("preferences", "moduleId"));
      }
      if(StringUtils.isEmpty(preferences.getPreferencesId()))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_INVALID_SPEC.raise("preferences", "preferencesId"));
      }

      getPreferenceStore().savePreferences(preferences, true);
   }

   public void savePreferences(List<Preferences> preferencesList)
   {
      if(preferencesList == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("preferences"));
      }

      for (Preferences preferences : preferencesList)
      {
         savePreferences(preferences);
      }
   }

   public ConfigurationVariables getConfigurationVariables(String modelId)
   {
      if (StringUtils.isEmpty(modelId))
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("modelId"));
      }

      return ConfigurationVariableUtils.getConfigurationVariables(getPreferenceStore(),
            modelId, true);
   }

   public List<ConfigurationVariables> getConfigurationVariables(List<String> modelIds)
   {
      if(modelIds == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("modelIds"));
      }

      List<ConfigurationVariables> confVars = new ArrayList<ConfigurationVariables>();
      for (String modelId : modelIds)
      {
         if (StringUtils.isEmpty(modelId))
         {
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("modelId"));
         }
         confVars.add(ConfigurationVariableUtils.getConfigurationVariables(
               getPreferenceStore(), modelId, true));
      }
      return confVars;
   }


   public ConfigurationVariables getConfigurationVariables(byte[] model)
   {
      if(model == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("model"));
      }

      String modelXml = new String(model);
      if (ParametersFacade.instance().getBoolean(
            KernelTweakingProperties.XPDL_MODEL_DEPLOYMENT, true))
      {
         // convert to CWM format
         String encoding = Parameters.instance().getObject(
               PredefinedConstants.XML_ENCODING, XpdlUtils.ISO8859_1_ENCODING);
         modelXml = XpdlUtils.convertXpdl2Carnot(modelXml, encoding);
      }

      IModel iModel = new DefaultXMLReader(false,
            new NullConfigurationVariablesProvider()).importFromXML(new StringReader(
            modelXml));

      return ConfigurationVariableUtils.getConfigurationVariables(getPreferenceStore(), iModel);
   }

   public List<ModelReconfigurationInfo> saveConfigurationVariables(
         ConfigurationVariables configurationVariables, boolean force)
   {
      if(configurationVariables == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("configurationVariables"));
      }

      List<ModelReconfigurationInfo> info;
      try
      {
         info = ConfigurationVariableUtils.saveConfigurationVariables(
               getPreferenceStore(), configurationVariables, force);
      }
      finally
      {
         reloadModelManagerAfterModelOperation();
      }

      return info;
   }

   public RuntimePermissions getGlobalPermissions()
   {
      Map<String, List<String>> globalPermissions = PermissionUtils.getGlobalPermissions(getPreferenceStore(), true);

      return DetailsFactory.create(
            globalPermissions, Map.class, RuntimePermissionsDetails.class);
   }

   public void setGlobalPermissions(RuntimePermissions permissions) throws ValidationException
   {
      if(permissions == null)
      {
         throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("permissions"));
      }

      try
      {
         Map<String, List<String>> permissionsMap;
         if (permissions instanceof RuntimePermissionsDetails)
         {
            permissionsMap = ((RuntimePermissionsDetails) permissions).getPermissionMap();
         }
         else
         {
            permissionsMap = CollectionUtils.newMap();
            for (String permissionId : permissions.getAllPermissionIds())
            {
               Set<ModelParticipantInfo> grants = permissions.getGrants(permissionId);
               if (grants != null)
               {
                  List<String> grantIds = new LinkedList<String>();

                  for (ModelParticipantInfo modelParticipantInfo : grants)
                  {
                     if (modelParticipantInfo.getDepartment() != null)
                     {
                        throw new ValidationException(new IllegalArgumentException(
                              Department.class.getName()).getLocalizedMessage(), false);
                     }
                     if (modelParticipantInfo instanceof QualifiedModelParticipantInfo)
                     {
                        grantIds.add(((QualifiedModelParticipantInfo) modelParticipantInfo).getQualifiedId());
                     }
                     else
                     {
                        grantIds.add(modelParticipantInfo.getId());
                     }
                  }

                  permissionsMap.put(permissionId, grantIds);
               }
            }
         }
         PermissionUtils.setGlobalPermissions(getPreferenceStore(), permissionsMap);
      }
      finally
      {
         reloadModelManagerAfterModelOperation();
      }
   }

   private static IPreferenceStorageManager getPreferenceStore()
   {
      return PreferenceStorageFactory.getCurrent();
   }
   
   /**
    * Class is used to list all possible global permissions. With it the permissions 
    * can be evaluated by <code>Authorization2</code> class.
    * @author sven.rottstock
    * @see GlobalPermissionConstants
    */
   private static interface GlobalPermissionSpecificService extends Service
   {
      @ExecutionPermission(id=ExecutionPermission.Id.manageAuthorization)
      Permission getManageAuthorizationPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.controlProcessEngine)
      Permission getControlProcessEnginePermission();

      @ExecutionPermission(id=ExecutionPermission.Id.deployProcessModel)
      Permission getDeployProcessModelPermission();

      @ExecutionPermission(id=ExecutionPermission.Id.forceSuspend)
      Permission getForceSuspendPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.manageDaemons)
      Permission getManageDaemonsPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.modifyAuditTrail)
      Permission getModifyAuditTrailPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.modifyDepartments)
      Permission getModifyDepartmentsPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.modifyUserData)
      Permission getModifyUserDataPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.readAuditTrailStatistics)
      Permission getReadAuditTrailStatisticsPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.createCase)
      Permission getCreateCasePermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.readDepartments)
      Permission getReadDepartmentsPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.readModelData)
      Permission getReadModelDataPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.readUserData)
      Permission getReadUserDataPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.resetUserPassword)
      Permission getResetUserPasswordPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.runRecovery)
      Permission getRunRecoveryPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.saveOwnUserScopePreferences)
      Permission getSaveOwnUserScopePreferencesPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.saveOwnRealmScopePreferences)
      Permission getSaveOwnRealmScopePreferencesPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.saveOwnPartitionScopePreferences)
      Permission getSaveOwnPartitionScopePreferencesPermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.joinProcessInstance)
      Permission getJoinProcessInstancePermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.spawnPeerProcessInstance)
      Permission getSpawnPeerProcessInstancePermission();
      
      @ExecutionPermission(id=ExecutionPermission.Id.spawnSubProcessInstance)
      Permission getSpawnSubProcessInstancePermission();
   }
}