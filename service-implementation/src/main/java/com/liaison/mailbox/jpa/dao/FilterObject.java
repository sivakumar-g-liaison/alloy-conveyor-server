/**
 * Copyright Liaison Technologies, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.mailbox.jpa.dao;

import java.io.Serializable;

/**
 *
 * @author OFS
 *
 */
public class FilterObject implements Serializable {

    private static final long serialVersionUID = 1L;

    public String field;
    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public boolean isMultiselect() {
        return multiselect;
    }
    public void setMultiselect(boolean multiselect) {
        this.multiselect = multiselect;
    }
    public String text;
    public boolean multiselect;

}
