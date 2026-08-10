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

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;

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
