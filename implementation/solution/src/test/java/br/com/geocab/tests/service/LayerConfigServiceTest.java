/**
 * 
 */
package br.com.geocab.tests.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import br.com.geocab.domain.entity.datasource.DataSource;
import br.com.geocab.domain.entity.layer.Attribute;
import br.com.geocab.domain.entity.layer.AttributeType;
import br.com.geocab.domain.entity.layer.Layer;
import br.com.geocab.domain.entity.layer.LayerGroup;
import br.com.geocab.domain.entity.layer.MapScale;
import br.com.geocab.domain.service.DataSourceService;
import br.com.geocab.domain.service.LayerGroupService;
import br.com.geocab.tests.AbstractIntegrationTest;

/**
 * @author Thiago
 *
 */
public class LayerConfigServiceTest extends AbstractIntegrationTest
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	@Autowired
	private LayerGroupService layerGroupService;

	@Autowired
	private DataSourceService dataSourceService;

	/*-------------------------------------------------------------------
	 *				 		    BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 */
	@Test
	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml" })
	public void insertLayer()
	{
		this.authenticate(100L);

		Layer layer = new Layer();

		Assert.assertNull(layer.getId());

		DataSource dataSource = dataSourceService.findDataSourceById(1L);
		LayerGroup layerGroup = layerGroupService.findLayerGroupById(1L);

		layer.setDataSource(dataSource);
		layer.setLayerGroup(layerGroup);
		layer.setName("bdgeo:v_ag_demandantes2");
		layer.setTitle("Demandantes2");
		layer.setMaximumScaleMap(MapScale.UM100km);
		layer.setOrderLayer(1);
		layer.setMinimumScaleMap(MapScale.UM10km);
		layer.setLegend(
				"http://172.17.6.112:80/geoserver/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=v_ag_demandantes");

		layer = layerGroupService.insertLayer(layer);

		Assert.assertNotNull(layer);
		Assert.assertNotNull(dataSource);
		Assert.assertNotNull(layerGroup);
		Assert.assertEquals(dataSource, layer.getDataSource());
		Assert.assertEquals(layerGroup, layer.getLayerGroup());
		Assert.assertEquals("bdgeo:v_ag_demandantes2", layer.getName());
		Assert.assertEquals("Demandantes2", layer.getTitle());
		Assert.assertEquals(MapScale.UM100km, layer.getMaximumScaleMap());
		Assert.assertEquals(MapScale.UM10km, layer.getMinimumScaleMap());
		Assert.assertEquals(
				"http://172.17.6.112:80/geoserver/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=v_ag_demandantes",
				layer.getLegend());
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml" })
	public void updateLayer() throws Exception
	{
		this.authenticate(100L);

		Layer layer = this.layerGroupService.findLayerById(2L);

		layer.setName("bdgeo:v_ag_demandantes2");
		layer.setTitle("Demandantes2");
		layer.setMaximumScaleMap(MapScale.UM100m);
		layer.setMinimumScaleMap(MapScale.UM200m);
		layer.setOrderLayer(1);

		layer = layerGroupService.updateLayer(layer);

		Assert.assertNotNull(layer);
		Assert.assertEquals("bdgeo:v_ag_demandantes", layer.getName());
		Assert.assertEquals("Demandantes2", layer.getTitle());
		Assert.assertEquals(MapScale.UM100m, layer.getMaximumScaleMap());
		Assert.assertEquals(MapScale.UM200m, layer.getMinimumScaleMap());

	}

	/**
	 * 
	 */
	@Test
	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml" })
	public void findLayerById() throws Exception
	{
		this.authenticate(100L);

		Layer layer = this.layerGroupService.findLayerById(2L);
		Assert.assertTrue(layer.getId().equals(2L));
	}

	/**
	 * 
	 */
	@Test
	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml"

	})
	public void removeLayer()
	{
		this.authenticate(100L);

		this.layerGroupService.removeLayer(2L);
	}

	/**
	 * 
	 */
	@Test
	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml" })
	public void insertLayerWithPhotoAlbum()
	{
		this.authenticate(100L);

		Layer layer = new Layer();

		Assert.assertNull(layer.getId());

		DataSource dataSource = dataSourceService.findDataSourceById(1L);
		LayerGroup layerGroup = layerGroupService.findLayerGroupById(1L);

		layer.setDataSource(dataSource);
		layer.setLayerGroup(layerGroup);
		
		
		
		Attribute attribute = new Attribute();
		attribute.setRequired(false);
		attribute.setType(AttributeType.NUMBER);
		attribute.setName("NOME");
		
		
		List<Attribute> attributes = new ArrayList<>();
		
		layer.setAttributes(attributes);
		layer.setName("bdgeo:v_ag_demandantes2");
		layer.setTitle("Demandantes2");
		layer.setMaximumScaleMap(MapScale.UM100km);
		layer.setOrderLayer(1);
		layer.setMinimumScaleMap(MapScale.UM10km);
		layer.setLegend(
				"http://172.17.6.112:80/geoserver/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=v_ag_demandantes");

		layer = layerGroupService.insertLayer(layer);

		Assert.assertNotNull(layer);
		Assert.assertNotNull(dataSource);
		Assert.assertNotNull(layerGroup);
		Assert.assertEquals(dataSource, layer.getDataSource());
		Assert.assertEquals(layerGroup, layer.getLayerGroup());
		Assert.assertEquals("bdgeo:v_ag_demandantes2", layer.getName());
		Assert.assertEquals("Demandantes2", layer.getTitle());
		Assert.assertEquals(MapScale.UM100km, layer.getMaximumScaleMap());
		Assert.assertEquals(MapScale.UM10km, layer.getMinimumScaleMap());
		Assert.assertEquals("http://172.17.6.112:80/geoserver/ows?service=WMS&request=GetLegendGraphic&format=image%2Fpng&width=20&height=20&layer=v_ag_demandantes", layer.getLegend());
	}

//	/**
//	 * 
//	 */
//	@Test
//	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
//	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml" })
//	public void insertLayerWithPhotoAlbumAndListPhotosByPath()
//	{
//
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	@DatabaseSetup(type = DatabaseOperation.INSERT, value =
//	{ "/dataset/LayerConfigDataSet.xml", "/dataset/AccountDataSet.xml" })
//	public void insertLayerWithPhotoAlbumAndFindPhotoById()
//	{
//
//	}

}
