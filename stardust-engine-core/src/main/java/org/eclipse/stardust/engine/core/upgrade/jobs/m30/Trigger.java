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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class Trigger extends IdentifiableElement
{
   private String type;
   private Vector mappings = new Vector();
   private Vector accessPoints = new Vector();
   private Model model;

   public Trigger(String type, String id, String name, int oid, Model model)
   {
      super(id, name, null);
      this.type = type;
      this.model = model;
      model.register(this, oid);
   }

   public ParameterMapping createParameterMapping(String data, String parameter, String parameterPath, int oid)
   {
      ParameterMapping result = new ParameterMapping(data, parameter, parameterPath, oid, model);
      mappings.add(result);
      return result;
   }

   public void addAccessPoint(AccessPoint ap)
   {
      accessPoints.add(ap);
   }

   public String getType()
   {
      return type;
   }

   public Iterator getAllAccessPoints()
   {
      return accessPoints.iterator();
   }

   public Iterator getAllParameterMappings()
   {
      return mappings.iterator();
   }

   public String toString()
   {
      return "Trigger: " + getName();
   }
}
