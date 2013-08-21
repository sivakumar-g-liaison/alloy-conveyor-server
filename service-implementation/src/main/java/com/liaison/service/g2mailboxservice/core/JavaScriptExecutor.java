/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
package com.liaison.service.g2mailboxservice.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.liaison.framework.util.ServiceUtils;

/**
 * JavaScriptExecutor
 *
 * <P>Class is used to execute the java script using script engine.
 *
 * @author veerasamyn
 */

public class JavaScriptExecutor {

    /**
     * Method is used to group files based on the logic available in the script.
     *
     * @param files List of files
     * @return List<List<Path>> which contains list of file groups
     * @throws NoSuchMethodException
     * @throws ScriptException
     */
    public List<List<Path>> groupFiles(List<Path> files)
            throws NoSuchMethodException, ScriptException {

        //create a script engine manager
        ScriptEngineManager manager = new ScriptEngineManager();

        //create a JavaScript engine
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        //Loading java script from classpath
        String groupingConfiguration = ServiceUtils.readFileFromClassPath("grouping.js");
        engine.eval(groupingConfiguration);

        // javax.script.Invocable is an optional interface.
        // Check whether your script engine implements or not!
        // Note that the JavaScript engine implements Invocable interface.
        Invocable inv = (Invocable) engine;

        // invoke the method named "groupFiles" on the java object "files"
        Object obj = inv.invokeFunction("groupFiles", files);
        Object[] objs = removeNullFromObject((Object[]) obj);

        List<List<Path>> fileGroups = new ArrayList<>();
        for (Object o : objs) {
            Path[] path = removeNullFromPath((Path[]) o);
            fileGroups.add(Arrays.asList(path));
        }

        return fileGroups;
    }

    /**
     * Method is used to remove null objects from an object array.
     *
     * @param array which contains not null values and null values.
     * @return Object[] which contains the not null values.
     */
    private static Object[] removeNullFromObject(Object[] array) {

        List<Object> list = new ArrayList<Object>();

        for (Object obj : array) {
            if (obj != null) {
                list.add(obj);
            }
        }
        return list.toArray(new Object[list.size()]);
    }

    /**
     * Method is used to remove null object from Path array.
     *
     * @param array which contains not null values and null values.
     * @return Path[] which contains not null values.
     */
    private static Path[] removeNullFromPath(Path[] array) {

        List<Path> list = new ArrayList<Path>();
        for (Path path : array) {
            if (path != null) {
                list.add(path);
            }
        }

        return list.toArray(new Path[list.size()]);
    }

}
