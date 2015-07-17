/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Date;

import org.eclipse.stardust.engine.api.runtime.RuntimeArtifact;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

public class RuntimeArtifactBean extends IdentifiablePersistentBean implements IRuntimeArtifact
{
   private static final long serialVersionUID = 1L;

   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;

   public static final String FIELD__ARTIFACT_TYPE_ID = "artifactTypeId";

   public static final String FIELD__ARTIFACT_ID = "artifactId";

   public static final String FIELD__ARTIFACT_NAME = "artifactName";

   public static final String FIELD__REFERENCE_ID = "referenceId";

   public static final String FIELD__VALID_FROM = "validFrom";

   public static final String FIELD__PARTITION = "partition";

   public static final FieldRef FR__OID = new FieldRef(RuntimeArtifactBean.class,
         FIELD__OID);

   public static final FieldRef FR__ARTIFACT_TYPE_ID = new FieldRef(
         RuntimeArtifactBean.class, FIELD__ARTIFACT_TYPE_ID);

   public static final FieldRef FR__ARTIFACT_ID = new FieldRef(RuntimeArtifactBean.class,
         FIELD__ARTIFACT_ID);

   public static final FieldRef FR__ARTIFACT_NAME = new FieldRef(
         RuntimeArtifactBean.class, FIELD__ARTIFACT_NAME);

   public static final FieldRef FR__REFERENCE_ID = new FieldRef(
         RuntimeArtifactBean.class, FIELD__REFERENCE_ID);

   public static final FieldRef FR__VALID_FROM = new FieldRef(RuntimeArtifactBean.class,
         FIELD__VALID_FROM);

   public static final FieldRef FR__PARTITION = new FieldRef(RuntimeArtifactBean.class,
         FIELD__PARTITION);

   public static final String TABLE_NAME = "runtime_artifact";

   public static final String DEFAULT_ALIAS = "ra";

   public static final String PK_FIELD = FIELD__OID;

   public static final String PK_SEQUENCE = "runtime_artifact_seq";

   public static final boolean TRY_DEFERRED_INSERT = true;

   public static final String[] runtime_artifact_idx1_UNIQUE_INDEX = new String[] {FIELD__OID};

   public static final String[] runtime_artifact_idx2_INDEX = new String[] {
         FIELD__ARTIFACT_TYPE_ID, FIELD__ARTIFACT_ID, FIELD__VALID_FROM, FIELD__PARTITION};

   static final int artifactTypeId_COLUMN_LENGTH = 255;
   private String artifactTypeId;

   static final int artifactId_COLUMN_LENGTH = 255;
   private String artifactId;

   static final int artifactName_COLUMN_LENGTH = 255;
   private String artifactName;

   static final int referenceId_COLUMN_LENGTH = 255;
   private String referenceId;

   private long validFrom;

   private long partition;

   public RuntimeArtifactBean()
   {

   }

   public RuntimeArtifactBean(RuntimeArtifact runtimeArtifact)
   {
      this.artifactTypeId = runtimeArtifact.getArtifactTypeId();
      this.artifactId = runtimeArtifact.getArtifactId();
      this.artifactName = runtimeArtifact.getArtifactName();
      this.referenceId = null;
      Date validFromDate = runtimeArtifact.getValidFrom();
      if (validFromDate != null)
      {
         this.validFrom = validFromDate.getTime();
      }
      this.partition = SecurityProperties.getPartitionOid();

      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }

   public static RuntimeArtifactBean findByOid(long oid)
   {
      RuntimeArtifactBean result = (RuntimeArtifactBean) SessionFactory.getSession(
            SessionFactory.AUDIT_TRAIL).findByOID(RuntimeArtifactBean.class, oid);

      return result;
   }

   public static IRuntimeArtifact findActive(String artifactType, String artifactId, long time)
   {
      final Session session = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

      QueryExtension whereTerm = QueryExtension.where(Predicates.andTerm(
            Predicates.isEqual(FR__ARTIFACT_TYPE_ID, artifactType),//
            Predicates.isEqual(FR__ARTIFACT_ID, artifactId),//
            Predicates.isEqual(FR__PARTITION, SecurityProperties.getPartitionOid()),
            Predicates.lessOrEqual(FR__VALID_FROM, time)));

      whereTerm.addOrderBy(FR__VALID_FROM, false);
      whereTerm.addOrderBy(FR__OID, false);

      return (IRuntimeArtifact) session
            .findFirst(RuntimeArtifactBean.class, whereTerm);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#getReferenceId()
    */
   @Override
   public String getReferenceId()
   {
      fetch();

      return referenceId;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#setReferenceId(java.lang.String)
    */
   @Override
   public void setReferenceId(String referenceId)
   {
      markModified();

      this.referenceId = referenceId;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#getArtifactType()
    */
   @Override
   public String getArtifactTypeId()
   {
      fetch();

      return artifactTypeId;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#getArtifactId()
    */
   @Override
   public String getArtifactId()
   {
      fetch();

      return artifactId;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#getArtifactName()
    */
   @Override
   public String getArtifactName()
   {
      fetch();

      return artifactName;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#getValidFrom()
    */
   @Override
   public Date getValidFrom()
   {
      fetch();

      return new Date(validFrom);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.spi.artifact.IRuntimeArtifact#getPartition()
    */
   @Override
   public short getPartition()
   {
      fetch();

      return (short) partition;
   }

   @Override
   public String toString()
   {
      return "RuntimeArtifactBean [artifactTypeId=" + artifactTypeId + ", artifactId="
            + artifactId + ", artifactName=" + artifactName + ", referenceId="
            + referenceId + ", validFrom=" + validFrom + ", partition=" + partition + "]";
   }

}
