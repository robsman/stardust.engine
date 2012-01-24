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
package org.eclipse.stardust.engine.core.model.convert.aris;

import java.io.InputStream;
import java.util.List;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.model.convert.Converter;
import org.w3c.dom.Document;


/**
 * @author kberberich
 * @version $Revision$
 */
public class Aris62Converter extends Converter
{
   private int mode;

   public Aris62Converter(int mode)
   {
      super();
      this.mode = mode;
   }

   public IModel convert(InputStream inputStream)
   {
      Document document = getDocumentFromInputStream(inputStream);
      AML aml = new AML(document);
      model = aml.createModel(mode);
      List errors = aml.getErrors();
      if (errors != null)
      {
         throw new ValidationException("Could not convert model from ARIS " + aml.getVersion() + " to CARNOT.", errors, false);
      }
      return model;
   }
}
