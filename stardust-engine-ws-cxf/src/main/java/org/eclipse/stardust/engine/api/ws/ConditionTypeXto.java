
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ConditionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ConditionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Condition"/>
 *     &lt;enumeration value="Otherwise"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ConditionType")
@XmlEnum
public enum ConditionTypeXto {

    @XmlEnumValue("Condition")
    CONDITION("Condition"),
    @XmlEnumValue("Otherwise")
    OTHERWISE("Otherwise");
    private final String value;

    ConditionTypeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ConditionTypeXto fromValue(String v) {
        for (ConditionTypeXto c: ConditionTypeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
