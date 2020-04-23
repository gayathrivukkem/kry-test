package se.kry.codetest.http;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import se.kry.codetest.database.Service;
import se.kry.codetest.dto.ServiceDTO;
import se.kry.codetest.util.ServiceConstants;

public class HttpServerVerticle extends AbstractVerticle {

    private Service dbService;

    @Override
    public void start(Promise<Void> promise) throws Exception {

        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress(ServiceConstants.DB_QUEUE);

        dbService = builder.build(Service.class);

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        setRoutes(router);

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(8080, result -> {
                if (result.succeeded()) {
                    System.out.println("KRY code test service started");
                    promise.complete();
                } else {
                    promise.fail(result.cause());
                }
            });

    }
    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/services").handler(this::getAll);
        router.route("/services*").handler(BodyHandler.create());
        router.post("/service").handler(this::createService);
        router.get("/service/:id").handler(this::getService);
        router.put("/service/:id").handler(this::updateService);
        router.delete("/service/:id").handler(this::deleteService);
    }

    private void getAll(RoutingContext routingContext) {
        dbService.getAll(reply -> {
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(reply.result()));
        });
    }

    private void getService(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            dbService.getServiceById(id, reply -> {
                if (reply.failed()) {
                    Future.failedFuture(reply.cause());
                    return;
                }
                routingContext.response().setStatusCode(201)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(reply.result()));
            });
        }

    }

    private void createService(RoutingContext routingContext) {
        ServiceDTO serviceDTO = Json.decodeValue(routingContext.getBodyAsString(),
                ServiceDTO.class);
        dbService.createService(serviceDTO, reply -> {
            if (reply.failed()) {
                Future.failedFuture(reply.cause());
                return;
            }
            updateServiceStatus(reply.result());
            routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(reply.result()));
        });
    }

    private void updateService(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        ServiceDTO serviceDTO = Json.decodeValue(routingContext.getBodyAsString(),
                ServiceDTO.class);
        if (id == null || serviceDTO == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            dbService.updateService(serviceDTO, reply -> {
                if (reply.failed()) {
                    Future.failedFuture(reply.cause());
                    return;
                }
                updateServiceStatus(reply.result());
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(reply.result()));
            });
        }
    }

    private void deleteService(RoutingContext routingContext) {
        final String id = routingContext.request().getParam("id");
        if (id == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            dbService.deleteService(id, reply -> {
                if (reply.failed()) {
                    Future.failedFuture(reply.cause());
                    routingContext.response().setStatusCode(204).end();
                    return;
                }
                routingContext.response().setStatusCode(204).end();
            });
        }

    }

    private void updateServiceStatus(ServiceDTO serviceDTO) {
        vertx.eventBus().send(ServiceConstants.POLL, "update services");
    }

}
