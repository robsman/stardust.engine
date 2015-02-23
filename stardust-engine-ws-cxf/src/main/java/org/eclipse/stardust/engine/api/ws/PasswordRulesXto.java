
package org.eclipse.stardust.engine.api.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			Represents rules that apply to passwords.
 * 			
 * 
 * <p>Java-Klasse für PasswordRules complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PasswordRules">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="passwordTracking" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="notificationMails" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="expirationTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="disableUserTime" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="differentCharacters" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="minimalPasswordLength" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="letters" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="digits" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="mixedCase" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="punctuation" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PasswordRules", propOrder = {
    "passwordTracking",
    "notificationMails",
    "expirationTime",
    "disableUserTime",
    "differentCharacters",
    "minimalPasswordLength",
    "letters",
    "digits",
    "mixedCase",
    "punctuation"
})
public class PasswordRulesXto {

    protected int passwordTracking;
    protected int notificationMails;
    protected int expirationTime;
    protected int disableUserTime;
    protected int differentCharacters;
    protected int minimalPasswordLength;
    protected int letters;
    protected int digits;
    protected int mixedCase;
    protected int punctuation;

    /**
     * Ruft den Wert der passwordTracking-Eigenschaft ab.
     * 
     */
    public int getPasswordTracking() {
        return passwordTracking;
    }

    /**
     * Legt den Wert der passwordTracking-Eigenschaft fest.
     * 
     */
    public void setPasswordTracking(int value) {
        this.passwordTracking = value;
    }

    /**
     * Ruft den Wert der notificationMails-Eigenschaft ab.
     * 
     */
    public int getNotificationMails() {
        return notificationMails;
    }

    /**
     * Legt den Wert der notificationMails-Eigenschaft fest.
     * 
     */
    public void setNotificationMails(int value) {
        this.notificationMails = value;
    }

    /**
     * Ruft den Wert der expirationTime-Eigenschaft ab.
     * 
     */
    public int getExpirationTime() {
        return expirationTime;
    }

    /**
     * Legt den Wert der expirationTime-Eigenschaft fest.
     * 
     */
    public void setExpirationTime(int value) {
        this.expirationTime = value;
    }

    /**
     * Ruft den Wert der disableUserTime-Eigenschaft ab.
     * 
     */
    public int getDisableUserTime() {
        return disableUserTime;
    }

    /**
     * Legt den Wert der disableUserTime-Eigenschaft fest.
     * 
     */
    public void setDisableUserTime(int value) {
        this.disableUserTime = value;
    }

    /**
     * Ruft den Wert der differentCharacters-Eigenschaft ab.
     * 
     */
    public int getDifferentCharacters() {
        return differentCharacters;
    }

    /**
     * Legt den Wert der differentCharacters-Eigenschaft fest.
     * 
     */
    public void setDifferentCharacters(int value) {
        this.differentCharacters = value;
    }

    /**
     * Ruft den Wert der minimalPasswordLength-Eigenschaft ab.
     * 
     */
    public int getMinimalPasswordLength() {
        return minimalPasswordLength;
    }

    /**
     * Legt den Wert der minimalPasswordLength-Eigenschaft fest.
     * 
     */
    public void setMinimalPasswordLength(int value) {
        this.minimalPasswordLength = value;
    }

    /**
     * Ruft den Wert der letters-Eigenschaft ab.
     * 
     */
    public int getLetters() {
        return letters;
    }

    /**
     * Legt den Wert der letters-Eigenschaft fest.
     * 
     */
    public void setLetters(int value) {
        this.letters = value;
    }

    /**
     * Ruft den Wert der digits-Eigenschaft ab.
     * 
     */
    public int getDigits() {
        return digits;
    }

    /**
     * Legt den Wert der digits-Eigenschaft fest.
     * 
     */
    public void setDigits(int value) {
        this.digits = value;
    }

    /**
     * Ruft den Wert der mixedCase-Eigenschaft ab.
     * 
     */
    public int getMixedCase() {
        return mixedCase;
    }

    /**
     * Legt den Wert der mixedCase-Eigenschaft fest.
     * 
     */
    public void setMixedCase(int value) {
        this.mixedCase = value;
    }

    /**
     * Ruft den Wert der punctuation-Eigenschaft ab.
     * 
     */
    public int getPunctuation() {
        return punctuation;
    }

    /**
     * Legt den Wert der punctuation-Eigenschaft fest.
     * 
     */
    public void setPunctuation(int value) {
        this.punctuation = value;
    }

}
