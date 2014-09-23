﻿'use strict';

/**
 *
 * @param $scope
 * @param $log
 * @param $location
 */
function LayerConfigController($scope, $injector, $log, $state, $timeout, $modal, $location, $importService, $translate) {
    /**
     * Injeta os métodos, atributos e seus estados herdados de AbstractCRUDController.
     * @see AbstractCRUDController
     */
    $injector.invoke(AbstractCRUDController, this, {$scope: $scope});

    $importService("layerGroupService");
    
    /*-------------------------------------------------------------------
     * 		 				 	EVENT HANDLERS
     *-------------------------------------------------------------------*/

    /**
     *  Handler que escuta toda vez que o usuário/programadamente faz o sorting na ng-grid.
     *  Quando o evento é disparado, configuramos o pager do spring-data
     *  e chamamos novamente a consulta, considerando também o estado do filtro (@see $scope.data.filter)
     */
    $scope.$on('ngGridEventSorted', function (event, sort) {

        if(event.targetScope.gridId != $scope.gridOptions.gridId)
        {
            return;
        }

        // compara os objetos para garantir que o evento seja executado somente uma vez q não entre em loop
        if (!angular.equals(sort, $scope.gridOptions.sortInfo)) {
            $scope.gridOptions.sortInfo = angular.copy(sort);

            //Order do spring-data
            var order = new Order();
            order.direction = sort.directions[0].toUpperCase();
            order.property = sort.fields[0];

            //Sort do spring-data
            $scope.currentPage.pageable.sort = new Sort();
            $scope.currentPage.pageable.sort.orders = [ order ];

            $scope.listLayersByFilters($scope.data.filter, $scope.currentPage.pageable);
        }
    });

    /*-------------------------------------------------------------------
     * 		 				 	ATTRIBUTES
     *-------------------------------------------------------------------*/
    //STATES
    /**
     * Variável estática que representa
     * o estado de listagem de registros.
     */
    $scope.LIST_STATE = "layer-config.list";
    /**
     * Variável estática que representa
     * o estado de detalhe de um registro.
     */
    $scope.DETAIL_STATE = "layer-config.detail";
    /**
     * Variável estática que representa
     * o estado para a criação de registros.
     */
    $scope.INSERT_STATE = "layer-config.create";
    /**
     * Variável estática que representa
     * o estado para a edição de registros.
     */
    $scope.UPDATE_STATE = "layer-config.update";
    /**
     * Variável que armazena o estado corrente da tela.
     * Esta variável deve SEMPRE estar de acordo com a URL
     * que está no browser.
     */
    $scope.currentState;

    /**
     *
     * @type {Array}
     */
    $scope.removeGroups = [];

    /**
     *
     * @type {Array}
     */
    $scope.addGroups = [];

    /**
     *
     * @type {Array}
     */
    $scope.selectedGroups = [];

    /**
     *
     * @type {Array}
     */
    $scope.originalGroups = [];

    //DATA GRID
    /**
     * Variável estática coms os botões de ações da grid
     * O botão de editar navega via URL (sref) por que a edição é feita em outra página,
     * já o botão de excluir chama um método direto via ng-click por que não tem um estado da tela específico.
     */
    var GRID_ACTION_BUTTONS = '<div class="cell-centered">' +
        '<a ui-sref="configuracao-camadas.editar({id:row.entity.id})" title="Editar" class="btn btn-mini"><i class="itaipu-icon-edit"></i></a>' +
        '<a ng-click="changeToRemove(row.entity)" title="Excluir" class="btn btn-mini"><i class="itaipu-icon-delete"></i></a>' +
        '</div>';
    
    var IMAGE_LEGEND = '<div align="center" class="ngCellText" ng-cell-text ng-class="col.colIndex()">' +
	'<img style="width: 20px; height: 20px; border: solid 1px #c9c9c9;" ng-src="{{row.entity.legend}}"/>' +
	'</div>';

    /**
     * Configurações gerais da ng-grid.
     * @see https://github.com/angular-ui/ng-grid/wiki/Configuration-Options
     */
    $scope.gridOptions = {
        data: 'currentPage.content',
        multiSelect: false,
        useExternalSorting: true,
        headerRowHeight: 45,
        rowHeight: 45,
        beforeSelectionChange: function (row, event) {
            //evita chamar a selecao, quando clicado em um action button.
            if ($(event.target).is("a") || $(event.target).is("i")) return false;
            $state.go($scope.DETAIL_STATE, {id: row.entity.id});
        },
        columnDefs: [
            {displayName: 'Simbologia', field:'legend', sortable:false, width: '120px', cellTemplate: IMAGE_LEGEND},
            {displayName: 'Título', field: 'title'},
            {displayName: 'Camada', field: 'name'},
            {displayName: 'Fonte de dados', field: 'dataSource.name'},
            {displayName: 'Grupo de camadas', field: 'layersGroup.name', width: '15%'},
            {displayName: 'Ações', sortable: false, cellTemplate: GRID_ACTION_BUTTONS, width: '100px'}
        ]
    };

    var GRID_ACTION_ACESSO_BUTTONS = '<div class="cell-centered">' +
        '<a ng-click="removeGrupoAcesso(row.entity)" ng-if="currentState != DETAIL_STATE" title="Excluir" class="btn btn-mini"><i class="itaipu-icon-delete"></i></a>' +
        '</div>';

    /**
     * Configurações gerais da ng-grid.
     * @see https://github.com/angular-ui/ng-grid/wiki/Configuration-Options
     */
    $scope.gridAcessoOptions = {
        data: 'selectedGroups',
        useExternalSorting: false,
        multiSelect: false,
        headerRowHeight: 45,
        rowHeight: 45,
        beforeSelectionChange: function (row, event) {
            //evita chamar a selecao, quando clicado em um action button.
            return false;
        },
        columnDefs: [
            {displayName: 'Nome', field: 'name'},
            {displayName: 'Descrição', field: 'description'},
            {displayName: '', sortable: false, cellTemplate: GRID_ACTION_ACESSO_BUTTONS, width: '100px'}
        ]
    };

    /**
     * Variável que armazena o estado da paginação
     * para renderizar o pager e também para fazer as requisições das
     * novas páginas, contendo o estado do Sort incluído.
     *
     * @type PageRequest
     */
    $scope.currentPage;

    /**
     *
     */
    $scope.escalas = {};

    //FORM
    /**
     * Variável para armazenar atributos do formulário que
     * não cabem em uma entidade. Ex.:
     * @filter - Filtro da consulta
     */
    $scope.data =
    {
        filter: null,
        tipoAcesso: 'PUBLICO',
        nomeGrupoCamadas: null,
        selects: { fontesDados: null }
    };
    /**
     * Armazena a entitidade corrente para edição ou detalhe.
     */
    $scope.currentEntity = {sistema: false, habilitada: false};

    /*-------------------------------------------------------------------
     * 		 				 	  NAVIGATIONS
     *-------------------------------------------------------------------*/
    /**
     * Método principal que faz o papel de front-controller da tela.
     * Ele é invocado toda vez que ocorre uma mudança de URL (@see $stateChangeSuccess),
     * quando isso ocorre, obtém o estado através do $state e chama o método inicial daquele estado.
     * Ex.: /list -> changeToList()
     *      /criar -> changeToInsert()
     *
     * Caso o estado não for encontrado, ele direciona para a listagem,
     * apesar que o front-controller do angular não deixa digitar uma URL inválida.
     */
    $scope.initialize = function (toState, toParams, fromState, fromParams) {
        var state = $state.current.name;
        /**
         * É necessario remover o atributo sortInfo pois o retorno de uma edição estava duplicando o valor do mesmo com o atributo Sort
         * impossibilitando as ordenações nas colunas da grid.
         */
        if ($scope.gridOptions.sortInfo) {
            delete $scope.gridOptions.sortInfo;
        }

        $log.info("Starting the front controller.");

        switch (state) {
            case $scope.LIST_STATE:
            {
                $scope.changeToList();
            }
                break;
            case $scope.DETAIL_STATE:
            {
                $scope.changeToDetail($state.params.id);
            }
                break;
            case $scope.INSERT_STATE:
            {
                $scope.changeToInsert();
            }
                break;
            case $scope.UPDATE_STATE:
            {
                $scope.changeToUpdate($state.params.id);
            }
                break;
            default :
            {
                $state.go($scope.LIST_STATE);
            }
        }
    };

    /**
     * Realiza os procedimentos iniciais (prepara o estado)
     * para a tela de consulta e após isso, muda o estado para list.
     * @see LIST_STATE
     * @see $stateChangeSuccess
     *
     * Para mudar para este estado, deve-se primeiro carregar os dados da consulta.
     */
    $scope.changeToList = function () {
        $log.info("changeToList");
        
        $scope.currentState = $scope.LIST_STATE;

        /*var pageRequest = new PageRequest();
        pageRequest.size = 10;
        $scope.pageRequest = pageRequest;

        $scope.listLayersByFilters(null, pageRequest);*/
    };

    /**
     * Realiza os procedimentos iniciais (prepara o estado)
     * para a tela de inserção e após isso, muda o estado para insert.
     * @see INSERT_STATE
     * @see $stateChangeSuccess
     *
     * Para mudar para este estado, deve-se primeiro instanciar um novo currentEntity,
     * para limpar os campos e configurar valores defaults.
     */
    $scope.changeToInsert = function () {
        $log.info("changeToInsert");

        $scope.escalas = {};

        $scope.originalGroups = [];
        $scope.selectedGroups = [];
        $scope.addGroups = [];
        $scope.removeGroups = [];

        $scope.currentEntity = new Object();

        $scope.currentState = $scope.INSERT_STATE;

    };

    /**
     * Realiza os procedimentos iniciais (prepara o estado)
     * para a tela de edição e após isso, muda o estado para update.
     * @see UPDATE_STATE
     * @see $stateChangeSuccess
     *
     * Para mudar para este estado, deve-se primeiro obter via id
     * o registro pelo serviço de consulta e só então mudar o estado da tela.
     */
    $scope.changeToUpdate = function (id) {

        $log.info("changeToUpdate", id);

        grupoCamadasService.findCamadaById(id, {
            callback: function (result) {
                $scope.currentEntity = result;
                $scope.escalas.values = {};
                $scope.escalas.values[0] = '1:'+$scope.currentEntity.escalaMinimaMapa.substring(2);
                $scope.escalas.values[1] = '1:'+$scope.currentEntity.escalaMaximaMapa.substring(2);
                $scope.currentState = $scope.UPDATE_STATE;
                $state.go($scope.UPDATE_STATE);
                $scope.$apply();

                $scope.loadGruposAcesso(result.id);
            },
            errorHandler: function (message, exception) {
                $scope.msg = {type: "danger", text: message, dismiss: true};
                $scope.$apply();
            }
        });
    };

    /**
     * Realiza os procedimentos iniciais (prepara o estado)
     * para a tela de detalhe e após isso, muda o estado para detail.
     * @see DETAIL_STATE
     * @see $stateChangeSuccess
     *
     * Para mudar para este estado, deve-se primeiro obter via id
     * o registro atualizado pelo serviço de consulta e só então mudar o estado da tela.
     * Caso o indentificador esteja inválido, retorna para o estado de listagem.
     */
    $scope.changeToDetail = function (id) {
        $log.info("changeToDetail", id);

        if (id == null || id == "" || id == 0) {
            $scope.msg = {type: "error", text: $scope.INVALID_ID_MESSAGE, dismiss: true};
            $scope.currentState = $scope.LIST_STATE;
            $state.go($scope.LIST_STATE);
            return;
        }

        grupoCamadasService.findCamadaById(id, {
            callback: function (result) {
                $scope.currentEntity = result;
                $scope.escalas.values = {};
                $scope.escalas.values[0] = '1:'+$scope.currentEntity.escalaMinimaMapa.substring(2);
                $scope.escalas.values[1] = '1:'+$scope.currentEntity.escalaMaximaMapa.substring(2);
                $scope.currentState = $scope.DETAIL_STATE;
                $state.go($scope.DETAIL_STATE, {id: id});
                $scope.$apply();

                $scope.loadGruposAcesso(result.id);
            },
            errorHandler: function (message, exception) {
                $scope.msg = {type: "danger", text: message, dismiss: true};
                $scope.$apply();
            }
        });
    };

    /**
     * Realiza os procedimentos iniciais (prepara o estado)
     * para a tela de exclusão.
     *
     * Antes de excluir, o usuário notificado para confirmação
     * e só então o registro é excluido.
     * Após excluído, atualizamos a grid com estado de filtro, paginação e sorting.
     */
    $scope.changeToRemove = function (camada) {
        $log.info("changeToRemove", camada);

        var dialog = $modal.open({
            templateUrl: "assets/libs/eits-directives/dialog/dialog-template.html",
            controller: DialogController,
            windowClass: 'dialog-delete',
            resolve: {
                title: function () {
                    return "Exclusão de camada";
                },
                message: function () {
                    return 'Tem certeza que deseja excluir a camada "' + camada.nome + '"? <br/>Esta operação não poderá mais ser desfeita.';
                },
                buttons: function () {
                    return [
                        {label: 'Excluir', css: 'btn btn-danger'},
                        {label: 'Cancelar', css: 'btn btn-default', dismiss: true}
                    ];
                }
            }
        });


        dialog.result.then(function (result) {

            grupoCamadasService.removeCamada(camada.id, {
                callback: function (result) {
                    //caso o currentPage esteja null, configura o pager default
                    if ($scope.currentPage == null) {
                        $scope.changeToList();
                        //caso não, usa o mesmo estado para carregar a listagem
                    } else {
                        $scope.listCamadasByFilters($scope.data.filter, $scope.currentPage.pageable);
                    }

                    $scope.msg = {type: "success", text: 'O registro "' + camada.nome + '" foi excluído com sucesso.', dismiss: true};
                },
                errorHandler: function (message, exception) {
                    $scope.msg = {type: "danger", text: message, dismiss: true};
                    $scope.$apply();
                }
            });
        });
    };

    /**
     * Configura o pageRequest conforme o componente visual pager
     * e chama o serviçoe de listagem, considerando o filtro corrente na tela.
     *
     * @see currentPage
     * @see data.filter
     */
    $scope.changeToPage = function (filter, pageNumber) {
        $scope.currentPage.pageable.page = pageNumber - 1;
        $scope.listCamadasByFilters(filter, $scope.currentPage.pageable);
    };

    /*-------------------------------------------------------------------
     * 		 				 	  BEHAVIORS
     *-------------------------------------------------------------------*/

    /**
     * Realiza a consulta de registros, consirando filtro, paginação e sorting.
     * Quando ok, muda o estado da tela para list.
     *
     * @see data.filter
     * @see currentPage
     */
    $scope.listLayersByFilters = function (filter, pageRequest) {

        layerGroupService.listLayersByFilters(filter, pageRequest, {
            callback: function (result) {
                $scope.currentPage = result;
                $scope.currentPage.pageable.pageNumber++;//Para fazer o bind com o pagination
                $scope.currentState = $scope.LIST_STATE;
                $state.go($scope.LIST_STATE);
                $scope.$apply();
            },
            errorHandler: function (message, exception) {
                $scope.msg = {type: "danger", text: message, dismiss: true};
                $scope.$apply();
            }
        });
    };

    /**
     * Realiza a inserção de um novo registro
     * e no suscesso, modifica o estado da tela para o detail.
     */
    $scope.insertLayer = function (layer) {

    	layer.escalaMinimaMapa = 'UM'+$scope.escalas.values[0].substring(2);
    	layer.escalaMaximaMapa = 'UM'+$scope.escalas.values[1].substring(2);
        
        if (!$scope.form().$valid) {
            $scope.msg = {type: "danger", text: $scope.INVALID_FORM_MESSAGE, dismiss: true};
            return;
        }

        layerGroupService.insertLayer(layer, {
            callback: function (result) {
				$scope.currentState = $scope.LIST_STATE;
                $scope.currentEntity = result;
				$state.go($scope.LIST_STATE);
                $scope.msg = {type: "success", text: "Camada inserida com sucesso!", dismiss: true};
                $scope.$apply();
                $scope.saveGroups();
            },
            errorHandler: function (message, exception) {
                $scope.msg = {type: "danger", text: message, dismiss: true};
                $scope.$apply();
            }
        });
    };

    /**
     * Realiza a atualiza de um registro
     * e no suscesso, modifica o estado da tela para o detail.
     */
    $scope.updateCamada = function (camada) {

    	camada.escalaMinimaMapa = 'UM'+$scope.escalas.values[0].substring(2);
        camada.escalaMaximaMapa = 'UM'+$scope.escalas.values[1].substring(2);

        if (!$scope.form().$valid) {
            $scope.msg = {type: "danger", text: $scope.INVALID_FORM_MESSAGE, dismiss: true};
            return;
        }

        grupoCamadasService.updateCamada(camada, {
            callback: function () {
				$scope.currentState = $scope.LIST_STATE;
                $scope.saveGroups();
				$state.go($scope.LIST_STATE);
                $scope.msg = {type: "success", text: "Camada atualizada com sucesso!", dismiss: true};
                $scope.$apply();
            },
            errorHandler: function (message, exception) {
                $scope.msg = {type: "danger", text: message, dismiss: true};
                $scope.$apply();
            }
        });
    };

    /**
     * 
     */
    $scope.selectLayerGroup = function () {

        layerGroupService.listLayersGroupUpper({
            callback: function (result) {

                var dialog = $modal.open({
                    templateUrl: "modules/admin/ui/layer-config/popup/layer-group-popup.jsp",
                    controller: SelectLayerGroupPopUpController,
                    windowClass: 'xx-dialog grupo-camada-dialog',
                    resolve: {
                    	layerGroups: function () {
                            return result;
                        },
                        currentLayerGroup: function () {
                            return $scope.currentEntity.layerGroups;
                        }
                    }
                });

                dialog.result.then(function (result) {

                    if (result) {
                        $scope.currentEntity.layerGroups = result;
                        $scope.currentEntity.layerGroups.name = result.label;
                    }

                });

            },
            errorHandler: function (message, exception) {
                $scope.message = {type: "error", text: message};
                $scope.$apply();
            }
        });
    };

    /**
     *
     */
    $scope.selectDataSource = function () {
        var dialog = $modal.open({
            templateUrl: "modules/admin/ui/layer-config/popup/data-source-popup.jsp",
            controller: SelectDataSourcePopUpController,
            resolve: {
                dataSourceSelected: function () {
                    return $scope.currentEntity.dataSource;
                }
            }
        });

        dialog.result.then(function (result) {

            // atribui os dados selecionados

            if( $scope.currentEntity.dataSource && $scope.currentEntity.dataSource.id != result.id )
            {
                $scope.currentEntity.dataSource = result;
                $scope.currentEntity.title = null;
                $scope.currentEntity.name = null;
            }
            else
            {
                $scope.currentEntity.dataSource = result;
            }

        });
    };

    /**
     *
     */
    $scope.selectCamada = function () {
        var dialog = $modal.open({
            templateUrl: "modules/administrativo/ui/configuracao-camadas/popup/camadas-popup.html",
            controller: SelectCamadasPopUpController,
            windowClass: 'xx-dialog',
            resolve: {
                currentEntity: function () {
                    return $scope.currentEntity;
                }
            }
        });

        dialog.result.then(function (result) {

            if (result) {
                $scope.currentEntity.nome = result.nome;
                $scope.currentEntity.titulo = result.titulo;
                $scope.currentEntity.legenda = result.legenda;
            }

        });
    };

    /**
     *
     */
    $scope.selectGrupoAcesso = function() {
        var dialog = $modal.open({
            templateUrl: "modules/administrativo/ui/configuracao-camadas/popup/grupo-acesso-popup.html",
            controller: SelectGrupoAcessoPopUpController,
            resolve: {
            	selectedGroups : function () {
                    return $scope.selectedGroups;
                }
            }
        });

        dialog.result.then(function (result) {

            if (result) {
                for (var i = 0; i < result.length; i++) {
                    var index = $scope.findByIdInArray($scope.selectedGroups, result[i]);
                    var index2 = $scope.findByIdInArray($scope.originalGroups, result[i]);
                    var index3 = $scope.findByIdInArray($scope.removeGroups, result[i]);

                    //Identifica se marcou novos registros
                    if (index == -1 && index2 == -1) {
                        var indexAdd = $scope.findByIdInArray($scope.addGroups, result[i]);
                        if (indexAdd == -1)
                            $scope.addGroups.push(result[i]);
                    }

                    if (index3 > -1) {
                        $scope.removeGroups.splice(index3, 1);
                    }

                }
                for (var i = 0; i < $scope.selectedGroups.length; i++) {

                    var index = $scope.findByIdInArray(result, $scope.selectedGroups[i]);

                    if (index == -1) {
                        var index2 = $scope.findByIdInArray($scope.addGroups, $scope.selectedGroups[i]);
                        var index3 = $scope.findByIdInArray($scope.removeGroups, $scope.selectedGroups[i]);
                        var index4 = $scope.findByIdInArray($scope.originalGroups, $scope.selectedGroups[i]);

                        if (index2 > -1){
                            var indexAdd = $scope.findByIdInArray($scope.removeGroups, $scope.selectedGroups[i]);
                            if (indexAdd > -1)
                                $scope.addGroups.splice(indexAdd, 1);
                        }
                        if (index3 == -1 && index4 > -1) {
                            $scope.removeGroups.push($scope.selectedGroups[i]);
                        }

                    }
                }
                $scope.selectedGroups = result;
            }

        });
    };

    /**
     *
     */
    $scope.clearFields = function () {
        if (!$scope.data.showFields) {
            $scope.currentEntity.nomeUsuario = "";
            $scope.currentEntity.senha = "";
        }
    };

    /**
     *
     */
    $scope.close = function () {
        $scope.msg = null;
    };

    /**
     *
     * @param entity
     */
    $scope.removeGrupoAcesso = function (entity) {
        var index = $scope.findByIdInArray($scope.selectedGroups, entity);
        var index2 = $scope.findByIdInArray($scope.addGroups, entity);
        var index3 = $scope.findByIdInArray($scope.originalGroups, entity);
        if (index > -1) {
            $scope.selectedGroups.splice(index, 1);
        }
        if (index2 > -1) {
            $scope.addGroups.splice(index2, 1);
        }
        if (index3 > -1) {
            $scope.removeGroups.push(entity);
        }
    };

    /**
     *
     */
    $scope.saveGroups = function() {
        if ($scope.addGroups.length > 0) {
            $scope.linkGrupos();
        }
        if ($scope.removeGroups.length > 0) {
            $scope.unlinkGrupos();
        }
    }

    /**
     *
     */
    $scope.linkGrupos = function() {
        grupoCamadasService.linkGrupoAcesso($scope.addGroups, $scope.currentEntity.id, {
            callback: function(){
                $scope.addGroups = [];
                $scope.$apply();
            },
            errorHandler: function(error){
                $log.error(error);
            }
        })
    };

    /**
    *
    * @param pesquisa
    * @param grupoAcesso
    */
    $scope.unlinkGrupos = function() {
        grupoCamadasService.unlinkGrupoAcesso($scope.removeGroups, $scope.currentEntity.id, {
            callback: function(){
                $scope.removeGroups = [];
                $scope.$apply();
            },
            errorHandler: function(error){
                $log.error(error);
            }
        });
    };

    /**
     *
     * @param camadaId
     */
    $scope.loadGruposAcesso = function(camadaId) {
        grupoCamadasService.listGrupoAcessoByCamadaId(camadaId, {
            callback: function(result) {
                $scope.selectedGroups = result;
                $scope.originalGroups = result.slice(0);

                $scope.$apply();
            },
            errorHandler: function(error) {
                $log.error(error);
            }
        })
    };
};