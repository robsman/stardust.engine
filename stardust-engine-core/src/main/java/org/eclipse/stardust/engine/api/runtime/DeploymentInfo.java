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

/**
 * The <code>DeploymentInfo</code> class is used to receive information about a
 * deployment operation. Deployment operations are all operations which modifies the
 * models in audit trail or their attributes, i.e. deploy, overwrite, modify or delete.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface DeploymentInfo extends ModelReconfigurationInfo
{
   /**
    * Returns the validity start time for the model. No processes from this model are
    * startable when the current date is before this time stamp.
    *
    * @return the validity start date or null if unlimited.
    */
   Date getValidFrom();

   /**
    * Returns the time when the deployment operation was performed.
    *
    * @return the deployment time
    */
   Date getDeploymentTime();

   /**
    * Returns the comment provided when the deployment was performed.
    *
    * @return the deployment comment.
    */
   String getDeploymentComment();

   /**
    * The revision of the model. The revision is incremented each time the model is
    * overwritten.
    *
    * @return the revision of the model.
    */
   int getRevision();
}
