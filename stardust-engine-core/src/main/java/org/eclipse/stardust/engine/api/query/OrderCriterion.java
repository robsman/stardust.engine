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
 * Interface to be implemented by custom order criteria.
 *
 * <p>Order criteria are used to define a specific order of elements returned by a query
 * and can be thought of an analogon to an SQL ORDER-BY-clause.</p>
 *
 * @author rsauer
 * @version $Revision$
 */
public interface OrderCriterion extends Serializable
{
   /**
    * Visitor dispatch callback used for evaluating order criteria. Usually implemented
    * as <code>return visitor.visit(this, context)</code>, thus calling the appropriately
    * overloaded visitation method.
    *
    * @param visitor The visitor performing the evaluation.
    * @param context Information used by the visitor during the visitation process.
    * @return Visitor specific result of the visitation.
    */
   Object accept(OrderEvaluationVisitor visitor, Object context);
}
