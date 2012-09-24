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
 * <p>Java class for HistoricalEventType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HistoricalEventType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="StateChange"/>
 *     &lt;enumeration value="Delegation"/>
 *     &lt;enumeration value="Note"/>
 *     &lt;enumeration value="Exception"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HistoricalEventType")
@XmlEnum
public enum HistoricalEventTypeXto {

    @XmlEnumValue("StateChange")
    STATE_CHANGE("StateChange"),
    @XmlEnumValue("Delegation")
    DELEGATION("Delegation"),
    @XmlEnumValue("Note")
    NOTE("Note"),
    @XmlEnumValue("Exception")
    EXCEPTION("Exception");
    private final String value;

    HistoricalEventTypeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HistoricalEventTypeXto fromValue(String v) {
        for (HistoricalEventTypeXto c: HistoricalEventTypeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
