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

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Filter criterion for matching specific process instances.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see ProcessInstanceQuery#OID
 */
public final class ProcessInstanceFilter implements FilterCriterion
{
   private final Set oids;
   private final boolean includingSubprocesses;

   /**
    * Creates a filter matching the closure of the process instance identified by
    * <code>processInstanceOID</code>.
    *
    * @param processInstanceOID The OID of the process instance to be matched.
    *
    * @see #ProcessInstanceFilter(long, boolean)
    * @see ProcessInstanceQuery#OID
    */
   public ProcessInstanceFilter(long processInstanceOID)
   {
      this(processInstanceOID, true);
   }

   /**
    * Creates a filter matching either the closure of the process instance identified by
    * <code>processInstanceOID</code> or just the process instance itself.
    *
    * @param processInstanceOID The OID of the process instance to be matched.
    * @param includeSubprocesses Flag indicating if the instance closure (including all
    *                            subprocess instances) will be used for matching or not.
    *
    * @see #ProcessInstanceFilter(long)
    * @see ProcessInstanceQuery#OID
    */
   public ProcessInstanceFilter(long processInstanceOID, boolean includeSubprocesses)
   {
      this.oids = new HashSet();
      oids.add(new Long(processInstanceOID));

      this.includingSubprocesses = includeSubprocesses;
   }

   protected ProcessInstanceFilter(Set oids)
   {
      this(oids, true);
   }

   protected ProcessInstanceFilter(Set oids, boolean includingSubprocesses)
   {
      this.oids = new HashSet(oids);
      this.includingSubprocesses = includingSubprocesses;
   }

   /**
    * Returns the OID of the process instance this filter is based on.
    *
    * @return The OID of the filter's process instance.
    *
    * @see #isIncludingSubprocesses()
    */
   public long getOID()
   {
      if (oids.isEmpty())
      {
         throw new IllegalStateException("No instance OIDs available");
      }
      else if (1 == oids.size())
      {
         return ((Long) oids.iterator().next()).longValue();
      }
      else
      {
         throw new IllegalStateException("Multiple instance OIDs available");
      }
   }

   public Set getOids()
   {
      return Collections.unmodifiableSet(oids);
   }

   /**
    * Flag indicating if the filter is matching the instance closure (including all
    * subprocess instances) or just the process instance itself.
    *
    * @return <code>true</code> if the filter is matching the instance closure (including
    *         all subprocess instances), <code>false</code> if just the process instance
    *         itself.
    *
    * @see #getOID()
    */
   public boolean isIncludingSubprocesses()
   {
      return includingSubprocesses;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
   
   public static ProcessInstanceFilter in(Set<Long> oids)
   {
      return new ProcessInstanceFilter(oids);
   }

   public static ProcessInstanceFilter in(Set<Long> oids, boolean includingSubprocesses)
   {
      return new ProcessInstanceFilter(oids, includingSubprocesses);
   }
}
