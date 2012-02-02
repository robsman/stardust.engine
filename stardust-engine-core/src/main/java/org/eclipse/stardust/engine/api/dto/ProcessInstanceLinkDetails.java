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

import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLink;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLinkType;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;


public class ProcessInstanceLinkDetails implements ProcessInstanceLink
{
   private static final long serialVersionUID = 1L;

   private long sourceOID;
   private long targetOID;
   private ProcessInstanceLinkType linkType;
   private Date createTime;
   private long creatingUserOID;
   private User creatingUser;
   private String comment;

   public ProcessInstanceLinkDetails(long sourceOID, long targetOID,
         ProcessInstanceLinkType linkType, Date createTime, long creatingUserOID,
         String comment)
   {
      this.sourceOID = sourceOID;
      this.targetOID = targetOID;
      this.linkType = linkType;
      this.createTime = createTime;
      this.creatingUserOID = creatingUserOID;
      this.comment = comment;
      
      IUser user= UserBean.findByOid(creatingUserOID);
      if (user != null)
      {
         this.creatingUser = (UserDetails) DetailsFactory.create(user, IUser.class,
               UserDetails.class);
      }
      else
      {
         this.creatingUser = null;
      }
   }

   public long getSourceOID()
   {
      return sourceOID;
   }

   public long getTargetOID()
   {
      return targetOID;
   }

   public ProcessInstanceLinkType getLinkType()
   {
      return linkType;
   }

   public Date getCreateTime()
   {
      return createTime;
   }
   
   public User getCreatingUser()
   {
      return creatingUser;
   }

   public String getComment()
   {
      return comment;
   }
}
