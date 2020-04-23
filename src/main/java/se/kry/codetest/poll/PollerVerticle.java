package se.kry.codetest.poll;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import java.util.List;
import se.kry.codetest.database.Service;
import se.kry.codetest.dto.ServiceDTO;
import se.kry.codetest.util.ServiceConstants;

public class PollerVerticle extends AbstractVerticle {
    private Service dbService;
    WebClient webClient;
    @Override
    public void start(Future<Void> fut) throws Exception {

        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(ServiceConstants.DB_QUEUE);

        dbService = builder.build(Service.class);

        webClient = WebClient.create(vertx);

        vertx.eventBus().consumer(ServiceConstants.POLL, (message) -> {
            dbService.getAll(reply -> {
                List<ServiceDTO> serviceDTOS = reply.result();
                for(ServiceDTO serviceDTO : serviceDTOS) {
                    updateStatus(serviceDTO);
                }
            });

        });
    }

    private ServiceDTO updateStatus(ServiceDTO serviceDTO) {
        webClient
            .get(80, serviceDTO.getUrl(),"")
            .send(r -> {
                if (r.succeeded()) {
                    HttpResponse response = r.result();
                    serviceDTO.setStatus("OK");
                } else {
                    serviceDTO.setStatus("FAIL");
                }
                dbService.updateServiceStatus(serviceDTO, reply -> {
                    if (reply.failed()) {
                        Future.failedFuture(reply.cause());
                        return;
                    }
                });
            });

        return serviceDTO;
    }

}
