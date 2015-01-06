package org.eclipse.stardust.engine.extensions.camel.util.client;

import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

/**
 * Classes implementing this interface provide access to a newly created ServiceFactory
 * with a fresh login.
 * 
 * @author JanHendrik.Scheufen
 */
public interface INewServiceFactoryProvider
{

   ServiceFactory getServiceFactory(String user, String password, Map<String, ? > properties);
}
