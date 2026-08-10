package com.project.hugme.infra.ai.embedding;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BgeM3ModelLoader {
    private final OrtEnvironment environment;
    private final OrtSession session;

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

        System.out.println("BGE-M3 ONNX 모델 로드 성공");
    }

    public OrtEnvironment getEnvironment() {
        return environment;
    }

    public OrtSession getSesstion() {
        return session;
    }
}
