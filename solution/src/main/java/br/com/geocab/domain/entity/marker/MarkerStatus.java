/**
 * 
 */
package br.com.geocab.domain.entity.marker;

import org.directwebremoting.annotations.DataTransferObject;

/**
 * 
 * @author Thiago Rossetto Afonso
 * @since 30/09/2014
 * @version 1.0
 * 
 */
@DataTransferObject(type="enum", javascript = "MarkerStatus")
public enum MarkerStatus
{
	
	/*-------------------------------------------------------------------
	 *				 		     ENUMS
	 *-------------------------------------------------------------------*/
	//Do not change this order
	PENDING, ACCEPTED, REFUSED, SAVED, CANCELED;
//	YELLOW    GREEN    ORANGE    GRAY   RED
	/**
	 *
	 * @return value of enum
	 */
	public int getOrdinal()
	{
		return this.ordinal();
	}
}