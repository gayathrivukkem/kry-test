package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import se.kry.codetest.database.DatabaseVerticle;
import se.kry.codetest.poll.PollerVerticle;
import se.kry.codetest.util.ServiceConstants;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {

        vertx.deployVerticle(new DatabaseVerticle(), res -> {
            if (res.succeeded()) {
                vertx.deployVerticle("se.kry.codetest.http.HttpServerVerticle", result -> {
                    System.out.println("Deployment id is: " + result.result());
                    fut.complete();
                });
                vertx.deployVerticle("se.kry.codetest.poll.PollerVerticle");
            } else {
                System.out.println("Deployment failed!");
                fut.fail(res.cause());
            }
        });
        vertx.setPeriodic(1000 * 60, x -> {
            vertx.eventBus().send(ServiceConstants.POLL, "update services");
        });
    }

}
