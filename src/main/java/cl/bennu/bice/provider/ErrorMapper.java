package cl.bennu.bice.provider;

import cl.bennu.commons.domain.base.Error;
import cl.bennu.commons.domain.base.StackTrace;
import cl.bennu.commons.exception.*;
import cl.bennu.commons.utils.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.ArrayList;
import java.util.List;


@Provider
public class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
        LogUtil.error(exception.getMessage(), exception);

        int code = 500;
        if (exception instanceof WebApplicationException) {
            code = ((WebApplicationException) exception).getResponse().getStatus();
        } else if (exception instanceof AuthorizationException) {
            code = 403;
        } else if (exception instanceof NoDataException
                || exception instanceof ForeignKeyException
                || exception instanceof UniqueException
                || exception instanceof AppException) {
            code = 409;
        }

        Error error = getError(exception, code);

        return Response.status(code).entity(error).build();
    }

    private Error getError(Exception exception, int code) {
        Error error = new Error();
        error.setType(exception.getClass().getName());
        error.setCode(code);

        if (exception.getMessage() != null) error.setError(exception.getMessage().replace("\\", "/"));
        if (exception.getStackTrace() != null) {
            List<StackTrace> stackTraces = getStackTraces(exception);
            error.setStackTraces(stackTraces);
        }
        return error;
    }

    private List<StackTrace> getStackTraces(Exception exception) {
        List<StackTrace> stackTraces = new ArrayList<>();
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {

            StackTrace stackTrace = new StackTrace();
            stackTrace.setClazz(stackTraceElement.getClassName());
            stackTrace.setMethod(stackTraceElement.getMethodName());
            stackTrace.setLine(stackTraceElement.getLineNumber());
            stackTrace.setError(stackTraceElement.toString());
            stackTraces.add(stackTrace);
        }
        return stackTraces;
    }

}
