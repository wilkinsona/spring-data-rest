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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.Repositories;
import org.springframework.hateoas.alps.Alps;
import org.springframework.hateoas.alps.Descriptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Oliver Gierke
 */
@Controller
public class AlpsController {

	private final Repositories repositories;
	private final PersistentEntityToAlpsDescriptorConverter converter;

	public AlpsController(Repositories repositories, PersistentEntityToAlpsDescriptorConverter converter) {

		Assert.notNull(repositories, "Repositories must not be null!");
		Assert.notNull(converter, "PersistentEntityToAlpsDescriptorConverter must not be null!");

		this.repositories = repositories;
		this.converter = converter;
	}

	@RequestMapping("/alps")
	HttpEntity<Alps> foo() {

		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		for (Class<?> domainType : repositories) {
			PersistentEntity<?, ?> persistentEntity = repositories.getPersistentEntity(domainType);
			descriptors.addAll(converter.convert(persistentEntity).getDescriptors());
		}

		Alps alps = Alps.alps().//
				descriptors(descriptors).//
				build();
		return new ResponseEntity<Alps>(alps, HttpStatus.OK);
	}
}
