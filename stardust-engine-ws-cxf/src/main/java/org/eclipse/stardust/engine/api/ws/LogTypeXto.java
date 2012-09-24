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
 * <p>Java class for LogType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LogType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Debug"/>
 *     &lt;enumeration value="Info"/>
 *     &lt;enumeration value="Warn"/>
 *     &lt;enumeration value="Error"/>
 *     &lt;enumeration value="Fatal"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LogType")
@XmlEnum
public enum LogTypeXto {

    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Debug")
    DEBUG("Debug"),
    @XmlEnumValue("Info")
    INFO("Info"),
    @XmlEnumValue("Warn")
    WARN("Warn"),
    @XmlEnumValue("Error")
    ERROR("Error"),
    @XmlEnumValue("Fatal")
    FATAL("Fatal");
    private final String value;

    LogTypeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LogTypeXto fromValue(String v) {
        for (LogTypeXto c: LogTypeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
