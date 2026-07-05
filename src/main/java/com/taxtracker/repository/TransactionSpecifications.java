package com.taxtracker.repository;

import com.taxtracker.entity.TransactionEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic JPA Specification for transaction filtering. The email
 * filter is always applied; financialYear, month, organizationName and type
 * are applied only when provided (non-blank). This lets the dashboard combine
 * any subset of filters without an explosion of repository methods.
 */
public final class TransactionSpecifications {

    private TransactionSpecifications() {
    }

    public static Specification<TransactionEntity> withFilters(
            String email, String financialYear, String month, String organizationName, String type) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("email"), email));

            if (financialYear != null && !financialYear.isBlank()) {
                predicates.add(cb.equal(root.get("financialYear"), financialYear));
            }
            if (month != null && !month.isBlank()) {
                predicates.add(cb.equal(root.get("month"), month));
            }
            if (organizationName != null && !organizationName.isBlank()) {
                predicates.add(cb.equal(root.get("organizationName"), organizationName));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
