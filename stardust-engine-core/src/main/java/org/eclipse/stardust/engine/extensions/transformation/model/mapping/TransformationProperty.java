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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Transformation Property</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty#getFieldMappings <em>Field Mappings</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty#getExternalClasses <em>External Classes</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getTransformationProperty()
 * @model
 * @generated
 */
public interface TransformationProperty extends EObject {
	/**
	 * Returns the value of the '<em><b>Field Mappings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Field Mappings</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Field Mappings</em>' containment reference list.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getTransformationProperty_FieldMappings()
	 * @model containment="true"
	 * @generated
	 */
	EList<FieldMapping> getFieldMappings();

	/**
	 * Returns the value of the '<em><b>External Classes</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>External Classes</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>External Classes</em>' containment reference list.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage#getTransformationProperty_ExternalClasses()
	 * @model containment="true"
	 * @generated
	 */
	EList<ExternalClass> getExternalClasses();

} // TransformationProperty
