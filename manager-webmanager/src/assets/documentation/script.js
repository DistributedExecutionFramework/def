var titleLabel;
var programsButton;
var resourcesButton;
var libraryButton;
var cloudConnectionButton;
var contentFrame;

var overviewFile = 'overview.html';
var programsFile = 'programs.html';
var resourcesFile = 'resources.html';
var libraryFile = 'library.html';
var cloudConnectionFile = 'cloudConnection.html';

var selectedClass = 'selected';

function titleClicked() {
	removeAllSelections();

	loadContent(overviewFile);
}

function loadProgramsDocumentation() {
	removeAllSelections();
	programsButton.classList.add(selectedClass);

	loadContent(programsFile);
}

function loadResourcesDocumentation() {
	removeAllSelections();
	resourcesButton.classList.add(selectedClass);

	loadContent(resourcesFile);
}

function loadLibraryDocumentation() {
	removeAllSelections();
	libraryButton.classList.add(selectedClass);

	loadContent(libraryFile);
}

function loadCloudConnectionDocumentation() {
	removeAllSelections();
	cloudConnectionButton.classList.add(selectedClass);

	loadContent(cloudConnectionFile);
}

function removeAllSelections() {
	programsButton.classList.remove(selectedClass);
	resourcesButton.classList.remove(selectedClass);
	libraryButton.classList.remove(selectedClass);
	cloudConnectionButton.classList.remove(selectedClass);
}

function loadContent(src) {
	contentFrame.src = src;
}

window.onload = function() {
	if (document.readyState == 'complete') {
		programsButton = document.getElementById('programsButton');
		resourcesButton = document.getElementById('resourcesButton');
		libraryButton = document.getElementById('libraryButton');
		cloudConnectionButton = document.getElementById('cloudConnectionButton');
		contentFrame = document.getElementById('contentFrame');

		titleClicked();
	}
}