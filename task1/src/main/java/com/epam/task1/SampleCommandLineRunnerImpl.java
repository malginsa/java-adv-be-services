package com.epam.task1;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleCommandLineRunnerImpl implements CommandLineRunner {

    @Override
    public void run(String... args){
        System.out.println("hello world");
    }
}
