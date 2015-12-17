/**
 * 
 */
package br.com.geocab.domain.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.io.FileTransfer;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.files.ShpFileType;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import br.com.geocab.domain.entity.layer.Layer;
import br.com.geocab.domain.entity.marker.Marker;
import br.com.geocab.domain.entity.shapefile.ShapeFile;
import br.com.geocab.domain.repository.marker.IMarkerRepository;

/**
 * @author emanuelvictor
 *
 */
@Service
@RemoteProxy(name = "shapeFileService")
public class ShapeFileService
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 * shapeFile path
	 */
	private static String PATH_SHAPE_FILES = "/tmp/geocab/files/shapefile/";
	/**
	 * export shapeFile path
	 */
	private static String PATH_SHAPE_FILES_EXPORT = PATH_SHAPE_FILES + "export/";

	/**
	 * import shapeFile path
	 */
	private static String PATH_SHAPE_FILES_IMPORT = PATH_SHAPE_FILES + "import/";
		
	/**
	 * Log
	 */
	private static final Logger LOG = Logger.getLogger(DataSourceService.class.getName());
	/**
	 * 
	 */
	@Autowired
	private IMarkerRepository markerRepository;	
	/*-------------------------------------------------------------------
	 *				 		    CONSTRUCTORS
	 *-------------------------------------------------------------------*/
	
	/**
	 * Sempre que o component for instanciado, 
	 * o mesmo vai verificar se existe a pasta shapefile e caso a mesma n�o exista ser� criada
	 */
	public ShapeFileService()
	{
		super();
		try
		{
			new File(PATH_SHAPE_FILES_EXPORT).mkdirs();
			new File(PATH_SHAPE_FILES_IMPORT).mkdirs();
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			LOG.info(e.getMessage());
		}
	}
		
	
	/*-------------------------------------------------------------------
	 *				 		    BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 * Servi�o de importa��o de shapeFile
	 * 
	 * @param shapeFile
	 * @return
	 */
	public List<Marker> importShapeFile(List<ShapeFile> shapeFiles)
	{
		try
		{
			// L� os arquivos
			List<File> files = new ArrayList<File>();
			for (ShapeFile shapeFile : shapeFiles)
			{
				files.add(this.readFile(shapeFile));
			}
			
			
			for (ShapeFile shapeFile : shapeFiles)
			{
				if (shapeFile.getType() == ShpFileType.SHP)
				{
					importt(this.readFile(shapeFile));
				}
			}
			
			
			// ShpFiles shpFiles = new ShpFiles(file);
			//
			// FileDataStore store = FileDataStoreFinder.getDataStore(file);
			// SimpleFeatureSource featureSource = store.getFeatureSource();

			// Deleta o arquivo
			delete(new File(PATH_SHAPE_FILES_IMPORT));
			
			return null;
		}
		catch (Exception e)
		{
			LOG.info(e.getMessage());
			throw new RuntimeException("Ocorreu um erro durante a importa��o: " + e.getMessage());
		}
	}
	
	private void importt(File file){
		
		try
		{
			Map<String, Object> map = new HashMap<String, Object>();
		    map.put("url", file.toURI().toURL());

		    DataStore dataStore = DataStoreFinder.getDataStore(map);
		    String typeName = dataStore.getTypeNames()[0];

		    FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);

		    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();
		    try (FeatureIterator<SimpleFeature> features = collection.features()) {
		        while (features.hasNext()) {
		            SimpleFeature feature = features.next();
		            System.out.print(feature.getID());
		            System.out.print(": ");
		            System.out.println(feature.getDefaultGeometryProperty().getValue());
		        }
		    }
		}
		catch ( IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Insere no sistema de arquivos o shapeFile, provisoriamente
	 * 
	 * @param shapeFile
	 * @return
	 */
	private File readFile(ShapeFile shapeFile)
	{
		String pathFile = PATH_SHAPE_FILES_IMPORT + String.valueOf("Geocab_imported_" + new SimpleDateFormat("yyyy-mm-dd").format(Calendar.getInstance().getTime())) +"."+ shapeFile.getType().toString().toLowerCase();//".shp";

		Base64 decoder = new Base64();
		byte[] shpBytes = decoder.decode(shapeFile.getSource());
		FileOutputStream osf;
		try
		{
			osf = new FileOutputStream(new File(pathFile));
			osf.write(shpBytes);
			osf.flush();
			osf.close();
			return new File(pathFile);
		}
		catch (IOException e)
		{
			LOG.info(e.getMessage());
			throw new RuntimeException("Erro ao gravar arquivo de shapefile: " + e.getMessage());
		}
	}
	
	/**
	 * Agrupa as postagens pelas camadas
	 * @param markers
	 * @return
	 */
	private List<Layer> groupByLayers(final List<Marker> markers)
	{
		final Set<Layer> layers = new HashSet<>();
		
		for (final Marker marker : markers)
		{	
			layers.add(marker.getLayer());
		}
		
		for (final Layer layer : layers)
		{	
			for (final Marker marker : markers)
			{
				layer.setMarkers(new ArrayList<Marker>());
				if (marker.getLayer().getId() == layer.getId())
				{
					layer.getMarkers().add(marker);
				}
			}
		}
		return new ArrayList<Layer>(layers);
	}


	/**
	 * Servi�o de exporta��o para shapeFile
	 * 
	 * @param markers
	 * @return
	 */
	public FileTransfer exportShapeFile(final List<Marker> markers)
	{
		
		final List<Layer> layers = groupByLayers(markers);
		//TODO colocar segundos
		final String fileExport = String.valueOf("Geocab_exported_" + new SimpleDateFormat("yyyy-mm-dd").format(Calendar.getInstance().getTime()) );

		//Cria shapeFiles
		final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();		
		
		for (final Layer layer : layers){
			try
			{				
				layer.setName(layer.getName().replaceAll(" ", "_"));
//				location:Point:srid=4326,
				final SimpleFeatureType TYPE = DataUtilities.createType(layer.getName(), "location:Point:,"+layer.formattedAttributes());
				
//				final WKTReader2 wkt = new WKTReader2();
				
				final File newFile = new File(PATH_SHAPE_FILES_EXPORT + layer.getName() + ".shp");
				
				final ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

				final Map<String, Serializable> params = new HashMap<String, Serializable>();
		        params.put("url", newFile.toURI().toURL());
		        params.put("create spatial index", Boolean.TRUE);
		        
		        final ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		       
		        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);
				for (Marker marker : layer.getMarkers())
				{
					marker = markerRepository.findOne(marker.getId());											
					
	                Point point = factory.createPoint(marker.getLocation().getCoordinate()/*new Coordinate(marker.getLocation().getX(), latitude)*/);
	                SimpleFeature feature = SimpleFeatureBuilder.build(TYPE, new Object[]{point}, null);
		                
					featureCollection.add( feature /*SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT(" + marker.getLocation().getX() + " "+ marker.getLocation().getY() + ")")}, null)*/ );
				}
				
				newDataStore.createSchema(TYPE);
		        newDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
		        
		        Transaction transaction = new DefaultTransaction("create");
		        String typeName = newDataStore.getTypeNames()[0];
		        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore.getFeatureSource(typeName);
		        featureStore.setTransaction(transaction);
		        try {
		            featureStore.addFeatures(featureCollection);
		            transaction.commit();
		        } catch (Exception problem) {
		            problem.printStackTrace();
		            transaction.rollback();
		        } finally {
		            transaction.close();
		        }
			}
			catch (final RuntimeException | SchemaException /*| ParseException*/ | IOException e)
			{
				// Quando ocorre um erro os arquivos s�o removidos
				delete(new File(PATH_SHAPE_FILES_EXPORT));
				e.printStackTrace();
				LOG.info(e.getMessage());
			}
		}
		
		//Compcta os arquivos de exporta��o
		final FileTransfer fileTransfer = new FileTransfer(fileExport + ".zip", "application/zip", this.compactFilesToZip(PATH_SHAPE_FILES_EXPORT, fileExport + ".zip"));

		//Deleta os arquivos de exporta��o
		delete(new File(PATH_SHAPE_FILES_EXPORT));

		return fileTransfer;
	}
	/**
	 * Compacta os arquivos .shp, .dbf e shx para o formato zip e devolve um fileTransfer
	 * @param pathExport
	 * @return
	 */
	public FileInputStream compactFilesToZip(final String pathExport, final String fileExport)
	{
		try
		{
			final File file = new File(pathExport);

			byte[] buffer = new byte[10240];
			
			final FileOutputStream fileOutputStream = new FileOutputStream(pathExport + fileExport);

			final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			final List<ZipEntry> entries = new ArrayList<ZipEntry>();
			
			final List<FileInputStream> filesInputStream = new ArrayList<FileInputStream>();
			
			for (int i = 0; i < file.list().length; i++)
			{
				if (!file.list()[i].contains(".zip"))
				{
					entries.add(new ZipEntry(file.list()[i]));
					filesInputStream.add(new FileInputStream(pathExport +file.list()[i]));
				}
			}

			for (int i = 0; i < entries.size(); i++)
			{
				zipOutputStream.putNextEntry(entries.get(i));

				int len;
				while ((len = filesInputStream.get(i).read(buffer)) > 0)
				{
					zipOutputStream.write(buffer, 0, len);
				}

				filesInputStream.get(i).close();
			}

			zipOutputStream.closeEntry();

			zipOutputStream.close();
			
			return new FileInputStream(pathExport + fileExport);

		}
		catch (final IOException e)
		{
			LOG.info(e.getMessage());
			throw new RuntimeException("Ocorreu um erro durante a exporta��o: " + e.getMessage());
		}
	}
	
	/**
	 * Deleta uma pasta com todos os arquivos dentro
	 * @param file
	 */
	public static void delete(final File file)
	{
		if (file.isDirectory())
		{
			
			for (final String temp : file.list())
			{
				delete(new File(file, temp));
			}
		}
		else
		{
			file.delete();
		}
	}

}
