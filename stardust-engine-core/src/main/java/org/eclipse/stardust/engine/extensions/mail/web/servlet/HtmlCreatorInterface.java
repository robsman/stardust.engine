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
package org.eclipse.stardust.engine.extensions.mail.web.servlet;

import java.io.IOException;
import java.net.URL;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;


public interface HtmlCreatorInterface {
	/*
	 * @param: URL of the responsepage
	 * @param: ActivityInstance of the current processstep
	 * @return: String of Html-Source from the page, which should be rendered
	 * 
	 */
	public String complete(URL url, ActivityInstance activityInstance,WorkflowService workflowService, String outputValue) throws IOException; 
}
