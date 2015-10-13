/*
 * Copyright 2014 Dino Tsoumakis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dimaki.refuel.appcast.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Dino Tsoumakis
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Enclosure {
    @XmlAttribute
    String url;
    @XmlAttribute
    long length;
    @XmlAttribute
    String type;
    @XmlAttribute(namespace="http://www.andymatuschak.org/xml-namespaces/sparkle")
    String version;
    @XmlAttribute(namespace="http://www.andymatuschak.org/xml-namespaces/sparkle")
    String shortVersionString;
    @XmlAttribute(namespace="http://www.andymatuschak.org/xml-namespaces/sparkle")
    String dsaSignature;
    @XmlAttribute(namespace="http://www.andymatuschak.org/xml-namespaces/sparkle")
    String md5;
    @XmlAttribute(namespace="http://www.andymatuschak.org/xml-namespaces/sparkle")
    String sha1;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShortVersionString() {
        return shortVersionString;
    }

    public void setShortVersionString(String shortVersionString) {
        this.shortVersionString = shortVersionString;
    }

    public String getDsaSignature() {
        return dsaSignature;
    }

    public void setDsaSignature(String dsaSignature) {
        this.dsaSignature = dsaSignature;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
}
