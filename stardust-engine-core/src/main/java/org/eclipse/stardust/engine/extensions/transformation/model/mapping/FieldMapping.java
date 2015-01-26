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
package org.eclipse.stardust.engine.extensions.transformation.model.mapping;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getFieldPath <em>Field Path</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getMappingExpression <em>Mapping Expression</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isAdvancedMapping <em>Advanced Mapping</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isContentMapping <em>Content Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getFieldMapping()
 * @model
 * @generated
 */
public interface FieldMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>Field Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Field Path</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Field Path</em>' attribute.
	 * @see #setFieldPath(String)
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getFieldMapping_FieldPath()
	 * @model
	 * @generated
	 */
	String getFieldPath();

	/**
	 * Sets the value of the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getFieldPath <em>Field Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Field Path</em>' attribute.
	 * @see #getFieldPath()
	 * @generated
	 */
	void setFieldPath(String value);

	/**
	 * Returns the value of the '<em><b>Mapping Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Mapping Expression</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mapping Expression</em>' attribute.
	 * @see #setMappingExpression(String)
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getFieldMapping_MappingExpression()
	 * @model
	 * @generated
	 */
	String getMappingExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getMappingExpression <em>Mapping Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Mapping Expression</em>' attribute.
	 * @see #getMappingExpression()
	 * @generated
	 */
	void setMappingExpression(String value);

	/**
	 * Returns the value of the '<em><b>Advanced Mapping</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Advanced Mapping</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Advanced Mapping</em>' attribute.
	 * @see #setAdvancedMapping(boolean)
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getFieldMapping_AdvancedMapping()
	 * @model
	 * @generated
	 */
	boolean isAdvancedMapping();

	/**
	 * Sets the value of the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isAdvancedMapping <em>Advanced Mapping</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Advanced Mapping</em>' attribute.
	 * @see #isAdvancedMapping()
	 * @generated
	 */
	void setAdvancedMapping(boolean value);

	/**
	 * Returns the value of the '<em><b>Content Mapping</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Content Mapping</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Content Mapping</em>' attribute.
	 * @see #setContentMapping(boolean)
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getFieldMapping_ContentMapping()
	 * @model
	 * @generated
	 */
	boolean isContentMapping();

	/**
	 * Sets the value of the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isContentMapping <em>Content Mapping</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content Mapping</em>' attribute.
	 * @see #isContentMapping()
	 * @generated
	 */
	void setContentMapping(boolean value);

} // FieldMapping
