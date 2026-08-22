package com.project.hugme.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoMessageResponse(

        @JsonProperty("result_code")
        Integer resultCode
) {
}
