function handleHttpRequest(rd) {

	//Importing java classes.
	var SwingGui = new JavaImporter(java.lang.Object, java.io.ByteArrayOutputStream, java.nio.file.Path, java.lang.reflect.Array, java.lang.Integer, com.liaison.commons.util.client.http.HTTPRequest, com.liaison.commons.util.client.http.HTTPResponse, java.net.URL, java.net.MalformedURLException, java.io.OutputStream,
			com.liaison.mailbox.service.util.HTTPStringOutputStream, com.liaison.mailbox.service.core.processor.HttpRemoteDownloader,
			org.slf4j.Logger,org.slf4j.LoggerFactory);
	
    var clientContent = null;
    with (SwingGui) {
		var httpConfig = rd.getClientWithInjectedConfiguration();
		var byteStream = java.io.ByteArrayOutputStream();
		httpConfig.setOutputStream(byteStream);
		var reponse = httpConfig.execute();
		rd.writeResponseToMailBox(byteStream);
    }
		return reponse.getStatusCode();
}

function handleHttpRequestWithoutProperties(httpConfig, logger) {

	//Importing java classes.
	var SwingGui = new JavaImporter(java.lang.Object, java.nio.file.Path, java.lang.reflect.Array, java.lang.Integer, com.liaison.commons.util.client.http.HTTPRequest, com.liaison.commons.util.client.http.HTTPResponse, java.net.URL, java.net.MalformedURLException, java.io.OutputStream,
			com.liaison.mailbox.service.util.HTTPStringOutputStream, com.liaison.mailbox.service.core.processor.HttpRemoteDownloader,
			org.slf4j.Logger,org.slf4j.LoggerFactory);
	
    var clientContent = null;
    with (SwingGui) {
		
		var reponse = httpConfig.execute();
		
    }
		return reponse.getStatusCode();
}