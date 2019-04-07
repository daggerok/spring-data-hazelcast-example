package com.github.daggerok;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.hazelcast.HazelcastKeyValueAdapter;
import org.springframework.data.hazelcast.repository.config.EnableHazelcastRepositories;
import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static lombok.AccessLevel.PACKAGE;

@Data
@KeySpace("entities")
@NoArgsConstructor(access = PACKAGE)
@RequiredArgsConstructor(staticName = "of")
class MyObject implements Serializable {

  private static final long serialVersionUID = 4014069328106601233L;

  @Id
  private UUID id;

  @NonNull
  private String data;
}

@Configuration
@EnableHazelcastRepositories
class HzCfg {

  @Bean
  public HazelcastInstance hazelcastInstance() {
    return Hazelcast.newHazelcastInstance();
  }

  @Bean
  public KeyValueTemplate keyValueTemplate() {
    return new KeyValueTemplate(new HazelcastKeyValueAdapter(hazelcastInstance()));
  }
}

@Repository
interface MyObjectStore extends KeyValueRepository<MyObject, UUID> {}

@Log4j2
@Configuration
@RequiredArgsConstructor
class AppCfg {

  private final MyObjectHandlers handlers;

  @Bean
  public RouterFunction<ServerResponse> routes() {
    return RouterFunctions.route()
                          .POST("/api/v1/**", handlers::post)
                          .GET("/api/v1/{uuid}", handlers::getById)
                          .GET("/api/v1/**", handlers::get)
                          .build();
  }

}

@Log4j2
@Service
@RequiredArgsConstructor
class MyObjectHandlers {

  private final MyObjectStore myObjects;

  @PostConstruct
  public void runner() {
    log.info("initialize test-data...");
    Stream.of("one", "two", "three", "four")
          .map(MyObject::of)
          .map(myObjects::save)
          .forEach(log::info);
  }

  public ServerResponse getById(ServerRequest serverRequest) {
    log.info("getting object by uid...");
    final String uuid = serverRequest.pathVariable("uuid");
    return myObjects.findById(UUID.fromString(uuid))
                    .map(myObject -> ServerResponse.ok().body(myObject))
                    .orElse(ServerResponse.notFound().build());
  }

  @SneakyThrows
  public ServerResponse post(ServerRequest serverRequest) {
    log.info("save objects...");
    final MyObject myObject = serverRequest.body(MyObject.class);
    final MyObject created = myObjects.save(myObject);
    final String uuid = created.getId().toString();
    final URI uri = serverRequest.uri();
    final String url = format("%s://%s/api/v1/%s", uri.getScheme(), uri.getAuthority(), uuid);
    return ServerResponse.created(URI.create(url)).body(created);
  }

  public ServerResponse get(ServerRequest serverRequest) {
    log.info("getting objects...");
    return ServerResponse.ok().body(myObjects.findAll());
  }
}

@SpringBootApplication
public class SpringDataHzApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringDataHzApplication.class, args);
  }
}
