package br.com.geocab.domain.repository;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.concurrent.Future;

import br.com.geocab.domain.entity.configuration.account.Email;
import org.directwebremoting.io.FileTransfer;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface para o envio de e-mails de contato
 * 
 * @author emanuelvictor
 *
 */
public interface IContactMailRepository
{
	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/
	/**
	 * 
	 * @param email
	 * @return
	 */
	public Future<Void> sendContactUs(final Email email);
}