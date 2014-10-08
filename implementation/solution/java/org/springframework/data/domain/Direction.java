package org.springframework.data.domain;

import java.util.Locale;

/*
* Copyright 2008-2010 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Enumeration for sort directions.
 * 
 * @author Oliver Gierke
 */
public enum Direction
{

	ASC, DESC;

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static Direction fromString( String value )
	{
		try
		{
			return Direction.valueOf(value.toUpperCase(Locale.US));
		}
		catch ( Exception e )
		{
			throw new IllegalArgumentException( String.format( "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value), e);
		}
	}
}