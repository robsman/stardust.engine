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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Simple container of basic model information. 
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class ModelInfo implements IModelInfo
{
   /**
    * The id of the model.
    */
   public String id;
   
   /**
    * A list with declared external packages.
    */
   @XmlElementWrapper(name="ExternalPackages", namespace="http://www.wfmc.org/2008/XPDL2.1")
   @XmlElement(name="ExternalPackage", namespace="http://www.wfmc.org/2008/XPDL2.1")
   public List<ExternalPackageInfo> externalPackages;

   @Override
   public String getId()
   {
      return id;
   }
}
