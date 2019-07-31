package com.steamyao.miaosha.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * @Package com.steamyao.miaosha.config
 * @date 2019/7/26 8:01
 * @description
 */
//当spring容器没有TomcatEmbeddServletContainerFactory时，会把这个bean加载进spring容器
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        //用工厂提供的接口来定制化 Tomcat connector
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

                //定制KeepAliveTimeout，设置30s内无请求则断开连接
                protocol.setKeepAliveTimeout(3000);
                //当客户端发送超过10000个请求时，则自动断开连接
                protocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
