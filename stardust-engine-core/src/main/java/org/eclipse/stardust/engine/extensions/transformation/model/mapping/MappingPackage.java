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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.MappingFactory
 * @model kind="package"
 * @generated
 */
public interface MappingPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "mapping";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "java://org.eclipse.stardust.engine.extensions.transformation.model";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "mapping";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	MappingPackage eINSTANCE = org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl <em>Field Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl#getFieldMapping()
	 * @generated
	 */
	int FIELD_MAPPING = 0;

	/**
	 * The feature id for the '<em><b>Field Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__FIELD_PATH = 0;

	/**
	 * The feature id for the '<em><b>Mapping Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__MAPPING_EXPRESSION = 1;

	/**
	 * The feature id for the '<em><b>Advanced Mapping</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__ADVANCED_MAPPING = 2;

	/**
	 * The feature id for the '<em><b>Content Mapping</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING__CONTENT_MAPPING = 3;

	/**
	 * The number of structural features of the '<em>Field Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FIELD_MAPPING_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.TransformationPropertyImpl <em>Transformation Property</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.TransformationPropertyImpl
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl#getTransformationProperty()
	 * @generated
	 */
	int TRANSFORMATION_PROPERTY = 1;

	/**
	 * The feature id for the '<em><b>Field Mappings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORMATION_PROPERTY__FIELD_MAPPINGS = 0;

	/**
	 * The feature id for the '<em><b>External Classes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES = 1;

	/**
	 * The number of structural features of the '<em>Transformation Property</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TRANSFORMATION_PROPERTY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.ExternalClassImpl <em>External Class</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.ExternalClassImpl
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl#getExternalClass()
	 * @generated
	 */
	int EXTERNAL_CLASS = 2;

	/**
	 * The feature id for the '<em><b>Instance Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTERNAL_CLASS__INSTANCE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Class Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTERNAL_CLASS__CLASS_NAME = 1;

	/**
	 * The number of structural features of the '<em>External Class</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXTERNAL_CLASS_FEATURE_COUNT = 2;


	/**
	 * Returns the meta object for class '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping <em>Field Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Field Mapping</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping
	 * @generated
	 */
	EClass getFieldMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getFieldPath <em>Field Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Field Path</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getFieldPath()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_FieldPath();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getMappingExpression <em>Mapping Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Mapping Expression</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#getMappingExpression()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_MappingExpression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isAdvancedMapping <em>Advanced Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Advanced Mapping</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isAdvancedMapping()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_AdvancedMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isContentMapping <em>Content Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Content Mapping</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.FieldMapping#isContentMapping()
	 * @see #getFieldMapping()
	 * @generated
	 */
	EAttribute getFieldMapping_ContentMapping();

	/**
	 * Returns the meta object for class '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty <em>Transformation Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Transformation Property</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty
	 * @generated
	 */
	EClass getTransformationProperty();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty#getFieldMappings <em>Field Mappings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Field Mappings</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty#getFieldMappings()
	 * @see #getTransformationProperty()
	 * @generated
	 */
	EReference getTransformationProperty_FieldMappings();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty#getExternalClasses <em>External Classes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>External Classes</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.TransformationProperty#getExternalClasses()
	 * @see #getTransformationProperty()
	 * @generated
	 */
	EReference getTransformationProperty_ExternalClasses();

	/**
	 * Returns the meta object for class '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass <em>External Class</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>External Class</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass
	 * @generated
	 */
	EClass getExternalClass();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass#getInstanceName <em>Instance Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Instance Name</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass#getInstanceName()
	 * @see #getExternalClass()
	 * @generated
	 */
	EAttribute getExternalClass_InstanceName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass#getClassName <em>Class Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Class Name</em>'.
	 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.ExternalClass#getClassName()
	 * @see #getExternalClass()
	 * @generated
	 */
	EAttribute getExternalClass_ClassName();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	MappingFactory getMappingFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl <em>Field Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.FieldMappingImpl
		 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl#getFieldMapping()
		 * @generated
		 */
		EClass FIELD_MAPPING = eINSTANCE.getFieldMapping();

		/**
		 * The meta object literal for the '<em><b>Field Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__FIELD_PATH = eINSTANCE.getFieldMapping_FieldPath();

		/**
		 * The meta object literal for the '<em><b>Mapping Expression</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__MAPPING_EXPRESSION = eINSTANCE.getFieldMapping_MappingExpression();

		/**
		 * The meta object literal for the '<em><b>Advanced Mapping</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__ADVANCED_MAPPING = eINSTANCE.getFieldMapping_AdvancedMapping();

		/**
		 * The meta object literal for the '<em><b>Content Mapping</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FIELD_MAPPING__CONTENT_MAPPING = eINSTANCE.getFieldMapping_ContentMapping();

		/**
		 * The meta object literal for the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.TransformationPropertyImpl <em>Transformation Property</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.TransformationPropertyImpl
		 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl#getTransformationProperty()
		 * @generated
		 */
		EClass TRANSFORMATION_PROPERTY = eINSTANCE.getTransformationProperty();

		/**
		 * The meta object literal for the '<em><b>Field Mappings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORMATION_PROPERTY__FIELD_MAPPINGS = eINSTANCE.getTransformationProperty_FieldMappings();

		/**
		 * The meta object literal for the '<em><b>External Classes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TRANSFORMATION_PROPERTY__EXTERNAL_CLASSES = eINSTANCE.getTransformationProperty_ExternalClasses();

		/**
		 * The meta object literal for the '{@link org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.ExternalClassImpl <em>External Class</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.ExternalClassImpl
		 * @see org.eclipse.stardust.engine.extensions.transformation.model.mapping.impl.MappingPackageImpl#getExternalClass()
		 * @generated
		 */
		EClass EXTERNAL_CLASS = eINSTANCE.getExternalClass();

		/**
		 * The meta object literal for the '<em><b>Instance Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXTERNAL_CLASS__INSTANCE_NAME = eINSTANCE.getExternalClass_InstanceName();

		/**
		 * The meta object literal for the '<em><b>Class Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXTERNAL_CLASS__CLASS_NAME = eINSTANCE.getExternalClass_ClassName();

	}

} //MappingPackage
