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
package com.serotonin.mango.rt.dataSource;

import br.org.scadabr.ImplementMeException;

import br.org.scadabr.logger.LogUtils;
import br.org.scadabr.timer.cron.CronExpression;
import br.org.scadabr.timer.cron.DataSourceCronTask;
import com.serotonin.mango.Common;
import com.serotonin.mango.vo.dataSource.DataSourceVO;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class PollingDataSource<T extends DataSourceVO<T>> extends DataSourceRT<T> {

    private final static Logger LOG = Logger.getLogger(LogUtils.LOGGER_SCARABR_DS_RT);

    private DataSourceCronTask timerTask;

    public PollingDataSource(T vo, boolean doCache) {
        super(vo, doCache);
    }

    abstract public void doPoll(long time);

    //
    //
    // Data source interface
    //
    @Override
    public void beginPolling() {
        try {
            timerTask = new DataSourceCronTask(this, getCronExpression());
            super.beginPolling();
            Common.dataSourcePool.schedule(timerTask);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void terminate() {
        if (timerTask != null) {
            timerTask.cancel();
            if (!timerTask.isExecuting()) {
                timerTask = null;
            }
        }
        super.terminate();
    }

    @Override
    public void joinTermination() {
        super.joinTermination();

        final DataSourceCronTask local = timerTask;
        if (local != null) {
//TODO ???
            /*            try {
                local.join(30000); // 30 seconds
            } catch (InterruptedException e) { /* no op 

            }
  */
          if (timerTask != null) {
//                throw new ShouldNeverHappenException("Timeout waiting for data source to stop: id=" + getId() + ", type=" + getClass() + ", stackTrace=" + Arrays.toString(localThread.getStackTrace()));
            }
        }
    }
    
    protected abstract CronExpression getCronExpression() throws ParseException;
    
    @Deprecated
    protected void setPollingPeriod(int updatePeriodType, int updatePeriods, boolean quantize) {
        // Set cronpattern from this
        throw new ImplementMeException();
    }

    public boolean overrunDetected(long lastExecutionTime, long thisExecutionTime) {
            LOG.log(Level.WARNING, "{0}: poll at {1} aborted because a previous poll started at {2} is still running", new Object[]{vo.getName(), new Date(thisExecutionTime), new Date(lastExecutionTime)});
            return false;
    }
    
}