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
package org.eclipse.stardust.engine.api.query;

/**
 * Evaluation policy affecting query execution in a multi-version model environment. Can
 * be used to restrict query evaluation involving model elements to only consider the
 * currently active model version.
 *
 * @author rsauer
 * @version $Revision$
 */
public class ModelVersionPolicy implements EvaluationPolicy
{
   private final boolean restrictedToActiveModel;

   /**
    * Initializes a policy to either restrict query evaluation to only the active model
    * version or not.
    *
    * @param restrictedToActiveModel Flag indicating if query evaluation will only
    *                                consider the active model version or not.
    */
   public ModelVersionPolicy(boolean restrictedToActiveModel)
   {
      this.restrictedToActiveModel = restrictedToActiveModel;
   }

   /**
    * Indicates if this policy is restricting query evaluation to the active model version
    * or not.
    *
    * @return <code>true</code> if query evaluation is restricted to the active model
    *         version, <code>false</code> if not.
    */
   public boolean isRestrictedToActiveModel()
   {
      return restrictedToActiveModel;
   }
}
