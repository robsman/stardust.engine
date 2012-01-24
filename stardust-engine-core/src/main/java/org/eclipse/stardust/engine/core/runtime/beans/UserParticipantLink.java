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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.api.model.IOrganization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Joins;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.utils.DepartmentUtils;


/**
 * Auxiliary class for getting the n:m relationship between user and
 * participants.
 * <p/>
 * Users are managed by the runtime database; participants are managed
 * by the (XML) model database.
 */
public class UserParticipantLink extends IdentifiablePersistentBean
{
   private static final long serialVersionUID = 2L;

   private static final Logger trace = LogManager.getLogger(DepartmentHierarchyBean.class);

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__USER = "workflowUser";
   public static final String FIELD__PARTICIPANT = "participant";
   public static final String FIELD__DEPARTMENT = "department";

   public static final FieldRef FR__OID = new FieldRef(UserParticipantLink.class, FIELD__OID);
   public static final FieldRef FR__USER = new FieldRef(UserParticipantLink.class, FIELD__USER);
   public static final FieldRef FR__PARTICIPANT = new FieldRef(UserParticipantLink.class, FIELD__PARTICIPANT);
   public static final FieldRef FR__DEPARTMENT = new FieldRef(UserParticipantLink.class, FIELD__DEPARTMENT);

   public static final String TABLE_NAME = "user_participant";
   public static final String DEFAULT_ALIAS = "ump";
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "user_participant_seq";
   public static final String[] user_particip_idx1_INDEX = new String[] {FIELD__USER};
   public static final String[] user_particip_idx2_INDEX = new String[] {FIELD__PARTICIPANT, FIELD__DEPARTMENT};
   // TODO: need more Indexes containing scope
   
   public static final String[] user_particip_idx3_UNIQUE_INDEX = new String[] {FIELD__OID};

   private UserBean workflowUser;
   static final String workflowUser_EAGER_FETCH = "true";
   static final String workflowUser_MANDATORY = "true";

   private long participant;
   private long department;
   
   private transient IModelParticipant cachedParticipant;

   /**
    * Count all link objects for a give participant.
    */
   public static long countAllFor(IModelParticipant participant)
   {
      long rtOid = ModelManagerFactory.getCurrent().getRuntimeOid(participant);
      if (0 == rtOid)
      {
         throw new InternalException("Missing runtime OID for participant " + participant);
      }

      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      Join participantsJoin = new Join(AuditTrailParticipantBean.class, "p")
         .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
      QueryExtension where = QueryExtension.where(Predicates.andTerm(
            Predicates.isEqual(FR__PARTICIPANT, rtOid),
            Predicates.isEqual(participantsJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), participant.getModel().getModelOID())));
      where.addJoin(participantsJoin);
      return session.getCount(UserParticipantLink.class, where);
   }
   
   public static long countAllFor(long rtOid, long modelOid)
   {
      Join participantsJoin = new Join(AuditTrailParticipantBean.class, "p")
         .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
      Join modelsJoin = new Join(ModelPersistorBean.class, "m")
         .on(participantsJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), ModelPersistorBean.FIELD__OID);

      Joins joins = new Joins();
      joins.add(participantsJoin);
      if (modelOid == 0)
      {
         joins.add(modelsJoin);
      }
      
      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      QueryExtension where = QueryExtension.where(Predicates.andTerm(
            Predicates.isEqual(FR__PARTICIPANT, rtOid),
            modelOid == 0
               ? Predicates.isEqual(modelsJoin.fieldRef(ModelPersistorBean.FIELD__PARTITION), SecurityProperties.getPartitionOid())
               : Predicates.isEqual(participantsJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), modelOid)));
      where.addJoins(joins);
      return session.getCount(UserParticipantLink.class, where);
   }
   
   public static long findFirstAssignedDepartment(IUser user, IModelParticipant participant, List<Long> departments)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      long rtOid = modelManager.getRuntimeOid(participant);
      if (0 == rtOid)
      {
         throw new InternalException("Missing runtime OID for participant " + participant);
      }

      Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      int modelOID = participant.getModel().getModelOID();
      Join participantsJoin = new Join(AuditTrailParticipantBean.class, "p")
         .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
      QueryDescriptor piDescriptor = QueryDescriptor.from(UserParticipantLink.class)
            .select(UserParticipantLink.FR__DEPARTMENT, UserParticipantLink.FR__PARTICIPANT)
            .where(Predicates.andTerm(
                  Predicates.isEqual(FR__USER, user.getOID()),
                  Predicates.isEqual(participantsJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), modelOID),
                  Predicates.inList(FR__DEPARTMENT, departments)));
      piDescriptor.getQueryExtension().addJoin(participantsJoin);
      ResultSet resultSet = null;
      try
      {
         resultSet = session.executeQuery(piDescriptor);
         while (resultSet.next())
         {
            long department = resultSet.getLong(1);
            long participantRtOid = resultSet.getLong(2);
            if (rtOid == participantRtOid)
            {
               return department;
            }
            if (participant instanceof IOrganization)
            {
               IModelParticipant other = modelManager.findModelParticipant(modelOID, participantRtOid);
               if (DepartmentUtils.isChild(other, (IOrganization) participant))
               {
                  return department;
               }
            }
         }
         return -1;
      }
      catch (SQLException e)
      {
         trace.warn("Failed executing query.", e);
         throw new PublicException(e);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
      }
   }

   /**
    * Retrieves all link objects for a give participant.
    */
   public static Iterator findAllFor(IModelParticipant participant)
   {
      long rtOid = ModelManagerFactory.getCurrent().getRuntimeOid(participant);
      if (0 == rtOid)
      {
         throw new InternalException("Missing runtime OID for participant " + participant);
      }

      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      Join participantsJoin = new Join(AuditTrailParticipantBean.class, "p")
         .on(UserParticipantLink.FR__PARTICIPANT, AuditTrailParticipantBean.FIELD__OID);
      QueryExtension where = QueryExtension.where(Predicates.andTerm(
            Predicates.isEqual(FR__PARTICIPANT, rtOid),
            Predicates.isEqual(participantsJoin.fieldRef(AuditTrailParticipantBean.FIELD__MODEL), participant.getModel().getModelOID())));
      where.addJoin(participantsJoin);
      Vector result = session.getVector(UserParticipantLink.class, where);
      return result.iterator();
   }

   public UserParticipantLink(UserBean workflowUser, IModelParticipant participant)
   {
      this(workflowUser, participant, null);
   }

   public UserParticipantLink(UserBean workflowUser, IModelParticipant participant, IDepartment department)
   {
      this.workflowUser = workflowUser;
      
      long rtOid = ModelManagerFactory.getCurrent().getRuntimeOid(participant);
      if (0 == rtOid)
      {
         throw new InternalException("Missing runtime OID for participant " + participant);
      }
      
      this.participant = rtOid;
      this.department = department == null ? 0 : department.getOID();
   }

   /**
    * For transient creation of links.
    */
   public UserParticipantLink(long oid, UserBean workflowUser, long participant, long department)
   {
      this.oid = oid;
      this.workflowUser = workflowUser;
      this.participant = participant;
      this.department = department;
   }
   
   public UserParticipantLink()
   {
      participant = 0;
      department = 0;
   }

   public void delete()
   {
      ((UserBean) getUser()).participantLinks.remove(this);
      super.delete();
   }

   public IUser getUser()
   {
      fetchLink(FIELD__USER);
      return workflowUser;
   }

   public IModelParticipant getParticipant()
   {
      if (cachedParticipant == null)
      {
         fetch();
         cachedParticipant = ModelManagerFactory.getCurrent().findModelParticipant(PredefinedConstants.ANY_MODEL, participant);
      }
      return cachedParticipant;
   }
   
   public long getRuntimeParticipantOid()
   {
      fetch();
      return participant;
   }

   public IDepartment getDepartment()
   {
      fetch();
      if (department != 0)
      {
         return DepartmentBean.findByOID(department);
      }
      return null;
   }
   
   public long getDepartmentOid()
   {
      fetch();
      return department;
   }

   public static void deleteAllForDepartment(IDepartment department)
   {
      org.eclipse.stardust.engine.core.persistence.Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      ComparisonTerm predicate = Predicates.isEqual(FR__DEPARTMENT, department.getOID());
      QueryExtension queryExtension = QueryExtension.where(predicate);
      Vector<UserParticipantLink> links = session.getVector(UserParticipantLink.class, queryExtension);
      for (int i = 0; i < links.size(); i++)
      {
         UserParticipantLink link = links.get(i);

         // associated user needs to be updated, as it stores the link in 2nd level cache
         ((UserBean) link.getUser()).removeFromParticipants(link.getParticipant(), link.getDepartment());
      }
   }
}
