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
package org.eclipse.stardust.engine.core.persistence;

import java.util.List;

public interface MultiPartPredicateTerm extends PredicateTerm
{
   /**
    * Adds an <code>PredicateTerm</code> to this <code>AddTerm</code>.
    * 
    * @param part The <code>PredicateTerm</code> to be added
    * 
    * @return This <code>AddTerm</code>
    */
   public MultiPartPredicateTerm add(PredicateTerm part);
   
   /**
    * Returns the <code>PredicateTerm</code>s currently hold by this <code>AddTerm</code>.
    * 
    * @return A list <code>PredicateTerm</code>s
    */
   public List<PredicateTerm> getParts();
}
