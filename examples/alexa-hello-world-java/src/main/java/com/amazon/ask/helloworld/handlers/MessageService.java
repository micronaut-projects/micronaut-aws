package com.amazon.ask.helloworld.handlers;

import javax.inject.Singleton;

@Singleton
public class MessageService {

    public String sayHello() {
        return "Hello world";
    }
}
