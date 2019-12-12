var programOverviewHeader;
var programOverviewHeaderSticky;

var programDetailsHeader;
var programDetailsHeaderSticky;

var jobDetailsHeader;
var jobDetailsHeaderSticky;

var taskDetailsHeader;
var taskDetailsHeaderSticky;

window.onload = function() {

	programOverviewHeader = document.getElementById('program-overview');
	programOverviewHeaderSticky = programOverviewHeader.offsetTop;

	programDetailsHeader = document.getElementById('program-details');
	programDetailsHeaderSticky = programDetailsHeader.offsetTop;

	jobDetailsHeader = document.getElementById('job-details');
	jobDetailsHeaderSticky = jobDetailsHeader.offsetTop;

	taskDetailsHeader = document.getElementById('task-details');
	taskDetailsHeaderSticky = taskDetailsHeader.offsetTop;
}

window.onscroll = function() {
	if (window.pageYOffset >= taskDetailsHeaderSticky) {
		programOverviewHeader.classList.remove('sticky');
		programDetailsHeader.classList.remove('sticky');
		jobDetailsHeader.classList.remove('sticky');
		taskDetailsHeader.classList.add('sticky');
	} else if (window.pageYOffset >= jobDetailsHeaderSticky) {
		programOverviewHeader.classList.remove('sticky');
		programDetailsHeader.classList.remove('sticky');
		jobDetailsHeader.classList.add('sticky');
		taskDetailsHeader.classList.remove('sticky');
	} else if (window.pageYOffset >= programDetailsHeaderSticky) {
		programOverviewHeader.classList.remove('sticky');
		programDetailsHeader.classList.add('sticky');
		jobDetailsHeader.classList.remove('sticky');
		taskDetailsHeader.classList.remove('sticky');
	} else if (window.pageYOffset >= programOverviewHeaderSticky) {
		programOverviewHeader.classList.add('sticky');
		programDetailsHeader.classList.remove('sticky');
		jobDetailsHeader.classList.remove('sticky');
		taskDetailsHeader.classList.remove('sticky');
	} else {
		programOverviewHeader.classList.remove('sticky');
		programDetailsHeader.classList.remove('sticky');
		jobDetailsHeader.classList.remove('sticky');
		taskDetailsHeader.classList.remove('sticky');
	}
}

