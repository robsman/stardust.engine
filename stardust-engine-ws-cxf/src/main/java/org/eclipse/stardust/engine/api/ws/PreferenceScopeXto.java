
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für PreferenceScope.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="PreferenceScope">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DEFAULT"/>
 *     &lt;enumeration value="PARTITION"/>
 *     &lt;enumeration value="REALM"/>
 *     &lt;enumeration value="USER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PreferenceScope")
@XmlEnum
public enum PreferenceScopeXto {

    DEFAULT,
    PARTITION,
    REALM,
    USER;

    public String value() {
        return name();
    }

    public static PreferenceScopeXto fromValue(String v) {
        return valueOf(v);
    }

}
