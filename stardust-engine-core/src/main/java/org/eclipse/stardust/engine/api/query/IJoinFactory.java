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

import org.eclipse.stardust.engine.core.persistence.FieldRef;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.DataFilterExtension;


/**
 * IJoinFactory defines an abstraction layer between {@link DataFilterExtension} and 
 * different query evaluation profiles (Carnot.Engine.Tuning.Query.EvaluationProfile="",dataClusters,inlined)
 * 
 * The way the joins to (structured) data value table are created depends on the evaluation profile.  
 */
public interface IJoinFactory
{
   Join createDataFilterJoins(int dataFilterMode, int index, Class dvClass, FieldRef dvProcessInstanceField);
}
