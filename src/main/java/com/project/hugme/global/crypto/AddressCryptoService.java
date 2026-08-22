package com.project.hugme.global.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AddressCryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
/*
AES       → 암호화 알고리즘
GCM       → AES 동작 모드 GCM은 암호화뿐 아니라 암호문이 변경됐는지도 확인해주는 방식
NoPadding → 별도 패딩을 적용하지 않음

 */
    private static final int AES_256_KEY_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    /*
     * 암호화 방식이나 키가 변경될 경우를 대비해
     * 암호문 앞에 버전을 붙인다.
     */
    private static final String VERSION_PREFIX = "v1:";

    private final SecretKey secretKey;
    private final SecureRandom secureRandom =
            new SecureRandom();

    public AddressCryptoService(
            @Value("${security.crypto.address-key}")
            String encodedKey
    ) {
        byte[] keyBytes;

        try {
            keyBytes =
                    Base64.getDecoder()
                            .decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "주소 암호화 키는 Base64 형식이어야 합니다.",
                    exception
            );
        }

        if (keyBytes.length != AES_256_KEY_BYTES) {
            throw new IllegalStateException(
                    "주소 암호화 키는 32바이트여야 합니다."
            );
        }

        //Java 암호화 API가 이해할 수 있는 SecretKey 객체로 변환
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String encrypt(String plainAddress) {

        if (plainAddress == null) {
            return null;
        }

        //새로운 iv 생성
        try {
            byte[] iv =
                    new byte[IV_LENGTH_BYTES];

            secureRandom.nextBytes(iv);

            //Cyper 객체
            //Java에서 실제 암호화와 복호화 계산을 수행하는 클래스입니다.
            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] encryptedBytes =
                    cipher.doFinal(
                            plainAddress.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            /*
             * 복호화할 때 같은 IV가 필요하므로 IV도 DB에 함께 저장해야 합니다.
             */
            ByteBuffer buffer =
                    ByteBuffer.allocate(
                            iv.length
                                    + encryptedBytes.length
                    );

            buffer.put(iv);
            buffer.put(encryptedBytes);

            String encoded =
                    Base64.getEncoder()
                            .encodeToString(
                                    buffer.array()
                            );

            return VERSION_PREFIX + encoded;

        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                    "주소 암호화에 실패했습니다.",
                    exception
            );
        }
    }

    //decrypt() 동작
    //복호화 과정은 암호화 과정의 역순입니다.
    public String decrypt(String encryptedAddress) {

        if (encryptedAddress == null) {
            return null;
        }

        /*
         * 기존 DB에 저장된 평문 주소를 임시로 지원한다.
         * 기존 데이터 암호화가 끝난 뒤에는 예외 처리로
         * 변경하는 것이 안전하다.
         */
        if (!encryptedAddress.startsWith(
                VERSION_PREFIX
        )) {
            return encryptedAddress;
        }

        try {
            String encoded =
                    encryptedAddress.substring(
                            VERSION_PREFIX.length()
                    );

            byte[] combined =
                    Base64.getDecoder()
                            .decode(encoded);

            if (combined.length
                    <= IV_LENGTH_BYTES) {

                throw new IllegalStateException(
                        "올바르지 않은 주소 암호문입니다."
                );
            }

            ByteBuffer buffer =
                    ByteBuffer.wrap(combined);

            byte[] iv =
                    new byte[IV_LENGTH_BYTES];

            buffer.get(iv);

            byte[] encryptedBytes =
                    new byte[buffer.remaining()];

            buffer.get(encryptedBytes);

            Cipher cipher =
                    Cipher.getInstance(
                            TRANSFORMATION
                    );

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            TAG_LENGTH_BITS,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] decryptedBytes =
                    cipher.doFinal(
                            encryptedBytes
                    );

            return new String(
                    decryptedBytes,
                    StandardCharsets.UTF_8
            );

        } catch (GeneralSecurityException
                 | IllegalArgumentException exception) {

            throw new IllegalStateException(
                    "주소 복호화에 실패했습니다.",
                    exception
            );
        }
    }
}