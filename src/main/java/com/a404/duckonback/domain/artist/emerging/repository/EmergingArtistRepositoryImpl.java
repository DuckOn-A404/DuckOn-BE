package com.a404.duckonback.domain.artist.emerging.repository;

import java.util.*;
import com.a404.duckonback.common.enums.SortOrder;
import com.a404.duckonback.domain.artist.emerging.dto.EmergingArtistListResponseDTO;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtist;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistFollow;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistSort;
import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmergingArtistRepositoryImpl implements EmergingArtistRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<EmergingArtistListResponseDTO> pageEmergingArtists(
            Pageable pageable,
            EmergingArtistSort sort,
            SortOrder order,
            String keyword
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ===== content query =====
        CriteriaQuery<EmergingArtistListResponseDTO> cq = cb.createQuery(EmergingArtistListResponseDTO.class);
        Root<EmergingArtist> ea = cq.from(EmergingArtist.class);
        Join<EmergingArtist, EmergingArtistFollow> f = ea.join("followers", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(ea.get("status"), EmergingArtistStatus.ACTIVE));

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            predicates.add(cb.or(
                    cb.like(ea.get("nameKr"), like),
                    cb.like(ea.get("nameEn"), like)
            ));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // follower count
        Expression<Long> followerCnt = cb.count(f);

        // group by
        cq.groupBy(
                ea.get("emergingArtistId"),
                ea.get("createdAt"),
                ea.get("debutDate"),
                ea.get("nameKr"),
                ea.get("nameEn"),
                ea.get("imgUrl"),
                ea.get("status")
        );

        // select (DTO에 followerCount 필드/생성자 필요)
        cq.select(cb.construct(
                EmergingArtistListResponseDTO.class,
                ea.get("emergingArtistId"),
                ea.get("createdAt"),
                ea.get("debutDate"),
                ea.get("nameKr"),
                ea.get("nameEn"),
                ea.get("imgUrl"),
                ea.get("status"),
                followerCnt
        ));

        // sort path
        Expression<?> sortExpr = switch (sort) {
            case CREATED -> ea.get("createdAt");
            case NAME -> ea.get("nameKr");
            case DEBUT -> ea.get("debutDate");
            case FOLLOWERS -> followerCnt;
        };

        Order jpaOrder = (order == SortOrder.ASC) ? cb.asc(sortExpr) : cb.desc(sortExpr);
        cq.orderBy(jpaOrder, cb.desc(ea.get("createdAt")));

        TypedQuery<EmergingArtistListResponseDTO> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<EmergingArtistListResponseDTO> content = query.getResultList();

        // ===== count query =====
        // groupBy 때문에 count는 "distinct emergingArtistId"로.
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<EmergingArtist> ea2 = countCq.from(EmergingArtist.class);

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(ea2.get("status"), EmergingArtistStatus.ACTIVE));

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim() + "%";
            countPredicates.add(cb.or(
                    cb.like(ea2.get("nameKr"), like),
                    cb.like(ea2.get("nameEn"), like)
            ));
        }

        countCq.select(cb.countDistinct(ea2.get("emergingArtistId")))
                .where(countPredicates.toArray(new Predicate[0]));

        long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

}
