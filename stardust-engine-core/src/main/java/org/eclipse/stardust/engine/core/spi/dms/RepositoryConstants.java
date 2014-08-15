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

import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * Contains constants that are used by all repositories.
 *
 * @author Roland.Stamm
 */
public class RepositoryConstants
{

   /**
    * The character that is used to separate folders and document in path strings.
    * A valid path starts with this separator.
    */
   public static final String PATH_SEPARATOR = "/";

   /**
    * The path of the root folder.
    */
   public static final String ROOT_FOLDER_PATH = PATH_SEPARATOR;

   /**
    * The revision name and revision id of a non versioned {@link Document}.
    */
   public static final String VERSION_UNVERSIONED = "UNVERSIONED";

}
