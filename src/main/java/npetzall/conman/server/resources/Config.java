package npetzall.conman.server.resources;

import npetzall.conman.server.api.Configuration;
import npetzall.conman.server.jdbi.ConfigurationDAO;
import org.skife.jdbi.v2.exceptions.StatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

@Path("/config")
public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private final ConfigurationDAO configurationDAO;

    public Config(ConfigurationDAO configurationDAO) {
        this.configurationDAO = configurationDAO;
    }

    @Path("/{service}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Configuration> getConfigurations(
            @PathParam("service") String service,
            @DefaultValue(Configuration.DEFAULT_ENV) @QueryParam("env") String env) {
        return configurationDAO.fetchAllForService(service, env);
    }

    @Path("/{service}/{key}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Configuration getConfiguration(
            @PathParam("service") String service,
            @PathParam("key") String key,
            @DefaultValue(Configuration.DEFAULT_ENV) @QueryParam("env") String env) {
        return configurationDAO.fetchConfigurationForService(service, key, env);
    }

    @Path("/{service}/{key}/value")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getConfigurationValue(
            @PathParam("service") String service,
            @PathParam("key") String key,
            @DefaultValue(Configuration.DEFAULT_ENV) @QueryParam("env") String env) {
        return configurationDAO.fetchConfigurationForService(service, key, env).getValue();
    }

    @Path("/{service}/{key}/value")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response createOrUpdateConfiguration(
            @PathParam("service") String service,
            @PathParam("key") String key,
            @DefaultValue(Configuration.DEFAULT_ENV) @QueryParam("env") String env,
            String value) {
        try {
            ConfigurationDAO.Event event = configurationDAO.createOrUpdate(service, key, env, value);
            if (event == ConfigurationDAO.Event.CREATED) {
                return Response.created(UriBuilder.fromPath("/config/{service}/{key}").queryParam("env", env).build(service, key)).build();
            } else if (event == ConfigurationDAO.Event.UPDATED || event == ConfigurationDAO.Event.UNMODIFIED) {
                return Response.ok().location(UriBuilder.fromPath("/config/{service}/{key}").queryParam("env", env).build(service, key)).build();
            } else {
                return Response.serverError().build();
            }
        } catch (StatementException e) {
            log.error("Failed to create or update [service: "+service+",key: "+key+", env: "+env+"]", e);
            return Response.serverError().build();
        }
    }
}
