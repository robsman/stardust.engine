/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;

/**
 * TODO: javadoc
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface TransitionReport extends Serializable
{
   ActivityInstance getSourceActivityInstance();

   ActivityInstance getTargetActivityInstance();
}
