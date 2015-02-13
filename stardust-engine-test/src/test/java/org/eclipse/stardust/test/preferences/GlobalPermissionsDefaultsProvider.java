/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.preferences;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.preferences.permissions.GlobalPermissionConstants;
import org.eclipse.stardust.engine.core.preferences.permissions.PermissionUtils;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2;
import org.eclipse.stardust.engine.core.spi.preferences.IStaticConfigurationProvider;

import java.util.*;

/**
 * Sets the grant for {@link GlobalPermissionConstants#GLOBAL_MANAGE_DEPUTIES} to all.
 *
 * @author Roland.Stamm
 */
public class GlobalPermissionsDefaultsProvider implements IStaticConfigurationProvider, IStaticConfigurationProvider.Factory {

    private static final IStaticConfigurationProvider INSTANCE = new GlobalPermissionsDefaultsProvider();

    public static final String MODULE_ID = PermissionUtils.PERMISSIONS;

    public static final String PREF_ID = PermissionUtils.GLOBAL_SCOPE;

    public String getModuleId() {
        return MODULE_ID;
    }

    public Map<String, Object> getPreferenceDefaults(String preferencesId) {
        Map<String, Object> map = CollectionUtils.newHashMap();
        // Needs to adhere to internal format:
        // - Even single grants need to be in a List<String>.
        // - The all grant is represented by Authorization2.ALL.
        map.put(GlobalPermissionConstants.GLOBAL_MANAGE_DEPUTIES, Collections.singletonList(Authorization2.ALL));
        return map;
    }

    public List<String> getPreferenceIds() {
        return Collections.singletonList(PREF_ID);
    }

    public IStaticConfigurationProvider getProvider() {
        return INSTANCE;
    }
}