package io.efremov.rococo.api.core;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcConsoleInterceptor implements ClientInterceptor {

  private static final JsonFormat.Printer printer = JsonFormat.printer().alwaysPrintFieldsWithNoPresence();

  @Override
  @Step("gRPC call")
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
      CallOptions callOptions, Channel channel) {
    return new ForwardingClientCall.SimpleForwardingClientCall(
        channel.newCall(methodDescriptor, callOptions)) {
      @Override
      public void start(Listener responseListener, Metadata headers) {
        ForwardingClientCallListener<Object> clientCallListener = new ForwardingClientCallListener<>() {
          @Override
          protected Listener<Object> delegate() {
            return responseListener;
          }

          @Override
          public void onMessage(Object message) {
            try {
              String printed = printer.print((MessageOrBuilder) message);
              log.info("RESPONSE: {}", printed);
              attachToLastAllureStep("response", printed);
            } catch (InvalidProtocolBufferException e) {
              throw new RuntimeException(e);
            }
            super.onMessage(message);
          }
        };
        super.start(clientCallListener, headers);
      }

      @Override
      public void sendMessage(Object message) {
        try {
          String printed = printer.print((MessageOrBuilder) message);
          log.info("REQUEST: {}", printed);
          attachToLastAllureStep("request", printed);
        } catch (InvalidProtocolBufferException e) {
          throw new RuntimeException(e);
        }
        super.sendMessage(message);
      }

      private void attachToLastAllureStep(String name, String content) {
        Optional<String> currentTest = Allure.getLifecycle().getCurrentTestCase();
        if (currentTest.isPresent()) {
          Consumer<TestResult> updateStepAttachment = test -> {
            if (!test.getSteps().isEmpty()) {
              String source = UUID.randomUUID() + "-attachment.txt";
              Attachment attachment = new Attachment()
                  .setName(name)
                  .setType("text/plain")
                  .setSource(source);
              try (InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                Allure.getLifecycle().writeAttachment(source, stream);
              } catch (IOException e) {
                throw new RuntimeException("Failed to write attachment", e);
              }
              StepResult last = test.getSteps().getLast();
              List<Attachment> attachments = last.getAttachments();
              attachments.add(attachment);
            }
          };
          Allure.getLifecycle().updateTestCase(currentTest.get(), updateStepAttachment);
        }
      }
    };
  }
}
