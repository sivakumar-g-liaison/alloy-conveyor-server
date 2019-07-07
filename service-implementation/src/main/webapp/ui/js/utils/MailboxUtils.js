function checkNameDuplicate(array, name) {

    var elem;

    for (elem=0; elem<array.length; elem++) {

        if (array[elem].name === name) {
            return true;
        }
    }
    return false;
}

function getIndex(objArray, name) {

    var pos = -1;
    for (var i=0; i<objArray.length; i++) {

        pos ++;
        if(objArray[i].name === name) {
            return pos;
        }
    }

    return -1
}

function getIndexOfId(objArray, id) {

    var pos = -1;
    for (var i=0; i<objArray.length; i++) {

        pos ++;
        if(objArray[i].id === id) {
            return pos;
        }
    }

    return -1
}

function getId(objArray, name) {

    for (var i=0; i<objArray.length; i++) {

        if(objArray[i].name === name) {
            return objArray[i].id;
        }
    }

    return '';
}

function getName(objArray, id) {

    for (var i=0; i<objArray.length; i++) {

        if(objArray[i].id === id) {
            return objArray[i].name;
        }
    }

    return '';
}
