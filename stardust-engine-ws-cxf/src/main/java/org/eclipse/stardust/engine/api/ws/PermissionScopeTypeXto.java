
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PermissionScopeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="PermissionScopeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Model"/>
 *     &lt;enumeration value="Process"/>
 *     &lt;enumeration value="Activity"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PermissionScopeType")
@XmlEnum
public enum PermissionScopeTypeXto {

    @XmlEnumValue("Model")
    MODEL("Model"),
    @XmlEnumValue("Process")
    PROCESS("Process"),
    @XmlEnumValue("Activity")
    ACTIVITY("Activity");
    private final String value;

    PermissionScopeTypeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PermissionScopeTypeXto fromValue(String v) {
        for (PermissionScopeTypeXto c: PermissionScopeTypeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
