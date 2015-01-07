
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UserDetailsLevel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="UserDetailsLevel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Core"/>
 *     &lt;enumeration value="WithProperties"/>
 *     &lt;enumeration value="Full"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "UserDetailsLevel")
@XmlEnum
public enum UserDetailsLevelXto {

    @XmlEnumValue("Core")
    CORE("Core"),
    @XmlEnumValue("WithProperties")
    WITH_PROPERTIES("WithProperties"),
    @XmlEnumValue("Full")
    FULL("Full");
    private final String value;

    UserDetailsLevelXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UserDetailsLevelXto fromValue(String v) {
        for (UserDetailsLevelXto c: UserDetailsLevelXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
