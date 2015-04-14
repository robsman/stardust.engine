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
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class CamelTestUtils
{
   private static final transient Logger LOG = LoggerFactory.getLogger(CamelTestUtils.class);

   @Produce(uri = "direct:camelTestUtils")
   private static ProducerTemplate producerTemplate;

   public static Exchange invokeEndpoint(String uri, Exchange exchange, Map<String,Object> headerMap, Object body)
   {
      Message message = new DefaultMessage();
      if( null != headerMap )
      {
          message.setHeaders( headerMap );
      }

      if( null != body )
      {
         message.setBody(body);
      }

      exchange.setIn( message );
      LOG.info("Invoking endpoint URI: "+uri);
      return producerTemplate.send(uri, exchange);
    }
}
