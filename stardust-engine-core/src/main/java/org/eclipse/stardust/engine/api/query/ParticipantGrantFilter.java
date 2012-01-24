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
 * @deprecated Superseded by
 *             {@link org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter}.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class ParticipantGrantFilter extends ParticipantAssociationFilter
{
   /**
    * @deprecated Use {@link ParticipantAssociationFilter#forModelParticipant(String)}
    *             instead.
    */
   public ParticipantGrantFilter(String participantID)
   {
      super(new LegacyModelParticipant(participantID), false);
   }

   /**
    * @deprecated Use
    *             {@link ParticipantAssociationFilter#forModelParticipant(String, boolean)}
    *             instead.
    */
   public ParticipantGrantFilter(String participantID, boolean recursively)
   {
      super(new LegacyModelParticipant(participantID), recursively);
   }
}
