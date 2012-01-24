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
package org.eclipse.stardust.engine.core.repository;

import org.eclipse.stardust.common.StringKey;

/**
 * @author sauer
 * @version $Revision: $
 */
public class RepositorySpaceKey extends StringKey
{
   
   public static final RepositorySpaceKey SKINS = new RepositorySpaceKey("skins");
   
   public static final RepositorySpaceKey CONTENT = new RepositorySpaceKey("content");
   
   public static final RepositorySpaceKey BUNDLES = new RepositorySpaceKey("bundles");
   
   private static final long serialVersionUID = 1L;

   public RepositorySpaceKey(String id)
   {
      super(id, id);
   }

}
