package se.kry.codetest.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.SQLClient;
import java.util.List;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import se.kry.codetest.dto.ServiceDTO;

@ProxyGen
@VertxGen
public interface Service {

    @GenIgnore
    static Service create(
            SQLClient dbClient,Handler<AsyncResult<Service>> readyHandler) {
        return new ServiceImpl(dbClient, readyHandler);
    }

    @Fluent
    Service getAll(Handler<AsyncResult<List<ServiceDTO>>> resultHandler);

    @Fluent
    Service getServiceById(String id, Handler<AsyncResult<ServiceDTO>> resultHandler);

    @Fluent
    Service createService(ServiceDTO serviceDTO, Handler<AsyncResult<ServiceDTO>> resultHandler);

    @Fluent
    Service updateService(ServiceDTO serviceDTO, Handler<AsyncResult<ServiceDTO>> resultHandler);

    @Fluent
    Service deleteService(String id, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    Service updateServiceStatus(ServiceDTO serviceDTO, Handler<AsyncResult<ServiceDTO>> resultHandler);


}
