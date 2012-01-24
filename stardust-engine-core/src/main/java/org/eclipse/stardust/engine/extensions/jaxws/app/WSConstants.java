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
package org.eclipse.stardust.engine.extensions.jaxws.app;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

public final class WSConstants
{
   public static final String WS_WSDL_URL_ATT = PredefinedConstants.ENGINE_SCOPE + "wsdlUrl";
   public static final String WS_SERVICE_NAME_ATT = PredefinedConstants.ENGINE_SCOPE + "wsServiceName";
   public static final String WS_PORT_NAME_ATT = PredefinedConstants.ENGINE_SCOPE + "wsPortName";
   public static final String WS_OPERATION_NAME_ATT = PredefinedConstants.ENGINE_SCOPE + "wsOperationName";
   public static final String WS_BINDING_STYLE_ATT = PredefinedConstants.ENGINE_SCOPE + "wsStyle";
   public static final String WS_OPERATION_USE_ATT = PredefinedConstants.ENGINE_SCOPE + "wsUse";
   public static final String WS_IMPLEMENTATION_ATT = PredefinedConstants.ENGINE_SCOPE + "wsImplementation";
   public static final String WS_AUTHENTICATION_ATT = PredefinedConstants.ENGINE_SCOPE + "wsAuthentication";
   public static final String WS_VARIANT_ATT = PredefinedConstants.ENGINE_SCOPE + "wsAuthenticationVariant";

   public static final String WS_MAPPING_ATTR_PREFIX = PredefinedConstants.ENGINE_SCOPE + "mapping:";
   public static final String WS_NAMESPACE_ATTR_PREFIX = PredefinedConstants.ENGINE_SCOPE + "namespace:";
   public static final String WS_TEMPLATE_ATTR_PREFIX = PredefinedConstants.ENGINE_SCOPE + "template:";
   
   public static final String WS_ENDPOINT_ADDRESS_ID = PredefinedConstants.ENGINE_SCOPE + "endpointAddress";
   public static final String WS_ENDPOINT_REFERENCE_ID = PredefinedConstants.ENGINE_SCOPE + "endpointReference";
   public static final String WS_AUTHENTICATION_ID = PredefinedConstants.ENGINE_SCOPE + "authentication";

   public static final String WS_BASIC_AUTHENTICATION = "basic";
   public static final String WS_SECURITY_AUTHENTICATION = "ws-security";
   
   public static final String WS_GENERIC_EPR = "generic";
   public static final String WS_CARNOT_EPR = "carnot";

   public static final String WS_PASSWORD_TEXT = "passwordText";
   public static final String WS_PASSWORD_DIGEST = "passwordDigest";
   public static final String WS_XWSS_CONFIGURATION = "xwssConfiguration";

   public static final String PLAINXML_EVALUATOR_CLASS = PredefinedConstants.PLAINXML_EVALUATOR_CLASS;
   
   public static final String STRUCT_POSTFIX = "_struct";
   
   public static final String RUNTIME_ATT = PredefinedConstants.ENGINE_SCOPE + "wsRuntime";
   public static final String JAXWS_RUNTIME = "jaxws";

   private WSConstants() {}
}
