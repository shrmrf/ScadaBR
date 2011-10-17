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
package com.serotonin.mango.db.upgrade;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthew Lohbihler
 */
public class Upgrade0_8_1 extends DBUpgrade {
    private final static Logger LOG = LoggerFactory.getLogger(Upgrade0_8_1.class);

    @Override
    public void upgrade() throws Exception {
        OutputStream out = createUpdateLogOutputStream("0_8_1");

        // Run the script.
        LOG.info("Running script");
        runScript(script, out);

        out.flush();
        out.close();
    }

    @Override
    protected String getNewSchemaVersion() {
        return "0.9.0";
    }

    private static String[] script = {
            // Create the new watch list tables.
            "create table watchLists ( ",
            "  id int not null generated by default as identity (start with 1, increment by 1), ",
            "  userId int not null, ",
            "  name varchar(50) ",
            ");",
            "alter table watchLists add constraint watchListsPk primary key (id);",
            "alter table watchLists add constraint watchListsFk1 foreign key (userId) references users(id);",

            "create table watchListPoints ( ",
            "  watchListId int not null, ",
            "  dataPointId int not null, ",
            "  sortOrder int not null ",
            ");",
            "alter table watchListPoints add constraint watchListPointsFk1 foreign key (watchListId) references watchLists(id); ",
            "alter table watchListPoints add constraint watchListPointsFk2 foreign key (dataPointId) references dataPoints(id); ",

            "alter table users add column selectedWatchList int;",

            // Transfer old data into the new tables.
            "insert into watchLists (userId, name) select distinct userId, '(unnamed)' from userWatchList;",
            "insert into watchListPoints select wl.id, uwl.dataPointId, uwl.sortOrder from watchLists wl, userWatchList uwl where wl.userId=uwl.userId;",

            // Delete the old tables
            "drop table userWatchList;", };
}
