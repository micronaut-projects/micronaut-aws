package com.amazon.ask.helloworld.handlers

import javax.inject.Singleton

@Singleton
class MessageService {

    String sayHello() {
        return "Hello world"
    }
}
