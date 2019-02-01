package com.amazon.ask.helloworld.handlers

import javax.inject.Singleton

@Singleton
class MessageService {
    fun sayHello() : String {
        return "Hello world"
    }
}
