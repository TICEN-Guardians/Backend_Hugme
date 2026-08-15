package com.project.hugme.domain.diagnosis.dto.internal;

public record FastApiPropertyResolveRequest(String address) {

    public static FastApiPropertyResolveRequest from(String address) {
        return new FastApiPropertyResolveRequest(address);
    }
}