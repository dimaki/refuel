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
package de.dimaki.refuel.updater.control;

import de.dimaki.refuel.appcast.entity.Appcast;
import java.util.Date;

/**
 *
 * @author Dino Tsoumakis
 */
public enum ApplicationStatus {
    OK,
    UPDATE_AVAILABLE,
    UNKNOWN,
    NOT_INSTALLED,
    DISABLED,
    FAILURE;

    String info;
    Date updateTime;
    Appcast appcast;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Appcast getAppcast() {
        return appcast;
    }

    public void setAppcast(Appcast appcast) {
        this.appcast = appcast;
    }

    @Override
    public String toString() {
        return name() + " {info=" + info + ", updateTime=" + updateTime + '}';
    }


}
