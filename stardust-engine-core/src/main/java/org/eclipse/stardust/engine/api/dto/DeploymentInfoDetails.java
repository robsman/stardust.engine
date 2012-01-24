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
package org.eclipse.stardust.engine.api.dto;

import java.util.Date;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DeploymentInfoDetails extends ModelReconfigurationInfoDetails implements DeploymentInfo
{
   private static final long serialVersionUID = 2L;
   private Date validFrom;
   private Date deploymentTime;
   private String comment;
   private int revision;

   public DeploymentInfoDetails(Date validFrom, String id, String comment)
   {
      super(id);
      
      this.validFrom = validFrom;
      this.comment = comment;
   }

   public DeploymentInfoDetails(IModel model)
   {
      super(model);
      
      this.comment = (String) model.getAttribute(PredefinedConstants.DEPLOYMENT_COMMENT_ATT);
      this.validFrom = (Date) model.getAttribute(PredefinedConstants.VALID_FROM_ATT);
      this.deploymentTime = (Date) model.getAttribute(PredefinedConstants.DEPLOYMENT_TIME_ATT);
      this.revision = model.getIntegerAttribute(PredefinedConstants.REVISION_ATT);
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public Date getDeploymentTime()
   {
      return deploymentTime;
   }

   public void setDeploymentTime(Date date)
   {
      deploymentTime = date;
   }

   public String getDeploymentComment()
   {
      return comment;
   }

   public int getRevision()
   {
      return revision;
   }

   public void setRevision(int revision)
   {
      this.revision = revision;
   }
}
