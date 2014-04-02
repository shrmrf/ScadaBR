package com.serotonin.mango.rt.maint;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import com.serotonin.mango.Common;
import br.org.scadabr.monitor.IntegerMonitor;
import br.org.scadabr.timer.CronTask;
import br.org.scadabr.timer.FixedRateTrigger;
import br.org.scadabr.timer.TimerTask;
import br.org.scadabr.timer.cron.CronExpression;

@Deprecated// "Whats this for?)"
public class WorkItemMonitor extends CronTask {

    /**
     * This method will set up the memory checking job. It assumes that the
     * corresponding system setting for running this job is true.
     */
    public static void start() {
        Common.systemCronPool.schedule(new WorkItemMonitor());
    }

    private final IntegerMonitor mediumPriorityServiceQueueSize = new IntegerMonitor(
            "WorkItemMonitor.mediumPriorityServiceQueueSize", null);
    private final IntegerMonitor scheduledTimerTaskCount = new IntegerMonitor(
            "WorkItemMonitor.scheduledTimerTaskCount", null);
    private final IntegerMonitor highPriorityServiceQueueSize = new IntegerMonitor(
            "WorkItemMonitor.highPriorityServiceQueueSize", null);
    private final IntegerMonitor maxStackHeight = new IntegerMonitor("WorkItemMonitor.maxStackHeight", null);
    private final IntegerMonitor threadCount = new IntegerMonitor("WorkItemMonitor.threadCount", null);

    private WorkItemMonitor() {
        super(CronExpression.createPeriodBySecond(10, 0));

        Common.MONITORED_VALUES.addIfMissingStatMonitor(mediumPriorityServiceQueueSize);
        Common.MONITORED_VALUES.addIfMissingStatMonitor(scheduledTimerTaskCount);
        Common.MONITORED_VALUES.addIfMissingStatMonitor(highPriorityServiceQueueSize);
        Common.MONITORED_VALUES.addIfMissingStatMonitor(maxStackHeight);
        Common.MONITORED_VALUES.addIfMissingStatMonitor(threadCount);
    }

    @Override
    public void run() {
        BackgroundProcessing bp = Common.ctx.getBackgroundProcessing();

        mediumPriorityServiceQueueSize.setValue(bp.getMediumPriorityServiceQueueSize());
        scheduledTimerTaskCount.setValue(Common.systemCronPool.poolSize());
        highPriorityServiceQueueSize.setValue(((ThreadPoolExecutor) Common.systemCronPool.getExecutorService()).getActiveCount());

        // Check the stack heights
        int max = 0;
        Collection<StackTraceElement[]> stacks = Thread.getAllStackTraces().values();
        threadCount.setValue(stacks.size());
        for (StackTraceElement[] stack : stacks) {
            if (max < stack.length) {
                max = stack.length;
            }
        }
        maxStackHeight.setValue(max);
    }
}
