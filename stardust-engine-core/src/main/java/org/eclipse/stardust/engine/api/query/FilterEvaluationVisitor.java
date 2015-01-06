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
 * Interface to be implemented when evaluating a query's filter criteria using the
 * visitor pattern approach.
 *
 * @author rsauer
 * @version $Revision$
 *
 * @see FilterCriterion#accept
 */
public interface FilterEvaluationVisitor
{
   Object visit(FilterTerm filter, Object context);

   Object visit(UnaryOperatorFilter filter, Object context);

   Object visit(BinaryOperatorFilter filter, Object context);

   Object visit(TernaryOperatorFilter filter, Object context);

   Object visit(ProcessDefinitionFilter filter, Object context);

   Object visit(ProcessStateFilter filter, Object context);

   Object visit(ProcessInstanceFilter filter, Object context);

   Object visit(StartingUserFilter filter, Object context);

   Object visit(ActivityFilter filter, Object context);

   Object visit(ActivityInstanceFilter filter, Object context);

   Object visit(ActivityStateFilter filter, Object context);

   Object visit(PerformingUserFilter filter, Object context);

   Object visit(PerformingParticipantFilter filter, Object context);

   Object visit(PerformingOnBehalfOfFilter filter, Object context);

   Object visit(PerformedByUserFilter filter, Object context);

   Object visit(AbstractDataFilter filter, Object context);

   Object visit(ParticipantAssociationFilter filter, Object context);

   Object visit(CurrentPartitionFilter filter, Object context);

   Object visit(UserStateFilter filter, Object context);

   Object visit(ProcessInstanceLinkFilter filter, Object context);

   Object visit(ProcessInstanceHierarchyFilter filter, Object context);

   Object visit(DocumentFilter filter, Object context);
}
