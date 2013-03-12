
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AcknowledgementState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="AcknowledgementState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OK"/>
 *     &lt;enumeration value="Response Requested"/>
 *     &lt;enumeration value="Failure"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "AcknowledgementState")
@XmlEnum
public enum AcknowledgementStateXto {

    OK("OK"),
    @XmlEnumValue("Response Requested")
    RESPONSE_REQUESTED("Response Requested"),
    @XmlEnumValue("Failure")
    FAILURE("Failure");
    private final String value;

    AcknowledgementStateXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AcknowledgementStateXto fromValue(String v) {
        for (AcknowledgementStateXto c: AcknowledgementStateXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
