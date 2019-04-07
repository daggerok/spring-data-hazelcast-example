package com.github.daggerok;

import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@Log4j2
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SpringTddExampleApplicationTests {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void test() {
    final ResponseEntity response = restTemplate.getForEntity("/api/v1", List.class);
    assertThat(response.getStatusCode()).isEqualTo(OK);

    final Object body = response.getBody();
    assertThat(body).isNotNull();

    final List list = (List) body;
    assertThat(list).hasSizeGreaterThanOrEqualTo(4);

    log.info(() -> list);
  }
}
