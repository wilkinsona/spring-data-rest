/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.data.rest.webmvc.alps;

import java.util.Arrays;

import org.springframework.hateoas.alps.Alps;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Oliver Gierke
 */
public class AlpsJsonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

	/**
	 * 
	 */
	public AlpsJsonHttpMessageConverter() {

		setSupportedMediaTypes(Arrays.asList(MediaType.parseMediaType("application/alps+json"), MediaType.ALL));

		ObjectMapper mapper = getObjectMapper();
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		setPrettyPrint(true);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.MappingJackson2HttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return clazz.isAssignableFrom(Alps.class) && super.canRead(mediaType);
	}
}
