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
package org.eclipse.stardust.engine.api.ejb3.beans;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.sql.DataSource;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.ejb3.ForkingService;
import org.eclipse.stardust.engine.api.ejb3.TunneledContext;
import org.eclipse.stardust.engine.api.ejb3.interceptors.SessionBeanInvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ManagedService;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;



/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractEjb3ServiceBean implements Ejb3Service
{
	private static final Logger trace = LogManager
			.getLogger(AbstractEjb3ServiceBean.class);

	private static final long serialVersionUID = 1L;

    @Resource
	protected SessionContext sessionContext;
    
    //@Resource(mappedName="java:/AuditTrail.DataSource")
    @Resource(mappedName="jdbc/AuditTrail.DataSource")
    protected DataSource dataSource;
    
    //@Resource(mappedName="java:/jcr/jackrabbit")
    @Resource(mappedName="jcr/ContentRepository")
    protected Object repository;    

    @EJB
    private ForkingService forkingService;
    
	protected Object service;

	private InvocationManager invocationManager;

	protected Class serviceType;
	
	protected Class serviceTypeImpl;
	
	private String serviceTypeName;
	
	
	public AbstractEjb3ServiceBean()
	{

	}

	@PostConstruct
	public void init() 
	{
		
		this.serviceTypeName = serviceType.getName();
		
		prepareInvocationManager(serviceTypeImpl);
		setupServiceProxy();		
	}
	
   
   public LoggedInUser login(String username, String password, Map properties)
   {
	   

         return ((ManagedService) service).login(username, password,
               properties);         
      
   }

   protected Map initInvocationContext(TunneledContext tunneledContext)
   {
      if (null != tunneledContext)
      {
         if (null != tunneledContext.getInvokerPrincipal())
         {
            InvokerPrincipal principalBackup = InvokerPrincipalUtils.setCurrent(tunneledContext.getInvokerPrincipal());
            
            if (null != principalBackup)
            {
               return Collections.singletonMap(InvokerPrincipal.class.getName(), principalBackup);
            }
         }
      }
      
      return null;
   }

   protected void clearInvocationContext(TunneledContext tunneledContext, Map contextBackup)
   {
      if (null != tunneledContext)
      {
         if (null != tunneledContext.getInvokerPrincipal())
         {
            InvokerPrincipal backup = (null != contextBackup)
            ? (InvokerPrincipal) contextBackup.get(InvokerPrincipal.class.getName())
                  : null;
            
            if (null != backup)
            {
               InvokerPrincipalUtils.setCurrent(backup);
            }
            else
            {
               InvokerPrincipalUtils.removeCurrent();
            }
         }
      }
   }
   
   private void prepareInvocationManager(Class serviceImplType)
   {
      Object serviceInstance = null;
      try
      {
         serviceInstance = serviceImplType.newInstance();
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
      invocationManager = new SessionBeanInvocationManager(sessionContext, this,
            serviceInstance, serviceTypeName);
   }   
   
   
   private void setupServiceProxy()
   {
      ClassLoader classLoader = getClass().getClassLoader();
      try
      {
         Class serviceType = classLoader.loadClass(serviceTypeName);
         this.service = Proxy.newProxyInstance(classLoader, new Class[] {
               serviceType, ManagedService.class}, invocationManager);
      }
      catch (ClassNotFoundException e)
      {
         throw new PublicException("Failed loading service interface class.", e);
      }
   } 
   
   public DataSource getDataSource()
   {
	   return this.dataSource;
   }
   
   public ForkingService getForkingService()
   {
	   return this.forkingService;
   }
   
   public Object getRepository()
   {
	   return this.repository;
   }

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}
   
   

}
