
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DaemonExecutionState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DaemonExecutionState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OK"/>
 *     &lt;enumeration value="Warning"/>
 *     &lt;enumeration value="Fatal"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "DaemonExecutionState")
@XmlEnum
public enum DaemonExecutionStateXto {

    OK("OK"),
    @XmlEnumValue("Warning")
    WARNING("Warning"),
    @XmlEnumValue("Fatal")
    FATAL("Fatal");
    private final String value;

    DaemonExecutionStateXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DaemonExecutionStateXto fromValue(String v) {
        for (DaemonExecutionStateXto c: DaemonExecutionStateXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
