
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HierarchyMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HierarchyMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="RootProcess"/>
 *     &lt;enumeration value="SubProcess"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HierarchyMode")
@XmlEnum
public enum HierarchyModeXto {

    @XmlEnumValue("RootProcess")
    ROOT_PROCESS("RootProcess"),
    @XmlEnumValue("SubProcess")
    SUB_PROCESS("SubProcess");
    private final String value;

    HierarchyModeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HierarchyModeXto fromValue(String v) {
        for (HierarchyModeXto c: HierarchyModeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
