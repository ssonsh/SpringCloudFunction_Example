# SpringCloudFunction_Example

# Notion Link
https://five-cosmos-fb9.notion.site/Spring-Cloud-Function-Lambda-0d067b3723d14b0f803986b95f9fc84b


# **Introduction**

Spring Cloud Function의 핵심은 **"Function"** 

Cloud 환경을 자주 접하고 활용한다면 **Server-side**에서 **function**과 연관되어 생각나는 부분이 바로 **Serverless 이다.**

**`Spring cloud function`**은 

Serverless의 특성에 맞게 개발할 수 있도록 도와주는 Spring 진영의 framework

**Spring Cloud Function 의 목적**

- **"Function"**을 통해 비즈니스 로직을 구현한다.
- 같은 코드가 웹 엔드포인트, 스트림 프로세서 등으로 작동할 수 있도록 
특정 타겟 런타임으로부터 비즈니스 로직의 개발 라이프사이클을 분리한다.
- 특정 Serverless Provider에 종속되지 않고 통합된 프로그래밍 모델을 제공한다. 
또한, 독립적(standalone, locally)으로 구동할 수 있는 기능도 제공한다.
    - Serverless Provider ? AWS, Google Cloud, etc ...
- Serverless환경에서 Spring Boot 기능(Auto configure, dependency injection, metrics)을 사용할 수 있도록 한다.

<aside>
🎈 Spring Cloud Function 은 전송과 인프라 계층을 추상화시킴으로서 
개발자들이 익숙한 도구 및 프로세스를 사용할 수 있도록 도와주고, 
비즈니스 로직에만 집중할 수 있도록 지원한다.

</aside>

ref. 

- [https://binux.tistory.com/61](https://binux.tistory.com/61)
- [https://www.baeldung.com/spring-cloud-function](https://www.baeldung.com/spring-cloud-function)

---

---

<aside>
🎈 실질적인 Lambda Function 개발 전 가볍게 어떻게 구성되는지 알아보자.

</aside>

### 구성해보고자 하는 프로젝트 구조

1. 휴면처리 대상자에게 안내 메일을 발송하는 Lambda Function
2. 휴면처리 대상자를 휴면처리하는 Lambdma Function

![image](https://user-images.githubusercontent.com/18654358/156862221-3708203a-ab4a-4593-989e-c643cb7b4450.png)

### 의존성

Spring Boot의 Cloud 를 이용하여 AWS Lambda 함수를 작성할 수 있다.

<aside>
🎈 기본적으로 DB접근, JPA, 등 스프링의 IoC와 DI를 활용한 방법으로 Lambda 함수를 작성하고자 한다면 이 방법을 이용하여 프로젝트를 구성하자.

</aside>

**build.gradle**

```java
plugins {
    id 'org.springframework.boot' version '2.6.4'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
}

group = 'com.hrp'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '1.8'

repositories {
    mavenCentral()
}

ext {
    set('springCloudVersion', "2021.0.1")
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.cloud:spring-cloud-function-context'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    // aws - lambdas
    implementation 'org.springframework.cloud:spring-cloud-function-adapter-aws'
    implementation 'com.amazonaws:aws-lambda-java-core:1.2.1'
    implementation 'com.amazonaws:aws-lambda-java-events:3.8.0'

    // aws - lambdas - test
    implementation 'org.springframework.cloud:spring-cloud-starter-function-web'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}

task buildZip(type: Zip) {
    from compileJava
    from processResources
    into('lib') {
        from configurations.runtimeClasspath
    }
}
```

**개발에 필요한 dependencies** 

- spring-cloud-function-adapter-aws
- aws-lambda-java-core:1.2.1
- aws-lambda-java-events:3.8.0

**구성한 Lambda 함수를 패키징하기 위한 task**

- buildZip

![image](https://user-images.githubusercontent.com/18654358/156862237-1d694d26-a63c-4775-b140-03766f8a5cd7.png)

<aside>
🎈 개발된 프로젝트는 zip 파일로 패키징하여 S3에 업로드 하거나 AWS Lambda 함수에 업로드 할 수 있다. 
- 이를 통해 Lambda Function Deploy 를 수행한다.

</aside>

- [https://codetinkering.com/spring-cloud-function-aws-lambda/](https://codetinkering.com/spring-cloud-function-aws-lambda/)

**Lambda 함수로 실행시켜볼 간단한 함수를 구성해본다.**

DormantAccountNotificationFunction .java

```java
package com.hrp.job.dormantaccount.function;

import com.hrp.job.dormantaccount.config.contextholder.TenantContextHolder;
import com.hrp.job.dormantaccount.function.rqrs.DormantAccountNotificationRq;
import com.hrp.job.dormantaccount.function.rqrs.DormantAccountNotificationRs;
import com.hrp.job.dormantaccount.function.rqrs.DormantAccountRq;
import com.hrp.job.dormantaccount.function.rqrs.DormantAccountRs;
import com.hrp.job.dormantaccount.function.service.DormantAccountService;
import com.hrp.job.dormantaccount.function.service.TenantFinderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DormantAccountNotificationFunction implements Supplier<DormantAccountNotificationRs> {

    private final Environment env;
    private final DormantAccountService dormantAccountService;
    private final TenantFinderService tenantFinderService;

    @Override
    public DormantAccountNotificationRs get() {

        String[] activeProfiles = env.getActiveProfiles();
        String dbUrl = env.getProperty("spring.datasource.url");

        log.info("activeProfiles : {}", activeProfiles);
        log.info("dbUrl : {}", dbUrl);

        List<String> tenants = tenantFinderService.findTargetTenants();
        for (String tenant : tenants) {
            TenantContextHolder.setTenant(tenant);

            log.info("target tenant : {} ", tenant);
            dormantAccountService.notifyToSleeperAccount();
        }
        return DormantAccountNotificationRs.builder().result("[DormantAccountNotificationFunction] complete!").build();
    }
}
```

**🎈 포인트 → 어떤 Functional Interface 를 사용할 것인가?!**

- implements **`Supplier`**<Rs>
    - **`어떤 Functional Interface를 사용할 것인지 용도에 따라 지정하여 사용한다!!`**
- Function은 Java8 이후 부터 제공하는 핵심 Functional Interface 이다.
    - **`Function`**<I, O>
        - 값을 다른 값으로 변환해 리턴
    - **`Supplier`** <O>
        - 입력 값 없이 리턴 값만 있는 경우
    - **`Consumer`**<I>
        - 값을 받아 처리만하고 리턴 값이 없는 경우

구성된 Function은 `**@Component 애노태이션**`을 통해 **스프링 빈으로 주입**될 수 있도록 한다. 

내부에서 수행되는 sleeperAccountService 는 하드코딩된 정보를 반환해주는 Service로써 이또한 스프링 빈으로 주입될 수 있도록 한다. 

SleeperAccountService.java

```java
package com.hrp.springlambdatest1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SleeperAccountService {

    public List<String> findTargetTenants() {
        List<String> tenants = new ArrayList<>();
        tenants.add("hrp");
        tenants.add("sprint001");
        tenants.add("sprint002");
        return tenants;
    }

    public List<Long> findSleeperAccountTargetsByTenant(String tenant) {
        List<Long> accountIds = new ArrayList<>();
        accountIds.add(1L);
        accountIds.add(2L);
        accountIds.add(3L);
        return accountIds;
    }

    public void toSleeperAccount(List<Long> accountIds) {
        for (Long accountId : accountIds) {
            log.info("to sleeper account : {}", accountId);
        }
    }
}
```

---

### **작성된 내용을 로컬에서 테스트 해보자.**

테스트 시작에 앞서 로컬테스트를 위해서는 추가적인 Dependency가 필요하다.

- ***해당 의존성을 주입하고 프로젝트를 기동시키면 서버가 기동된 것과 동일하게 동작한다.***
- 개발한 Function이 endpoint로 제공된다.

```java
// aws - lambdas - test
implementation 'org.springframework.cloud:spring-cloud-starter-function-web'
```

SpringBootApplication 을 실행하면 정의된 포트로 서버가 뜨는 것을 확인할 수 있다.

![image](https://user-images.githubusercontent.com/18654358/156862257-f16c58ea-fe38-40b6-a605-7cae2e2e4b0c.png)

여기서 함수 요청에 대한 endpoint는 작성한 함수명이다.

- 위 예시에선 **“SleeperAccountNotification“** 이 된다.

![image](https://user-images.githubusercontent.com/18654358/156862272-1669b7d1-22f0-43da-80fa-01fcda6f6acb.png)

**호출 후 서버의 로그를 확인해보자.**

- 테스트를 위해 찍어두었던 log 정보들이 정상적으로 호출되는 것을 볼 수 있다.

```java
2022-02-28 17:21:53.765  INFO 22844 --- [nio-8080-exec-2] c.h.s.f.SleeperAccountNotification       : target tenant : hrp 
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.f.SleeperAccountNotification       : target tenant : hrp _ target account count : 3
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 1
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 2
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 3
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.f.SleeperAccountNotification       : target tenant : sprint001 
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.f.SleeperAccountNotification       : target tenant : sprint001 _ target account count : 3
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 1
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 2
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 3
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.f.SleeperAccountNotification       : target tenant : sprint002 
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.f.SleeperAccountNotification       : target tenant : sprint002 _ target account count : 3
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 1
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 2
2022-02-28 17:21:53.766  INFO 22844 --- [nio-8080-exec-2] c.h.s.SleeperAccountService              : to sleeper account : 3
```

---

### 여기까지 간단하게 함수를 만들고 로컬에서 실행해보았다.

### 이제 이 함수를 AWS Lambda로 올려서 테스트해보자!

**AWS Handler 생성**

- AWS Lambda가 Spring Cloud Function  프로젝트를 다루기 위해
- Spring Cloud Function AWS 에서 제공하는 핸들러를 상속받은 Handler 클래스가 필요하다.
- **extends SpringBootStreamHandler**

```java
package com.hrp.springlambdatest1.handler;

import org.springframework.cloud.function.adapter.aws.SpringBootStreamHandler;

public class LambdaHandlers extends SpringBootStreamHandler {
}
```

- 가볍게 위와 같이 LambdaHandler 를 생성해주자.
- 여기까지가 끝이다

**작성한 Spring Cloud Function 프로젝트를 Zip 으로 압축한다.**

<aside>
🎈 패키징 관련!
[https://docs.aws.amazon.com/lambda/latest/dg/java-package.html](https://docs.aws.amazon.com/lambda/latest/dg/java-package.html)

maven package issue
  [https://stackoverflow.com/questions/55748745/unable-to-parse-configuration-of-mojo-org-apache-maven-pluginsmaven-shade-plugi](https://stackoverflow.com/questions/55748745/unable-to-parse-configuration-of-mojo-org-apache-maven-pluginsmaven-shade-plugi)

</aside>

- 위에서 생성한 buildZip Task를 이용해 zip 파일을 생성한다.

![image](https://user-images.githubusercontent.com/18654358/156862295-cf3791f0-0326-4038-9c79-826969e7642d.png)

- 위와 같이 AWS Lambda 를 생성하고 zip 파일을 업로드한다.

**런타임 핸들러 경로 잡아주기**

![image](https://user-images.githubusercontent.com/18654358/156862321-43d57015-49ef-41dd-b806-538e544f5128.png)

**람다 함수 환경변수 잡아주기**

- 여기서 “FUNCTION_NAME” 은 해당 LambdaHandler 가 수행할 함수 Job 을 명시하는 것과 같다.

<aside>
🎈 만약 개발한 Spring Cloud Function 프로젝트에 1개가 아닌 N개의 Function을 개발하였다면

하나의 zip Package 형상으로 여러개의 AWS Lambda 함수에 하나의 형상을 Deploy 하고 

각 AWS Lambda 함수가 수행할 Function을 정의하여 원하는 Function을 수행시킬 수 있다.

</aside>

![image](https://user-images.githubusercontent.com/18654358/156862340-285a52a2-0f72-46af-850e-35beeaab0026.png)

**간단히 테스트 해보자.**

![image](https://user-images.githubusercontent.com/18654358/156862346-f10c4134-fd70-4b77-aa99-702757902cc4.png)

<aside>
🎈 초기 실행은 Spring 이 올라오는 시간이 있어서 조금 느리지만 이후 요청은 올라간 Spring 컨테이너에서 바로바로 실행함으로 빠른 속도를 나타내었다.

</aside>

<aside>
🎈 그러나 5분 이상 요청이 없으면 인스턴스가 사라지게 된다.

</aside>

---

---

### 생성된 Lambda 를 원하는 주기별로 실행시켜보자.

- **AWS EventBridge**
    - AWS CloudWatch + Event
**Amazon EventBridge 접근 → 이벤트 → 규칙**

![image](https://user-images.githubusercontent.com/18654358/157833570-7d581583-3e0c-43cf-add2-2b8ff136fbe9.png)
![image](https://user-images.githubusercontent.com/18654358/157833610-a25d9d98-704d-48e3-b6c7-92c4dc2c48ed.png)
    
<aside>
🎈 매일 00시00분에 수행되게 하고싶다 🙂
- [https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-create-rule-schedule.html#eb-cron-expressions](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-create-rule-schedule.html#eb-cron-expressions)

</aside>

![image](https://user-images.githubusercontent.com/18654358/157833648-0f432bb6-26f8-43e6-9c63-8aa12ee36eea.png)

- Target은 AWS 서비스의 Lambda 함수이며
    - 구성해놓은 hrp-auth-functions-dormant-account 라는 명을 가진 AWS Lambda 함수이다.
   

    
    ### Bibutcket을 이용해 AWS S3의 Bucket으로 배포형상을 Deploy 해보자. 🙂

bitbucket-pipeline.yml

```yaml
image: gradle:6.6.0

pipelines:
  branches:
    release/adv:
      - step:
          name: Build and Test
          caches:
            - gradle
          script:
            - chmod +x ./gradlew
            - ./gradlew buildZip
          artifacts:
            - build/distributions/*.zip
      - step:
          name: Deploy to AWS S3
          script:
            - pipe: atlassian/aws-s3-deploy:0.3.7
              variables:
                AWS_ACCESS_KEY_ID: # AWS ACCESS KEY
                AWS_SECRET_ACCESS_KEY: # AWS SECRET KEY
                AWS_DEFAULT_REGION: 'ap-northeast-2'
                S3_BUCKET: 'hrp-auth-functions-bucket'
                LOCAL_PATH: 'build/distributions'
                EXTRA_ARGS: '--delete'
```
