/**
 * 
 */
package br.com.geocab.domain.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.json.parse.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import br.com.geocab.domain.entity.datasource.DataSource;
import br.com.geocab.domain.entity.layer.Attribute;
import br.com.geocab.domain.entity.layer.ExternalLayer;
import br.com.geocab.domain.entity.layer.FieldLayer;
import br.com.geocab.domain.entity.layer.Layer;
import br.com.geocab.domain.entity.layer.LayerGroup;
import br.com.geocab.domain.repository.attribute.IAttributeRepository;
import br.com.geocab.domain.repository.layergroup.ILayerGroupRepository;
import br.com.geocab.domain.repository.layergroup.ILayerRepository;
import br.com.geocab.infrastructure.geoserver.GeoserverConnection;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 
 * @author Vinicius Ramos Kawamoto
 * @since 22/09/2014
 * @version 1.0
 * @category Service
 *
 */

@Service
@RemoteProxy(name="layerGroupService")
public class LayerGroupService
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	
	/**
	 * 
	 */
	@Autowired
	private ILayerGroupRepository layerGroupRepository;
	
	/**
	 * 
	 */
	@Autowired
	private ILayerRepository layerRepository;

	/**
	 * 
	 */
	@Autowired
	private IAttributeRepository attributeRepository;
	
	/**
	 * 
	 */
	protected RestTemplate template;
	
	/*-------------------------------------------------------------------
	 *				 		    BEHAVIORS
	 *-------------------------------------------------------------------*/
	
	/**
	 * Method to insert an {@link LayerGroup}
	 * 
	 * @param layerGroup
	 * @return layerGroup
	 */
	public LayerGroup insertLayerGroup( LayerGroup layerGroup )
	{
		layerGroup.setPublished(false);
		return this.layerGroupRepository.save( layerGroup );
		
	}
	
	/**
	 * Method to update an {@link LayerGroup}
	 * 
	 * @param layerGroup
	 * @return layerGroup
	 */
	public LayerGroup updateLayerGroup( LayerGroup layerGroup )
	{
		return this.layerGroupRepository.save( layerGroup );
		
	}
	
	/**
	 * Method to save a list of {@link LayerGroup}
	 * 
	 * @param List<layerGroup>
	 * @return
	 */
	public void saveAllLayersGroup( List<LayerGroup> layerGroup )
	{
		this.prioritizeLayersGroup( layerGroup, null );
		
		this.prioritizeLayers( layerGroup);
	}
	
	/**
	 * Find {@link LayerGroup} by id
	 * 
	 * @param id
	 * @return
	 */
	@Transactional(readOnly = true)
	public LayerGroup findLayerGroupById( Long id )
	{
		return this.layerGroupRepository.findOne( id );
	}
	
	/**
	 * 
	 * @param List<layerGroup>
	 */
	public void saveAllParentLayerGroup(List<LayerGroup> layerGroup)
	{
		List<LayerGroup> layersGroups = this.layerGroupRepository.listAllParentLayerGroup();
		
		for (int i = 0; i < layersGroups.size(); i++)
		{
			layersGroups.get(i).setOrderLayerGroup(i);
			this.layerGroupRepository.save( layersGroups.get(i) );
		}
	}
	
	
	/**
	 * 
	 * @param List<layerGroup>
	 */
	public void publishLayerGroup(List<LayerGroup> layersGroup)
	{
		this.saveAllLayersGroup(layersGroup); // save layersGroup
		
		final List<LayerGroup> layerGroupOriginals = this.listLayersGroupUpper();//list parent groups
		
		for ( LayerGroup layerGroupOriginal : layerGroupOriginals )
		{
			this.recursive(layerGroupOriginal, null);//recursion to insert or update a layers group published	
		}

		//exclude published groups
		//set children groups in correct parent group
		this.populateChildrenInLayerGroupPublished();
		
		final List<LayerGroup> layersGroupPublished = this.layerGroupRepository.listLayersGroupUpperPublished();
		
		
		if ( layersGroupPublished != null )
		{
			for ( LayerGroup layerGroupPublished : layersGroupPublished )
			{
				this.removeLayerGroupPublished(layerGroupPublished);
				
				// remove o grupo de camada publicado superior
				// remove quando o rascunho for null e o publicado is true
				if (layerGroupPublished.getPublished() && layerGroupPublished.getDraft() == null)
				{
					this.layerGroupRepository.delete(layerGroupPublished);
				}
			}
		}
		
	}
	
	/**
	 * M�todo que seta todos os grupos publicados filhos em seus respectivos grupos publicados pai
	 */
	private void populateChildrenInLayerGroupPublished()
	{
		final List<LayerGroup> layersGroupPublished = this.layerGroupRepository.listAllLayersGroupPublished();
		
		for (LayerGroup layerGroupPublished : layersGroupPublished)
		{
			layerGroupPublished.setLayersGroup(this.layerGroupRepository.listLayersGroupPublishedChildren(layerGroupPublished.getId()));
			this.layerGroupRepository.save(layerGroupPublished);
		}
	}
	
	
	/**
	 * M�todo recursivo que remove os grupos de camadas publicados filhos
	 * @param gruposCamadasPublicados
	 * @param grupoCamadaPublicadosSuperior
	 */
	private void removeLayerGroupPublished( LayerGroup layerGroupPublished )
	{
		if ( layerGroupPublished.getLayersGroup() != null )
		{
			for (LayerGroup layerGroupChildPublished : layerGroupPublished.getLayersGroup())
			{
				// remove quando o rascunho for null e o publicado is true
				if (layerGroupChildPublished.getPublished() && layerGroupChildPublished.getDraft() == null )
				{
					removeLayerGroupPublished(layerGroupChildPublished);
					this.layerGroupRepository.delete(layerGroupChildPublished);
				}	
			}	
		}
		
	}
	
	
	/**
	 * 
	 * @param grupoCamadaOriginal
	 * @param grupoCamadaPaiPublicado
	 */
	public void recursive( LayerGroup layerGroupOriginal, LayerGroup layerGroupUpperPublished )
	{
		final Long layerGroupOriginalId = layerGroupOriginal.getId();
		
		// verifica se j� possui o grupo publicado
		final LayerGroup layerGroupPublishedExistent = this.layerGroupRepository.findByDraftId(layerGroupOriginalId);
		
		// efetua a c�pia do grupo de camadas original
		LayerGroup layerGroupPublished = new LayerGroup();
		BeanUtils.copyProperties(layerGroupOriginal, layerGroupPublished);
		
		// update nos dados do grupo publicado
		layerGroupPublished.setPublished(true);
		layerGroupPublished.setLayerGroupUpper(layerGroupUpperPublished);
		layerGroupPublished.setDraft(new LayerGroup(layerGroupOriginalId));
		layerGroupPublished.setLayersGroup(new ArrayList<LayerGroup>());
		layerGroupPublished.setLayers(new ArrayList<Layer>());
		
		// se j�s possui o grupo criado apenas altera o existente sen�o cria o grupo publicado
		if (layerGroupPublishedExistent != null)
		{
			layerGroupPublished.setId(layerGroupPublishedExistent.getId());
			layerGroupPublished = this.layerGroupRepository.save(layerGroupPublished);
		} 
		else
		{
			layerGroupPublished.setId(null);
			layerGroupPublished = this.layerGroupRepository.save(layerGroupPublished);
		}
		
		// cria��o/atualiza��o de camadas para camadas publicadas
		if ( layerGroupOriginal.getLayers() != null )
		{
			for ( Layer layerOriginal : layerGroupOriginal.getLayers() )
			{
				//final Camada camadaPublicadaExistente = this.camadaRepository.findByRascunhoId(camadaOriginal.getId());
				
				// cria��o da camada publicada que ir� conter a ordem publicada e os grupos
				Layer layerPublished = new Layer();
//				BeanUtils.copyProperties(camadaOriginal, camadaPublicada);
				
				
				// cria��o/update na camada publicada
				layerPublished.setName(layerOriginal.getName());
				layerPublished.setTitle(layerOriginal.getTitle());
				layerPublished.setMinimumScaleMap(layerOriginal.getMinimumScaleMap());
				layerPublished.setMaximumScaleMap(layerOriginal.getMaximumScaleMap());
				layerPublished.setOrderLayer(layerOriginal.getOrderLayer());
				layerPublished.setLayerGroup(layerGroupPublished);
				layerPublished.setPublished(true);
				
				// se j� possui a camada publicada apenas altera a existente sen�o cria a camada publicada
				if (layerOriginal.getPublishedLayer() != null)
				{
					layerPublished.setId(layerOriginal.getPublishedLayer().getId());
				} 
				else
				{
					layerPublished.setId(null);
				}
				
				layerPublished = this.layerRepository.save(layerPublished);
				
				layerGroupPublished.getLayers().add(layerPublished);
				layerGroupPublished = this.layerGroupRepository.save(layerGroupPublished);
				
				// update na camada original
				layerOriginal.setPublishedLayer(layerPublished);
				layerOriginal = this.layerRepository.save(layerOriginal);
			}
		}
		
		// faz a recurs�o para atualizar todos os filhos
		if ( layerGroupPublished.getLayersGroup() != null)
		{
			for ( LayerGroup layerGroupOriginalChild : layerGroupOriginal.getLayersGroup() )
			{
				this.recursive( layerGroupOriginalChild, layerGroupPublished );
			}
		}
		
	}

	/**
	 * 
	 * @param layerGroups
	 * @param layerGroupUpper
	 */
	private void prioritizeLayersGroup( List<LayerGroup> layerGroups, LayerGroup layerGroupUpper )
	{
		if ( layerGroups != null )
		{
			
			for (int i = 0; i < layerGroups.size(); i++)
			{
				layerGroups.get(i).setOrderLayerGroup(i);
				layerGroups.get(i).setLayerGroupUpper(layerGroupUpper);
				this.layerGroupRepository.save( layerGroups.get(i) );
				
				prioritizeLayersGroup(layerGroups.get(i).getLayersGroup(), layerGroups.get(i));
			}
		}
	}
	
	/**
	 * 
	 * @param layerGroups
	 */
	private void prioritizeLayers( List<LayerGroup> layerGroups )
	{
		if ( layerGroups != null )
		{
			for (LayerGroup layerGroup : layerGroups)
			{
				if( layerGroup.getLayers() != null )
				{					
					if( layerGroup.getLayers().size() > 0 )
					{
						for(int j = 0; j < layerGroup.getLayers().size(); j++)
						{
							layerGroup.getLayers().get(j).setOrderLayer(j);
							layerGroup.getLayers().get(j).setLayerGroup(layerGroup);
							this.layerRepository.save( layerGroup.getLayers().get(j) );
						}
					}
				}
				
				prioritizeLayers(layerGroup.getLayersGroup());
			}
		}
	}
	
	/**
	 * m�todo para remover um {@link GrupoCamadas}
	 * 
	 * @param id
	 */
	public void removeLayerGroup( Long id )
	{
		final LayerGroup layerGroup = this.layerGroupRepository.findOne(id);
		
		LayerGroup layerGroupPublished = this.layerGroupRepository.findByDraftId(id);
		
		// verifica se existe o grupo j� publicado
		if (layerGroupPublished != null)
		{
			// seta null no campo rascunho do grupo de camada publicado para permitir excluir o grupo original
			layerGroupPublished.setDraft(null);
			layerGroupPublished = this.layerGroupRepository.save(layerGroupPublished);
		}
		
		this.layerGroupRepository.delete( layerGroup );
	}
	
	/**
	 * 
	 * @param filter
	 * @param idExcluso
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<LayerGroup> listLayersGroupUpper()
	{
		List<LayerGroup> layersGroup = this.layerGroupRepository.listLayersGroupUpper();
		
		setLegendsLayers(layersGroup);
		
		return layersGroup;
		
	}
	
	/**
	 * M�todo que retorna a estrutura completa dos grupos de camadas publicados
	 * @param filter
	 * @param idExcluso
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<LayerGroup> listLayerGroupUpperPublished()
	{
		List<LayerGroup> layersGroupUpperPublished = new ArrayList<LayerGroup>();
		final List<LayerGroup> layersGroupPublished = this.layerGroupRepository.listAllLayersGroupPublished();
		
		if ( layersGroupPublished != null )
		{
			for (LayerGroup layerGroupPublished : layersGroupPublished)
			{
				layerGroupPublished.setLayers(this.layerRepository.listLayersByLayerGroupPublished(layerGroupPublished.getId()));
				
				if (layerGroupPublished.getLayerGroupUpper() == null )
				{
					layersGroupUpperPublished.add(layerGroupPublished);
				}
				
			}
		}
		
		setLegendsLayers(layersGroupUpperPublished);
		
		return layersGroupUpperPublished;
		
	}
	
	/**
	 * 
	 * @param gruposCamadas
	 * @param grupoCamadasSuperior
	 */
	private void setLegendsLayers( List<LayerGroup> layersGroup )
	{
		if ( layersGroup != null )
		{
			
			for (LayerGroup layerGroup : layersGroup)
			{
				if( layerGroup.getLayers() != null )
				{					
					if( layerGroup.getLayers().size() > 0 )
					{
						for(int j = 0; j < layerGroup.getLayers().size(); j++)
						{
							// traz a legenda da camada do GeoServer
							if( layerGroup.getLayers().get(j).getDataSource().getUrl() != null ) {
								layerGroup.getLayers().get(j).setLegend((getLegendLayerFromGeoServer(layerGroup.getLayers().get(j))));	
							}
							
						}
					}
				}
				
				setLegendsLayers(layerGroup.getLayersGroup());
			}
		}
	}
	
	
	//Camadas
	
	/**
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly=true)
	public Page<LayerGroup> listLayerGroups(String filter, PageRequest pageable)
	{
		return this.layerGroupRepository.listByFilter(filter, pageable);
	}
	
	/**
	 * 
	 * @param filter
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<LayerGroup> listAllLayerGroups()
	{
		return this.layerGroupRepository.findAll();
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws JAXBException
	 */
	@Transactional(readOnly=true)
	public List<ExternalLayer> listExternalLayersByFilters( DataSource dataSource )
	{
		GeoserverConnection geoserverConnection = new GeoserverConnection();
		return geoserverConnection.listExternalLayersByFilters(dataSource);
	}
	
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws JAXBException
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@Transactional(readOnly=true)
	public List<FieldLayer> listFieldLayersByFilter(Layer layer)
	{
		String sUrl;
		
		int posicao = layer.getDataSource().getUrl().lastIndexOf("geoserver/");
		String urlGeoserver = layer.getDataSource().getUrl().substring(0, posicao+10);
		
		sUrl = urlGeoserver + ExternalLayer.CAMPO_CAMADA_URL + layer.getName();
		
		BufferedReader reader = null;
	    try 
	    {
	        URL url = new URL(sUrl);
	        reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	        {
	        	buffer.append(chars, 0, read); 
	        }
	        
			try
			{
				JSONObject json = new JSONObject(buffer.toString());
				JSONArray featureTypes = json.getJSONArray("featureTypes");
				JSONObject properties = (JSONObject) featureTypes.get(0);
				JSONArray propertiesArray = (JSONArray) properties.get("properties");
				
				List<FieldLayer> campos = new ArrayList<FieldLayer>();
				
				for (int i = 0; i < propertiesArray.length(); i++) 
				{
					
					JSONObject jsonOb = (JSONObject) propertiesArray.get(i);
					FieldLayer fieldLayer = new FieldLayer();
					fieldLayer.setNome(jsonOb.get("name").toString());
					campos.add(fieldLayer);
				}
				
				return campos;
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
	    }
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		} 
	    finally 
	    {
	        if (reader != null) 
	        {
	        	 try
	 			{
	 				reader.close();
	 			}
	 			catch (IOException e)
	 			{
	 				e.printStackTrace();
	 			}
	        }
	    }
		return null;
	}

	
	/**
	 * M�todo respons�vel para listar as camadas
	 *
	 * @param filter
	 * @param idExcluso
	 * @param pageable
	 * @return camadas
	 */
	@Transactional(readOnly=true)
	public Page<Layer> listLayersByFilters( String filter, PageRequest pageable )
	{
		Page<Layer> layers = this.layerRepository.listByFilters(filter, null, pageable);
		
		for ( Layer layer : layers.getContent() )
		{
			// traz a legenda da camada do GeoServer
			if(layer.getDataSource().getUrl() != null ) {
				layer.setLegend(getLegendLayerFromGeoServer(layer));	
			}
		}
		
		return layers;
	}
	
	/**
	 * 
	 * @return
	 */
	@Transactional(readOnly=true)
	public List<Layer> listLayersPublished()
	{
		return this.layerRepository.listLayersPublished();
	}
	
	/**
	 * Method to inserir uma {@link Layer}
	 * @param layer
	 * @return
	 */
	public Layer insertLayer( Layer layer )
	{
		layer.setLayerGroup(this.findLayerGroupById(layer.getLayerGroup().getId()));
		layer.setPublished(false);
		return this.layerRepository.save( layer );
	}
	
	/**
	 * m�todo para atualizar uma {@link Camada}
	 * 
	 * @param camada
	 * @return camada
	 */
	public Layer updateLayer( Layer layer )
	{
		Layer layerDatabase = this.findLayerById(layer.getId());
		layer.setLayerGroup(layer.getLayerGroup());
		
		List<Attribute> attributesByLayer = attributeRepository.listAttributeByLayer(layer.getId());
	
		for(Attribute attribute : attributesByLayer) {
			
			Boolean attributeDeleted = true;
			
			for(Attribute attributeInLayer : layer.getAttributes()) {
				if(	attributeInLayer.getId() == attribute.getId() ) {
					attributeDeleted = false;
				}
			}
			
			if( attributeDeleted ) {
				this.attributeRepository.delete( attribute.getId() );
			}
			
		}
		
		/* Na atualiza��o n�o foi permitido modificar a fonte de dados, camada e t�tuulo, dessa forma, 
		Os valores originais s�o mantidos. */
		layer.setDataSource(layerDatabase.getDataSource());
		layer.setName(layerDatabase.getName());
		
		return this.layerRepository.save( layer );
	}
	
	/**
	 * m�todo para remover uma {@link Camada}
	 * 
	 * @param id
	 */
	public void removeLayer( Long id )
	{	
		this.layerRepository.delete( id );
	}
	
	/**
	 * m�todo para encontrar uma {@link Camada} pelo id
	 * 
	 * @param id
	 * @return camada
	 * @throws JAXBException 
	 */
	@Transactional(readOnly = true)
	public Layer findLayerById( Long id )
	{
		final Layer layer = this.layerRepository.findOne(id);
		
		// traz a legenda da camada do GeoServer
		if( layer.getDataSource().getUrl() != null ) {
			layer.setLegend(getLegendLayerFromGeoServer(layer));	
		}
		
		return layer;
	}
	
	
	/**
	 * M�todo para listar as configura��es de camadas paginadas com op��o do filtro
	 *
	 * @param filter
	 * @param pageable
	 * @return
	 * @throws JAXBException 
	 */
	@Transactional(readOnly=true)
	public Page<Layer> listLayersConfigurationByFilter( String filter, Long dataSourceId, PageRequest pageable )
	{
		Page<Layer> layers = this.layerRepository.listByFilters(filter, dataSourceId, pageable);
		
		for ( Layer layer : layers.getContent() )
		{
			// traz a legenda da camada do GeoServer
			layer.setLegend(getLegendLayerFromGeoServer(layer));
		}
		
		return layers;
	}
	
	
	/**
	 * M�todo que busca a legenda de uma camada no geo server
	 * @param camada
	 * @return
	 */
	public String getLegendLayerFromGeoServer( Layer layer )
	{
		int position = layer.getDataSource().getUrl().lastIndexOf("geoserver/");
		String urlGeoserver = layer.getDataSource().getUrl().substring(0, position+10);
		
		return urlGeoserver + Layer.LEGEND_GRAPHIC_URL + layer.getName() + Layer.LEGEND_GRAPHIC_FORMAT;
	}
	
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public String listAllFeatures(String url)
	{
		this.template = new RestTemplate();
		
		String features = this.template.getForObject(url, String.class);
		
		return features;
	}
	
	
	/**
	 * 
	 * @param grupoCamadas
	 */
	private void removeLayersGroupPublishedEmpty( LayerGroup layerGroup )
	{
		if ( layerGroup.getLayersGroup() != null )
		{
			List<LayerGroup> layersGroupExclusion = new ArrayList<LayerGroup>();
			
			for (LayerGroup layerGroupChild : layerGroup.getLayersGroup())
			{
				removeLayersGroupPublishedEmpty(layerGroupChild);
				
				// remove o grupo de camada publicado superior vazio
				if (layerGroupChild.getLayersGroup().isEmpty() && layerGroupChild.getLayers().isEmpty())
				{
					layersGroupExclusion.add(layerGroupChild);
				}
				
			}
			
			layerGroup.getLayersGroup().removeAll(layersGroupExclusion);
		}
		
	}
	
	public List<Attribute> listAttributesByLayer(Long layerId){
		
		return this.attributeRepository.listAttributeByLayer(layerId);
	}
}
