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
package org.eclipse.stardust.engine.core.runtime.interceptor;

import java.lang.reflect.AccessibleObject;

/**
 * AOP Frameworks must provide an implementation of this method
 * for use by Interceptors.
 *
 * Implementing classes must return attribute information for use
 * by Interceptors.
 * @author Rod Johnson
 */
public interface AttributeRegistry {

	/**
	 * Return a list of attributes for the given field, method or
	 * constructor. AttributeAwareInterceptors will use this to
	 * find the attributes that apply to a method or field.
	 * @param ao
	 * @return Attribute[] a set of attributes applying to this object.
	 * The ordering is significant.
	 * Attributes declared against a supertype will also be returned.
	 * May return null if there are no attributes associated with this
	 * object
	 */
	Object[] getAttributes(AccessibleObject ao);

	Object[] getAttributes(Class clazz);

}
