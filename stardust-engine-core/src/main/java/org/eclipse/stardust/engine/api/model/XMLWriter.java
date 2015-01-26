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

import org.w3c.dom.Document;

import java.io.File;
import java.io.OutputStream;

import javax.xml.transform.Transformer;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface XMLWriter
{
   void exportAsXML(IModel model, File file);

   void exportAsXML(IModel model, OutputStream stream);

   void exportAsXML(IModel model, Transformer transformation, OutputStream stream);

   void exportAsXML(IModel model, Document document);
}
