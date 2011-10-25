--
--    Mango - Open Source M2M - http://mango.serotoninsoftware.com
--    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
--    @author Matthew Lohbihler
--    
--    This program is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    This program is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with this program.  If not, see <http://www.gnu.org/licenses/>.
--
--

--
-- System settings
create table systemSettings (
  settingName varchar(32) not null,
  settingValue clob
);
alter table systemSettings add constraint systemSettingsPk primary key (settingName);


--
-- Users
create table users (
  id int generated by default as identity primary key,
  mangoUsername varchar(40) not null,
  mangoUserPassword varchar(30) not null,
  email varchar(255) not null,
  phone varchar(40),
  mangoAdmin boolean not null,
  disabled boolean not null,
  lastLogin timestamp,
  selectedWatchList int,
  homeUrl varchar(255),
  receiveAlarmEmails varchar(16) not null,
  receiveOwnAuditEvents boolean not null
);
alter table users add constraint usersUni unique (mangoUsername);
create index userNameIdx on users (mangoUsername);

create table userComments (
  userId int,
  commentType int not null,
  typeKey int not null,
  ts bigint not null,
  commentText varchar(1024) not null
);
alter table userComments add constraint userCommentsFk1 foreign key (userId) references users(id);


--
-- Mailing lists
create table mailingLists (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  mailingListName varchar(40) not null
);
alter table mailingLists add constraint mailingListsUn1 unique (xid);

create table mailingListInactive (
  mailingListId int not null,
  inactiveInterval int not null
);
alter table mailingListInactive add constraint mailingListInactiveFk1 foreign key (mailingListId) 
  references mailingLists(id) on delete cascade;

create table mailingListMembers (
  mailingListId int not null,
  typeId int not null,
  userId int not null,
  address varchar(255)
);
alter table mailingListMembers add constraint mailingListMembersFk1 foreign key (mailingListId) 
  references mailingLists(id) on delete cascade;
alter table mailingListMembers add constraint mailingListMembersFk2 foreign key (userId) 
  references users(id) on delete cascade;

--
--
-- Data Sources
--
create table dataSources (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  dataSourceName varchar(40) not null,
  dataSourceType varchar(48) not null,
  jsonSerialized clob not null
);
alter table dataSources add constraint dataSourcesUn1 unique (xid);


-- Data source permissions
create table dataSourceUsers (
  dataSourceId int not null,
  userId int not null
);
alter table dataSourceUsers add constraint dataSourceUsersFk1 foreign key (dataSourceId) references dataSources(id) on delete cascade;
alter table dataSourceUsers add constraint dataSourceUsersFk2 foreign key (userId) references users(id) on delete cascade;

--
--
-- scripts
--
create table scripts (
  id int generated by default as identity primary key,
  userId int not null,
  xid varchar(50) not null,
  scriptName varchar(40) not null,
  script varchar(16384) not null,
  scriptData blob not null
);
alter table scripts add constraint scriptsFk1 foreign key (userId) references users(id);

--
--
-- Flex Projects
--
create table flexProjects(
  id int generated by default as identity primary key,
  flexProjectName varchar(40) not null,
  description varchar(1024),
  xmlConfig varchar(16384) not null
);

--
--
-- Data Points
--
create table dataPoints (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  dataSourceId int not null,
  dataPointData blob not null
);
alter table dataPoints add constraint dataPointsUn1 unique (xid);
alter table dataPoints add constraint dataPointsFk1 foreign key (dataSourceId) references dataSources(id);


-- Data point permissions
create table dataPointUsers (
  dataPointId int not null,
  userId int not null,
  dataPointUserPermission int not null
);
alter table dataPointUsers add constraint dataPointUsersFk1 foreign key (dataPointId) references dataPoints(id);
alter table dataPointUsers add constraint dataPointUsersFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Views
--
create table mangoViews (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  mangoViewName varchar(100) not null,
  background varchar(255),
  userId int not null,
  anonymousAccess int not null,
  mangoViewData blob not null
);
alter table mangoViews add constraint mangoViewsUn1 unique (xid);
alter table mangoViews add constraint mangoViewsFk1 foreign key (userId) references users(id) on delete cascade;

create table mangoViewUsers (
  mangoViewId int not null,
  userId int not null,
  accessType int not null
);
alter table mangoViewUsers add constraint mangoViewUsersPk primary key (mangoViewId, userId);
alter table mangoViewUsers add constraint mangoViewUsersFk1 foreign key (mangoViewId) references mangoViews(id) on delete cascade;
alter table mangoViewUsers add constraint mangoViewUsersFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Point Values (historical data)
--
create table pointValues (
  id bigint generated by default as identity primary key,
  dataPointId int not null,
  dataType int not null,
  pointValue double,
  ts bigint not null
);
alter table pointValues add constraint pointValuesFk1 foreign key (dataPointId) references dataPoints(id) on delete cascade;
create index pointValuesIdx1 on pointValues (ts, dataPointId);
create index pointValuesIdx2 on pointValues (dataPointId, ts);

create table pointValueAnnotations (
  pointValueId bigint not null,
  textPointValueShort varchar(128),
  textPointValueLong clob,
  sourceType smallint,
  sourceId int
);
alter table pointValueAnnotations add constraint pointValueAnnotationsFk1 foreign key (pointValueId) 
  references pointValues(id) on delete cascade;


--
--
-- Watch list
--
create table watchLists (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  userId int not null,
  watchListName varchar(50)
);
alter table watchLists add constraint watchListsUn1 unique (xid);
alter table watchLists add constraint watchListsFk1 foreign key (userId) references users(id) on delete cascade;

create table watchListPoints (
  watchListId int not null,
  dataPointId int not null,
  sortOrder int not null
);
alter table watchListPoints add constraint watchListPointsFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
alter table watchListPoints add constraint watchListPointsFk2 foreign key (dataPointId) references dataPoints(id);

create table watchListUsers (
  watchListId int not null,
  userId int not null,
  accessType int not null
);
alter table watchListUsers add constraint watchListUsersPk primary key (watchListId, userId);
alter table watchListUsers add constraint watchListUsersFk1 foreign key (watchListId) references watchLists(id) on delete cascade;
alter table watchListUsers add constraint watchListUsersFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Point event detectors
--
create table pointEventDetectors (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  pointEventDetectorAlias varchar(255),
  dataPointId int not null,
  detectorType int not null,
  alarmLevel int not null,
  stateLimit double,
  duration int,
  durationType int,
  binaryState char(1),
  multistateState int,
  changeCount int,
  alphanumericState varchar(128),
  weight double
);
alter table pointEventDetectors add constraint pointEventDetectorsUn1 unique (xid, dataPointId);
alter table pointEventDetectors add constraint pointEventDetectorsFk1 foreign key (dataPointId) 
  references dataPoints(id);


--
--
-- Events
--
create table events (
  id int generated by default as identity primary key,
  typeId int not null,
  typeRef1 int not null,
  typeRef2 int not null,
  activeTs bigint not null,
  rtnApplicable char(1) not null,
  rtnTs bigint,
  rtnCause int,
  alarmLevel int not null,
  message clob,
  ackTs bigint,
  ackUserId int,
  alternateAckSource int
);
alter table events add constraint eventsFk1 foreign key (ackUserId) references users(id);

create table userEvents (
  eventId int not null,
  userId int not null,
  silenced char(1) not null
);
alter table userEvents add constraint userEventsPk primary key (eventId, userId);
alter table userEvents add constraint userEventsFk1 foreign key (eventId) references events(id) on delete cascade;
alter table userEvents add constraint userEventsFk2 foreign key (userId) references users(id) on delete cascade;


--
--
-- Event handlers
--
create table eventHandlers (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  eventAlias varchar(255),
  eventTypeId int not null,
  eventTypeRef1 int not null,
  eventTypeRef2 int not null,
  eventData blob not null
);
alter table eventHandlers add constraint eventHandlersUn1 unique (xid);


--
--
-- Scheduled events
--
create table scheduledEvents (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  scheduledEventAlias varchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  returnToNormal char(1) not null,
  disabled char(1) not null,
  activeYear int,
  activeMonth int,
  activeDay int,
  activeHour int,
  activeMinute int,
  activeSecond int,
  activeCron varchar(25),
  inactiveYear int,
  inactiveMonth int,
  inactiveDay int,
  inactiveHour int,
  inactiveMinute int,
  inactiveSecond int,
  inactiveCron varchar(25)
);
alter table scheduledEvents add constraint scheduledEventsUn1 unique (xid);


--
--
-- Point Hierarchy
--
create table pointHierarchy (
  id int generated by default as identity primary key,
  parentId int,
  pointHierarchyName varchar(100)
);


--
--
-- Compound events detectors
--
create table compoundEventDetectors (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  compoundEventDetectorName varchar(100),
  alarmLevel int not null,
  returnToNormal char(1) not null,
  disabled char(1) not null,
  conditionText varchar(256) not null
);
alter table compoundEventDetectors add constraint compoundEventDetectorsUn1 unique (xid);


--
--
-- Reports
--
create table reports (
  id int generated by default as identity primary key,
  userId int not null,
  reportName varchar(100) not null,
  reportData blob not null
);
alter table reports add constraint reportsFk1 foreign key (userId) references users(id) on delete cascade;

create table reportInstances (
  id int generated by default as identity primary key,
  userId int not null,
  reportInstancename varchar(100) not null,
  includeEvents int not null,
  includeUserComments char(1) not null,
  reportStartTime bigint not null,
  reportEndTime bigint not null,
  runStartTime bigint,
  runEndTime bigint,
  recordCount int,
  preventPurge char(1)
);
alter table reportInstances add constraint reportInstancesFk1 foreign key (userId) references users(id) on delete cascade;

create table reportInstancePoints (
  id int generated by default as identity primary key,
  reportInstanceId int not null,
  dataSourceName varchar(40) not null,
  pointName varchar(100) not null,
  dataType int not null,
  startValue varchar(4096),
  textRenderer blob,
  colour varchar(6),
  consolidatedChart char(1)
);
alter table reportInstancePoints add constraint reportInstancePointsFk1 foreign key (reportInstanceId) 
  references reportInstances(id) on delete cascade;

create table reportInstanceData (
  pointValueId bigint not null,
  reportInstancePointId int not null,
  pointValue double,
  ts bigint not null
);
alter table reportInstanceData add constraint reportInstanceDataPk primary key (pointValueId, reportInstancePointId);
alter table reportInstanceData add constraint reportInstanceDataFk1 foreign key (reportInstancePointId) 
  references reportInstancePoints(id) on delete cascade;

create table reportInstanceDataAnnotations (
  pointValueId bigint not null,
  reportInstancePointId int not null,
  textPointValueShort varchar(128),
  textPointValueLong clob,
  sourceValue varchar(128)
);
alter table reportInstanceDataAnnotations add constraint reportInstanceDataAnnotationsPk 
  primary key (pointValueId, reportInstancePointId);
alter table reportInstanceDataAnnotations add constraint reportInstanceDataAnnotationsFk1 
  foreign key (pointValueId, reportInstancePointId) references reportInstanceData(pointValueId, reportInstancePointId) 
  on delete cascade;

create table reportInstanceEvents (
  eventId int not null,
  reportInstanceId int not null,
  typeId int not null,
  typeRef1 int not null,
  typeRef2 int not null,
  activeTs bigint not null,
  rtnApplicable char(1) not null,
  rtnTs bigint,
  rtnCause int,
  alarmLevel int not null,
  message clob,
  ackTs bigint,
  ackUsername varchar(40),
  alternateAckSource int
);
alter table reportInstanceEvents add constraint reportInstanceEventsPk primary key (eventId, reportInstanceId);
alter table reportInstanceEvents add constraint reportInstanceEventsFk1 foreign key (reportInstanceId)
  references reportInstances(id) on delete cascade;

create table reportInstanceUserComments (
  reportInstanceId int not null,
  username varchar(40),
  commentType int not null,
  typeKey int not null,
  ts bigint not null,
  commentText varchar(1024) not null
);
alter table reportInstanceUserComments add constraint reportInstanceUserCommentsFk1 foreign key (reportInstanceId)
  references reportInstances(id) on delete cascade;


--
--
-- Publishers
--
create table publishers (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  publisherData blob not null
);
alter table publishers add constraint publishersUn1 unique (xid);


--
--
-- Point links
--
create table pointLinks (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  sourcePointId int not null,
  targetPointId int not null,
  script clob,
  eventType int not null,
  disabled char(1) not null
);
alter table pointLinks add constraint pointLinksUn1 unique (xid);


--
--
-- Maintenance events
--
create table maintenanceEvents (
  id int generated by default as identity primary key,
  xid varchar(50) not null,
  dataSourceId int not null,
  eventAlias varchar(255),
  alarmLevel int not null,
  scheduleType int not null,
  disabled char(1) not null,
  activeYear int,
  activeMonth int,
  activeDay int,
  activeHour int,
  activeMinute int,
  activeSecond int,
  activeCron varchar(25),
  inactiveYear int,
  inactiveMonth int,
  inactiveDay int,
  inactiveHour int,
  inactiveMinute int,
  inactiveSecond int,
  inactiveCron varchar(25)
);
alter table maintenanceEvents add constraint maintenanceEventsUn1 unique (xid);
alter table maintenanceEvents add constraint maintenanceEventsFk1 foreign key (dataSourceId) references dataSources(id);
