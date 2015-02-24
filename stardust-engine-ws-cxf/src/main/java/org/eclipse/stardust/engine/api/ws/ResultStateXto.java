
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ResultState.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ResultState">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="PASS_WITH_CORRECTION"/>
 *     &lt;enumeration value="PASS_NO_CORRECTION"/>
 *     &lt;enumeration value="FAILED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ResultState")
@XmlEnum
public enum ResultStateXto {

    PASS_WITH_CORRECTION,
    PASS_NO_CORRECTION,
    FAILED;

    public String value() {
        return name();
    }

    public static ResultStateXto fromValue(String v) {
        return valueOf(v);
    }

}
