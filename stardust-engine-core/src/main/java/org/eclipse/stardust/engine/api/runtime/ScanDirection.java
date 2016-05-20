/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.api.runtime;

/**
 * Specifies the direction to search potential target activities from an activity instance
 * for relocation. Possible directions are {@link ScanDirection#FORWARD},
 * {@link ScanDirection#BACKWARD} or {@link ScanDirection#BOTH}
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public enum ScanDirection
{
   /**
    * To retrieve possible target activities for forward transitions to continue the
    * process but skip current activity.
    */
   FORWARD,
   /**
    * To retrieve backward transition targets to repeat a previous executed activity.
    */
   BACKWARD,
   /**
    * To retrieve both transition targets: forward and backward.
    */
   BOTH
}
