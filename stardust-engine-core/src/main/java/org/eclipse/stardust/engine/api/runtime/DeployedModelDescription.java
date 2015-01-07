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
package org.eclipse.stardust.engine.api.runtime;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ModelElement;


/**
 * The <code>DeployedModelDescription</code> class provides deployment information for a
 * workflow model.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface DeployedModelDescription extends ModelElement
{
   /**
    * Gets whether the model is the active model. The model is active if it is not
    * disabled, the current time is between the validity start time and validity end time,
    * and there is no predecessor satisfying these constraints.
    *
    * @return true if this model is the currently active one.
    */
   boolean isActive();

   /**
    * Gets the validity start time for this model.
    *
    * @return the validity start time or null if unlimited.
    */
   Date getValidFrom();

   /**
    * Gets the time when this model was deployed.
    *
    * @return the deployment time.
    */
   Date getDeploymentTime();

   /**
    * Gets the comment of the last deployment.
    *
    * @return the deployment coment.
    */
   String getDeploymentComment();

   /**
    * Gets the version of the model.
    *
    * @return the model version.
    */
   String getVersion();

   /**
    * Gets the revision of the model.
    *
    * @return a number specifying how many times the model was overwritten.
    */
   int getRevision();

   /**
    * Gets the Model OIDs of all linked Providers (Models which are used by the deployed model)
    *
    * @return a list which contains the OIDs of the models which are used by the deployed model.
    */
   List<Long> getProviderModels();
   
   /**
    * Gets the Model OIDs of all linked Consumers (Models which are using the deployed model)
    *
    * @return a list which contains the OIDs of the models which are using the deployed model.
    */
   List<Long> getConsumerModels();
   
   /**
    * Gets a map which, per Process Interface ID, describes all available Implementation Alternatives 
    * and indicates which one is the Primary Implementation.
    * 
    * @return a map which contains per process interface id corresponding implementations.
    */
   Map<String, List<ImplementationDescription>> getImplementationProcesses(); 
}
