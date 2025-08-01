package org.example.rewrite;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.example.config.ShardingSphereConfig;

public class CustomSQLRewriter implements SQLRewriteContextDecorator<EncryptRule> {
    private static final ThreadLocal<Integer> threadLocalOrder = ThreadLocal.withInitial(() -> 0);
    private static final String chunksColumn = ShardingSphereConfig.getChunksColumnName();
    @Override
    public void decorate(EncryptRule rule, ConfigurationProperties props,
                         SQLRewriteContext context, RouteContext routeContext) {
        String likePattern = chunksColumn + " LIKE ?";
        SQLStatementContext<?> sqlStatementContext = context.getSqlStatementContext();
        String sql = context.getSql();

        if (sqlStatementContext instanceof SelectStatementContext && sql.contains(likePattern)) {
            int startIndex = sql.indexOf(likePattern);
            int stopIndex = startIndex + likePattern.length();
            context.getSqlTokens().add(new ReplacementToken(startIndex, stopIndex, chunksColumn));
        }
    }
    public static void setOrder(int order) {
        threadLocalOrder.set(order);
    }

    public static void clearOrder() {
        threadLocalOrder.remove();
    }

    @Override
    public int getOrder() {
        return threadLocalOrder.get();
    }

    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
    private static class ReplacementToken extends SQLToken implements Substitutable {
        private final int stopIndex;

        public ReplacementToken(int startIndex, int stopIndex, String chunksColumn) {
            super(startIndex);
            this.stopIndex = stopIndex;
        }
        @Override
        public int getStopIndex() {
            return stopIndex;
        }

        @Override
        public String toString() {
            return chunksColumn + " @> ?::text[]";
        }
    }
}
