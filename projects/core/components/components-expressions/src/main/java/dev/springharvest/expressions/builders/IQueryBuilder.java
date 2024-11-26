package dev.springharvest.expressions.builders;

import java.util.List;

public interface IQueryBuilder {

    public List<String> cleanFields(Class<?> rootClass, Class<?> keyClass, List<String> fields) throws NoSuchFieldException;
}
