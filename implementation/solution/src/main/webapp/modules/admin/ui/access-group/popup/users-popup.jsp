<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>

<div class="modal-content">

	<div class="modal-header">
		<button type="button" class="close" ng-click="close(true)"><span aria-hidden="true">&times;</span><span class="sr-only"><spring:message code="admin.access-group.Close"/></span></button>
	    <h3 class="modal-title"></h3>
	</div>

	<div class="modal-body" ng-init="initialize();" style="overflow: visible">
	
		<div ng-include="static/libs/eits-directives/alert/alert.html"></div>
		
        <form novalidate name="form" default-button="{{buttonInsert}}">
            <input type="text" class="form-control" autofocus ng-model="data.filter" placeholder="<spring:message code="admin.access-group.Search-by-name"/>" style="width: 300px; float: left; margin-bottom: 10px;"/>
            <input type="submit" class="btn btn-default" style="margin-left: 5px" value="Search" ng-disabled="data.filter.length < 3" ng-click="listUsuariosByFilters(data.filter)"/>
            <div ng-show="showLoading" class="grid-loading" style="top: 21px; left: 702px;"></div>
        </form>

		<br style="clear: both"/>

		<div ng-grid="gridOptions" style="height: 350px; border: 1px solid rgb(212,212,212);"></div>

		<div class="grid-elements-count" ng-show="currentPage.totalElements > 0">
			{{users.length}} <spring:message code="admin.access-group.Registers"/>
		</div>
        
	</div>
	<div class="modal-footer">
		<button id="buttonClose" ng-disabled="selectedEntity == null && gridOptions.selectedItems.length == 0" class="btn btn-primary" ng-click="close(false)">Selecionar</button>
        <button class="btn btn-default" ng-click="close(true)"><spring:message code="admin.access-group.Close"/></button>
	</div>

</div>

</html>