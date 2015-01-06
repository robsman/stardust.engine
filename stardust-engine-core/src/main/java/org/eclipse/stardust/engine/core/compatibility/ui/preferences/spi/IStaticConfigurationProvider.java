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
package org.eclipse.stardust.engine.core.compatibility.ui.preferences.spi;

import java.util.List;
import java.util.Map;

/**
 * An implementor of this interface can provide default preferences for one moduleId and
 * multiple preferenceIds scoped by the moduleId.<br>
 * <br>
 * To publish an implementor to the engine a file named by the interface's factory has to be created in
 * the '/META-INF/services' folder of the jar.<br>
 * In this case: <b>org.eclipse.stardust.engine.core.compatibility.ui.preferences.spi.IStaticConfigurationProvider$Factory</b><br>
 * This file needs to contain the qualified class name of the implementor of the factory interface.<br>
 * <br>
 * This pattern follows the concept of the JDK6 <code>ServiceLoader.</code>
 * 
 * @author sauer
 */
public interface IStaticConfigurationProvider
{

   /**
    * @return the moduleId which the static preferences are provided for
    */
   String getModuleId();
   
   /**
    * @return the preferenceIds for which the static configuration provider provides preferences for.
    */
   List/*<String>*/ getPreferenceIds();
   
   /**
    * @param preferencesId the preferencesId to lookup default preferences for.
    * @return a map containing default preferences for the specified preferencesId.
    */
   Map/*<String, Serializable>*/ getPreferenceDefaults(String preferencesId);

   /**
    * The factory interface for IStaticConfigurationProvider
    */
   interface Factory
   {
      IStaticConfigurationProvider getProvider();
   }
   
}
