/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.dms;

import java.io.Serializable;
import java.util.Map;

public interface IRepositoryConfiguration extends Serializable
{

   public static final String PROVIDER_ID = "providerId";
   public static final String REPOSITORY_ID = "repositoryId";
   public Map<String, Serializable> getAttributes();

}
