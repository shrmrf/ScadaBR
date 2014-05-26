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
package com.serotonin.mango.web.mvc.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import br.org.scadabr.db.IntValuePair;
import com.serotonin.mango.Common;
import com.serotonin.mango.db.dao.WatchListDao;
import com.serotonin.mango.view.ShareUser;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.vo.WatchList;
import com.serotonin.mango.vo.permission.Permissions;
import br.org.scadabr.web.l10n.Localizer;
import javax.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/watch_list.shtm")
public class WatchListController {

    @Inject
    protected WatchListDao watchListDao;
    
    public static final String KEY_WATCHLISTS = "watchLists";
    public static final String KEY_SELECTED_WATCHLIST = "selectedWatchList";

    @RequestMapping(method = RequestMethod.GET)
    public String initializeForm(ModelMap model, HttpServletRequest request) {
        createModel(request, model);
        return "watchList";
    }

    protected void createModel(HttpServletRequest request, ModelMap modelMap) {
        User user = Common.getUser(request);

        // The user's permissions may have changed since the last session, so make sure the watch lists are correct.
        List<WatchList> watchLists = watchListDao.getWatchLists(user.getId());

        if (watchLists.isEmpty()) {
            // Add a default watch list if none exist.
            WatchList watchList = new WatchList();
            watchList.setName(Localizer.localizeI18nKey("common.newName", ControllerUtils.getResourceBundle(request)));
            watchLists.add(watchListDao.createNewWatchList(watchList, user.getId()));
        }

        int selected = user.getSelectedWatchList();
        boolean found = false;

        List<IntValuePair> watchListNames = new ArrayList<>(watchLists.size());
        for (WatchList watchList : watchLists) {
            if (watchList.getId() == selected) {
                found = true;
            }

            if (watchList.getUserAccess(user) == ShareUser.ACCESS_OWNER) {
                // If this is the owner, check that the user still has access to the points. If not, remove the
                // unauthorized points, resave, and continue.
                boolean changed = false;
                List<DataPointVO> list = watchList.getPointList();
                List<DataPointVO> copy = new ArrayList<>(list);
                for (DataPointVO point : copy) {
                    if (point == null || !Permissions.hasDataPointReadPermission(user, point)) {
                        list.remove(point);
                        changed = true;
                    }
                }

                if (changed) {
                    watchListDao.saveWatchList(watchList);
                }
            }

            watchListNames.add(new IntValuePair(watchList.getId(), watchList.getName()));
        }

        if (!found) {
            // The user's default watch list was not found. It was either deleted or unshared from them. Find a new one.
            // The list will always contain at least one, so just use the id of the first in the list.
            selected = watchLists.get(0).getId();
            user.setSelectedWatchList(selected);
            watchListDao.saveSelectedWatchList(user.getId(), selected);
        }

        modelMap.put(KEY_WATCHLISTS, watchListNames);
        modelMap.put(KEY_SELECTED_WATCHLIST, selected);
        modelMap.put("NEW_ID", Common.NEW_ID);
    }
}