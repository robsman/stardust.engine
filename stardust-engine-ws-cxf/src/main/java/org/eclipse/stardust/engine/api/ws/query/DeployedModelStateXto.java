
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeployedModelState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeployedModelState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Active"/>
 *     &lt;enumeration value="Alive"/>
 *     &lt;enumeration value="Disabled"/>
 *     &lt;enumeration value="Inactive"/>
 *     &lt;enumeration value="Valid"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DeployedModelState")
@XmlEnum
public enum DeployedModelStateXto {

    @XmlEnumValue("Active")
    ACTIVE("Active"),
    @XmlEnumValue("Alive")
    ALIVE("Alive"),
    @XmlEnumValue("Disabled")
    DISABLED("Disabled"),
    @XmlEnumValue("Inactive")
    INACTIVE("Inactive"),
    @XmlEnumValue("Valid")
    VALID("Valid");
    private final String value;

    DeployedModelStateXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeployedModelStateXto fromValue(String v) {
        for (DeployedModelStateXto c: DeployedModelStateXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
