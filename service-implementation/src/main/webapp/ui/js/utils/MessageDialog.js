function showAlert(msg) {
    noty({
      text: msg,
      type: 'notification',
      dismissQueue: true,
      layout: 'center',
      theme: 'defaultTheme',
      buttons: [
        {addClass: 'btn btn-primary', text: 'Ok', onClick: function($noty) {
            $noty.close();
          }
        }
      ]
    });
 }