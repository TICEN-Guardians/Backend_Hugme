package com.project.hugme.domain.diagnosis.dto.internal;
import com.project.hugme.domain.diagnosis.dto.request.PropertyResolveRequest;

public record FastApiPropertyResolveRequest(String address,String dongName,String hoName) {

    public static FastApiPropertyResolveRequest from(PropertyResolveRequest request) {
        return new FastApiPropertyResolveRequest(
                request.address(),
                request.dongName(),
                request.hoName()
        );
    }
}