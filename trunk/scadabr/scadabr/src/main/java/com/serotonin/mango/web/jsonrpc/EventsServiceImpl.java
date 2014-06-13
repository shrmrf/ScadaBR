package com.serotonin.mango.web.jsonrpc;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.web.l10n.Localizer;
import com.serotonin.mango.db.dao.EventDao;
import com.serotonin.mango.rt.event.EventInstance;
import com.serotonin.mango.vo.User;
import com.serotonin.mango.web.UserSessionContextBean;
import java.util.Collection;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Named
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EventsServiceImpl implements EventsService {

    private static final Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCADABR_WEB);

    @Inject
    private Localizer localizer;
    @Inject
    private UserSessionContextBean userSessionContextBean;
    @Inject
    private EventDao eventDao;

    @Override
    public Collection<JsonEventInstance> acknowledgeAllPendingEvents() {
        final User user = userSessionContextBean.getUser();
        if (user != null) {
            long now = System.currentTimeMillis();
            for (EventInstance evt : eventDao.getPendingEvents(user)) {
                eventDao.ackEvent(evt.getId(), now, user.getId(), 0);
            }
//TODO impl            MiscDWR.resetLastAlarmLevelChange();
        }
        return JsonEventInstance.wrap(eventDao.getPendingEvents(user), localizer);

    }

    @Override
    public Collection<JsonEventInstance> acknowledgePendingEvent(int eventId) {
        final User user = userSessionContextBean.getUser();
        if (user != null) {
            long now = System.currentTimeMillis();
            eventDao.ackEvent(eventId, now, user.getId(), 0);
//TODO impl            MiscDWR.resetLastAlarmLevelChange();
        }
        return JsonEventInstance.wrap(eventDao.getPendingEvents(user), localizer);

    }

}