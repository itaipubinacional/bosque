'use strict';

/**
 *
 * @param $scope
 * @param $log
 * @param $location
 */
function GalleryPopUpController($scope, $modalInstance, $log, $filter, layer, attribute, attributesByLayer) {

    /*-------------------------------------------------------------------
     * 		 				 	ATTRIBUTES
     *-------------------------------------------------------------------*/

    /**
     *
     * @type {null}
     */
    $scope.msg = null;

    $scope.filter = $filter;
    /**
     *
     */
    $scope.layer = layer;

    $scope.attribute = attribute;

    $scope.attributesByLayer = attributesByLayer;


    /*-------------------------------------------------------------------
     * 		 				 	  NAVIGATIONS
     *-------------------------------------------------------------------*/
    /**
     * Main method that makes the role of front-controller of the screen.
     * He is invoked whenever there is a change of URL (@see $stateChangeSuccess),
     * When this occurs, gets the State via the $state and calls the initial method of that State.
     *
     * If the State is not found, he directs to the listing,
     * Although the front controller of Angular won't let enter an invalid URL.
     */
    $scope.initialize = function()
    {

    };

    $scope.onSuccess = function(files) {

      console.log(files);

      $scope.attribute.files = [];
      $scope.filter('filter')($scope.attributesByLayer, {id: $scope.attribute.id}, true)[0].files = [];

      for (var i = 0, file; file = files[i]; i++) {

          var reader = new FileReader();

          reader.onloadend = (function (readFile) {
              return function (e) {
                  readFile.src = e.target.result;
                  $scope.attribute.files.push(readFile);
                  //$scope.filter('filter')($scope.attributesByLayer, {id: $scope.attribute.id}, true)[0].files.push(readFile);

                  $scope.$apply();

              }
          })(file);

          reader.readAsDataURL(file);
      }

    };


    /*-------------------------------------------------------------------
     * 		 				 	  BEHAVIORS
     *-------------------------------------------------------------------*/

    /**
     *
     */
    $scope.close = function(fechar)
    {
        // verifica se o usuário selecionou a opção de fechar ou selecionar na pop up
        if (fechar){
            $modalInstance.close();
        } else {
            $modalInstance.close($scope.attributesByLayer);
        }
    };
    
    
};