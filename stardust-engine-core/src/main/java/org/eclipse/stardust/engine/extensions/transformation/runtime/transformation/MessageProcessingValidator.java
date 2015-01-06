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
package org.eclipse.stardust.engine.extensions.transformation.runtime.transformation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;


/**
 * 
 */
public class MessageProcessingValidator implements ApplicationValidator
{
	public List validate(Map attributes, Map typeAttributes,
				Iterator accessPoints)
	{
		return Collections.EMPTY_LIST;
	}
}
