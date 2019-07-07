/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.core.processor;

import com.liaison.mailbox.MailBoxConstants;
import com.liaison.mailbox.dtdm.model.Processor;
import com.liaison.mailbox.enums.ProcessorType;
import com.liaison.mailbox.enums.Protocol;
import com.liaison.mailbox.service.util.MailBoxUtil;

/**
 * Factory class to instantiate MailBox Processors.
 * 
 * @author veerasamyn
 */
public class MailBoxProcessorFactory {

	/**
	 *
	 */
	private MailBoxProcessorFactory() {
	}

	/**
	 * Factory to method to create instances for mailbox processor.
	 * 
	 * @param processor
	 *            The Processor Entity
	 * @return The MailBox Processor instance.
	 */
	public static MailBoxProcessorI getInstance(Processor processor) {

		MailBoxProcessorI mailBoxProcessor = null;

		Protocol foundProtocolType = Protocol.findByCode(processor.getProcsrProtocol());

		if (ProcessorType.REMOTEDOWNLOADER.equals(processor.getProcessorType())) {

			switch (foundProtocolType) {

			case FTPS:
			case FTP:
				mailBoxProcessor = new FTPSRemoteDownloader(processor);
				break;

			case SFTP:
				mailBoxProcessor = new SFTPRemoteDownloader(processor);
				break;

			case HTTP:
			case HTTPS:
				mailBoxProcessor = new HTTPRemoteDownloader(processor);
				break;
			default:
				break;

			}
		} else if (ProcessorType.REMOTEUPLOADER.equals(processor
				.getProcessorType())) {

			switch (foundProtocolType) {

			case FTPS:
			case FTP:
				mailBoxProcessor = new FTPSRemoteUploader(processor);
				break;

			case SFTP:
				mailBoxProcessor = new SFTPRemoteUploader(processor);
				break;

			case HTTP:
			case HTTPS:
				mailBoxProcessor = new HTTPRemoteUploader(processor);
				break;
			default:
				break;

			}
		} else if (ProcessorType.SWEEPER.equals(processor.getProcessorType())) {
		    boolean isInboundFileEnabled = MailBoxUtil.getEnvironmentProperties().getBoolean(MailBoxConstants.ENABLE_INBOUND_FILE, false);
		    if (isInboundFileEnabled) {
		        mailBoxProcessor = new EnhancedDirectorySweeper(processor);
		    } else {
		        mailBoxProcessor = new DirectorySweeper(processor);
		    }
		} else if (ProcessorType.CONDITIONALSWEEPER.equals(processor.getProcessorType())) {
		    boolean isInboundFileEnabled = MailBoxUtil.getEnvironmentProperties().getBoolean(MailBoxConstants.ENABLE_INBOUND_FILE, false);
		    if (isInboundFileEnabled) {
		        mailBoxProcessor = new EnhancedConditionalSweeper(processor);
		    } else {
		        mailBoxProcessor = new ConditionalSweeper(processor);
		    }
		} else if (ProcessorType.FILEWRITER.equals(processor.getProcessorType())) {
			mailBoxProcessor = new FileWriter(processor);
		}
		return mailBoxProcessor;
	}

    /**
     * Factory method to create instances for mailbox processor.
     *
     * @param processor
     *            The Processor Entity
     * @return The MailBox Processor instance.
     */
    public static RemoteUploaderI getUploaderInstance(Processor processor) {

        Protocol foundProtocolType = Protocol.findByCode(processor.getProcsrProtocol());
        if (ProcessorType.REMOTEUPLOADER.equals(processor.getProcessorType())) {

            switch (foundProtocolType) {

                case FTPS:
                case FTP:
                    return new FTPSRemoteUploader(processor);
                case SFTP:
                    return new SFTPRemoteUploader(processor);
                case HTTP:
                case HTTPS:
                    return new HTTPRemoteUploader(processor);
                default:
                    return null;

            }
        }
        return null;
    }

}