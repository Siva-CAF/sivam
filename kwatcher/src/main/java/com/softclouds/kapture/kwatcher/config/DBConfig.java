package com.softclouds.kapture.kwatcher.config;

import javax.persistence.EntityManagerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/*@Configuration
@EnableJpaRepositories("com.softclouds.kapture.kwatcher.repository")
@EnableTransactionManagement*/
public class DBConfig {

		@Bean
		public PlatformTransactionManager transactionManager() {

			JpaTransactionManager txManager = new JpaTransactionManager();
			txManager.setEntityManagerFactory(entityManagerFactory());
			return txManager;
		}

		@Bean
		public HibernateExceptionTranslator hibernateExceptionTranslator() {
			return new HibernateExceptionTranslator();
		}

		@Bean
		public EntityManagerFactory entityManagerFactory() {

			// will set the provider to 'org.hibernate.ejb.HibernatePersistence'
			HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
			// will set hibernate.show_sql to 'true'
			vendorAdapter.setShowSql(true);
			// if set to true, will set hibernate.hbm2ddl.auto to 'update'
			vendorAdapter.setGenerateDdl(false);

			LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
			factory.setJpaVendorAdapter(vendorAdapter);
			//factory.setPackagesToScan("com.geowarin.standalonedatajpa.model");
			factory.setPackagesToScan("com.softclouds.kapture.kwatcher.bo");
			//factory.setDataSource(dataSource());

			// This will trigger the creation of the entity manager factory
			factory.afterPropertiesSet();

			return factory.getObject();
		}

}
