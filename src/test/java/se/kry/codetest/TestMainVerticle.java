package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {
    private WebClient webClient;

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
      webClient = WebClient.create(vertx, new WebClientOptions()
              .setDefaultHost("::1")
              .setDefaultPort(8080));
  }

  @Test
  @DisplayName(" get services /services")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) {
      webClient
        .get("/services")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray body = response.result().bodyAsJsonArray();
          testContext.completeNow();
        }));
  }

  @Test
  @DisplayName("get service with id /service/id")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void getService(Vertx vertx, VertxTestContext testContext) {
      webClient.get("/service/0")
        .send(response -> testContext.verify(() -> {
          assertEquals(201, response.result().statusCode());
          testContext.completeNow();
        }));
  }

  @Test
  @DisplayName("Start a web server on localhost responding to create service")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void createService(Vertx vertx, VertxTestContext testContext) {
      webClient.post("/service")
        .sendJsonObject(new JsonObject()
        .put("name", "test")
        .put("url", "www.kry.se"),response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          assertEquals("test", response.result().bodyAsJsonObject().getString("name"));
          testContext.completeNow();
        }));
  }

  @Test
  @DisplayName("update service ")
  @Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
  void updateService(Vertx vertx, VertxTestContext testContext) {
      webClient.post("/service")
      .sendJsonObject(new JsonObject()
      .put("name", "test")
      .put("url", "www.kry.se"),response -> testContext.verify(() -> {
          String id = response.result().bodyAsJsonObject().getValue("id").toString();
          webClient.put("/service/"+id)
          .sendJsonObject(new JsonObject()
          .put("name", "updated")
          .put("url", "www.kry.se"),put -> testContext.verify(() -> {
            assertEquals(200, put.result().statusCode());
            assertEquals("updated", put.result().bodyAsJsonObject().getString("name"));
            testContext.completeNow();
          }));
      }));
  }

  @Test
  @DisplayName("delete service")
  @Timeout(value = 600, timeUnit = TimeUnit.SECONDS)
  void deleteService(Vertx vertx, VertxTestContext testContext) {
      webClient.post("/service")
        .sendJsonObject(new JsonObject()
          .put("name", "test")
          .put("url", "www.kry.se"),response -> testContext.verify(() -> {
              String id = response.result().bodyAsJsonObject().getValue("id").toString();
                webClient.delete("/service/"+id).send(delete -> testContext.verify(() -> {
                assertEquals(204, delete.result().statusCode());
                testContext.completeNow();
              }));
        }));
  }


}
