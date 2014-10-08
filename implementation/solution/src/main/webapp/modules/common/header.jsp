<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>

<!-- header -->
<div id="header-page">
	<!-- navbar 1 -->
	<div class="navbar navbar-1" style="z-index: 1002;">
		<a class="logo" href="./">&nbsp;</a>
		<div class="nav-collapse collapse">
			<div class="left-side">
				<div style="float: left; margin-top: 23px">
					<span style="font-size: 17px"><b style="font-size: 17px">GEOCAB</b>
						- Cultivando Água Boa</span>
				</div>
			</div>

			<ul class="nav navbar-nav pull-right right-side">
				<li><a href="#" class="active box-separator"
					style="border: none; margin-top: 7px;"> <span
						style="color: #000000" ng-bind="usuarioAutenticado.name"></span>
				</a></li>
				<li class="box-separator"></li>
				<li>
					<div class="user-logout">
						<a onclick="autenticacaoService.deslogar();" href="./authentication">Logout</a>
					</div>
				</li>
			</ul>
		</div>
	</div>
	<!-- navbar 2 -->
	<!-- ng-if="usuarioAutenticado.papelUsuario == 'ADMINISTRADOR'" -->
	<div class="navbar navbar-2" style="z-index: 1001;">
		<div class="navbar-inner border-radius-0">

			<div class="nav-collapse collapse">
				<ul class="nav navbar-nav">
											
					<li class="position-relative"><a href="./"
						style="width: 50px;" ng-class="{active: menuActive == null}"><span
							class="icon-mapa-interativo"></span></a></li>

					<li class="position-relative"><a
						href="admin#/users"
						ng-class="{active: menuActive == 'users'}"
						style="width: 150px;"><spring:message code="admin.users.Users"/></a></li>
						
					<li class="position-relative"><a
						href="admin#/data-source"
						ng-class="{active: menuActive == 'data-source'}"
						style="width: 150px;"><spring:message code="admin.datasource.Data-Source"/></label></a></li>

					<li class="position-relative"><a
						href="admin#/layer-group"
						ng-class="{active: menuActive == 'layer-group'}"
						style="width: 150px;"><spring:message code="admin.layer-group.Layer-group" /></a></li>

                    <li class="position-relative"><a
                            href="admin#/layer-config"
                            ng-class="{active: menuActive == 'layer-config'}"
                            style="width: 150px;"><spring:message code="admin.layer-config.Layers"/></a></li>
                </ul>
			</div>
		</div>
	</div>
</div>


</html>