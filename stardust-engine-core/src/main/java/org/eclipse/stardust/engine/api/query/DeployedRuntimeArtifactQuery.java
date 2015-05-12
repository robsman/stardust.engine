/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.query;

import java.util.Date;

import org.eclipse.stardust.engine.core.runtime.beans.RuntimeArtifactBean;

/**
 *
 * @author Roland.Stamm
 */
public class DeployedRuntimeArtifactQuery extends Query
{
   private static final long serialVersionUID = 399762422480557626L;
   public static final Attribute OID = new Attribute(RuntimeArtifactBean.FIELD__OID);
   public static final Attribute ARTIFACT_TYPE_ID = new Attribute(RuntimeArtifactBean.FIELD__ARTIFACT_TYPE_ID);
   public static final Attribute ARTIFACT_ID = new Attribute(RuntimeArtifactBean.FIELD__ARTIFACT_ID);
   public static final Attribute ARTIFACT_NAME = new Attribute(RuntimeArtifactBean.FIELD__ARTIFACT_NAME);
   public static final Attribute VALID_FROM = new Attribute(RuntimeArtifactBean.FIELD__VALID_FROM);

   private static final FilterVerifier FILTER_VERIFIER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[]
         {
            FilterTerm.class,
            UnaryOperatorFilter.class,
            BinaryOperatorFilter.class,
            TernaryOperatorFilter.class,
            CurrentPartitionFilter.class
         }),
         DeployedRuntimeArtifactQuery.class);
   private boolean includeOnlyActive;

   /**
    * Creates a query for finding all runtime artifacts
    *
    * @return The readily configured query.
    */
   public static DeployedRuntimeArtifactQuery findAll()
   {
      DeployedRuntimeArtifactQuery query = new DeployedRuntimeArtifactQuery();

      return query;
   }

   /**
    * Creates a query for finding all currently active runtime artifacts ordered descending by a given date.
    *
    * @return The readily configured query.
    */
   public static DeployedRuntimeArtifactQuery findAllActive(Date activeAt)
   {
      DeployedRuntimeArtifactQuery query = new DeployedRuntimeArtifactQuery(true);

      query.where(VALID_FROM.lessOrEqual(activeAt.getTime()));

      return query;
   }

   /**
    * Creates a query for finding active runtime artifacts of a specified type ordered descending by a given date.
    *
    * @return The readily configured query.
    */
   public static DeployedRuntimeArtifactQuery findActive(String artifactType, Date activeAt)
   {
      DeployedRuntimeArtifactQuery query = findAllActive(activeAt);

      query.where(ARTIFACT_TYPE_ID.isEqual(artifactType));

      return query;
   }

   /**
    * Creates a query for finding the active runtime artifact specified by artifact id and of the specified type.
    *
    * @return The readily configured query.
    */
   public static DeployedRuntimeArtifactQuery findActive(String artifactId, String artifactType, Date activeAt)
   {
      DeployedRuntimeArtifactQuery query = findActive(artifactType, activeAt);

      query.where(ARTIFACT_ID.isEqual(artifactId));

      return query;
   }

   public DeployedRuntimeArtifactQuery()
   {
      this(false);
   }

   protected DeployedRuntimeArtifactQuery(boolean includeOnlyActive)
   {
      super(FILTER_VERIFIER);
      this.includeOnlyActive = includeOnlyActive;
      setPolicy(new ModelVersionPolicy(false));

   }

   public boolean isIncludeOnlyActive()
   {
      return includeOnlyActive;
   }

   /**
    * Log entry attribute supporting filter operations.
    * <p />
    * Not for direct use.
    *
    */
   public static final class Attribute extends FilterableAttributeImpl
   {
      private static final long serialVersionUID = -1404154020427509685L;

      private Attribute(String attribute)
      {
         super(DeployedRuntimeArtifactQuery.class, attribute);
      }
   }
}