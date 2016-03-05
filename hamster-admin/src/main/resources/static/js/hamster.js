var hamsterApp = angular.module('Hamster', [ 'ui.router', 'oc.lazyLoad',
		'xeditable', 'ui.bootstrap', 'ngMessages' ]);

hamsterApp.config([ '$ocLazyLoadProvider', function($ocLazyLoadProvider) {
	$ocLazyLoadProvider.config({});
} ]);

hamsterApp.config([ '$stateProvider', '$urlRouterProvider',
		function($stateProvider, $urlRouterProvider) {
			$urlRouterProvider.otherwise('/');
			// 视图路由
			$stateProvider.state("servicelist", {
				url : '/',
				views : {
					'body-content' : {
						templateUrl : 'tpl/servicelist.html'
					}
				}
			}).state("serviceconfig", {
				url : '/config?app&service&key&isSearch',
				views : {
					'body-content' : {
						templateUrl : 'tpl/serviceconfig.html'
					}
				}
			});
		} ]);
hamsterApp.run(function(editableOptions) {
	editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2',
	// 'default'
});

hamsterApp.factory('searchState', function($rootScope, $http) {
	'use strict';

	var state = {

		serviceName : '',

		serviceVersion : '',

		appName : '',

		serviceAddr : '',

		servicePort : -1,

		serviceProtocol : '',

		currentPage : -1,

		isInit : false,

		data : null

	};

	var broadcast = function(state) {

		$rootScope.$broadcast('searchstate.update', state);
	};

	var update = function(newState) {
		if (newState == null) {
			broadcast(state);
			return;
		}
		if (newState.serviceName && newState.serviceName.trim()) {
			state.serviceName = newState.serviceName;
		} else {
			state.serviceName = '';
		}
		if (newState.serviceVersion && newState.serviceVersion.trim()) {
			state.serviceVersion = newState.serviceVersion;
		} else {
			state.serviceVersion = '';
		}
		if (newState.appName && newState.appName.trim()) {
			state.appName = newState.appName;
		} else {
			state.appName = '';
		}
		if (newState.serviceAddr && newState.serviceAddr.trim()) {
			state.serviceAddr = newState.serviceAddr;
		} else {
			state.serviceAddr = '';
		}
		if (newState.serviceProtocol && newState.serviceProtocol.trim()) {
			state.serviceProtocol = newState.serviceProtocol;
		} else {
			state.serviceProtocol = '';
		}
		if (newState.servicePort) {
			state.servicePort = parseInt(newState.servicePort);
		} else {
			state.servicePort = -1;
		}
		var promise = $http({
			method : 'GET',
			url : '/searchService',
			params : {
				'app' : state.appName,
				'serviceName' : state.serviceName,
				'version' : state.serviceVersion,
				'host' : state.serviceAddr,
				'port' : state.servicePort,
				'protocol' : state.serviceProtocol

			}

		});

		promise.then(function(rsp) {
			state.isInit = true;
			state.data = rsp.data;
			state.currentPage = 1;
			broadcast(state);

		}, function(rsp) {
			console.log(rsp);
		});

	};
	var setCurrentPage = function(curPage) {

		state.currentPage = curPage;
	}

	return {
		update : update,
		state : state,
		setCurrentPage : setCurrentPage
	};
});
hamsterApp.factory('searchConfigState', function($rootScope, $http) {
	'use strict';

	var state = {

		serviceName : '',

		appName : '',

		configKey : '',

		isInit : false,

		data : null

	};

	var broadcast = function(state) {

		$rootScope.$broadcast('configstate.update', state);
	};

	var update = function(newState) {

		if (newState == null) {
			broadcast(state);
			return;
		}
		if (newState.serviceName && newState.serviceName.trim()) {
			state.serviceName = newState.serviceName;
		} else {
			state.serviceName = '';
		}

		if (newState.appName && newState.appName.trim()) {
			state.appName = newState.appName;
		} else {
			state.appName = '';
		}

		if (newState.configKey && newState.configKey.trim()) {

			state.configKey = newState.configKey;
		} else {
			state.configKey = '';
		}

		var promise = $http({
			method : 'GET',
			url : '/serviceConfig',
			params : {
				'app' : state.appName,
				'serviceName' : state.serviceName,
				'configKey' : state.configKey
			}

		});

		promise.then(function(rsp) {
			state.isInit = true;

			var mapData = rsp.data;

			var arrayData = [];

			for ( var key in mapData) {
				var d = {
					'key' : key,
					'value' : mapData[key]
				};

				arrayData.push(d);
			}
			state.data = arrayData;

			broadcast(state);

		}, function(rsp) {
			console.log(rsp);
		});
	};

	return {
		update : update,
		state : state,
	};
});

hamsterApp.controller('HamsterBannerController', function($scope, $http) {

	
	var promise=$http({

		method : 'GET',
		url : '/currentUserName',
			
	});
	promise.then(function(rsp){
		$scope.username=rsp.data;
	});

});
hamsterApp.controller('ServiceSearchController', function($scope, $state,
		$http, searchState) {
	$scope.searchParam = {};
	$scope.searchService = function() {

		var newState = {
			serviceName : $scope.searchParam.serviceName,

			serviceVersion : $scope.searchParam.serviceVersion,

			appName : $scope.searchParam.appName,

			serviceAddr : $scope.searchParam.serviceAddr,

			servicePort : $scope.searchParam.servicePort,

			serviceProtocol : $scope.searchParam.serviceProtocol

		};
		searchState.update(newState);
	};

	$scope.showConfig = function(service) {

		$state.go('serviceconfig', {
			service : service.serviceName,
			app : service.app,
			key : service.configKey,
			isSearch : '1'
		});
	};
	$scope.$on('searchstate.update', function(evt, evtData) {

		var data = evtData.data;
		var pageNum = 5;
		$scope.totalItems = data.length;
		$scope.currentPage = evtData.currentPage;
		$scope.itemsPerPage = pageNum;
		$scope.pageChanged = function() {

			var len = pageNum * ($scope.currentPage);
			if ((data.length - len) >= pageNum) {
				$scope.servicelist = data.slice(len - pageNum, len);
			} else {
				$scope.servicelist = data.slice(len - pageNum);
			}
			searchState.setCurrentPage($scope.currentPage);
		};
		$scope.pageChanged();
	});

	if (!searchState.state.isInit) {
		searchState.update(searchState.state);
	} else {

		$scope.searchParam.serviceName = searchState.state.serviceName;

		$scope.searchParam.serviceVersion = searchState.state.serviceVersion;

		$scope.searchParam.appName = searchState.state.appName;

		$scope.searchParam.serviceAddr = searchState.state.serviceAddr;

		$scope.searchParam.servicePort = searchState.state.servicePort;

		$scope.searchParam.serviceProtocol = searchState.state.serviceProtocol;
		searchState.update(null);
	}

});

hamsterApp
		.controller(
				'ServiceConfigController',
				function($scope, $stateParams, searchConfigState, $http) {

					$scope.searchParam = {};
					$scope.searchConfigService = function() {

						var newState = {
							serviceName : $scope.searchParam.serviceName,
							appName : $scope.searchParam.appName,

							configKey : $scope.searchParam.serviceConfigKey

						};
						searchConfigState.update(newState);
					};

					$scope.$on('configstate.update', function(evt, evtData) {

						var data = evtData.data;
						$scope.serviceConfigList = data;
						$scope.configItemDeleted = new Array(data.length);

					});

					$scope.checkKey = function(data) {
						if (!data) {
							return "key can not be null";
						}
					};

					$scope.saveConfig = function(data) {
					};

					// remove config
					$scope.removeConfig = function(index) {
						$scope.serviceConfigList.splice(index, 1);

					};

					// add config
					$scope.addConfig = function() {
						$scope.inserted = {
							key : '',
							value : ''
						};
						$scope.serviceConfigList.push($scope.inserted);

					};

					$scope.saveAllConfig = function() {

						var saveData = {};
						if ($scope.serviceConfigList) {
							for ( var i = 0, config; config = $scope.serviceConfigList[i++];) {

								if (config['key']) {
									saveData[config['key']] = config['value'];
								}
							}
						}
						$http({

							method : 'POST',
							url : '/updateServiceConfig',
							params : {
								'app' : searchConfigState.state.appName,
								'serviceName' : searchConfigState.state.serviceName,
								'configKey' : searchConfigState.state.configKey
							},
							data : saveData
						});

					};

					if ($stateParams.isSearch) {
						var newState = {
							serviceName : $stateParams.service,
							appName : $stateParams.app,

							configKey : $stateParams.key

						};

						searchConfigState.update(newState);
						$scope.searchParam.serviceName = searchConfigState.state.serviceName;

						$scope.searchParam.appName = searchConfigState.state.appName;

						$scope.searchParam.serviceConfigKey = searchConfigState.state.configKey;
					} else {
						if (!searchConfigState.state.isInit) {
							searchConfigState.update(searchConfigState.state);
						} else {

							$scope.searchParam.serviceName = searchConfigState.state.serviceName;

							$scope.searchParam.appName = searchConfigState.state.appName;

							$scope.searchParam.serviceConfigKey = searchConfigState.state.configKey;

							searchConfigState.update(null);
						}
					}
					;

				});

$(function() {

	$('#side-menu').metisMenu();

});

// Loads the correct sidebar on window load,
// collapses the sidebar on window resize.
// Sets the min-height of #page-wrapper to window size
$(function() {
	$(window)
			.bind(
					"load resize",
					function() {
						topOffset = 50;
						width = (this.window.innerWidth > 0) ? this.window.innerWidth
								: this.screen.width;
						if (width < 768) {
							$('div.navbar-collapse').addClass('collapse');
							topOffset = 100; // 2-row-menu
						} else {
							$('div.navbar-collapse').removeClass('collapse');
						}

						height = ((this.window.innerHeight > 0) ? this.window.innerHeight
								: this.screen.height) - 1;
						height = height - topOffset;
						if (height < 1)
							height = 1;
						if (height > topOffset) {
							$("#page-wrapper").css("min-height",
									(height) + "px");
						}
					});

	var url = window.location;
	var element = $('ul.nav a').filter(function() {
		return this.href == url || url.href.indexOf(this.href) == 0;
	}).addClass('active').parent().parent().addClass('in').parent();
	if (element.is('li')) {
		element.addClass('active');
	}
});
