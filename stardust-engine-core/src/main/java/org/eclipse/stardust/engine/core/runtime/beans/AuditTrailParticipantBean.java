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

import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.IModelParticipant;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.PersistentModelElement;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


/**
 *
 */
public class AuditTrailParticipantBean extends IdentifiablePersistentBean implements PersistentModelElement
{
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__MODEL = "model";
   public static final String FIELD__ID = "id";
   public static final String FIELD__NAME = "name";
   public static final String FIELD__TYPE = "type";
   public static final String FIELD__DESCRIPTION = "description";

   public static final FieldRef FR__OID = new FieldRef(AuditTrailParticipantBean.class, FIELD__OID);
   public static final FieldRef FR__MODEL = new FieldRef(AuditTrailParticipantBean.class, FIELD__MODEL);
   public static final FieldRef FR__ID = new FieldRef(AuditTrailParticipantBean.class, FIELD__ID);
   public static final FieldRef FR__NAME = new FieldRef(AuditTrailParticipantBean.class, FIELD__NAME);
   public static final FieldRef FR__TYPE = new FieldRef(AuditTrailParticipantBean.class, FIELD__TYPE);
   public static final FieldRef FR__DESCRIPTION = new FieldRef(AuditTrailParticipantBean.class, FIELD__DESCRIPTION);

   public static final String TABLE_NAME = "participant";
   public static final String DEFAULT_ALIAS = "mpd";
   public static final String[] PK_FIELD = new String[] {FIELD__OID, FIELD__MODEL};
   public static final String[] participant_idx1_UNIQUE_INDEX = new String[] {
         FIELD__OID, FIELD__MODEL};
   public static final String[] participant_idx2_INDEX = new String[] {
         FIELD__ID, FIELD__OID, FIELD__MODEL};

   static final boolean type_USE_LITERALS = true;
   
   private long model;
   
   private static final int id_COLUMN_LENGTH = 50;
   private String id;
   private static final int name_COLUMN_LENGTH = 100;
   private String name;
   private int type;
   private static final int description_COLUMN_LENGTH = 4000;
   private String description;

   public static Iterator findAll(short partitionOid)
   {
      return SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).getIterator(
            AuditTrailParticipantBean.class,
            new QueryExtension() //
                  .addJoin(new Join(ModelPersistorBean.class) //
                        .on(AuditTrailParticipantBean.FR__MODEL,
                              ModelPersistorBean.FIELD__OID) //
                        .where(
                              Predicates.isEqual(ModelPersistorBean.FR__PARTITION,
                                    partitionOid))));
   }

   public static AuditTrailParticipantBean findByOid(long rtOid, long modelOid)
   {
      return (AuditTrailParticipantBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findFirst(
            AuditTrailParticipantBean.class, QueryExtension.where(Predicates.andTerm(
                  Predicates.isEqual(FR__OID, rtOid),
                  Predicates.isEqual(FR__MODEL, modelOid))));
   }

   public static int getMaxIdLength()
   {
      return id_COLUMN_LENGTH;
   }

   public AuditTrailParticipantBean()
   {
   }

   public AuditTrailParticipantBean(long rtOid, long modelOid, IModelParticipant participant)
   {
      setOID(rtOid);
      this.model = modelOid;
      
      update(participant);
   }

   public long getModel()
   {
      return model;
   }
   
   public String getId()
   {
      return id;
   }
   
   public String getName()
   {
      return name;
   }
   
   public long getParent()
   {
      return getModel();
   }
   
   public int getType()
   {
      return type;
   }
   
   public String getDescription()
   {
      return description;
   }

   public void update(IModelParticipant participant)
   {
      markModified();
      
      this.id = org.eclipse.stardust.common.StringUtils.cutString(participant.getId(),
            id_COLUMN_LENGTH);
      this.name = org.eclipse.stardust.common.StringUtils.cutString(participant.getName(),
            name_COLUMN_LENGTH);
      this.description = org.eclipse.stardust.common.StringUtils.cutString(
            participant.getDescription(), description_COLUMN_LENGTH);
      // @todo (france, ub): type attribute
   }
}
