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

import java.util.Map;

import javax.swing.*;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class TriggerPropertiesPanel extends JPanel
{
   protected Map triggerTypeAttributes;

   public void setTriggerTypeAttributes(Map attributes)
   {
      triggerTypeAttributes = attributes;
   }

   public Object getTriggerTypeAttribute(String name)
   {
      return triggerTypeAttributes.get(name);
   }

   public abstract void setData(IProcessDefinition processDefinition, ITrigger trigger);

   public abstract void apply();

   public abstract void validateSettings() throws ValidationException;
}
