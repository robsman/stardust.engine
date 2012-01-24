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
package org.eclipse.stardust.engine.core.runtime.utils;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IParticipant;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
import org.eclipse.stardust.engine.core.runtime.beans.IDepartment;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserGroup;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserGroupBean;


/**
 * @author rsauer
 * @version $Revision$
 */
public class PerformerUtils
{

   public static EncodedPerformer encodeParticipant(IParticipant performer)
   {
      return encodeParticipant(performer, null);
   }
   
   public static EncodedPerformer encodeParticipant(IParticipant performer,
         IDepartment department)
   {
      PerformerType performerKind;
      long performerOid;

      if (null == performer)
      {
         performerKind = PerformerType.None;
         performerOid = 0;
      }
      else if (performer instanceof IUser)
      {
         performerKind = PerformerType.User;
         performerOid = ((IUser) performer).getOID();
      }
      else if (performer instanceof IUserGroup)
      {
         performerKind = PerformerType.UserGroup;
         performerOid = ((IUserGroup) performer).getOID();
      }
      else if (performer instanceof IModelParticipant)
      {
         performerKind = PerformerType.ModelParticipant;
         performerOid = ModelManagerFactory.getCurrent().getRuntimeOid(
               (IModelParticipant) performer);
      }
      else
      {
         throw new InternalException("Unsupported performer: " + performer);
      }
      
      long departmentOid = 0;
      if (department != null)
      {
         departmentOid = department.getOID();
      }

      return new EncodedPerformer(performerKind, performerOid, departmentOid);
   }

   public static IParticipant decodePerformer(EncodedPerformer encodedPerformer,
         IModel model)
   {
      return decodePerformer(encodedPerformer.kind, encodedPerformer.oid, model);
   }

   public static IParticipant decodePerformer(PerformerType performerKind,
         long performerOid, IModel model)
   {
      IParticipant performer;

      if ((PerformerType.ModelParticipant == performerKind) && (0 < performerOid))
      {
         performer = ModelManagerFactory.getCurrent().findModelParticipant(
               model.getModelOID(), performerOid);
      }
      else if ((PerformerType.UserGroup == performerKind) && (0 != performerOid))
      {
         performer = UserGroupBean.findByOid(performerOid);
      }
      else if ((PerformerType.User == performerKind) && (0 != performerOid))
      {
         performer = UserBean.findByOid(performerOid);
      }
      else
      {
         performer = null;
      }

      return performer;
   }
   
   public static String getQualifiedName(IUser user)
   {
      StringBuffer label = new StringBuffer();
      
      if (null != user)
      {
         boolean parenthesis = false;
         if (!StringUtils.isEmpty(user.getLastName()))
         {
            label.append(user.getLastName());
            parenthesis = true;
         }
         if (!StringUtils.isEmpty(user.getFirstName()))
         {
            if (label.length() > 0)
            {
               label.append(", ");
            }
            label.append(user.getFirstName());
            parenthesis = true;
         }
         if(parenthesis)
         {
            label.append(" (");
         }
         label.append(user.getAccount());
         if(parenthesis)
         {
            label.append(")");
         }
      }

      return label.toString();
   }

   private PerformerUtils()
   {
      // utility class
   }

   public static class EncodedPerformer
   {
      public final PerformerType kind;
      public final long oid;
      public final long departmentOid;

      public EncodedPerformer(PerformerType participantKind, long participantOid)
      {
         this(participantKind, participantOid, 0);
      }

      public EncodedPerformer(PerformerType participantKind, long participantOid, long departmentOid)
      {
         this.kind = participantKind;
         this.oid = participantOid;
         this.departmentOid = departmentOid;
      }
   }
}
