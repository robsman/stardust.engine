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
package org.eclipse.stardust.engine.ws.processinterface;

import javax.xml.transform.Source;
import javax.xml.ws.BindingType;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.Service.Mode;

/**
 * <p>
 * This class acts as a generic WS endpoint.
 * </p>
 * 
 * @author Nicolas.Werlein, Roland.Stamm
 */
@WebServiceProvider(serviceName = "ProcessInterfaceService", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/pi", portName = "ProcessInterfaceHttpBasicAuth")
@BindingType(value = "http://schemas.xmlsoap.org/wsdl/soap/http")
@ServiceMode(Mode.PAYLOAD)
public class GenericWebServiceProviderHttpBasicAuth extends GenericWebServiceProvider
{   
   public Source invoke(final Source args)
   {
      return super.invoke(args);
   }
}
