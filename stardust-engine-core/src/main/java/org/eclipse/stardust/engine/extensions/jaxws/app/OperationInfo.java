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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import java.util.List;

/**
 * @author fherinean
 * @version $Revision$
 */
public class OperationInfo
{
   private String name;
   private String SOAPaction;
   private List parameters;
   private ParameterInfo result;
   private List faults;
   private String style;
   private String use;

   public OperationInfo(String name, String SOAPaction, List parameters,
         ParameterInfo result, List faults, String style, String use)
   {
      this.name = name;
      this.SOAPaction = SOAPaction;
      this.parameters = parameters;
      this.result = result;
      this.faults = faults;
      this.style = style;
      this.use = use;
   }

   public String getName()
   {
      return name;
   }

   public String getSOAPAction()
   {
      return SOAPaction;
   }

   public List getParameters()
   {
      return parameters;
   }

   public ParameterInfo getResult()
   {
      return result;
   }

   public List getFaults()
   {
      return faults;
   }

   public String getStyle()
   {
      return style;
   }

   public String getUse()
   {
      return use;
   }

   public String toString()
   {
      return name;
   }
}
