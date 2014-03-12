package cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.server;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("/api/v1/*")
public class MyApplication extends ResourceConfig {

	public MyApplication() {
        setApplicationName("RESTful API for fdubbs");
        packages("cn.edu.fudan.ss.xulvcai.fdubbs.api.restful.resource");
        property(ServerProperties.TRACING, "ALL");
        property(ServerProperties.TRACING_THRESHOLD, "VERBOSE");
    }
}