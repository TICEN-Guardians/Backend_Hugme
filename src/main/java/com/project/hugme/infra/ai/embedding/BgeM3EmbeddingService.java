package com.project.hugme.infra.ai.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class BgeM3EmbeddingService {

    private final OrtEnvironment environment; // ONNX Runtime 실행 환경 객체
    private final OrtSession session; // BGE-M3 ONNX 모델을 실행하는 세션
    private final HuggingFaceTokenizer tokenizer; // 자연어 문자 토큰화

    public BgeM3EmbeddingService(
            BgeM3ModelLoader modelLoader
    ) throws Exception {

        this.environment = modelLoader.getEnvironment();
        this.session = modelLoader.getSession();

        this.tokenizer = HuggingFaceTokenizer.newInstance(
                Paths.get(
                        "src/main/resources/models/bge-m3/tokenizer.json"
                )
        );
    }

    // 입력 문장을 임베딩 벡터로 변환
    public float[] embed(String text) throws Exception {

        Encoding encoding = tokenizer.encode(text);

        long[] ids = encoding.getIds();
        long[] attentionMask = encoding.getAttentionMask();

        long[][] inputIds = new long[][] { ids };
        long[][] masks = new long[][] { attentionMask };

        try (
                OnnxTensor inputIdsTensor =
                        OnnxTensor.createTensor(environment, inputIds);
                OnnxTensor attentionMaskTensor =
                        OnnxTensor.createTensor(environment, masks)

        ) {
            Map<String, OnnxTensor> inputs = new HashMap<>();

            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            try (OrtSession.Result result = session.run(inputs)) {
                float[][][] lastHiddenState =
                        (float[][][]) result
                                .get("last_hidden_state")
                                .orElseThrow()
                                .getValue();

                float[] embedding = lastHiddenState[0][0];

                return normalize(embedding);
            }

        }
    }

    // 정규화 (벡터의 길이를 1로 맞춰주는 과정)
    private float[] normalize(float[] vector) {
        double sum = 0.0;

        for (float value : vector) {
            sum += value * value;
        }

        double norm = Math.sqrt(sum);

        float[] normalized = new float[vector.length];

        for (int i = 0; i < vector.length; i++) {
            normalized[i] = (float) (vector[i] / norm);
        }

        return normalized;
    }
}
