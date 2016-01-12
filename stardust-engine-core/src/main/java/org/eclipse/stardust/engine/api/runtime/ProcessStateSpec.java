/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * Specification class that holds information about which activities should be started during spawning of a peer process.
 *
 * @author Florin.Herinean
 */
public class ProcessStateSpec implements Serializable, Iterable<List<String>>
{
   private static final long serialVersionUID = 1L;

   private Set<List<String>> targets = CollectionUtils.newSet();

   /**
    * Adds a new jump target by specifying an array of activity ids representing the jump hierarchy.
    *
    * @param activities the array of activity ids.
    */
   public void addJumpTarget(String... activities)
   {
      if (activities != null)
      {
         addJumpTarget(Arrays.asList(activities));
      }
   }

   /**
    * Adds a new jump target by specifying a list of activity ids representing the jump hierarchy.
    *
    * @param activities the list of activity ids.
    */
   public void addJumpTarget(List<String> activities)
   {
      if (activities != null && !activities.isEmpty())
      {
         targets.add(Collections.unmodifiableList(activities));
      }
   }

   /**
    * Gets an iterator over the jump targets.
    */
   @Override
   public Iterator<List<String>> iterator()
   {
      return targets.iterator();
   }

   /**
    * Utility method to create a ProcessStateSpec for a target consisting of a specific single activity.
    *
    * @param startActivity the jump target.
    * @return the initialized ProcessStateSpec.
    */
   public static ProcessStateSpec simpleSpec(String startActivity)
   {
      ProcessStateSpec spec = new ProcessStateSpec();
      if (startActivity != null)
      {
         spec.addJumpTarget(startActivity);
      }
      return spec;
   }
}
