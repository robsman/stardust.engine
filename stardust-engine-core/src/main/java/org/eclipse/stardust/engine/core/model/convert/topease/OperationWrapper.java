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
package org.eclipse.stardust.engine.core.model.convert.topease;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 *
 *
 * @author kberberich
 * @version $Revision$
 */
public class OperationWrapper
{
   private String name;
   private String identifier;
   private String description;
   private SystemWrapper system;
   private Vector inputParameter;
   private Vector outputParameter;
   private Vector supportedActivityIds;

   public OperationWrapper(String identifier, String name, String description,
                           SystemWrapper system)
   {
      this.identifier = identifier;
      this.name = name;
      this.description = description;
      this.system = system;
      inputParameter = new Vector();
      outputParameter = new Vector();
      supportedActivityIds = new Vector();
   }

   public void addOutputParameter(ParameterWrapper parameter)
   {
      outputParameter.addElement(parameter);
   }

   public List getOutputParameter()
   {
      return outputParameter;
   }

   public List getInputParameter()
   {
      return inputParameter;
   }

   public void addInputParameter(ParameterWrapper parameter)
   {
      inputParameter.addElement(parameter);
   }

   public Collection getSupportedActivityIds()
   {
      return supportedActivityIds;
   }

   public void addSupportedActivityId(String id)
   {
      supportedActivityIds.addElement(id);
   }

   public String getName()
   {
      return name;
   }

   public SystemWrapper getSystem()
   {
      return system;
   }

   public String getFullName()
   {
      return system.getFullName() + "." + name;
   }

   public String getIdentifier()
   {
      return identifier;
   }

   public String getDescription()
   {
      return description;
   }
}
