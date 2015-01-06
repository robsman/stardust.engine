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
package org.eclipse.stardust.engine.api.model;

import java.util.List;
import java.util.Set;

/**
 * The client view of a workflow activity.
 * <p>An activity is a piece of work, which will be processed by a combination of resource
 * (specified by participant assignment) and/or computer applications (specified by
 * application assignment), forming one logical step in the realization of the process.</p>
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface Activity extends ModelElement, EventAware
{
   /**
    * Enumeration of gateway types.
    */
   public enum GatewayType
   {
      /**
       * AND gateway, meaning that the activity will be executed only after all
       * incomming transitions have been followed. After completion of the activity,
       * all outgoing transitions will be followed.
       */
      And,

      /**
       * OR gateway (inclusive), meaning that the activity will be executed only after all
       * possible incomming transitions have been followed. After completion of the activity,
       * all outgoing transitions having conditions that evaluates to true will be followed.
       */
      Or,

      /**
       * XOR gateway (exclusive), meaning that the activity will be executed for each
       * incomming transition. After completion of the activity, first outgoing transition
       * having condition that evaluates to true will be followed.
       */
      Xor
   }

   /**
    * Gets the flow join type for this activity.
    * <p>Null if there is no join.</p>
    */
   GatewayType getJoinType();

   /**
    * Gets the flow split type for this activity.
    * <p>Null if there is no split.</p>
    */
   GatewayType getSplitType();

   /**
    * TODO
    */
   boolean isQualityAssuranceEnabled();

   /**
    * TODO
    */
   ModelParticipant getQualityAssurancePerformer();

   /**
    * TODO
    */
   int getDefaultQualityAssuranceProbability();

   /**
    * TODO
    */
   String getQualityAssuranceFormula();

   /**
    * TODO
    */
   Set<QualityAssuranceCode> getAllQualityAssuranceCodes();

   /**
    * Gets the runtime OID of the model element.
    * <p>
    * Contrary to the element OID, runtime element OIDs are guaranteed to be stable over
    * model versions for model elements of same type and identical fully qualified IDs.
    * </p>
    *
    * <p>
    * The fully qualified ID of a model element consists of the concatenation of the fully
    * qualified element ID of its parent element, if existent, and the element ID.
    * </p>
    *
    * @return the runtime model element OID
    *
    * @see ModelElement#getElementOID()
    */
   long getRuntimeElementOID();

   /**
    * Gets the implementation type of this activity.
    *
    * @return the implementation type.
    */
   ImplementationType getImplementationType();

   /**
    * Checks if this activity can be aborted.
    *
    * @return true if the activity can be aborted.
    */
   boolean isAbortable();

   /**
    * Checks if this activity is interactive.
    *
    * @return true if the activity is manual or executes an interactive application.
    */
   boolean isInteractive();

   /**
    * Gets the id of the process definition containing this activity.
    *
    * @return the id of the containing process definition.
    */
   String getProcessDefinitionId();

   /**
    * Gets the noninteractive application executed by this activity, if any
    *
    * @return the noninteractive application executed by this activity,
    *         or null if there is no application to be executed.
    */
   Application getApplication();

   /**
    * Gets all interactive application contexts available for this activity.
    *
    * @return a List of {@link ApplicationContext} objects.
    */
   List<ApplicationContext> getAllApplicationContexts();

   /**
    * Gets the specified application context.
    *
    * @param id the ID of the application context (possible IDs include
    * {@link PredefinedConstants#DEFAULT_CONTEXT}, {@link PredefinedConstants#ENGINE_CONTEXT}
    * and {@link PredefinedConstants#APPLICATION_CONTEXT})
    *
    * @return the application context or null if no application context with the specified id was found.
    */
   ApplicationContext getApplicationContext(String id);

   /**
    * Gets the participant assigned as a performer to the activity in the workflow model.
    * <p />
    * If the activity is not interactive or no default performer is assigned,
    * <code>null</code> is returned.
    *
    * @return The default performer as defined in the model.
    */
   ModelParticipant getDefaultPerformer();

   /**
    * Gets the ID of the role or organization assigned as a performer to the activity in
    * the workflow model.
    * <p>
    * If no performer or a conditional performer is assigned, <code>null</code> is
    * returned.
    * </p>
    *
    * @deprecated Superseded by {@link #getDefaultPerformer()}. The old behavior was to
    *             silently resolve conditional performers if possible and return the ID of
    *             the resolved participant. Migration to the new API probably requires
    *             testing for {@link ConditionalPerformer} and a call to
    *             {@link ConditionalPerformer#getResolvedPerformer()} instead.
    */
   String getDefaultPerformerID();

   /**
    * Gets the name of the role or organization assigned as a performer to the activity in
    * the workflow model.
    * <p>
    * If no performer or a conditional performer is assigned, <code>null</code> is
    * returned.
    * </p>
    *
    * @deprecated Superseded by {@link #getDefaultPerformer()}. The old behavior was to
    *             silently resolve conditional performers if possible and return the name
    *             of the resolved participant. Migration to the new API probably requires
    *             testing for {@link ConditionalPerformer} and a call to
    *             {@link ConditionalPerformer#getResolvedPerformer()} instead.
    */
   String getDefaultPerformerName();

   /**
    * Retrieves a reference to an external application or process definition.
    *
    * @return the reference
    */
   Reference getReference();

   /**
    * Gets the id of the process definition this activity will start as subprocess.
    *
    * @return the id of the process definition
    */
   String getImplementationProcessDefinitionId();

   /**
    * Gets the qualified id of the process definition this activity will start as subprocess.
    *
    * @return the qualified id of the process definition in the form '{<namespace>}<processId>'
    */
   String getQualifiedImplementationProcessDefinitionId();
}
