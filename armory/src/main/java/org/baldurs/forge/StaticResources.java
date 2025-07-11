package org.baldurs.forge;

import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class StaticResources {

    @Inject
    @ConfigProperty(name = "baldurs.forge.data.path", defaultValue = "/home/bburke/projects/baldurs-forge-data")
    private String rootPath;

    
    @Route(path = "/static/img/*", methods = Route.HttpMethod.GET)
    public void images(RoutingContext rc) {
        //Log.info("STATIC RESOURCES" + rc.request().path());
        StaticHandler staticHandler = StaticHandler.create(FileSystemAccess.ROOT, rootPath + "/images").setCachingEnabled(false).setDirectoryListing(true);
        staticHandler.handle(rc);
    }
}
