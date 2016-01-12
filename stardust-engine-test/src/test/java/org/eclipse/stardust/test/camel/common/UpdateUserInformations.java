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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TODO JavaDoc
 * </p>
 *
 * @author Sabri.Bousselmi
 */
public class UpdateUserInformations implements Processor
{
   private static final transient Logger logger = LoggerFactory.getLogger(UpdateUserInformations.class);

   @Override
   public void process(Exchange exchange) throws Exception
   {
      String person = exchange.getIn().getBody(String.class);
      logger.info("old exchange = " + person);
      person = "{\"creditCard\":{\"creditCardNumber\":411152,\"creditCardType\":\"MasterCard\"},\"lastName\":\"Last Name Updated In Addtional Bean\",\"firstName\":\"First Name Updated In Addtional Bean\"}";
      exchange.getIn().setBody(person);
   }
}
