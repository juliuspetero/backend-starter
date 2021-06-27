package com.mojagap.backenstarter.repository.company;

import com.mojagap.backenstarter.model.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    String topBottomQuery = """
            WITH RECURSIVE companyCTE (id,
                                       name,
                                       registration_date,
                                       registration_number,
                                       company_type,
                                       account_id,
                                       address,
                                       email,
                                       phone_number,
                                       parent_company_id,
                                       created_on,
                                       modified_on,
                                       created_by,
                                       modified_by,
                                       record_status) AS
                               (
                                   SELECT *
                                   FROM company
                                   WHERE parent_company_id = :parentId
                                      OR id = :parentId
                                   UNION
                                   DISTINCT
                                   SELECT com.*
                                   FROM companyCTE AS cte
                                            JOIN company AS com
                                                 ON com.parent_company_id = cte.id
                               )
            SELECT *
            FROM companyCTE
            """;

    String bottomUpQuery = """
            WITH RECURSIVE companyCTE (id,
                                       name,
                                       registration_date,
                                       registration_number,
                                       company_type,
                                       account_id,
                                       address,
                                       email,
                                       phone_number,
                                       parent_company_id,
                                       created_on,
                                       modified_on,
                                       created_by,
                                       modified_by,
                                       record_status) AS
                               (
                                   SELECT *
                                   FROM company
                                   WHERE parent_company_id = :childId
                                      OR id = :childId
                                   UNION
                                   DISTINCT
                                   SELECT com.*
                                   FROM companyCTE AS cte
                                            JOIN company AS com
                                                 ON com.id = cte.parent_company_id
                               )
            SELECT *
            FROM companyCTE
            """;

    @Query(value = topBottomQuery,
            nativeQuery = true)
    List<Company> topBottomHierarchy(Integer parentId);

    @Query(value = bottomUpQuery,
            nativeQuery = true)
    List<Company> bottomTopHierarchy(Integer childId);


    @Query(value = "SELECT * FROM company WHERE record_status = 'ACTIVE' AND id = :companyId",
            nativeQuery = true)
    Optional<Company> findCompanyById(Integer companyId);
}
