
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für FolderLevelOfDetail.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="FolderLevelOfDetail">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NoMembers"/>
 *     &lt;enumeration value="DirectMembers"/>
 *     &lt;enumeration value="MembersOfMembers"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "FolderLevelOfDetail")
@XmlEnum
public enum FolderLevelOfDetailXto {


    /**
     * No member of a folder will be selected.
     * 
     */
    @XmlEnumValue("NoMembers")
    NO_MEMBERS("NoMembers"),

    /**
     * Direct members of a folder will be selected.
     * 
     */
    @XmlEnumValue("DirectMembers")
    DIRECT_MEMBERS("DirectMembers"),

    /**
     * All direct members and their members will be selected.
     * 
     */
    @XmlEnumValue("MembersOfMembers")
    MEMBERS_OF_MEMBERS("MembersOfMembers");
    private final String value;

    FolderLevelOfDetailXto(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FolderLevelOfDetailXto fromValue(String v) {
        for (FolderLevelOfDetailXto c: FolderLevelOfDetailXto.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
