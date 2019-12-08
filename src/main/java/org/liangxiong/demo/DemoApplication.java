package org.liangxiong.demo;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;

/**
 * @author liangxiong
 * @date 2019-11-14 20:39
 * @description 注意:@EnableAdminServer会影响swagger-ui.html.导致404
 **/
@EnableSwagger2Doc
///@EnableAdminServer
@SpringBootApplication(exclude = {ElasticsearchAutoConfiguration.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
