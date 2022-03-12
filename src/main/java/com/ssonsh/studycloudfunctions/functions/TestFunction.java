package com.ssonsh.studycloudfunctions.functions;

import com.ssonsh.studycloudfunctions.functions.rqrs.TestRq;
import com.ssonsh.studycloudfunctions.functions.rqrs.TestRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class TestFunction implements Function<TestRq, TestRs> {
    @Override
    public TestRs apply(TestRq testRq) {
        log.info("testRq : {}", testRq);
        return TestRs.builder().result(true).build();
    }
}
