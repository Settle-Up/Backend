package settleup.backend.global.Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import io.sentry.spring.jakarta.tracing.TransactionNameProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Component
public class CustomTransactionNameProvider implements TransactionNameProvider {
    @Override
    @Nullable
    public String provideTransactionName(@NotNull HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }
}