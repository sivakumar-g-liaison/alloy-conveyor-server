function showAlert(msg) {
    noty({
      text: msg,
      type: 'alert',
	  modal: true, 
	  animation: {
        open: {height: 'toggle'},
        close: {height: 'toggle'},
        easing: 'swing',
        speed: 100 // opening & closing animation speed
	  },
      dismissQueue: false,
      layout: 'center',
      theme: 'defaultTheme', // ['click', 'button', 'hover']
	  callback: {
			onShow: function() {},
			afterShow: function() {},
			onClose: function() {},
			afterClose: function() {}
		},
      buttons: [
        {addClass: 'btn btn-primary', text: 'Ok', onClick: function($noty) {
            $noty.close();
          }
        }
      ]
    });
 }
 
 function showConfirmationAlert(msg) {
 
	//var click;
    var n = noty({
      text: msg,
      type: 'alert',
      dismissQueue: true,
      layout: 'center',
	  modal: true,
      theme: 'defaultTheme', // ['click', 'button', 'hover']
	  callback: {
			onShow: function() {},
			afterShow: function() {},
			onClose: function() {},
			afterClose: function() { alert('dd') }
		},
      buttons: [
        {addClass: 'btn btn-primary', text: 'Ok', onClick: function($noty) {
		
            $noty.close();
          }
        },
        {addClass: 'btn btn-danger', text: 'Cancel', onClick: function($noty) {
            
			$noty.close();
          }
        }
      ]
    });
	
 }