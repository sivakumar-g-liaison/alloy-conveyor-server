/*
 * Copyright 2014 Liaison Technologies, Inc.
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */
/**
 * This software is the confidential and proprietary information of
 * Liaison Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Liaison Technologies.
 */

/**
* function for grouping files.
*/
function groupFiles(files) {

	//Importing java classes.
	new JavaImporter(java.lang.Object, java.nio.file.Path, java.lang.reflect.Array, java.lang.Integer);

    //Getting sub string from the filename
    var fileNamesArray = getFileNamesArray(files);

    //Removing duplicates from the give fileNamesArray
    var uniqueFileNamesArray = removeDuplicates(fileNamesArray);

    //Final grouped array of files 
    var result = java.lang.reflect.Array.newInstance(java.lang.Object, files.size());

    //loop to grouping
    for (i = 0; i < uniqueFileNamesArray.length; i++) {

    	result[i] = java.lang.reflect.Array.newInstance(java.nio.file.Path, files.size());
        for (j = 0, k = 0; j < files.size(); j++, k++) {

            if (uniqueFileNamesArray[i] == files.get(j).getFileName().toString().substring(0, 5)) {

                result[i][k]= files.get(j);
                files.remove(j);
                j--;
            }

        }
    }

    return result;
}

/**
* function for getting file name array
*/
function getFileNamesArray(files) {

	var size = files.size();

	var fileNamesArray = new Array();
    for (i = 0; i < size; i++) {
    	fileNamesArray[i] = files.get(i).getFileName().toString().substring(0, 5);
    }
    return fileNamesArray;
}

/**
 * Function to remove duplicates in the given file name array
 */
function removeDuplicates(array) {

    array.sort();
    var i = array.length - 1;
    
    while (i > 0) {
    	
        if (array[i] == array[i - 1]) {
            array.splice(i, 1);
        }
        i--;
    }
    return array;
}