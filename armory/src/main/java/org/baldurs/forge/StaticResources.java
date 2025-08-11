package org.baldurs.forge;

import io.quarkus.logging.Log;
import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.inject.Inject;

import org.baldurs.forge.services.LibraryService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class StaticResources {

    @Inject
    @ConfigProperty(name = "baldurs.forge.data.path", defaultValue = "/home/bburke/projects/baldurs-forge-data")
    private String rootPath;

    @Inject
    LibraryService library;

    
    @Route(path = "/static/img/*", methods = Route.HttpMethod.GET)
    public void images(RoutingContext rc) {
        //Log.info("STATIC RESOURCES" + rc.request().path());
        StaticHandler staticHandler = StaticHandler.create(FileSystemAccess.ROOT, rootPath + "/images").setCachingEnabled(false).setDirectoryListing(false);
        staticHandler.handle(rc);
    }
    @Route(path = "/static/mods/*", methods = Route.HttpMethod.GET)
    public void mods(RoutingContext rc) {
        //Log.info("STATIC RESOURCES" + rc.request().path());
        StaticHandler staticHandler = StaticHandler.create(FileSystemAccess.ROOT, library.modsPath().toString()).setCachingEnabled(false).setDirectoryListing(false);
        staticHandler.handle(rc);
    }
}
