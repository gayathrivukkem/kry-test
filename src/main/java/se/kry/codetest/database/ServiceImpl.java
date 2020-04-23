package se.kry.codetest.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import se.kry.codetest.dto.ServiceDTO;

public class ServiceImpl implements Service {
    private final SQLClient dbClient;

    ServiceImpl(SQLClient dbClient, Handler<AsyncResult<Service>> readyHandler) {
        this.dbClient = dbClient;

        dbClient.getConnection(ar -> {
            if (ar.failed()) {
                readyHandler.handle(Future.failedFuture(ar.cause()));
            } else {
                SQLConnection connection = ar.result();
                String sql = "CREATE TABLE IF NOT EXISTS service (id INTEGER IDENTITY, name varchar(100) NOT NULL, url varchar(128) NOT NULL, status varchar(100))";
                connection.execute(sql, create -> {
                    if (create.failed()) {
                        readyHandler.handle(Future.failedFuture(create.cause()));
                        connection.close();
                    } else {
                        connection.query("SELECT * FROM service", select -> {
                            if (select.failed()) {
                                readyHandler.handle(Future.failedFuture(select.cause()));
                                connection.close();
                                return;
                            }
                            if (select.result().getNumRows() == 0) {
                                createService(
                                        new ServiceDTO("kry", "www.kry.se", "UNKNOWN"),
                                        (v) -> {
                                            readyHandler.handle(Future.succeededFuture(this));
                                            connection.close();
                                        });
                            } else {
                                readyHandler.handle(Future.succeededFuture(this));
                                connection.close();
                            }
                        });
                    }
                    ;
                });
            }
        });
    }

    @Override
    public Service getAll(Handler<AsyncResult<List<ServiceDTO>>> resultHandler) {
        dbClient.query("SELECT * FROM service", res -> {
            if (res.succeeded()) {
                List<ServiceDTO> serviceDTOS = res.result().getRows().stream().map(
                        ServiceDTO::new).collect(Collectors.toList());
                resultHandler.handle(Future.succeededFuture(serviceDTOS));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public Service getServiceById(String id, Handler<AsyncResult<ServiceDTO>> resultHandler) {
        String sql = "SELECT * FROM service WHERE id = ?";
        JsonArray params = new JsonArray().add(id);
        dbClient.queryWithParams(sql, params, result -> {
            List<ServiceDTO> serviceDTOS = result.result().getRows().stream().map(
                    ServiceDTO::new).collect(Collectors.toList());
            if (serviceDTOS.size() == 0) {
                resultHandler.handle(Future.failedFuture(new NoSuchElementException("Unknown service " + id)));
            } else {
                resultHandler.handle(Future.succeededFuture(serviceDTOS.get(0)));
            }
        });
        return this;
    }

    @Override
    public Service createService(ServiceDTO serviceDTO, Handler<AsyncResult<ServiceDTO>> resultHandler) {
        String sql = "INSERT INTO service (id, name, url, status) VALUES  (?, ?, ?, ?)";
        JsonArray params = new JsonArray()
                .add(serviceDTO.getId())
                .add(serviceDTO.getName())
                .add(serviceDTO.getUrl())
                .add(serviceDTO.getStatus());
        dbClient.updateWithParams(sql, params, (ar) -> {
            if (ar.failed()) {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
            UpdateResult result = ar.result();
            ServiceDTO s = new ServiceDTO(result.getKeys().getInteger(0), serviceDTO.getName(), serviceDTO.getUrl(), serviceDTO
                    .getStatus());
            resultHandler.handle(Future.succeededFuture(s));
        });
        return this;
    }

    @Override
    public Service updateService(ServiceDTO serviceDTO, Handler<AsyncResult<ServiceDTO>> resultHandler) {
        String sql = "UPDATE service SET name = ?, url = ? WHERE id = ?";
        JsonArray params = new JsonArray()
                .add(serviceDTO.getName())
                .add(serviceDTO.getUrl())
                .add(serviceDTO.getId());
        dbClient.updateWithParams(sql, params, (ar) -> {
            if (ar.failed()) {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
            UpdateResult result = ar.result();
            ServiceDTO s = new ServiceDTO(result.getKeys().getInteger(0), serviceDTO.getName(), serviceDTO.getUrl(), serviceDTO
                    .getStatus());
            resultHandler.handle(Future.succeededFuture(s));
        });
        return this;
    }

    @Override
    public Service updateServiceStatus(ServiceDTO serviceDTO, Handler<AsyncResult<ServiceDTO>> resultHandler) {
        String sql = "UPDATE service SET status = ? WHERE id = ?";
        JsonArray params = new JsonArray()
                .add(serviceDTO.getStatus())
                .add(serviceDTO.getId());
        dbClient.updateWithParams(sql, params, (ar) -> {
            if (ar.failed()) {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
            UpdateResult result = ar.result();
            ServiceDTO s = new ServiceDTO(result.getKeys().getInteger(0), serviceDTO.getName(), serviceDTO.getUrl(), serviceDTO
                    .getStatus());
            resultHandler.handle(Future.succeededFuture(s));
        });
        return this;
    }


    @Override
    public Service deleteService(String id, Handler<AsyncResult<Void>> resultHandler) {
        String sql = "DELETE FROM service WHERE id = ?";
        JsonArray params = new JsonArray().add(id);
        dbClient.updateWithParams(sql, params, (ar) -> {
            if (ar.failed()) {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
            if (ar.result().getUpdated() == 0) {
                resultHandler.handle(Future.failedFuture(new NoSuchElementException("Unknown service " + id)));
            } else {
                resultHandler.handle(Future.succeededFuture());
            }
        });
        return this;
    }
}
