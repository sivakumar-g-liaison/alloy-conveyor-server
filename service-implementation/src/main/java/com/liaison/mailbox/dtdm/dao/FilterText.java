/*
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.dtdm.dao;

import com.google.gson.annotations.SerializedName;
import com.liaison.mailbox.dtdm.model.FilterMatchMode;

import java.io.Serializable;
import java.util.List;

public class FilterText implements Serializable {

    @SerializedName("filterText")
    private List<FilterObject> filterTextListObject;
    private FilterMatchMode matchMode;

    public List<FilterObject> getFilterTextListObject() {
        return filterTextListObject;
    }

    public void setFilterTextListObject(List<FilterObject> filterTextListObject) {
        this.filterTextListObject = filterTextListObject;
    }

    public FilterMatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(FilterMatchMode matchMode) {
        this.matchMode = matchMode;
    }
}
