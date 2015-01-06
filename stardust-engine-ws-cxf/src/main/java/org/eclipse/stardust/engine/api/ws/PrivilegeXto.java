
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Privilege.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="Privilege">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Read"/>
 *     &lt;enumeration value="Modify"/>
 *     &lt;enumeration value="Create"/>
 *     &lt;enumeration value="Delete"/>
 *     &lt;enumeration value="DeleteChildren"/>
 *     &lt;enumeration value="ReadAcl"/>
 *     &lt;enumeration value="ModifyAcl"/>
 *     &lt;enumeration value="All"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "Privilege")
@XmlEnum
public enum PrivilegeXto {

    @XmlEnumValue("Read")
    READ("Read"),
    @XmlEnumValue("Modify")
    MODIFY("Modify"),
    @XmlEnumValue("Create")
    CREATE("Create"),
    @XmlEnumValue("Delete")
    DELETE("Delete"),
    @XmlEnumValue("DeleteChildren")
    DELETE_CHILDREN("DeleteChildren"),
    @XmlEnumValue("ReadAcl")
    READ_ACL("ReadAcl"),
    @XmlEnumValue("ModifyAcl")
    MODIFY_ACL("ModifyAcl"),
    @XmlEnumValue("All")
    ALL("All");
    private final String value;

    PrivilegeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PrivilegeXto fromValue(String v) {
        for (PrivilegeXto c: PrivilegeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
