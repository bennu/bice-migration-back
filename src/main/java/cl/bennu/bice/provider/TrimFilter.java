package cl.bennu.bice.provider;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

@Provider
public class TrimFilter implements ContainerRequestFilter {

    @Context
    protected UriInfo info;

    @ConfigProperty(name = "log.curl.enabled", defaultValue = "false")
    protected boolean CURL_LOG;

    @ConfigProperty(name = "log.curl.header.authorization.enabled", defaultValue = "false")
    protected boolean CURL_HEADER_AUTHORIZATION_LOG;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // request ...
        final String method = requestContext.getMethod();
        String endpoint = info.getPath();
        MultivaluedMap<String, String> headers = requestContext.getHeaders();

        // arma curl de paticion
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("curl");
        stringBuilder.append(StringUtils.SPACE);
        stringBuilder.append("-X");
        stringBuilder.append(StringUtils.SPACE);
        stringBuilder.append(method);
        stringBuilder.append(StringUtils.SPACE);
        stringBuilder.append(endpoint);
        stringBuilder.append(StringUtils.SPACE);

        // para los métodos post, put patch y delete busca un body para "trimearlo" y agregarlo al string de log
        if (HttpMethod.GET.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.POST.equals(method) || HttpMethod.PATCH.equals(method) || HttpMethod.DELETE.equals(method)) {
            InputStream inputStream = requestContext.getEntityStream();

            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                if (StringUtils.isNoneBlank(body)) {
                    body = body.replaceAll(" *\" *", "\"").replace("\\n", StringUtils.EMPTY).replace("\\r", StringUtils.EMPTY).replace("\\t", StringUtils.EMPTY);
                    stringBuilder.append("-d");
                    stringBuilder.append(StringUtils.SPACE);
                    stringBuilder.append(body);
                    stringBuilder.append(StringUtils.SPACE);

                    requestContext.setEntityStream(IOUtils.toInputStream(body, Charset.defaultCharset()));
                }
            }
        }

        // itera las cabeceras y las agrega al string de log
        for (String header : headers.keySet()) {
            if (!CURL_HEADER_AUTHORIZATION_LOG && "Authorization".equalsIgnoreCase(StringUtils.trim(header))) {
                continue;
            }

            stringBuilder.append("-H");
            stringBuilder.append(StringUtils.SPACE);
            stringBuilder.append(header);
            stringBuilder.append("=");
            stringBuilder.append(String.join(",", headers.get(header)));
            stringBuilder.append(StringUtils.SPACE);
        }

        if (CURL_LOG) {
            System.out.println(stringBuilder);
        }
    }

}
