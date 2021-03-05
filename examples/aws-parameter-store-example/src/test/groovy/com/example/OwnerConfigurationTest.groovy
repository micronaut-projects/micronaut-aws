package com.example

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.Specification

class OwnerConfigurationTest extends Specification {

    void "test it can resolve owners"(){
        given:
        ApplicationContext context = ApplicationContext.run(
                [
                        "owners.fred.name": "Fred",
                        "owners.fred.age": 33,
                        "owners.fred.pets": ["marty", "dino"]
                ], Environment.TEST
        )

        when:
        def owners = context.getBeansOfType(OwnerConfiguration.class)

        then:
        owners
        owners[0].name == "Fred"
        owners[0].age == 33
        owners[0].pets == ["marty", "dino"]
    }

}
