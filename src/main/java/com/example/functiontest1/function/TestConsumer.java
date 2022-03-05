package com.example.functiontest1.function;

import com.example.functiontest1.rqrs.TestRq;
import com.example.functiontest1.rqrs.TestRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Component
public class TestConsumer implements Consumer<TestRq> {

    @Override
    public void accept(TestRq testRq) {
        log.info("----- request Test Consumer ------ Rq : {}", testRq.toString());
    }
}
