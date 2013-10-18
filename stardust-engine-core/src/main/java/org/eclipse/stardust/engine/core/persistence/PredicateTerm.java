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

/**
 * Marker interface for query predicates
 *
 * @author sborn
 * @version $Revision$
 */
public interface PredicateTerm
{
   /**
    * Set a tag to the predicate term. Tags can be used for identifying special terms within a query.
    * As the tag does not need to be unique the applier of the tag needs to take care.
    *
    * @param tag The tag to be set. Previously set tag will be overwritten.
    */
   void setTag(String tag);

   /**
    * The tag which identifies the predicate term.
    *
    * @return The tag previously set by {@link #setTag(String)}
    */
   String getTag();
}
