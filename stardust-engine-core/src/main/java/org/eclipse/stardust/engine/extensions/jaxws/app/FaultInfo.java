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

import java.util.Set;

import javax.xml.namespace.QName;

/**
 * @author fherinean
 * @version $Revision$
 */
public class FaultInfo
{
   private String name;
   private QName qName;
   private QName type;
   private Set xmlTypes;
   private Set defaults;

   public FaultInfo(String name, QName qName, QName type, Set xmlTypes, Set defaults)
   {
      this.name = name;
      this.qName = qName;
      this.type = type;
      this.xmlTypes = xmlTypes;
      this.defaults = defaults;
   }

   public String getName()
   {
      return name;
   }

   public QName getQName()
   {
      return qName;
   }

   public QName getType()
   {
      return type;
   }

   public Set getXmlTypes()
   {
      return xmlTypes;
   }

   public Set getDefaults()
   {
      return defaults;
   }
}
