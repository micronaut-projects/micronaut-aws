package io.micronaut.aws.dynamodb;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class DynamoDbConversionServiceTest  {

    @Test
    void youCannotConvertFromAPojoWhichIsNotIntrospected(DynamoDbConversionService dbConversionService) {
        assertNotNull(dbConversionService);
        Executable e = () -> dbConversionService.convert(new User("WARRENBUFFETT", "Warren Buffet", "Admin"));
        IntrospectionException thrown = assertThrows(IntrospectionException.class, e);
    }

    @Test
    void canConvertFromAnIntrospectedPojo(DynamoDbConversionService dbConversionService) {
        assertNotNull(dbConversionService);
        Map<String, AttributeValue> item = dbConversionService.convert(new UserItem("ORG#BERKSHIRE", "USER#WARRENTBUFFET", "WARRENBUFFETT", "Warren Buffet", "Admin"));
        assertNotNull(item);
        assertTrue(item.containsKey("pk"));
        assertTrue(item.containsKey("sk"));
        assertTrue(item.containsKey("id"));
        assertTrue(item.containsKey("username"));
        assertTrue(item.containsKey("role"));
        assertEquals("ORG#BERKSHIRE", item.get("pk").s());
        assertEquals("USER#WARRENTBUFFET", item.get("sk").s());
        assertEquals("WARRENBUFFETT", item.get("id").s());
        assertEquals("Warren Buffet", item.get("username").s());
        assertEquals("Admin", item.get("role").s());


        UserItem userItem = (UserItem) dbConversionService.convert(item, UserItem.class);
        assertNotNull(userItem);
        assertEquals("ORG#BERKSHIRE", userItem.getPk());
        assertEquals("USER#WARRENTBUFFET", userItem.getSk());
        assertEquals("WARRENBUFFETT", userItem.getId());
        assertEquals("Warren Buffet", userItem.getUsername());
        assertEquals("Admin", userItem.getRole());
    }

    static class User {
        private final String id;
        private final String username;
        private final String role;

        User(String id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }

        public String getId() {
            return id;
        }

        String getUsername() {
            return username;
        }

        String getRole() {
            return role;
        }
    }

    static class Organization {
        private final String id;
        private final String organizationName;
        private final String subscriptionLevel;

        Organization(String id, String organizationName, String subscriptionLevel) {
            this.id = id;
            this.organizationName = organizationName;
            this.subscriptionLevel = subscriptionLevel;
        }

        String getId() {
            return id;
        }

        String getOrganizationName() {
            return organizationName;
        }

        String getSubscriptionLevel() {
            return subscriptionLevel;
        }
    }

    @Introspected
    static class OrganizationItem {
        private final String pk;
        private final String sk;
        private final String id;
        private final String organizationName;
        private final String subscriptionLevel;

        OrganizationItem(String pk, String sk, String id, String organizationName, String subscriptionLevel) {
            this.pk = pk;
            this.sk = sk;
            this.id = id;
            this.organizationName = organizationName;
            this.subscriptionLevel = subscriptionLevel;
        }

        String getPk() {
            return pk;
        }

        String getSk() {
            return sk;
        }

        String getId() {
            return id;
        }

        String getOrganizationName() {
            return organizationName;
        }

        String getSubscriptionLevel() {
            return subscriptionLevel;
        }
    }

    @Introspected
    static class UserItem {
        private final String pk;
        private final String sk;
        private final String id;
        private final String username;
        private final String role;

        UserItem(String pk, String sk, String id, String username, String role) {
            this.pk = pk;
            this.sk = sk;
            this.id = id;
            this.username = username;
            this.role = role;
        }

        String getPk() {
            return pk;
        }

        String getSk() {
            return sk;
        }

        String getId() {
            return id;
        }

        String getUsername() {
            return username;
        }

        String getRole() {
            return role;
        }
    }
}
