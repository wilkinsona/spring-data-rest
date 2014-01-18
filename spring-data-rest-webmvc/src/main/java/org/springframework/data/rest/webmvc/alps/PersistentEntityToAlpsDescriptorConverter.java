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

import static org.springframework.hateoas.alps.Alps.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.mapping.MethodResourceMapping;
import org.springframework.data.rest.core.mapping.ResourceDescription;
import org.springframework.data.rest.core.mapping.ResourceMapping;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.mapping.SimpleResourceDescription;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.alps.Alps;
import org.springframework.hateoas.alps.Descriptor;
import org.springframework.hateoas.alps.Descriptor.DescriptorBuilder;
import org.springframework.hateoas.alps.Doc;
import org.springframework.hateoas.alps.Format;
import org.springframework.hateoas.alps.Type;

/**
 * @author Oliver Gierke
 */
public class PersistentEntityToAlpsDescriptorConverter implements Converter<PersistentEntity<?, ?>, Alps> {

	private final Repositories repositories;
	private final ResourceMappings mappings;
	private final EntityLinks entityLinks;
	private final MessageSourceAccessor messageSource;

	/**
	 * @param mappings
	 */
	public PersistentEntityToAlpsDescriptorConverter(ResourceMappings mappings, Repositories repositories,
			EntityLinks entityLinks, MessageSourceAccessor messageSource) {

		this.mappings = mappings;
		this.repositories = repositories;
		this.entityLinks = entityLinks;
		this.messageSource = messageSource;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public Alps convert(PersistentEntity<?, ?> source) {

		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		descriptors.add(buildCollectionResourceDescriptor(source.getType()));
		descriptors.add(buildSingleResourceDescriptor(source));
		descriptors.addAll(buildSearchResourceDescriptors(source));

		return Alps.alps().descriptors(descriptors).build();
	}

	private Descriptor buildCollectionResourceDescriptor(Class<?> type) {

		ResourceMetadata metadata = mappings.getMappingFor(type);
		RepositoryInformation information = repositories.getRepositoryInformationFor(type);

		// Add collection rel
		DescriptorBuilder descriptorBuilder = getSafeDescriptorBuilder(metadata.getRel(), metadata.getDescription());

		if (information.isPagingRepository()) {

			Link linkToCollectionResource = entityLinks.linkToCollectionResource(type);
			List<Descriptor> paginationDescriptors = new ArrayList<Descriptor>();

			for (TemplateVariable variable : linkToCollectionResource.getVariables()) {

				ResourceDescription description = SimpleResourceDescription.defaultFor(variable.getDescription());
				paginationDescriptors.add(getSemanticDescriptorBuilder(variable.getName(), description).build());
			}

			descriptorBuilder.descriptors(paginationDescriptors);
		}

		return descriptorBuilder.build();
	}

	private Descriptor buildSingleResourceDescriptor(PersistentEntity<?, ?> source) {

		final ResourceMetadata metadata = mappings.getMappingFor(source.getType());
		final List<Descriptor> propertyDescriptors = new ArrayList<Descriptor>();

		source.doWithProperties(new SimplePropertyHandler() {

			@Override
			public void doWithPersistentProperty(PersistentProperty<?> property) {

				ResourceDescription description = metadata.getMappingFor(property).getDescription();
				DescriptorBuilder builder = getSemanticDescriptorBuilder(property.getName(), description);

				propertyDescriptors.add(builder.build());
			}
		});

		source.doWithAssociations(new SimpleAssociationHandler() {

			@Override
			public void doWithAssociation(Association<? extends PersistentProperty<?>> association) {

				PersistentProperty<?> property = association.getInverse();
				ResourceMapping mapping = metadata.getMappingFor(property);

				propertyDescriptors.add(getSafeDescriptorBuilder(mapping.getRel(), mapping.getDescription()).build());
			}
		});

		return getSafeDescriptorBuilder(metadata.getItemResourceRel(), metadata.getDescription()).//
				descriptors(propertyDescriptors).//
				build();
	}

	private Collection<Descriptor> buildSearchResourceDescriptors(PersistentEntity<?, ?> entity) {

		ResourceMetadata metadata = mappings.getMappingFor(entity.getType());
		List<Descriptor> descriptors = new ArrayList<Descriptor>();

		for (MethodResourceMapping methodMapping : metadata.getSearchResourceMappings()) {

			List<Descriptor> parameterDescriptors = new ArrayList<Descriptor>();

			for (String parameterName : methodMapping.getParameterNames()) {
				parameterDescriptors.add(descriptor().name(parameterName).type(Type.SEMANTIC).build());
			}

			descriptors.add(descriptor().//
					type(Type.SAFE).//
					name(methodMapping.getRel()).//
					descriptors(parameterDescriptors).//
					build());
		}

		return descriptors;
	}

	private DescriptorBuilder getSafeDescriptorBuilder(String name, ResourceDescription description) {

		return descriptor().//
				name(name).//
				type(Type.SAFE).//
				doc(getDocFor(description));
	}

	private DescriptorBuilder getSemanticDescriptorBuilder(String name, ResourceDescription description) {

		return descriptor().//
				name(name).//
				type(Type.SEMANTIC).//
				doc(getDocFor(description));
	}

	private Doc getDocFor(ResourceDescription description) {
		return new Doc(messageSource.getMessage(description), Format.TEXT);
	}
}
