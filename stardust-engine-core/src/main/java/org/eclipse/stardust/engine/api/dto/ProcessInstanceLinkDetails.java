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


public class ProcessInstanceLinkDetails implements ProcessInstanceLink
{
   private static final long serialVersionUID = 1L;

   private long sourceOID;
   private long targetOID;
   private ProcessInstanceLinkType linkType;
   private Date createTime;
   private long creatingUserOID;
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

   public long getCreatingUserOID()
   {
      return creatingUserOID;
   }

   public String getComment()
   {
      return comment;
   }
}
