package com.starwars.backend;

import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

public class Utils {

    public static final String URL_PLANETS = "https://swapi.dev/api/planets";
    public static final String URL_PEOPLE = "https://swapi.dev/api/people";



    public static <T> Comparator<T> buildComparator(
            Sort sort,
            Map<String, Function<T, Comparable>> fieldAccessors,
            String defaultField
    ) {
        if (sort.isUnsorted()) {
            return Comparator.comparing(fieldAccessors.get(defaultField));
        }

        Sort.Order order = sort.iterator().next();
        Function<T, Comparable> keyExtractor = fieldAccessors.getOrDefault(
                order.getProperty(),
                fieldAccessors.get(defaultField)
        );

        Comparator<T> comparator = Comparator.comparing(keyExtractor);

        return order.isAscending() ? comparator : comparator.reversed();
    }
}
