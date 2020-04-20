package io.micronaut.aws.alexa.httpserver

import com.amazon.ask.model.Application
import com.amazon.ask.model.Context
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Request
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.Session
import com.amazon.ask.model.User
import com.amazon.ask.model.interfaces.system.SystemState
import java.time.OffsetDateTime
import java.time.ZoneId

import static com.amazon.ask.util.SdkConstants.FORMAT_VERSION

trait RequestEnvelopFixture {
    private static final String LOCALE = "en-US";

    RequestEnvelope buildRequestEnvelope(String version, Request request, String applicationId) {
        Application application = Application.builder().withApplicationId(applicationId).build()
        SystemState systemState = SystemState.builder().withApplication(application).build();
        Context context = Context.builder().withSystem(systemState).build()
        RequestEnvelope
                .builder()
                .withContext(context)
                .withVersion(version)
                .withSession(buildSession(application))
                .withRequest(request)
                .build()
    }

    RequestEnvelope launchRequestEnvelop(String skillId) {
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault())
        LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("rId").withLocale(LOCALE).withTimestamp(timestamp).build()
        buildRequestEnvelope(FORMAT_VERSION, launchRequest, skillId)
    }

    Session buildSession(Application application) {
        Session
                .builder()
                .withSessionId("sId")
                .withApplication(application)
                .withUser(User.builder().withUserId("UserId").build())
                .build()
    }
}
