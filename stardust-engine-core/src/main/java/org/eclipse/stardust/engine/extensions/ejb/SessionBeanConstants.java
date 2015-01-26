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
package org.eclipse.stardust.engine.extensions.ejb;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

public interface SessionBeanConstants
{
   String CREATION_METHOD_PARAMETER_PREFIX = "InitParam";
   String METHOD_PARAMETER_PREFIX = "Param";

   String VERSION_2_X = "sessionBean20"; //$NON-NLS-1$
   String VERSION_3_X = "sessionBean30"; //$NON-NLS-1$
   String VERSION_ATT = PredefinedConstants.ENGINE_SCOPE + "ejbVersion"; //$NON-NLS-1$
}
