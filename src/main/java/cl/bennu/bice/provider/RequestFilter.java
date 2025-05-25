package cl.bennu.bice.provider;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // request ...
        // TODO: la integración con casbin se podria hacer a nivel de filtro, pero hay que unificar las rutas del gateway con la map
        String method = requestContext.getMethod();
        String endpoint = requestContext.getUriInfo().getPath();

    }

}
