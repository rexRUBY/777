package com.example.order.config;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class CustomJsonDeserializer<T> implements Deserializer<T> {
    private final Gson gson = new Gson();
    private Class<T> targetType;

    // 기본 생성자 필요
    public CustomJsonDeserializer() {
    }

    // targetType을 설정할 수 있는 생성자
    public CustomJsonDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> configs, boolean isKey) {
        // targetType을 configs에서 가져옴
        if (configs.containsKey("targetType")) {
            this.targetType = (Class<T>) configs.get("targetType");
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        System.out.println("Received message from topic: " + topic);
        return gson.fromJson(new String(data), targetType);
    }

    @Override
    public void close() {
        // 리소스 정리 필요 시 작성
    }
}
