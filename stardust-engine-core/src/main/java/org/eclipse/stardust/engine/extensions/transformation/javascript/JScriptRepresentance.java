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
package org.eclipse.stardust.engine.extensions.transformation.javascript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JScriptRepresentance {
	Object type;	
	List dependencies = new ArrayList();
	String declarations = "";
	String instantiations = "";
	String complexDeclarations = "";
	String complexInstantiations = "";
	String constructorFields = "";
	Map constructorOrder;
	
	public JScriptRepresentance(Object type, List dependencies, String declarations, String instantiations, String complexDeclaration, String complexInstantiations, Map constructorOrder, String constructorFields) {
		super();
		this.type = type;
		this.dependencies = dependencies;	
		this.declarations = declarations;
		this.instantiations = instantiations;
		this.constructorFields = constructorFields;
		this.constructorOrder = constructorOrder;
		this.complexDeclarations = complexDeclaration;
		this.complexInstantiations = complexInstantiations;
	}
	
	public Object getType() {
		return type;
	}
	public void setType(Object type) {
		this.type = type;
	}
	public List getDependencies() {
		return dependencies;
	}
	public void setDependencies(List dependencies) {
		this.dependencies = dependencies;
	}

	public String getDelcarations() {
		return declarations;
	}

	public void setDeclarations(String declarations) {
		this.declarations = declarations;
	}

	public String getInstantiations() {
		return instantiations;
	}

	public void setInstantiations(String instantiations) {
		this.instantiations = instantiations;
	}
	
	public String toString() {
		return declarations + "\n" + instantiations;
	}

	public String getConstructorFields() {
		return constructorFields;
	}

	public void setConstructorFields(String constructorFields) {
		this.constructorFields = constructorFields;
	}

	public Map getConstructorOrder() {
		return constructorOrder;
	}

	public void setConstructorOrder(Map constructorOrder) {
		this.constructorOrder = constructorOrder;
	}

	public String getComplexDeclarations() {
		return complexDeclarations;
	}

	public void setComplexDeclarations(String complexDeclarations) {
		this.complexDeclarations = complexDeclarations;
	}

	public String getComplexInstantiations() {
		return complexInstantiations;
	}

	public void setComplexInstantiations(String complexInstantiations) {
		this.complexInstantiations = complexInstantiations;
	}

}
