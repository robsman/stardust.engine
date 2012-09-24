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

package org.eclipse.stardust.engine.api.ws.interactions;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BpmInteractionFaultCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BpmInteractionFaultCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="UnknownError"/>
 *     &lt;enumeration value="UnknownInteraction"/>
 *     &lt;enumeration value="WrongParameter"/>
 *     &lt;enumeration value="UnsupportedParameterValue"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "BpmInteractionFaultCode")
@XmlEnum
public enum BpmInteractionFaultCodeXto {

    @XmlEnumValue("UnknownError")
    UNKNOWN_ERROR("UnknownError"),
    @XmlEnumValue("UnknownInteraction")
    UNKNOWN_INTERACTION("UnknownInteraction"),
    @XmlEnumValue("WrongParameter")
    WRONG_PARAMETER("WrongParameter"),
    @XmlEnumValue("UnsupportedParameterValue")
    UNSUPPORTED_PARAMETER_VALUE("UnsupportedParameterValue");
    private final String value;

    BpmInteractionFaultCodeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BpmInteractionFaultCodeXto fromValue(String v) {
        for (BpmInteractionFaultCodeXto c: BpmInteractionFaultCodeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
