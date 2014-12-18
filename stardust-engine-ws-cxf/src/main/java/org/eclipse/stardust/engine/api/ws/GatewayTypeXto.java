
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GatewayType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="GatewayType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="And"/>
 *     &lt;enumeration value="Or"/>
 *     &lt;enumeration value="Xor"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "GatewayType")
@XmlEnum
public enum GatewayTypeXto {

    @XmlEnumValue("And")
    AND("And"),
    @XmlEnumValue("Or")
    OR("Or"),
    @XmlEnumValue("Xor")
    XOR("Xor");
    private final String value;

    GatewayTypeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GatewayTypeXto fromValue(String v) {
        for (GatewayTypeXto c: GatewayTypeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
