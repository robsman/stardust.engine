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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.ComparisonTerm;
import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.IdentifiablePersistentBean;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * Deployment history table.
 * All changes to the models are recorded here.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ModelDeploymentBean extends IdentifiablePersistentBean implements Serializable
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ModelDeploymentBean.class);

   // convenient identifiers of field names
   public static final String FIELD__OID = IdentifiablePersistentBean.FIELD__OID;
   public static final String FIELD__DEPLOYER = "deployer";
   public static final String FIELD__DEPLOYMENT_TIME = "deploymentTime";
   public static final String FIELD__VALID_FROM = "validFrom";
   public static final String FIELD__DEPLOYMENT_COMMENT = "deploymentComment";

   // convenient field references
   public static final FieldRef FR__OID = new FieldRef(ModelDeploymentBean.class, FIELD__OID);
   public static final FieldRef FR__DEPLOYER = new FieldRef(ModelDeploymentBean.class, FIELD__DEPLOYER);
   public static final FieldRef FR__DEPLOYMENT_TIME = new FieldRef(ModelDeploymentBean.class, FIELD__DEPLOYMENT_TIME);
   public static final FieldRef FR__VALID_FROM = new FieldRef(ModelDeploymentBean.class, FIELD__VALID_FROM);
   public static final FieldRef FR__DEPLOYMENT_COMMENT = new FieldRef(ModelDeploymentBean.class, FIELD__DEPLOYMENT_COMMENT);

   // table descriptor
   public static final String TABLE_NAME = "model_dep";
   public static final String DEFAULT_ALIAS = "md";
   public static final String LOCK_TABLE_NAME = "model_dep_lck";
   public static final String LOCK_INDEX_NAME = "model_dep_lck_idx";

   // primary key descriptor
   public static final String PK_FIELD = FIELD__OID;
   public static final String PK_SEQUENCE = "model_dep_seq";
   public static final boolean TRY_DEFERRED_INSERT = true;
   
   // indexes descriptors
   public static final String[] model_dep_idx1_UNIQUE_INDEX = new String[]{FIELD__OID};
   public static final String[] model_dep_idx2_INDEX = new String[]{FIELD__DEPLOYMENT_TIME};
   public static final String[] model_dep_idx3_INDEX = new String[]{FIELD__DEPLOYER};

   /**
    * The OID of the user who performed the deployment.
    */
   private long deployer;
   
   /**
    * The time when deployment was performed.
    */
   private long deploymentTime;
   
   /**
    * Models deployed are valid from this moment on.
    */
   private long validFrom;
   
   /**
    * The deployment comment
    */
   private String deploymentComment;

   /**
    * For internal use only.
    */
   public ModelDeploymentBean()
   {
   }

   /**
    * Creates a new ModelDeployment.
    * 
    * @param comment the deployment comment.
    */
   public ModelDeploymentBean(String comment)
   {
      this(null, comment);
   }
   
   /**
    * Creates a new ModelDeployment.
    * 
    * @param validFrom the date from which the models are valid.
    * @param validTo the date up to the models are valid.
    * @param disabled specifies is the models are disabled.
    * @param comment the deployment comment.
    */
   public ModelDeploymentBean(Date validFrom, String comment)
   {
      this.validFrom = validFrom == null ? Long.MIN_VALUE + 1 : validFrom.getTime();
      this.deployer = SecurityProperties.getUserOID();
      this.deploymentTime = System.currentTimeMillis();
      this.deploymentComment = comment;
      if (trace.isDebugEnabled())
      {
         trace.debug(this);
      }
      SessionFactory.getSession(SessionFactory.AUDIT_TRAIL).cluster(this);
   }
   
   public Date getValidFrom()
   {
      fetch();
      return validFrom == Long.MIN_VALUE ? null : new Date(validFrom);
   }

   public IUser getDeployer()
   {
      return UserBean.findByOid(deployer);
   }

   public Date getDeploymentTime()
   {
      return new Date(deploymentTime);
   }

   public String getDeploymentComment()
   {
      return deploymentComment;
   }

   public String toString()
   {
      return "Deployment: " + getDeployer().getAccount() + "@" + getDeploymentTime();
   }
   
   public static long getLastDeployment()
   {
      org.eclipse.stardust.engine.core.persistence.Session dbSession = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);
      if (!(dbSession instanceof Session))
      {
         return 0;
      }
      Session jdbcSession = (Session) dbSession;
      QueryDescriptor query = QueryDescriptor.from(ModelDeploymentBean.class).select(Functions.max(FR__OID));
      ResultSet resultSet = jdbcSession.executeQuery(query);
      try
      {
         resultSet.next();
         return resultSet.getLong(1);
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

   public static void delete(long deployment, Session session)
   {
      ComparisonTerm deploymentPredicate = Predicates.isEqual(FR__OID, deployment);
      session.delete(ModelDeploymentBean.class, deploymentPredicate, false);
   }
}

