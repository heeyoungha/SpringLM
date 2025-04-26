package com.example.springlm.user.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GoogleReponse implements OAuth2Response{

    private final Map<String, Object> attribute;

    public GoogleReponse(Map<String, Object> attribute) {
        this.attribute = Objects.requireNonNullElse(attribute, new HashMap<>());
    }

    @Override
    public String getProvider() {

        return "google";
    }

    @Override
    public String getProviderId() {
        attribute.get("sub");
        return Optional.ofNullable(attribute.get("sub"))
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    public String getEmail() {
        attribute.get("email");
        return Optional.ofNullable(attribute.get("email"))
                .map(Object::toString)
                .orElse(null);
    }

    @Override
    public String getName() {
        attribute.get("name");
        return Optional.ofNullable(attribute.get("name"))
                .map(Object::toString)
                .orElse(null);
    }
}
