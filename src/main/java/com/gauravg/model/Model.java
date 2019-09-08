
package com.gauravg.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Model {

    @JsonProperty("value")
    private Integer value;
    public Integer getValue() {
        return value;
    }
    public void setValue(Integer value) {
        this.value = value;
    }

    @JsonProperty("request")
    private Integer request;
    public Integer getRequest() {
        return request;
    }
    public void setRequest(Integer request) {
        this.request = request;
    }

    @JsonProperty("reply")
    private Integer reply;
    public Integer getReply() {
        return reply;
    }
    public void setReply(Integer reply) {
        this.reply = reply;
    }

    @JsonProperty("backend")
    private Integer backend;
    public Integer getBackend() {
        return backend;
    }
    public void setBackend(Integer backend) {
        this.backend = backend;
    }

    @JsonProperty("status")
    private Integer status;
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Model{" +
                "value=" + value +
                ", request=" + request +
                ", reply=" + reply +
                ", backend=" + backend +
                ", status=" + status +
                '}';
    }
}