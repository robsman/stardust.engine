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
package org.eclipse.stardust.engine.api.ejb3;

import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.utils.ejb.EJBUtils;
import org.eclipse.stardust.engine.api.ejb3.beans.Ejb3Service;
import org.eclipse.stardust.engine.api.runtime.ServiceNotAvailableException;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;

/**
 * @author ubirkemeyer
 * @version $Revision: 60059 $
 */
public class InternallyAuthentifiedSecureSessionFactory implements
		SecureSessionFactory, TunnelingAwareSecureSessionFactory {

	public Object get(String jndiName, Class homeClass, Class remoteClass,
			Class[] creationArgTypes, final Object[] creationArgs,
			Map credentials, Map properties) {
		// invoke new contract, to determine if deployment uses tunneling or not
		SecureSession result = getSecureSession(jndiName, homeClass,
				remoteClass, creationArgTypes, creationArgs, credentials,
				properties);

		if (null != result.tunneledContext) {
			// no way to propagate tunneled context via old contract
			throw new ServiceNotAvailableException(
					"Service is only available for tunneling invocations, but service factory does not seem to be aware.");
		}

		return result.endpoint;
	}

	public SecureSession getSecureSession(String jndiName, Class homeClass,
			Class remoteClass, Class[] creationArgTypes, Object[] creationArgs,
			Map credentials, Map properties)
			throws ServiceNotAvailableException {
		// work for florin
		// bug #3099: extract invocation of login method in a separate try/catch
		Object endpoint = null;

		TunneledContext tunneledContext = null;

		// first obtain the service.
		try {
			Context context = EJBUtils.getInitialContext(false, false);
			Object rawService = context.lookup(jndiName);
			LogUtils.traceObject(rawService, false);

			endpoint = rawService;
			
			homeClass = TunnelingUtils.getTunnelingHomeClass(homeClass);
			LogUtils.traceObject(homeClass, false);

			LogUtils.traceObject(endpoint, false);
		} catch (NamingException e) {
			throw new ServiceNotAvailableException(e.getMessage(), e);
		} catch (Exception e) {
			throw new InternalException("Failed to create session bean.", e);
		}

		// now attempt to login
		try {

			tunneledContext = TunnelingUtils.performTunnelingLogin(
					(Ejb3Service) endpoint,
					(String) credentials.get(SecurityProperties.CRED_USER),
					(String) credentials.get(SecurityProperties.CRED_PASSWORD),
					properties);

		} catch (Exception e) {
			throw ClientInvocationHandler.unwrapException(e);
		}

		return new SecureSession(endpoint, tunneledContext);
	}
}
