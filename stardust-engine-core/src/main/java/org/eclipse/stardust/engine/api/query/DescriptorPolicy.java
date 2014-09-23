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
import java.util.TreeSet;

/**
 * Policy for specifying retrieval of descriptors.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class DescriptorPolicy implements EvaluationPolicy
{
   private static final long serialVersionUID = 1L;

   private final boolean includeDescriptors;

   private Set<String> descriptorIds;

   /**
    * Ships descriptor values with process instance details.
    */
   public static final DescriptorPolicy WITH_DESCRIPTORS = new DescriptorPolicy(true);

   /**
    * Does not ship descriptor values with process instance details.
    */
   public static final DescriptorPolicy NO_DESCRIPTORS = new DescriptorPolicy(false);

   public static DescriptorPolicy withIds(Set<String> descriptorIds)
   {
      return new DescriptorPolicy(descriptorIds);
   }

   private DescriptorPolicy(boolean includeDescriptors)
   {
      this.includeDescriptors = includeDescriptors;
   }

   private DescriptorPolicy(Set<String> descriptorIds)
   {
      this.includeDescriptors = true;
      this.descriptorIds = new TreeSet<String>(descriptorIds);
   }

   public Set<String> getDescriptorIds()
   {
      return descriptorIds;
   }

   public boolean includeDescriptors()
   {
      return includeDescriptors;
   }
}
