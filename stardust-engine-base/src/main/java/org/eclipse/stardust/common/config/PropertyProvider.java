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
package org.eclipse.stardust.common.config;

import java.util.Map;

/**
 * PropertyProvider provides properties. 
 * 
 * In an spring context, properties are provided in the usual spring style.
 * The CARNOT service beans are defined in carnot-spring-context.xml resource file.
 * Additional key-value items should be added as a Properties object for the
 * 'carnotProperties' field of the beans like in the example below:
 * <pre>
 * &lt;bean id="carnotWorkflowService"
 *     class="org.eclipse.stardust.engine.api.spring.WorkflowServiceBean"&gt;
 *     ...
 *     &lt;property name="carnotProperties"&gt;
 *         &lt;props&gt;
 *             &lt;prop key="name"&gt;value&lt;/prop&gt;
 *             ...
 *         &lt;/props&gt;
 *     &lt;/property&gt;
 * &lt;/bean&gt; 
 * </pre>
 * @author rsauer
 * @version $Revision$
 */
public interface PropertyProvider
{
   Map<String, Object> getProperties();
   
   String getPropertyDisplayValue(String key);
}
