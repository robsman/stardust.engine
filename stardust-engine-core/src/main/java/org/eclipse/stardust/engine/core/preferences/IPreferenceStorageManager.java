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

import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.ReconfigurationInfo;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;


public interface IPreferenceStorageManager
{
   public static final String PRP_USE_DOCUMENT_REPOSITORY = "Carnot.Configuration.UseDocumentRepository";

   public Preferences getPreferences(PreferenceScope scope, String moduleId,
         String preferenceId);

   public Preferences getPreferences(IUser user, PreferenceScope scope, String moduleId,
         String preferenceId);
   
   public List<ReconfigurationInfo> savePreferences(Preferences preferences, boolean force);

   public List<Preferences> getAllPreferences(PreferenceQuery preferenceQuery, boolean checkPermissions);

   public void flushCaches();

}