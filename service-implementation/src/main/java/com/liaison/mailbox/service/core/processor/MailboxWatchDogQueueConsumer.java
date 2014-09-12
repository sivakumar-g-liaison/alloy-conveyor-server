package com.liaison.mailbox.service.core.processor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liaison.mailbox.service.util.MailBoxUtil;

public class MailboxWatchDogQueueConsumer {

	private static int threadCount;		
	private static final Logger logger = LogManager.getLogger(MailboxWatchDogQueueConsumer.class);
	private static MailboxWatchDogQueueConsumer qConsumerInstance = null;
	
	private MailboxWatchDogQueueConsumer() {
		// defeat instantiation.
	}
	
	private LinkedBlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<Runnable>();
	private MailBoxThreadPoolExecutor execSrvc = new MailBoxThreadPoolExecutor(threadCount, threadCount,60L, TimeUnit.MILLISECONDS,linkedBlockingQueue,true);
	
	public void invokeWatchDog(String requestJSON) throws InterruptedException {		
		execSrvc.execute(new WatchDogInvoker(requestJSON));
		
	}
 
	public void printExecutorDiagonostics(){
		logger.info("************************************************************************************************");
		logger.info("Number of tasks currently queued up and waiting for invocation {}",linkedBlockingQueue.size() );
		logger.info("Number of threads in Executor that are active {}",execSrvc.getActiveCount() );
		logger.info("Number of tasks  queued in total - from the time beginning {} ",execSrvc.getTaskCount() );
		logger.info("Number of tasks  completed in total - from the time beginning {} ",execSrvc.getCompletedTaskCount() );
		logger.info("************************************************************************************************");
	}
	
	/**
	 * Get MailboxProcessorQueueConsumer Instance.
	 * @return MailboxProcessorQueueConsumer
	 * @throws Exception 
	 * @throws NumberFormatException 
	 */
	public static MailboxWatchDogQueueConsumer getMailboxWatchDogQueueConsumerInstance() throws Exception {
		
		threadCount = Integer.parseInt(MailBoxUtil.getEnvironmentProperties().getString("mailbox.watchdog.queue.consumer.thread.count"));	
		if (qConsumerInstance == null) {
			qConsumerInstance = new MailboxWatchDogQueueConsumer();
		}
		
		return qConsumerInstance;
	}
}
