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

/**
 * Interface to be implemented when evaluating a query's order criteria using the
 * visitor pattern approach.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see OrderCriterion#accept
 */
public interface OrderEvaluationVisitor
{
   Object visit(OrderCriteria order, Object context);

   Object visit(AttributeOrder order, Object context);

   Object visit(DataOrder order, Object context);
   
   Object visit(CustomOrderCriterion order, Object context);

   Object visit(RootProcessDefinitionDescriptor rootProcessDefinitionDescriptor, Object context);
}