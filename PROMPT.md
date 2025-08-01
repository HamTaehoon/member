-----------------------------------------------------------------

계정, 암호, 성명, 주민등록번호, 휴대전화 번호, 주소. 휴대전화 번호랑 주민등록번호는 11자리 등의 자릿수로, client 값을 전적으로 믿는 가정이야

이 정보들을 담을 Member 엔티티 객체를 만들어.

클라이언트에 보낼 MemberDto를 정의해. 비밀번호랑 주민등록번호는 JSON 응답에 포함하지 마

Mock을 사용해서 회원가입 테스트 코드를 작성해 줘.

-----------------------------------------------------------------

관리자 API를 만들 거야.

회원 리스트는 페이징이 돼야 해.

수정

삭제

이렇게 3가지 API가 필요해.

QueryDSL은 쓰지 마.

수정은 더티 체킹으로 진행해.

관리자 화면에서 수정하는 거니까, accountId 대신 id로 수정하게 해 줘.

-----------------------------------------------------------------

Basic Auth 처리를 위한 Spring Security Gradle 의존성을 추가하고, SecurityConfig를 만들어.

Swagger랑 회원가입 API는 권한 없이 처리되게 해야 하니까, Filter Chain을 나눠서 빈으로 등록해 줘.

JWT 토큰을 만들 거야. JwtFilter를 만들고, JWT 토큰 인증이 필요한 Filter Chain 빈에만 넣어 줘.

URL 패턴 /v1/api/members/** 하위는 모두 토큰 인증이 필요해.

Access Token과 Refresh Token을 생성해.

Access Token은 헤더에 담고, Refresh Token은 Secure 및 HttpOnly 쿠키로 구워 줘.

-----------------------------------------------------------------

회원 상세 조회 API를 만들 거야.

주소는 '서울특별시'처럼 행정구역만 반환해야 해. 회원가입 때 주소를 필드로 받았으니까, 클라이언트 데이터를 믿는다는 가정하에 공백으로 구분해서 앞에 오는 값을 행정구역으로 생각하고 반환하자

-----------------------------------------------------------------

회원을 연령층으로 구분해야 하는데, 주민등록번호밖에 판단 근거가 없어

Member 엔티티에 birthYear(출생연도) 컬럼을 추가하고 인덱스도 걸자

회원가입 로직에 출생연도 연산을 추가해서, 회원가입할 때 바로바로 넣어주도록 수정해.

연령층별 조회에 QueryDSL을 넣자

QueryDSL Gradle 의존성을 추가해 줘.

Custom 접미사를 붙여서 인터페이스랑 구현체를 만들어.

엔티티를 그대로 반환하지 말고, QueryDSL용 DTO를 만들어서 필요한 값만 Projection으로 선택하게 해.

인덱스 잘 타는지 EXPLAIN 찍어볼 수 있도록 MySQL 툴에 넣을 수 있게, 
Hibernate: select m1_0.name,m1_0.phone_number,m1_0.birth_year from member m1_0 where m1_0.birth_year between ? and ? limit ?,? 
이 쿼리에 값 바인딩해줘.

-----------------------------------------------------------------

3천만 명 규모면 API 수행이 오래 걸리니까 요청이 오면 바로 PENDING 같은 상태로 응답하고, 
진행 중에 IN_PROGRESS 같은 상태로 관리해야 할 것 같아

이를 관리할 MessageJobs 엔티티를 만들어. 생성일자, 갱신일자, 에러메시지 정도 포함하면 될 거야.

레이트 리밋을 걸어야 하니까 Bucket4j Gradle 의존성을 추가해 줘.

Bucket 빈을 등록하고, 카카오톡은 분당 100회, SMS는 분당 500회로 설정해

-----------------------------------------------------------------

README.md에 Gradle 파일을 참고해서 사용한 기술 스택의 버전들을 명시해 줘.