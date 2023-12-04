package xyz.erupt.eql.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Dql {

    //去重
    private boolean distinct = false;

    private Collection<?> source;

    //列信息
    private List<Column<?>> columns = new ArrayList<>();

    //关联信息
    private List<JoinSchema<?>> joinSchemas = new ArrayList<>();

    //条件信息
    private List<Function<Map<Column<?>, ?>, Boolean>> conditions = new ArrayList<>();

    //分组信息
    private List<Column<?>> groupBys = new ArrayList<>();

    private Long limit = null;

    private Long offset = null;


    public boolean isDistinct() {
        return distinct;
    }

    public Collection<?> getSource() {
        return source;
    }

    public void setSource(Collection<?> source) {
        this.source = source;
    }


    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<Column<?>> getColumns() {
        return columns;
    }

    public void setColumns(List<Column<?>> columns) {
        this.columns = columns;
    }

    public List<Function<Map<Column<?>, ?>, Boolean>> getConditions() {
        return conditions;
    }

    public void setConditions(List<Function<Map<Column<?>, ?>, Boolean>> conditions) {
        this.conditions = conditions;
    }

    public List<JoinSchema<?>> getJoinSchemas() {
        return joinSchemas;
    }

    public void setJoinSchemas(List<JoinSchema<?>> joinSchemas) {
        this.joinSchemas = joinSchemas;
    }

    public List<Column<?>> getGroupBys() {
        return groupBys;
    }

    public void setGroupBys(List<Column<?>> groupBys) {
        this.groupBys = groupBys;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

}
