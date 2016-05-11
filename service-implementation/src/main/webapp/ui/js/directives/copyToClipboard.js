angular.module(
	'myApp.copyToClipboard', []
).directive('copyToClipboard', function($window) {
	var body = angular.element($window.document.body);
	var textarea = angular.element('<textarea/>');
	textarea.css({
		position: 'fixed',
		opacity: '0'
	});

	function copy(toCopy) {
		textarea.val(toCopy);
		body.append(textarea);
		textarea[0].select();
		document.execCommand('copy');
		textarea.remove();
	}
	
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			element.bind('click', function(e) {
				copy(attrs.copyToClipboard);
			});
		}
	};
});