# TDD [![Build Status](https://travis-ci.org/daggerok/spring-data-hazelcast-example.svg?branch=master)](https://travis-ci.org/daggerok/spring-data-hazelcast-example)
Spring TDD with spring-boot 2.2.0.BUILD-SNAPSHOT, spring-data-hazelcast and functional servlet router builder...

_automation_

```bash
./mvnw
```

_manual_

```bash
./mvnw spring-boot:run
http post :8080/api/v1 data=hey
http  get :8080/api/v1
```
