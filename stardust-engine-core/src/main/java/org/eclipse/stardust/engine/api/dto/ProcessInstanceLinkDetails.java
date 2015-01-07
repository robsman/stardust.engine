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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
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
      
      PropertyLayer layer = null;        
      if (creatingUserOID != 0)
      {
         try
         {
            IUser user= UserBean.findByOid(creatingUserOID);
            
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(UserDetailsLevel.PRP_USER_DETAILS_LEVEL, UserDetailsLevel.Core);
            layer = ParametersFacade.pushLayer(props);
            
            this.creatingUser = (UserDetails) DetailsFactory.create(user, IUser.class,
                  UserDetails.class);
         }   
         catch (ObjectNotFoundException e)
         {
            creatingUser = null;       
         }
         finally
         {
            if (null != layer)
            {
               ParametersFacade.popLayer();
            }           
         }         
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
