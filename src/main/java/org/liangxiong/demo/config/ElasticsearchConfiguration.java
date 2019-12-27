package org.liangxiong.demo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.liangxiong.demo.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author liangxiong
 * @Description 配置类, 注册RestHighLevelClient
 * @Date 2019-10-28
 * @Version 1.0.0
 */
@Configuration
public class ElasticsearchConfiguration implements FactoryBean<RestHighLevelClient> {

    private static final String COLON = ":";

    private static final String COMMA = ",";

    private static final String SCHEMA = "http";

    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    private RestHighLevelClient restHighLevelClient;

    private Logger logger = LoggerFactory.getLogger(ElasticsearchConfiguration.class);

    /**
     * 控制Bean的实例化过程
     *
     * @return
     */
    @Override
    public RestHighLevelClient getObject() {
        return restHighLevelClient;
    }

    /**
     * 获取接口返回的实例的class
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return RestHighLevelClient.class;
    }

    @PreDestroy
    public void destroy() {
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
        } catch (final Exception e) {
            logger.error("Error closing ElasticSearch client: ", e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @PostConstruct
    public void afterPropertiesSet() {
        restHighLevelClient = buildClientNew();
    }

    /**
     * 向后兼容老版本方式API
     *
     * @return
     */
    private RestHighLevelClient buildClientOld() {
        try {
            List<HttpHost> httpHosts = new ArrayList<>(3);
            for (String clusterNode : clusterNodes.split(COMMA)) {
                String hostName = StringUtil.substringBeforeLast(clusterNode, COLON);
                String port = StringUtil.substringAfterLast(clusterNode, COLON);
                HttpHost httpHost = new HttpHost(hostName, Integer.parseInt(port), SCHEMA);
                httpHosts.add(httpHost);
            }
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()])));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return restHighLevelClient;
    }

    /**
     * 官方推荐方式(spring-data-elasticsearch,since:3.2)
     *
     * @return
     */
    private RestHighLevelClient buildClientNew() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(StringUtils.delimitedListToStringArray(clusterNodes, COMMA))
                .withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(3))
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
