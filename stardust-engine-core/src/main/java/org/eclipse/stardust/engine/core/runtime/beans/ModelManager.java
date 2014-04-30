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

import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.ParsedDeploymentUnit;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface ModelManager
{
   /**
    * Returns the active version of the model.
    * <p>
    * The active version is the version with the latest <tt>validFrom</tt>
    * time stamp whose <tt>validTo</tt> time stamp is in the future.
    */
   IModel findActiveModel();

   /**
    * Returns all appearances of the participant with the ID <tt>id</tt> in
    * all loaded model versions.
    */
   Iterator<IModelParticipant> getParticipantsForID(String id);

   /**
    * Retrieves model with most recent valid from timestamp deployed in the audit trail. You could use
    * this model if active model returned is null.
    * The need for this may arise in situations where there is a model that will be activated in future.
    * And you might like to perform some actions against administartion session e.g, to start
    * daemons etc and you need a model to perform login before that.
    */
   IModel findLastDeployedModel();

   IModel findLastDeployedModel(String id);
   List<IModel> findLastDeployedModels();

   List<DeploymentInfo> deployModel(List<ParsedDeploymentUnit> units, DeploymentOptions options);

   DeploymentInfo overwriteModel(ParsedDeploymentUnit unit, DeploymentOptions options);

   Iterator<IModel> getAllModels();
   Iterator<IModel> getAllModelsForId(String id);

   /**
    * @deprecated
    */
   org.eclipse.stardust.engine.core.model.utils.ModelElement lookupObjectByOID(long oid);

   IModelParticipant findModelParticipant(long modelOid, long runtimeOid);
   IModelParticipant findModelParticipant(ModelParticipantInfo info);
   IData findData(long modelOid, long runtimeOid);
   IProcessDefinition findProcessDefinition(long modelOid, long runtimeOid);
   IActivity findActivity(long modelOid, long runtimeOid);
   ITransition findTransition(long modelOid, long runtimeOid);

   /**
    * Looks up the IData object which is referenced by given structured data.
    *
    * @param modelOid The model oid of the model the data / structured data belongs to.
    * @param runtimeOid The runtime oid of the structured data.
    *
    * @return The IData object referenced by given structured data.
    */
   IData findDataForStructuredData(long modelOid, long runtimeOid);
   IEventHandler findEventHandler(long modelOid, long runtimeOid);

   long getRuntimeOid(IdentifiableElement modelElement);

   long getRuntimeOid(IData data, String xPath);

   DeploymentInfo deleteModel(IModel model);

   void deleteAllModels();

   Iterator getAllAliveModels();

   void resetLastDeployment();

   long getLastDeployment();

   boolean isAlive(IModel model);

   IModel findModel(Predicate predicate);

   IModel findModel(long modelOID);

   IModel findModel(long modelOID, String modelId);

   boolean isActive(IModel model);

   DependentObjectsCache getDependentCache();

   void reanimate(IModel model);

   List validateDeployment(ParsedDeploymentUnit unit, DeploymentOptions options);

   List validateOverwrite(IModel oldModel, IModel newModel);

   int getModelCount();

   List<IModel> getModels();
   List<IModel> getModelsForId(String id);

   List<IModel> findActiveModels();

   IModel getFirstByPriority(List<IModel> candidates);

   IModel findActiveModel(String modelId);
}