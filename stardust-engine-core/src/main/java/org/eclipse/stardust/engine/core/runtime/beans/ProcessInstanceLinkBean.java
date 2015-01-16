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

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.persistence.*;
import org.eclipse.stardust.engine.core.persistence.jdbc.PersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


public class ProcessInstanceLinkBean extends PersistentBean implements IProcessInstanceLink, IProcessInstanceAware
{
   public static final String FIELD__PROCESS_INSTANCE = "processInstance";
   public static final String FIELD__LINKED_PROCESS_INSTANCE = "linkedProcessInstance";
   public static final String FIELD__LINK_TYPE = "linkType";
   public static final String FIELD__CREATE_TIME = "createTime";
   public static final String FIELD__CREATING_USER = "creatingUser";
   public static final String FIELD__LINKING_COMMENT = "linkingComment";

   public static final FieldRef FR__PROCESS_INSTANCE = new FieldRef(ProcessInstanceLinkBean.class, FIELD__PROCESS_INSTANCE);
   public static final FieldRef FR__LINKED_PROCESS_INSTANCE = new FieldRef(ProcessInstanceLinkBean.class, FIELD__LINKED_PROCESS_INSTANCE);
   public static final FieldRef FR__LINK_TYPE = new FieldRef(ProcessInstanceLinkBean.class, FIELD__LINK_TYPE);
   public static final FieldRef FR__CREATE_TIME = new FieldRef(ProcessInstanceLinkBean.class, FIELD__CREATE_TIME);
   public static final FieldRef FR__CREATING_USER = new FieldRef(ProcessInstanceLinkBean.class, FIELD__CREATING_USER);
   public static final FieldRef FR__LINKING_COMMENT = new FieldRef(ProcessInstanceLinkBean.class, FIELD__LINKING_COMMENT);

   public static final String TABLE_NAME = "procinst_link";
   public static final String DEFAULT_ALIAS = "pil";
   public static final String[] PK_FIELD = new String[] {
         FIELD__PROCESS_INSTANCE,
         FIELD__LINKED_PROCESS_INSTANCE,
         FIELD__LINK_TYPE};
   public static final boolean TRY_DEFERRED_INSERT = true;

   private long processInstance;
   private long linkedProcessInstance;
   private long linkType;
   private long createTime;
   private long creatingUser;

   static final int linkingComment_COLUMN_LENGTH = 255;
   private String linkingComment;

   public ProcessInstanceLinkBean()
   {
   }

   public ProcessInstanceLinkBean(IProcessInstance processInstance, IProcessInstance linkedProcessInstance,
         IProcessInstanceLinkType linkType, String comment)
   {
      assert processInstance != null : "Process instance must not be null";
      assert linkedProcessInstance != null : "Linked process instance must not be null";
      assert linkType != null : "Link type must not be null";

      this.processInstance = processInstance.getOID();
      this.linkedProcessInstance = linkedProcessInstance.getOID();
      this.linkType = linkType.getOID();

      createTime = System.currentTimeMillis();
      creatingUser = SecurityProperties.getUserOID();
      this.linkingComment = StringUtils.cutString(comment, linkingComment_COLUMN_LENGTH);

      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      session.cluster(this);
   }

   public static ResultIterator<IProcessInstanceLink> findAllForProcessInstance(IProcessInstance processInstance)
   {
      assert processInstance != null : "Process instance must not be null";

      return findAllForProcessInstance(processInstance.getOID());
   }

   public static ResultIterator<IProcessInstanceLink> findAllForProcessInstance(Long processInstanceOid)
   {
      Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryExtension query = QueryExtension.where(Predicates.orTerm(
            Predicates.isEqual(FR__PROCESS_INSTANCE, processInstanceOid),
            Predicates.isEqual(FR__LINKED_PROCESS_INSTANCE, processInstanceOid)));

      query.setOrderCriteria(new OrderCriteria(FR__CREATE_TIME));

      return session.<IProcessInstanceLink,ProcessInstanceLinkBean>getIterator(ProcessInstanceLinkBean.class, query);
   }

   public long getProcessInstanceOID()
   {
      return processInstance;
   }

   public IProcessInstance getProcessInstance()
   {
      return ProcessInstanceBean.findByOID(processInstance);
   }

   public long getLinkedProcessInstanceOID()
   {
      return linkedProcessInstance;
   }

   public IProcessInstance getLinkedProcessInstance()
   {
      return ProcessInstanceBean.findByOID(linkedProcessInstance);
   }

   public IProcessInstanceLinkType getLinkType()
   {
      return ProcessInstanceLinkTypeBean.findByOID(linkType);
   }

   public Date getCreateTime()
   {
      return new Date(createTime);
   }

   public long getCreatingUserOID()
   {
      return creatingUser;
   }

   public IUser getCreatingUser()
   {
      return UserBean.findByOid(creatingUser);
   }

   public String getComment()
   {
      return linkingComment;
   }
}
