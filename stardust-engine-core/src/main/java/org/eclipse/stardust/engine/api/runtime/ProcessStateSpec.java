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
 *
 * @author Florin.Herinean
 */
public class ProcessStateSpec implements Serializable, Iterable<List<String>>
{
   private static final long serialVersionUID = 1L;

   private Set<List<String>> targets = CollectionUtils.newSet();

   public void addJumpTarget(String... activities)
   {
      if (activities != null)
      {
         addJumpTarget(Arrays.asList(activities));
      }
   }

   public void addJumpTarget(List<String> activities)
   {
      if (activities != null && !activities.isEmpty())
      {
         targets.add(Collections.unmodifiableList(activities));
      }
   }

   public static ProcessStateSpec simpleSpec(String startActivity)
   {
      ProcessStateSpec spec = new ProcessStateSpec();
      if (startActivity != null)
      {
         spec.addJumpTarget(startActivity);
      }
      return spec;
   }

   @Override
   public Iterator<List<String>> iterator()
   {
      return targets.iterator();
   }
}
