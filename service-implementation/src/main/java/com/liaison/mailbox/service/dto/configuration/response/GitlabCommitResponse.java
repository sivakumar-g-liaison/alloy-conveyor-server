/**
 * Copyright 2018 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.service.dto.configuration.response;

import java.io.Serializable;

import org.gitlab.api.models.GitlabCommit;

/**
 * 
 * Class which contains response message and status for gitlab commit history.
 * 
 */
public class GitlabCommitResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private String message;
    private int status;
    private GitlabCommit gitlabCommit;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public GitlabCommit getGitlabCommit() {
        return gitlabCommit;
    }

    public void setGitlabCommit(GitlabCommit gitlabCommit) {
        this.gitlabCommit = gitlabCommit;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
