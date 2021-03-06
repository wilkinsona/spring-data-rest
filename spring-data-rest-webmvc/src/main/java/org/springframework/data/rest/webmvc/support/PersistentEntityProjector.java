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
package org.springframework.data.rest.webmvc.support;

import org.springframework.data.rest.core.projection.ProjectionDefinitions;
import org.springframework.data.rest.core.projection.ProjectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link Projector} looking up a projection by name for the given source type.
 * 
 * @author Oliver Gierke
 */
public class PersistentEntityProjector implements Projector {

	private final ProjectionDefinitions projectionDefinitions;
	private final ProjectionFactory factory;
	private final String projection;

	/**
	 * Creates a new {@link PersistentEntityProjector} using the given {@link ProjectionDefinitions},
	 * {@link ProjectionFactory} and projection name.
	 * 
	 * @param projectionDefinitions must not be {@literal null}.
	 * @param factory must not be {@literal null}.
	 * @param projection can be empty.
	 */
	public PersistentEntityProjector(ProjectionDefinitions projectionDefinitions, ProjectionFactory factory,
			String projection) {

		Assert.notNull(projectionDefinitions, "ProjectionDefinitions must not be null!");
		Assert.notNull(factory, "ProjectionFactory must not be null!");

		this.projectionDefinitions = projectionDefinitions;
		this.factory = factory;
		this.projection = projection;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.rest.webmvc.support.Projector#project(java.lang.Object)
	 */
	public Object project(Object source) {

		if (!StringUtils.hasText(projection)) {
			return source;
		}

		Class<?> projectionType = projectionDefinitions.getProjectionType(source.getClass(), projection);
		return factory.createProjection(source, projectionType);
	}
}
