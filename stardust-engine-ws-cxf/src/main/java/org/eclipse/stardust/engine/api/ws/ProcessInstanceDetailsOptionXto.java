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

package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessInstanceDetailsOption.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProcessInstanceDetailsOption">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="withHierarchyInfo"/>
 *     &lt;enumeration value="withLinkInfo"/>
 *     &lt;enumeration value="withNotes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ProcessInstanceDetailsOption")
@XmlEnum
public enum ProcessInstanceDetailsOptionXto {

    @XmlEnumValue("withHierarchyInfo")
    WITH_HIERARCHY_INFO("withHierarchyInfo"),
    @XmlEnumValue("withLinkInfo")
    WITH_LINK_INFO("withLinkInfo"),
    @XmlEnumValue("withNotes")
    WITH_NOTES("withNotes");
    private final String value;

    ProcessInstanceDetailsOptionXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcessInstanceDetailsOptionXto fromValue(String v) {
        for (ProcessInstanceDetailsOptionXto c: ProcessInstanceDetailsOptionXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
