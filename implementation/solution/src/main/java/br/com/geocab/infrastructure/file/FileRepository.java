/**
 * 
 */
package br.com.geocab.infrastructure.file;

import java.io.File;

import org.springframework.stereotype.Component;

import br.com.geocab.domain.repository.IFileRepository;

/**
 * @author thiago
 *
 */
@Component
public class FileRepository implements IFileRepository
{
//	/**
//	 * 
//	 */
//	@Autowired(required=false)
//	private ServletContext servletContext;
	
	@Override
	public File[] listFilePath( String path )
	{
		final File iconsFolder = new File( path );
		File[] listFiles = iconsFolder.listFiles();
		return listFiles;
	}

}
