package se.kry.codetest.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.atomic.AtomicInteger;

@DataObject(generateConverter = true)
public class ServiceDTO {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final int id;

    private String name;

    private String url;

    private String status = "UNKNOWN";

    public ServiceDTO(String name, String url, String status) {
        this.id = COUNTER.getAndIncrement();
        this.name = name;
        this.url = url;
        this.status = null == status ? "UNKNOWN" :status;
    }

    public ServiceDTO(int id, String name, String url, String status) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.status = status;
    }

    public ServiceDTO(JsonObject json) {
        this.status = json.getString("status");
        this.name = json.getString("name");
        this.url = json.getString("url");
        this.id = json.getInteger("id");
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public ServiceDTO() {
        this.id = COUNTER.getAndIncrement();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
