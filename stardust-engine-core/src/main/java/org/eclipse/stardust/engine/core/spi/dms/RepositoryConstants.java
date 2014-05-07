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
