/*
 * Copyright 2016 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

package com.liaison.mailbox.enums;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UppercaseEnumAdapter implements JsonDeserializer<Enum> {

    @Override
    public Enum deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            if (type instanceof Class && ((Class<?>) type).isEnum()) {
                return Enum.valueOf((Class<Enum>) type, json.getAsString().toUpperCase());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
