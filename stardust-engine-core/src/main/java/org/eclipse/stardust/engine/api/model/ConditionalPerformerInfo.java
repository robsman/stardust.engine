/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.model;

/**
 * A client view of a workflow conditional performer with base information only.
 * Conditional performers allows to use late bound participant associations, i.e. deriving
 * the concrete participant from process state.
 *
 * @author Florin.Herinean
 * @version $Revision: $
 */
public interface ConditionalPerformerInfo extends ModelParticipantInfo
{

}