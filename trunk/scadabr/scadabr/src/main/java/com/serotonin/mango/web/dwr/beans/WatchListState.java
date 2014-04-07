/*
 Mango - Open Source M2M - http://mango.serotoninsoftware.com
 Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
 @author Matthew Lohbihler
    
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.web.dwr.beans;

import br.org.scadabr.ShouldNeverHappenException;
import java.util.Date;
import java.util.Objects;

public class WatchListState extends BasePointState {

    private String value;
    private Date time;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public WatchListState clone() throws CloneNotSupportedException {
        try {
            return (WatchListState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    public void removeEqualValue(WatchListState that) {
        super.removeEqualValue(that);
        if (Objects.equals(value, that.value)) {
            value = null;
        }
        if (Objects.equals(time, that.time)) {
            time = null;
        }
    }

    @Override
    public boolean isEmpty() {
        return value == null && time == null && super.isEmpty();
    }
}
