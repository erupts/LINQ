package xyz.erupt.eql.query;

import xyz.erupt.eql.consts.JoinExchange;
import xyz.erupt.eql.consts.JoinMethod;
import xyz.erupt.eql.exception.EqlException;
import xyz.erupt.eql.lambda.LambdaInfo;
import xyz.erupt.eql.schema.Column;
import xyz.erupt.eql.schema.Dql;
import xyz.erupt.eql.schema.JoinSchema;
import xyz.erupt.eql.util.Columns;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultQuery extends Query {

    @Override
    public <T> Collection<T> dql(Dql dql, Class<T> target) {
        List<Map<Column<?>, Object>> table = LambdaInfo.objectToLambdaInfos(dql.getSource());
        // join process
        if (!dql.getJoinSchemas().isEmpty()) {
            for (JoinSchema<?> joinSchema : dql.getJoinSchemas()) {
                Column<?> lon = Columns.fromLambda(joinSchema.getLon());
                Column<?> ron = Columns.fromLambda(joinSchema.getRon());
                if (joinSchema.getJoinExchange() == JoinExchange.HASH) {
                    switch (joinSchema.getJoinMethod()) {
                        case LEFT:
                        case INNER:
                        case FULL:
                            List<Map<Column<?>, Object>> targetData = LambdaInfo.objectToLambdaInfos(joinSchema.getTarget());
                            Map<Object, Map<Column<?>, ?>> rightMap = targetData.stream().collect(Collectors.toMap(map -> map.get(lon), map -> map));
                            for (Map<Column<?>, Object> map : table) {
                                if (rightMap.containsKey(map.get(ron))) map.putAll(rightMap.get(map.get(ron)));
                            }
                            if (JoinMethod.INNER == joinSchema.getJoinMethod())
                                table.removeIf(it -> !it.containsKey(lon));
                            if (JoinMethod.FULL == joinSchema.getJoinMethod()) {
                                Map<Object, Map<Column<?>, ?>> leftMap = table.stream().collect(Collectors.toMap(map -> map.get(ron), map -> map));
                                for (Map<Column<?>, Object> tmap : targetData) {
                                    if (!leftMap.containsKey(tmap.get(lon))) table.add(tmap);
                                }
                            }
                            break;
                        case RIGHT:
                            Map<Object, Map<Column<?>, ?>> leftMap = table.stream().collect(Collectors.toMap(map -> map.get(ron), map -> map));
                            table.clear();
                            for (Map<Column<?>, Object> map : LambdaInfo.objectToLambdaInfos(joinSchema.getTarget())) {
                                table.add(map);
                                if (leftMap.containsKey(map.get(lon))) map.putAll(leftMap.get(map.get(lon)));
                            }
                            break;
                    }
                } else {
                    throw new EqlException(joinSchema.getJoinExchange().name() + " is not supported yet");
                }
            }
        }
        // condition process
        table.removeIf(it -> {
            for (Function<Map<Column<?>, ?>, Boolean> condition : dql.getConditions()) {
                if (!condition.apply(it)) return true;
            }
            return false;
        });
        // group by process
        if (null != dql.getGroupBys()) {
            Map<String, List<Map<Column<?>, ?>>> groupMap = new HashMap<>();
            for (Map<Column<?>, Object> columns : table) {
                StringBuilder key = new StringBuilder();
                for (Column<?> groupBy : dql.getGroupBys()) {
                    if (null != groupBy.getValueConvertFun()) {
                        key.append(groupBy.getValueConvertFun().apply(columns.get(groupBy)));
                    } else {
                        key.append(columns.get(groupBy));
                    }
                }
                if (!groupMap.containsKey(key.toString())) {
                    groupMap.put(key.toString(), new ArrayList<>());
                }
                groupMap.get(key.toString()).add(columns);
            }
            table.clear();
            for (Map.Entry<String, List<Map<Column<?>, ?>>> entry : groupMap.entrySet()) {
                Map<Column<?>, Object> values = new HashMap<>(dql.getColumns().size());
                table.add(values);
                for (Column<?> column : dql.getColumns()) {
                    Object val = null;
                    if (null != column.getGroupByFun()) {
                        val = column.getGroupByFun().apply(entry.getValue());
                    } else {
                        if (!entry.getValue().isEmpty()) {
                            val = entry.getValue().get(0).get(column);
                        }
                    }
                    values.put(column, val);
                }
            }
        }

        // order by process
        for (Map<Column<?>, Object> map : table) {
            for (Column<?> orderBy : dql.getOrderBys()) {

            }
        }

        // limit
        if (null != dql.getOffset()) {
            table = dql.getOffset() > table.size() ? new ArrayList<>(0) : table.subList(dql.getOffset(), table.size());
        }
        if (null != dql.getLimit()) {
            table = table.subList(0, dql.getLimit() > table.size() ? table.size() : dql.getLimit());
        }

        System.out.println(table);

        List<T> result = new ArrayList<>();
        // select process
        for (Map<Column<?>, ?> t : table) {
            Map<Column<?>, Object> map = new HashMap<>();
            for (Column<?> column : t.keySet()) {
                for (Column<?> dqlColumn : dql.getColumns()) {
                    if (target == Map.class) {

                    } else {
                        if (dqlColumn == column) {
                            map.put(dqlColumn, t.get(dqlColumn));
                        }
                    }
                }
            }
        }
        // distinct process
        if (dql.isDistinct()) {
            result = result.stream().distinct().collect(Collectors.toList());
        }
        return result;
    }

    private boolean eq(Object a, Object b) {
        if (null == a || null == b) {
            return false;
        } else {
            return a.equals(b);
        }
    }

}
