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
package com.serotonin.mango.view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import br.org.scadabr.json.JsonArray;
import br.org.scadabr.json.JsonException;
import br.org.scadabr.json.JsonObject;
import br.org.scadabr.json.JsonReader;
import br.org.scadabr.json.JsonRemoteEntity;
import br.org.scadabr.json.JsonRemoteProperty;
import br.org.scadabr.json.JsonSerializable;
import br.org.scadabr.json.JsonValue;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.UserDao;
import com.serotonin.mango.db.dao.ViewDao;
import com.serotonin.mango.util.LocalizableJsonException;
import com.serotonin.mango.view.component.CompoundComponent;
import com.serotonin.mango.view.component.PointComponent;
import com.serotonin.mango.view.component.ViewComponent;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import br.org.scadabr.web.dwr.DwrResponseI18n;

@JsonRemoteEntity
public class View implements Serializable, JsonSerializable {

    public static final String XID_PREFIX = "GV_";

    private int id = Common.NEW_ID;
    @JsonRemoteProperty
    private String xid;
    @JsonRemoteProperty
    private String name;
    @JsonRemoteProperty
    private String backgroundFilename;
    private int userId;
    private List<ViewComponent> viewComponents = new CopyOnWriteArrayList<>();
    private int anonymousAccess = ShareUser.ACCESS_NONE;
    private List<ShareUser> viewUsers = new CopyOnWriteArrayList<>();

    public void addViewComponent(ViewComponent viewComponent) {
        // Determine an index for the component.
        int min = 0;
        for (ViewComponent vc : viewComponents) {
            if (min < vc.getIndex()) {
                min = vc.getIndex();
            }
        }
        viewComponent.setIndex(min + 1);

        viewComponents.add(viewComponent);
    }

    public ViewComponent getViewComponent(int index) {
        for (ViewComponent vc : viewComponents) {
            if (vc.getIndex() == index) {
                return vc;
            }
        }
        return null;
    }

    public void removeViewComponent(ViewComponent vc) {
        if (vc != null) {
            viewComponents.remove(vc);
        }
    }

    public boolean isNew() {
        return id == Common.NEW_ID;
    }

    public boolean containsValidVisibleDataPoint(int dataPointId) {
        for (ViewComponent vc : viewComponents) {
            if (vc.containsValidVisibleDataPoint(dataPointId)) {
                return true;
            }
        }
        return false;
    }

    public DataPointVO findDataPoint(String viewComponentId) {
        for (ViewComponent vc : viewComponents) {
            if (vc.isPointComponent()) {
                if (vc.getId().equals(viewComponentId)) {
                    return ((PointComponent) vc).tgetDataPoint();
                }
            } else if (vc.isCompoundComponent()) {
                PointComponent pc = ((CompoundComponent) vc)
                        .findPointComponent(viewComponentId);
                if (pc != null) {
                    return pc.tgetDataPoint();
                }
            }
        }
        return null;
    }

    public int getUserAccess(User user) {
        if (user == null) {
            return anonymousAccess;
        }

        if (userId == user.getId()) {
            return ShareUser.ACCESS_OWNER;
        }

        for (ShareUser vu : viewUsers) {
            if (vu.getUserId() == user.getId()) {
                return vu.getAccessType();
            }
        }
        return ShareUser.ACCESS_NONE;
    }

    /**
     * This method is used before the view is displayed in order to validate: -
     * that the given user is allowed to access points that back any components
     * - that the points that back components still have valid data types for
     * the components that render them
     */
    public void validateViewComponents(boolean makeReadOnly) {
        User owner = UserDao.getInstance().getUser(userId);
        for (ViewComponent viewComponent : viewComponents) {
            viewComponent.validateDataPoint(owner, makeReadOnly);
        }
    }

    public String getBackgroundFilename() {
        return backgroundFilename;
    }

    public void setBackgroundFilename(String backgroundFilename) {
        this.backgroundFilename = backgroundFilename;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ViewComponent> getViewComponents() {
        return viewComponents;
    }

    public int getAnonymousAccess() {
        return anonymousAccess;
    }

    public void setAnonymousAccess(int anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<ShareUser> getViewUsers() {
        return viewUsers;
    }

    public void setViewUsers(List<ShareUser> viewUsers) {
        this.viewUsers = viewUsers;
    }

    public void validate(DwrResponseI18n response) {
        if (name.isEmpty()) {
            response.addContextual("name",  "validate.required");
        } else if (name.length() > 100) {
            response.addContextual("name", "validate.notLongerThan", 100);
        }

        if (xid.isEmpty()) {
            response.addContextual("xid", "validate.required");
        } else if (xid.length() >  50) {
            response.addContextual("xid", "validate.notLongerThan", 50);
        } else if (!ViewDao.getInstance().isXidUnique(xid, id)) {
            response.addContextual("xid", "validate.xidUsed");
        }

        for (ViewComponent vc : viewComponents) {
            vc.validate(response);
        }
    }

	//
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeObject(viewComponents);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int ver = in.readInt();

		// Switch on the version of the class so that version changes can be
        // elegantly handled.
        if (ver == 1) {
            viewComponents = new CopyOnWriteArrayList<>(
                    (List<ViewComponent>) in.readObject());
        }
    }

    @Override
    public void jsonDeserialize(JsonReader reader, JsonObject json)
            throws JsonException {
        if (isNew()) {
            String username = json.getString("user");
            if (username.isEmpty()) {
                throw new LocalizableJsonException("emport.error.missingValue",
                        "user");
            }
            User user = UserDao.getInstance().getUser(username);
            if (user == null) {
                throw new LocalizableJsonException("emport.error.missingUser",
                        username);
            }
            userId = user.getId();
        }

        JsonArray components = json.getJsonArray("viewComponents");
        if (components != null) {
            viewComponents.clear();
            for (JsonValue jv : components.getElements()) {
                addViewComponent(reader.readPropertyValue(jv,
                        ViewComponent.class, null));
            }
        }

        String text = json.getString("anonymousAccess");
        if (text != null) {
            anonymousAccess = ShareUser.ACCESS_CODES.getId(text);
            if (anonymousAccess == -1) {
                throw new LocalizableJsonException("emport.error.invalid",
                        "anonymousAccess", text, ShareUser.ACCESS_CODES
                        .getCodeList());
            }
        }

        JsonArray jsonSharers = json.getJsonArray("sharingUsers");
        if (jsonSharers != null) {
            viewUsers.clear();

            for (JsonValue jv : jsonSharers.getElements()) {
                ShareUser shareUser = reader.readPropertyValue(jv,
                        ShareUser.class, null);
                if (shareUser.getUserId() != userId) // No need for the owning user to be in this list.
                {
                    viewUsers.add(shareUser);
                }
            }
        }
    }

    @Override
    public void jsonSerialize(Map<String, Object> map) {
        map.put("user", UserDao.getInstance().getUser(userId).getUsername());
        map.put("anonymousAccess", ShareUser.ACCESS_CODES
                .getCode(anonymousAccess));
        map.put("viewComponents", viewComponents);
        map.put("sharingUsers", viewUsers);
    }
}