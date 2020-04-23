package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import se.kry.codetest.database.DatabaseVerticle;
import se.kry.codetest.util.ServiceConstants;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> promise) {

        Promise<String> dbDeploymentPromise = Promise.promise();
        vertx.deployVerticle(new DatabaseVerticle(), dbDeploymentPromise);

        Future<String> deployHttpFuture = dbDeploymentPromise.future().compose(id -> {
            Promise<String> deployPromise = Promise.promise();
            vertx.deployVerticle("se.kry.codetest.http.HttpServerVerticle", deployPromise);
            return deployPromise.future();
        });

        vertx.deployVerticle("se.kry.codetest.poll.PollerVerticle", Promise.promise());
        deployHttpFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        vertx.setPeriodic(1000 * 60, x -> {
            vertx.eventBus().send(ServiceConstants.POLL, "update services");
        });
    }

}
