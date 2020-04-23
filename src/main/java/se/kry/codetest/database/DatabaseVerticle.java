package se.kry.codetest.database;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.serviceproxy.ServiceBinder;
import se.kry.codetest.util.ServiceConstants;

public class DatabaseVerticle extends AbstractVerticle {

    private final String DB_PATH = "testDB.db";

    @Override
    public void start(Promise<Void> promise) throws Exception {

        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:" + DB_PATH)
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 30);

        SQLClient dbClient = JDBCClient.createShared(vertx, config);

        Service.create(dbClient, ready -> {
            if (ready.succeeded()) {
                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(ServiceConstants.DB_QUEUE).register(Service.class, ready.result());
                promise.complete();
            } else {
                promise.fail(ready.cause());
            }
        });
    }

}
