<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>

	<style>
	.content {
		margin: 0;
		width: 100%;
		position : static;
	}
	
/* 	div#olmap { */
/* 		width:62%;  */
/* 		float: right; */
/* 	} */
	
	</style>
	<!-- OpenLayers 3  -->
	<link rel="stylesheet" href="static/libs/openlayers/ol.css" type="text/css">
	
	<!-- OpenLayers 3 -->
	<script src="<c:url value="/static/libs/openlayers/ol.js"/>"></script>
	
	<!-- Google Maps -->
	<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?client=gme-itaipubinacional&sensor=false&channel=geocab"></script>

		<!-- Posting evaluation - List -->
		<div style="width:35%; height: 100%; float:left; margin: 20px;">
		        
			<!-- Filter Bar -->
			<div class="search-div" style="margin-bottom:10px">
				<form>
					<input type="text" ng-model="data.filter" class="form-control" title="<spring:message code="admin.users.Search"/>" placeholder="<spring:message code="admin.marker-moderation.Name-layer-tag"/>" style="float:left; width:300px;margin-right:10px"/>
					
					<select class="form-control" style="width:22%;">
						<option value="" disabled="" selected="" style="display:none"><spring:message code="admin.marker-moderation.Layer"/> ></option>
					</select>
					
					<a class="btn btn-mini" ng-if="!hiding" ng-click="showFields(false)" style="position:absolute;top:10px;left:30%"><i class="glyphicon glyphicon-chevron-up"></i></a>													    
			    	<a class="btn btn-mini" ng-if="hiding" 	ng-click="showFields(true)"  style="position:absolute;top:10px;left:30%"><i class="glyphicon glyphicon-chevron-down"></i></a>
			    </form>		    		    		    
			
				<div style="margin-top:10px" ng-hide="hiding">
					<form style="display:flex">
						<input type="checkbox"> Revisão <i style="margin-right:5px"></i>
						
						<select class="form-control" style="width:24%;margin-right:10px" >
							<option value="" disabled="" selected="" style="display:none"><spring:message code="admin.marker-moderation.Refused"/></option>
						</select>
						
						<input name="date" class="form-control datepicker" style="width:20%;;margin-right:10px" placeholder="<spring:message code="admin.marker-moderation.Beginning"/>" onfocus="(this.type='date')" onblur="(this.type='text')"  id="date" />
						
						<input name="date" class="form-control datepicker" style="width:20%;;margin-right:10px" placeholder="<spring:message code="admin.marker-moderation.Ending"/>" onfocus="(this.type='date')" onblur="(this.type='text')" id="date"/>
					</form>
				</div>
			
			</div>
			
			<div ng-grid="gridOptions" style="height: 499px;border: 1px solid rgb(212,212,212);"></div>					
			
			<div class="gridFooterDiv">
			       <pagination style="text-align: center"
			                   total-items="currentPage.total" rotate="false"
			                   items-per-page="currentPage.size"
			                   max-size="currentPage.totalPages"
			                   ng-change="changeToPage(data.filter, currentPage.pageable.pageNumber)"
			                   ng-model="currentPage.pageable.pageNumber" boundary-links="true"
			                   previous-text="‹" next-text="›" first-text="«" last-text="»">
			       </pagination>
			</div> 	
			 
			    <div class="grid-elements-count">
			        {{currentPage.numberOfElements}} <spring:message code="admin.users.of"/> {{currentPage.totalElements}} <spring:message code="admin.users.items"/>
			    </div>
		
		</div>
		
		<!-- Map -->
		<div style="position : absolute;top:116px;left : 38%;right: 0;bottom: 0;">
				<!-- Openlayer Map -->
				<div id="olmap" style="position : absolute;top : 0;left : 0;right: 0;bottom: 0;"> 
					<div id="popup" class="ol-popup">
						<div id="popup-content"></div>
					</div>
					<div id="info"></div>
				</div>
		</div>
	
	
	
	
	
</html>