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
package org.eclipse.stardust.engine.core.spi.query;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;

/**
 * @author rsauer
 * @version $Revision$
 */
public class CustomActivityInstanceQuery extends ActivityInstanceQuery
{
   private final String queryKind;

   public CustomActivityInstanceQuery(String queryKind)
   {
      this.queryKind = queryKind;
   }

   public String getQueryId()
   {
      return queryKind;
   }
}
