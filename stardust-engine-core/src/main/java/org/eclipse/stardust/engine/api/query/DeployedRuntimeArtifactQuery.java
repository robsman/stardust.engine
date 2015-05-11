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
   public static final Attribute ARTIFACT_TYPE = new Attribute(RuntimeArtifactBean.FIELD__ARTIFACT_TYPE_ID);
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
    * Creates a query for finding runtime artifacts of a specified type ordered descending by a given date.
    * The first occurrence for an artifactId is the valid artifact at the specified date,
    * further occurrences of the same artifactId are the predecessor artifacts that are active before.
    *
    * @return The readily configured query.
    */
   public static DeployedRuntimeArtifactQuery findActiveBefore(String artifactType, Date activeAt)
   {
      DeployedRuntimeArtifactQuery query = new DeployedRuntimeArtifactQuery();

      query.where(ARTIFACT_TYPE.isEqual(artifactType)).and(VALID_FROM.lessOrEqual(activeAt.getTime()));

      query.orderBy(VALID_FROM, false);
      query.orderBy(OID, false);

      return query;
   }

   /**
    * Creates a query for finding runtime artifacts of a specified type ordered descending by a given date.
    * The first occurrence for an artifactId is the valid artifact at the specified date,
    * further occurrences of the same artifactId are the predecessor artifacts that are active before.
    *
    * @return The readily configured query.
    */
   public static DeployedRuntimeArtifactQuery findActiveBefore(String artifactType, String artifactId, Date activeAt)
   {
      DeployedRuntimeArtifactQuery query = findActiveBefore(artifactType, activeAt);

      query.where(ARTIFACT_ID.isEqual(artifactId));

      return query;
   }

   public DeployedRuntimeArtifactQuery()
   {
      super(FILTER_VERIFIER);
      setPolicy(new ModelVersionPolicy(false));
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