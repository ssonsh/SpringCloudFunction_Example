package com.ssonsh.studycloudfunctions.functions;

import com.ssonsh.studycloudfunctions.functions.rqrs.TestRq;
import com.ssonsh.studycloudfunctions.functions.rqrs.TestRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
public class TestConsumer implements Consumer<TestRq> {
    @Override
    public void accept(TestRq testRq) {
        log.info("TestRq : {} ", testRq);
    }
}
