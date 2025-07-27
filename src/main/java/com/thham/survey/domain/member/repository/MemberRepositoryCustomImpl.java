package com.thham.survey.domain.member.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thham.survey.domain.message.dto.AgeGroupMemberDto;
import com.thham.survey.domain.member.entity.QMember;
import com.thham.survey.domain.message.dto.QAgeGroupMemberDto;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

//    EXPLAIN
//    SELECT name, phone_number, birth_year
//    FROM member
//    WHERE birth_year BETWEEN 1986 AND 1995
//    LIMIT 0, 100;

//    {
//        "id": 1,
//            "select_type": "SIMPLE",
//            "table": "member",
//            "partitions": null,
//            "type": "index",
//            "possible_keys": "idx_birth_year",
//            "key": "idx_birth_year",
//            "key_len": "4",
//            "ref": null,
//            "rows": 1,
//            "filtered": 100.00,
//            "Extra": "Using where; Using index"
//    }

//    EXPLAIN
//    SELECT count(id)
//    FROM member WHERE birth_year BETWEEN 1986 AND 1995;

//    {
//        "id": 1,
//        "select_type": "SIMPLE",
//        "table": "member",
//        "partitions": null,
//        "type": "index",
//        "possible_keys": "idx_birth_year",
//        "key": "idx_birth_year",
//        "key_len": "4",
//        "ref": null,
//        "rows": 1,
//        "filtered": 100.00,
//        "Extra": "Using where; Using index"
//    }

    @Override
    public Page<AgeGroupMemberDto> findByAgeGroup(String ageGroup, int currentYear, Pageable pageable) {
        QMember member = QMember.member;

        BooleanExpression condition = switch (ageGroup) {
            case "0~10" -> member.birthYear.between(currentYear - 10, currentYear);
            case "20s" -> member.birthYear.between(currentYear - 29, currentYear - 20);
            case "30s" -> member.birthYear.between(currentYear - 39, currentYear - 30);
            case "40s" -> member.birthYear.between(currentYear - 49, currentYear - 40);
            case "50s" -> member.birthYear.between(currentYear - 59, currentYear - 50);
            case "60s" -> member.birthYear.between(currentYear - 69, currentYear - 60);
            case "70s+" -> member.birthYear.loe(currentYear - 70);
            default -> throw new IllegalArgumentException("Invalid age group: " + ageGroup);
        };

        List<AgeGroupMemberDto> content = queryFactory
                .select(new QAgeGroupMemberDto(
                        member.name,
                        member.phoneNumber,
                        member.birthYear))
                .from(member)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        log.debug("Retrieved {} records for age group [{}]", content.size(), ageGroup);

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(condition)
                .fetchOne();

        long totalCount = total != null ? total : 0L;

        return new PageImpl<>(content, pageable, totalCount);
    }
}