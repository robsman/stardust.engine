
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessDefinitionDetailsLevel.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProcessDefinitionDetailsLevel">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Core"/>
 *     &lt;enumeration value="WithoutActivities"/>
 *     &lt;enumeration value="Full"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 *
 */
@XmlType(name = "ProcessDefinitionDetailsLevel")
@XmlEnum
public enum ProcessDefinitionDetailsLevelXto {

    @XmlEnumValue("Core")
    CORE("Core"),
    @XmlEnumValue("WithoutActivities")
    WITHOUT_ACTIVITIES("WithoutActivities"),
    @XmlEnumValue("Full")
    FULL("Full");
    private final String value;

    ProcessDefinitionDetailsLevelXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcessDefinitionDetailsLevelXto fromValue(String v) {
        for (ProcessDefinitionDetailsLevelXto c: ProcessDefinitionDetailsLevelXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
