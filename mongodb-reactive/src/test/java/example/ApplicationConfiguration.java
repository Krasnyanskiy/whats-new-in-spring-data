/*
 * Copyright 2016 the original author or authors.
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
package example;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoDbFactory;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener;
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory;
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository;
import org.springframework.data.repository.query.DefaultEvaluationContextProvider;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;

/**
 * @author Mark Paluch
 */
@SpringBootApplication
public class ApplicationConfiguration implements BeanClassLoaderAware, BeanFactoryAware {

	@Value("${mongo.database:reactive}") private String database;

	private ClassLoader classLoader;
	private BeanFactory beanFactory;

	@Bean
	MongoClient mongoClient() {
		return MongoClients.create();
	}

	@Bean
	MongoDatabase mongoDatabase(MongoClient mongoClient) {
		return mongoClient.getDatabase(database);
	}

	@Bean
	ReactiveMongoDbFactory reactiveMongoDbFactory(MongoClient mongoClient) {
		return new ReactiveMongoDbFactory(mongoClient, database);
	}

	@Bean
	ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDbFactory mongoDbFactory) {
		return new ReactiveMongoTemplate(mongoDbFactory);
	}

	@Bean
	ReactiveMongoRepositoryFactory reactiveMongoRepositoryFactory(ReactiveMongoOperations reactiveMongoOperations) {

		ReactiveMongoRepositoryFactory factory = new ReactiveMongoRepositoryFactory(reactiveMongoOperations);
		factory.setRepositoryBaseClass(SimpleReactiveMongoRepository.class);
		factory.setBeanClassLoader(classLoader);
		factory.setBeanFactory(beanFactory);
		factory.setEvaluationContextProvider(DefaultEvaluationContextProvider.INSTANCE);

		return factory;
	}

	@Bean
	RxJavaPersonRepository rxJavaPersonRepository(ReactiveMongoRepositoryFactory factory) {
		return factory.getRepository(RxJavaPersonRepository.class);
	}

	@Bean
	ReactorPersonRepository reactivePersonRepository(ReactiveMongoRepositoryFactory factory) {
		return factory.getRepository(ReactorPersonRepository.class);
	}

	@Bean
	ReactivePersonRepository mixedReactivePersonRepository(ReactiveMongoRepositoryFactory factory) {
		return factory.getRepository(ReactivePersonRepository.class);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public @Bean LoggingEventListener mongoEventListener() {
		return new LoggingEventListener();
	}
}
