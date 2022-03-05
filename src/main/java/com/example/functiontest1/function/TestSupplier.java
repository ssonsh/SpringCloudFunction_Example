package com.example.functiontest1.function;

import com.example.functiontest1.rqrs.TestRq;
import com.example.functiontest1.rqrs.TestRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
public class TestSupplier implements Supplier<TestRs> {

    @Override
    public TestRs get() {
        log.info("----- request Test Supplier ------");

        return TestRs.builder().result(true).build();
    }
}
