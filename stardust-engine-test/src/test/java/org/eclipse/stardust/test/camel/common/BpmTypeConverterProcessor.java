/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.camel.common;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class BpmTypeConverterProcessor implements Processor
{
   @Override
   public void process(Exchange exchange) throws Exception
   {
      Object inBody = exchange.getIn().getBody();
      exchange.getOut().setBody(inBody);

      Map<String, Object> headers = exchange.getIn().getHeaders();
      exchange.getOut().setHeaders(headers);
   }
}
