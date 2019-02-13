package io.jatoms.osgi.patterns;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component
public class TaskExtenderInitializer {

    private TaskExtender extender;

    @Activate
    void activate(BundleContext context) {
        extender = new TaskExtender(context);
        extender.open();
    }

    @Deactivate
    void deactivate() {
        extender.close();
    }

}