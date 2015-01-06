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
package org.eclipse.stardust.engine.extensions.transformation.runtime.transformation.xsl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.pojo.utils.JavaAccessPointType;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;


/**
 * 
 */
public class XSLMessageTransformationAccessPointProvider implements
		AccessPointProvider
{
	public static final Logger trace = LogManager
			.getLogger(XSLMessageTransformationAccessPointProvider.class);

	/**
	 * 
	 */
	public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
	{
		Map result = new HashMap();

		result.put("InputMessage", JavaDataTypeUtils
				.createIntrinsicAccessPoint("InputMessage",
						"InoutMessage: java.util.Map", "java.util.Map",
						Direction.IN, false, new String[] {
								JavaAccessPointType.PARAMETER.getId(),
								JavaAccessPointType.class.getName()
						}));
		result.put("OutputMessage", JavaDataTypeUtils
				.createIntrinsicAccessPoint("OutputMessage",
						"OutputMessage: java.util.Map", "java.util.Map",
						Direction.OUT, true, new String[] {
								JavaAccessPointType.PARAMETER.getId(),
								JavaAccessPointType.class.getName()
						}));

		return result.values().iterator();
	}
}
