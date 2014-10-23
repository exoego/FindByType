import java.net.URL;

import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class Main {
    public static void main(String[] args) throws Exception {
        final Server server = new Server(Integer.valueOf(System.getenv("PORT")));

        RewriteHandler rewrite = new RewriteHandler();
        rewrite.setOriginalPathAttribute("requestedPath");

        RewriteRegexRule reverse = new RewriteRegexRule();
        reverse.setRegex("/q/([^/]*)");
        reverse.setReplacement("/index.html?q=$1");
        rewrite.addRule(reverse);

        final ResourceHandler resource_handler = newResourceHandler();

        // Adding handlers
        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{rewrite, resource_handler});

        // Start server
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    private static ResourceHandler newResourceHandler() {
        final URL rootUrl = ClassLoader.getSystemClassLoader().getResource("webapp/root");
        final String staticContentRoot = rootUrl.toExternalForm();
        System.out.printf("resources:%s%n", staticContentRoot);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setResourceBase(staticContentRoot);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        return resource_handler;
    }
}
