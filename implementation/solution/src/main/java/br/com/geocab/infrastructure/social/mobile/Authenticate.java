/**
 * 
 */
package br.com.geocab.infrastructure.social.mobile;

import javax.servlet.http.HttpServletRequest;

import br.com.geocab.domain.entity.account.User;

/**
 * @author emanuelvictor
 *
 */
public interface Authenticate
{
	/**
	 * Realiza o login do usu�rio, e devolve uma sess�o para o navegador
	 */
	User login(User user, HttpServletRequest request);
	/**
	 * Utilizado para validar o nome do usu�rio (login) do usu�rio
	 */
	User validateUsername(User user);
	
	/**
	 * Utilizado para validar o token de acesso do usu�rio
	 */
	User validateToken(User user);
}
