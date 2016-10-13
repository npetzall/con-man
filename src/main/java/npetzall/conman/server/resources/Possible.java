package npetzall.conman.server.resources;

import npetzall.conman.server.api.PossibleConfiguration;
import npetzall.conman.server.api.PossibleConfigurationData;
import npetzall.conman.server.jdbi.PossibleConfigurationDAO;
import org.skife.jdbi.v2.exceptions.StatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

@Path("/possible")
public class Possible {

    private static final Logger log = LoggerFactory.getLogger(Possible.class);

    private final PossibleConfigurationDAO possibleConfigurationDAO;

    public Possible(PossibleConfigurationDAO possibleConfigurationDAO) {
        this.possibleConfigurationDAO = possibleConfigurationDAO;
    }

    @Path("/{service}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PossibleConfiguration> getAllPossibleConfigurationsForService(
            @PathParam("service") String service) {
        return possibleConfigurationDAO.fetchAllForService(service);
    }

    @Path("/{service}/{key}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PossibleConfiguration getPossibleConfigurationForService(
            @PathParam("service") String service,
            @PathParam("key") String key) {
        return possibleConfigurationDAO.fetchPossibleConfigurationForService(service, key);
    }

    @Path("/{service}/{key}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdatePossibleConfiguration(
            @PathParam("service") String service,
            @PathParam("key") String key,
            PossibleConfigurationData possibleConfigurationData) {
        try {
            PossibleConfigurationDAO.Event event = possibleConfigurationDAO.createOrUpdate(
                    service,
                    key,
                    possibleConfigurationData.getDescription(),
                    possibleConfigurationData.getValueRestriction(),
                    possibleConfigurationData.getValueRestrictionType());
            if (event == PossibleConfigurationDAO.Event.CREATED) {
                return Response.created(UriBuilder.fromPath("/possible/{service}/{key}").build(service, key)).build();
            } else if (event == PossibleConfigurationDAO.Event.UPDATED || event == PossibleConfigurationDAO.Event.UNMODIFIED) {
                return Response.ok().location(UriBuilder.fromPath("/possible/{service}/{key}").build(service, key)).build();
            } else {
                return Response.serverError().build();
            }
        } catch (StatementException e) {
            log.error("Failed to create or update " + identifier(service, key), e);
            return Response.serverError().build();
        }
    }

    @Path("/{service}/{key}/description")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateDescription(
            @PathParam("service") String service,
            @PathParam("key") String key,
            String description) {
        try {
            if (possibleConfigurationDAO.exists(service, key)) {
                possibleConfigurationDAO.updateDescription(service, key, description);
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (StatementException se) {
            log.error("Failed to update description for " + identifier(service, key), se);
            return Response.serverError().build();
        }
    }

    @Path("/{service}/{key}/valueRestriction")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateValueRestriction(
            @PathParam("service") String service,
            @PathParam("key") String key,
            String valueRestriction) {
        try {
            if (possibleConfigurationDAO.exists(service, key)) {
                possibleConfigurationDAO.updateValueRestriction(service, key, valueRestriction);
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (StatementException se) {
            log.error("Failed to update valueRestriction for " + identifier(service, key), se);
            return Response.serverError().build();
        }
    }

    @Path("/{service}/{key}/valueRestrictionType")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updateValueRestrictionType(
            @PathParam("service") String service,
            @PathParam("key") String key,
            String valueRestrictionType) {
        try {
            if (possibleConfigurationDAO.exists(service, key)) {
                possibleConfigurationDAO.updateValueRestrictionType(service, key, valueRestrictionType);
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (StatementException se) {
            log.error("Failed to update valueRestrictionType for " + identifier(service, key), se);
            return Response.serverError().build();
        }
    }

    private static final String identifier(String service, String key) {
        return String.format("[service: %s, key: %s]", service, key);
    }
}
