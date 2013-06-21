/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
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
 * Evaluation Policy for specifying layout of result for worklist queries.
 *
 * @author stephan.born
 * @version $Revision: 52557 $
 *
 * @see Query#setPolicy
 */
public final class WorklistLayoutPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;

   /**
    * Predefined policy indicating layout which merges deputy relates AIs into standard layout.
    */
   public static final WorklistLayoutPolicy MERGED_DEPUTY = new WorklistLayoutPolicy(true);

   /**
    * Predefined policy indicating layout which adds separate userWorklist deputy relates AIs.
    */
   public static final WorklistLayoutPolicy SEPARATE_DEPUTY = new WorklistLayoutPolicy(false);
   
   private final boolean merged;

   private WorklistLayoutPolicy(boolean merged)
   {
      this.merged = merged;
   }
   
   public boolean isMerged()
   {
      return merged;
   }
}