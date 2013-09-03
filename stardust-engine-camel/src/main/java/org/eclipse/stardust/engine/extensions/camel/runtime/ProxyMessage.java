package org.eclipse.stardust.engine.extensions.camel.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyMessage implements InvocationHandler {

	private Object obj;

    public static Object newInstance(Object obj) {
	return java.lang.reflect.Proxy.newProxyInstance(
	    obj.getClass().getClassLoader(),
	    obj.getClass().getInterfaces(),
	    new ProxyMessage(obj));
    }

    private ProxyMessage(Object obj) {
	this.obj = obj;
    }

    public Object invoke(Object proxy, Method m, Object[] args)
	throws Throwable
    {
        Object result;
	try {
	    result = m.invoke(obj, args);
        } catch (InvocationTargetException e) {
	    throw e.getTargetException();
    } 
	catch (Exception e) {
	    throw new RuntimeException("unexpected invocation exception: " +
				       e.getMessage());
	}
	return result;
    }
}