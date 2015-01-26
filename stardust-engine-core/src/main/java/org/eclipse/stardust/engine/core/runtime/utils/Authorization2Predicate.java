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
package org.eclipse.stardust.engine.core.runtime.utils;

import java.util.List;

import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.FieldRef;


/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface Authorization2Predicate extends FetchPredicate
{
   public static final String AUTHORIZATION_PREDICATE = "__authorization_predicate__";

   void setFetchPredicate(FetchPredicate delegate);

   boolean addPrefetchDataHints(Query query);

   void setSelectionExtension(int extensionIndex, List<FieldRef> selectExtension);
   
   void check(Object o);
}
