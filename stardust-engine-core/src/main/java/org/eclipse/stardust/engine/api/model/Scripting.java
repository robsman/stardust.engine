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
package org.eclipse.stardust.engine.api.model;

import java.io.Serializable;

public class Scripting implements Serializable
{
   public static final String JAVA_SCRIPT = "text/javascript";

   public static final String ECMA_SCRIPT = "text/ecmascript";

   public static final String CARNOT_EL = "text/carnotEL";

   private static final long serialVersionUID = 1L;
   
   private String type;
   private String version;
   private String grammar;
   
   public Scripting(String type, String version, String grammar)
   {
      this.type = type;
      this.version = version;
      this.grammar = grammar;
   }

   public String getType()
   {
      return type;
   }

   public String getVersion()
   {
      return version;
   }

   public String getGrammar()
   {
      return grammar;
   }
   
   public boolean isSupported()
   {
      return CARNOT_EL.equals(type) || ECMA_SCRIPT.equals(type) || JAVA_SCRIPT.equals(type);
   }
}
