
package org.eclipse.stardust.engine.api.ws.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LinkDirection.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LinkDirection">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="from"/>
 *     &lt;enumeration value="to"/>
 *     &lt;enumeration value="toFrom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LinkDirection")
@XmlEnum
public enum LinkDirectionXto {

    @XmlEnumValue("from")
    FROM("from"),
    @XmlEnumValue("to")
    TO("to"),
    @XmlEnumValue("toFrom")
    TO_FROM("toFrom");
    private final String value;

    LinkDirectionXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LinkDirectionXto fromValue(String v) {
        for (LinkDirectionXto c: LinkDirectionXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
