package uz.paykap.agent.tran.helper;

import org.springframework.data.jpa.domain.Specification;
import uz.paykap.domain.P2pParam;

import javax.persistence.criteria.Expression;

public class ConditionSpecification {

    public static Specification<P2pParam> titleContains(String searchWord) {
        return (root, query, builder) -> {
            Expression<String> titleLowerCase = builder.lower(root.get("title"));
            return builder.like(titleLowerCase, "%" + searchWord.toLowerCase() + "%");
        };
    }

}
