package com.example.functiontest1.function;

import com.example.functiontest1.rqrs.TestRq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestFunction1Test {

    @Autowired
    private TestFunction testFunction;

    @Autowired
    private TestSupplier testSupplier;

    @Autowired
    private TestConsumer testConsumer;

    @Test
    void testFunction1(){
        testFunction.apply(TestRq.builder().name("ssh1224@midasin.com").build());
        testSupplier.get();
        testConsumer.accept(TestRq.builder().name("ssh1224@midasin.com").build());
    }

}