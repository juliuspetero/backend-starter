package com.mojagap.backenstarter.repository.branch;

import com.mojagap.backenstarter.model.branch.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    String topBottomQuery = """
            WITH RECURSIVE branchCTE (id, name, parent_branch_id, company_id, created_on, created_by, modified_on, modified_by,
                                      record_status) AS
                               (
                                   SELECT id,
                                          name,
                                          parent_branch_id,
                                          company_id,
                                          created_on,
                                          created_by,
                                          modified_on,
                                          modified_by,
                                          record_status
                                   FROM branch
                                   WHERE parent_branch_id = :parentId
                                      OR id = :parentId
                                   UNION
                                   DISTINCT
                                   SELECT br.id,
                                          br.name,
                                          br.parent_branch_id,
                                          br.company_id,
                                          br.created_on,
                                          br.created_by,
                                          br.modified_on,
                                          br.modified_by,
                                          br.record_status
                                   FROM branchCTE AS cte
                                            JOIN branch AS br
                                                 ON br.parent_branch_id = cte.id
                               )
            SELECT id,
                   name,
                   parent_branch_id,
                   company_id,
                   created_on,
                   created_by,
                   modified_on,
                   modified_by,
                   record_status
            FROM branchCTE
            """;

    String bottomUpQuery = """
            WITH RECURSIVE branchCTE (id, name, parent_branch_id, company_id, created_on, created_by, modified_on, modified_by,
                                      record_status) AS
                               (
                                   SELECT *
                                   FROM branch
                                   WHERE parent_branch_id = :childId
                                      OR id = :childId
                                   UNION
                                   DISTINCT
                                   SELECT br.*
                                   FROM branchCTE AS cte
                                            JOIN branch AS br
                                                 ON br.id = cte.parent_branch_id
                               )
            SELECT *
            FROM branchCTE
            """;

    @Query(value = topBottomQuery,
            nativeQuery = true)
    List<Branch> topBottomHierarchy(Integer parentId);

    @Query(value = bottomUpQuery,
            nativeQuery = true)
    List<Branch> bottomTopHierarchy(Integer childId);

    @Override
    @Query(value = "SELECT * FROM branch WHERE record_status = 'ACTIVE' AND id = :branchId",
            nativeQuery = true)
    Optional<Branch> findById(Integer branchId);
}
