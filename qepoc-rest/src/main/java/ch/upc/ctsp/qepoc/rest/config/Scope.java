/*
 * (c) 2013 panter llc, Zurich, Switzerland.
 */
package ch.upc.ctsp.qepoc.rest.config;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * TODO: add type comment.
 * 
 */
@XmlType(
        namespace = "http://www.upc-cablecom.ch/query-engine/config")
public enum Scope {
    @XmlEnumValue("public")
    PUBLIC, @XmlEnumValue("private")
    PRIVATE
}
