package com.project.hugme.domain.chatbot.document.repository;

import com.project.hugme.domain.chatbot.document.dto.DocumentSearchData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DocumentSearchDataRepository {

    @Qualifier("mainJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    public List<DocumentSearchData> findAll() {
        String sql = """
                SELECT
                    d.document_id,
                    d.document_name,
                    dg_group.group_name AS document_group_name,
                    d.description,
                    d.issuer,

                    dg.preparation_method,
                    dg.online_availability,
                    dg.online_url,
                    dg.offline_availability,
                    dg.offline_location,
                    dg.required_documents,
                    dg.applicant_eligibility,
                    dg.fee,
                    dg.processing_time,
                    dg.notes,
                    dg.contact_info,
                    dg.official_guide_url,
                    dg.hug_reference_urls,
                    dg.verified_at

                FROM documents d

                LEFT JOIN document_groups dg_group
                    ON d.document_group_id = dg_group.document_group_id

                LEFT JOIN document_guides dg
                    ON d.document_id = dg.document_id

                ORDER BY d.document_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRow(rs)
        );
    }

    private DocumentSearchData mapRow(ResultSet rs) throws SQLException {

        return new DocumentSearchData(
                rs.getLong("document_id"),
                rs.getString("document_name"),
                rs.getString("document_group_name"),
                rs.getString("description"),
                rs.getString("issuer"),
                rs.getString("preparation_method"),
                rs.getString("online_availability"),
                rs.getString("online_url"),
                rs.getString("offline_availability"),
                rs.getString("offline_location"),
                rs.getString("required_documents"),
                rs.getString("applicant_eligibility"),
                rs.getString("fee"),
                rs.getString("processing_time"),
                toStringList(rs.getArray("notes")),
                rs.getString("contact_info"),
                rs.getString("official_guide_url"),
                toStringList(rs.getArray("hug_reference_urls")),
                rs.getObject("verified_at", java.time.LocalDate.class)
        );
    }

    private List<String> toStringList(Array sqlArray) throws SQLException {

        if (sqlArray == null) {
            return Collections.emptyList();
        }

        String[] values = (String[]) sqlArray.getArray();

        return Arrays.asList(values);
    }
}
