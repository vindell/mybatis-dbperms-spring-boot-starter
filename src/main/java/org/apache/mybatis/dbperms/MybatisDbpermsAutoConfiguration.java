package org.apache.mybatis.dbperms;

import org.apache.ibatis.plugin.Plugin;
import org.apache.mybatis.dbperms.interceptor.DataPermissionStatementInterceptor;
import org.apache.mybatis.dbperms.parser.DefaultTablePermissionAnnotationHandler;
import org.apache.mybatis.dbperms.parser.DefaultTablePermissionAutowireHandler;
import org.apache.mybatis.dbperms.parser.ITablePermissionAnnotationHandler;
import org.apache.mybatis.dbperms.parser.ITablePermissionAutowireHandler;
import org.apache.mybatis.dbperms.parser.TablePermissionAnnotationParser;
import org.apache.mybatis.dbperms.parser.TablePermissionAutowireParser;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ Plugin.class, DataPermissionStatementInterceptor.class })
@EnableConfigurationProperties(MybatisDbpermsProperties.class)
public class MybatisDbpermsAutoConfiguration implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
    @Bean
    @ConditionalOnMissingBean
    public TablePermissionAutowireParser autowirePermissionParser() {
    	
    	TablePermissionAutowireParser autowirePermissionParser = new TablePermissionAutowireParser() {
    		
    		@Override
    		protected void internalInit() {
    			
    			super.internalInit();
    			
    			ITablePermissionAutowireHandler tablePermissionHandler = super.getTablePermissionHandler();
    			
    			if(null == tablePermissionHandler) {
    				tablePermissionHandler = getApplicationContext().getBean(ITablePermissionAutowireHandler.class);
    			}
    			if(null == tablePermissionHandler) {
    				tablePermissionHandler = new DefaultTablePermissionAutowireHandler((metaHandler, tableName) -> {
	            		return null;
	            	});
				}
    			
    			this.setTablePermissionHandler(tablePermissionHandler);
    		}
    		
    	};
    	 
    	return autowirePermissionParser;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ITablePermissionAnnotationHandler tablePermissionAnnotationHandler() {
    	return new DefaultTablePermissionAnnotationHandler();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public TablePermissionAnnotationParser annotationPermissionParser(ITablePermissionAnnotationHandler tablePermissionHandler) {
    	TablePermissionAnnotationParser annotationPermissionParser = new TablePermissionAnnotationParser();
    	annotationPermissionParser.setTablePermissionHandler(tablePermissionHandler);
    	return annotationPermissionParser;
    }
    
    @Bean
    public DataPermissionStatementInterceptor dataPermissionStatementInterceptor(
    		TablePermissionAutowireParser autowirePermissionParser,
    		TablePermissionAnnotationParser annotationPermissionParser) {
        return new DataPermissionStatementInterceptor(autowirePermissionParser, annotationPermissionParser);
    }

	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
}
