package com.vince.retailmanager.repository;

import com.vince.retailmanager.model.entity.fees.PercentageFee;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PercentageFeeRepository extends JpaRepository<PercentageFee, Integer> {

  List<PercentageFee> findAllByFranchiseeIdIn(Set<Integer> ids);

  List<PercentageFee> findAllByFranchiseeIdInAndFeeType(Set<Integer> ids, String type);

}
