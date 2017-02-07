package br.com.geocab.infrastructure.mail;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.mail.internet.MimeMessage;

import br.com.geocab.domain.entity.MetaFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.*;
import org.apache.velocity.app.VelocityEngine;
import org.directwebremoting.io.FileTransfer;
import org.directwebremoting.io.OutputStreamLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.ui.velocity.VelocityEngineUtils;

import br.com.geocab.domain.entity.configuration.account.Email;
import br.com.geocab.domain.repository.IContactMailRepository;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author emanuelvictor
 *
 */
@Component
public class ContactMailRepository implements IContactMailRepository
{
	/*-------------------------------------------------------------------
	 * 		 					ATTRIBUTES
	 *-------------------------------------------------------------------*/
	/**
	 *
	 */
	@Autowired
	private JavaMailSender mailSender;
	/**
	 *
	 */
	@Autowired
	private VelocityEngine velocityEngine;
	/**
	 *
	 */
	@Value("${mail.support}")
	private String mailSupport;

	/*-------------------------------------------------------------------
	 * 		 					BEHAVIORS
	 *-------------------------------------------------------------------*/

	@Async
	public Future<Void> sendContactUs(final Email email)
	{
		final MimeMessagePreparator preparator = new MimeMessagePreparator()
		{
			public void prepare(MimeMessage mimeMessage) throws Exception
			{

				final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
				message.setSubject(email.getSubject());
				message.setTo(mailSupport);
				message.setFrom(email.getEmail());

				if (email.getAttachmentName() != null) {
					//ByteArrayOutputStream os = new ByteArrayOutputStream();
					//ImageIO.write(attachment, "jpg", os);
					//InputStream is = new ByteArrayInputStream(os.toByteArray());

					message.addAttachment(email.getAttachmentName(), new ByteArrayResource(email.getAttachment()));

				}

				
				final Map<String, Object> model = new HashMap<String, Object>();
				model.put("email", email);

				final String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,"mail-templates/contact-us.html", StandardCharsets.ISO_8859_1.toString(), model);
				message.setText(content, true);
			}
		};

		this.mailSender.send(preparator);

		return new AsyncResult<Void>(null);
	}

}