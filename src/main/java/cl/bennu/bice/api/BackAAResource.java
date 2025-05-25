package cl.bennu.bice.api;

import cl.bennu.bice.domain.Request;
import cl.bennu.bice.service.MigrationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.jboss.logging.Logger;

@Path("/v1/migration")
@Consumes(MediaType.APPLICATION_JSON)
public class BackAAResource {

    private final Logger LOGGER = Logger.getLogger(this.getClass());
    private @Inject MigrationService migrationService;

    @SneakyThrows
    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    public Response generate(Request request) {
        LOGGER.info("Solicitud de generación de script para back-aa recibida.");
        cl.bennu.bice.domain.Response response = migrationService.generate(request);
        return Response.ok(response).build();
    }

}
