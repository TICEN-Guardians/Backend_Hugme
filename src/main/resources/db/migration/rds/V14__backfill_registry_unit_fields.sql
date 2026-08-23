UPDATE registry_results
SET dong_name = COALESCE(
        dong_name, substring(raw_address FROM '제\s*([가-힣A-Za-z0-9]+)\s*동')
    ),
    floor = COALESCE(
        floor, substring(raw_address FROM '제\s*([0-9]+)\s*층')::INTEGER
    ),
    ho_name = COALESCE(
        ho_name, substring(raw_address FROM '제\s*([A-Za-z0-9]+)\s*호')
    )
