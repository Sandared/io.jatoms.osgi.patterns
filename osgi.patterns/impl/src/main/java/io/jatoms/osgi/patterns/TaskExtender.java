package io.jatoms.osgi.patterns;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.util.tracker.BundleTracker;

import io.jatoms.osgi.patterns.api.ITask;

public class TaskExtender extends BundleTracker<List<ServiceRegistration<ITask>>> {

    private Bundle trackerBundle;

    public TaskExtender(BundleContext context) {
        super(context, Bundle.ACTIVE, null);
        trackerBundle = context.getBundle();
    }

    @Override
    public List<ServiceRegistration<ITask>> addingBundle(Bundle bundle, BundleEvent event) {
        // not interested in ourselves
		if(bundle == trackerBundle)
			return Collections.EMPTY_LIST;

		BundleWiring wiring = bundle.adapt(BundleWiring.class);

		// should get us all wires that are created from import/export package headers
		List<BundleWire> wires = wiring.getRequiredWires(PackageNamespace.PACKAGE_NAMESPACE);

		for (BundleWire wire : wires) {

			// we are only interested in bundles that use a specific annotation,
			// so we only use the requirements not the capabilities it provides
			BundleRequirement requirement = wire.getRequirement();
			String filter = requirement.getDirectives().get(PackageNamespace.REQUIREMENT_FILTER_DIRECTIVE);

			if (filter != null) {

				// if this wire points to the package we track,
				// then this bundle is scanned for an ITask
				if (filter.contains(ITask.class.getPackage().getName())) {

					List<String> classNames = getBundleClassNames(bundle);

					// scan for classes that are actually ITasks
					List<Class<?>> tasks = getHandledClasses(bundle, classNames, ITask.class);

					if(!tasks.isEmpty()) {
                        // for each task we create an instance and register this instance as an ITask service,
                        // so it can be found by any ITask whiteboard implementation
                        List<ServiceRegistration<ITask>> registrations = new ArrayList<>();
                        for(Class<?> task : tasks){
                            ITask instance = null;
							try {
								instance = (ITask)task.newInstance();
                                registrations.add(context.registerService(ITask.class, instance, null));
							} catch (InstantiationException | IllegalAccessException e) {
								e.printStackTrace();
							}
                        }
                        return registrations;
					}
				}
			}
		}
		return Collections.EMPTY_LIST;
    }

    @Override
    public void removedBundle(Bundle bundle, BundleEvent event, List<ServiceRegistration<ITask>> registrations) {
        if(!registrations.isEmpty()){
            registrations.forEach(registration -> registration.unregister());
        }
        super.removedBundle(bundle, event, registrations);
    }

    // gets all classnames within a specific bundle and its fragment bundles
	private List<String> getBundleClassNames(Bundle bundle) {
		BundleWiring wiring = bundle.adapt(BundleWiring.class);

		// get all resources in the specific bundle and in its fragment bundles
		List<URL> resources = wiring.findEntries("/", "*.class", BundleWiring.FINDENTRIES_RECURSE);
		List<String> classNames = new ArrayList<String>();
		for (URL resource : resources) {
			String className = resource.getPath()
					// remove leading slash
					.substring(1, resource.getPath().length())
					// replace trailing .class
					.replace(".class", "")
					// replace all slashes
					.replace('/', '.');
			classNames.add(className);
		}
		return classNames;
	}

    // used to get all classes within a bundle that implement typesToHandle
	private List<Class<?>> getHandledClasses(Bundle bundle, List<String> classNames, Class<?> typeToHandle) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		for (String className :classNames) {
			try {
				Class<?> clazz = bundle.loadClass(className);
                if(typeToHandle.isAssignableFrom(clazz)) {
                    classes.add(clazz);
                }
			} catch (ClassNotFoundException | NoClassDefFoundError exception) {
				exception.printStackTrace();
			}
		}
		return classes;
	}
}