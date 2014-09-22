package br.com.geocab.domain.repository.layergroup;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.geocab.domain.entity.layer.Layer;
import br.com.geocab.infrastructure.jpa2.springdata.IDataRepository;

/**
 * @author Vinicius Ramos Kawamoto
 * @since 22/09/2014
 * @version 1.0
 * @category Repository
 *
 */
public interface ILayerRepository extends IDataRepository<Layer, Long>
{
	/*-------------------------------------------------------------------
	 *				 		     BEHAVIORS
	 *-------------------------------------------------------------------*/	
	/**
	 * @param filter
	 * @param pageable
	 * @return
	 */
	@Query(value="SELECT new Layer( layer.id, layer.name, layer.title, layer.startEnabled, layer.startVisible, layer.orderLayer, layer.minimumScaleMap, layer.maximumScaleMap, dataSource, layerGroup ) " +
				"FROM Layer layer " +
				"LEFT OUTER JOIN layer.dataSource dataSource " + 
				"LEFT OUTER JOIN layer.layerGroup layerGroup " +
				"WHERE ( ( LOWER(layer.name) LIKE '%' || LOWER(CAST(:filter AS string))  || '%' OR :filter = NULL ) " +
				"OR ( LOWER(layer.title) LIKE '%' || LOWER(CAST(:filter AS string))  || '%' OR :filter = NULL ) " +
				"OR ( LOWER(dataSource.name) LIKE '%' || LOWER(CAST(:filter AS string))  || '%' OR :filter = NULL ) " +
				"OR ( LOWER(layerGroup.name) LIKE '%' || LOWER(CAST(:filter AS string))  || '%' OR :filter = NULL ) ) " +
				"AND ( layer.dataSource.id = :dataSourceId OR :dataSourceId = NULL ) " +
				"AND ( layer.published = false )")
	public Page<Layer> listByFilters( @Param("filter") String filter, @Param("dataSourceId") Long dataSourceId, Pageable pageable );
	
	
	/**
	 * 
	 * @param idLayer
	 * @return
	 */
	@Query(value="SELECT layer " +
			"FROM Layer layer " +
			"WHERE ( layer.publishedLayer.layerGroup.id = :idLayer ) " +
			"ORDER BY layer.publishedLayer.orderLayer")
	public List<Layer> listLayersByLayerGroupPublished( @Param("idLayer") Long idLayer);
}
