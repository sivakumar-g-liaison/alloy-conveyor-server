/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.g2mailboxservice.pluggable;

/**
 * The <code>Task</code> interface should be implemented by any
 * class whose instances are executed via Mailbox Executor. The
 * class must define a method of no arguments called <code>run</code>.
 * 
 * @author Sivakumar Gopalakrishnan
 * @version 1.0
 */
public interface Task {

	/**
	 * When an object implementing interface <code>Task</code> is used
     * to create a Mailbox Task.
     * <p>
     * The general contract of the method <code>run</code> is the common protocol for all Mail box task.
	 */
	public void run();
	public Object getResponse();
}
