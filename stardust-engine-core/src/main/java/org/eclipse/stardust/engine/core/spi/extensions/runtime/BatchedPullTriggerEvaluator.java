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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.util.Iterator;

import org.eclipse.stardust.engine.api.model.ITrigger;


/**
 * @author rsauer
 * @version $Revision$
 * 
 * @since 3.1
 */
public interface BatchedPullTriggerEvaluator
{
   /**
    * Performs the actual evaluation of a trigger at runtime, resulting in sequence of
    * {@link TriggerMatch} instances.
    *  
    * @param trigger The trigger definition to be evaluated.
    * @param batchSize The maximum number of events to be generated.
    * 
    * @return An iterator over the evaluated sequence of trigger events.
    * 
    * @since 3.1
    */
   Iterator getMatches(ITrigger trigger, long batchSize);
}
