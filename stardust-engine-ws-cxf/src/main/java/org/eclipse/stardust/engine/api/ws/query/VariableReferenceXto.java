/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Used to reference workflow data by assigning an dataId to the 'operand' element.
 *         Different scopes are available:
 *         'Local' (default) scope is used for finding activity instances belonging to process instances with same scope process instance containing specific workflow data.
 *         'AnyParent' scope is used for finding activity instances belonging to process instances and its subprocess instances containing specific workflow data.
 *         'AnyParentOrChild' scope is used for for finding activity instances belonging to the complete hierarchy of process instances containing specific workflow data.
 *         
 * 
 * <p>Java class for VariableReference complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VariableReference">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://eclipse.org/stardust/ws/v2012a/api/query>Operand">
 *       &lt;attribute name="scope" type="{http://eclipse.org/stardust/ws/v2012a/api/query}VariableReferenceScope" default="Local" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariableReference")
public class VariableReferenceXto
    extends OperandXto
{

    @XmlAttribute
    protected VariableReferenceScopeXto scope;

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link VariableReferenceScopeXto }
     *     
     */
    public VariableReferenceScopeXto getScope() {
        if (scope == null) {
            return VariableReferenceScopeXto.LOCAL;
        } else {
            return scope;
        }
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableReferenceScopeXto }
     *     
     */
    public void setScope(VariableReferenceScopeXto value) {
        this.scope = value;
    }

}
