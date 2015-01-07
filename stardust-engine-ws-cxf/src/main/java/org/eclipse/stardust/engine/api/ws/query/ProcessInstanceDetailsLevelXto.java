
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessInstanceDetailsLevel.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProcessInstanceDetailsLevel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Core"/>
 *     &lt;enumeration value="WithProperties"/>
 *     &lt;enumeration value="WithResolvedProperties"/>
 *     &lt;enumeration value="Full"/>
 *     &lt;enumeration value="Default"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ProcessInstanceDetailsLevel")
@XmlEnum
public enum ProcessInstanceDetailsLevelXto {

    @XmlEnumValue("Core")
    CORE("Core"),
    @XmlEnumValue("WithProperties")
    WITH_PROPERTIES("WithProperties"),
    @XmlEnumValue("WithResolvedProperties")
    WITH_RESOLVED_PROPERTIES("WithResolvedProperties"),
    @XmlEnumValue("Full")
    FULL("Full"),
    @XmlEnumValue("Default")
    DEFAULT("Default");
    private final String value;

    ProcessInstanceDetailsLevelXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcessInstanceDetailsLevelXto fromValue(String v) {
        for (ProcessInstanceDetailsLevelXto c: ProcessInstanceDetailsLevelXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
