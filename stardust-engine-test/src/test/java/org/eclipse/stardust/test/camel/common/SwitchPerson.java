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

import java.util.HashMap;
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
public class SwitchPerson implements Processor
{
   @Override
   public void process(Exchange exchange) throws Exception
   {
      @SuppressWarnings("unchecked")
      Map<String, Object> inBody = (Map<String, Object>) exchange.getIn().getBody();
      inBody.get("firstName");

      Map<String, Object> outBody = new HashMap<String, Object>();
      outBody.put("lastName", inBody.get("firstName"));
      outBody.put("firstName", inBody.get("lastName"));

      exchange.getOut().setHeaders(exchange.getIn().getHeaders());
      exchange.getOut().setBody(outBody);
   }
}
