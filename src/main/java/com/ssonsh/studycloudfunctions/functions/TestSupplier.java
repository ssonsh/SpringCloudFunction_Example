package com.ssonsh.studycloudfunctions.functions;

import com.ssonsh.studycloudfunctions.functions.rqrs.TestRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class TestSupplier implements Supplier<TestRs> {
    @Override
    public TestRs get() {
        log.info("supplier-----*");
        return TestRs.builder().result(true).build();
    }
}
