(function (angular) {
  'use strict';

/**
 *
 * @param $scope
 * @param $state
 */
angular.module('application')
  .controller('AuthenticationController', function ($importService, $timeout, $scope, $state, $http, $window, $ionicPopup, $API_ENDPOINT, ngFB, $ionicLoading, $translate) {


    $scope.teste = $translate("map.Field-required");
    console.log($scope.teste);
    // /TODO retirar

    /*-------------------------------------------------------------------
     * 		 				 	ATTRIBUTES
     *-------------------------------------------------------------------*/
    /**
     *
     */
    $scope.model = {
      form: null,
      user: {}
    };

    /*-------------------------------------------------------------------
    *                POST CONSTRUCT
    *-------------------------------------------------------------------*/



    $scope.model.user.email = 'test_prognus@mailinator.com'; //TODO lembrar de retirar
    $scope.model.user.password = 'admin';//TODO lembrar de retirar

    /*-------------------------------------------------------------------
     * 		 				 	  HANDLERS
     *-------------------------------------------------------------------*/
    /**
     *
     */
    $scope.loginHandler = function () {

      if ($scope.model.form.$invalid) {
        $ionicPopup.alert({
          title: 'Opss...',//TODO translate
          subTitle: 'Os campos estão inválidos.',//TODO translate
          template: 'Por favor verifique e tente novamente.' //TODO utilizar as mensagens providas pelos callbacks de erros
        });

      } else {

        var config = {
          headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}
        };

        $http.post($API_ENDPOINT + "/j_spring_security_check", $.param($scope.model.user), config)
          .success(function (data, status, headers, config) {
            $scope.loginSuccess();
          })
          .error(function (data, status, headers, config) {
            $scope.loginFailed();
          }
        );
      }
    };

    /**
     * Facebook login
     */
    $scope.fbLogin = function () {
      $ionicLoading.show({
        template: 'Logging in...' //TODO translate
      });
      //Realiza a autenticação
      ngFB.login({scope: 'email,public_profile,user_friends'})
      .then(function (response) {
        if (response.status === 'connected') {
          //Requisita da api do facebook as informações do usuário
          ngFB.api({
            path: '/me',
            params: {fields: 'id,name,email'}
          }).then(function (user) {
              $scope.login('facebook', user.email, response.authResponse.accessToken);
            },
            function (error) {
              $scope.loginFailed();
            });
        } else {
          $scope.loginFailed();
        }
      });
    };

    /**
     * This method is executed when the user press the "Sign in with Google" button  *
    */
    $scope.googleSignIn = function() {
      $ionicLoading.show({
        template: 'Logging in...' //TODO translate
      });

      window.plugins.googleplus.login(
        {
          'offline': true, // optional, used for Android only - if set to true the plugin will also return the OAuth access token ('oauthToken' param), that can be used to sign in to some third party services that don't accept a Cross-client identity token (ex. Firebase)
        },
        function (user) {
          $scope.login('google', user.email, user.oauthToken);
        },
        function (msg) {
          $scope.loginFailed();
        }
      );
    };


    $scope.login = function(server, user, token){
      //Valida o token provido pelo facebook no back-end, o back-end devolve a sessão do usuário
      $http.get($API_ENDPOINT + "/login/" + server + "?userName=" + user + "&token=" + token)
        .success(function (data, status, headers, config) {
          $scope.model.user.email = user;
          $scope.model.user.token = data;
          $scope.loginSuccess();
        })
        .error(function (data, status, headers, config) {
          $scope.loginFailed();
        });
    }

    /**
      *
    */
    $scope.loginSuccess = function () {
      $ionicLoading.hide();
      localStorage.setItem('token', $scope.model.user.token);
      localStorage.setItem('userEmail', $scope.model.user.email);
      $state.go('intro');
    };

    /**
      *
    */
    $scope.loginFailed = function () {
      $ionicLoading.hide();
      localStorage.removeItem('token', $scope.model.user.token);
      localStorage.removeItem('userEmail', $scope.model.user.email);
      $ionicPopup.alert({
        title: 'Opss...', //TODO translate
        subTitle: 'Não foi possível autenticar.', //TODO traduzir
        template: 'Verifique seu usuário e tente novamente' //TODO utilizar as mensagens providas pelos callbacks de erros
      });
    };
     /**
     * token handler
    */
    if(localStorage.getItem('token')){
      $scope.model.user.email = localStorage.getItem('userEmail');
      $scope.login('geocab', $scope.model.user.email, localStorage.getItem('token'));
    };

    if(localStorage.getItem('userEmail')){
      $scope.model.user.email = localStorage.getItem('userEmail');
    };

  });

}(window.angular));
