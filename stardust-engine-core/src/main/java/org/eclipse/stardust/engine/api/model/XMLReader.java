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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface XMLReader
{
   IModel loadModel(Element node);

   IModel importFromXML(File file);

   IModel importFromXML(InputStream inputStream);

   IModel importFromXML(Reader reader);

   IModel importFromXML(InputSource inputSource);
}
