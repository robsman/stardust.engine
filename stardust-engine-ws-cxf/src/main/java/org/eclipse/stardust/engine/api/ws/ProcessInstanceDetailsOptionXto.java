
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ProcessInstanceDetailsOption.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="ProcessInstanceDetailsOption">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="withHierarchyInfo"/>
 *     &lt;enumeration value="withLinkInfo"/>
 *     &lt;enumeration value="withNotes"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ProcessInstanceDetailsOption")
@XmlEnum
public enum ProcessInstanceDetailsOptionXto {

    @XmlEnumValue("withHierarchyInfo")
    WITH_HIERARCHY_INFO("withHierarchyInfo"),
    @XmlEnumValue("withLinkInfo")
    WITH_LINK_INFO("withLinkInfo"),
    @XmlEnumValue("withNotes")
    WITH_NOTES("withNotes");
    private final String value;

    ProcessInstanceDetailsOptionXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcessInstanceDetailsOptionXto fromValue(String v) {
        for (ProcessInstanceDetailsOptionXto c: ProcessInstanceDetailsOptionXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
