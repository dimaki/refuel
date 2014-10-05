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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Dino Tsoumakis
 */
@XmlRootElement(name = "rss")
@XmlAccessorType(XmlAccessType.FIELD)
public class Appcast {

    @XmlAttribute
    String version;
    Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        if (channel == null) {
            return null;
        }
        return channel.getTitle();
    }

    public String getLatestVersion() {
        String version = null;
        Enclosure enclosure = getLatestEnclosure();
        if (enclosure != null) {
            version = enclosure.getVersion();
        }

        return version;
    }

    public Enclosure getLatestEnclosure() {
        Enclosure enclosure = null;
        Channel c = getChannel();
        if (c != null) {
            List<Item> items = c.getItems();
            if (items != null && !items.isEmpty()) {
                // Assume the latest version on top
                // Normally appcast should only contain one item
                Item item = items.get(0);
                if (item != null) {
                    enclosure = item.getEnclosure();
                }
            }
        }
        return enclosure;
    }
}
