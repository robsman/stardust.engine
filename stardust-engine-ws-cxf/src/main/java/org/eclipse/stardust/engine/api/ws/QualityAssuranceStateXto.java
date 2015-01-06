
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for QualityAssuranceState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="QualityAssuranceState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NO_QUALITY_ASSURANCE"/>
 *     &lt;enumeration value="IS_QUALITY_ASSURANCE"/>
 *     &lt;enumeration value="QUALITY_ASSURANCE_TRIGGERED"/>
 *     &lt;enumeration value="IS_REVISED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "QualityAssuranceState")
@XmlEnum
public enum QualityAssuranceStateXto {

    NO_QUALITY_ASSURANCE,
    IS_QUALITY_ASSURANCE,
    QUALITY_ASSURANCE_TRIGGERED,
    IS_REVISED;

    public String value() {
        return name();
    }

    public static QualityAssuranceStateXto fromValue(String v) {
        return valueOf(v);
    }

}
