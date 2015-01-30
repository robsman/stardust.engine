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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.beans.ModelBean;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.ForeignKey;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 *
 */
public class ActivityInstanceLogBean extends IdentifiablePersistentBean
      implements IActivityInstanceLog, IActivityInstanceAware
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__TYPE = "type";
   public static final String FIELD__STAMP = "stamp";
   public static final String FIELD__ACTIVITY_INSTANCE = "activityInstance";
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__PARTICIPANT = "participant";
   public static final String FIELD__WORKFLOW_USER = "workflowUser";

   public static final FieldRef FR__OID = new FieldRef(ActivityInstanceLogBean.class, FIELD__OID);
   public static final FieldRef FR__TYPE = new FieldRef(ActivityInstanceLogBean.class, FIELD__TYPE);
   public static final FieldRef FR__STAMP = new FieldRef(ActivityInstanceLogBean.class, FIELD__STAMP);
   public static final FieldRef FR__ACTIVITY_INSTANCE = new FieldRef(ActivityInstanceLogBean.class, FIELD__ACTIVITY_INSTANCE);
   public static final FieldRef FR__MODEL = new FieldRef(ActivityInstanceLogBean.class, FIELD__MODEL);
   public static final FieldRef FR__PARTICIPANT = new FieldRef(ActivityInstanceLogBean.class, FIELD__PARTICIPANT);
   public static final FieldRef FR__WORKFLOW_USER = new FieldRef(ActivityInstanceLogBean.class, FIELD__WORKFLOW_USER);

   public static final String TABLE_NAME = "activity_inst_log";
   private static final String PK_FIELD = FIELD__OID;
   private static final String PK_SEQUENCE = "activity_instance_log_seq";
   public static final String[] act_inst_log_idx1_INDEX =
         new String[]{FIELD__ACTIVITY_INSTANCE};
   public static final String[] act_inst_log_idx2_UNIQUE_INDEX =
         new String[]{FIELD__OID};

   static final boolean type_USE_LITERALS = true;

   private int type;

   private long stamp;
   private ActivityInstanceBean activityInstance;
   // @todo (france, ub): remove that field. it is never used

   @ForeignKey (modelElement=ModelBean.class)
   private long model;
   private long participant;
   private long workflowUser;

   /**
    *
    */
   public ActivityInstanceLogBean()
   {
   }

   /**
    *
    */
   public ActivityInstanceLogBean(int type, IActivityInstance activityInstance,
         long stamp)
   {
      this.type = type;
      this.stamp = stamp;
      this.activityInstance = (ActivityInstanceBean) activityInstance;
      this.workflowUser = SecurityProperties.getUserOID();

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   /*
    * Retrieves the type of the log.
    */
   public int getType()
   {
      fetch();

      return type;
   }

   /*
    * Retrieves the activity instances for which the log has been
    * written.
    */
   public IActivityInstance getActivityInstance()
   {
      fetchLink(FIELD__ACTIVITY_INSTANCE);

      return activityInstance;
   }

   /*
    * Retrieves the timestamp of the log.
    */
   public Calendar getTimeStamp()
   {
      fetch();

      Calendar time = Calendar.getInstance();

      time.setTime(new Date(stamp));

      return time;
   }

   /*
    * Retrieves the participant of the activity instance log context.
    */
   public long getParticipant()
   {
      return participant;
   }

   /*
    * Retrieves the user of the activity instance log context.
    */
   public IUser getUser()
   {
      if (workflowUser == 0)
      {
         return null;
      }
      return UserBean.findByOID(workflowUser);
   }
}
