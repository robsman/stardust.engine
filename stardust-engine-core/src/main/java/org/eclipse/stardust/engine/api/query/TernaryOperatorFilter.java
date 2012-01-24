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

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.core.persistence.Operator;


/**
 * Ternary filter operator to be applied directly to attributes of items queried for.
 *
 * <p>
 * IMPORTANT: This interface is <em>not</em> intended to be implemented.
 * </p>
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see UnaryOperatorFilter
 * @see BinaryOperatorFilter
 * @see ActivityInstanceQuery
 * @see ProcessInstanceQuery
 * @see LogEntryQuery
 * @see UserQuery
 */
public interface TernaryOperatorFilter extends AttributedScopedFilter
{
   public abstract Operator.Ternary getOperator();
   public abstract Pair getValue();
}
