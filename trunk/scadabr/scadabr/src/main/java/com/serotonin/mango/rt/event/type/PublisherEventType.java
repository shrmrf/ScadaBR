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
package com.serotonin.mango.rt.event.type;

import java.util.Map;

import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.rt.event.type.DuplicateHandling;
import br.org.scadabr.rt.event.type.EventSources;
import br.org.scadabr.vo.event.AlarmLevel;
import com.serotonin.mango.db.dao.PublisherDao;
import com.serotonin.mango.vo.publish.PublisherVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Matthew Lohbihler
 */
@JsonRemoteEntity
@Configurable
public class PublisherEventType extends EventType {

    @Autowired
    private PublisherDao publisherDao;
    
    private int publisherId;
    private int publisherEventTypeId;
    private AlarmLevel alarmLevel;

    public PublisherEventType() {
        // Required for reflection.
    }

    @Deprecated
    public PublisherEventType(int publisherId, int publisherEventTypeId) {
        this.publisherId = publisherId;
        this.publisherEventTypeId = publisherEventTypeId;
//        this.alarmLevel = alarmLevel;
    }

    public PublisherEventType(PublisherVO vo, int publisherEventTypeId) {
        this.publisherId = vo.getId();
        this.publisherEventTypeId = publisherEventTypeId;
        this.alarmLevel = AlarmLevel.URGENT;
    }

    @Override
    public EventSources getEventSource() {
        return EventSources.PUBLISHER;
    }

    public int getPublisherEventTypeId() {
        return publisherEventTypeId;
    }

    @Override
    public int getPublisherId() {
        return publisherId;
    }

    @Override
    public String toString() {
        return "PublisherEventType(publisherId=" + publisherId + ", eventTypeId=" + publisherEventTypeId + ")";
    }

    @Override
    public DuplicateHandling getDuplicateHandling() {
        return DuplicateHandling.IGNORE;
    }

    public int getReferenceId1() {
        return publisherId;
    }

    public int getReferenceId2() {
        return publisherEventTypeId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + publisherEventTypeId;
        result = prime * result + publisherId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PublisherEventType other = (PublisherEventType) obj;
        if (publisherEventTypeId != other.publisherEventTypeId) {
            return false;
        }
        if (publisherId != other.publisherId) {
            return false;
        }
        return true;
    }

    //
    // /
    // / Serialization
    // /
    //
    @Override
    public void jsonSerialize(Map<String, Object> map) {
        super.jsonSerialize(map);
        PublisherVO<?> pub = publisherDao.getPublisher(publisherId);
        map.put("XID", pub.getXid());
        map.put("publisherEventTypeId", pub.getEventCodes().getCode(publisherEventTypeId));
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json) throws JsonException {
        super.jsonDeserialize(reader, json);
        PublisherVO<?> pb = getPublisher(json, "XID");
        publisherId = pb.getId();
        publisherEventTypeId = getInt(json, "publisherEventTypeId", pb.getEventCodes());
    }

    @Override
    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }

    @Override
    public boolean isStateful() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
