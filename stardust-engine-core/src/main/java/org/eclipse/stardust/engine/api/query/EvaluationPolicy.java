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
package org.eclipse.stardust.engine.api.query;

import java.io.Serializable;

/**
 * Interface to be implemented by custom query evaluation policies.
 *
 * <p>An evaluation policy selects (parts of) the strategy for query evaluation, i.e.
 * which model version to use or what subset to deliver.</p>
 *
 * @author rsauer
 * @version $Revision$
 */
public interface EvaluationPolicy extends Serializable
{
}
