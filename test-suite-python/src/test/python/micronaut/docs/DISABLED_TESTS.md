# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut AWS that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task list
for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 0.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci`.
- Last full-suite result: build successful, 11 tests executed, 0 skipped.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut annotations are generated from imports (`from micronaut.function import FunctionBean`,
  `from micronaut.function.client import FunctionClient`, `from micronaut.context.annotation import Factory, Bean, Requires`).
- `@FunctionClient` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies.
- Methods that implement or override a Java interface keep the Java (camelCase) name (`accept`, `onCreated`,
  `canHandle`, `handle`); other methods are snake_case.
- Java classes are imported from their Java packages (`from software.amazon.awssdk.services.s3 import S3ClientBuilder`,
  `from com.amazon.ask.model.ui import SimpleCard`, `from java.util import Optional`, `from micronaut.function.client.aws.v2
  import AwsInvokeRequestDefinition`) and Python classes from their modules (`from .EventLogger import EventLogger`);
  `java.type(...)` is used only where the import form fails (see "java.type usages" below). Imported classes work as
  `java.instanceof` arguments, in `containsBean(PyClass)` and as injected test fields (`builder: Annotated[S3ClientBuilder, Inject]`,
  `event_logger: Annotated[EventLogger, Inject]`), which the tests use instead of `getBean(...)`.
- Logging uses Python's `logging` module (`LOG = logging.getLogger(__name__)`), never slf4j.
- Annotation members that are Java constants in the Java sources (`@Named(RekognitionClient.SERVICE_NAME)`) use the
  literal value in Python (`@Named("rekognition")`).
- The test-suite `Test` tasks run with `enableAssertions = false`: `jakarta.validation.Validator#validate(T, Class...)`
  called from Python trips a Truffle host-interop assertion on the varargs overload.

## Active `@Disabled` Tests

None.

## java.type usages

Each remaining `java.type(...)` call carries a `# TODO(python): java.type needed because ...` comment.

| File | Call | Reason |
| --- | --- | --- |
| `function/aws/AnalyticsClientTest.py` | `java.type("io.micronaut.function.client.FunctionDefinition")` | `getBeansOfType(FunctionDefinition)` with the imported Micronaut interface fails: the name generated for `from micronaut.function.client import FunctionDefinition` is a wrapper ("Unsupported operation identifier 'typeHashCode' ... type: _MicronautJavaType"). `getBean(S3ClientBuilder)` / `getBean(RekognitionClient)` with imported SDK classes fail the same way ("invalid instantiation of foreign object"); those tests inject the beans as fields instead. |
| `function/aws/test/RequestHandlerTest.py` | `java.type("io.micronaut.docs.function.aws.test.SampleRequestHandler")` | The Java `SampleRequestHandler` copy compiled from this project's `src/test/java` is not on the Python compile classpath (`testPythonCompileClasspath` only extends `testCompileClasspath`, which does not contain the source set's own Java output), so the compiler generates no importable module for it: `from micronaut.docs.function.aws.test... import SampleRequestHandler` fails with `ModuleNotFoundError`, and the `io.micronaut.docs.function.aws.test` package name is also shadowed by the Python package of the test itself. |
| `aws/sdk/v2/RekognitionClientFactory.py` | `java.type("software.amazon.awssdk.http.async.SdkAsyncHttpClient")` | The last segment of the Java package `software.amazon.awssdk.http.async` is a Python keyword, so the package cannot be imported. |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `io.micronaut.docs.function.aws.test.RequestHandlerTest` | `@MicronautLambdaTest` (a Micronaut annotation meta-annotated with JUnit's `@ExtendWith`) is not emitted on the generated Java test class - only `@MicronautTest` is - so the test runs without an application context ("GraalPy context has not been initialized"). The Python test uses `@MicronautTest(environments=["function", "lambda"])`, the environments `MicronautLambdaJunit5Extension` activates, and instantiates the Java `SampleRequestHandler` copy kept in `test-suite-python/src/test/java` (a Python class cannot extend `MicronautRequestHandler`). The guide shows a `[.lang-python]` note. |
| `io.micronaut.docs.aws.sdk.v2.RekognitionClientFactory` | A Python class cannot extend the Java class `AwsClientFactory`, so the Python `@Factory` configures the sync and async builders itself (HTTP client, region, credentials, User-Agent suffix and endpoint override) with the same injected beans the parent class uses. The guide shows a `[.lang-python]` note and language-specific callouts. |

## Intentionally Unsupported Snippet Targets

| Target | Reason |
| --- | --- |
| `io.micronaut.docs.function.aws.Handler` (`languages="java,kotlin,groovy"`) | An AWS Lambda handler must extend the Java class `MicronautRequestStreamHandler` (it is instantiated by the Lambda runtime before any application context exists) and a Python class cannot extend a Java class. The guide shows a `[.lang-python]` note instead; the `EventLogger` function bean it resolves is ported. |
