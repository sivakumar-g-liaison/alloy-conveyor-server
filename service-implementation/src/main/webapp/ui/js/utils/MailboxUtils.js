function checkNameDuplicate(array, name) {

    var elem;

    for (elem=0; elem<array.length-1; elem++) {

        if (array[elem].name === name) {
            return true;
        }
    }
    return false;
}
