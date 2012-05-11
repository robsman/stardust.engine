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
package org.eclipse.stardust.common.utils.xml;

/**
 * @author rsauer
 * @version $Revision$
 */
public interface XmlProperties
{
   final String NONVALIDATING_SAX_PARSER_FACTORY = "Carnot.Xml.SAX.NonvalidatingParserFactory";

   final String VALIDATING_SAX_PARSER_FACTORY = "Carnot.Xml.SAX.ValidatingParserFactory";

   final String NONVALIDATING_DOM_BUILDER_FACTORY = "Carnot.Xml.DOM.NonvalidatingDocumentBuilderFactory";

   final String VALIDATING_DOM_BUILDER_FACTORY = "Carnot.Xml.DOM.ValidatingDocumentBuilderFactory";

   final String XSLT_TRANSFORMER_FACTORY = "Carnot.Xml.TrAX.TransformerFactory";

   final String XSLT_TRANSFORMER_FACTORY_XALAN = "org.apache.xalan.processor.TransformerFactoryImpl";
}
