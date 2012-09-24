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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VariableReferenceScope.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="VariableReferenceScope">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NCName">
 *     &lt;enumeration value="Local"/>
 *     &lt;enumeration value="AnyParent"/>
 *     &lt;enumeration value="AnyParentOrChild"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "VariableReferenceScope")
@XmlEnum
public enum VariableReferenceScopeXto {

    @XmlEnumValue("Local")
    LOCAL("Local"),
    @XmlEnumValue("AnyParent")
    ANY_PARENT("AnyParent"),
    @XmlEnumValue("AnyParentOrChild")
    ANY_PARENT_OR_CHILD("AnyParentOrChild");
    private final String value;

    VariableReferenceScopeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static VariableReferenceScopeXto fromValue(String v) {
        for (VariableReferenceScopeXto c: VariableReferenceScopeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
