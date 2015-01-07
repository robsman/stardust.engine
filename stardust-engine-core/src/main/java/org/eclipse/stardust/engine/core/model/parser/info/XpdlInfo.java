/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.model.parser.info;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper class to extract information from an XPDL model.
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
@XmlRootElement(name="Package", namespace="http://www.wfmc.org/2008/XPDL2.1")
public final class XpdlInfo extends ModelInfo
{
   @XmlAttribute(name="Id")
   void setId(String id)
   {
      this.id = id;
   }
   
   private XpdlInfo() {}
}
