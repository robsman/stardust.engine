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
package org.eclipse.stardust.engine.core.preferences;

import java.util.List;

import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;




/**
 * @author sauer
 * @version $Revision: $
 */
public interface IPreferencesPersistenceManager
{
   public static final String PARTITIONS_FOLDER = DocumentRepositoryFolderNames.PARTITIONS_FOLDER;
   public static final String REALMS_FOLDER = DocumentRepositoryFolderNames.REALMS_FOLDER;
   public static final String USERS_FOLDER = DocumentRepositoryFolderNames.USERS_FOLDER;
   public static final String PREFS_FOLDER = DocumentRepositoryFolderNames.PREFS_FOLDER;

   void updatePreferences(Preferences preferences, IPreferencesWriter preferenceWriter);

   Preferences loadPreferences(PreferenceScope scope, String moduleId, String preferencesId,
         IPreferencesReader xmlPreferenceReader);
   
   public List<Preferences> getAllPreferences(ParsedPreferenceQuery evaluatedQuery, IPreferencesReader xmlPreferenceReader);

   
}
