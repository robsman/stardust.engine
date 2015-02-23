
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PolicyScope.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="PolicyScope">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="All"/>
 *     &lt;enumeration value="Applicable"/>
 *     &lt;enumeration value="Effective"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PolicyScope")
@XmlEnum
public enum PolicyScopeXto {

    @XmlEnumValue("All")
    ALL("All"),
    @XmlEnumValue("Applicable")
    APPLICABLE("Applicable"),
    @XmlEnumValue("Effective")
    EFFECTIVE("Effective");
    private final String value;

    PolicyScopeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PolicyScopeXto fromValue(String v) {
        for (PolicyScopeXto c: PolicyScopeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
