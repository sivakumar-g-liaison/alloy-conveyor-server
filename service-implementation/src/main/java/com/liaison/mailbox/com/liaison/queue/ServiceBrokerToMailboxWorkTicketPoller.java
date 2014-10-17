package com.liaison.mailbox.com.liaison.queue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.commons.util.settings.DecryptableConfiguration;
import com.liaison.commons.util.settings.LiaisonConfigurationFactory;
import com.liaison.mailbox.service.core.processor.ServiceBrokerToMailboxWorkTicketConsumer;

public class ServiceBrokerToMailboxWorkTicketPoller {
	private static final Logger logger = LogManager.getLogger(ServiceBrokerToMailboxWorkTicketPoller.class);

    private static final DecryptableConfiguration configuration = LiaisonConfigurationFactory.getConfiguration();

    private static final int POOL_SIZE = 1;

    private static final ScheduledExecutorService pool = Executors.newScheduledThreadPool(POOL_SIZE);



    private static final long DEFAULT_INITIAL_DELAY = 10000;
    public static final String PROPERTY_WATCHDOG_QUEUE_POLLER_INITIALDELAY = "com.liaison.mailboxToServiceBrokerWorkTicket.queue.poller.initialdelay";

    private static final long DEFAULT_INTERVAL_DELAY = 1000;
    public static final String PROPERTY_WATCHDOG_QUEUE_POLLER_INTERVAL_DELAY = "com.liaison.mailboxToServiceBrokerWorkTicket.queue.poller.intervaldelay";

    private static final String DEFAULT_TIME_UNIT_NAME = TimeUnit.MILLISECONDS.name();
    public static final String PROPERTY_WATCHDOG_QUEUE_POLLER_TIMEUNIT = "com.liaison.mailboxToServiceBrokerWorkTicket.queue.poller.timeunit";

    private static final long INITIAL_DELAY = configuration.getLong(PROPERTY_WATCHDOG_QUEUE_POLLER_INITIALDELAY, DEFAULT_INITIAL_DELAY);
    private static final long INTERVAL_DELAY = configuration.getLong(PROPERTY_WATCHDOG_QUEUE_POLLER_INTERVAL_DELAY, DEFAULT_INTERVAL_DELAY);
    private static final TimeUnit DELAY_TIME_UNIT = TimeUnit.valueOf(configuration.getString(PROPERTY_WATCHDOG_QUEUE_POLLER_TIMEUNIT,DEFAULT_TIME_UNIT_NAME));

    private static boolean started = false;

    public static synchronized void startPolling() {
        logger.debug("startPolling");
        if (started) {
            return;
        }
        final Runnable messageProcessor = new Runnable() {
            public void run() {
                logger.debug("Polling message Process");
                String message = ServiceBrokerToMailboxWorkTicket.getInstance().receiveMessage();
                if (message != null) {
                    logger.debug("Polling message found {}", message);
                    
                    try {
                    	ServiceBrokerToMailboxWorkTicketConsumer qconsumer = ServiceBrokerToMailboxWorkTicketConsumer.getMailboxWatchDogQueueConsumerInstance();
                        qconsumer.invokeWatchDog(message);
                    } catch (Exception e) {
                        logger.error("Recovering from processing error", e);
                    }
                }
            }
        };
       pool.scheduleWithFixedDelay(messageProcessor, INITIAL_DELAY, INTERVAL_DELAY, DELAY_TIME_UNIT);
       started = true;
    }
}
