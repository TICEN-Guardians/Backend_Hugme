package com.project.hugme.global.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Converter(autoApply = false)
@Component
@RequiredArgsConstructor
public class AddressCryptoConverter
        implements AttributeConverter<String, String> {

    private final AddressCryptoService cryptoService;

    /*
     * Entity의 평문 주소를
     * DB에 저장할 암호문으로 변환한다.
     */
    @Override
    public String convertToDatabaseColumn(
            String attribute
    ) {
        return cryptoService.encrypt(attribute);
    }

    /*
     * DB 암호문을 Entity에서 사용할
     * 평문 주소로 변환한다.
     */
    @Override
    public String convertToEntityAttribute(
            String databaseValue
    ) {
        return cryptoService.decrypt(databaseValue);
    }
}