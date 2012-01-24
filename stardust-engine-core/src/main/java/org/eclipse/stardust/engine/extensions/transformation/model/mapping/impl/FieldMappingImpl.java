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


import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping;
import org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Field Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl#getFieldPath <em>Field Path</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl#getMappingExpression <em>Mapping Expression</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl#isAdvancedMapping <em>Advanced Mapping</em>}</li>
 *   <li>{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl#isContentMapping <em>Content Mapping</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FieldMappingImpl extends EObjectImpl implements FieldMapping {
	/**
	 * The default value of the '{@link #getFieldPath() <em>Field Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFieldPath()
	 * @generated
	 * @ordered
	 */
	protected static final String FIELD_PATH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFieldPath() <em>Field Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFieldPath()
	 * @generated
	 * @ordered
	 */
	protected String fieldPath = FIELD_PATH_EDEFAULT;

	/**
	 * The default value of the '{@link #getMappingExpression() <em>Mapping Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMappingExpression()
	 * @generated
	 * @ordered
	 */
	protected static final String MAPPING_EXPRESSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMappingExpression() <em>Mapping Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMappingExpression()
	 * @generated
	 * @ordered
	 */
	protected String mappingExpression = MAPPING_EXPRESSION_EDEFAULT;

	/**
	 * The default value of the '{@link #isAdvancedMapping() <em>Advanced Mapping</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAdvancedMapping()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ADVANCED_MAPPING_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAdvancedMapping() <em>Advanced Mapping</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAdvancedMapping()
	 * @generated
	 * @ordered
	 */
	protected boolean advancedMapping = ADVANCED_MAPPING_EDEFAULT;

	/**
	 * The default value of the '{@link #isContentMapping() <em>Content Mapping</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContentMapping()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONTENT_MAPPING_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isContentMapping() <em>Content Mapping</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContentMapping()
	 * @generated
	 * @ordered
	 */
	protected boolean contentMapping = CONTENT_MAPPING_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FieldMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MappingPackage.Literals.FIELD_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFieldPath() {
		return fieldPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFieldPath(String newFieldPath) {
		String oldFieldPath = fieldPath;
		fieldPath = newFieldPath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.FIELD_MAPPING__FIELD_PATH, oldFieldPath, fieldPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getMappingExpression() {
		return mappingExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setMappingExpression(String newMappingExpression) {
		String oldMappingExpression = mappingExpression;
		mappingExpression = newMappingExpression;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.FIELD_MAPPING__MAPPING_EXPRESSION, oldMappingExpression, mappingExpression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isAdvancedMapping() {
		return advancedMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setAdvancedMapping(boolean newAdvancedMapping) {
		boolean oldAdvancedMapping = advancedMapping;
		advancedMapping = newAdvancedMapping;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.FIELD_MAPPING__ADVANCED_MAPPING, oldAdvancedMapping, advancedMapping));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isContentMapping() {
		return contentMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContentMapping(boolean newContentMapping) {
		boolean oldContentMapping = contentMapping;
		contentMapping = newContentMapping;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MappingPackage.FIELD_MAPPING__CONTENT_MAPPING, oldContentMapping, contentMapping));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MappingPackage.FIELD_MAPPING__FIELD_PATH:
				return getFieldPath();
			case MappingPackage.FIELD_MAPPING__MAPPING_EXPRESSION:
				return getMappingExpression();
			case MappingPackage.FIELD_MAPPING__ADVANCED_MAPPING:
				return isAdvancedMapping() ? Boolean.TRUE : Boolean.FALSE;
			case MappingPackage.FIELD_MAPPING__CONTENT_MAPPING:
				return isContentMapping() ? Boolean.TRUE : Boolean.FALSE;
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MappingPackage.FIELD_MAPPING__FIELD_PATH:
				setFieldPath((String)newValue);
				return;
			case MappingPackage.FIELD_MAPPING__MAPPING_EXPRESSION:
				setMappingExpression((String)newValue);
				return;
			case MappingPackage.FIELD_MAPPING__ADVANCED_MAPPING:
				setAdvancedMapping(((Boolean)newValue).booleanValue());
				return;
			case MappingPackage.FIELD_MAPPING__CONTENT_MAPPING:
				setContentMapping(((Boolean)newValue).booleanValue());
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
			case MappingPackage.FIELD_MAPPING__FIELD_PATH:
				setFieldPath(FIELD_PATH_EDEFAULT);
				return;
			case MappingPackage.FIELD_MAPPING__MAPPING_EXPRESSION:
				setMappingExpression(MAPPING_EXPRESSION_EDEFAULT);
				return;
			case MappingPackage.FIELD_MAPPING__ADVANCED_MAPPING:
				setAdvancedMapping(ADVANCED_MAPPING_EDEFAULT);
				return;
			case MappingPackage.FIELD_MAPPING__CONTENT_MAPPING:
				setContentMapping(CONTENT_MAPPING_EDEFAULT);
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
			case MappingPackage.FIELD_MAPPING__FIELD_PATH:
				return FIELD_PATH_EDEFAULT == null ? fieldPath != null : !FIELD_PATH_EDEFAULT.equals(fieldPath);
			case MappingPackage.FIELD_MAPPING__MAPPING_EXPRESSION:
				return MAPPING_EXPRESSION_EDEFAULT == null ? mappingExpression != null : !MAPPING_EXPRESSION_EDEFAULT.equals(mappingExpression);
			case MappingPackage.FIELD_MAPPING__ADVANCED_MAPPING:
				return advancedMapping != ADVANCED_MAPPING_EDEFAULT;
			case MappingPackage.FIELD_MAPPING__CONTENT_MAPPING:
				return contentMapping != CONTENT_MAPPING_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (fieldPath: ");
		result.append(fieldPath);
		result.append(", mappingExpression: ");
		result.append(mappingExpression);
		result.append(", advancedMapping: ");
		result.append(advancedMapping);
		result.append(", contentMapping: ");
		result.append(contentMapping);
		result.append(')');
		return result.toString();
	}

} //FieldMappingImpl
