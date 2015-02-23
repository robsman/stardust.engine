
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RepositoryCapabilities complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RepositoryCapabilities">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fullTextSearchSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="metaDataSearchSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="metaDataWriteSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="versioningSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="transactionSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="accessControlPolicySupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="writeSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoryCapabilities", propOrder = {
    "fullTextSearchSupported",
    "metaDataSearchSupported",
    "metaDataWriteSupported",
    "versioningSupported",
    "transactionSupported",
    "accessControlPolicySupported",
    "writeSupported"
})
@XmlSeeAlso({
    RepositoryProviderInfoXto.class,
    RepositoryInstanceInfoXto.class
})
public class RepositoryCapabilitiesXto {

    protected boolean fullTextSearchSupported;
    protected boolean metaDataSearchSupported;
    protected boolean metaDataWriteSupported;
    protected boolean versioningSupported;
    protected boolean transactionSupported;
    protected boolean accessControlPolicySupported;
    protected boolean writeSupported;

    /**
     * Ruft den Wert der fullTextSearchSupported-Eigenschaft ab.
     * 
     */
    public boolean isFullTextSearchSupported() {
        return fullTextSearchSupported;
    }

    /**
     * Legt den Wert der fullTextSearchSupported-Eigenschaft fest.
     * 
     */
    public void setFullTextSearchSupported(boolean value) {
        this.fullTextSearchSupported = value;
    }

    /**
     * Ruft den Wert der metaDataSearchSupported-Eigenschaft ab.
     * 
     */
    public boolean isMetaDataSearchSupported() {
        return metaDataSearchSupported;
    }

    /**
     * Legt den Wert der metaDataSearchSupported-Eigenschaft fest.
     * 
     */
    public void setMetaDataSearchSupported(boolean value) {
        this.metaDataSearchSupported = value;
    }

    /**
     * Ruft den Wert der metaDataWriteSupported-Eigenschaft ab.
     * 
     */
    public boolean isMetaDataWriteSupported() {
        return metaDataWriteSupported;
    }

    /**
     * Legt den Wert der metaDataWriteSupported-Eigenschaft fest.
     * 
     */
    public void setMetaDataWriteSupported(boolean value) {
        this.metaDataWriteSupported = value;
    }

    /**
     * Ruft den Wert der versioningSupported-Eigenschaft ab.
     * 
     */
    public boolean isVersioningSupported() {
        return versioningSupported;
    }

    /**
     * Legt den Wert der versioningSupported-Eigenschaft fest.
     * 
     */
    public void setVersioningSupported(boolean value) {
        this.versioningSupported = value;
    }

    /**
     * Ruft den Wert der transactionSupported-Eigenschaft ab.
     * 
     */
    public boolean isTransactionSupported() {
        return transactionSupported;
    }

    /**
     * Legt den Wert der transactionSupported-Eigenschaft fest.
     * 
     */
    public void setTransactionSupported(boolean value) {
        this.transactionSupported = value;
    }

    /**
     * Ruft den Wert der accessControlPolicySupported-Eigenschaft ab.
     * 
     */
    public boolean isAccessControlPolicySupported() {
        return accessControlPolicySupported;
    }

    /**
     * Legt den Wert der accessControlPolicySupported-Eigenschaft fest.
     * 
     */
    public void setAccessControlPolicySupported(boolean value) {
        this.accessControlPolicySupported = value;
    }

    /**
     * Ruft den Wert der writeSupported-Eigenschaft ab.
     * 
     */
    public boolean isWriteSupported() {
        return writeSupported;
    }

    /**
     * Legt den Wert der writeSupported-Eigenschaft fest.
     * 
     */
    public void setWriteSupported(boolean value) {
        this.writeSupported = value;
    }

}
