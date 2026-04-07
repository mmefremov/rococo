package io.efremov.rococo.data.core;

import static org.apache.commons.lang3.StringUtils.isNoneEmpty;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.SqlFormatter.Formatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllureHibernateAppender implements MessageFormattingStrategy {

  private static final Formatter formatter = SqlFormatter.of(Dialect.PostgreSql);
  private static final FormatConfig config = FormatConfig.builder()
      .maxColumnLength(100)
      .uppercase(true)
      .build();

  @Override
  public String formatMessage(final int connectionId, final String now, final long elapsed, final String category,
      final String prepared, final String sql, final String url) {
    if (isNoneEmpty(sql)) {
      Allure.addAttachment("query", "text/plain", formatter.format(sql, config), "txt");
    }
    return "SQL query: %s ms%n%s".formatted(elapsed, formatter.format(sql, config));
  }
}
