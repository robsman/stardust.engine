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
package org.eclipse.stardust.engine.core.compatibility.spi.model.gui;

import org.eclipse.stardust.engine.api.model.IModel;

/**
 * @author rsauer
 * @version $Revision$
 * @since 3.1
 */
public abstract class ApplicationPropertiesPanel2 extends ApplicationPropertiesPanel
{
   public abstract void setModel(IModel model);
}