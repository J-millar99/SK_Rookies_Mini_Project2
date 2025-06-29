-- src/main/resources/db/migration/V2__insert_initial_data.sql

INSERT INTO areas (area_name) VALUES
('서울특별시'),
('부산광역시'),
('대구광역시'),
('인천광역시'),
('광주광역시'),
('대전광역시'),
('울산광역시'),
('세종특별자치시'),
('경기도'),
('강원특별자치도'),
('충청북도'),
('충청남도'),
('전북특별자치도'),
('전라남도'),
('경상북도'),
('경상남도'),
('제주특별자치도');

INSERT INTO categories (category_name) VALUES
('여성의류'),
('남성의류'),
('신발'),
('가방/지갑'),
('시계'),
('쥬얼리'),
('패션 액세서리'),
('디지털'),
('가전제품'),
('스포츠/레저'),
('차량/오토바이'),
('스타굿즈'),
('키덜트'),
('예술/희귀/수집품'),
('음반/악기'),
('도서/티켓/문구'),
('뷰티/미용'),
('가구/인테리어'),
('생활/주방용품'),
('삽니다');

INSERT INTO cancelation_reasons (cancelation_reason_type, is_cancelation_reason_of_buyer) VALUES
('단순 변심', true),
('상품 정보 오류', true),
('판매자와 연락 불가', true),
('질병', true),
('사고 및 재난', true),
('도난/사기 물품 의심', true),
('기타 사유', true),
('단순 변심', false),
('상품 파손', false),
('질병', false),
('사고 및 재난', false),
('구매자와 연락 불가', false);

INSERT INTO report_reasons (report_reason_type) VALUES
('불쾌한 언어 사용'),
('광고/스팸'),
('부적절한 게시물'),
('거래 무단 파기'),
('기타 사유');
