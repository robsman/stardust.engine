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

import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * Filter that reduces the query result to elements of the current partition.
 * 
 * @author sborn
 * @version $Revision$
 */
public class CurrentPartitionFilter implements FilterCriterion
{
   private Class type;
   
   public CurrentPartitionFilter(Class type)
   {
      this.type = type;
   }
   
   public short getPartitionOid()
   {
      return SecurityProperties.getPartitionOid();
   }
   
   public Class getType()
   {
      return type;
   }

   public Object accept(FilterEvaluationVisitor visitor, Object context)
   {
      return visitor.visit(this, context);
   }
}
