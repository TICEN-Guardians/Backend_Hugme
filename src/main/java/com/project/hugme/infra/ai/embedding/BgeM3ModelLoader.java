package com.project.hugme.infra.ai.embedding;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BgeM3ModelLoader {
    private final OrtEnvironment environment; // ONNX Runtime을 실행하기 위한 기본 실행 환경 객체
    private final OrtSession session; // ONNX Runtime에서 ONNX 모델을 메모리에 로드하고 추론을 실행하는 객체

    public BgeM3ModelLoader(
            @Value("${hugme.embedding.model-path}") String modelPath
    ) throws Exception {
        this.environment = OrtEnvironment.getEnvironment();

        OrtSession.SessionOptions sessionOptions =
                new OrtSession.SessionOptions();

        this.session = environment.createSession(
                modelPath,
                sessionOptions
        );

        log.info("BGE-M3 ONNX 모델 로드 성공");
    }

    public OrtEnvironment getEnvironment() {
        return environment;
    }

    public OrtSession getSession() {
        return session;
    }
}
