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

import java.io.Serializable;
import java.util.Date;

/**
 * Container class for the deployment options.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class DeploymentOptions implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * Deployment options with default values: no comment, warning are not ignored, no validity restriction
    * and 'overwrite' operations without an initial model are not allowed.
    */
   public static final DeploymentOptions DEFAULT = new DeploymentOptions();

   /**
    * The deployment comment.
    */
   private String comment;

   /**
    * Validity start time for the model or null if unlimited.
    */
   private Date validFrom;

   /**
    * Specifies that the deployment should continue if only warnings were issued.
    */
   private boolean ignoreWarnings;

   /**
    * Allows an 'overwrite' operation without having an initial model. If this option is set, the operation to perform
    * is 'overwrite' and there's no initial model, a 'deploy' operation is done instead.
    */
   private boolean allowOverwriteWithoutInitialModel;

   /**
    * Retrieves the deployment comment.
    *
    * @return the comment, or null if not set.
    */
   public String getComment()
   {
      return comment;
   }

   /**
    * Set the deployment comment.
    *
    * @param comment the deployment comment.
    */
   public void setComment(String comment)
   {
      this.comment = comment;
   }

   /**
    * Retrieves the moment from when the deployed models will be valid.
    *
    * @return the valid from timestamp, or null if there are no validity restrictions.
    */
   public Date getValidFrom()
   {
      return validFrom;
   }

   /**
    * Sets the validity start time.
    *
    * @param validFrom the valid from timestamp or null if there are no validity restrictions.
    */
   public void setValidFrom(Date validFrom)
   {
      this.validFrom = validFrom;
   }

   /**
    * Retrieves if the warnings should be ignored during deployment.
    *
    * @return true if warning will be ignored. If false, warning will be treated like errors.
    */
   public boolean isIgnoreWarnings()
   {
      return ignoreWarnings;
   }

   /**
    * Sets that warnings should be ignored during deployment.
    *
    * @param ignoreWarnings true to ignore warnings, false to treat warnings as errors.
    */
   public void setIgnoreWarnings(boolean ignoreWarnings)
   {
      this.ignoreWarnings = ignoreWarnings;
   }

   /**
    * Retrieves if 'overwrite' operations without an initial model are allowed. If this option is set,
    * the operation to perform is 'overwrite' and there's no initial model, a 'deploy' operation is done instead.
    *
    * @return whether 'overwrite' operations without an initial model are allowed
    */
   public boolean isAllowOverwriteWithoutInitialModel()
   {
      return allowOverwriteWithoutInitialModel;
   }

   /**
    * Sets whether 'overwrite' operations without an initial model should be allowed. If this option is set,
    * the operation to perform is 'overwrite' and there's no initial model, a 'deploy' operation is done instead.
    *
    * @param allowOverwriteWithoutInitialModel whether 'overwrite' operations without an initial model should be allowed
    */
   public void setAllowOverwriteWithoutInitialModel(boolean allowOverwriteWithoutInitialModel)
   {
      this.allowOverwriteWithoutInitialModel = allowOverwriteWithoutInitialModel;
   }
}
