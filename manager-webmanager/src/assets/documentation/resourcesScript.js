var clusterOverviewHeader;
var clusterOverviewHeaderSticky;

var clusterDetailsHeader;
var clusterDetailsHeaderSticky;

var workerDetailsHeader;
var workerDetailsHeaderSticky;

window.onload = function() {

	clusterOverviewHeader = document.getElementById('cluster-overview');
	clusterOverviewHeaderSticky = clusterOverviewHeader.offsetTop;

	clusterDetailsHeader = document.getElementById('cluster-details');
	clusterDetailsHeaderSticky = clusterDetailsHeader.offsetTop;

	workerDetailsHeader = document.getElementById('worker-details');
	workerDetailsHeaderSticky = workerDetailsHeader.offsetTop;
}

window.onscroll = function() {
	if (window.pageYOffset >= workerDetailsHeaderSticky) {
		clusterOverviewHeader.classList.remove('sticky');
		clusterDetailsHeader.classList.remove('sticky');
		workerDetailsHeader.classList.add('sticky');
	} else if (window.pageYOffset >= clusterDetailsHeaderSticky) {
		clusterOverviewHeader.classList.remove('sticky');
		clusterDetailsHeader.classList.add('sticky');
		workerDetailsHeader.classList.remove('sticky');
	} else if (window.pageYOffset >= clusterOverviewHeaderSticky) {
		clusterOverviewHeader.classList.add('sticky');
		clusterDetailsHeader.classList.remove('sticky');
		workerDetailsHeader.classList.remove('sticky');
	} else {
		clusterOverviewHeader.classList.remove('sticky');
		clusterDetailsHeader.classList.remove('sticky');
		workerDetailsHeader.classList.remove('sticky');
	}
}

