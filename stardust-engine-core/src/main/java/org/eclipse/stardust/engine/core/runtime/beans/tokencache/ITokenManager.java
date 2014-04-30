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
package org.eclipse.stardust.engine.core.runtime.beans.tokencache;

import org.eclipse.stardust.engine.api.model.ITransition;
import org.eclipse.stardust.engine.core.runtime.beans.TransitionTokenBean;

public interface ITokenManager
{
   public void registerToken(ITransition transition, TransitionTokenBean token);
   public boolean removeToken(TransitionTokenBean token);
   public TransitionTokenBean lockFirstAvailableToken(ITransition transition);
   public void flush();
   public TransitionTokenBean getTokenForTarget(ITransition transition, long targetActivityInstanceOid);
   public TransitionTokenBean lockSourceAndOtherToken(TransitionTokenBean token);
}
