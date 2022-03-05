package com.example.functiontest1.function;

import com.example.functiontest1.rqrs.TestRq;
import com.example.functiontest1.rqrs.TestRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class TestFunction implements Function<TestRq, TestRs> {
    @Override
    public TestRs apply(TestRq testRq) {

        log.info("----- request Test Function 1 ------ rq : {}", testRq.toString());

        return TestRs.builder().result(true).build();
    }
}
