/**
 * 
 */
package br.com.geocab.domain.service;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.geocab.domain.entity.account.UserRole;
import br.com.geocab.domain.entity.datasource.DataSource;
import br.com.geocab.infrastructure.geoserver.GeoserverConnection;
import br.com.geocab.domain.repository.datasource.IDataSourceRepository;

/**
 * Classe pertinente ao controle das a��es da {@link FonteDados}}
 * 
 * @author Marcos
 * @since 27/05/2014
 * @version 1.0
 * @category Service
 *
 */

@Service
@Transactional
@RemoteProxy(name="dataSourceService")
//@PreAuthorize("hasRole('"+UserRole.ADMINISTRADOR_VALUE+"')")
public class DataSourceService
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	private static final Logger LOG = Logger.getLogger( DataSourceService.class.getName() );
	/**
	 * Reposit�rio de uma {@link DataSource}
	 */
	@Autowired
	private IDataSourceRepository dataSourceRepository;
	
	/*-------------------------------------------------------------------
	 *				 		    BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 * m�todo para inserir uma {@link DataSource}
	 * 
	 * @param fonteDados
	 * @return fonteDados
	 */
	public DataSource insertDataSource( DataSource dataSource )
	{
		try{
			dataSource = this.dataSourceRepository.save( dataSource );
		}
		catch ( DataIntegrityViolationException e )
		{
			LOG.info( e.getMessage() );
			final String error = e.getCause().getCause().getMessage();
//			
//			// Captura e retorna a exce��o de dados �nicos
//			if(error.contains("uk_fonte_dados_nome"))
//			{
//				throw new IllegalArgumentException( Messages.getException( "fontedados.nome_existe" ) );
//			}
//			else if(error.contains("uk_fonte_dados_endereco"))
//			{
//				throw new IllegalArgumentException( Messages.getException( "fontedados.endereco_existe" ) );
//			}
		}
		return dataSource; 
	}
	
	/**
	 * m�todo para atualizar uma {@link FonteDados}
	 * 
	 * @param fonteDados
	 * @return fonteDados
	 */
	public DataSource updateDataSource( DataSource dataSource )
	{			
		try{
			dataSource = this.dataSourceRepository.save( dataSource );
		}
		catch ( DataIntegrityViolationException e )
		{
			LOG.info( e.getMessage() );
			final String error = e.getCause().getCause().getMessage();
			
//			// Captura e retorna a exce��o de dados �nicos
//			if(error.contains("uk_fonte_dados_nome"))
//			{
//				throw new IllegalArgumentException( Messages.getException( "fontedados.nome_existe" ) );
//			}
//			else if(error.contains("uk_fonte_dados_endereco"))
//			{
//				throw new IllegalArgumentException( Messages.getException( "fontedados.endereco_existe" ) );
//			}
		}
		return dataSource;
	}
	
	/**
	 * m�todo para remover uma {@link FonteDados}
	 * 
	 * @param id
	 */
	public void removeDataSource( Long id )
	{
		this.dataSourceRepository.delete( id );
	}
	
	/**
	 * m�todo para encontrar uma {@link FonteDados} pelo id
	 * 
	 * @param id
	 * @return fonteDados
	 * @throws JAXBException 
	 */
	@Transactional(readOnly = true)
	public DataSource findDataSourceById( Long id )
	{
		return this.dataSourceRepository.findOne( id );
	}
	
	/**
	 * 
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<DataSource> listAllDataSource()
	{
		return this.dataSourceRepository.listAll();
	}
	
	/**
	 * M�todo para listar as fontes de dados paginadas com op��o do filtro
	 *
	 * @param filter
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly=true)
	public Page<DataSource> listDataSourceByFilters( String filter, PageRequest pageable )
	{
		return this.dataSourceRepository.listByFilters(filter, pageable);
	}
	
	@Transactional(readOnly=true)
	public boolean testaConexao(String url)
	{
		GeoserverConnection geoserverConnection = new GeoserverConnection();
		return geoserverConnection.testaConexao(url);
	}
	
}
