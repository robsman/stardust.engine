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
 * Filter criterion for matching specific activity instances.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see ActivityInstanceQuery#OID
 */
public class ActivityInstanceFilter implements FilterCriterion
{
   private final long oid;

   /**
    * Creates a filter matching the activity instance identified by
    * <code>activityInstanceOID</code>.
    *
    * @param activityInstanceOID The OID of the activity instance to be matched.
    *
    * @see ActivityInstanceQuery#OID
    */
   public ActivityInstanceFilter(long activityInstanceOID)
   {
      this.oid = activityInstanceOID;
   }

   /**
    * Returns the OID of the activity instance matched by the filter.
    *
    * @return The OID of the activity instance to be matched.
    */
   public long getOID()
   {
      return oid;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
