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
package org.eclipse.stardust.engine.core.struct.sxml;

/**
 * Node identified by name and namespace URI
 *
 * @author robert.sauer
 */
public interface NamedNode
{
   /**
    * @return the local name of this node
    */
   public abstract String getLocalName();

   /**
    * @return the prefix qualified name of this node
    */
   public abstract String getQualifiedName();

   /**
    * @return the namespace URI of this node
    */
   public abstract String getNamespaceURI();

   /**
    * @return the namespace prefix of this node
    */
   public abstract String getNamespacePrefix();
}