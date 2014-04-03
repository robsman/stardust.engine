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
package org.eclipse.stardust.engine.core.preferences.configurationvariables;

/**
 * A ConfigurationVariableScope defines scoping of a configuration-variable.
 * 
 * @author barry.grotjahn
 */
public enum ConfigurationVariableScope
{   
   /**
    * The STRING scope is the default scope.
    */
   String,
   /**
    * The PASSWORD scope is used in security context.
    */
   Password
}