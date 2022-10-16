package j2d.generated.repository;

import dabba.doo.annotationprocessor.db.Pojo;
import dabba.doo.annotationprocessor.db.paramresolver.ParameterMapCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public final class PojoRepository {
  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  PojoRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  public boolean create(final Pojo instance) {
    return namedParameterJdbcTemplate.update(
                    "insert into pojo_table( values(",
                    ParameterMapCreator.buildParamsMap(instance)
                    ) > 0;
  }

  public boolean delete(final Pojo instance) {
    return namedParameterJdbcTemplate.update(
                    "delete from pojo_table where ",
                    ParameterMapCreator.buildParamsMap(instance)
                    ) > 0;
  }
}
