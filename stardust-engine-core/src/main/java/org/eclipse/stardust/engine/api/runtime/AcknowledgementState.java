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
package org.eclipse.stardust.engine.api.runtime;

import org.eclipse.stardust.common.IntKey;

/**
 * Represents the state of an acknowledgement request to a daemon.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AcknowledgementState extends IntKey
{
   /**
    * Request was sent, no response received.
    */
   public static final AcknowledgementState Requested =
         new AcknowledgementState(0, "Response Requested");

   /**
    * Request was sent, positive response received.
    */
   public static final AcknowledgementState RespondedOK =
         new AcknowledgementState(1, "OK");

   /**
    * Request was sent, daemon reported a problem.
    */
   public static final AcknowledgementState RespondedFailure =
         new AcknowledgementState(2, "Failure");

   private AcknowledgementState(int id, String defaultName)
   {
      super(id, defaultName);
   }
}
