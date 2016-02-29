
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SpawnMode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SpawnMode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="KEEP"/>
 *     &lt;enumeration value="ABORT"/>
 *     &lt;enumeration value="HALT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SpawnMode")
@XmlEnum
public enum SpawnModeXto {

    KEEP,
    ABORT,
    HALT;

    public String value() {
        return name();
    }

    public static SpawnModeXto fromValue(String v) {
        return valueOf(v);
    }

}
