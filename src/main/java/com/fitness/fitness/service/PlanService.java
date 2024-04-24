package com.fitness.fitness.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitness.fitness.model.Benefit;
import com.fitness.fitness.model.Plan;
import com.fitness.fitness.model.PlanDurationPrice;
import com.fitness.fitness.repository.PlanDurationPriceRepo;
import com.fitness.fitness.repository.PlanRepo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanService {
    @Autowired
    private PlanRepo planRepo;
    @Autowired
    private PlanDurationPriceRepo planDurationPriceRepo;
    public List<Plan> findByPlanType(String planType) {
        return planRepo.findByPlanType(planType);
    }

    public Map<String, Double> getStartingPricesForAllPlanTypes() {
        List<PlanDurationPrice> plans = planDurationPriceRepo.findAll();
        Map<String, List<PlanDurationPrice>> plansByType = plans.stream()
                .collect(Collectors.groupingBy(plan -> plan.getPlan().getPlanType()));

        Map<String, Double> startingPrices = new HashMap<>();
        plansByType.forEach((planType, planList) -> {
            double startingPrice = planList.stream()
                    .mapToDouble(PlanDurationPrice::getPlanPrice)
                    .min()
                    .orElse(0.0);
            startingPrices.put(planType, startingPrice);
        });
        return startingPrices;
    }
    public LinkedHashMap<String,Double> getSortedPrices(){
    LinkedHashMap<String, Double> sortedStartingPrices = getStartingPricesForAllPlanTypes().entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (oldValue, newValue) -> oldValue,
                LinkedHashMap::new));
                return sortedStartingPrices;
    }
    public Map<String, List<Benefit>> findBenefitsByPlanType(String planType) {
        Map<String, List<Benefit>> planBenefits = new HashMap<>();
        
        List<Benefit> benefits = planRepo.findByPlanType(planType).stream()
                                          .flatMap(plan -> plan.getBenefits().stream())
                                          .sorted(Comparator.comparing(Benefit::getDescription)) // Assuming you want to sort by description
                                          .collect(Collectors.toList());
        
        planBenefits.put(planType, benefits);
        
        return planBenefits;
    }
    public Map<String, List<Benefit>> sortedBenefits(){
        List<String> sortedPlanTypes = new ArrayList<>(getSortedPrices().keySet());
        Map<String, List<Benefit>> planBenefits = new HashMap<>();
        sortedPlanTypes.forEach(planType -> {
            List<Benefit> benefits = findBenefitsByPlanType(planType).get(planType);
            planBenefits.put(planType, benefits);
        });
        return planBenefits;
    }
}
    
    
    

