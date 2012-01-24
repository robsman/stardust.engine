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
package org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl;


import java.util.Collection;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Transformation Property</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.TransformationPropertyImpl#getFieldMappings <em>Field Mappings</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.TransformationPropertyImpl#getExternalClasses <em>External Classes</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TransformationPropertyImpl extends EObjectImpl implements TransformationProperty {
	/**
	 * The cached value of the '{@link #getFieldMappings() <em>Field Mappings</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFieldMappings()
	 * @generated
	 * @ordered
	 */
	protected EList<FieldMapping> fieldMappings;

	/**
	 * The cached value of the '{@link #getExternalClasses() <em>External Classes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExternalClasses()
	 * @generated
	 * @ordered
	 */
	protected EList<ExternalClass> externalClasses;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected TransformationPropertyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MappingPackage.Literals.TRANSFORMATION_PROPERTY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<FieldMapping> getFieldMappings() {
		if (fieldMappings == null) {
			fieldMappings = new EObjectContainmentEList<FieldMapping>(FieldMapping.class, this, MappingPackage.TRANSFORMATION_PROPERTY__FIELD_MAPPINGS);
		}
		return fieldMappings;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ExternalClass> getExternalClasses() {
		if (externalClasses == null) {
			externalClasses = new EObjectContainmentEList<ExternalClass>(ExternalClass.class, this, MappingPackage.TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES);
		}
		return externalClasses;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MappingPackage.TRANSFORMATION_PROPERTY__FIELD_MAPPINGS:
				return ((InternalEList<?>)getFieldMappings()).basicRemove(otherEnd, msgs);
			case MappingPackage.TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES:
				return ((InternalEList<?>)getExternalClasses()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MappingPackage.TRANSFORMATION_PROPERTY__FIELD_MAPPINGS:
				return getFieldMappings();
			case MappingPackage.TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES:
				return getExternalClasses();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MappingPackage.TRANSFORMATION_PROPERTY__FIELD_MAPPINGS:
				getFieldMappings().clear();
				getFieldMappings().addAll((Collection<? extends FieldMapping>)newValue);
				return;
			case MappingPackage.TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES:
				getExternalClasses().clear();
				getExternalClasses().addAll((Collection<? extends ExternalClass>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case MappingPackage.TRANSFORMATION_PROPERTY__FIELD_MAPPINGS:
				getFieldMappings().clear();
				return;
			case MappingPackage.TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES:
				getExternalClasses().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case MappingPackage.TRANSFORMATION_PROPERTY__FIELD_MAPPINGS:
				return fieldMappings != null && !fieldMappings.isEmpty();
			case MappingPackage.TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES:
				return externalClasses != null && !externalClasses.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //TransformationPropertyImpl
