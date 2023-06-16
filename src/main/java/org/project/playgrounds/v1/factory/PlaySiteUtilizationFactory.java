package org.project.playgrounds.v1.factory;

import lombok.extern.slf4j.Slf4j;
import org.project.playgrounds.enums.UtilizationType;
import org.project.playgrounds.v1.dto.Equipment;
import org.project.playgrounds.v1.strategy.PlaySiteUtilization;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlaySiteUtilizationFactory {
    private final Map<UtilizationType, PlaySiteUtilization> calculatorMap;

    public PlaySiteUtilizationFactory(List<PlaySiteUtilization> calculators) {
        this.calculatorMap = calculators.stream()
                .collect(Collectors.toMap(this::getCalculatorType, Function.identity(), (a, b) -> a));
    }

    public PlaySiteUtilization getCalculator(Set<Equipment> equipments) {
        log.info("calculators {}", calculatorMap);
        return equipments.stream()
                .map((s) -> calculatorMap.get(UtilizationType.valueOf(s.name().name()))
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(calculatorMap.get(UtilizationType.DEFAULT));
    }

    private UtilizationType getCalculatorType(PlaySiteUtilization calculator) {
        UtilizationType[] types = UtilizationType.values();
        for (UtilizationType type : types) {
            if (calculator.getClass().getSimpleName().startsWith(type.getName())) {
                return type;
            }
        }
        return UtilizationType.DEFAULT;
    }
}
