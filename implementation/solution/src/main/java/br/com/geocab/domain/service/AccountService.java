package br.com.geocab.domain.service;

import java.util.logging.Logger;

import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import br.com.geocab.domain.entity.account.User;
import br.com.geocab.domain.repository.account.IUserRepository;

/**
 * 
 * @author Cristiano Correa 
 * @since 22/04/2014
 * @version 1.0
 * @category Service
 */
@Service
@Transactional
//@PreAuthorize("hasRole('"+UserRole.ADMINISTRADOR_VALUE+"')")
@RemoteProxy(name="accountService")
public class AccountService
{
	/*-------------------------------------------------------------------
	 *				 		     ATTRIBUTES
	 *-------------------------------------------------------------------*/
	
	/**
	 * Password encoder
	 */
	@Autowired
	private ShaPasswordEncoder passwordEncoder;
	
	/**
	 * Hash generator for encryption
	 */
	@Autowired
	private SaltSource saltSource;
	
	/**
	 * User Repository
	 */
	@Autowired
	private IUserRepository userRepository;
	
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger( AccountService.class.getName() );

	
	/*-------------------------------------------------------------------
	 *				 		     BEHAVIORS
	 *-------------------------------------------------------------------*/
	
	/**
	 * Insert a new User
	 * 
	 * @param user
	 * @return
	 */
	public User insertUser( User user )
	{
		Assert.notNull( user );
		
		user.setEnabled(true);
		//encrypt password
		final String encodedPassword = this.passwordEncoder.encodePassword( user.getPassword(), saltSource.getSalt( user ) ); 
		user.setPassword( encodedPassword );
		
		return this.userRepository.save( user );
	}
		
	/**
	 * List Users with pagination and filters
	 *
	 * @param filter
	 * @param pageable
	 * @return
	 */
	@Transactional(readOnly=true)
	public Page<User> listUsersByFilters( String filter, PageRequest pageable )
	{
		return this.userRepository.listByFilters(filter, pageable);
	}
	
	/**
	 * Find User by id
	 * 
	 * @param id
	 * @return User
	 */
	@Transactional(readOnly = true)
	public User findUserById( Long id )
	{
		return this.userRepository.findOne( id );
	}
	
	/**
	 * Find User by userName
	 * 
	 * @param userName
	 * @return User
	 */
	@Transactional(readOnly = true)
	public User findUserByEmail( String userName )
	{
		return this.userRepository.findByEmail( userName );
	}
	
	/**
	 * Disable User
	 * 
	 * @param id
	 * @return boolean 
	 */
	public Boolean disableUser( Long id )
	{
		User user = this.userRepository.findOne( id ); //Load user
		user.setEnabled(false); //Disable user
		this.userRepository.save( user ); //Save user
		
		return true;
	}
	
	/**
	 * Enable User
	 * 
	 * @param id
	 * @return boolean
	 */
	public Boolean enableUser( Long id )
	{	
		User user = this.userRepository.findOne( id ); //Load user
		user.setEnabled(true); //Enable user
		this.userRepository.save( user ); //Save user
		
		return true;
	}
	
	/**
	 * Update User
	 * 
	 * @param User
	 * @return User
	 */
	public User updateUser( User user )
	{			
		try{
			User dbUser = this.userRepository.findOne(user.getId());
			
			//Update database user
			dbUser.setEmail(user.getEmail());
			dbUser.setName(user.getName());
			dbUser.setRole(user.getRole());
			
			if( !user.getPassword().isEmpty() ){ //if set new password
				final String encodedPassword = this.passwordEncoder.encodePassword( user.getPassword(), saltSource.getSalt( dbUser ) ); 
				dbUser.setPassword( encodedPassword );
			}
						
			user = this.userRepository.save( dbUser );//save data in database
			
		}
		catch ( DataIntegrityViolationException e )
		{
			LOG.info( e.getMessage() );
			final String error = e.getCause().getCause().getMessage();
		}
		
		return user;
	}
}