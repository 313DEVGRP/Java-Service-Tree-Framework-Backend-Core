package com.config;

import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = {"com.arms.globaltreemap.*"}
        ,entityManagerFactoryRef = "entityManagerJpaFactory"
        ,transactionManagerRef =  "transactionJpaManager"
)
@EnableTransactionManagement
@AllArgsConstructor
@EnableConfigurationProperties(HibernateProperties.class)
public class SpringDataConfig {

    private final JpaProperties jpaProperties;
    private final HibernateProperties hibernateProperties;

    @Autowired
    @Qualifier("onlyJpaDataSource")
    DataSource onlyJpaDataSource;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerJpaFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(onlyJpaDataSource);
        em.setPackagesToScan(new String[] { "com.arms.globaltreemap.**"});
        em.setJpaPropertyMap(jpaProperties.getProperties());
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }

    @Bean(name = "transactionJpaManager")
    public PlatformTransactionManager transactionJpaManager() {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerJpaFactory().getObject());
        return tm;
    }

    @Bean(name = "sessionJpaFactory")
    public LocalSessionFactoryBean sessionJpaFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(onlyJpaDataSource);
        sessionFactory.setPackagesToScan(new String[] { "com.arms.globaltreemap.**"});
        Map<String, Object> hibernateProps
                = hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());

        Properties props = new Properties();
        props.putAll(hibernateProps);

        sessionFactory.setHibernateProperties(props);
        return sessionFactory;
    }
}
