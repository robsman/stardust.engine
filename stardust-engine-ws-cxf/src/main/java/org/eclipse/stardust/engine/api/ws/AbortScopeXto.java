
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für AbortScope.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="AbortScope">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="RootHierarchy"/>
 *     &lt;enumeration value="SubHierarchy"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AbortScope")
@XmlEnum
public enum AbortScopeXto {

    @XmlEnumValue("RootHierarchy")
    ROOT_HIERARCHY("RootHierarchy"),
    @XmlEnumValue("SubHierarchy")
    SUB_HIERARCHY("SubHierarchy");
    private final String value;

    AbortScopeXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AbortScopeXto fromValue(String v) {
        for (AbortScopeXto c: AbortScopeXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
