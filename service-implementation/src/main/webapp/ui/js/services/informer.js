myApp.factory('Informer', function(){

  var messages = [];  
  var Informer = {};

  Informer.inform = function(msg, type) {
    messages.push({
      msg: msg,
      type: type
    });
  };

  Informer.allInfos = function() {
    return messages;
  };

  Informer.remove = function(info) {
    messages.splice(messages.indexOf(info), 1);
  };  

  return Informer;
});